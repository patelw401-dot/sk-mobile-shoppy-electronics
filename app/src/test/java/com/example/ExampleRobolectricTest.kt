package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.admin.model.AdminTab
import com.example.model.MockData
import com.example.model.ReelItem
import com.example.viewmodel.AppScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SK Mobile Shoppy and Electronic", appName)
  }

  @Test
  fun `verify Reels tab and Admin Reels tab availability`() {
    assertNotNull(AppScreen.valueOf("REELS"))
    assertNotNull(AdminTab.valueOf("REELS"))
    assertEquals("Reels & Videos", AdminTab.REELS.displayName)
  }

  @Test
  fun `verify ReelItem product linking logic`() {
    val linkedReel = ReelItem(
      id = "test-1",
      title = "OnePlus Nord CE4 Test",
      productId = "mob-1",
      productName = "OnePlus Nord CE4 (8GB RAM, 128GB)",
      productPrice = 24999
    )
    assertTrue("Should be product linked", linkedReel.isProductLinked)

    val generalReel = ReelItem(
      id = "test-2",
      title = "Repair Tutorial",
      productId = null,
      productName = null
    )
    assertFalse("General reel should not be product linked", generalReel.isProductLinked)
  }

  @Test
  fun `verify default reels are non empty and valid`() {
    val defaultReels = MockData.defaultReels
    assertTrue("Default reels list should not be empty", defaultReels.isNotEmpty())
    defaultReels.forEach { reel ->
      assertTrue("Reel ID should not be blank", reel.id.isNotBlank())
      assertTrue("Reel Title should not be blank", reel.title.isNotBlank())
      assertTrue("Reel Video URL should not be blank", reel.videoUrl.isNotBlank())
    }
  }

  @Test
  fun `verify authorized admin allowlist contains required owners and user`() {
    val allowlist = com.example.admin.auth.AdminAuthService.AUTHORIZED_ADMIN_GMAILS
    assertTrue("Store owner email must be authorized", allowlist.contains("skmobileshoppyandelectronics@gmail.com"))
    assertTrue("Admin user email must be authorized", allowlist.contains("patelw401@gmail.com"))
    assertFalse("Arbitrary customer accounts must be rejected", allowlist.contains("random.customer@gmail.com"))
  }

  @Test
  fun `verify default web client id string exists and matches Google OAuth Web Client`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val webClientId = context.getString(R.string.default_web_client_id)
    assertEquals(
      "23597341071-4f8sr6sum748fcm30eba02332i2n3isr.apps.googleusercontent.com",
      webClientId
    )
  }

  @Test
  fun `verify error parsing handles Google cancellation and web context cancellation`() {
    val cancelError = com.google.firebase.auth.FirebaseAuthException(
      "ERROR_WEB_CONTEXT_CANCELED",
      "The user canceled the web context operation"
    )
    val parsed = com.example.admin.auth.AdminAuthService.parseAuthError(cancelError)
    assertTrue("Must mention cancelled", parsed.contains("cancelled", ignoreCase = true))
  }

  @Test
  fun `verify error parsing handles ERROR_INVALID_CERT_HASH`() {
    val certError = com.google.firebase.auth.FirebaseAuthException(
      "ERROR_INVALID_CERT_HASH",
      "There was an error while trying to get your package certificate hash."
    )
    val context = ApplicationProvider.getApplicationContext<Context>()
    val parsed = com.example.admin.auth.AdminAuthService.parseAuthError(certError, context)
    assertTrue("Must mention SHA-1", parsed.contains("SHA-1", ignoreCase = true))
    assertTrue("Must mention package name", parsed.contains("com.aistudio.skmobile.udgirs", ignoreCase = true))
    assertTrue("Must mention Firebase Console", parsed.contains("Firebase Console", ignoreCase = true))
  }

  @Test
  fun `verify OrderTrackingStage mapping and validation`() {
    assertEquals(com.example.model.OrderTrackingStage.ORDER_PLACED, com.example.model.OrderTrackingStage.fromStatus("Order Placed"))
    assertEquals(com.example.model.OrderTrackingStage.ORDER_CONFIRMED, com.example.model.OrderTrackingStage.fromStatus("Order Confirmed"))
    assertEquals(com.example.model.OrderTrackingStage.PREPARING, com.example.model.OrderTrackingStage.fromStatus("Preparing / Packed"))
    assertEquals(com.example.model.OrderTrackingStage.OUT_FOR_DELIVERY, com.example.model.OrderTrackingStage.fromStatus("Shipped / Out for Delivery"))
    assertEquals(com.example.model.OrderTrackingStage.DELIVERED, com.example.model.OrderTrackingStage.fromStatus("Delivered"))
    assertEquals(com.example.model.OrderTrackingStage.CANCELLED, com.example.model.OrderTrackingStage.fromStatus("Cancelled"))

    // Transition validation
    assertTrue(com.example.model.OrderTrackingStage.canTransition("Order Placed", "Order Confirmed"))
    assertTrue(com.example.model.OrderTrackingStage.canTransition("Order Confirmed", "Preparing / Packed"))
    assertTrue(com.example.model.OrderTrackingStage.canTransition("Preparing / Packed", "Shipped / Out for Delivery"))
    assertTrue(com.example.model.OrderTrackingStage.canTransition("Shipped / Out for Delivery", "Delivered"))
    assertTrue(com.example.model.OrderTrackingStage.canTransition("Order Placed", "Cancelled"))
    // Cannot jump directly from placed to delivered
    assertFalse(com.example.model.OrderTrackingStage.canTransition("Order Placed", "Delivered"))
    // Final states cannot transition further
    assertFalse(com.example.model.OrderTrackingStage.canTransition("Delivered", "Cancelled"))
    assertFalse(com.example.model.OrderTrackingStage.canTransition("Cancelled", "Delivered"))
  }

  @Test
  fun `verify OrderStatusHistoryEntry serialization`() {
    val entry = com.example.model.OrderStatusHistoryEntry(
      status = "Preparing / Packed",
      timestamp = 1700000000000L,
      note = "Packed at Udgir warehouse",
      updatedBy = "skmobileshoppyandelectronics@gmail.com"
    )
    val map = entry.toMap()
    assertEquals("Preparing / Packed", map["status"])
    assertEquals(1700000000000L, map["timestamp"])
    assertEquals("Packed at Udgir warehouse", map["note"])
    assertEquals("skmobileshoppyandelectronics@gmail.com", map["updatedBy"])

    val reconstructed = com.example.model.OrderStatusHistoryEntry.fromMap(map)
    assertEquals(entry.status, reconstructed.status)
    assertEquals(entry.timestamp, reconstructed.timestamp)
    assertEquals(entry.note, reconstructed.note)
    assertEquals(entry.updatedBy, reconstructed.updatedBy)
  }

  @Test
  fun `verify ShopOrder backward compatibility with missing tracking fields`() {
    val legacyOrder = com.example.model.ShopOrder(
      orderId = "LEGACY-001",
      items = emptyList(),
      subtotal = 500,
      discount = 0,
      deliveryFee = 49,
      totalAmount = 549,
      customerName = "Sachin Patil",
      mobileNumber = "9876543210",
      address = "Station Road, Udgir",
      deliveryOption = "Standard",
      paymentMethod = "COD",
      orderDate = "27 Oct 2026",
      status = "Order Placed"
    )
    assertNotNull(legacyOrder.statusHistory)
    assertTrue(legacyOrder.statusHistory.isEmpty())
    assertFalse(legacyOrder.isCancelled)
    assertFalse(legacyOrder.isDelivered)
    assertEquals(com.example.model.OrderTrackingStage.ORDER_PLACED, legacyOrder.trackingStage)
  }

  @Test
  fun `verify LocalDataService order tracking status updates and customer isolation`() = kotlinx.coroutines.runBlocking {
    val localService = com.example.data.service.LocalDataService.getInstance()

    val testOrder = com.example.model.ShopOrder(
      orderId = "ORD-TEST-999",
      items = listOf(
        com.example.model.CartItem(
          product = com.example.model.MockData.products.first(),
          quantity = 1
        )
      ),
      subtotal = 1000,
      discount = 0,
      deliveryFee = 0,
      totalAmount = 1000,
      customerName = "Rohan Shinde",
      mobileNumber = "9988776655",
      address = "Nagar Parishad, Udgir",
      deliveryOption = "Standard",
      paymentMethod = "COD",
      orderDate = "Today",
      status = "Order Placed",
      customerUid = "CUST-UID-123"
    )

    // Create order
    val createResult = localService.createOrder(testOrder)
    assertTrue("Create order should succeed", createResult.isSuccess)
    val created = createResult.getOrThrow()
    assertTrue("Should have initial status history entry", created.statusHistory.isNotEmpty())

    // Update status with admin audit log
    val updateResult = localService.updateOrderStatusWithHistory(
      id = "ORD-TEST-999",
      status = "Order Confirmed",
      note = "Order verified by Udgir store manager",
      updatedBy = "patelw401@gmail.com"
    )
    assertTrue("Update status should succeed", updateResult.isSuccess)
    val updated = updateResult.getOrThrow()
    assertEquals("Order Confirmed", updated.status)
    assertEquals(2, updated.statusHistory.size)
    assertEquals("patelw401@gmail.com", updated.statusHistory.last().updatedBy)

    // Customer isolation: customer sees only their orders
    val customerOrders = localService.getCustomerOrders("CUST-UID-123")
    assertTrue("Customer must find their own order", customerOrders.any { it.orderId == "ORD-TEST-999" })

    val otherCustomerOrders = localService.getCustomerOrders("CUST-OTHER-456")
    assertFalse("Other customer must NOT see this order", otherCustomerOrders.any { it.orderId == "ORD-TEST-999" })

    // Unauthorized cancellation attempt
    val failCancel = localService.cancelOrder("ORD-TEST-999", "I want to cancel", "CUST-OTHER-456")
    assertTrue("Unauthorized user cannot cancel order", failCancel.isFailure)

    // Authorized cancellation by owner
    val successCancel = localService.cancelOrder("ORD-TEST-999", "Found cheaper alternative", "CUST-UID-123")
    assertTrue("Owner can cancel order", successCancel.isSuccess)
    val cancelledOrder = successCancel.getOrThrow()
    assertTrue(cancelledOrder.isCancelled)
    assertEquals("Found cheaper alternative", cancelledOrder.cancellationReason)
  }

  @Test
  fun `verify submitRepairBooking successful execution resets loading state and updates state`() = kotlinx.coroutines.runBlocking {
    val localService = com.example.data.service.LocalDataService.getInstance()
    val viewModel = com.example.viewmodel.ShopViewModel(localService)

    assertFalse("Loading state must be false initially", viewModel.uiState.value.isSubmittingRepair)

    val job = viewModel.submitRepairBooking(
      customerName = "Anita Deshmukh",
      mobileNumber = "9876543210",
      brand = "Xiaomi",
      model = "Redmi Note 12",
      problem = "Display Replacement",
      pickupRequired = true,
      address = "Station Road, Udgir",
      preferredDate = "Today",
      additionalDetails = "Screen flickers after drop"
    )
    job.join()

    // Loading state must be reset to false in finally
    assertFalse("Loading state must be reset to false after completion", viewModel.uiState.value.isSubmittingRepair)
    assertNotNull("Last confirmed booking must be populated", viewModel.uiState.value.lastConfirmedBooking)
    assertEquals("Redmi Note 12", viewModel.uiState.value.lastConfirmedBooking?.model)
    assertEquals("Xiaomi", viewModel.uiState.value.lastConfirmedBooking?.brand)
    assertEquals("Pickup Scheduled", viewModel.uiState.value.lastConfirmedBooking?.status)
    assertTrue("Active bookings must include new booking", viewModel.uiState.value.activeRepairBookings.any { it.model == "Redmi Note 12" })
    assertTrue("Toast message must confirm success", viewModel.uiState.value.toastMessage?.contains("booked successfully", ignoreCase = true) == true)
  }

  @Test
  fun `verify submitRepairBooking failure resets loading state in finally block and shows error`() = kotlinx.coroutines.runBlocking {
    val failingService = object : com.example.data.service.DataService by com.example.data.service.LocalDataService.getInstance() {
      override suspend fun createRepairBooking(booking: com.example.model.RepairBooking): Result<com.example.model.RepairBooking> {
        return Result.failure(RuntimeException("Firestore permission denied or network unavailable"))
      }
    }
    val viewModel = com.example.viewmodel.ShopViewModel(failingService)

    assertFalse("Loading state must be false initially", viewModel.uiState.value.isSubmittingRepair)

    val job = viewModel.submitRepairBooking(
      customerName = "Anita Deshmukh",
      mobileNumber = "9876543210",
      brand = "Samsung",
      model = "Galaxy S23",
      problem = "Battery Replacement",
      pickupRequired = false,
      address = "Drop-off at Shop",
      preferredDate = "Tomorrow Morning",
      additionalDetails = "Battery drains in 2 hours"
    )
    job.join()

    // CRITICAL: Loading state MUST NOT remain stuck on true
    assertFalse("Loading state MUST be reset to false even on failure", viewModel.uiState.value.isSubmittingRepair)
    assertEquals("No confirmed booking on failure", null, viewModel.uiState.value.lastConfirmedBooking)
    assertTrue("Toast message must report error", viewModel.uiState.value.toastMessage?.contains("Firestore permission denied", ignoreCase = true) == true)
  }

  @Test
  fun `verify submitRepairBooking duplicate submission prevention`() = kotlinx.coroutines.runBlocking {
    val localService = com.example.data.service.LocalDataService.getInstance()
    val viewModel = com.example.viewmodel.ShopViewModel(localService)

    // Pre-set submitting state to true to simulate an in-flight request
    val job1 = viewModel.submitRepairBooking(
      customerName = "Ramesh Pawar",
      mobileNumber = "9988776655",
      brand = "Vivo",
      model = "V29 Pro",
      problem = "Charging Problem",
      pickupRequired = true,
      address = "Nagar Parishad, Udgir",
      preferredDate = "Today",
      additionalDetails = ""
    )

    // Attempt second submission while first is running
    val countBeforeSecond = viewModel.uiState.value.activeRepairBookings.size
    val job2 = viewModel.submitRepairBooking(
      customerName = "Ramesh Pawar",
      mobileNumber = "9988776655",
      brand = "Vivo",
      model = "V29 Pro",
      problem = "Charging Problem",
      pickupRequired = true,
      address = "Nagar Parishad, Udgir",
      preferredDate = "Today",
      additionalDetails = ""
    )
    job1.join()
    job2.join()

    assertFalse("Loading state must be false after completion", viewModel.uiState.value.isSubmittingRepair)
  }

  @Test
  fun `verify LocalDataService createRepairBooking validation and persistence`() = kotlinx.coroutines.runBlocking {
    val localService = com.example.data.service.LocalDataService.getInstance()

    // Missing customer name or mobile number
    val invalidBooking1 = com.example.model.RepairBooking(
      bookingId = "SKM-REP-ERR1",
      customerName = "",
      mobileNumber = "9876543210",
      brand = "OnePlus",
      model = "Nord CE4",
      problem = "Display Replacement",
      pickupRequired = true,
      address = "Udgir",
      preferredDate = "Today",
      additionalDetails = ""
    )
    val failResult1 = localService.createRepairBooking(invalidBooking1)
    assertTrue("Should fail with blank name", failResult1.isFailure)

    // Missing brand or problem
    val invalidBooking2 = com.example.model.RepairBooking(
      bookingId = "SKM-REP-ERR2",
      customerName = "Sunil Kumar",
      mobileNumber = "9876543210",
      brand = "",
      model = "Nord CE4",
      problem = "Display Replacement",
      pickupRequired = true,
      address = "Udgir",
      preferredDate = "Today",
      additionalDetails = ""
    )
    val failResult2 = localService.createRepairBooking(invalidBooking2)
    assertTrue("Should fail with blank brand", failResult2.isFailure)

    // Valid booking
    val validBooking = com.example.model.RepairBooking(
      bookingId = "SKM-REP-TEST-VALID",
      customerName = "Sunil Kumar",
      mobileNumber = "9876543210",
      brand = "Apple",
      model = "iPhone 13",
      problem = "Camera / Glass",
      pickupRequired = false,
      address = "Drop-off at Shop",
      preferredDate = "Today",
      additionalDetails = "Rear wide lens glass scratched",
      status = "Appointment Booked"
    )
    val successResult = localService.createRepairBooking(validBooking)
    assertTrue("Valid booking must succeed", successResult.isSuccess)
    val stored = localService.getRepairBookingById("SKM-REP-TEST-VALID")
    assertNotNull("Stored booking must be retrievable", stored)
    assertEquals("iPhone 13", stored?.model)
  }
}

