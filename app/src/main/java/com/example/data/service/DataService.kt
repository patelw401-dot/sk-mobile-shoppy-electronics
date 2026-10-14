package com.example.data.service

import com.example.admin.model.CategoryRecord
import com.example.admin.model.CustomerRecord
import com.example.admin.model.ShopSettings
import com.example.model.AppNotification
import com.example.model.CartItem
import com.example.model.CustomerReview
import com.example.model.Product
import com.example.model.RepairBooking
import com.example.model.SellRequest
import com.example.model.ShopOrder
import kotlinx.coroutines.flow.Flow

/**
 * Clean data service contract separating UI components and ViewModels
 * from concrete database / backend implementations (e.g. Local Storage, Firebase, or REST).
 */
interface DataService {

    // --- PRODUCTS ---
    fun observeProducts(): Flow<List<Product>>
    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: String): Product?
    suspend fun createProduct(product: Product): Result<Product>
    suspend fun updateProduct(product: Product): Result<Product>
    suspend fun deleteProduct(id: String): Result<Boolean>

    // --- ORDERS ---
    fun observeOrders(): Flow<List<ShopOrder>>
    suspend fun getOrders(): List<ShopOrder>
    suspend fun getOrderById(id: String): ShopOrder?
    suspend fun createOrder(order: ShopOrder): Result<ShopOrder>
    suspend fun updateOrderStatus(id: String, status: String): Result<ShopOrder>

    // --- REPAIRS ---
    fun observeRepairBookings(): Flow<List<RepairBooking>>
    suspend fun getRepairBookings(): List<RepairBooking>
    suspend fun getRepairBookingById(id: String): RepairBooking?
    suspend fun createRepairBooking(booking: RepairBooking): Result<RepairBooking>
    suspend fun updateRepairStatus(id: String, status: String, cost: String? = null): Result<RepairBooking>

    // --- OLD PHONE REQUESTS ---
    fun observeOldPhoneRequests(): Flow<List<SellRequest>>
    suspend fun getOldPhoneRequests(): List<SellRequest>
    suspend fun createOldPhoneRequest(request: SellRequest): Result<SellRequest>
    suspend fun updateOldPhoneRequest(id: String, status: String, offerMin: Int? = null, offerMax: Int? = null): Result<SellRequest>

    // --- CUSTOMERS ---
    suspend fun getCustomers(): List<CustomerRecord>
    suspend fun getCustomerById(id: String): CustomerRecord?

    // --- CART ---
    suspend fun getCart(): List<CartItem>
    suspend fun saveCart(items: List<CartItem>): Result<Unit>
    suspend fun clearCart(): Result<Unit>

    // --- REVIEWS, CATEGORIES & SETTINGS ---
    suspend fun getReviews(): List<CustomerReview>
    suspend fun getCategories(): List<CategoryRecord>
    suspend fun toggleCategoryActive(categoryId: String): Result<CategoryRecord>
    suspend fun addCategory(name: String, slug: String): Result<CategoryRecord>
    suspend fun getSettings(): ShopSettings
    suspend fun saveSettings(settings: ShopSettings): Result<ShopSettings>

    // --- NOTIFICATIONS ---
    fun observeNotifications(customerId: String): Flow<List<AppNotification>>
    suspend fun getNotifications(customerId: String): List<AppNotification>
    suspend fun createNotification(notification: AppNotification): Result<AppNotification>
    suspend fun markNotificationAsRead(id: String, customerId: String): Result<Boolean>
    suspend fun markAllNotificationsAsRead(customerId: String): Result<Boolean>
    suspend fun clearNotifications(customerId: String): Result<Boolean>
}
