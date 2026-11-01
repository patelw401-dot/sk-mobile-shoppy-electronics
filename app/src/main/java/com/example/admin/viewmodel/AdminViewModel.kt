package com.example.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin.auth.AdminAuthService
import com.example.admin.model.AdminReviewRecord
import com.example.admin.model.AdminTab
import com.example.admin.model.CategoryRecord
import com.example.admin.model.CustomerRecord
import com.example.admin.model.PaymentRecord
import com.example.admin.model.ShopSettings
import com.example.data.firebase.FirebaseConfig
import com.example.data.service.DataService
import com.example.data.service.DataServiceLocator
import com.example.data.service.LocalDataService
import com.example.model.CustomerReview
import com.example.model.MockData
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.ReelItem
import com.example.model.RepairBooking
import com.example.model.SellRequest
import com.example.model.ShopOrder
import android.app.Activity
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val adminEmail: String? = null,
    val currentTab: AdminTab = AdminTab.DASHBOARD,
    val isSidebarOpen: Boolean = false,
    val products: List<Product> = MockData.products,
    val orders: List<ShopOrder> = emptyList(),
    val repairs: List<RepairBooking> = emptyList(),
    val sellRequests: List<SellRequest> = emptyList(),
    val customers: List<CustomerRecord> = emptyList(),
    val categories: List<CategoryRecord> = emptyList(),
    val reviews: List<AdminReviewRecord> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val settings: ShopSettings = ShopSettings(),
    val selectedReportFilter: String = "30 Days",
    val toastMessage: String? = null,
    val reels: List<ReelItem> = MockData.defaultReels,
    val isUploadingReel: Boolean = false,
    val uploadReelProgress: Int = 0
) {
    val totalOrdersCount: Int get() = orders.size
    val pendingOrdersCount: Int get() = orders.count { it.status == "Pending" || it.status == "Confirmed" }
    val totalRepairsCount: Int get() = repairs.size
    val completedRepairsCount: Int get() = repairs.count { it.status == "Delivered" || it.status == "Ready" }
    val totalSalesAmount: Int get() = orders.sumOf { it.totalAmount } + repairs.count { it.status == "Delivered" } * 950
    val estimatedProfitAmount: Int get() = (totalSalesAmount * 0.28).toInt()
    val oldPhonesPurchasedCount: Int get() = sellRequests.count { it.status == "Purchased" || it.status == "Accepted" }
    val oldPhonesPendingCount: Int get() = sellRequests.count { it.status != "Purchased" && it.status != "Rejected" }
    val totalCustomersCount: Int get() = customers.size
}

