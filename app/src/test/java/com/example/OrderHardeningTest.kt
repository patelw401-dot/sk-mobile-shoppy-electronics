package com.example

import com.example.data.service.AdminOrderCache
import com.example.data.service.firstOrderText
import com.example.data.service.formatOrderDate
import com.example.data.service.orderTimestampMillis
import com.example.model.ShopOrder
import com.example.viewmodel.ShopUiState
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OrderHardeningTest {
    @Test
    fun adminCacheIsClearedAndOldListenerCannotRepopulateIt() {
        val cache = AdminOrderCache()
        val adminSession = cache.reset()
        val order = order("AliceUid")
        cache.replace(adminSession, listOf(order))
        assertEquals(listOf(order), cache.orders.value)

        val nextSession = cache.reset()
        cache.replace(adminSession, listOf(order))
        assertEquals(emptyList<ShopOrder>(), cache.orders.value)

        cache.replace(nextSession, listOf(order))
        cache.reset()
        assertEquals(emptyList<ShopOrder>(), cache.orders.value)
    }

    @Test
    fun customerViewsRequireExactUidAndHidePriorAccountDetails() {
        val owned = order("AliceUid")
        val other = order("OtherUid")
        val wrongCase = order("aliceuid")
        val legacy = order("")
        val state = ShopUiState(
            customerUid = "AliceUid",
            customerOrders = listOf(owned, other, wrongCase, legacy),
            selectedOrder = other,
            lastConfirmedOrder = wrongCase
        )
        assertEquals(listOf(owned), state.ownedCustomerOrders)
        assertNull(state.ownedSelectedOrder)
        assertNull(state.ownedConfirmedOrder)

        val switched = state.copy(selectedOrder = owned, lastConfirmedOrder = owned)
            .copy(customerUid = "OtherUid")
        assertEquals(listOf(other), switched.ownedCustomerOrders)
        assertNull(switched.ownedSelectedOrder)
        assertNull(switched.ownedConfirmedOrder)
        assertEquals(emptyList<ShopOrder>(), state.copy(customerUid = null).ownedCustomerOrders)
    }

    @Test
    fun orderReadersAcceptFirestoreAndLegacyFieldFormats() {
        val millis = 1764072000123L
        assertEquals(millis, orderTimestampMillis(Timestamp(Date(millis))))
        assertEquals(millis, orderTimestampMillis(millis))
        assertNull(orderTimestampMillis("invalid timestamp"))
        assertNotEquals("Today", formatOrderDate(null, millis))
        assertEquals("Legacy date", formatOrderDate("Legacy date", millis))
        assertEquals("012345", firstOrderText(null, "012345"))
        assertEquals("Doorstep", firstOrderText(" ", "Doorstep"))
        assertEquals("Pickup", firstOrderText("Pickup", "Doorstep"))
    }

    @Test
    fun deliveredOrderWithoutPaymentEvidenceIsPending() {
        val delivered = order("AliceUid").copy(status = "Delivered")
        assertEquals("Pending", delivered.paymentStatus)
        assertEquals("Paid", delivered.copy(transactionStatus = "Paid").paymentStatus)
    }

    private fun order(uid: String) = ShopOrder(
        orderId = "order-$uid",
        items = emptyList(),
        subtotal = 100,
        discount = 0,
        deliveryFee = 0,
        totalAmount = 100,
        customerName = "Alice",
        mobileNumber = "1234567890",
        address = "Udgir",
        deliveryOption = "Doorstep",
        paymentMethod = "Cash on Delivery",
        orderDate = "Today",
        customerUid = uid
    )
}
