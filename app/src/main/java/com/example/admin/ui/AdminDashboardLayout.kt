package com.example.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.model.AdminTab
import com.example.admin.viewmodel.AdminUiState
import com.example.admin.viewmodel.AdminViewModel
import com.example.model.Product
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardLayout(
    adminViewModel: AdminViewModel,
    uiState: AdminUiState,
    onReturnToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.dismissToast()
        }
    }

    LaunchedEffect(uiState.isSidebarOpen) {
        if (uiState.isSidebarOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen && uiState.isSidebarOpen) {
            adminViewModel.toggleSidebar(false)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminSidebarSheet(
                currentTab = uiState.currentTab,
                pendingOrders = uiState.pendingOrdersCount,
                repairJobs = uiState.totalRepairsCount,
                onSelectTab = { tab ->
                    adminViewModel.selectTab(tab)
                    coroutineScope.launch { drawerState.close() }
                },
                onReturnToStore = {
                    coroutineScope.launch { drawerState.close() }
                    onReturnToStore()
                },
                onLogout = {
                    adminViewModel.logout()
                    onReturnToStore()
                },
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                // Top Admin Navigation Bar
                Surface(
                    color = BlackSurface,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                        adminViewModel.toggleSidebar(true)
                                    }
                                },
                                modifier = Modifier.testTag("admin_sidebar_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Sidebar Menu",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Text(
                                    text = uiState.currentTab.displayName,
                                    color = TextMain,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SK Mobile Shoppy and Electronics Admin • Udgir",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Right actions: Customer Store & Logout
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = BlackSurfaceSecondary,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BlackBorder),
                                modifier = Modifier
                                    .clickable(onClick = onReturnToStore)
                                    .testTag("admin_exit_to_store_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = "Customer Store",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Customer Store",
                                        color = TextMain,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    adminViewModel.logout()
                                    onReturnToStore()
                                },
                                modifier = Modifier.testTag("admin_logout_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentTab) {
                    AdminTab.DASHBOARD -> {
                        AdminDashboardHome(
                            uiState = uiState,
                            onNavigateTab = { tab -> adminViewModel.selectTab(tab) }
                        )
                    }

                    AdminTab.PRODUCTS -> {
                        AdminProductsScreen(
                            products = uiState.products,
                            onAddProduct = { p -> adminViewModel.addProduct(p) },
                            onUpdateProduct = { p -> adminViewModel.updateProduct(p) },
                            onDeleteProduct = { id -> adminViewModel.deleteProduct(id) },
                            onToggleActive = { id -> adminViewModel.toggleProductActive(id) }
                        )
                    }

                    AdminTab.ORDERS -> {
                        AdminOrdersScreen(
                            orders = uiState.orders,
                            onUpdateOrderStatus = { id, status, note -> adminViewModel.updateOrderStatus(id, status, note) }
                        )
                    }

                    AdminTab.REPAIRS -> {
                        AdminRepairsScreen(
                            repairs = uiState.repairs,
                            onUpdateRepairStatus = { id, status, cost -> adminViewModel.updateRepairStatus(id, status, cost) }
                        )
                    }

                    AdminTab.OLD_PHONE_BUY, AdminTab.SELL_REQUESTS -> {
                        AdminOldPhoneScreen(
                            sellRequests = uiState.sellRequests,
                            onUpdateSellRequestStatus = { id, status, min, max ->
                                adminViewModel.updateSellRequestStatus(id, status, min, max)
                            }
                        )
                    }

                    AdminTab.CUSTOMERS -> {
                        AdminCustomersScreen(
                            customers = uiState.customers
                        )
                    }

                    AdminTab.CATEGORIES -> {
                        AdminCategoriesScreen(
                            categories = uiState.categories,
                            products = uiState.products,
                            onSaveCategory = { cat -> adminViewModel.saveCategory(cat) },
                            onToggleCategoryActive = { id -> adminViewModel.toggleCategoryActive(id) },
                            onDeleteCategory = { id, reassignTo -> adminViewModel.deleteCategory(id, reassignTo) }
                        )
                    }

                    AdminTab.REVIEWS -> {
                        AdminReviewsScreen(
                            reviews = uiState.reviews,
                            onToggleApproval = { id -> adminViewModel.toggleReviewApproval(id) }
                        )
                    }

                    AdminTab.REELS -> {
                        AdminReelsScreen(
                            reels = uiState.reels,
                            products = uiState.products,
                            isUploading = uiState.isUploadingReel,
                            uploadProgress = uiState.uploadReelProgress,
                            adminViewModel = adminViewModel
                        )
                    }

                    AdminTab.PAYMENTS -> {
                        AdminPaymentsScreen(
                            payments = uiState.payments
                        )
                    }

                    AdminTab.REPORTS -> {
                        AdminReportsScreen(
                            uiState = uiState,
                            onFilterChange = { f -> adminViewModel.setReportFilter(f) }
                        )
                    }

                    AdminTab.SETTINGS -> {
                        AdminSettingsScreen(
                            settings = uiState.settings,
                            onSaveSettings = { s -> adminViewModel.updateSettings(s) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSidebarSheet(
    currentTab: AdminTab,
    pendingOrders: Int,
    repairJobs: Int,
    onSelectTab: (AdminTab) -> Unit,
    onReturnToStore: () -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(),
        drawerContainerColor = BlackSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 16.dp)
        ) {
            // Sidebar Header
            Surface(
                color = BlackSurfaceSecondary,
                border = BorderStroke(1.dp, BlackBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = Color(0xFF0B0B0B),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "SK Mobile Shoppy and Electronics Admin",
                                color = TextMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Store Manager • Udgir",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMain)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sidebar Tabs List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                AdminTab.values().forEach { tab ->
                    item {
                        val isSelected = currentTab == tab
                        val icon = getTabIcon(tab)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { onSelectTab(tab) }
                                .testTag("admin_nav_${tab.name}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GoldPrimary else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, GoldHighlight) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = tab.displayName,
                                        tint = if (isSelected) Color(0xFF0B0B0B) else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = tab.displayName,
                                        color = if (isSelected) Color(0xFF0B0B0B) else TextMain,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }

                                if (tab == AdminTab.ORDERS && pendingOrders > 0) {
                                    Surface(
                                        color = if (isSelected) Color(0xFF0B0B0B) else GoldHighlight,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "$pendingOrders",
                                            color = if (isSelected) GoldHighlight else Color(0xFF0B0B0B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (tab == AdminTab.REPAIRS && repairJobs > 0) {
                                    Surface(
                                        color = if (isSelected) Color(0xFF0B0B0B) else GoldPrimary,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "$repairJobs",
                                            color = if (isSelected) GoldPrimary else Color(0xFF0B0B0B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = BlackBorder, modifier = Modifier.padding(horizontal = 14.dp))
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Buttons: Back to Store and Logout
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onReturnToStore),
                    shape = RoundedCornerShape(8.dp),
                    color = BlackSurfaceSecondary,
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Store, contentDescription = "Store", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Customer Store View", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogout),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = ColorError, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Logout Admin", color = ColorError, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun getTabIcon(tab: AdminTab): ImageVector {
    return when (tab) {
        AdminTab.DASHBOARD -> Icons.Default.Dashboard
        AdminTab.PRODUCTS -> Icons.Default.Inventory2
        AdminTab.ORDERS -> Icons.Default.ShoppingBag
        AdminTab.REPAIRS -> Icons.Default.Build
        AdminTab.OLD_PHONE_BUY -> Icons.Default.PhoneAndroid
        AdminTab.SELL_REQUESTS -> Icons.Default.SwapHoriz
        AdminTab.CUSTOMERS -> Icons.Default.People
        AdminTab.CATEGORIES -> Icons.Default.Category
        AdminTab.REVIEWS -> Icons.Default.RateReview
        AdminTab.REELS -> Icons.Default.PlayCircle
        AdminTab.PAYMENTS -> Icons.Default.AttachMoney
        AdminTab.REPORTS -> Icons.Default.TrendingUp
        AdminTab.SETTINGS -> Icons.Default.Settings
    }
}
