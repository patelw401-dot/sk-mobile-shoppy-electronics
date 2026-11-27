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
    REELS("Reels & Videos", "reels"),
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
    val category: ProductCategory = ProductCategory.ACCESSORIES,
    val isActive: Boolean = true,
    val productCount: Int = 0,
    val imageUrl: String = "",
    val description: String = "",
    val displayOrder: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

data class AdminReviewRecord(
    val review: CustomerReview,
    val isApproved: Boolean = true
)

data class ShopSettings(
    val shopName: String = "SK Mobile Shoppy and Electronics",
    val address: String = "Near Nagar Parishad Office, Station Road, Udgir, Maharashtra",
    val pincode: String = "413517",
    val landmark: String = "Opposite Nagar Parishad Municipal Complex",
    val phoneNumber: String = "+91 9823109313",
    val whatsappNumber: String = "+91 9823109313",
    val email: String = "skmobileshoppyandelectronics@gmail.com",
    val openingTime: String = "09:30 AM",
    val closingTime: String = "09:30 PM",
    val openingHours: String = "9:30 AM – 9:30 PM (Open All 7 Days)",
    val services: List<String> = DEFAULT_SERVICES,
    val googleMapsUrl: String = "https://maps.google.com/?q=SK+Mobile+Shoppy+and+Electronics+Nagar+Parishad+Udgir+413517",
    val pickupAvailable: Boolean = true,
    val homeDeliveryAvailable: Boolean = true,
    val deliveryCharge: Int = 49,
    val freeDeliveryThreshold: Int = 499,
    val upiEnabled: Boolean = true,
    val codEnabled: Boolean = true,
    val onlinePaymentEnabled: Boolean = true
) {
    val fullAddressWithPincode: String
        get() {
            val cleanAddr = address.trim()
            val cleanPin = pincode.trim()
            return when {
                cleanPin.isBlank() -> cleanAddr
                cleanAddr.contains(cleanPin) -> cleanAddr
                cleanAddr.isBlank() -> cleanPin
                else -> "$cleanAddr - $cleanPin"
            }
        }

    val formattedHours: String
        get() = openingHours.trim().ifBlank {
            "${openingTime.trim()} – ${closingTime.trim()} (Open All 7 Days)"
        }

    val shortLocationLabel: String
        get() {
            val cityPart = address.split(",").map { it.trim() }.firstOrNull {
                it.contains("Udgir", ignoreCase = true)
            }?.substringBefore("-")?.trim() ?: "Udgir"
            val cleanPin = pincode.trim()
            return if (cleanPin.isNotBlank()) "$cityPart $cleanPin" else cityPart
        }

    val dialablePhone: String
        get() {
            val raw = phoneNumber.trim()
            val digits = raw.filter { it.isDigit() }
            if (digits.isEmpty()) return "+919823109313"
            return if (raw.startsWith("+")) "+$digits"
            else if (digits.length == 10) "+91$digits"
            else "+$digits"
        }

    val whatsappDigits: String
        get() {
            val raw = whatsappNumber.ifBlank { phoneNumber }.trim()
            val digits = raw.filter { it.isDigit() }
            if (digits.isEmpty()) return "919823109313"
            return if (digits.length == 10) "91$digits" else digits
        }

    companion object {
        val DEFAULT_SERVICES = listOf(
            "Mobile repair (Display, Battery, Charging, Audio, Software)",
            "New 5G mobile phones (All major brands)",
            "Old mobile buy (Instant valuation & cash)",
            "Old mobile sell (Certified refurbished with warranty)",
            "Fast chargers & GaN power adapters",
            "High-speed braided data cables",
            "Wired & Bluetooth headphones",
            "TWS earbuds & wireless Bluetooth speakers",
            "Mobile accessories (Power banks, tempered glass, cases)",
            "Home pickup and delivery across Udgir",
            "Online repair booking with live tracking"
        )
    }
}

