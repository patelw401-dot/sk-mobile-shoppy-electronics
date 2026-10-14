package com.example.admin.model

import com.example.model.CustomerReview
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.RepairBooking
import com.example.model.SellRequest
import com.example.model.ShopOrder

enum class AdminTab(val displayName: String, val iconName: String) {
    DASHBOARD("Dashboard", "dashboard"),
    PRODUCTS("Products", "products"),
    ORDERS("Orders", "orders"),
    REPAIRS("Repair Bookings", "repairs"),
    OLD_PHONE_BUY("Old Phone Buy", "old_buy"),
    SELL_REQUESTS("Sell Old Phone Requests", "sell_requests"),
    CUSTOMERS("Customers", "customers"),
    CATEGORIES("Categories", "categories"),
    REVIEWS("Reviews", "reviews"),
    PAYMENTS("Payments", "payments"),
    REPORTS("Reports", "reports"),
    SETTINGS("Settings", "settings")
}

data class CustomerRecord(
    val id: String,
    val name: String,
    val mobileNumber: String,
    val address: String,
    val totalOrders: Int,
    val totalRepairs: Int,
    val totalSpending: Int,
    val lastActivity: String,
    val email: String = "${name.lowercase().replace(" ", ".")}@skmobile.in",
    val createdAt: Long = System.currentTimeMillis()
) {
    val phone: String get() = mobileNumber
    val totalSpent: Int get() = totalSpending
}

data class PaymentRecord(
    val paymentId: String,
    val orderId: String,
    val customerName: String,
    val amount: Int,
    val method: String, // "UPI", "Cash on Delivery", "Credit Card", "Net Banking"
    val status: String, // "Successful", "Paid", "Pending", "Failed", "Cancelled", "Refunded"
    val date: String,
    val settlementStatus: String = "Unsettled", // "Unsettled", "Pending Settlement", "Settled to Bank", "Reversed"
    val transactionRef: String = "",
    val idempotencyKey: String = ""
)

data class CategoryRecord(
    val id: String,
    val name: String,
    val slug: String,
    val category: ProductCategory,
    val isActive: Boolean = true,
    val productCount: Int = 0
)

data class AdminReviewRecord(
    val review: CustomerReview,
    val isApproved: Boolean = true
)

data class ShopSettings(
    val shopName: String = "SK Mobile",
    val address: String = "Nagar Parishad, Udgir, Maharashtra - 413517",
    val phoneNumber: String = "+91 98765 43210",
    val whatsappNumber: String = "+91 98765 43210",
    val openingTime: String = "09:30 AM",
    val closingTime: String = "09:30 PM",
    val pickupAvailable: Boolean = true,
    val homeDeliveryAvailable: Boolean = true,
    val deliveryCharge: Int = 49,
    val freeDeliveryThreshold: Int = 499,
    val upiEnabled: Boolean = true,
    val codEnabled: Boolean = true,
    val onlinePaymentEnabled: Boolean = true
)
