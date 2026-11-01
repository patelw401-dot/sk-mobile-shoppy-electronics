package com.example.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin.model.CategoryRecord
import com.example.auth.CustomerAuthService
import com.example.data.firebase.FirebaseConfig
import com.example.data.service.DataService
import com.example.data.service.DataServiceLocator
import com.example.data.service.LocalDataService
import com.example.model.AppNotification
import com.example.model.CartItem
import com.example.model.CustomerReview
import com.example.model.MockData
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.ReelItem
import com.example.model.RepairBooking
import com.example.model.RepairService
import com.example.model.SellRequest
import com.example.model.ShopOrder
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    HOME,
    PRODUCTS,
    REELS,
    PRODUCT_DETAIL,
    CART,
    CHECKOUT,
    ORDER_SUCCESS,
    MY_ORDERS,
    ORDER_DETAIL,
    REPAIR_BOOKING,
    OLD_PHONE_BUY,
    SELL_OLD_PHONE,
    NOTIFICATIONS,
    CONTACT,
    ADMIN_LOGIN,
    ADMIN_DASHBOARD
}

enum class SortOption(val label: String) {
    RECOMMENDED("Recommended"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    HIGHEST_RATED("Highest Rated"),
    NEWEST("Newest")
}

data class ShopUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val screenStack: List<AppScreen> = listOf(AppScreen.HOME),
    val products: List<Product> = MockData.products,
    val repairServices: List<RepairService> = MockData.repairServices,
    val reviews: List<CustomerReview> = MockData.customerReviews,
    val cartItems: List<CartItem> = emptyList(),
    val selectedProduct: Product? = null,
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val selectedCategorySlug: String = "all",
    val activeCategories: List<CategoryRecord> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.RECOMMENDED,
    val preselectedRepairProblem: String = "Display Replacement",
    val preselectedRepairBrand: String = "Apple",
    val activeRepairBookings: List<RepairBooking> = emptyList(),
    val activeSellRequests: List<SellRequest> = emptyList(),
    val pastOrders: List<ShopOrder> = emptyList(),
    val customerOrders: List<ShopOrder> = emptyList(),
    val selectedOrder: ShopOrder? = null,
    val isOrdersLoading: Boolean = false,
    val ordersErrorMessage: String? = null,
    val lastConfirmedBooking: RepairBooking? = null,
    val lastConfirmedSellRequest: SellRequest? = null,
    val lastConfirmedOrder: ShopOrder? = null,
    val toastMessage: String? = null,
    val isLoginSheetVisible: Boolean = false,
    val isMobileMenuOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isCustomerAuthenticated: Boolean = false,
    val customerUid: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val customerPhotoUrl: String? = null,
    val isGoogleLinked: Boolean = false,
    val isPhoneLinked: Boolean = false,
    val isSendingOtp: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val phoneVerificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    val otpSentPhoneNumber: String? = null,
    val customerAuthError: String? = null,
    val isSubmittingOrder: Boolean = false,
    val isSubmittingRepair: Boolean = false,
    val isSubmittingSell: Boolean = false,
    val userName: String = "Guest Customer",
    val userPhone: String = "",
    val userAddress: String = "Near Nagar Parishad Office, Station Road",
    val userCity: String = "Udgir",
    val userState: String = "Maharashtra",
    val userPin: String = "413517",
    val notifications: List<AppNotification> = emptyList(),
    val reels: List<ReelItem> = MockData.defaultReels,
    val currentReelIndex: Int = 0,
    val isReelMuted: Boolean = false
) {
    val unreadNotificationCount: Int
        get() = notifications.count { !it.isRead }

    val cartItemCount: Int
        get() = cartItems.sumOf { it.quantity }

    val cartSubtotal: Int
        get() = cartItems.sumOf { it.product.originalPrice * it.quantity }

    val cartTotal: Int
        get() = cartItems.sumOf { it.product.price * it.quantity }

    val cartTotalSavings: Int
        get() = cartSubtotal - cartTotal

    val deliveryFee: Int
        get() = if (cartTotal >= 499 || cartItems.isEmpty()) 0 else 49

    // Search query matching across name, category, and brand
    val filteredProducts: List<Product>
        get() {
            var list = if (selectedCategorySlug == "all" || selectedCategory == ProductCategory.ALL) {
                products
            } else {
                products.filter { it.categoryMatches(selectedCategorySlug) || it.category == selectedCategory }
            }

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                    it.brand.lowercase().contains(q) ||
                    it.category.displayName.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)
                }
            }

            return when (sortOption) {
                SortOption.RECOMMENDED -> list.sortedByDescending { it.reviewCount }
                SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
                SortOption.HIGHEST_RATED -> list.sortedByDescending { it.rating }
                SortOption.NEWEST -> list.sortedByDescending { it.releaseOrder }
            }
        }
}

