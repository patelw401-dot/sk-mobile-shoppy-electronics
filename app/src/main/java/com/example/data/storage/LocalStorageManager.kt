package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.admin.model.CategoryRecord
import com.example.admin.model.CustomerRecord
import com.example.admin.model.ShopSettings
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
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local storage manager providing persistent client-side storage for demo orders,
 * repair bookings, sell requests, cart items, and product catalog updates.
 * Safely falls back to in-memory cache if Android Context is unavailable.
 */
class LocalStorageManager(private val context: Context? = null) {

    private val prefs: SharedPreferences? by lazy {
        try {
            context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize SharedPreferences, falling back to memory: ${e.message}")
            null
        }
    }

    // In-memory fallbacks / caches
    private var cachedProducts: MutableList<Product>? = null
    private var cachedOrders: MutableList<ShopOrder>? = null
    private var cachedRepairs: MutableList<RepairBooking>? = null
    private var cachedSellRequests: MutableList<SellRequest>? = null
    private var cachedCart: MutableList<CartItem>? = null
    private var cachedSettings: ShopSettings? = null

    // ==========================================
    // PRODUCTS
    // ==========================================
    @Synchronized
    fun loadProducts(): List<Product> {
        cachedProducts?.let { return it.toList() }

        val jsonString = prefs?.getString(KEY_PRODUCTS, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<Product>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(jsonToProduct(obj))
                }
                if (list.isNotEmpty()) {
                    cachedProducts = list
                    return list
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing products: ${e.message}")
            }
        }

