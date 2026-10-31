package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen

@Composable
fun MobileMenuDrawerSheet(
    currentScreen: AppScreen,
    cartCount: Int,
    userName: String,
    unreadNotificationCount: Int = 0,
    onNavigate: (AppScreen) -> Unit,
    onProfileClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight(),
        drawerContainerColor = BlackSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 20.dp)
        ) {
            // Header with SK Mobile Shoppy and Electronics branding & Close Button
            Surface(
                color = BlackSurfaceSecondary,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text(
                                    text = "SK",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = BlackBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "SK Mobile Shoppy and Electronics",
                                        color = TextMain,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = GoldHighlight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = "Nagar Parishad, Udgir",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = TextMain
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // User Profile quick pill
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onProfileClick()
                                onClose()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Account",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = userName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Manage Account & Orders",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Menu Items
            val menuItems = listOf(
                NavigationItem("Home", Icons.Default.Home, AppScreen.HOME),
                NavigationItem("My Orders & Tracking", Icons.Default.LocalShipping, AppScreen.MY_ORDERS),
                NavigationItem("SK Reels & Videos", Icons.Default.PlayCircle, AppScreen.REELS),
                NavigationItem("All Products & Shop", Icons.Default.ShoppingBag, AppScreen.PRODUCTS),
                NavigationItem("Mobile Repair Booking", Icons.Default.Build, AppScreen.REPAIR_BOOKING),
                NavigationItem("Buy Certified Old Phones", Icons.Default.PhoneAndroid, AppScreen.OLD_PHONE_BUY),
                NavigationItem("Sell Your Old Phone", Icons.Default.SwapHoriz, AppScreen.SELL_OLD_PHONE),
                NavigationItem("Notification Center", Icons.Default.Notifications, AppScreen.NOTIFICATIONS, hasBadge = true),
                NavigationItem("Shopping Cart", Icons.Default.ShoppingCart, AppScreen.CART, hasBadge = true),
                NavigationItem("Contact & Store Info", Icons.Default.LocationOn, AppScreen.CONTACT),
                NavigationItem("Store Admin Portal", Icons.Default.AdminPanelSettings, AppScreen.ADMIN_LOGIN)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                menuItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                onNavigate(item.screen)
                                onClose()
                            }
                            .testTag("drawer_item_${item.screen.name}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) BlackSurfaceSecondary else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) GoldHighlight else ShopTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) GoldHighlight else ShopTextPrimary
                                )
                            }

                            val badgeCount = if (item.screen == AppScreen.CART) cartCount else if (item.screen == AppScreen.NOTIFICATIONS) unreadNotificationCount else 0
                            if (item.hasBadge && badgeCount > 0) {
                                Surface(
                                    color = GoldHighlight,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        color = Color(0xFF0B0B0B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = ShopOutlineLight, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Assistance in Drawer
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = "Need Urgent Assistance?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextPrimary
                )
                Text(
                    text = "Store: Nagar Parishad, Udgir\nHelpline: +91 9823109313\nEmail: skmobileshoppyandelectronics@gmail.com",
                    fontSize = 11.sp,
                    color = ShopTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

private data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val screen: AppScreen,
    val hasBadge: Boolean = false
)