class ShopViewModel(
    private val dataService: DataService = DataServiceLocator.getDataService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    private var notifsJob: kotlinx.coroutines.Job? = null
    private var customerOrdersJob: kotlinx.coroutines.Job? = null
    private val notifiedTransitions = mutableSetOf<String>()

    fun observeCustomerNotifications(uid: String?) {
        notifsJob?.cancel()
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(notifications = emptyList()) }
            return
        }
        notifsJob = viewModelScope.launch {
            dataService.observeNotifications(uid).collect { notifs ->
                _uiState.update { it.copy(notifications = notifs) }
            }
        }
    }

    fun observeCustomerOrders(uid: String?) {
        customerOrdersJob?.cancel()
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(customerOrders = emptyList()) }
            return
        }
        customerOrdersJob = viewModelScope.launch {
            _uiState.update { it.copy(isOrdersLoading = true, ordersErrorMessage = null) }
            try {
                dataService.observeCustomerOrders(uid).collect { orders ->
                    _uiState.update { current ->
                        current.copy(
                            customerOrders = orders,
                            pastOrders = orders,
                            isOrdersLoading = false,
                            ordersErrorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("ShopViewModel", "Error observing customer orders: ${e.message}", e)
                _uiState.update { it.copy(isOrdersLoading = false, ordersErrorMessage = e.message) }
            }
        }
    }

    init {
        // Collect products continuously from DataService (reflects admin changes in real time)
        viewModelScope.launch {
            dataService.observeProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }

        // Collect published reels continuously from DataService
        viewModelScope.launch {
            dataService.observeReels().collect { allReels ->
                val published = allReels.filter { it.isPublished }
                _uiState.update { state ->
                    state.copy(
                        reels = if (published.isNotEmpty()) published else MockData.defaultReels
                    )
                }
            }
        }

        // Collect categories continuously from DataService
        viewModelScope.launch {
            dataService.observeCategories().collect { categories ->
                val active = categories.filter { it.isActive }
                _uiState.update { it.copy(activeCategories = active) }
            }
        }

        // Collect orders & detect customer order status updates
        viewModelScope.launch {
            dataService.observeOrders().collect { orders ->
                _uiState.update { it.copy(pastOrders = orders) }

                val currentUid = _uiState.value.customerUid
                if (!currentUid.isNullOrBlank()) {
                    for (order in orders) {
                        val orderCust = order.customerUid.ifBlank { order.customerId }
                        if (orderCust == currentUid && order.status.isNotBlank()) {
                            val transitionKey = "ORD_${order.orderId}_${order.status.uppercase()}"
                            if (notifiedTransitions.add(transitionKey)) {
                                val notif = AppNotification.createOrderNotification(
                                    orderId = order.orderId,
                                    customerId = currentUid,
                                    status = order.status,
                                    orderDate = order.orderDate
                                )
                                viewModelScope.launch {
                                    dataService.createNotification(notif)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collect repair bookings & detect customer repair status updates
        viewModelScope.launch {
            dataService.observeRepairBookings().collect { repairs ->
                _uiState.update { it.copy(activeRepairBookings = repairs) }

                val currentUid = _uiState.value.customerUid
                if (!currentUid.isNullOrBlank()) {
                    for (repair in repairs) {
                        val repairCust = repair.customerUid.ifBlank { repair.customerId }
                        if (repairCust == currentUid && repair.status.isNotBlank()) {
                            val transitionKey = "REP_${repair.bookingId}_${repair.status.uppercase()}"
                            if (notifiedTransitions.add(transitionKey)) {
                                val notif = AppNotification.createRepairNotification(
                                    bookingId = repair.bookingId,
                                    customerId = currentUid,
                                    status = repair.status,
                                    brand = repair.brand,
                                    model = repair.model,
                                    cost = repair.estimatedCost
                                )
                                viewModelScope.launch {
                                    dataService.createNotification(notif)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Collect sell requests
        viewModelScope.launch {
            dataService.observeOldPhoneRequests().collect { sellRequests ->
                _uiState.update { it.copy(activeSellRequests = sellRequests) }
            }
        }

        // Hydrate saved cart
        viewModelScope.launch {
            try {
                val savedCart = dataService.getCart()
                if (savedCart.isNotEmpty()) {
                    _uiState.update { it.copy(cartItems = savedCart) }
                }
            } catch (e: Exception) {
                // Safe error fallback
            }
        }

        // Observe Customer Firebase Authentication state & persist session
        try {
            CustomerAuthService.getFirebaseAuth()?.addAuthStateListener { fbAuth ->
                val user = fbAuth.currentUser
                if (user != null) {
                    val isGoogle = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
                    val isPhone = user.providerData.any { it.providerId == PhoneAuthProvider.PROVIDER_ID }
                    val name = user.displayName ?: if (!user.phoneNumber.isNullOrBlank()) "Customer (${user.phoneNumber})" else "Customer"
                    val phone = user.phoneNumber?.replace("+91", "")?.trim() ?: _uiState.value.userPhone

                    _uiState.update { current ->
                        current.copy(
                            isCustomerAuthenticated = true,
                            customerUid = user.uid,
                            customerEmail = user.email,
                            customerPhone = user.phoneNumber,
                            customerPhotoUrl = user.photoUrl?.toString(),
                            isGoogleLinked = isGoogle,
                            isPhoneLinked = isPhone,
                            userName = if (user.displayName.isNullOrBlank()) current.userName else name,
                            userPhone = if (phone.isNotBlank()) phone else current.userPhone,
                            customerAuthError = null
                        )
                    }
                    observeCustomerNotifications(user.uid)
                    observeCustomerOrders(user.uid)
                } else {
                    _uiState.update { current ->
                        current.copy(
                            isCustomerAuthenticated = false,
                            customerUid = null,
                            customerEmail = null,
                            customerPhone = null,
                            customerPhotoUrl = null,
                            isGoogleLinked = false,
                            isPhoneLinked = false,
                            notifications = emptyList(),
                            customerOrders = emptyList(),
                            selectedOrder = null,
                            customerAuthError = null
                        )
                    }
                    observeCustomerNotifications(null)
                    observeCustomerOrders(null)
                }
            }
        } catch (e: Exception) {
            Log.w("ShopViewModel", "Auth listener setup skipped or error: ${e.message}")
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { state ->
            val newStack = if (state.currentScreen == screen) state.screenStack else state.screenStack + screen
            state.copy(
                currentScreen = screen,
                screenStack = newStack,
                isMobileMenuOpen = false
            )
        }
    }

    fun toggleMobileMenu(open: Boolean? = null) {
        _uiState.update { it.copy(isMobileMenuOpen = open ?: !it.isMobileMenuOpen) }
    }

    fun navigateBack(): Boolean {
        var handled = false
        _uiState.update { state ->
            if (state.isMobileMenuOpen) {
                handled = true
                state.copy(isMobileMenuOpen = false)
            } else if (state.screenStack.size > 1) {
                val newStack = state.screenStack.dropLast(1)
                handled = true
                state.copy(currentScreen = newStack.last(), screenStack = newStack)
            } else if (state.currentScreen != AppScreen.HOME) {
                handled = true
                state.copy(currentScreen = AppScreen.HOME, screenStack = listOf(AppScreen.HOME))
            } else {
                handled = false
                state
            }
        }
        return handled
    }

    fun selectProduct(product: Product?) {
        if (product == null) {
            _uiState.update { it.copy(toastMessage = "Product is currently unavailable") }
            return
        }
        _uiState.update { state ->
            val newStack = if (state.currentScreen == AppScreen.PRODUCT_DETAIL) state.screenStack else state.screenStack + AppScreen.PRODUCT_DETAIL
            state.copy(
                selectedProduct = product,
                currentScreen = AppScreen.PRODUCT_DETAIL,
                screenStack = newStack
            )
        }
    }

    fun filterByCategory(category: ProductCategory) {
        val slug = category.name.lowercase().replace("_", "-")
        _uiState.update { state ->
            if (category == ProductCategory.REPAIR_SERVICES) {
                state.copy(currentScreen = AppScreen.REPAIR_BOOKING, screenStack = state.screenStack + AppScreen.REPAIR_BOOKING, isMobileMenuOpen = false)
            } else if (category == ProductCategory.OLD_PHONES) {
                state.copy(
                    selectedCategory = ProductCategory.OLD_PHONES,
                    selectedCategorySlug = "old-phones",
                    currentScreen = AppScreen.OLD_PHONE_BUY,
                    screenStack = state.screenStack + AppScreen.OLD_PHONE_BUY,
                    isMobileMenuOpen = false
                )
            } else {
                state.copy(
                    selectedCategory = category,
                    selectedCategorySlug = if (category == ProductCategory.ALL) "all" else slug,
                    currentScreen = AppScreen.PRODUCTS,
                    screenStack = if (state.currentScreen == AppScreen.PRODUCTS) state.screenStack else state.screenStack + AppScreen.PRODUCTS,
                    isMobileMenuOpen = false
                )
            }
        }
    }

    fun filterByCategoryRecord(categoryRecord: CategoryRecord) {
        val slug = categoryRecord.slug.lowercase()
        if (slug == "repair" || categoryRecord.category == ProductCategory.REPAIR_SERVICES) {
            filterByCategory(ProductCategory.REPAIR_SERVICES)
        } else if (slug == "old-phones" || slug == "old_phones" || categoryRecord.category == ProductCategory.OLD_PHONES) {
            filterByCategory(ProductCategory.OLD_PHONES)
        } else {
            _uiState.update { state ->
                state.copy(
                    selectedCategory = categoryRecord.category,
                    selectedCategorySlug = slug,
                    currentScreen = AppScreen.PRODUCTS,
                    screenStack = if (state.currentScreen == AppScreen.PRODUCTS) state.screenStack else state.screenStack + AppScreen.PRODUCTS,
                    isMobileMenuOpen = false
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        _uiState.update { state ->
            val existingIndex = state.cartItems.indexOfFirst { it.product.id == product.id }
            val newItems = if (existingIndex != -1) {
                state.cartItems.mapIndexed { index, item ->
                    if (index == existingIndex) item.copy(quantity = item.quantity + quantity) else item
                }
            } else {
                state.cartItems + CartItem(product = product, quantity = quantity)
            }
            viewModelScope.launch {
                dataService.saveCart(newItems)
            }
            state.copy(
                cartItems = newItems,
                toastMessage = "Added ${product.name.take(24)}... to cart"
            )
        }
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        _uiState.update { state ->
            val newItems = state.cartItems.mapNotNull { item ->
                if (item.product.id == productId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else {
                    item
                }
            }
            viewModelScope.launch {
                dataService.saveCart(newItems)
            }
            state.copy(cartItems = newItems)
        }
    }

    fun removeFromCart(productId: String) {
        _uiState.update { state ->
            val removedItem = state.cartItems.find { it.product.id == productId }
            val newItems = state.cartItems.filterNot { it.product.id == productId }
            viewModelScope.launch {
                dataService.saveCart(newItems)
            }
            state.copy(
                cartItems = newItems,
                toastMessage = removedItem?.let { "Removed ${it.product.name.take(20)} from cart" }
            )
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            dataService.clearCart()
        }
        _uiState.update { it.copy(cartItems = emptyList()) }
    }

    fun buyNow(product: Product) {
        addToCart(product, 1)
        navigateTo(AppScreen.CHECKOUT)
    }

    fun bookRepairWithProblem(problem: String, brand: String = "Apple") {
        _uiState.update { state ->
            state.copy(
                preselectedRepairProblem = problem,
                preselectedRepairBrand = brand,
                currentScreen = AppScreen.REPAIR_BOOKING,
                screenStack = state.screenStack + AppScreen.REPAIR_BOOKING,
                isMobileMenuOpen = false
            )
        }
    }

    private val orderCounter = java.util.concurrent.atomic.AtomicInteger(1001)
    private val repairCounter = java.util.concurrent.atomic.AtomicInteger(1001)
    private val sellCounter = java.util.concurrent.atomic.AtomicInteger(1001)

    fun submitRepairBooking(
        customerName: String,
        mobileNumber: String,
        brand: String,
        model: String,
        problem: String,
        pickupRequired: Boolean,
        address: String,
        preferredDate: String,
        additionalDetails: String
    ): kotlinx.coroutines.Job {
        if (_uiState.value.isSubmittingRepair) {
            Log.w("ShopViewModel", "submitRepairBooking skipped: duplicate submission prevented")
            return kotlinx.coroutines.Job().apply { complete() }
        }

        val trimmedName = customerName.trim()
        val trimmedPhone = mobileNumber.trim()
        val trimmedModel = model.trim()

        if (trimmedName.isBlank() || trimmedPhone.isBlank() || trimmedModel.isBlank()) {
            val err = "Please enter customer name, phone number, and device model."
            _uiState.update { it.copy(isSubmittingRepair = false, toastMessage = err) }
            return kotlinx.coroutines.Job().apply { complete() }
        }

        _uiState.update { it.copy(isSubmittingRepair = true) }

        return viewModelScope.launch {
            try {
                val nextNumber = 1000 + _uiState.value.activeRepairBookings.size + repairCounter.getAndIncrement()
                val bookingId = "SKM-REP-$nextNumber"
                val currentUid = try {
                    FirebaseConfig.getAuth().currentUser?.uid
                } catch (e: Exception) {
                    null
                } ?: _uiState.value.customerUid.orEmpty()

                val booking = RepairBooking(
                    bookingId = bookingId,
                    customerName = trimmedName,
                    mobileNumber = trimmedPhone,
                    brand = brand.trim(),
                    model = trimmedModel,
                    problem = problem.trim(),
                    pickupRequired = pickupRequired,
                    address = address.trim(),
                    preferredDate = preferredDate.trim(),
                    additionalDetails = additionalDetails.trim(),
                    status = if (pickupRequired) "Pickup Scheduled" else "Appointment Booked",
                    customerUid = currentUid
                )

                val result = dataService.createRepairBooking(booking)
                result.fold(
                    onSuccess = { createdBooking ->
                        _uiState.update { state ->
                            val updatedList = listOf(createdBooking) + state.activeRepairBookings.filterNot { it.bookingId == createdBooking.bookingId }
                            state.copy(
                                activeRepairBookings = updatedList,
                                lastConfirmedBooking = createdBooking,
                                toastMessage = "Your repair request ($bookingId) has been booked successfully."
                            )
                        }
                    },
                    onFailure = { error ->
                        val errMsg = error.message ?: "Failed to book repair. Please try again."
                        Log.e("ShopViewModel", "createRepairBooking failed: $errMsg", error)
                        _uiState.update { state ->
                            state.copy(toastMessage = errMsg)
                        }
                    }
                )
            } catch (e: Exception) {
                val errMsg = e.message ?: "An unexpected error occurred while booking repair."
                Log.e("ShopViewModel", "submitRepairBooking error", e)
                _uiState.update { state ->
                    state.copy(toastMessage = errMsg)
                }
            } finally {
                _uiState.update { it.copy(isSubmittingRepair = false) }
            }
        }
    }

    fun submitSellRequest(
        customerName: String,
        mobileNumber: String,
        brand: String,
        model: String,
        storage: String,
        condition: String,
        expectedPrice: String,
        address: String
    ): SellRequest {
        if (_uiState.value.isSubmittingSell) {
            _uiState.value.lastConfirmedSellRequest?.let { return it }
        }
        _uiState.update { it.copy(isSubmittingSell = true) }

        val nextNumber = 1000 + _uiState.value.activeSellRequests.size + sellCounter.getAndIncrement()
        val requestId = "SKM-SELL-$nextNumber"
        val currentUid = try {
            FirebaseConfig.getAuth().currentUser?.uid
        } catch (e: Exception) {
            null
        } ?: _uiState.value.customerUid.orEmpty()

        // Realistic valuation calculation based on condition & storage
        val baseVal = when (brand.lowercase()) {
            "apple" -> 22000
            "oneplus" -> 16000
            "samsung" -> 14000
            else -> 10000
        }
        val conditionMult = when (condition) {
            "Like New (Flawless)" -> 1.0
            "Good (Minor usage)" -> 0.85
            "Fair (Scratches/Dents)" -> 0.70
            else -> 0.45
        }
        val quoteMin = (baseVal * conditionMult * 0.9).toInt()
        val quoteMax = (baseVal * conditionMult * 1.15).toInt()

        val request = SellRequest(
            requestId = requestId,
            customerName = customerName,
            mobileNumber = mobileNumber,
            brand = brand,
            model = model,
            storage = storage,
            condition = condition,
            expectedPrice = expectedPrice,
            address = address,
            estimatedQuoteMin = quoteMin,
            estimatedQuoteMax = quoteMax,
            customerUid = currentUid
        )

        _uiState.update { state ->
            state.copy(
                isSubmittingSell = false,
                activeSellRequests = listOf(request) + state.activeSellRequests,
                lastConfirmedSellRequest = request,
                toastMessage = "Valuation created! Quote: ₹$quoteMin - ₹$quoteMax"
            )
        }
        viewModelScope.launch {
            try {
                dataService.createOldPhoneRequest(request)
            } catch (e: Exception) {
                Log.w("ShopViewModel", "Sell request offline or network fallback: ${e.message}")
            }
        }
        return request
    }

    fun placeOrder(
        customerName: String,
        mobileNumber: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        deliveryOption: String,
        paymentMethod: String
    ): ShopOrder {
        if (_uiState.value.isSubmittingOrder) {
            _uiState.value.lastConfirmedOrder?.let { return it }
        }
        _uiState.update { it.copy(isSubmittingOrder = true) }

        val currentUiState = _uiState.value
        val nextNumber = 1000 + currentUiState.pastOrders.size + orderCounter.getAndIncrement()
        val orderId = "SKM-ORD-$nextNumber"
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val currentUid = try {
            FirebaseConfig.getAuth().currentUser?.uid
        } catch (e: Exception) {
            null
        } ?: currentUiState.customerUid.orEmpty()

        val idempotencyKey = "IDEMP_${orderId}_${System.currentTimeMillis()}"
        val isCod = paymentMethod.contains("Cash on Delivery", ignoreCase = true)
        val initialPaymentStatus = if (isCod) "Pending" else "Paid"
        val initialSettlementStatus = if (isCod) "Unsettled" else "Pending Settlement"
        val gatewayRef = if (isCod) "COD-${orderId}" else "GATEWAY-REF-${orderId}"

        val order = ShopOrder(
            orderId = orderId,
            items = currentUiState.cartItems,
            subtotal = currentUiState.cartSubtotal,
            discount = currentUiState.cartTotalSavings,
            deliveryFee = currentUiState.deliveryFee,
            totalAmount = currentUiState.cartTotal + currentUiState.deliveryFee,
            customerName = customerName,
            mobileNumber = mobileNumber,
            address = address,
            city = city,
            state = state,
            pinCode = pinCode,
            deliveryOption = deliveryOption,
            paymentMethod = paymentMethod,
            orderDate = currentDate,
            status = "Order Confirmed",
            customerUid = currentUid,
            paymentGatewayRef = gatewayRef,
            transactionStatus = initialPaymentStatus,
            bankSettlementStatus = initialSettlementStatus,
            idempotencyKey = idempotencyKey
        )

        _uiState.update { current ->
            current.copy(
                isSubmittingOrder = false,
                pastOrders = listOf(order) + current.pastOrders,
                customerOrders = listOf(order) + current.customerOrders,
                lastConfirmedOrder = order,
                selectedOrder = order,
                cartItems = emptyList(),
                currentScreen = AppScreen.ORDER_SUCCESS,
                screenStack = current.screenStack + AppScreen.ORDER_SUCCESS,
                toastMessage = "Order $orderId placed successfully!"
            )
        }
        viewModelScope.launch {
            try {
                dataService.createOrder(order)
                dataService.clearCart()
            } catch (e: Exception) {
                Log.w("ShopViewModel", "Order creation offline or network fallback: ${e.message}")
            }
        }
        return order
    }

    fun clearConfirmedBooking() {
        _uiState.update { it.copy(lastConfirmedBooking = null) }
    }

    fun clearConfirmedSellRequest() {
        _uiState.update { it.copy(lastConfirmedSellRequest = null) }
    }

    fun clearConfirmedOrder() {
        _uiState.update { it.copy(lastConfirmedOrder = null) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun setLoginSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isLoginSheetVisible = visible) }
    }

    fun setCustomerAuthError(error: String?) {
        _uiState.update { it.copy(customerAuthError = error) }
    }

    /**
     * Customer Google Sign-In via Credential Manager token.
     */
    fun signInCustomerWithGoogle(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        _uiState.update { it.copy(isLoading = true, customerAuthError = null) }
        viewModelScope.launch {
            val result = CustomerAuthService.signInWithGoogle(idToken)
            _uiState.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            toastMessage = "Welcome, ${user.displayName ?: "Customer"}!"
                        )
                    }
                    onComplete(true, null)
                },
                onFailure = { error ->
                    val errorMsg = CustomerAuthService.parseAuthError(error)
                    _uiState.update { it.copy(customerAuthError = errorMsg) }
                    onComplete(false, errorMsg)
                }
            )
        }
    }

    /**
     * Initiate phone OTP dispatch using real Firebase Phone Authentication.
     */
    fun sendCustomerPhoneOtp(
        activity: Activity,
        rawPhoneInput: String,
        isResend: Boolean = false,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val formattedPhone = CustomerAuthService.sanitizeAndValidateIndianPhone(rawPhoneInput)
        if (formattedPhone == null) {
            val err = "Please enter a valid 10-digit Indian mobile number."
            _uiState.update { it.copy(customerAuthError = err) }
            onComplete(false, err)
            return
        }

        _uiState.update {
            it.copy(
                isSendingOtp = true,
                customerAuthError = null,
                otpSentPhoneNumber = formattedPhone
            )
        }

        val resendToken = if (isResend) _uiState.value.resendToken else null

        CustomerAuthService.sendVerificationCode(
            activity = activity,
            phoneNumberE164 = formattedPhone,
            forceResendingToken = resendToken,
            onCodeSent = { verificationId, token ->
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        phoneVerificationId = verificationId,
                        resendToken = token,
                        toastMessage = "OTP sent to $formattedPhone via SMS"
                    )
                }
                onComplete(true, null)
            },
            onVerificationCompleted = { phoneCredential ->
                _uiState.update { it.copy(isSendingOtp = false) }
                // Auto-retrieval / instant verification completed by Google Play Services
                viewModelScope.launch {
                    val signInResult = CustomerAuthService.signInWithPhoneCredential(phoneCredential)
                    signInResult.fold(
                        onSuccess = { user ->
                            _uiState.update {
                                it.copy(
                                    phoneVerificationId = null,
                                    toastMessage = "Phone verified automatically! Welcome, ${user.phoneNumber}."
                                )
                            }
                            onComplete(true, null)
                        },
                        onFailure = { err ->
                            val msg = CustomerAuthService.parseAuthError(err)
                            _uiState.update { it.copy(customerAuthError = msg) }
                            onComplete(false, msg)
                        }
                    )
                }
            },
            onVerificationFailed = { error ->
                val errorMsg = CustomerAuthService.parseAuthError(error)
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        customerAuthError = errorMsg
                    )
                }
                onComplete(false, errorMsg)
            }
        )
    }

    /**
     * Verify customer OTP manually entered into UI.
     */
    fun verifyCustomerOtp(
        otpCode: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val verificationId = _uiState.value.phoneVerificationId
        if (verificationId.isNullOrBlank()) {
            val err = "Session expired. Please request a new OTP."
            _uiState.update { it.copy(customerAuthError = err) }
            onComplete(false, err)
            return
        }

        val cleanOtp = otpCode.trim()
        if (cleanOtp.length != 6 || !cleanOtp.all { it.isDigit() }) {
            val err = "Please enter the complete 6-digit OTP received via SMS."
            _uiState.update { it.copy(customerAuthError = err) }
            onComplete(false, err)
            return
        }

        _uiState.update { it.copy(isVerifyingOtp = true, customerAuthError = null) }

        val credential = PhoneAuthProvider.getCredential(verificationId, cleanOtp)
        viewModelScope.launch {
            val currentUser = CustomerAuthService.getFirebaseAuth()?.currentUser
            // Check if customer is already signed in (linking flow) or signing in fresh
            val result = if (currentUser != null && !_uiState.value.isPhoneLinked) {
                CustomerAuthService.linkCredential(currentUser, credential)
            } else {
                CustomerAuthService.signInWithPhoneCredential(credential)
            }

            _uiState.update { it.copy(isVerifyingOtp = false) }

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            phoneVerificationId = null,
                            toastMessage = "Phone verified successfully! Welcome to SK Mobile Shoppy and Electronics."
                        )
                    }
                    onComplete(true, null)
                },
                onFailure = { error ->
                    val msg = CustomerAuthService.parseAuthError(error)
                    _uiState.update { it.copy(customerAuthError = msg) }
                    onComplete(false, msg)
                }
            )
        }
    }

    /**
     * Link Google account to an existing Phone-authenticated customer session.
     */
    fun linkGoogleAccount(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = CustomerAuthService.getFirebaseAuth()?.currentUser
        if (currentUser == null) {
            onComplete(false, "No active customer session to link.")
            return
        }

        _uiState.update { it.copy(isLoading = true, customerAuthError = null) }
        viewModelScope.launch {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = CustomerAuthService.linkCredential(currentUser, credential)
            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            toastMessage = "Google account (${user.email}) linked successfully!"
                        )
                    }
                    onComplete(true, null)
                },
                onFailure = { error ->
                    val msg = CustomerAuthService.parseAuthError(error)
                    _uiState.update { it.copy(customerAuthError = msg) }
                    onComplete(false, msg)
                }
            )
        }
    }

    /**
     * Customer Logout.
     */
    fun logoutCustomer() {
        CustomerAuthService.signOut()
        observeCustomerNotifications(null)
        observeCustomerOrders(null)
        notifiedTransitions.clear()
        _uiState.update {
            it.copy(
                isCustomerAuthenticated = false,
                customerUid = null,
                customerEmail = null,
                customerPhone = null,
                customerPhotoUrl = null,
                isGoogleLinked = false,
                isPhoneLinked = false,
                phoneVerificationId = null,
                customerAuthError = null,
                userName = "Guest Customer",
                userPhone = "",
                pastOrders = emptyList(),
                customerOrders = emptyList(),
                selectedOrder = null,
                activeRepairBookings = emptyList(),
                activeSellRequests = emptyList(),
                notifications = emptyList(),
                lastConfirmedOrder = null,
                lastConfirmedBooking = null,
                lastConfirmedSellRequest = null,
                toastMessage = "Signed out of customer account."
            )
        }
    }

    fun markNotificationAsRead(id: String) {
        val uid = _uiState.value.customerUid ?: return
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map {
                if (it.id == id) it.copy(isRead = true) else it
            })
        }
        viewModelScope.launch {
            dataService.markNotificationAsRead(id, uid)
        }
    }

    fun markAllNotificationsAsRead() {
        val uid = _uiState.value.customerUid ?: return
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        }
        viewModelScope.launch {
            dataService.markAllNotificationsAsRead(uid)
        }
    }

    fun clearNotifications() {
        val uid = _uiState.value.customerUid ?: return
        _uiState.update { it.copy(notifications = emptyList()) }
        viewModelScope.launch {
            dataService.clearNotifications(uid)
        }
    }

    fun updateUserProfile(name: String, phone: String, address: String) {
        _uiState.update {
            it.copy(
                userName = name,
                userPhone = phone,
                userAddress = address,
                isLoginSheetVisible = false,
                toastMessage = "Profile updated!"
            )
        }
        val currentUser = CustomerAuthService.getFirebaseAuth()?.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                CustomerAuthService.syncCustomerProfileToFirestore(
                    user = currentUser,
                    customDeliveryAddress = address
                )
            }
        }
    }

    fun toggleReelLike(reelId: String) {
        val currentReels = _uiState.value.reels
        val reel = currentReels.find { it.id == reelId } ?: return
        val newLiked = !reel.isLikedByMe
        val newLikesCount = if (newLiked) reel.likesCount + 1 else (reel.likesCount - 1).coerceAtLeast(0)
        _uiState.update { state ->
            state.copy(
                reels = state.reels.map {
                    if (it.id == reelId) it.copy(isLikedByMe = newLiked, likesCount = newLikesCount) else it
                }
            )
        }
        viewModelScope.launch {
            dataService.toggleReelLike(reelId, newLiked)
        }
    }

    fun shareReel(reelId: String) {
        _uiState.update { state ->
            state.copy(
                reels = state.reels.map {
                    if (it.id == reelId) it.copy(sharesCount = it.sharesCount + 1) else it
                }
            )
        }
        viewModelScope.launch {
            dataService.incrementReelShare(reelId)
        }
    }

    fun toggleReelMute() {
        _uiState.update { it.copy(isReelMuted = !it.isReelMuted) }
    }

    fun setCurrentReelIndex(index: Int) {
        _uiState.update { it.copy(currentReelIndex = index) }
    }

    fun openReelProduct(productId: String) {
        val product = _uiState.value.products.find { it.id == productId }
        if (product != null) {
            selectProduct(product)
            navigateTo(AppScreen.PRODUCT_DETAIL)
        } else {
            navigateTo(AppScreen.PRODUCTS)
        }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    // --- CUSTOMER ORDER TRACKING ---

    fun selectOrderForDetail(order: ShopOrder) {
        _uiState.update { it.copy(selectedOrder = order) }
        navigateTo(AppScreen.ORDER_DETAIL)
    }

    fun navigateToOrderTracking(orderId: String) {
        val found = _uiState.value.customerOrders.find { it.orderId == orderId }
            ?: _uiState.value.pastOrders.find { it.orderId == orderId }
        if (found != null) {
            selectOrderForDetail(found)
        } else {
            viewModelScope.launch {
                val order = dataService.getOrderById(orderId)
                if (order != null) {
                    selectOrderForDetail(order)
                } else {
                    showToast("Order #$orderId not found")
                }
            }
        }
    }

    fun refreshCustomerOrders() {
        val uid = _uiState.value.customerUid
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdersLoading = true, ordersErrorMessage = null) }
            try {
                val orders = if (!uid.isNullOrBlank()) {
                    dataService.getCustomerOrders(uid)
                } else {
                    val currentPhone = _uiState.value.userPhone
                    val currentName = _uiState.value.userName
                    dataService.getOrders().filter {
                        (currentPhone.isNotBlank() && it.mobileNumber == currentPhone) ||
                        (currentName.isNotBlank() && it.customerName.equals(currentName, ignoreCase = true))
                    }
                }
                _uiState.update {
                    it.copy(
                        customerOrders = orders,
                        pastOrders = orders,
                        isOrdersLoading = false,
                        ordersErrorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.w("ShopViewModel", "refreshCustomerOrders error", e)
                _uiState.update {
                    it.copy(
                        isOrdersLoading = false,
                        ordersErrorMessage = e.message ?: "Failed to refresh orders"
                    )
                }
            }
        }
    }

    fun cancelCustomerOrder(orderId: String, reason: String) {
        val uid = _uiState.value.customerUid ?: "CUSTOMER"
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdersLoading = true) }
            val result = dataService.cancelOrder(orderId, reason, uid)
            result.fold(
                onSuccess = { cancelledOrder ->
                    showToast("Order #${cancelledOrder.orderId} cancelled.")
                    _uiState.update { current ->
                        val updatedList = current.customerOrders.map { if (it.orderId == orderId) cancelledOrder else it }
                        val currentSelected = if (current.selectedOrder?.orderId == orderId) cancelledOrder else current.selectedOrder
                        current.copy(
                            customerOrders = updatedList,
                            pastOrders = updatedList,
                            selectedOrder = currentSelected,
                            isOrdersLoading = false
                        )
                    }
                },
                onFailure = { err ->
                    showToast(err.message ?: "Failed to cancel order")
                    _uiState.update { it.copy(isOrdersLoading = false) }
                }
            )
        }
    }
}
