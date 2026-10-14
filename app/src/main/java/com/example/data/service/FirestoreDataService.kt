package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.admin.auth.AdminAuthService
import com.example.admin.model.CategoryRecord
import com.example.admin.model.CustomerRecord
import com.example.admin.model.ShopSettings
import com.example.data.firebase.FirebaseConfig
import com.example.data.storage.LocalStorageManager
import com.example.model.AppNotification
import com.example.model.CartItem
import com.example.model.CustomerReview
import com.example.model.MockData
import com.example.model.NotificationType
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.RepairBooking
import com.example.model.SellRequest
import com.example.model.ShopOrder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreDataService(
    private val firestore: FirebaseFirestore,
    private val localStorage: LocalStorageManager = LocalStorageManager.getInstance()
) : DataService {

    companion object {
        private const val TAG = "FirestoreDataService"
        const val COLLECTION_PRODUCTS = "products"
        const val COLLECTION_ORDERS = "orders"
        const val COLLECTION_REPAIRS = "repairBookings"
        const val COLLECTION_OLD_PHONES = "oldPhoneRequests"
        const val COLLECTION_CUSTOMERS = "customers"
        const val COLLECTION_CATEGORIES = "categories"
        const val COLLECTION_REVIEWS = "reviews"
        const val COLLECTION_SETTINGS = "settings"
        const val COLLECTION_NOTIFICATIONS = "notifications"

        @Volatile
        private var instance: FirestoreDataService? = null

        fun getInstance(context: Context): FirestoreDataService {
            return instance ?: synchronized(this) {
                instance ?: FirestoreDataService(
                    FirebaseConfig.getFirestore(context.applicationContext)
                ).also { instance = it }
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // In-memory state flows for real-time reactivity
    private val _productsFlow = MutableStateFlow<List<Product>>(localStorage.loadProducts().ifEmpty { MockData.products })
    private val _ordersFlow = MutableStateFlow<List<ShopOrder>>(localStorage.loadOrders())
    private val _repairsFlow = MutableStateFlow<List<RepairBooking>>(localStorage.loadRepairBookings())
    private val _oldPhonesFlow = MutableStateFlow<List<SellRequest>>(localStorage.loadSellRequests())

    private var ordersListener: ListenerRegistration? = null
    private var repairsListener: ListenerRegistration? = null
    private var oldPhonesListener: ListenerRegistration? = null

    @Volatile
    private var isSeeding = false

    init {
        // Products collection has public read rule, so safe to listen without auth
        setupProductsListener()

        // Auth-gated snapshot listeners for admin collections (orders, repairs, oldPhones)
        try {
            val auth = FirebaseConfig.getAuth()
            auth.addAuthStateListener { fbAuth ->
                val user = fbAuth.currentUser
                if (user != null) {
                    serviceScope.launch {
                        val isAuthorized = AdminAuthService.verifyAdminAuthorization(user)
                        if (isAuthorized) {
                            // Authenticated as verified admin: attach admin listeners & seed products if empty
                            setupOrdersListener()
                            setupRepairsListener()
                            setupOldPhonesListener()
                            checkAndSeedProductsIfAdmin()
                        } else {
                            // Normal customer: detach admin listeners to prevent PERMISSION_DENIED
                            detachAdminListeners()
                        }
                    }
                } else {
                    // Unauthenticated: detach admin listeners to prevent PERMISSION_DENIED
                    detachAdminListeners()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Auth listener error: ${e.message}")
        }
    }

    private fun detachAdminListeners() {
        ordersListener?.remove()
        ordersListener = null
        repairsListener?.remove()
        repairsListener = null
        oldPhonesListener?.remove()
        oldPhonesListener = null
    }

    private suspend fun checkAndSeedProductsIfAdmin() {
        try {
            if (FirebaseConfig.getAuth().currentUser == null) return
            val snapshot = firestore.collection(COLLECTION_PRODUCTS).limit(1).get().await()
            if (snapshot.isEmpty) {
                seedDefaultProducts()
            }
        } catch (e: Exception) {
            Log.w(TAG, "checkAndSeedProductsIfAdmin error: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // PRODUCTS
    // -------------------------------------------------------------
    private fun setupProductsListener() {
        firestore.collection(COLLECTION_PRODUCTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Products listener failed", error)
                    if (_productsFlow.value.isEmpty()) {
                        _productsFlow.value = localStorage.loadProducts().ifEmpty { MockData.products }
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    if (snapshot.isEmpty) {
                        // Keep catalog populated locally for customers without attempting unauthorized writes to Cloud Firestore
                        _productsFlow.value = localStorage.loadProducts().ifEmpty { MockData.products }
                    } else {
                        val productList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val name = doc.getString("name") ?: return@mapNotNull null
                                val brand = doc.getString("brand") ?: "Generic"
                                val catStr = doc.getString("category") ?: "ALL"
                                val category = try {
                                    ProductCategory.valueOf(catStr)
                                } catch (e: Exception) {
                                    ProductCategory.entries.find { it.name.equals(catStr, ignoreCase = true) } ?: ProductCategory.MOBILE_PHONES
                                }
                                val price = doc.getLong("price")?.toInt() ?: 0
                                val originalPrice = doc.getLong("originalPrice")?.toInt() ?: price
                                val rating = doc.getDouble("rating")?.toFloat() ?: 4.5f
                                val reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0
                                val description = doc.getString("description") ?: ""
                                val stock = doc.getLong("stock")?.toInt() ?: 10
                                val sku = doc.getString("sku") ?: "SKM-$id"
                                val active = doc.getBoolean("active") ?: true
                                val featured = doc.getBoolean("featured") ?: false

                                Product(
                                    id = id,
                                    name = name,
                                    brand = brand,
                                    category = category,
                                    price = price,
                                    originalPrice = originalPrice,
                                    rating = rating,
                                    reviewCount = reviewCount,
                                    description = description,
                                    specifications = emptyMap(),
                                    stock = stock,
                                    sku = sku,
                                    active = active,
                                    featured = featured,
                                    isFeatured = featured
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _productsFlow.value = productList
                        localStorage.saveProducts(productList)
                    }
                }
            }
    }

    suspend fun seedDefaultProducts() {
        if (isSeeding) return
        isSeeding = true
        try {
            val batch = firestore.batch()
            MockData.products.forEach { prod ->
                val docRef = firestore.collection(COLLECTION_PRODUCTS).document(prod.id)
                val data = hashMapOf(
                    "id" to prod.id,
                    "name" to prod.name,
                    "brand" to prod.brand,
                    "category" to prod.category.name,
                    "price" to prod.price,
                    "originalPrice" to prod.originalPrice,
                    "rating" to prod.rating.toDouble(),
                    "reviewCount" to prod.reviewCount,
                    "description" to prod.description,
                    "stock" to prod.stock,
                    "sku" to prod.sku,
                    "active" to prod.active,
                    "featured" to prod.isFeatured,
                    "createdAt" to System.currentTimeMillis()
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            _productsFlow.value = MockData.products
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Seeding completed locally: ${e.message}")
            _productsFlow.value = localStorage.loadProducts().ifEmpty { MockData.products }
        }
    }

    override fun observeProducts(): Flow<List<Product>> = _productsFlow.asStateFlow()

    override suspend fun getProducts(): List<Product> {
        return try {
            val snapshot = firestore.collection(COLLECTION_PRODUCTS).get().await()
            if (snapshot.isEmpty) {
                if (FirebaseConfig.getAuth().currentUser != null) {
                    seedDefaultProducts()
                }
                _productsFlow.value.ifEmpty { MockData.products }
            } else {
                _productsFlow.value
            }
        } catch (e: Exception) {
            Log.w(TAG, "getProducts error, returning cache", e)
            _productsFlow.value.ifEmpty { localStorage.loadProducts().ifEmpty { MockData.products } }
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return _productsFlow.value.find { it.id == id } ?: try {
            val doc = firestore.collection(COLLECTION_PRODUCTS).document(id).get().await()
            if (doc.exists()) {
                val name = doc.getString("name") ?: ""
                val brand = doc.getString("brand") ?: ""
                val price = doc.getLong("price")?.toInt() ?: 0
                val originalPrice = doc.getLong("originalPrice")?.toInt() ?: price
                val catStr = doc.getString("category") ?: "ALL"
                val category = ProductCategory.entries.find { it.name.equals(catStr, true) } ?: ProductCategory.MOBILE_PHONES
                Product(
                    id = id,
                    name = name,
                    brand = brand,
                    category = category,
                    price = price,
                    originalPrice = originalPrice,
                    rating = doc.getDouble("rating")?.toFloat() ?: 4.5f,
                    reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                    description = doc.getString("description") ?: "",
                    specifications = emptyMap(),
                    stock = doc.getLong("stock")?.toInt() ?: 10,
                    sku = doc.getString("sku") ?: "SKM-$id"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createProduct(product: Product): Result<Product> {
        return try {
            val docRef = firestore.collection(COLLECTION_PRODUCTS).document(product.id)
            val data = hashMapOf(
                "id" to product.id,
                "name" to product.name,
                "brand" to product.brand,
                "category" to product.category.name,
                "price" to product.price,
                "originalPrice" to product.originalPrice,
                "rating" to product.rating.toDouble(),
                "reviewCount" to product.reviewCount,
                "description" to product.description,
                "stock" to product.stock,
                "sku" to product.sku,
                "active" to product.active,
                "featured" to product.isFeatured,
                "createdAt" to System.currentTimeMillis()
            )
            docRef.set(data).await()
            Result.success(product)
        } catch (e: Exception) {
            Log.e(TAG, "createProduct failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            val docRef = firestore.collection(COLLECTION_PRODUCTS).document(product.id)
            val updates = hashMapOf<String, Any>(
                "name" to product.name,
                "brand" to product.brand,
                "category" to product.category.name,
                "price" to product.price,
                "originalPrice" to product.originalPrice,
                "description" to product.description,
                "stock" to product.stock,
                "active" to product.active,
                "featured" to product.isFeatured
            )
            docRef.update(updates).await()
            Result.success(product)
        } catch (e: Exception) {
            Log.e(TAG, "updateProduct failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Boolean> {
        return try {
            firestore.collection(COLLECTION_PRODUCTS).document(id).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "deleteProduct failed", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // ORDERS
    // -------------------------------------------------------------
    private fun setupOrdersListener() {
        if (FirebaseConfig.getAuth().currentUser == null) return
        ordersListener?.remove()
        ordersListener = firestore.collection(COLLECTION_ORDERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Orders listener error", error)
                    if (_ordersFlow.value.isEmpty()) {
                        _ordersFlow.value = localStorage.loadOrders()
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orderList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val orderId = doc.getString("id") ?: doc.id
                            val customerName = doc.getString("customerName") ?: "Customer"
                            val mobileNumber = doc.getString("phone") ?: ""
                            val address = doc.getString("address") ?: ""
                            val subtotal = doc.getLong("subtotal")?.toInt() ?: 0
                            val deliveryFee = doc.getLong("deliveryCharge")?.toInt() ?: 0
                            val totalAmount = doc.getLong("total")?.toInt() ?: (subtotal + deliveryFee)
                            val deliveryOption = doc.getString("deliveryMethod") ?: "Standard Delivery"
                            val paymentMethod = doc.getString("paymentMethod") ?: "Cash on Delivery"
                            val status = doc.getString("orderStatus") ?: "Pending"
                            val orderDate = doc.getString("orderDate") ?: "Today"

                            // Reconstruct cart items if available
                            val rawItems = doc.get("items") as? List<Map<String, Any>>
                            val cartItems = rawItems?.mapNotNull { itemMap ->
                                val pName = itemMap["name"] as? String ?: return@mapNotNull null
                                val pPrice = (itemMap["price"] as? Number)?.toInt() ?: 0
                                val qty = (itemMap["quantity"] as? Number)?.toInt() ?: 1
                                val pId = itemMap["id"] as? String ?: "PROD-GEN"
                                val prod = _productsFlow.value.find { it.id == pId } ?: Product(
                                    id = pId,
                                    name = pName,
                                    brand = "SK Mobile",
                                    category = ProductCategory.MOBILE_PHONES,
                                    price = pPrice,
                                    originalPrice = pPrice,
                                    rating = 4.5f,
                                    reviewCount = 0,
                                    description = "",
                                    specifications = emptyMap()
                                )
                                CartItem(product = prod, quantity = qty)
                            } ?: emptyList()

                                val pGatewayRef = doc.getString("paymentGatewayRef").orEmpty()
                                val bSettlementStatus = doc.getString("bankSettlementStatus") ?: "Unsettled"
                                val idempKey = doc.getString("idempotencyKey").orEmpty()
                                val custUid = doc.getString("customerId").orEmpty()
                                val tStatus = doc.getString("paymentStatus").orEmpty()

                                ShopOrder(
                                    orderId = orderId,
                                    items = cartItems,
                                    subtotal = subtotal,
                                    discount = 0,
                                    deliveryFee = deliveryFee,
                                    totalAmount = totalAmount,
                                    customerName = customerName,
                                    mobileNumber = mobileNumber,
                                    address = address,
                                    deliveryOption = deliveryOption,
                                    paymentMethod = paymentMethod,
                                    orderDate = orderDate,
                                    status = status,
                                    customerUid = custUid,
                                    paymentGatewayRef = pGatewayRef,
                                    transactionStatus = tStatus,
                                    bankSettlementStatus = bSettlementStatus,
                                    idempotencyKey = idempKey
                                )
                            } catch (e: Exception) {
                                null
                            }
                    }
                    _ordersFlow.value = orderList
                    localStorage.saveOrders(orderList)
                }
            }
    }

    override fun observeOrders(): Flow<List<ShopOrder>> = _ordersFlow.asStateFlow()

    override suspend fun getOrders(): List<ShopOrder> = _ordersFlow.value

    override suspend fun getOrderById(id: String): ShopOrder? = _ordersFlow.value.find { it.id == id }

    override suspend fun createOrder(order: ShopOrder): Result<ShopOrder> {
        return try {
            val orderRef = firestore.collection(COLLECTION_ORDERS).document(order.orderId)
            val itemsData = order.items.map { item ->
                mapOf(
                    "id" to item.product.id,
                    "name" to item.product.name,
                    "price" to item.product.price,
                    "quantity" to item.quantity,
                    "color" to item.selectedColor
                )
            }

            val currentUid = try { FirebaseConfig.getAuth().currentUser?.uid } catch (e: Exception) { null }
            val effectiveCustomerId = if (order.customerUid.isNotBlank()) order.customerUid else (currentUid ?: order.customerId)

            val data = hashMapOf(
                "id" to order.orderId,
                "customerId" to effectiveCustomerId,
                "customerName" to order.customerName,
                "phone" to order.mobileNumber,
                "address" to order.address,
                "city" to order.city,
                "state" to order.state,
                "pinCode" to order.pinCode,
                "deliveryMethod" to order.deliveryOption,
                "paymentMethod" to order.paymentMethod,
                "paymentStatus" to order.paymentStatus,
                "paymentGatewayRef" to order.paymentGatewayRef,
                "bankSettlementStatus" to order.bankSettlementStatus,
                "idempotencyKey" to order.idempotencyKey,
                "orderStatus" to (if (order.status.isNotBlank()) order.status else "Pending"),
                "subtotal" to order.subtotal,
                "deliveryCharge" to order.deliveryFee,
                "total" to order.totalAmount,
                "orderDate" to order.orderDate,
                "items" to itemsData,
                "createdAt" to System.currentTimeMillis()
            )

            orderRef.set(data).await()

            // Update or create customer record
            updateCustomerStats(
                customerName = order.customerName,
                phone = order.mobileNumber,
                address = order.address,
                orderAmount = order.totalAmount,
                isOrder = true
            )

            Result.success(order)
        } catch (e: Exception) {
            Log.e(TAG, "createOrder failed, saving locally", e)
            // Fallback to local persistence
            val updated = listOf(order) + _ordersFlow.value
            _ordersFlow.value = updated
            localStorage.saveOrders(updated)
            Result.success(order)
        }
    }

    override suspend fun updateOrderStatus(id: String, status: String): Result<ShopOrder> {
        return try {
            val docRef = firestore.collection(COLLECTION_ORDERS).document(id)
            docRef.update(
                mapOf(
                    "orderStatus" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            val existing = _ordersFlow.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(status = status)
                val custId = updated.customerUid.ifBlank { updated.customerId }
                if (custId.isNotBlank()) {
                    val notif = AppNotification.createOrderNotification(updated.orderId, custId, status, updated.orderDate)
                    serviceScope.launch { createNotification(notif) }
                }
                Result.success(updated)
            } else {
                Result.failure(NoSuchElementException("Order $id not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateOrderStatus failed", e)
            val existing = _ordersFlow.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(status = status)
                val list = _ordersFlow.value.map { if (it.id == id) updated else it }
                _ordersFlow.value = list
                localStorage.saveOrders(list)
                val custId = updated.customerUid.ifBlank { updated.customerId }
                if (custId.isNotBlank()) {
                    val notif = AppNotification.createOrderNotification(updated.orderId, custId, status, updated.orderDate)
                    serviceScope.launch { createNotification(notif) }
                }
                Result.success(updated)
            } else {
                Result.failure(e)
            }
        }
    }

    // -------------------------------------------------------------
    // REPAIRS
    // -------------------------------------------------------------
    private fun setupRepairsListener() {
        if (FirebaseConfig.getAuth().currentUser == null) return
        repairsListener?.remove()
        repairsListener = firestore.collection(COLLECTION_REPAIRS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Repairs listener error", error)
                    if (_repairsFlow.value.isEmpty()) {
                        _repairsFlow.value = localStorage.loadRepairBookings()
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val repairList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val bookingId = doc.getString("id") ?: doc.id
                            val customerName = doc.getString("customerName") ?: ""
                            val mobileNumber = doc.getString("phone") ?: ""
                            val brand = doc.getString("brand") ?: ""
                            val model = doc.getString("model") ?: ""
                            val problem = doc.getString("problem") ?: ""
                            val pickupRequired = doc.getBoolean("pickupRequired") ?: false
                            val address = doc.getString("address") ?: ""
                            val preferredDate = doc.getString("preferredDate") ?: ""
                            val additionalDetails = doc.getString("notes") ?: ""
                            val status = doc.getString("repairStatus") ?: "Requested"
                            val estimatedCost = doc.getString("estimatedCost") ?: "₹499 - ₹1,499"
                            val timestamp = doc.getLong("createdAt") ?: System.currentTimeMillis()

                            RepairBooking(
                                bookingId = bookingId,
                                customerName = customerName,
                                mobileNumber = mobileNumber,
                                brand = brand,
                                model = model,
                                problem = problem,
                                pickupRequired = pickupRequired,
                                address = address,
                                preferredDate = preferredDate,
                                additionalDetails = additionalDetails,
                                status = status,
                                estimatedCost = estimatedCost,
                                bookingTimestamp = timestamp
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _repairsFlow.value = repairList
                    localStorage.saveRepairBookings(repairList)
                }
            }
    }

    override fun observeRepairBookings(): Flow<List<RepairBooking>> = _repairsFlow.asStateFlow()

    override suspend fun getRepairBookings(): List<RepairBooking> = _repairsFlow.value

    override suspend fun getRepairBookingById(id: String): RepairBooking? = _repairsFlow.value.find { it.id == id }

    override suspend fun createRepairBooking(booking: RepairBooking): Result<RepairBooking> {
        return try {
            val docRef = firestore.collection(COLLECTION_REPAIRS).document(booking.bookingId)
            val currentUid = try { FirebaseConfig.getAuth().currentUser?.uid } catch (e: Exception) { null }
            val effectiveCustomerId = if (booking.customerUid.isNotBlank()) booking.customerUid else (currentUid ?: booking.customerId)

            val data = hashMapOf(
                "id" to booking.bookingId,
                "customerId" to effectiveCustomerId,
                "customerName" to booking.customerName,
                "phone" to booking.mobileNumber,
                "brand" to booking.brand,
                "model" to booking.model,
                "problem" to booking.problem,
                "pickupRequired" to booking.pickupRequired,
                "address" to booking.address,
                "preferredDate" to booking.preferredDate,
                "estimatedCost" to booking.estimatedCost,
                "repairStatus" to (if (booking.status.isNotBlank()) booking.status else "Requested"),
                "notes" to booking.additionalDetails,
                "createdAt" to booking.bookingTimestamp
            )
            docRef.set(data).await()

            updateCustomerStats(
                customerName = booking.customerName,
                phone = booking.mobileNumber,
                address = booking.address,
                orderAmount = 0,
                isOrder = false
            )

            Result.success(booking)
        } catch (e: Exception) {
            Log.e(TAG, "createRepairBooking failed, saving locally", e)
            val updated = listOf(booking) + _repairsFlow.value
            _repairsFlow.value = updated
            localStorage.saveRepairBookings(updated)
            Result.success(booking)
        }
    }

    override suspend fun updateRepairStatus(id: String, status: String, cost: String?): Result<RepairBooking> {
        return try {
            val docRef = firestore.collection(COLLECTION_REPAIRS).document(id)
            val updates = mutableMapOf<String, Any>(
                "repairStatus" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            if (cost != null) updates["estimatedCost"] = cost
            docRef.update(updates).await()

            val existing = _repairsFlow.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(status = status, estimatedCost = cost ?: existing.estimatedCost)
                val custId = updated.customerUid.ifBlank { updated.customerId }
                if (custId.isNotBlank()) {
                    val notif = AppNotification.createRepairNotification(
                        updated.bookingId, custId, status, updated.brand, updated.model, cost ?: updated.estimatedCost
                    )
                    serviceScope.launch { createNotification(notif) }
                }
                Result.success(updated)
            } else {
                Result.failure(NoSuchElementException("Repair booking $id not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateRepairStatus failed", e)
            val existing = _repairsFlow.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(status = status, estimatedCost = cost ?: existing.estimatedCost)
                val list = _repairsFlow.value.map { if (it.id == id) updated else it }
                _repairsFlow.value = list
                localStorage.saveRepairBookings(list)
                val custId = updated.customerUid.ifBlank { updated.customerId }
                if (custId.isNotBlank()) {
                    val notif = AppNotification.createRepairNotification(
                        updated.bookingId, custId, status, updated.brand, updated.model, cost ?: updated.estimatedCost
                    )
                    serviceScope.launch { createNotification(notif) }
                }
                Result.success(updated)
            } else {
                Result.failure(e)
            }
        }
    }

    // -------------------------------------------------------------
    // OLD PHONE REQUESTS
    // -------------------------------------------------------------
    private fun setupOldPhonesListener() {
        if (FirebaseConfig.getAuth().currentUser == null) return
        oldPhonesListener?.remove()
        oldPhonesListener = firestore.collection(COLLECTION_OLD_PHONES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Old phones listener error", error)
                    if (_oldPhonesFlow.value.isEmpty()) {
                        _oldPhonesFlow.value = localStorage.loadSellRequests()
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val customerName = doc.getString("customerName") ?: ""
                            val phone = doc.getString("phone") ?: ""
                            val brand = doc.getString("brand") ?: ""
                            val model = doc.getString("model") ?: ""
                            val storage = doc.getString("storage") ?: ""
                            val condition = doc.getString("condition") ?: ""
                            val expectedPrice = doc.getString("expectedPrice") ?: "₹0"
                            val address = doc.getString("address") ?: ""
                            val minQuote = doc.getLong("minQuote")?.toInt() ?: 4000
                            val maxQuote = doc.getLong("maxQuote")?.toInt() ?: 6000
                            val status = doc.getString("status") ?: "Valuation Generated"
                            val timestamp = doc.getLong("createdAt") ?: System.currentTimeMillis()

                            SellRequest(
                                requestId = id,
                                customerName = customerName,
                                mobileNumber = phone,
                                brand = brand,
                                model = model,
                                storage = storage,
                                condition = condition,
                                expectedPrice = expectedPrice,
                                address = address,
                                estimatedQuoteMin = minQuote,
                                estimatedQuoteMax = maxQuote,
                                status = status,
                                timestamp = timestamp
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _oldPhonesFlow.value = requests
                    localStorage.saveSellRequests(requests)
                }
            }
    }

    override fun observeOldPhoneRequests(): Flow<List<SellRequest>> = _oldPhonesFlow.asStateFlow()

    override suspend fun getOldPhoneRequests(): List<SellRequest> = _oldPhonesFlow.value

    override suspend fun createOldPhoneRequest(request: SellRequest): Result<SellRequest> {
        return try {
            val docRef = firestore.collection(COLLECTION_OLD_PHONES).document(request.requestId)
            val currentUid = try { FirebaseConfig.getAuth().currentUser?.uid } catch (e: Exception) { null }
            val effectiveCustomerId = if (request.customerUid.isNotBlank()) request.customerUid else (currentUid ?: request.customerId)

            val data = hashMapOf(
                "id" to request.requestId,
                "customerId" to effectiveCustomerId,
                "customerName" to request.customerName,
                "phone" to request.mobileNumber,
                "brand" to request.brand,
                "model" to request.model,
                "storage" to request.storage,
                "condition" to request.condition,
                "expectedPrice" to request.expectedPrice,
                "address" to request.address,
                "minQuote" to request.estimatedQuoteMin,
                "maxQuote" to request.estimatedQuoteMax,
                "status" to request.status,
                "createdAt" to request.timestamp
            )
            docRef.set(data).await()
            Result.success(request)
        } catch (e: Exception) {
            Log.e(TAG, "createOldPhoneRequest failed, saving locally", e)
            val updated = listOf(request) + _oldPhonesFlow.value
            _oldPhonesFlow.value = updated
            localStorage.saveSellRequests(updated)
            Result.success(request)
        }
    }

    override suspend fun updateOldPhoneRequest(
        id: String,
        status: String,
        offerMin: Int?,
        offerMax: Int?
    ): Result<SellRequest> {
        return try {
            val docRef = firestore.collection(COLLECTION_OLD_PHONES).document(id)
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            if (offerMin != null) updates["minQuote"] = offerMin
            if (offerMax != null) updates["maxQuote"] = offerMax
            docRef.update(updates).await()

            val existing = _oldPhonesFlow.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(
                    status = status,
                    estimatedQuoteMin = offerMin ?: existing.estimatedQuoteMin,
                    estimatedQuoteMax = offerMax ?: existing.estimatedQuoteMax
                )
                Result.success(updated)
            } else {
                Result.failure(NoSuchElementException("Sell request $id not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateOldPhoneRequest failed", e)
            val existing = _oldPhonesFlow.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(status = status)
                val list = _oldPhonesFlow.value.map { if (it.id == id) updated else it }
                _oldPhonesFlow.value = list
                localStorage.saveSellRequests(list)
                Result.success(updated)
            } else {
                Result.failure(e)
            }
        }
    }

    // -------------------------------------------------------------
    // CUSTOMERS
    // -------------------------------------------------------------
    private suspend fun updateCustomerStats(
        customerName: String,
        phone: String,
        address: String,
        orderAmount: Int,
        isOrder: Boolean
    ) {
        val currentUser = try { FirebaseConfig.getAuth().currentUser } catch (e: Exception) { null }
        if (phone.isBlank() || currentUser == null) return
        try {
            val customerId = currentUser.uid
            val docRef = firestore.collection(COLLECTION_CUSTOMERS).document(customerId)
            val doc = docRef.get().await()

            if (doc.exists()) {
                val existingOrders = doc.getLong("totalOrders")?.toInt() ?: 0
                val existingRepairs = doc.getLong("totalRepairs")?.toInt() ?: 0
                val existingSpent = doc.getLong("totalSpent")?.toInt() ?: 0

                val updates = hashMapOf<String, Any>(
                    "totalOrders" to (if (isOrder) existingOrders + 1 else existingOrders),
                    "totalRepairs" to (if (!isOrder) existingRepairs + 1 else existingRepairs),
                    "totalSpent" to (existingSpent + orderAmount),
                    "lastActivity" to "Today"
                )
                docRef.update(updates).await()
            } else {
                val newCustomer = hashMapOf(
                    "id" to customerId,
                    "name" to customerName,
                    "phone" to phone,
                    "address" to address,
                    "totalOrders" to (if (isOrder) 1 else 0),
                    "totalRepairs" to (if (!isOrder) 1 else 0),
                    "totalSpent" to orderAmount,
                    "createdAt" to System.currentTimeMillis(),
                    "lastActivity" to "Today"
                )
                docRef.set(newCustomer).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update customer stats in Firestore", e)
        }
    }

    override suspend fun getCustomers(): List<CustomerRecord> {
        return try {
            val snapshot = firestore.collection(COLLECTION_CUSTOMERS).get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: "Customer"
                    val phone = doc.getString("phone") ?: ""
                    val address = doc.getString("address") ?: ""
                    val orders = doc.getLong("totalOrders")?.toInt() ?: 0
                    val repairs = doc.getLong("totalRepairs")?.toInt() ?: 0
                    val spent = doc.getLong("totalSpent")?.toInt() ?: 0
                    val last = doc.getString("lastActivity") ?: "Recent"

                    CustomerRecord(
                        id = id,
                        name = name,
                        mobileNumber = phone,
                        address = address,
                        totalOrders = orders,
                        totalRepairs = repairs,
                        totalSpending = spent,
                        lastActivity = last
                    )
                }
            } else {
                // Return default customer records
                listOf(
                    CustomerRecord("CUST-1", "Sachin Patil", "9823456789", "Nagar Parishad Road, Udgir", 3, 1, 38500, "Today"),
                    CustomerRecord("CUST-2", "Ganesh Kulkarni", "9890123456", "Shivaji Chowk, Udgir", 4, 0, 14200, "Today"),
                    CustomerRecord("CUST-3", "Pooja Shinde", "9765432109", "Bidar Road, Udgir", 2, 2, 34800, "Yesterday"),
                    CustomerRecord("CUST-4", "Imran Shaikh", "9988776655", "Nanded Naka, Udgir", 5, 1, 9800, "2 days ago")
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "getCustomers error, returning mock list", e)
            listOf(
                CustomerRecord("CUST-1", "Sachin Patil", "9823456789", "Nagar Parishad Road, Udgir", 3, 1, 38500, "Today"),
                CustomerRecord("CUST-2", "Ganesh Kulkarni", "9890123456", "Shivaji Chowk, Udgir", 4, 0, 14200, "Today"),
                CustomerRecord("CUST-3", "Pooja Shinde", "9765432109", "Bidar Road, Udgir", 2, 2, 34800, "Yesterday"),
                CustomerRecord("CUST-4", "Imran Shaikh", "9988776655", "Nanded Naka, Udgir", 5, 1, 9800, "2 days ago")
            )
        }
    }

    override suspend fun getCustomerById(id: String): CustomerRecord? {
        return getCustomers().find { it.id == id }
    }

    // -------------------------------------------------------------
    // CART (Local Storage)
    // -------------------------------------------------------------
    override suspend fun getCart(): List<CartItem> = localStorage.loadCart()

    override suspend fun saveCart(items: List<CartItem>): Result<Unit> {
        localStorage.saveCart(items)
        return Result.success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> {
        localStorage.clearCart()
        return Result.success(Unit)
    }

    // -------------------------------------------------------------
    // REVIEWS, CATEGORIES & SETTINGS
    // -------------------------------------------------------------
    override suspend fun getReviews(): List<CustomerReview> {
        return MockData.customerReviews
    }

    override suspend fun getCategories(): List<CategoryRecord> {
        return listOf(
            CategoryRecord("CAT-1", "Mobile Phones", "mobile-phones", ProductCategory.MOBILE_PHONES, true, 4),
            CategoryRecord("CAT-2", "Old Phones", "old-phones", ProductCategory.OLD_PHONES, true, 3),
            CategoryRecord("CAT-3", "Mobile Repair", "mobile-repair", ProductCategory.REPAIR_SERVICES, true, 6),
            CategoryRecord("CAT-4", "Chargers", "chargers", ProductCategory.CHARGERS, true, 3),
            CategoryRecord("CAT-5", "Data Cables", "data-cables", ProductCategory.DATA_CABLES, true, 3),
            CategoryRecord("CAT-6", "Headphones", "headphones", ProductCategory.HEADPHONES, true, 2),
            CategoryRecord("CAT-7", "Bluetooth", "bluetooth", ProductCategory.BLUETOOTH, true, 3),
            CategoryRecord("CAT-8", "Accessories", "accessories", ProductCategory.ACCESSORIES, true, 3)
        )
    }

    override suspend fun toggleCategoryActive(categoryId: String): Result<CategoryRecord> {
        val cat = getCategories().find { it.id == categoryId }
            ?: return Result.failure(NoSuchElementException("Category not found"))
        return Result.success(cat.copy(isActive = !cat.isActive))
    }

    override suspend fun addCategory(name: String, slug: String): Result<CategoryRecord> {
        val newCat = CategoryRecord(
            id = "CAT-${UUID.randomUUID().toString().take(4)}",
            name = name,
            slug = slug,
            category = ProductCategory.ACCESSORIES,
            isActive = true,
            productCount = 0
        )
        return Result.success(newCat)
    }

    override suspend fun getSettings(): ShopSettings {
        return try {
            val doc = firestore.collection(COLLECTION_SETTINGS).document("store_config").get().await()
            if (doc.exists()) {
                ShopSettings(
                    shopName = doc.getString("shopName") ?: "SK Mobile",
                    address = doc.getString("address") ?: "Nagar Parishad, Udgir, Maharashtra - 413517",
                    phoneNumber = doc.getString("phoneNumber") ?: "+91 98765 43210",
                    whatsappNumber = doc.getString("whatsappNumber") ?: "+91 98765 43210",
                    openingTime = doc.getString("openingTime") ?: "09:30 AM",
                    closingTime = doc.getString("closingTime") ?: "09:30 PM",
                    pickupAvailable = doc.getBoolean("pickupAvailable") ?: true,
                    homeDeliveryAvailable = doc.getBoolean("homeDeliveryAvailable") ?: true,
                    deliveryCharge = doc.getLong("deliveryCharge")?.toInt() ?: 49,
                    freeDeliveryThreshold = doc.getLong("freeDeliveryThreshold")?.toInt() ?: 499,
                    upiEnabled = doc.getBoolean("upiEnabled") ?: true,
                    codEnabled = doc.getBoolean("codEnabled") ?: true,
                    onlinePaymentEnabled = doc.getBoolean("onlinePaymentEnabled") ?: true
                )
            } else {
                ShopSettings()
            }
        } catch (e: Exception) {
            ShopSettings()
        }
    }

    override suspend fun saveSettings(settings: ShopSettings): Result<ShopSettings> {
        return try {
            val docRef = firestore.collection(COLLECTION_SETTINGS).document("store_config")
            val data = hashMapOf(
                "shopName" to settings.shopName,
                "address" to settings.address,
                "phoneNumber" to settings.phoneNumber,
                "whatsappNumber" to settings.whatsappNumber,
                "openingTime" to settings.openingTime,
                "closingTime" to settings.closingTime,
                "pickupAvailable" to settings.pickupAvailable,
                "homeDeliveryAvailable" to settings.homeDeliveryAvailable,
                "deliveryCharge" to settings.deliveryCharge,
                "freeDeliveryThreshold" to settings.freeDeliveryThreshold,
                "upiEnabled" to settings.upiEnabled,
                "codEnabled" to settings.codEnabled,
                "onlinePaymentEnabled" to settings.onlinePaymentEnabled
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(settings)
        } catch (e: Exception) {
            Log.e(TAG, "saveSettings failed", e)
            Result.success(settings)
        }
    }

    // -------------------------------------------------------------
    // NOTIFICATIONS
    // -------------------------------------------------------------
    override fun observeNotifications(customerId: String): Flow<List<AppNotification>> = callbackFlow {
        if (customerId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        trySend(localStorage.loadNotifications(customerId))

        val listener = firestore.collection(COLLECTION_CUSTOMERS)
            .document(customerId)
            .collection(COLLECTION_NOTIFICATIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Observe notifications listener error", error)
                    trySend(localStorage.loadNotifications(customerId))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val title = doc.getString("title").orEmpty()
                            val message = doc.getString("message").orEmpty()
                            val typeStr = doc.getString("type") ?: NotificationType.ORDER.name
                            val type = try { NotificationType.valueOf(typeStr) } catch (e: Exception) { NotificationType.ORDER }
                            val referenceId = doc.getString("referenceId").orEmpty()
                            val status = doc.getString("status").orEmpty()
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val isRead = doc.getBoolean("isRead") ?: false

                            AppNotification(
                                id = id,
                                customerId = customerId,
                                title = title,
                                message = message,
                                type = type,
                                referenceId = referenceId,
                                status = status,
                                timestamp = timestamp,
                                isRead = isRead
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.timestamp }

                    for (notif in list) {
                        localStorage.saveNotification(notif)
                    }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getNotifications(customerId: String): List<AppNotification> {
        if (customerId.isBlank()) return emptyList()
        return try {
            val snapshot = firestore.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .collection(COLLECTION_NOTIFICATIONS)
                .get().await()

            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val title = doc.getString("title").orEmpty()
                    val message = doc.getString("message").orEmpty()
                    val typeStr = doc.getString("type") ?: NotificationType.ORDER.name
                    val type = try { NotificationType.valueOf(typeStr) } catch (e: Exception) { NotificationType.ORDER }
                    val referenceId = doc.getString("referenceId").orEmpty()
                    val status = doc.getString("status").orEmpty()
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val isRead = doc.getBoolean("isRead") ?: false

                    AppNotification(
                        id = id,
                        customerId = customerId,
                        title = title,
                        message = message,
                        type = type,
                        referenceId = referenceId,
                        status = status,
                        timestamp = timestamp,
                        isRead = isRead
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.timestamp }

            for (notif in list) {
                localStorage.saveNotification(notif)
            }
            list.ifEmpty { localStorage.loadNotifications(customerId) }
        } catch (e: Exception) {
            Log.w(TAG, "getNotifications failed, reading local", e)
            localStorage.loadNotifications(customerId)
        }
    }

    override suspend fun createNotification(notification: AppNotification): Result<AppNotification> {
        val customerId = notification.customerId
        if (customerId.isBlank()) return Result.failure(IllegalArgumentException("Customer ID required"))
        localStorage.saveNotification(notification)
        return try {
            val docRef = firestore.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .collection(COLLECTION_NOTIFICATIONS)
                .document(notification.id)

            val data = hashMapOf(
                "id" to notification.id,
                "customerId" to customerId,
                "title" to notification.title,
                "message" to notification.message,
                "type" to notification.type.name,
                "referenceId" to notification.referenceId,
                "status" to notification.status,
                "timestamp" to notification.timestamp,
                "isRead" to notification.isRead
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write notification to Firestore, cached locally", e)
            Result.success(notification)
        }
    }

    override suspend fun markNotificationAsRead(id: String, customerId: String): Result<Boolean> {
        if (customerId.isBlank()) return Result.failure(IllegalArgumentException("Customer ID required"))
        localStorage.markNotificationAsRead(id, customerId)
        return try {
            val docRef = firestore.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .collection(COLLECTION_NOTIFICATIONS)
                .document(id)
            docRef.update("isRead", true).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update notification read state in Firestore", e)
            Result.success(true)
        }
    }

    override suspend fun markAllNotificationsAsRead(customerId: String): Result<Boolean> {
        if (customerId.isBlank()) return Result.failure(IllegalArgumentException("Customer ID required"))
        localStorage.markAllNotificationsAsRead(customerId)
        return try {
            val snapshot = firestore.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("isRead", false)
                .get().await()

            for (doc in snapshot.documents) {
                doc.reference.update("isRead", true).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to mark all notifications read in Firestore", e)
            Result.success(true)
        }
    }

    override suspend fun clearNotifications(customerId: String): Result<Boolean> {
        if (customerId.isBlank()) return Result.failure(IllegalArgumentException("Customer ID required"))
        localStorage.clearNotifications(customerId)
        return try {
            val snapshot = firestore.collection(COLLECTION_CUSTOMERS)
                .document(customerId)
                .collection(COLLECTION_NOTIFICATIONS)
                .get().await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear notifications in Firestore", e)
            Result.success(true)
        }
    }
}
