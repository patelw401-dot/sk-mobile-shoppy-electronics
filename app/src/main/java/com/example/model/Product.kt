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
    REPAIR_SERVICES("Mobile Repair", "repair");

    val slug: String
        get() = when (this) {
            MOBILE_PHONES -> "smartphones"
            OLD_PHONES -> "old-phones"
            CHARGERS -> "chargers"
            DATA_CABLES -> "data-cables"
            HEADPHONES -> "audio"
            BLUETOOTH -> "bluetooth"
            ACCESSORIES -> "accessories"
            REPAIR_SERVICES -> "repair"
            ALL -> "all"
        }

    companion object {
        fun fromSlug(slug: String?): ProductCategory {
            if (slug.isNullOrBlank()) return MOBILE_PHONES
            val norm = slug.lowercase().replace("_", "-").trim()
            return when {
                norm == "all" -> ALL
                norm == "smartphones" || norm.contains("phone") || norm.contains("mobile") -> MOBILE_PHONES
                norm == "old-phones" || norm.contains("pre-owned") -> OLD_PHONES
                norm == "chargers" || norm.contains("charger") -> CHARGERS
                norm == "cables" || norm == "data-cables" || norm.contains("cable") -> DATA_CABLES
                norm == "audio" || norm == "headphones" || norm.contains("audio") || norm.contains("headphone") || norm.contains("earphone") -> HEADPHONES
                norm == "bluetooth" || norm.contains("wireless") -> BLUETOOTH
                norm == "accessories" || norm.contains("accessori") || norm.contains("case") -> ACCESSORIES
                norm == "repair" || norm.contains("repair") -> REPAIR_SERVICES
                else -> {
                    try {
                        valueOf(slug)
                    } catch (e: Exception) {
                        entries.find { it.name.equals(slug, ignoreCase = true) } ?: ACCESSORIES
                    }
                }
            }
        }
    }
}

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val categorySlug: String = category.name.lowercase().replace("_", "-"),
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
    val hasWarranty: Boolean = false,
    val warrantyDuration: String = "",
    val warrantyDescription: String = "",
    val deliveryInfo: String = "Free Delivery in Udgir (Same Day)",
    val highlightBadge: String? = null,
    val releaseOrder: Int = 100, // Higher number = newer
    val stock: Int = 12,
    val sku: String = "SKM-${id.uppercase()}",
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val featured: Boolean = isFeatured,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val primaryImage: String
        get() = if (imageUrl.isNotBlank()) imageUrl else (images.firstOrNull() ?: "")

    val discount: Int
        get() = if (originalPrice > price) (((originalPrice - price).toDouble() / originalPrice) * 100).toInt() else 0

    val discountPercent: Int
        get() = discount

    val savingsAmount: Int
        get() = originalPrice - price

    fun categoryMatches(targetSlug: String): Boolean {
        val target = targetSlug.lowercase().replace("_", "-").trim()
        if (target == "all" || target.isBlank()) return true
        val curSlug = categorySlug.lowercase().replace("_", "-").trim()
        if (curSlug == target || curSlug.contains(target) || target.contains(curSlug)) return true
        val curName = category.name.lowercase().replace("_", "-").trim()
        val curDisplay = category.displayName.lowercase().replace("_", "-").trim()
        if (target == "smartphones" || target == "mobile-phones") {
            return curName.contains("phone") || curName.contains("mobile") || curDisplay.contains("phone") || curSlug.contains("phone") || curSlug.contains("smart")
        }
        if (target == "accessories") {
            return curName.contains("accessori") || curDisplay.contains("accessori") || curSlug.contains("accessori") || curSlug.contains("cover") || curSlug.contains("case") || curSlug.contains("power") || curSlug.contains("guard")
        }
        if (target == "old-phones" || target == "old_phones") {
            return curName.contains("old") || curSlug.contains("old") || curSlug.contains("pre-owned") || isPreOwned
        }
        if (target == "chargers") {
            return curName.contains("charger") || curDisplay.contains("charger") || curSlug.contains("charger")
        }
        if (target == "cables" || target == "data-cables") {
            return curName.contains("cable") || curDisplay.contains("cable") || curSlug.contains("cable")
        }
        if (target == "audio" || target == "headphones") {
            return curName.contains("headphone") || curName.contains("audio") || curDisplay.contains("headphone") || curSlug.contains("audio") || curSlug.contains("headphone") || curSlug.contains("earphone")
        }
        if (target == "bluetooth" || target == "speakers") {
            return curName.contains("bluetooth") || curSlug.contains("bluetooth") || curSlug.contains("speaker")
        }
        return curName == target || curDisplay == target || curName.contains(target) || curSlug == target
    }
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
