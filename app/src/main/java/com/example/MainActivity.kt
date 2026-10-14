package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin.ui.AdminDashboardLayout
import com.example.admin.ui.AdminLoginScreen
import com.example.admin.viewmodel.AdminViewModel
import com.example.data.service.DataServiceLocator
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppHeader
import com.example.ui.components.MobileMenuDrawerSheet
import com.example.ui.components.ProfileSheet
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OldPhoneScreen
import com.example.ui.screens.OrderSuccessScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.notification.PushNotificationHelper
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.RepairBookingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataServiceLocator.initialize(this)
        PushNotificationHelper.createNotificationChannels(this)
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
    val uiState by shopViewModel.uiState.collectAsStateWithLifecycle()
    val adminUiState by adminViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Sync drawer open state with viewModel
    LaunchedEffect(uiState.isMobileMenuOpen) {
        if (uiState.isMobileMenuOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen && uiState.isMobileMenuOpen) {
            shopViewModel.toggleMobileMenu(false)
        }
    }

    // Display Toast / Notification when state message arrives
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            shopViewModel.dismissToast()
        }
    }

    // Hardware / Gesture Back Handling
    BackHandler(enabled = drawerState.isOpen || uiState.currentScreen != AppScreen.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
            shopViewModel.toggleMobileMenu(false)
        } else {
            shopViewModel.navigateBack()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MobileMenuDrawerSheet(
                currentScreen = uiState.currentScreen,
                cartCount = uiState.cartItemCount,
                userName = uiState.userName,
                unreadNotificationCount = uiState.unreadNotificationCount,
                onNavigate = { screen ->
                    shopViewModel.navigateTo(screen)
                    coroutineScope.launch { drawerState.close() }
                },
                onProfileClick = {
                    shopViewModel.setLoginSheetVisible(true)
                },
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                    shopViewModel.toggleMobileMenu(false)
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (uiState.currentScreen != AppScreen.ADMIN_LOGIN && uiState.currentScreen != AppScreen.ADMIN_DASHBOARD) {
                    // Sticky Header on mobile and desktop
                    AppHeader(
                        cartCount = uiState.cartItemCount,
                        searchQuery = uiState.searchQuery,
                        notificationCount = uiState.unreadNotificationCount,
                        onNotificationClick = {
                            shopViewModel.navigateTo(AppScreen.NOTIFICATIONS)
                        },
                        onSearchChange = { query ->
                            shopViewModel.updateSearchQuery(query)
                            if (query.isNotEmpty() && uiState.currentScreen != AppScreen.PRODUCTS) {
                                shopViewModel.navigateTo(AppScreen.PRODUCTS)
                            }
                        },
                        onSearchSubmit = {
                            shopViewModel.navigateTo(AppScreen.PRODUCTS)
                        },
                        onCartClick = {
                            shopViewModel.navigateTo(AppScreen.CART)
                        },
                        onProfileClick = {
                            shopViewModel.setLoginSheetVisible(true)
                        },
                        onLogoClick = {
                            shopViewModel.navigateTo(AppScreen.HOME)
                        },
                        onMenuClick = {
                            coroutineScope.launch {
                                drawerState.open()
                                shopViewModel.toggleMobileMenu(true)
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (uiState.currentScreen != AppScreen.ADMIN_LOGIN && uiState.currentScreen != AppScreen.ADMIN_DASHBOARD) {
                    AppBottomNavBar(
                        currentScreen = uiState.currentScreen,
                        cartCount = uiState.cartItemCount,
                        onNavigate = { screen ->
                            shopViewModel.navigateTo(screen)
                        }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            featuredProducts = uiState.products,
                            repairServices = uiState.repairServices,
                            customerReviews = uiState.reviews,
                            onSelectProduct = { product ->
                                shopViewModel.selectProduct(product)
                            },
                            onAddToCart = { product ->
                                shopViewModel.addToCart(product)
                            },
                            onBuyNow = { product ->
                                shopViewModel.buyNow(product)
                            },
                            onCategoryClick = { category ->
                                shopViewModel.filterByCategory(category)
                            },
                            onNavigate = { screen ->
                                shopViewModel.navigateTo(screen)
                            },
                            onBookRepairProblem = { problem ->
                                shopViewModel.bookRepairWithProblem(problem)
                            }
                        )
                    }

                    AppScreen.PRODUCTS -> {
                        ProductsScreen(
                            products = uiState.products,
                            selectedCategory = uiState.selectedCategory,
                            searchQuery = uiState.searchQuery,
                            sortOption = uiState.sortOption,
                            onCategorySelect = { category ->
                                shopViewModel.filterByCategory(category)
                            },
                            onSortSelect = { sort ->
                                shopViewModel.updateSortOption(sort)
                            },
                            onClearSearch = {
                                shopViewModel.updateSearchQuery("")
                            },
                            onSelectProduct = { product ->
                                shopViewModel.selectProduct(product)
                            },
                            onAddToCart = { product ->
                                shopViewModel.addToCart(product)
                            },
                            onBuyNow = { product ->
                                shopViewModel.buyNow(product)
                            },
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }

                    AppScreen.PRODUCT_DETAIL -> {
                        uiState.selectedProduct?.let { product ->
                            ProductDetailScreen(
                                product = product,
                                relatedProducts = uiState.products.filter { it.category == product.category },
                                onAddToCart = { item ->
                                    shopViewModel.addToCart(item)
                                },
                                onBuyNow = { item ->
                                    shopViewModel.buyNow(item)
                                },
                                onSelectRelatedProduct = { item ->
                                    shopViewModel.selectProduct(item)
                                },
                                onBack = {
                                    shopViewModel.navigateBack()
                                }
                            )
                        } ?: run {
                            shopViewModel.navigateTo(AppScreen.PRODUCTS)
                        }
                    }

                    AppScreen.CART -> {
                        CartScreen(
                            cartItems = uiState.cartItems,
                            subtotal = uiState.cartSubtotal,
                            total = uiState.cartTotal,
                            totalSavings = uiState.cartTotalSavings,
                            deliveryFee = uiState.deliveryFee,
                            onIncreaseQuantity = { id ->
                                shopViewModel.updateCartQuantity(id, 1)
                            },
                            onDecreaseQuantity = { id ->
                                shopViewModel.updateCartQuantity(id, -1)
                            },
                            onRemoveItem = { id ->
                                shopViewModel.removeFromCart(id)
                            },
                            onContinueShopping = {
                                shopViewModel.navigateTo(AppScreen.PRODUCTS)
                            },
                            onProceedToCheckout = {
                                if (!uiState.isCustomerAuthenticated) {
                                    shopViewModel.setCustomerAuthError("Please sign in with your Google account to proceed to checkout.")
                                    shopViewModel.setLoginSheetVisible(true)
                                } else {
                                    shopViewModel.navigateTo(AppScreen.CHECKOUT)
                                }
                            },
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }

                    AppScreen.CHECKOUT -> {
                        CheckoutScreen(
                            cartItems = uiState.cartItems,
                            subtotal = uiState.cartSubtotal,
                            total = uiState.cartTotal,
                            totalSavings = uiState.cartTotalSavings,
                            deliveryFee = uiState.deliveryFee,
                            defaultName = uiState.userName,
                            defaultPhone = uiState.userPhone,
                            defaultAddress = uiState.userAddress,
                            isCustomerAuthenticated = uiState.isCustomerAuthenticated,
                            onSignInRequired = {
                                shopViewModel.setCustomerAuthError("Please sign in with your Google account to complete your order.")
                                shopViewModel.setLoginSheetVisible(true)
                            },
                            onPlaceOrder = { name, phone, address, city, state, pin, delivOpt, payMethod ->
                                shopViewModel.placeOrder(name, phone, address, city, state, pin, delivOpt, payMethod)
                            },
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }

                    AppScreen.ORDER_SUCCESS -> {
                        OrderSuccessScreen(
                            order = uiState.lastConfirmedOrder,
                            onContinueShopping = {
                                shopViewModel.navigateTo(AppScreen.HOME)
                            }
                        )
                    }

                    AppScreen.REPAIR_BOOKING -> {
                        RepairBookingScreen(
                            initialProblem = uiState.preselectedRepairProblem,
                            initialBrand = uiState.preselectedRepairBrand,
                            defaultName = uiState.userName,
                            defaultPhone = uiState.userPhone,
                            defaultAddress = uiState.userAddress,
                            activeBookings = uiState.activeRepairBookings,
                            lastConfirmedBooking = uiState.lastConfirmedBooking,
                            isCustomerAuthenticated = uiState.isCustomerAuthenticated,
                            onSubmitBooking = { name, phone, brand, model, problem, pickup, address, date, details ->
                                if (!uiState.isCustomerAuthenticated) {
                                    shopViewModel.setCustomerAuthError("Please sign in with your Google account to book a phone repair.")
                                    shopViewModel.setLoginSheetVisible(true)
                                } else {
                                    shopViewModel.submitRepairBooking(
                                        name, phone, brand, model, problem, pickup, address, date, details
                                    )
                                }
                            },
                            onDismissConfirmDialog = {
                                shopViewModel.clearConfirmedBooking()
                            },
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }

                    AppScreen.OLD_PHONE_BUY -> {
                        OldPhoneScreen(
                            initialTab = 0,
                            preOwnedProducts = uiState.products,
                            defaultName = uiState.userName,
                            defaultPhone = uiState.userPhone,
                            defaultAddress = uiState.userAddress,
                            activeSellRequests = uiState.activeSellRequests,
                            lastConfirmedSellRequest = uiState.lastConfirmedSellRequest,
                            onSelectProduct = { product ->
                                shopViewModel.selectProduct(product)
                            },
                            onAddToCart = { product ->
                                shopViewModel.addToCart(product)
                            },
                            onBuyNow = { product ->
                                shopViewModel.buyNow(product)
                            },
                            onSubmitSellRequest = { name, phone, brand, model, storage, cond, price, address ->
                                shopViewModel.submitSellRequest(
                                    name, phone, brand, model, storage, cond, price, address
                                )
                            },
                            onDismissSellDialog = {
                                shopViewModel.clearConfirmedSellRequest()
                            },
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }

                    AppScreen.SELL_OLD_PHONE -> {
                        OldPhoneScreen(
                            initialTab = 1,
                            preOwnedProducts = uiState.products,
                            defaultName = uiState.userName,
                            defaultPhone = uiState.userPhone,
                            defaultAddress = uiState.userAddress,
                            activeSellRequests = uiState.activeSellRequests,
                            lastConfirmedSellRequest = uiState.lastConfirmedSellRequest,
                            onSelectProduct = { product ->
                                shopViewModel.selectProduct(product)
                            },
                            onAddToCart = { product ->
                                shopViewModel.addToCart(product)
                            },
                            onBuyNow = { product ->
                                shopViewModel.buyNow(product)
                            },
                            onSubmitSellRequest = { name, phone, brand, model, storage, cond, price, address ->
                                shopViewModel.submitSellRequest(
                                    name, phone, brand, model, storage, cond, price, address
                                )
                            },
                            onDismissSellDialog = {
                                shopViewModel.clearConfirmedSellRequest()
                            },
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }

                    AppScreen.CONTACT -> {
                        ContactScreen(
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }

                    AppScreen.ADMIN_LOGIN -> {
                        AdminLoginScreen(
                            onLoginSuccess = {
                                shopViewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                            },
                            onBackToStore = {
                                shopViewModel.navigateTo(AppScreen.HOME)
                            },
                            onGoogleSignIn = { idToken, onComplete ->
                                adminViewModel.signInWithGoogleToken(idToken, onComplete)
                            }
                        )
                    }

                    AppScreen.ADMIN_DASHBOARD -> {
                        if (!adminUiState.isAuthenticated) {
                            // Protected route: Redirect unauthenticated users to login screen
                            AdminLoginScreen(
                                onLoginSuccess = {
                                    shopViewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                                },
                                onBackToStore = {
                                    shopViewModel.navigateTo(AppScreen.HOME)
                                },
                                onGoogleSignIn = { idToken, onComplete ->
                                    adminViewModel.signInWithGoogleToken(idToken, onComplete)
                                }
                            )
                        } else {
                            AdminDashboardLayout(
                                adminViewModel = adminViewModel,
                                uiState = adminUiState,
                                onReturnToStore = {
                                    shopViewModel.navigateTo(AppScreen.HOME)
                                }
                            )
                        }
                    }

                    AppScreen.NOTIFICATIONS -> {
                        NotificationCenterScreen(
                            notifications = uiState.notifications,
                            unreadCount = uiState.unreadNotificationCount,
                            onMarkAsRead = { notifId ->
                                shopViewModel.markNotificationAsRead(notifId)
                            },
                            onMarkAllAsRead = {
                                shopViewModel.markAllNotificationsAsRead()
                            },
                            onClearAll = {
                                shopViewModel.clearNotifications()
                            },
                            onNotificationClick = { notif ->
                                shopViewModel.markNotificationAsRead(notif.id)
                            },
                            onBack = {
                                shopViewModel.navigateBack()
                            }
                        )
                    }
                }
            }

            // Profile / Login Sheet
            if (uiState.isLoginSheetVisible) {
                ProfileSheet(
                    uiState = uiState,
                    onGoogleSignIn = { idToken, onComplete ->
                        shopViewModel.signInCustomerWithGoogle(idToken, onComplete)
                    },
                    onLogout = {
                        shopViewModel.logoutCustomer()
                    },
                    onClearAuthError = {
                        shopViewModel.setCustomerAuthError(null)
                    },
                    onUpdateProfile = { name, phone, address ->
                        shopViewModel.updateUserProfile(name, phone, address)
                    },
                    onOpenAdmin = {
                        shopViewModel.navigateTo(AppScreen.ADMIN_LOGIN)
                    },
                    onDismiss = {
                        shopViewModel.setLoginSheetVisible(false)
                    }
                )
            }
        }
    }
}
