package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShopAccentAmber
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.viewmodel.AppScreen

import com.example.ui.theme.BlackSurface
import com.example.ui.theme.TextSecondary

@Composable
fun AppBottomNavBar(
    currentScreen: AppScreen,
    cartCount: Int,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = BlackSurface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        // Home
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors()
        )

        // Products / Shop
        NavigationBarItem(
            selected = currentScreen == AppScreen.PRODUCTS || currentScreen == AppScreen.PRODUCT_DETAIL,
            onClick = { onNavigate(AppScreen.PRODUCTS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = "Shop",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Shop",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == AppScreen.PRODUCTS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors()
        )

        // Repair
        NavigationBarItem(
            selected = currentScreen == AppScreen.REPAIR_BOOKING,
            onClick = { onNavigate(AppScreen.REPAIR_BOOKING) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Repair",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Repair",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == AppScreen.REPAIR_BOOKING) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors()
        )

        // Buy & Sell Old Mobiles
        NavigationBarItem(
            selected = currentScreen == AppScreen.OLD_PHONE_BUY || currentScreen == AppScreen.SELL_OLD_PHONE,
            onClick = { onNavigate(AppScreen.OLD_PHONE_BUY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Old Phones",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Old Phones",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == AppScreen.OLD_PHONE_BUY) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors()
        )

        // Cart
        NavigationBarItem(
            selected = currentScreen == AppScreen.CART || currentScreen == AppScreen.CHECKOUT,
            onClick = { onNavigate(AppScreen.CART) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = ShopAccentAmber,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = "$cartCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Cart",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == AppScreen.CART) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = ShopPrimaryBlue,
    selectedTextColor = ShopPrimaryBlue,
    indicatorColor = ShopPrimaryLightBlue,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
)
