package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Integration Architecture & Helper for Customer Order and Repair Notifications.
 *
 * CURRENT STATUS:
 * - In-App Notification Center: 100% Active & Operational via Firestore & LocalStorage.
 * - Local Notification Channels: Configured.
 * - FCM Push Notifications: Integration points prepared.
 *
 * REMAINING MANUAL SETUP REQUIRED FOR LIVE REMOTE PUSH NOTIFICATIONS:
 * 1. Firebase Cloud Messaging (FCM) SDK dependency in app/build.gradle.kts.
 * 2. Trusted Backend / Firebase Cloud Functions trigger:
 *    - Cloud Function listening on `orders/{orderId}` onChange:
 *      when status changes, look up customer FCM token in `/customers/{customerId}` and send FCM payload.
 *    - Cloud Function listening on `repairBookings/{bookingId}` onChange:
 *      when repairStatus changes, send FCM payload.
 * 3. Android 13+ (API 33) Runtime Permission:
 *    - Request `android.permission.POST_NOTIFICATIONS` in MainActivity when customer enables push alerts.
 * 4. Google Services FCM registration token registration via FirebaseMessaging.getInstance().token.
 */
object PushNotificationHelper {
    private const val TAG = "PushNotificationHelper"

    const val CHANNEL_ID_ORDERS = "sk_mobile_orders"
    const val CHANNEL_NAME_ORDERS = "Order Updates"

    const val CHANNEL_ID_REPAIRS = "sk_mobile_repairs"
    const val CHANNEL_NAME_REPAIRS = "Repair Updates"

    /**
     * Initializes notification channels on Android 8.0+ (API 26+).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (notificationManager != null) {
                    val orderChannel = NotificationChannel(
                        CHANNEL_ID_ORDERS,
                        CHANNEL_NAME_ORDERS,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications about SK Mobile Shoppy and Electronic order status changes"
                        enableLights(true)
                        enableVibration(true)
                    }

                    val repairChannel = NotificationChannel(
                        CHANNEL_ID_REPAIRS,
                        CHANNEL_NAME_REPAIRS,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications about SK Mobile Shoppy and Electronic repair booking progress"
                        enableLights(true)
                        enableVibration(true)
                    }

                    notificationManager.createNotificationChannel(orderChannel)
                    notificationManager.createNotificationChannel(repairChannel)
                    Log.d(TAG, "Notification channels initialized successfully")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not initialize notification channels: ${e.message}")
            }
        }
    }

    /**
     * Integration hook for FCM device token registration.
     */
    fun onNewFcmTokenReceived(customerId: String, token: String) {
        Log.d(TAG, "FCM Token registered for customer $customerId: ${token.take(8)}...")
        // When Cloud Functions trigger is deployed, save token to /customers/{customerId}/fcmTokens
    }
}
