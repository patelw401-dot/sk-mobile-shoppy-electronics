package com.example.model

enum class ProductCategory(val displayName: String, val iconName: String) {
    ALL("All Items", "all"),
    MOBILE_PHONES("Mobile Phones", "phone"),
    OLD_PHONES("Old Phones", "old_phone"),
    CHARGERS("Chargers", "charger"),
    DATA_CABLES("Data Cables", "cable"),
    HEADPHONES("Headphones", "headphones"),
    BLUETOOTH("Bluetooth", "bluetooth"),
    ACCESSORIES("Accessories", "accessories"),
    REPAIR_SERVICES("Mobile Repair", "repair")
}

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val price: Int,
    val originalPrice: Int,
    val rating: Float,
    val reviewCount: Int,
    val description: String,
    val specifications: Map<String, String>,
    val inStock: Boolean = true,
    val stockStatus: String = "In Stock", // "In Stock", "Only 3 left", "Available"
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isPreOwned: Boolean = false,
    val conditionGrade: String? = null,
    val batteryHealth: String? = null,
    val warranty: String = "1 Year Brand Warranty",
    val deliveryInfo: String = "Free Delivery in Udgir (Same Day)",
    val highlightBadge: String? = null,
    val releaseOrder: Int = 100, // Higher number = newer
    val stock: Int = 12,
    val sku: String = "SKM-${id.uppercase()}",
    val images: List<String> = emptyList(),
    val featured: Boolean = isFeatured,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val discount: Int
        get() = if (originalPrice > price) (((originalPrice - price).toDouble() / originalPrice) * 100).toInt() else 0

    val discountPercent: Int
        get() = discount

    val savingsAmount: Int
        get() = originalPrice - price
}

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedColor: String = "Default"
)

data class RepairService(
    val id: String,
    val title: String,
    val estimatedStartingPrice: Int,
    val turnaroundTime: String,
    val warrantyDays: Int,
    val description: String,
    val iconType: String
)

data class RepairBooking(
    val bookingId: String,
    val customerName: String,
    val mobileNumber: String,
    val brand: String,
    val model: String,
    val problem: String,
    val pickupRequired: Boolean,
    val address: String,
    val preferredDate: String,
    val additionalDetails: String = "",
    val status: String = "Submitted Successfully",
    val estimatedCost: String = "₹499 - ₹1,499",
    val bookingTimestamp: Long = System.currentTimeMillis(),
    val customerUid: String = ""
) {
    val id: String get() = bookingId
    val customerId: String get() = if (customerUid.isNotBlank()) customerUid else "CUST-${customerName.hashCode().let { if (it < 0) -it else it }}"
    val phone: String get() = mobileNumber
    val repairStatus: String get() = status
    val notes: String get() = additionalDetails
    val createdAt: Long get() = bookingTimestamp
}

data class SellRequest(
    val requestId: String,
    val customerName: String,
    val mobileNumber: String,
    val brand: String,
    val model: String,
    val storage: String,
    val condition: String,
    val expectedPrice: String,
    val address: String,
    val estimatedQuoteMin: Int,
    val estimatedQuoteMax: Int,
    val status: String = "Valuation Generated",
    val timestamp: Long = System.currentTimeMillis(),
    val customerUid: String = ""
) {
    val id: String get() = requestId
    val customerId: String get() = if (customerUid.isNotBlank()) customerUid else "CUST-${customerName.hashCode().let { if (it < 0) -it else it }}"
    val phone: String get() = mobileNumber
    val offeredPrice: String get() = "₹${(estimatedQuoteMin + estimatedQuoteMax) / 2}"
    val requestType: String get() = "SELL"
    val createdAt: Long get() = timestamp
}

data class CustomerReview(
    val id: String,
    val customerName: String,
    val location: String,
    val rating: Int,
    val date: String,
    val comment: String,
    val verifiedPurchase: Boolean = true,
    val serviceOrProduct: String
)

data class ShopOrder(
    val orderId: String,
    val items: List<CartItem>,
    val subtotal: Int,
    val discount: Int,
    val deliveryFee: Int,
    val totalAmount: Int,
    val customerName: String,
    val mobileNumber: String,
    val address: String,
    val city: String = "Udgir",
    val state: String = "Maharashtra",
    val pinCode: String = "413517",
    val deliveryOption: String,
    val paymentMethod: String,
    val orderDate: String,
    val status: String = "Order Confirmed",
    val customerUid: String = "",
    val paymentGatewayRef: String = "",
    val transactionStatus: String = "",
    val bankSettlementStatus: String = "Unsettled",
    val idempotencyKey: String = "",
    val statusHistory: List<OrderStatusHistoryEntry> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val cancellationReason: String? = null,
    val trackingNumber: String? = null,
    val estimatedDeliveryDate: String? = null
) {
    val id: String get() = orderId
    val customerId: String get() = if (customerUid.isNotBlank()) customerUid else "CUST-${customerName.hashCode().let { if (it < 0) -it else it }}"
    val phone: String get() = mobileNumber
    val deliveryMethod: String get() = deliveryOption
    val paymentStatus: String get() = if (transactionStatus.isNotBlank()) {
        transactionStatus
    } else if (paymentMethod == "Cash on Delivery" && status != "Delivered") {
        "Pending"
    } else {
        "Paid"
    }
    val orderStatus: String get() = status
    val deliveryCharge: Int get() = deliveryFee
    val total: Int get() = totalAmount
    val createdAt: Long get() = System.currentTimeMillis()
    val trackingStage: OrderTrackingStage get() = OrderTrackingStage.fromStatus(status)
    val isCancelled: Boolean get() = trackingStage == OrderTrackingStage.CANCELLED
    val isDelivered: Boolean get() = trackingStage == OrderTrackingStage.DELIVERED
}
