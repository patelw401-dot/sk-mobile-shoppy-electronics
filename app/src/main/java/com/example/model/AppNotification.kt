package com.example.model

enum class NotificationType {
    ORDER,
    REPAIR,
    SYSTEM
}

data class AppNotification(
    val id: String,
    val customerId: String,
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.ORDER,
    val referenceId: String = "",
    val status: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    companion object {
        fun createOrderNotificationId(orderId: String, status: String): String =
            "NOTIF_ORD_${orderId}_${status.trim().replace(" ", "_").uppercase()}"

        fun createRepairNotificationId(bookingId: String, status: String): String =
            "NOTIF_REP_${bookingId}_${status.trim().replace(" ", "_").uppercase()}"

        fun createOrderNotification(
            orderId: String,
            customerId: String,
            status: String,
            orderDate: String = ""
        ): AppNotification {
            val (title, msg) = when (status.lowercase()) {
                "confirmed", "order confirmed" ->
                    "Order Confirmed" to "Your order ($orderId) has been confirmed and is being prepared."
                "packed", "processing" ->
                    "Order Packed" to "Your order ($orderId) has been safely packed by our warehouse team."
                "out for delivery", "shipped" ->
                    "Out for Delivery" to "Your order ($orderId) is out for delivery! Executive is en route."
                "delivered" ->
                    "Order Delivered" to "Your order ($orderId) has been delivered. Thank you for choosing SK Mobile Shoppy and Electronic!"
                "cancelled" ->
                    "Order Cancelled" to "Your order ($orderId) has been cancelled."
                else ->
                    "Order Update: $status" to "Your order ($orderId) status updated to $status."
            }

            return AppNotification(
                id = createOrderNotificationId(orderId, status),
                customerId = customerId,
                title = title,
                message = msg,
                type = NotificationType.ORDER,
                referenceId = orderId,
                status = status,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        }

        fun createRepairNotification(
            bookingId: String,
            customerId: String,
            status: String,
            brand: String = "",
            model: String = "",
            cost: String? = null
        ): AppNotification {
            val deviceDesc = if (brand.isNotBlank() && model.isNotBlank()) "$brand $model" else "device"
            val (title, msg) = when (status.lowercase()) {
                "requested", "pending" ->
                    "Repair Requested" to "Your repair request ($bookingId) for $deviceDesc has been received."
                "pickup scheduled" ->
                    "Pickup Scheduled" to "Our pickup executive will visit your address for $deviceDesc."
                "received" ->
                    "Device Received" to "Your $deviceDesc ($bookingId) has arrived safely at our repair lab."
                "diagnosing" ->
                    "Diagnosing Device" to "Technician is diagnosing hardware & software components of your $deviceDesc."
                "estimate sent" ->
                    "Estimate Sent" to "Repair estimate ${if (!cost.isNullOrBlank()) "($cost) " else ""}ready for $deviceDesc."
                "repairing" ->
                    "Repair Underway" to "Genuine parts replacement & precision repair started for your $deviceDesc."
                "ready" ->
                    "Device Ready" to "Your $deviceDesc is fixed, quality tested, and ready for pickup/delivery!"
                "delivered" ->
                    "Device Delivered" to "Your repaired $deviceDesc ($bookingId) has been delivered."
                "cancelled" ->
                    "Repair Cancelled" to "Repair booking ($bookingId) has been cancelled."
                else ->
                    "Repair Update: $status" to "Repair booking ($bookingId) status updated to $status."
            }

            return AppNotification(
                id = createRepairNotificationId(bookingId, status),
                customerId = customerId,
                title = title,
                message = msg,
                type = NotificationType.REPAIR,
                referenceId = bookingId,
                status = status,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        }
    }
}