        // Initialize with MockData products
        val defaults = MockData.products.toMutableList()
        cachedProducts = defaults
        saveProducts(defaults)
        return defaults
    }

    @Synchronized
    fun saveProducts(products: List<Product>): Boolean {
        cachedProducts = products.toMutableList()
        return try {
            val array = JSONArray()
            for (p in products) {
                array.put(productToJson(p))
            }
            prefs?.edit()?.putString(KEY_PRODUCTS, array.toString())?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving products to local storage: ${e.message}")
            false
        }
    }

    // ==========================================
    // ORDERS
    // ==========================================
    @Synchronized
    fun loadOrders(): List<ShopOrder> {
        cachedOrders?.let { return it.toList() }

        val jsonString = prefs?.getString(KEY_ORDERS, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<ShopOrder>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(jsonToOrder(obj))
                }
                cachedOrders = list
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing orders: ${e.message}")
            }
        }

        // Default initial seed orders
        val seed = defaultSeedOrders()
        cachedOrders = seed.toMutableList()
        saveOrders(seed)
        return seed
    }

    @Synchronized
    fun saveOrders(orders: List<ShopOrder>): Boolean {
        cachedOrders = orders.toMutableList()
        return try {
            val array = JSONArray()
            for (order in orders) {
                array.put(orderToJson(order))
            }
            prefs?.edit()?.putString(KEY_ORDERS, array.toString())?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving orders: ${e.message}")
            false
        }
    }

    // ==========================================
    // REPAIR BOOKINGS
    // ==========================================
    @Synchronized
    fun loadRepairBookings(): List<RepairBooking> {
        cachedRepairs?.let { return it.toList() }

        val jsonString = prefs?.getString(KEY_REPAIRS, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<RepairBooking>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(jsonToRepairBooking(obj))
                }
                cachedRepairs = list
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing repairs: ${e.message}")
            }
        }

        val seed = defaultSeedRepairs()
        cachedRepairs = seed.toMutableList()
        saveRepairBookings(seed)
        return seed
    }

    @Synchronized
    fun saveRepairBookings(repairs: List<RepairBooking>): Boolean {
        cachedRepairs = repairs.toMutableList()
        return try {
            val array = JSONArray()
            for (r in repairs) {
                array.put(repairBookingToJson(r))
            }
            prefs?.edit()?.putString(KEY_REPAIRS, array.toString())?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving repair bookings: ${e.message}")
            false
        }
    }

    // ==========================================
    // SELL REQUESTS (OLD PHONES)
    // ==========================================
    @Synchronized
    fun loadSellRequests(): List<SellRequest> {
        cachedSellRequests?.let { return it.toList() }

        val jsonString = prefs?.getString(KEY_SELL_REQUESTS, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<SellRequest>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(jsonToSellRequest(obj))
                }
                cachedSellRequests = list
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing sell requests: ${e.message}")
            }
        }

        val seed = defaultSeedSellRequests()
        cachedSellRequests = seed.toMutableList()
        saveSellRequests(seed)
        return seed
    }

    @Synchronized
    fun saveSellRequests(requests: List<SellRequest>): Boolean {
        cachedSellRequests = requests.toMutableList()
        return try {
            val array = JSONArray()
            for (req in requests) {
                array.put(sellRequestToJson(req))
            }
            prefs?.edit()?.putString(KEY_SELL_REQUESTS, array.toString())?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sell requests: ${e.message}")
            false
        }
    }

    // ==========================================
    // CART
    // ==========================================
    @Synchronized
    fun loadCart(): List<CartItem> {
        cachedCart?.let { return it.toList() }

        val jsonString = prefs?.getString(KEY_CART, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<CartItem>()
                val allProducts = loadProducts().associateBy { it.id }
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val productId = obj.getString("productId")
                    val quantity = obj.optInt("quantity", 1)
                    val color = obj.optString("color", "Default")
                    val product = allProducts[productId] ?: jsonToProduct(obj.getJSONObject("product"))
                    list.add(CartItem(product, quantity, color))
                }
                cachedCart = list
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing cart: ${e.message}")
            }
        }
        return emptyList()
    }

    @Synchronized
    fun saveCart(cartItems: List<CartItem>): Boolean {
        cachedCart = cartItems.toMutableList()
        return try {
            val array = JSONArray()
            for (item in cartItems) {
                val obj = JSONObject()
                obj.put("productId", item.product.id)
                obj.put("quantity", item.quantity)
                obj.put("color", item.selectedColor)
                obj.put("product", productToJson(item.product))
                array.put(obj)
            }
            prefs?.edit()?.putString(KEY_CART, array.toString())?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cart: ${e.message}")
            false
        }
    }

    @Synchronized
    fun clearCart(): Boolean {
        cachedCart = mutableListOf()
        return try {
            prefs?.edit()?.remove(KEY_CART)?.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==========================================
    // SETTINGS
    // ==========================================
    @Synchronized
    fun loadSettings(): ShopSettings {
        cachedSettings?.let { return it }

        val jsonString = prefs?.getString(KEY_SETTINGS, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val obj = JSONObject(jsonString)
                val s = ShopSettings(
                    shopName = obj.optString("shopName", "SK Mobile"),
                    address = obj.optString("address", "Nagar Parishad, Udgir, Maharashtra - 413517"),
                    phoneNumber = obj.optString("phoneNumber", "+91 98765 43210"),
                    whatsappNumber = obj.optString("whatsappNumber", "+91 98765 43210"),
                    openingTime = obj.optString("openingTime", "09:30 AM"),
                    closingTime = obj.optString("closingTime", "09:30 PM"),
                    pickupAvailable = obj.optBoolean("pickupAvailable", true),
                    homeDeliveryAvailable = obj.optBoolean("homeDeliveryAvailable", true),
                    deliveryCharge = obj.optInt("deliveryCharge", 49),
                    freeDeliveryThreshold = obj.optInt("freeDeliveryThreshold", 499),
                    upiEnabled = obj.optBoolean("upiEnabled", true),
                    codEnabled = obj.optBoolean("codEnabled", true),
                    onlinePaymentEnabled = obj.optBoolean("onlinePaymentEnabled", true)
                )
                cachedSettings = s
                return s
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing settings: ${e.message}")
            }
        }
        val def = ShopSettings()
        cachedSettings = def
        return def
    }

    @Synchronized
    fun saveSettings(settings: ShopSettings): Boolean {
        cachedSettings = settings
        return try {
            val obj = JSONObject()
            obj.put("shopName", settings.shopName)
            obj.put("address", settings.address)
            obj.put("phoneNumber", settings.phoneNumber)
            obj.put("whatsappNumber", settings.whatsappNumber)
            obj.put("openingTime", settings.openingTime)
            obj.put("closingTime", settings.closingTime)
            obj.put("pickupAvailable", settings.pickupAvailable)
            obj.put("homeDeliveryAvailable", settings.homeDeliveryAvailable)
            obj.put("deliveryCharge", settings.deliveryCharge)
            obj.put("freeDeliveryThreshold", settings.freeDeliveryThreshold)
            obj.put("upiEnabled", settings.upiEnabled)
            obj.put("codEnabled", settings.codEnabled)
            obj.put("onlinePaymentEnabled", settings.onlinePaymentEnabled)
            prefs?.edit()?.putString(KEY_SETTINGS, obj.toString())?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving settings: ${e.message}")
            false
        }
    }

    // ==========================================
    // JSON HELPERS
    // ==========================================
    private fun productToJson(p: Product): JSONObject {
        val obj = JSONObject()
        obj.put("id", p.id)
        obj.put("name", p.name)
        obj.put("brand", p.brand)
        obj.put("category", p.category.name)
        obj.put("price", p.price)
        obj.put("originalPrice", p.originalPrice)
        obj.put("rating", p.rating.toDouble())
        obj.put("reviewCount", p.reviewCount)
        obj.put("description", p.description)
        obj.put("inStock", p.inStock)
        obj.put("stockStatus", p.stockStatus)
        obj.put("isFeatured", p.isFeatured)
        obj.put("isBestSeller", p.isBestSeller)
        obj.put("isPreOwned", p.isPreOwned)
        p.conditionGrade?.let { obj.put("conditionGrade", it) }
        p.batteryHealth?.let { obj.put("batteryHealth", it) }
        obj.put("warranty", p.warranty)
        obj.put("deliveryInfo", p.deliveryInfo)
        p.highlightBadge?.let { obj.put("highlightBadge", it) }
        obj.put("releaseOrder", p.releaseOrder)
        obj.put("stock", p.stock)
        obj.put("sku", p.sku)

        val specsObj = JSONObject()
        p.specifications.forEach { (k, v) -> specsObj.put(k, v) }
        obj.put("specifications", specsObj)
        return obj
    }

    private fun jsonToProduct(obj: JSONObject): Product {
        val categoryName = obj.optString("category", ProductCategory.MOBILE_PHONES.name)
        val category = try {
            ProductCategory.valueOf(categoryName)
        } catch (e: Exception) {
            ProductCategory.MOBILE_PHONES
        }

        val specs = mutableMapOf<String, String>()
        val specsObj = obj.optJSONObject("specifications")
        specsObj?.let {
            val keys = it.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                specs[key] = it.getString(key)
            }
        }

        return Product(
            id = obj.getString("id"),
            name = obj.getString("name"),
            brand = obj.optString("brand", "SK Mobile"),
            category = category,
            price = obj.getInt("price"),
            originalPrice = obj.getInt("originalPrice"),
            rating = obj.optDouble("rating", 4.5).toFloat(),
            reviewCount = obj.optInt("reviewCount", 12),
            description = obj.optString("description", ""),
            specifications = specs,
            inStock = obj.optBoolean("inStock", true),
            stockStatus = obj.optString("stockStatus", "In Stock"),
            isFeatured = obj.optBoolean("isFeatured", false),
            isBestSeller = obj.optBoolean("isBestSeller", false),
            isPreOwned = obj.optBoolean("isPreOwned", false),
            conditionGrade = if (obj.has("conditionGrade")) obj.getString("conditionGrade") else null,
            batteryHealth = if (obj.has("batteryHealth")) obj.getString("batteryHealth") else null,
            warranty = obj.optString("warranty", "1 Year Brand Warranty"),
            deliveryInfo = obj.optString("deliveryInfo", "Free Delivery in Udgir"),
            highlightBadge = if (obj.has("highlightBadge")) obj.getString("highlightBadge") else null,
            releaseOrder = obj.optInt("releaseOrder", 100),
            stock = obj.optInt("stock", 10),
            sku = obj.optString("sku", "SKM-PROD-${obj.getString("id")}")
        )
    }

    private fun orderToJson(order: ShopOrder): JSONObject {
        val obj = JSONObject()
        obj.put("orderId", order.orderId)
        obj.put("subtotal", order.subtotal)
        obj.put("discount", order.discount)
        obj.put("deliveryFee", order.deliveryFee)
        obj.put("totalAmount", order.totalAmount)
        obj.put("customerName", order.customerName)
        obj.put("mobileNumber", order.mobileNumber)
        obj.put("address", order.address)
        obj.put("city", order.city)
        obj.put("state", order.state)
        obj.put("pinCode", order.pinCode)
        obj.put("deliveryOption", order.deliveryOption)
        obj.put("paymentMethod", order.paymentMethod)
        obj.put("orderDate", order.orderDate)
        obj.put("status", order.status)

        val itemsArr = JSONArray()
        for (item in order.items) {
            val itemObj = JSONObject()
            itemObj.put("quantity", item.quantity)
            itemObj.put("selectedColor", item.selectedColor)
            itemObj.put("product", productToJson(item.product))
            itemsArr.put(itemObj)
        }
        obj.put("items", itemsArr)
        return obj
    }

    private fun jsonToOrder(obj: JSONObject): ShopOrder {
        val items = mutableListOf<CartItem>()
        val itemsArr = obj.optJSONArray("items")
        if (itemsArr != null) {
            for (i in 0 until itemsArr.length()) {
                val itemObj = itemsArr.getJSONObject(i)
                val qty = itemObj.optInt("quantity", 1)
                val color = itemObj.optString("selectedColor", "Default")
                val prod = jsonToProduct(itemObj.getJSONObject("product"))
                items.add(CartItem(prod, qty, color))
            }
        }

        return ShopOrder(
            orderId = obj.getString("orderId"),
            items = items,
            subtotal = obj.getInt("subtotal"),
            discount = obj.optInt("discount", 0),
            deliveryFee = obj.optInt("deliveryFee", 0),
            totalAmount = obj.getInt("totalAmount"),
            customerName = obj.getString("customerName"),
            mobileNumber = obj.getString("mobileNumber"),
            address = obj.getString("address"),
            city = obj.optString("city", "Udgir"),
            state = obj.optString("state", "Maharashtra"),
            pinCode = obj.optString("pinCode", "413517"),
            deliveryOption = obj.optString("deliveryOption", "Standard Free Delivery in Udgir"),
            paymentMethod = obj.optString("paymentMethod", "UPI"),
            orderDate = obj.optString("orderDate", "Today"),
            status = obj.optString("status", "Order Confirmed")
        )
    }

    private fun repairBookingToJson(r: RepairBooking): JSONObject {
        val obj = JSONObject()
        obj.put("bookingId", r.bookingId)
        obj.put("customerName", r.customerName)
        obj.put("mobileNumber", r.mobileNumber)
        obj.put("brand", r.brand)
        obj.put("model", r.model)
        obj.put("problem", r.problem)
        obj.put("pickupRequired", r.pickupRequired)
        obj.put("address", r.address)
        obj.put("preferredDate", r.preferredDate)
        obj.put("additionalDetails", r.additionalDetails)
        obj.put("status", r.status)
        obj.put("estimatedCost", r.estimatedCost)
        obj.put("bookingTimestamp", r.bookingTimestamp)
        return obj
    }

    private fun jsonToRepairBooking(obj: JSONObject): RepairBooking {
        return RepairBooking(
            bookingId = obj.getString("bookingId"),
            customerName = obj.getString("customerName"),
            mobileNumber = obj.getString("mobileNumber"),
            brand = obj.getString("brand"),
            model = obj.getString("model"),
            problem = obj.getString("problem"),
            pickupRequired = obj.optBoolean("pickupRequired", true),
            address = obj.getString("address"),
            preferredDate = obj.optString("preferredDate", "Today"),
            additionalDetails = obj.optString("additionalDetails", ""),
            status = obj.optString("status", "Submitted Successfully"),
            estimatedCost = obj.optString("estimatedCost", "₹499 - ₹1,499"),
            bookingTimestamp = obj.optLong("bookingTimestamp", System.currentTimeMillis())
        )
    }

    private fun sellRequestToJson(s: SellRequest): JSONObject {
        val obj = JSONObject()
        obj.put("requestId", s.requestId)
        obj.put("customerName", s.customerName)
        obj.put("mobileNumber", s.mobileNumber)
        obj.put("brand", s.brand)
        obj.put("model", s.model)
        obj.put("storage", s.storage)
        obj.put("condition", s.condition)
        obj.put("expectedPrice", s.expectedPrice)
        obj.put("address", s.address)
        obj.put("estimatedQuoteMin", s.estimatedQuoteMin)
        obj.put("estimatedQuoteMax", s.estimatedQuoteMax)
        obj.put("status", s.status)
        obj.put("timestamp", s.timestamp)
        return obj
    }

    private fun jsonToSellRequest(obj: JSONObject): SellRequest {
        return SellRequest(
            requestId = obj.getString("requestId"),
            customerName = obj.getString("customerName"),
            mobileNumber = obj.getString("mobileNumber"),
            brand = obj.getString("brand"),
            model = obj.getString("model"),
            storage = obj.getString("storage"),
            condition = obj.getString("condition"),
            expectedPrice = obj.optString("expectedPrice", ""),
            address = obj.getString("address"),
            estimatedQuoteMin = obj.optInt("estimatedQuoteMin", 10000),
            estimatedQuoteMax = obj.optInt("estimatedQuoteMax", 15000),
            status = obj.optString("status", "Valuation Generated"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
        )
    }

    // Default seeds
    private fun defaultSeedOrders(): List<ShopOrder> {
        return listOf(
            ShopOrder(
                orderId = "SKM-ORD-1001",
                items = listOf(CartItem(MockData.products[0], 1)),
                subtotal = 27999,
                discount = 3000,
                deliveryFee = 0,
                totalAmount = 24999,
                customerName = "Sachin Patil",
                mobileNumber = "9823456789",
                address = "Nagar Parishad Road, Udgir",
                city = "Udgir",
                state = "Maharashtra",
                pinCode = "413517",
                deliveryOption = "Express 2-Hour Delivery in Udgir",
                paymentMethod = "UPI",
                orderDate = "Today, 11:30 AM",
                status = "Pending"
            ),
            ShopOrder(
                orderId = "SKM-ORD-1002",
                items = listOf(
                    CartItem(MockData.products[7], 1),
                    CartItem(MockData.products[10], 2)
                ),
                subtotal = 3298,
                discount = 1300,
                deliveryFee = 0,
                totalAmount = 1998,
                customerName = "Ganesh Kulkarni",
                mobileNumber = "9890123456",
                address = "Shivaji Chowk, Udgir",
                city = "Udgir",
                state = "Maharashtra",
                pinCode = "413517",
                deliveryOption = "Standard Free Delivery in Udgir",
                paymentMethod = "Cash on Delivery",
                orderDate = "Today, 09:15 AM",
                status = "Confirmed"
            ),
            ShopOrder(
                orderId = "SKM-ORD-1003",
                items = listOf(CartItem(MockData.products[4], 1)),
                subtotal = 49900,
                discount = 21401,
                deliveryFee = 0,
                totalAmount = 28499,
                customerName = "Pooja Shinde",
                mobileNumber = "9765432109",
                address = "Bidar Road, Udgir",
                city = "Udgir",
                state = "Maharashtra",
                pinCode = "413517",
                deliveryOption = "Store Pickup at SK Mobile",
                paymentMethod = "Online Payment",
                orderDate = "Yesterday, 04:45 PM",
                status = "Delivered"
            )
        )
    }

    private fun defaultSeedRepairs(): List<RepairBooking> {
        return listOf(
            RepairBooking(
                bookingId = "SKM-REP-1001",
                customerName = "Ramesh Kumar",
                mobileNumber = "9823456789",
                brand = "OnePlus",
                model = "Nord CE 3",
                problem = "Display Replacement",
                pickupRequired = true,
                address = "Near Nagar Parishad Office, Station Road, Udgir",
                preferredDate = "Today",
                additionalDetails = "Screen cracked, touch partially working.",
                status = "Repairing",
                estimatedCost = "₹2,499"
            ),
            RepairBooking(
                bookingId = "SKM-REP-1002",
                customerName = "Imran Shaikh",
                mobileNumber = "9988776655",
                brand = "Samsung",
                model = "Galaxy M34",
                problem = "Charging Problem",
                pickupRequired = false,
                address = "Nanded Naka, Udgir",
                preferredDate = "Today",
                additionalDetails = "Charging port loose, not connecting.",
                status = "Diagnosing",
                estimatedCost = "₹499"
            ),
            RepairBooking(
                bookingId = "SKM-REP-1003",
                customerName = "Ajay Deshmukh",
                mobileNumber = "9123456780",
                brand = "Apple",
                model = "iPhone 11",
                problem = "Battery Replacement",
                pickupRequired = true,
                address = "Gola Naka, Udgir",
                preferredDate = "Tomorrow Morning",
                additionalDetails = "Battery health 74%, draining very fast.",
                status = "Pickup Scheduled",
                estimatedCost = "₹1,899"
            )
        )
    }

    private fun defaultSeedSellRequests(): List<SellRequest> {
        return listOf(
            SellRequest(
                requestId = "SKM-SELL-1001",
                customerName = "Ganesh Kulkarni",
                mobileNumber = "9823456789",
                brand = "Apple",
                model = "iPhone 12",
                storage = "128GB",
                condition = "Like New (Flawless)",
                expectedPrice = "₹28,000",
                address = "Shivaji Chowk, Udgir",
                estimatedQuoteMin = 22000,
                estimatedQuoteMax = 25000,
                status = "Inspection"
            ),
            SellRequest(
                requestId = "SKM-SELL-1002",
                customerName = "Rahul Biradar",
                mobileNumber = "9421345678",
                brand = "OnePlus",
                model = "9RT 5G",
                storage = "256GB",
                condition = "Good (Minor usage)",
                expectedPrice = "₹18,000",
                address = "Main Market, Udgir",
                estimatedQuoteMin = 14500,
                estimatedQuoteMax = 17000,
                status = "Purchased"
            )
        )
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================
    private val cachedNotifications = mutableMapOf<String, MutableList<AppNotification>>()

    @Synchronized
    fun loadNotifications(customerId: String): List<AppNotification> {
        if (customerId.isBlank()) return emptyList()
        cachedNotifications[customerId]?.let { return it.toList() }

        val jsonString = prefs?.getString("${KEY_NOTIFICATIONS_PREFIX}$customerId", null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<AppNotification>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(jsonToNotification(obj))
                }
                cachedNotifications[customerId] = list
                return list
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing notifications: ${e.message}")
            }
        }
        return emptyList()
    }

    @Synchronized
    fun saveNotification(notification: AppNotification): Boolean {
        val customerId = notification.customerId
        if (customerId.isBlank()) return false
        val current = loadNotifications(customerId).toMutableList()
        val index = current.indexOfFirst { it.id == notification.id }
        if (index != -1) {
            current[index] = notification
        } else {
            current.add(0, notification)
        }
        cachedNotifications[customerId] = current
        return persistNotifications(customerId, current)
    }

    @Synchronized
    fun markNotificationAsRead(id: String, customerId: String): Boolean {
        if (customerId.isBlank()) return false
        val current = loadNotifications(customerId).toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(isRead = true)
            cachedNotifications[customerId] = current
            return persistNotifications(customerId, current)
        }
        return false
    }

    @Synchronized
    fun markAllNotificationsAsRead(customerId: String): Boolean {
        if (customerId.isBlank()) return false
        val current = loadNotifications(customerId).map { it.copy(isRead = true) }.toMutableList()
        cachedNotifications[customerId] = current
        return persistNotifications(customerId, current)
    }

    @Synchronized
    fun clearNotifications(customerId: String): Boolean {
        if (customerId.isBlank()) return false
        cachedNotifications[customerId] = mutableListOf()
        return try {
            prefs?.edit()?.remove("${KEY_NOTIFICATIONS_PREFIX}$customerId")?.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun persistNotifications(customerId: String, list: List<AppNotification>): Boolean {
        return try {
            val array = JSONArray()
            for (notif in list) {
                array.put(notificationToJson(notif))
            }
            prefs?.edit()?.putString("${KEY_NOTIFICATIONS_PREFIX}$customerId", array.toString())?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notifications: ${e.message}")
            false
        }
    }

    private fun notificationToJson(n: AppNotification): JSONObject {
        return JSONObject().apply {
            put("id", n.id)
            put("customerId", n.customerId)
            put("title", n.title)
            put("message", n.message)
            put("type", n.type.name)
            put("referenceId", n.referenceId)
            put("status", n.status)
            put("timestamp", n.timestamp)
            put("isRead", n.isRead)
        }
    }

    private fun jsonToNotification(obj: JSONObject): AppNotification {
        val typeStr = obj.optString("type", NotificationType.ORDER.name)
        val notifType = try { NotificationType.valueOf(typeStr) } catch (e: Exception) { NotificationType.ORDER }
        return AppNotification(
            id = obj.getString("id"),
            customerId = obj.optString("customerId", ""),
            title = obj.optString("title", ""),
            message = obj.optString("message", ""),
            type = notifType,
            referenceId = obj.optString("referenceId", ""),
            status = obj.optString("status", ""),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            isRead = obj.optBoolean("isRead", false)
        )
    }

    companion object {
        private const val TAG = "LocalStorageManager"
        private const val PREFS_NAME = "sk_mobile_prefs"
        private const val KEY_PRODUCTS = "key_products"
        private const val KEY_ORDERS = "key_orders"
        private const val KEY_REPAIRS = "key_repairs"
        private const val KEY_SELL_REQUESTS = "key_sell_requests"
        private const val KEY_CART = "key_cart"
        private const val KEY_SETTINGS = "key_settings"
        private const val KEY_NOTIFICATIONS_PREFIX = "key_notif_"

        // Singleton instance
        @Volatile
        private var INSTANCE: LocalStorageManager? = null

        fun getInstance(context: Context? = null): LocalStorageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalStorageManager(context?.applicationContext ?: context).also { INSTANCE = it }
            }
        }
    }
}
