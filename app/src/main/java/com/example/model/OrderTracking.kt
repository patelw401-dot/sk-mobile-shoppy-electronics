package com.example.model

/**
 * Audit log entry for tracking each status transition in an order lifecycle.
 */
data class OrderStatusHistoryEntry(
    val status: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val updatedBy: String = "System"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "status" to status,
        "timestamp" to timestamp,
        "note" to note,
        "updatedBy" to updatedBy
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): OrderStatusHistoryEntry {
            return OrderStatusHistoryEntry(
                status = map["status"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                note = map["note"] as? String ?: "",
                updatedBy = map["updatedBy"] as? String ?: "System"
            )
        }
    }
}

/**
 * Standard 5-step progress timeline for customer orders.
 */
enum class OrderTrackingStage(
    val stageIndex: Int,
    val title: String,
    val subtitle: String
) {
    ORDER_PLACED(0, "Order Placed", "We have received your order."),
    ORDER_CONFIRMED(1, "Order Confirmed", "Order verified and confirmed by store."),
    PREPARING(2, "Preparing / Packed", "Items inspected and packed at Udgir store."),
    OUT_FOR_DELIVERY(3, "Shipped / Out for Delivery", "Package dispatched with delivery partner."),
    DELIVERED(4, "Delivered", "Delivered successfully to your address."),
    CANCELLED(-1, "Cancelled", "Order was cancelled.");

    companion object {
        fun fromStatus(status: String): OrderTrackingStage {
            val s = status.trim().lowercase()
            return when {
                s.contains("cancel") -> CANCELLED
                s.contains("deliver") && !s.contains("out") -> DELIVERED
                s.contains("ship") || s.contains("out for delivery") -> OUT_FOR_DELIVERY
                s.contains("pack") || s.contains("prepar") || s.contains("process") -> PREPARING
                s.contains("confirm") -> ORDER_CONFIRMED
                else -> ORDER_PLACED
            }
        }

        /**
         * Validates whether an admin status transition is permitted.
         * Prevents skipping directly to Delivered without packaging or shipping,
         * and prevents cancelling already delivered orders.
         */
        fun getPermittedNextStatuses(currentStatus: String): List<String> {
            val currentStage = fromStatus(currentStatus)
            return when (currentStage) {
                ORDER_PLACED -> listOf("Order Confirmed", "Preparing / Packed", "Cancelled")
                ORDER_CONFIRMED -> listOf("Preparing / Packed", "Shipped / Out for Delivery", "Cancelled")
                PREPARING -> listOf("Shipped / Out for Delivery", "Delivered", "Cancelled")
                OUT_FOR_DELIVERY -> listOf("Delivered", "Cancelled")
                DELIVERED -> emptyList() // Final state
                CANCELLED -> emptyList() // Final state
            }
        }

        fun canTransition(fromStatus: String, toStatus: String): Boolean {
            val fromStage = fromStatus(fromStatus)
            val toStage = fromStatus(toStatus)
            if (fromStage == toStage) return true
            if (fromStage == DELIVERED || fromStage == CANCELLED) return false
            if (toStage == CANCELLED) return true
            // Disallow jumping directly from placed to delivered
            if (fromStage == ORDER_PLACED && toStage == DELIVERED) return false
            return toStage.stageIndex >= fromStage.stageIndex
        }
    }
}
