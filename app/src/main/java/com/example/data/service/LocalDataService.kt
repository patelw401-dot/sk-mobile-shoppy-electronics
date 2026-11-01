package com.example.data.service

import com.example.admin.model.CategoryRecord
import com.example.admin.model.CustomerRecord
import com.example.admin.model.ShopSettings
import com.example.data.storage.LocalStorageManager
import com.example.model.AppNotification
import com.example.model.CartItem
import com.example.model.CustomerReview
import com.example.model.MockData
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.ReelItem
import com.example.model.RepairBooking
import com.example.model.SellRequest
import com.example.model.ShopOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Concrete implementation of DataService using local persistent storage.
 * Designed to be easily replaced by Remote/Firebase/Supabase data service in future phases.
 */
class LocalDataService(
    private val storage: LocalStorageManager = LocalStorageManager.getInstance()
) : DataService {

    private val mutex = Mutex()

    private val _productsFlow = MutableStateFlow<List<Product>>(emptyList())
    private val _ordersFlow = MutableStateFlow<List<ShopOrder>>(emptyList())
    private val _repairsFlow = MutableStateFlow<List<RepairBooking>>(emptyList())
    private val _sellRequestsFlow = MutableStateFlow<List<SellRequest>>(emptyList())
    private val _reelsFlow = MutableStateFlow<List<ReelItem>>(emptyList())
    private val _categoriesFlow = MutableStateFlow<List<CategoryRecord>>(emptyList())

    private var categoriesCache = mutableListOf<CategoryRecord>()

    init {
        // Hydrate initial states from persistent storage
        _productsFlow.value = storage.loadProducts()
        _ordersFlow.value = storage.loadOrders()
        _repairsFlow.value = storage.loadRepairBookings()
        _sellRequestsFlow.value = storage.loadSellRequests()
        _reelsFlow.value = storage.loadReels()
        val loadedCategories = storage.loadCategories()
        categoriesCache = loadedCategories.toMutableList()
        _categoriesFlow.value = loadedCategories
    }

    // --- PRODUCTS ---
    override fun observeProducts(): Flow<List<Product>> = _productsFlow.asStateFlow()

    override suspend fun getProducts(): List<Product> = mutex.withLock {
        _productsFlow.value
    }

    override suspend fun getProductById(id: String): Product? = mutex.withLock {
        _productsFlow.value.find { it.id == id }
    }

    override suspend fun createProduct(product: Product): Result<Product> = mutex.withLock {
        if (product.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Product name cannot be empty"))
        }
        if (product.price <= 0) {
            return Result.failure(IllegalArgumentException("Price must be greater than zero"))
        }

        val updated = listOf(product) + _productsFlow.value
        _productsFlow.value = updated
        storage.saveProducts(updated)
        Result.success(product)
    }

    override suspend fun updateProduct(product: Product): Result<Product> = mutex.withLock {
        val existingIndex = _productsFlow.value.indexOfFirst { it.id == product.id }
        if (existingIndex == -1) {
            return Result.failure(NoSuchElementException("Product with id '${product.id}' not found"))
        }

        val updated = _productsFlow.value.toMutableList().apply {
            this[existingIndex] = product
        }
        _productsFlow.value = updated
        storage.saveProducts(updated)
        Result.success(product)
    }

    override suspend fun deleteProduct(id: String): Result<Boolean> = mutex.withLock {
        val originalSize = _productsFlow.value.size
        val updated = _productsFlow.value.filterNot { it.id == id }
        if (updated.size == originalSize) {
            return Result.failure(NoSuchElementException("Product with id '$id' not found"))
        }

        _productsFlow.value = updated
        storage.saveProducts(updated)
        Result.success(true)
    }

    // --- ORDERS ---
    override fun observeOrders(): Flow<List<ShopOrder>> = _ordersFlow.asStateFlow()

    override fun observeCustomerOrders(customerId: String): Flow<List<ShopOrder>> {
        return _ordersFlow.map { allOrders ->
            if (customerId.isBlank()) allOrders else allOrders.filter {
                it.customerUid.equals(customerId, ignoreCase = true) ||
                it.customerId.equals(customerId, ignoreCase = true)
            }
        }
    }

    override suspend fun getOrders(): List<ShopOrder> = mutex.withLock {
        _ordersFlow.value
    }

    override suspend fun getCustomerOrders(customerId: String): List<ShopOrder> = mutex.withLock {
        if (customerId.isBlank()) _ordersFlow.value else _ordersFlow.value.filter {
            it.customerUid.equals(customerId, ignoreCase = true) ||
            it.customerId.equals(customerId, ignoreCase = true)
        }
    }

    override suspend fun getOrderById(id: String): ShopOrder? = mutex.withLock {
        _ordersFlow.value.find { it.orderId == id }
    }

    override suspend fun createOrder(order: ShopOrder): Result<ShopOrder> = mutex.withLock {
        if (order.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot place order with empty cart"))
        }
        if (order.customerName.isBlank() || order.mobileNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name and phone number are required"))
        }

        val initialHistory = if (order.statusHistory.isEmpty()) {
            listOf(
                com.example.model.OrderStatusHistoryEntry(
                    status = order.status.ifBlank { "Order Placed" },
                    timestamp = System.currentTimeMillis(),
                    note = "Order placed successfully by customer",
                    updatedBy = "Customer"
                )
            )
        } else {
            order.statusHistory
        }

        val enrichedOrder = order.copy(
            statusHistory = initialHistory,
            lastUpdated = System.currentTimeMillis()
        )

        val updated = listOf(enrichedOrder) + _ordersFlow.value
        _ordersFlow.value = updated
        storage.saveOrders(updated)
        Result.success(enrichedOrder)
    }

    override suspend fun updateOrderStatus(id: String, status: String): Result<ShopOrder> {
        return updateOrderStatusWithHistory(id, status, note = "", updatedBy = "Admin")
    }

    override suspend fun updateOrderStatusWithHistory(
        id: String,
        status: String,
        note: String,
        updatedBy: String
    ): Result<ShopOrder> = mutex.withLock {
        val order = _ordersFlow.value.find { it.orderId == id }
            ?: return Result.failure(NoSuchElementException("Order with id '$id' not found"))

        val newHistoryEntry = com.example.model.OrderStatusHistoryEntry(
            status = status,
            timestamp = System.currentTimeMillis(),
            note = note.ifBlank { "Order status updated to $status" },
            updatedBy = updatedBy
        )

        val updatedHistory = order.statusHistory + newHistoryEntry
        val updatedOrder = order.copy(
            status = status,
            statusHistory = updatedHistory,
            lastUpdated = System.currentTimeMillis()
        )

        val updatedList = _ordersFlow.value.map { if (it.orderId == id) updatedOrder else it }
        _ordersFlow.value = updatedList
        storage.saveOrders(updatedList)

        val customerId = updatedOrder.customerUid.ifBlank { updatedOrder.customerId }
        if (customerId.isNotBlank()) {
            val notif = AppNotification.createOrderNotification(
                orderId = updatedOrder.orderId,
                customerId = customerId,
                status = status,
                orderDate = updatedOrder.orderDate
            )
            createNotificationInternal(notif)
        }
        Result.success(updatedOrder)
    }

    override suspend fun cancelOrder(id: String, reason: String, customerId: String): Result<ShopOrder> = mutex.withLock {
        val order = _ordersFlow.value.find { it.orderId == id }
            ?: return Result.failure(NoSuchElementException("Order with id '$id' not found"))

        // Authorization check: only order owner or admin can cancel
        val isOwner = order.customerUid.equals(customerId, ignoreCase = true) ||
                      order.customerId.equals(customerId, ignoreCase = true)
        if (!isOwner && customerId != "ADMIN") {
            return Result.failure(IllegalStateException("Unauthorized: You can only cancel your own orders."))
        }

        // State check: cannot cancel already delivered orders
        if (order.isDelivered) {
            return Result.failure(IllegalStateException("Delivered orders cannot be cancelled."))
        }

        val newHistoryEntry = com.example.model.OrderStatusHistoryEntry(
            status = "Cancelled",
            timestamp = System.currentTimeMillis(),
            note = if (reason.isNotBlank()) "Cancelled by customer: $reason" else "Cancelled by customer",
            updatedBy = if (isOwner) "Customer" else "Admin"
        )

        val updatedHistory = order.statusHistory + newHistoryEntry
        val updatedOrder = order.copy(
            status = "Cancelled",
            cancellationReason = reason,
            statusHistory = updatedHistory,
            lastUpdated = System.currentTimeMillis()
        )

        val updatedList = _ordersFlow.value.map { if (it.orderId == id) updatedOrder else it }
        _ordersFlow.value = updatedList
        storage.saveOrders(updatedList)

        val custId = updatedOrder.customerUid.ifBlank { updatedOrder.customerId }
        if (custId.isNotBlank()) {
            val notif = AppNotification.createOrderNotification(
                orderId = updatedOrder.orderId,
                customerId = custId,
                status = "Cancelled",
                orderDate = updatedOrder.orderDate
            )
            createNotificationInternal(notif)
        }

        Result.success(updatedOrder)
    }

    // --- REPAIRS ---
    override fun observeRepairBookings(): Flow<List<RepairBooking>> = _repairsFlow.asStateFlow()

    override suspend fun getRepairBookings(): List<RepairBooking> = mutex.withLock {
        _repairsFlow.value
    }

    override suspend fun getRepairBookingById(id: String): RepairBooking? = mutex.withLock {
        _repairsFlow.value.find { it.bookingId == id }
    }

    override suspend fun createRepairBooking(booking: RepairBooking): Result<RepairBooking> = mutex.withLock {
        if (booking.customerName.isBlank() || booking.mobileNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Name and mobile number are required"))
        }
        if (booking.brand.isBlank() || booking.problem.isBlank()) {
            return Result.failure(IllegalArgumentException("Device brand and problem description are required"))
        }

        val updated = listOf(booking) + _repairsFlow.value
        _repairsFlow.value = updated
        storage.saveRepairBookings(updated)
        Result.success(booking)
    }

    override suspend fun updateRepairStatus(
        id: String,
        status: String,
        cost: String?
    ): Result<RepairBooking> = mutex.withLock {
        val existing = _repairsFlow.value.find { it.bookingId == id }
            ?: return Result.failure(NoSuchElementException("Repair booking with id '$id' not found"))

        val updatedBooking = existing.copy(
            status = status,
            estimatedCost = cost ?: existing.estimatedCost
        )
        val updatedList = _repairsFlow.value.map { if (it.bookingId == id) updatedBooking else it }
        _repairsFlow.value = updatedList
        storage.saveRepairBookings(updatedList)

        val customerId = updatedBooking.customerUid.ifBlank { updatedBooking.customerId }
        if (customerId.isNotBlank()) {
            val notif = AppNotification.createRepairNotification(
                bookingId = updatedBooking.bookingId,
                customerId = customerId,
                status = status,
                brand = updatedBooking.brand,
                model = updatedBooking.model,
                cost = cost ?: updatedBooking.estimatedCost
            )
            createNotificationInternal(notif)
        }
        Result.success(updatedBooking)
    }

    // --- OLD PHONES ---
    override fun observeOldPhoneRequests(): Flow<List<SellRequest>> = _sellRequestsFlow.asStateFlow()

    override suspend fun getOldPhoneRequests(): List<SellRequest> = mutex.withLock {
        _sellRequestsFlow.value
    }

    override suspend fun createOldPhoneRequest(request: SellRequest): Result<SellRequest> = mutex.withLock {
        if (request.customerName.isBlank() || request.mobileNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Name and phone number are required"))
        }
        if (request.brand.isBlank() || request.model.isBlank()) {
            return Result.failure(IllegalArgumentException("Brand and model are required"))
        }

        val updated = listOf(request) + _sellRequestsFlow.value
        _sellRequestsFlow.value = updated
        storage.saveSellRequests(updated)
        Result.success(request)
    }

    override suspend fun updateOldPhoneRequest(
        id: String,
        status: String,
        offerMin: Int?,
        offerMax: Int?
    ): Result<SellRequest> = mutex.withLock {
        val existing = _sellRequestsFlow.value.find { it.requestId == id }
            ?: return Result.failure(NoSuchElementException("Request with id '$id' not found"))

        val updated = existing.copy(
            status = status,
            estimatedQuoteMin = offerMin ?: existing.estimatedQuoteMin,
            estimatedQuoteMax = offerMax ?: existing.estimatedQuoteMax
        )
        val list = _sellRequestsFlow.value.map { if (it.requestId == id) updated else it }
        _sellRequestsFlow.value = list
        storage.saveSellRequests(list)
        Result.success(updated)
    }

    // --- CUSTOMERS ---
    override suspend fun getCustomers(): List<CustomerRecord> = mutex.withLock {
        // Derive or load customer records
        listOf(
            CustomerRecord("CUST-1", "Sachin Patil", "9823456789", "Nagar Parishad Road, Udgir", 3, 1, 38500, "Today"),
            CustomerRecord("CUST-2", "Ganesh Kulkarni", "9890123456", "Shivaji Chowk, Udgir", 4, 0, 14200, "Today"),
            CustomerRecord("CUST-3", "Pooja Shinde", "9765432109", "Bidar Road, Udgir", 2, 2, 34800, "Yesterday"),
            CustomerRecord("CUST-4", "Imran Shaikh", "9988776655", "Nanded Naka, Udgir", 5, 1, 9800, "2 days ago")
        )
    }

    override suspend fun getCustomerById(id: String): CustomerRecord? = mutex.withLock {
        getCustomers().find { it.id == id }
    }

    // --- CART ---
    override suspend fun getCart(): List<CartItem> = mutex.withLock {
        storage.loadCart()
    }

    override suspend fun saveCart(items: List<CartItem>): Result<Unit> = mutex.withLock {
        storage.saveCart(items)
        Result.success(Unit)
    }

    override suspend fun clearCart(): Result<Unit> = mutex.withLock {
        storage.saveCart(emptyList())
        Result.success(Unit)
    }

    // --- REVIEWS, CATEGORIES & SETTINGS ---
    override suspend fun getReviews(): List<CustomerReview> = MockData.customerReviews

    override fun observeCategories(): Flow<List<CategoryRecord>> = _categoriesFlow.asStateFlow()

    override suspend fun getCategories(): List<CategoryRecord> = mutex.withLock {
        _categoriesFlow.value
    }

    override suspend fun saveCategory(category: CategoryRecord): Result<CategoryRecord> = mutex.withLock {
        val index = categoriesCache.indexOfFirst { it.id == category.id }
        if (index != -1) {
            categoriesCache[index] = category
        } else {
            categoriesCache.add(category)
        }
        val sorted = categoriesCache.sortedBy { it.displayOrder }
        categoriesCache = sorted.toMutableList()
        _categoriesFlow.value = sorted
        storage.saveCategories(sorted)
        Result.success(category)
    }

    override suspend fun toggleCategoryActive(categoryId: String): Result<CategoryRecord> = mutex.withLock {
        val index = categoriesCache.indexOfFirst { it.id == categoryId }
        if (index == -1) return Result.failure(NoSuchElementException("Category not found"))
        val cat = categoriesCache[index]
        val updated = cat.copy(isActive = !cat.isActive)
        categoriesCache[index] = updated
        _categoriesFlow.value = categoriesCache.toList()
        storage.saveCategories(categoriesCache.toList())
        Result.success(updated)
    }

    override suspend fun deleteCategory(categoryId: String, reassignToSlug: String?): Result<Boolean> = mutex.withLock {
        val cat = categoriesCache.find { it.id == categoryId }
        if (cat != null && !reassignToSlug.isNullOrBlank()) {
            val targetEnum = try { ProductCategory.valueOf(reassignToSlug.uppercase().replace("-", "_")) } catch (e: Exception) { ProductCategory.ACCESSORIES }
            val currentProds = _productsFlow.value.toMutableList()
            var modified = false
            for (i in 0 until currentProds.size) {
                val p = currentProds[i]
                if (p.categoryMatches(cat.slug) || p.category == cat.category) {
                    currentProds[i] = p.copy(category = targetEnum)
                    modified = true
                }
            }
            if (modified) {
                _productsFlow.value = currentProds
                storage.saveProducts(currentProds)
            }
        }
        categoriesCache.removeAll { it.id == categoryId }
        _categoriesFlow.value = categoriesCache.toList()
        storage.saveCategories(categoriesCache.toList())
        Result.success(true)
    }

    override suspend fun addCategory(name: String, slug: String): Result<CategoryRecord> = mutex.withLock {
        val newCat = CategoryRecord(
            id = "cat-${slug.ifBlank { System.currentTimeMillis().toString() }}",
            name = name,
            slug = slug,
            category = ProductCategory.ACCESSORIES,
            isActive = true,
            productCount = 0,
            displayOrder = categoriesCache.size + 1
        )
        categoriesCache.add(newCat)
        _categoriesFlow.value = categoriesCache.toList()
        storage.saveCategories(categoriesCache.toList())
        Result.success(newCat)
    }

    override suspend fun getSettings(): ShopSettings = mutex.withLock {
        storage.loadSettings()
    }

    override suspend fun saveSettings(settings: ShopSettings): Result<ShopSettings> = mutex.withLock {
        storage.saveSettings(settings)
        Result.success(settings)
    }

    // --- NOTIFICATIONS ---
    private val _notificationsFlowMap = mutableMapOf<String, MutableStateFlow<List<AppNotification>>>()

    private fun getOrCreateNotifFlow(customerId: String): MutableStateFlow<List<AppNotification>> {
        return _notificationsFlowMap.getOrPut(customerId) {
            MutableStateFlow(storage.loadNotifications(customerId))
        }
    }

    private fun createNotificationInternal(notification: AppNotification) {
        val customerId = notification.customerId
        if (customerId.isNotBlank()) {
            storage.saveNotification(notification)
            val list = storage.loadNotifications(customerId)
            getOrCreateNotifFlow(customerId).value = list
        }
    }

    override fun observeNotifications(customerId: String): Flow<List<AppNotification>> {
        return getOrCreateNotifFlow(customerId).asStateFlow()
    }

    override suspend fun getNotifications(customerId: String): List<AppNotification> = mutex.withLock {
        storage.loadNotifications(customerId)
    }

    override suspend fun createNotification(notification: AppNotification): Result<AppNotification> = mutex.withLock {
        createNotificationInternal(notification)
        Result.success(notification)
    }

    override suspend fun markNotificationAsRead(id: String, customerId: String): Result<Boolean> = mutex.withLock {
        val success = storage.markNotificationAsRead(id, customerId)
        if (success) {
            val list = storage.loadNotifications(customerId)
            getOrCreateNotifFlow(customerId).value = list
        }
        Result.success(success)
    }

    override suspend fun markAllNotificationsAsRead(customerId: String): Result<Boolean> = mutex.withLock {
        val success = storage.markAllNotificationsAsRead(customerId)
        if (success) {
            val list = storage.loadNotifications(customerId)
            getOrCreateNotifFlow(customerId).value = list
        }
        Result.success(success)
    }

    override suspend fun clearNotifications(customerId: String): Result<Boolean> = mutex.withLock {
        val success = storage.clearNotifications(customerId)
        if (success) {
            getOrCreateNotifFlow(customerId).value = emptyList()
        }
        Result.success(success)
    }

    // --- REELS ---
    override fun observeReels(): Flow<List<ReelItem>> = _reelsFlow.asStateFlow()

    override suspend fun getReels(): List<ReelItem> = mutex.withLock {
        storage.loadReels()
    }

    override suspend fun createReel(reel: ReelItem): Result<ReelItem> = mutex.withLock {
        val list = storage.loadReels().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == reel.id }
        if (existingIndex >= 0) {
            list[existingIndex] = reel
        } else {
            list.add(0, reel)
        }
        storage.saveReels(list)
        _reelsFlow.value = list
        Result.success(reel)
    }

    override suspend fun updateReel(reel: ReelItem): Result<ReelItem> = mutex.withLock {
        val list = storage.loadReels().toMutableList()
        val index = list.indexOfFirst { it.id == reel.id }
        if (index >= 0) {
            list[index] = reel
            storage.saveReels(list)
            _reelsFlow.value = list
            Result.success(reel)
        } else {
            Result.failure(NoSuchElementException("Reel not found: ${reel.id}"))
        }
    }

    override suspend fun deleteReel(id: String): Result<Boolean> = mutex.withLock {
        val list = storage.loadReels().toMutableList()
        val removed = list.removeAll { it.id == id }
        if (removed) {
            storage.saveReels(list)
            _reelsFlow.value = list
        }
        Result.success(removed)
    }

    override suspend fun toggleReelLike(id: String, increment: Boolean): Result<Unit> = mutex.withLock {
        val list = storage.loadReels().toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = list[index]
            val newCount = if (increment) item.likesCount + 1 else maxOf(0, item.likesCount - 1)
            list[index] = item.copy(likesCount = newCount, isLikedByMe = increment)
            storage.saveReels(list)
            _reelsFlow.value = list
        }
        Result.success(Unit)
    }

    override suspend fun incrementReelShare(id: String): Result<Unit> = mutex.withLock {
        val list = storage.loadReels().toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = list[index]
            list[index] = item.copy(sharesCount = item.sharesCount + 1)
            storage.saveReels(list)
            _reelsFlow.value = list
        }
        Result.success(Unit)
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalDataService? = null

        fun getInstance(storage: LocalStorageManager = LocalStorageManager.getInstance()): LocalDataService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalDataService(storage).also { INSTANCE = it }
            }
        }
    }
}
