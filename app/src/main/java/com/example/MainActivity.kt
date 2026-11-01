package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin.ui.AdminDashboardLayout
import com.example.admin.ui.AdminLoginScreen
import com.example.admin.viewmodel.AdminViewModel
import com.example.model.ProductCategory
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppHeader
import com.example.ui.components.MobileMenuDrawerSheet
import com.example.ui.components.ProfileSheet
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyOrdersScreen
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.OldPhoneScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.OrderSuccessScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.RepairBookingScreen
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SKMobileApp()
            }
        }
    }
}

@Composable
fun SKMobileApp(
    shopViewModel: ShopViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    val shopUiState by shopViewModel.uiState.collectAsState()
    val adminUiState by adminViewModel.uiState.collectAsState()
    val currentScreen = shopUiState.currentScreen

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shopUiState.toastMessage) {
        shopUiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            shopViewModel.dismissToast()
        }
    }

    // Back handling: pop customer screen stack or return to store
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        if (currentScreen == AppScreen.ADMIN_DASHBOARD || currentScreen == AppScreen.ADMIN_LOGIN) {
            shopViewModel.navigateTo(AppScreen.HOME)
        } else {
            shopViewModel.navigateBack()
        }
    }

    // Automatically navigate to Admin Dashboard when authenticated as an authorized administrator
    LaunchedEffect(currentScreen, adminUiState.isAuthenticated) {
        if (currentScreen == AppScreen.ADMIN_LOGIN && adminUiState.isAuthenticated) {
            shopViewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
        }
    }

    if (currentScreen == AppScreen.ADMIN_DASHBOARD) {
        AdminDashboardLayout(
            adminViewModel = adminViewModel,
            uiState = adminUiState,
            onReturnToStore = { shopViewModel.navigateTo(AppScreen.HOME) }
        )
        return
    }

    if (currentScreen == AppScreen.ADMIN_LOGIN) {
        AdminLoginScreen(
            onLoginSuccess = { shopViewModel.navigateTo(AppScreen.ADMIN_DASHBOARD) },
            onBackToStore = { shopViewModel.navigateTo(AppScreen.HOME) },
            onGoogleSignIn = { idToken, onComplete ->
                adminViewModel.signInWithGoogleToken(idToken, onComplete)
            },
            onGoogleSignInProvider = { activity, onComplete ->
                adminViewModel.signInWithGoogleProvider(activity, onComplete)
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MobileMenuDrawerSheet(
                currentScreen = currentScreen,
                cartCount = shopUiState.cartItemCount,
                userName = shopUiState.userName,
                unreadNotificationCount = shopUiState.unreadNotificationCount,
                onNavigate = { screen ->
                    coroutineScope.launch { drawerState.close() }
                    shopViewModel.navigateTo(screen)
                },
                onProfileClick = {
                    coroutineScope.launch { drawerState.close() }
                    shopViewModel.setLoginSheetVisible(true)
                },
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (currentScreen != AppScreen.REELS) {
                    AppHeader(
                        cartCount = shopUiState.cartItemCount,
                        searchQuery = shopUiState.searchQuery,
                        onSearchChange = { shopViewModel.updateSearchQuery(it) },
                        onSearchSubmit = { shopViewModel.navigateTo(AppScreen.PRODUCTS) },
                        onCartClick = { shopViewModel.navigateTo(AppScreen.CART) },
                        onProfileClick = { shopViewModel.setLoginSheetVisible(true) },
                        onLogoClick = { shopViewModel.navigateTo(AppScreen.HOME) },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        notificationCount = shopUiState.unreadNotificationCount,
                        onNotificationClick = { shopViewModel.navigateTo(AppScreen.NOTIFICATIONS) }
                    )
                }
            },
            bottomBar = {
                AppBottomNavBar(
                    currentScreen = currentScreen,
                    cartCount = shopUiState.cartItemCount,
                    onNavigate = { screen -> shopViewModel.navigateTo(screen) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            featuredProducts = shopUiState.products,
                            repairServices = shopUiState.repairServices,
                            customerReviews = shopUiState.reviews,
                            categories = shopUiState.activeCategories,
                            onSelectProduct = { prod ->
                                shopViewModel.selectProduct(prod)
                                shopViewModel.navigateTo(AppScreen.PRODUCT_DETAIL)
                            },
                            onAddToCart = { prod -> shopViewModel.addToCart(prod) },
                            onBuyNow = { prod ->
                                shopViewModel.buyNow(prod)
                                shopViewModel.navigateTo(AppScreen.CHECKOUT)
                            },
                            onCategoryClick = { cat ->
                                shopViewModel.filterByCategory(cat)
                                shopViewModel.navigateTo(AppScreen.PRODUCTS)
                            },
                            onCategoryRecordClick = { cat ->
                                shopViewModel.filterByCategoryRecord(cat)
                            },
                            onNavigate = { screen -> shopViewModel.navigateTo(screen) },
                            onBookRepairProblem = { prob ->
                                shopViewModel.bookRepairWithProblem(prob)
                                shopViewModel.navigateTo(AppScreen.REPAIR_BOOKING)
                            }
                        )
                    }

                    AppScreen.PRODUCTS -> {
                        ProductsScreen(
                            products = shopUiState.filteredProducts,
                            selectedCategory = shopUiState.selectedCategory,
                            selectedCategorySlug = shopUiState.selectedCategorySlug,
                            activeCategories = shopUiState.activeCategories,
                            searchQuery = shopUiState.searchQuery,
                            sortOption = shopUiState.sortOption,
                            onCategorySelect = { shopViewModel.filterByCategory(it) },
                            onCategoryRecordSelect = { shopViewModel.filterByCategoryRecord(it) },
                            onSortSelect = { shopViewModel.updateSortOption(it) },
                            onClearSearch = { shopViewModel.updateSearchQuery("") },
                            onSelectProduct = { prod ->
                                shopViewModel.selectProduct(prod)
                                shopViewModel.navigateTo(AppScreen.PRODUCT_DETAIL)
                            },
                            onAddToCart = { prod -> shopViewModel.addToCart(prod) },
                            onBuyNow = { prod ->
                                shopViewModel.buyNow(prod)
                                shopViewModel.navigateTo(AppScreen.CHECKOUT)
                            },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.REELS -> {
                        ReelsScreen(
                            reels = shopUiState.reels,
                            isMuted = shopUiState.isReelMuted,
                            onToggleMute = { shopViewModel.toggleReelMute() },
                            onLikeClick = { reelId -> shopViewModel.toggleReelLike(reelId) },
                            onShareClick = { reelId -> shopViewModel.shareReel(reelId) },
                            onShopNowClick = { productId -> shopViewModel.openReelProduct(productId) },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.PRODUCT_DETAIL -> {
                        shopUiState.selectedProduct?.let { product ->
                            ProductDetailScreen(
                                product = product,
                                relatedProducts = shopUiState.products.filter { it.category == product.category && it.id != product.id },
                                onAddToCart = { prod -> shopViewModel.addToCart(prod) },
                                onBuyNow = { prod ->
                                    shopViewModel.buyNow(prod)
                                    shopViewModel.navigateTo(AppScreen.CHECKOUT)
                                },
                                onSelectRelatedProduct = { prod ->
                                    shopViewModel.selectProduct(prod)
                                },
                                onBack = { shopViewModel.navigateBack() }
                            )
                        } ?: run {
                            shopViewModel.navigateTo(AppScreen.PRODUCTS)
                        }
                    }

                    AppScreen.CART -> {
                        CartScreen(
                            cartItems = shopUiState.cartItems,
                            subtotal = shopUiState.cartSubtotal,
                            total = shopUiState.cartTotal,
                            totalSavings = shopUiState.cartTotalSavings,
                            deliveryFee = shopUiState.deliveryFee,
                            onIncreaseQuantity = { id -> shopViewModel.updateCartQuantity(id, 1) },
                            onDecreaseQuantity = { id -> shopViewModel.updateCartQuantity(id, -1) },
                            onRemoveItem = { id -> shopViewModel.removeFromCart(id) },
                            onContinueShopping = { shopViewModel.navigateTo(AppScreen.PRODUCTS) },
                            onProceedToCheckout = { shopViewModel.navigateTo(AppScreen.CHECKOUT) },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.CHECKOUT -> {
                        CheckoutScreen(
                            cartItems = shopUiState.cartItems,
                            subtotal = shopUiState.cartSubtotal,
                            total = shopUiState.cartTotal,
                            totalSavings = shopUiState.cartTotalSavings,
                            deliveryFee = shopUiState.deliveryFee,
                            defaultName = shopUiState.userName,
                            defaultPhone = shopUiState.userPhone,
                            defaultAddress = shopUiState.userAddress,
                            isCustomerAuthenticated = shopUiState.isCustomerAuthenticated,
                            onSignInRequired = { shopViewModel.setLoginSheetVisible(true) },
                            onPlaceOrder = { name, phone, address, city, state, pin, delivery, payment ->
                                shopViewModel.placeOrder(name, phone, address, city, state, pin, delivery, payment)
                            },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.ORDER_SUCCESS -> {
                        OrderSuccessScreen(
                            order = shopUiState.lastConfirmedOrder,
                            onContinueShopping = {
                                shopViewModel.clearConfirmedOrder()
                                shopViewModel.navigateTo(AppScreen.HOME)
                            },
                            onTrackOrder = { orderId ->
                                shopViewModel.navigateToOrderTracking(orderId)
                            }
                        )
                    }

                    AppScreen.MY_ORDERS -> {
                        MyOrdersScreen(
                            orders = if (shopUiState.customerOrders.isNotEmpty()) shopUiState.customerOrders else shopUiState.pastOrders,
                            isLoading = shopUiState.isOrdersLoading,
                            errorMessage = shopUiState.ordersErrorMessage,
                            onSelectOrder = { order -> shopViewModel.selectOrderForDetail(order) },
                            onRefresh = { shopViewModel.refreshCustomerOrders() },
                            onBrowseShop = { shopViewModel.navigateTo(AppScreen.PRODUCTS) },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.ORDER_DETAIL -> {
                        OrderDetailScreen(
                            order = shopUiState.selectedOrder ?: shopUiState.lastConfirmedOrder,
                            onCancelOrder = { orderId, reason ->
                                shopViewModel.cancelCustomerOrder(orderId, reason)
                            },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.REPAIR_BOOKING -> {
                        RepairBookingScreen(
                            initialProblem = shopUiState.preselectedRepairProblem,
                            initialBrand = shopUiState.preselectedRepairBrand,
                            defaultName = shopUiState.userName,
                            defaultPhone = shopUiState.userPhone,
                            defaultAddress = shopUiState.userAddress,
                            activeBookings = shopUiState.activeRepairBookings,
                            lastConfirmedBooking = shopUiState.lastConfirmedBooking,
                            isCustomerAuthenticated = shopUiState.isCustomerAuthenticated,
                            isSubmitting = shopUiState.isSubmittingRepair,
                            onSubmitBooking = { name, phone, brand, model, prob, pickup, addr, date, details ->
                                shopViewModel.submitRepairBooking(name, phone, brand, model, prob, pickup, addr, date, details)
                            },
                            onDismissConfirmDialog = { shopViewModel.clearConfirmedBooking() },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.OLD_PHONE_BUY, AppScreen.SELL_OLD_PHONE -> {
                        val initialTab = if (currentScreen == AppScreen.SELL_OLD_PHONE) 1 else 0
                        OldPhoneScreen(
                            initialTab = initialTab,
                            preOwnedProducts = shopUiState.products.filter { it.category == ProductCategory.OLD_PHONES },
                            defaultName = shopUiState.userName,
                            defaultPhone = shopUiState.userPhone,
                            defaultAddress = shopUiState.userAddress,
                            activeSellRequests = shopUiState.activeSellRequests,
                            lastConfirmedSellRequest = shopUiState.lastConfirmedSellRequest,
                            onSelectProduct = { prod ->
                                shopViewModel.selectProduct(prod)
                                shopViewModel.navigateTo(AppScreen.PRODUCT_DETAIL)
                            },
                            onAddToCart = { prod -> shopViewModel.addToCart(prod) },
                            onBuyNow = { prod ->
                                shopViewModel.buyNow(prod)
                                shopViewModel.navigateTo(AppScreen.CHECKOUT)
                            },
                            onSubmitSellRequest = { name, phone, brand, model, storage, cond, price, addr ->
                                shopViewModel.submitSellRequest(name, phone, brand, model, storage, cond, price, addr)
                            },
                            onDismissSellDialog = { shopViewModel.clearConfirmedSellRequest() },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.NOTIFICATIONS -> {
                        NotificationCenterScreen(
                            notifications = shopUiState.notifications,
                            unreadCount = shopUiState.unreadNotificationCount,
                            onMarkAsRead = { shopViewModel.markNotificationAsRead(it) },
                            onMarkAllAsRead = { shopViewModel.markAllNotificationsAsRead() },
                            onClearAll = { shopViewModel.clearNotifications() },
                            onNotificationClick = { notif ->
                                when (notif.type) {
                                    com.example.model.NotificationType.ORDER -> shopViewModel.navigateTo(AppScreen.HOME)
                                    com.example.model.NotificationType.REPAIR -> shopViewModel.navigateTo(AppScreen.REPAIR_BOOKING)
                                    com.example.model.NotificationType.SYSTEM -> shopViewModel.navigateTo(AppScreen.HOME)
                                }
                            },
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    AppScreen.CONTACT -> {
                        ContactScreen(
                            onBack = { shopViewModel.navigateBack() }
                        )
                    }

                    else -> {
                        HomeScreen(
                            featuredProducts = shopUiState.products,
                            repairServices = shopUiState.repairServices,
                            customerReviews = shopUiState.reviews,
                            onSelectProduct = { prod ->
                                shopViewModel.selectProduct(prod)
                                shopViewModel.navigateTo(AppScreen.PRODUCT_DETAIL)
                            },
                            onAddToCart = { prod -> shopViewModel.addToCart(prod) },
                            onBuyNow = { prod ->
                                shopViewModel.buyNow(prod)
                                shopViewModel.navigateTo(AppScreen.CHECKOUT)
                            },
                            onCategoryClick = { cat ->
                                shopViewModel.filterByCategory(cat)
                                shopViewModel.navigateTo(AppScreen.PRODUCTS)
                            },
                            onNavigate = { screen -> shopViewModel.navigateTo(screen) },
                            onBookRepairProblem = { prob ->
                                shopViewModel.bookRepairWithProblem(prob)
                                shopViewModel.navigateTo(AppScreen.REPAIR_BOOKING)
                            }
                        )
                    }
                }
            }
        }
    }

    if (shopUiState.isLoginSheetVisible) {
        ProfileSheet(
            uiState = shopUiState,
            onGoogleSignIn = { idToken, onComplete ->
                shopViewModel.signInCustomerWithGoogle(idToken, onComplete)
            },
            onLogout = { shopViewModel.logoutCustomer() },
            onClearAuthError = { shopViewModel.setCustomerAuthError(null) },
            onUpdateProfile = { name, phone, addr ->
                shopViewModel.updateUserProfile(name, phone, addr)
            },
            onViewOrders = {
                shopViewModel.setLoginSheetVisible(false)
                shopViewModel.navigateTo(AppScreen.MY_ORDERS)
            },
            onTrackOrder = { order ->
                shopViewModel.setLoginSheetVisible(false)
                shopViewModel.selectOrderForDetail(order)
            },
            onOpenAdmin = {
                shopViewModel.setLoginSheetVisible(false)
                shopViewModel.navigateTo(AppScreen.ADMIN_LOGIN)
            },
            onDismiss = { shopViewModel.setLoginSheetVisible(false) }
        )
    }
}