class AdminViewModel(
    private val dataService: DataService = DataServiceLocator.getDataService()
) : ViewModel() {

    private val auth by lazy {
        try {
            FirebaseConfig.getAuth()
        } catch (e: Exception) {
            null
        }
    }

    private val _uiState = MutableStateFlow(
        AdminUiState(
            customers = listOf(
                CustomerRecord("CUST-1", "Sachin Patil", "9823456789", "Nagar Parishad Road, Udgir", 3, 1, 38500, "Today"),
                CustomerRecord("CUST-2", "Ganesh Kulkarni", "9890123456", "Shivaji Chowk, Udgir", 4, 0, 14200, "Today"),
                CustomerRecord("CUST-3", "Pooja Shinde", "9765432109", "Bidar Road, Udgir", 2, 2, 34800, "Yesterday"),
                CustomerRecord("CUST-4", "Imran Shaikh", "9988776655", "Nanded Naka, Udgir", 5, 1, 9800, "2 days ago")
            ),
            categories = listOf(
                CategoryRecord("CAT-1", "Mobile Phones", "mobile-phones", ProductCategory.MOBILE_PHONES, true, 4),
                CategoryRecord("CAT-2", "Old Phones", "old-phones", ProductCategory.OLD_PHONES, true, 3),
                CategoryRecord("CAT-3", "Mobile Repair", "mobile-repair", ProductCategory.REPAIR_SERVICES, true, 6),
                CategoryRecord("CAT-4", "Chargers", "chargers", ProductCategory.CHARGERS, true, 3),
                CategoryRecord("CAT-5", "Data Cables", "data-cables", ProductCategory.DATA_CABLES, true, 3),
                CategoryRecord("CAT-6", "Headphones", "headphones", ProductCategory.HEADPHONES, true, 2),
                CategoryRecord("CAT-7", "Bluetooth", "bluetooth", ProductCategory.BLUETOOTH, true, 3),
                CategoryRecord("CAT-8", "Accessories", "accessories", ProductCategory.ACCESSORIES, true, 3)
            ),
            reviews = MockData.customerReviews.map { AdminReviewRecord(it, isApproved = true) },
            payments = listOf(
                PaymentRecord("PAY-1001", "SKM-ORD-1001", "Sachin Patil", 24999, "UPI", "Paid", "Today, 11:32 AM"),
                PaymentRecord("PAY-1002", "SKM-ORD-1002", "Ganesh Kulkarni", 1998, "Cash on Delivery", "Pending", "Today, 09:15 AM"),
                PaymentRecord("PAY-1003", "SKM-ORD-1003", "Pooja Shinde", 28499, "Online Payment", "Paid", "Yesterday, 04:46 PM"),
                PaymentRecord("PAY-1004", "SKM-REP-1001", "Ramesh Kumar", 2499, "UPI", "Paid", "Today, 02:10 PM")
            )
        )
    )
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        // Collect products continuously from DataService and update category counts
        viewModelScope.launch {
            dataService.observeProducts().collect { products ->
                _uiState.update { state ->
                    val updatedCategories = state.categories.map { cat ->
                        val count = products.count { p -> p.categoryMatches(cat.slug) || (p.category == cat.category) }
                        cat.copy(productCount = count)
                    }
                    state.copy(products = products, categories = updatedCategories)
                }
            }
        }

        // Collect categories continuously from DataService
        viewModelScope.launch {
            dataService.observeCategories().collect { rawCategories ->
                val currentProducts = _uiState.value.products
                val enriched = rawCategories.map { cat ->
                    val count = currentProducts.count { p -> p.categoryMatches(cat.slug) || (p.category == cat.category) }
                    cat.copy(productCount = count)
                }
                _uiState.update { it.copy(categories = enriched) }
            }
        }

        // Collect orders and derive payment transaction records dynamically
        viewModelScope.launch {
            dataService.observeOrders().collect { orders ->
                val dynamicPayments = if (orders.isNotEmpty()) {
                    orders.mapIndexed { index, order ->
                        PaymentRecord(
                            paymentId = if (order.paymentGatewayRef.isNotBlank()) order.paymentGatewayRef else "PAY-${1000 + index}",
                            orderId = order.orderId,
                            customerName = order.customerName,
                            amount = order.totalAmount,
                            method = order.paymentMethod,
                            status = order.paymentStatus,
                            date = order.orderDate,
                            settlementStatus = order.bankSettlementStatus,
                            transactionRef = order.paymentGatewayRef,
                            idempotencyKey = order.idempotencyKey
                        )
                    }
                } else {
                    _uiState.value.payments
                }
                _uiState.update { it.copy(orders = orders, payments = dynamicPayments) }
            }
        }

        // Collect repair bookings
        viewModelScope.launch {
            dataService.observeRepairBookings().collect { repairs ->
                _uiState.update { it.copy(repairs = repairs) }
            }
        }

        // Collect sell requests
        viewModelScope.launch {
            dataService.observeOldPhoneRequests().collect { requests ->
                _uiState.update { it.copy(sellRequests = requests) }
            }
        }

        // Load settings
        viewModelScope.launch {
            try {
                val settings = dataService.getSettings()
                _uiState.update { it.copy(settings = settings) }
            } catch (e: Exception) {
                // Safe fallback
            }
        }

        // Load customers from DataService
        viewModelScope.launch {
            try {
                val customers = dataService.getCustomers()
                if (customers.isNotEmpty()) {
                    _uiState.update { it.copy(customers = customers) }
                }
            } catch (e: Exception) {
                // Safe fallback
            }
        }

        // Collect all reels from DataService
        viewModelScope.launch {
            dataService.observeReels().collect { reels ->
                _uiState.update { it.copy(reels = reels) }
            }
        }

        // Observe Firebase Auth state and verify administrator authorization
        try {
            auth?.addAuthStateListener { fbAuth ->
                val user = fbAuth.currentUser
                if (user != null) {
                    viewModelScope.launch {
                        val isAuthorized = AdminAuthService.verifyAdminAuthorization(user)
                        if (isAuthorized) {
                            _uiState.update {
                                it.copy(
                                    isAuthenticated = true,
                                    adminEmail = user.email ?: user.displayName ?: "Admin"
                                )
                            }
                        } else {
                            // Customer account: do not grant admin privileges
                            _uiState.update {
                                it.copy(
                                    isAuthenticated = false,
                                    adminEmail = null
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthenticated = false,
                            adminEmail = null
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }

        // Check for pending Web OAuth results in case Activity was recreated
        try {
            auth?.pendingAuthResult?.let { pending ->
                Log.i("AdminViewModel", "Found pendingAuthResult during ViewModel initialization; attaching listener")
                pending.addOnSuccessListener { result ->
                    Log.i("AdminViewModel", "pendingAuthResult on init succeeded for user: ${result.user?.email}")
                    handleAuthResult(result) { _, _ -> }
                }.addOnFailureListener { error ->
                    Log.w("AdminViewModel", "pendingAuthResult on init failed: ${error.javaClass.simpleName} - ${error.message}")
                }
            }
        } catch (e: Exception) {
            Log.w("AdminViewModel", "Error checking pendingAuthResult on init: ${e.message}")
        }
    }

    /**
     * Authenticate Administrator using Google Sign-In with CredentialManager.
     * Enforces server-side authorization check before granting admin dashboard access.
     */
    fun signInWithGoogleToken(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        val fbAuth = auth ?: run {
            Log.e("AdminViewModel", "Firebase Auth is null in signInWithGoogleToken")
            onComplete(false, "Firebase Authentication is currently unavailable.")
            return
        }

        _uiState.update { it.copy(isAuthenticating = true) }
        Log.i("AdminViewModel", "Authenticating Firebase with Google ID token credential")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        fbAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                Log.i("AdminViewModel", "Firebase signInWithCredential succeeded for user: ${result.user?.email}")
                handleAuthResult(result, onComplete)
            }
            .addOnFailureListener { error ->
                _uiState.update { it.copy(isAuthenticating = false) }
                val errorMsg = AdminAuthService.parseAuthError(error)
                Log.e("AdminViewModel", "Firebase signInWithCredential failed: ${error.javaClass.simpleName} ($errorMsg)", error)
                onComplete(false, errorMsg)
            }
    }

    /**
     * Authenticate Administrator using Firebase OAuthProvider with Activity flow.
     * This web OAuth flow leverages the authorized Firebase Web Client ID and does NOT
     * require manual SHA-1 fingerprint registration in Firebase Console.
     */
    fun signInWithGoogleProvider(activity: Activity, onComplete: (Boolean, String?) -> Unit) {
        val fbAuth = auth ?: run {
            Log.e("AdminViewModel", "Firebase Auth is null in signInWithGoogleProvider")
            onComplete(false, "Firebase Authentication is currently unavailable.")
            return
        }

        _uiState.update { it.copy(isAuthenticating = true) }
        val provider = OAuthProvider.newBuilder("google.com").apply {
            addCustomParameter("prompt", "select_account")
        }.build()

        val pending = fbAuth.pendingAuthResult
        if (pending != null) {
            Log.i("AdminViewModel", "Found existing pendingAuthResult; attaching callbacks")
            pending.addOnSuccessListener { result ->
                Log.i("AdminViewModel", "Pending auth succeeded for user: ${result.user?.email}")
                handleAuthResult(result, onComplete)
            }.addOnFailureListener { error ->
                _uiState.update { it.copy(isAuthenticating = false) }
                val errorMsg = AdminAuthService.parseAuthError(error, activity)
                Log.e("AdminViewModel", "Pending auth failed: ${error.javaClass.simpleName} ($errorMsg)", error)
                onComplete(false, errorMsg)
            }
            return
        }

        Log.i("AdminViewModel", "Launching startActivityForSignInWithProvider on activity: ${activity.localClassName}")
        fbAuth.startActivityForSignInWithProvider(activity, provider)
            .addOnSuccessListener { result ->
                Log.i("AdminViewModel", "startActivityForSignInWithProvider succeeded for user: ${result.user?.email}")
                handleAuthResult(result, onComplete)
            }
            .addOnFailureListener { error ->
                _uiState.update { it.copy(isAuthenticating = false) }
                val errorMsg = AdminAuthService.parseAuthError(error, activity)
                Log.e("AdminViewModel", "startActivityForSignInWithProvider failed: ${error.javaClass.simpleName} ($errorMsg)", error)
                onComplete(false, errorMsg)
            }
    }

    private fun handleAuthResult(result: AuthResult, onComplete: (Boolean, String?) -> Unit) {
        val user = result.user
        if (user == null) {
            _uiState.update { it.copy(isAuthenticating = false) }
            onComplete(false, "Authentication failed. No user profile returned.")
            return
        }

        // Verify server-side authorization (Requirement 7)
        viewModelScope.launch {
            val isAuthorized = AdminAuthService.verifyAdminAuthorization(user)
            _uiState.update { it.copy(isAuthenticating = false) }

            if (isAuthorized) {
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        adminEmail = user.email ?: user.displayName ?: "Admin",
                        toastMessage = "Welcome, ${user.displayName ?: "Administrator"}!"
                    )
                }
                onComplete(true, null)
            } else {
                // Customer or unauthorized account
                auth?.signOut()
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        adminEmail = null
                    )
                }
                onComplete(
                    false,
                    "Access Denied: Google Account '${user.email}' is not registered as an authorized store administrator."
                )
            }
        }
    }

    fun logout() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            // Ignored
        }
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentTab = AdminTab.DASHBOARD,
                adminEmail = null,
                toastMessage = "You have logged out of the Admin Portal."
            )
        }
    }

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(currentTab = tab, isSidebarOpen = false) }
    }

    fun toggleSidebar(open: Boolean? = null) {
        _uiState.update { it.copy(isSidebarOpen = open ?: !it.isSidebarOpen) }
    }

    // Product actions
    fun addProduct(product: Product) {
        viewModelScope.launch {
            dataService.createProduct(product)
            _uiState.update {
                it.copy(
                    products = listOf(product) + it.products,
                    toastMessage = "Added ${product.name.take(20)}..."
                )
            }
        }
    }

    fun updateProduct(updated: Product) {
        viewModelScope.launch {
            dataService.updateProduct(updated)
            _uiState.update { state ->
                val updatedList = state.products.map { if (it.id == updated.id) updated else it }
                state.copy(products = updatedList, toastMessage = "Product updated!")
            }
        }
    }

    fun toggleProductActive(productId: String) {
        val prod = _uiState.value.products.find { it.id == productId } ?: return
        val newActiveState = !prod.inStock
        val updated = prod.copy(
            inStock = newActiveState,
            stockStatus = if (newActiveState) (if (prod.stock > 0) "In Stock" else "Available") else "Out of Stock"
        )
        updateProduct(updated)
    }

    fun updateProductStock(productId: String, newStock: Int) {
        if (newStock < 0) {
            _uiState.update { it.copy(toastMessage = "Stock cannot be negative") }
            return
        }
        val prod = _uiState.value.products.find { it.id == productId } ?: return
        val isInStock = newStock > 0
        val updated = prod.copy(
            stock = newStock,
            inStock = isInStock,
            stockStatus = if (isInStock) "In Stock ($newStock units)" else "Out of Stock",
            specifications = prod.specifications + ("Stock" to "$newStock Units")
        )
        updateProduct(updated)
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            dataService.deleteProduct(productId)
            _uiState.update { state ->
                state.copy(
                    products = state.products.filterNot { it.id == productId },
                    toastMessage = "Product removed"
                )
            }
        }
    }

    // Order actions
    fun updateOrderStatus(orderId: String, newStatus: String, note: String = "") {
        val adminIdentity = _uiState.value.adminEmail ?: "Admin"
        viewModelScope.launch {
            val result = dataService.updateOrderStatusWithHistory(
                id = orderId,
                status = newStatus,
                note = note,
                updatedBy = adminIdentity
            )
            result.fold(
                onSuccess = { updatedOrder ->
                    _uiState.update { state ->
                        val updated = state.orders.map {
                            if (it.orderId == orderId) updatedOrder else it
                        }
                        state.copy(orders = updated, toastMessage = "Order $orderId status updated to $newStatus")
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(toastMessage = "Failed: ${err.message}") }
                }
            )
        }
    }

    // Repair actions
    fun updateRepairStatus(bookingId: String, newStatus: String, newCost: String? = null) {
        viewModelScope.launch {
            dataService.updateRepairStatus(bookingId, newStatus, newCost)
            _uiState.update { state ->
                val updated = state.repairs.map {
                    if (it.bookingId == bookingId) {
                        it.copy(
                            status = newStatus,
                            estimatedCost = newCost ?: it.estimatedCost
                        )
                    } else it
                }
                state.copy(repairs = updated, toastMessage = "Repair $bookingId updated to $newStatus")
            }
        }
    }

    // Sell requests actions
    fun updateSellRequestStatus(requestId: String, newStatus: String, offerMin: Int? = null, offerMax: Int? = null) {
        viewModelScope.launch {
            dataService.updateOldPhoneRequest(requestId, newStatus, offerMin, offerMax)
            _uiState.update { state ->
                val updated = state.sellRequests.map {
                    if (it.requestId == requestId) {
                        it.copy(
                            status = newStatus,
                            estimatedQuoteMin = offerMin ?: it.estimatedQuoteMin,
                            estimatedQuoteMax = offerMax ?: it.estimatedQuoteMax
                        )
                    } else it
                }
                state.copy(sellRequests = updated, toastMessage = "Sell request $requestId updated!")
            }
        }
    }

    // Category actions
    fun toggleCategoryActive(categoryId: String) {
        viewModelScope.launch {
            dataService.toggleCategoryActive(categoryId)
            _uiState.update { state ->
                val updated = state.categories.map {
                    if (it.id == categoryId) it.copy(isActive = !it.isActive) else it
                }
                val target = state.categories.find { it.id == categoryId }
                val statusStr = if (target?.isActive == true) "Hidden" else "Active"
                state.copy(categories = updated, toastMessage = "Category '${target?.name}' is now $statusStr")
            }
        }
    }

    fun saveCategory(category: CategoryRecord) {
        viewModelScope.launch {
            val result = dataService.saveCategory(category)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val existingIdx = state.categories.indexOfFirst { it.id == category.id }
                    val updatedList = if (existingIdx != -1) {
                        state.categories.map { if (it.id == category.id) category else it }
                    } else {
                        state.categories + category
                    }.sortedBy { it.displayOrder }
                    state.copy(
                        categories = updatedList,
                        toastMessage = "Category '${category.name}' saved successfully!"
                    )
                }
            } else {
                _uiState.update { it.copy(toastMessage = "Could not save category: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun deleteCategory(categoryId: String, reassignToSlug: String? = null) {
        viewModelScope.launch {
            val cat = _uiState.value.categories.find { it.id == categoryId }
            val catName = cat?.name ?: "Category"
            val result = dataService.deleteCategory(categoryId, reassignToSlug)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        categories = state.categories.filterNot { it.id == categoryId },
                        toastMessage = if (!reassignToSlug.isNullOrBlank()) {
                            "Deleted '$catName'. Products reassigned to '$reassignToSlug'."
                        } else {
                            "Category '$catName' deleted successfully."
                        }
                    )
                }
            } else {
                _uiState.update { it.copy(toastMessage = "Failed to delete category") }
            }
        }
    }

    fun addCategory(name: String, slug: String) {
        val nextOrder = (_uiState.value.categories.maxOfOrNull { it.displayOrder } ?: 0) + 1
        val newCat = CategoryRecord(
            id = "cat-${slug.ifBlank { System.currentTimeMillis().toString() }}",
            name = name,
            slug = slug,
            category = ProductCategory.ACCESSORIES,
            isActive = true,
            productCount = 0,
            displayOrder = nextOrder
        )
        saveCategory(newCat)
    }

    // Review actions
    fun toggleReviewApproval(reviewId: String) {
        _uiState.update { state ->
            val updated = state.reviews.map {
                if (it.review.id == reviewId) it.copy(isApproved = !it.isApproved) else it
            }
            state.copy(reviews = updated, toastMessage = "Review status updated!")
        }
    }

    // Report filter
    fun setReportFilter(filter: String) {
        _uiState.update { it.copy(selectedReportFilter = filter) }
    }

    // Settings actions
    fun updateSettings(newSettings: ShopSettings) {
        viewModelScope.launch {
            dataService.saveSettings(newSettings)
            _uiState.update { it.copy(settings = newSettings, toastMessage = "Shop settings saved successfully!") }
        }
    }

    // Reel Management actions
    fun createReel(reel: ReelItem) {
        viewModelScope.launch {
            val result = dataService.createReel(reel)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "Reel '${reel.title}' published successfully!") }
            } else {
                _uiState.update { it.copy(toastMessage = "Reel saved: ${result.exceptionOrNull()?.message ?: "Local fallback active"}") }
            }
        }
    }

    fun updateReel(reel: ReelItem) {
        viewModelScope.launch {
            val result = dataService.updateReel(reel)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "Reel '${reel.title}' updated!") }
            } else {
                _uiState.update { it.copy(toastMessage = "Reel updated locally") }
            }
        }
    }

    fun toggleReelPublish(reel: ReelItem) {
        val updated = reel.copy(isPublished = !reel.isPublished)
        updateReel(updated)
    }

    fun deleteReel(reelId: String) {
        viewModelScope.launch {
            val result = dataService.deleteReel(reelId)
            if (result.isSuccess) {
                _uiState.update { it.copy(toastMessage = "Reel deleted successfully!") }
            } else {
                _uiState.update { it.copy(toastMessage = "Could not delete reel: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun setUploadingProgress(isUploading: Boolean, progress: Int = 0) {
        _uiState.update { it.copy(isUploadingReel = isUploading, uploadReelProgress = progress) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
