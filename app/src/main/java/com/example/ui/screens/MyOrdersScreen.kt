package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderTrackingStage
import com.example.model.ShopOrder
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatInr
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.BlackSurfaceSecondary
import com.example.ui.theme.ColorError
import com.example.ui.theme.ColorSuccess
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OrderFilter(val label: String) {
    ALL("All Orders"),
    ACTIVE("In Progress"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    orders: List<ShopOrder>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectOrder: (ShopOrder) -> Unit,
    onRefresh: () -> Unit,
    onBrowseShop: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedFilter by remember { mutableStateOf(OrderFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredOrders = orders.filter { order ->
        val matchesFilter = when (selectedFilter) {
            OrderFilter.ALL -> true
            OrderFilter.ACTIVE -> !order.isDelivered && !order.isCancelled
            OrderFilter.DELIVERED -> order.isDelivered
            OrderFilter.CANCELLED -> order.isCancelled
        }

        val matchesSearch = searchQuery.isBlank() ||
                order.orderId.contains(searchQuery, ignoreCase = true) ||
                order.items.any { it.product.name.contains(searchQuery, ignoreCase = true) } ||
                order.orderDate.contains(searchQuery, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_orders_screen"),
        containerColor = BlackBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Orders & Tracking",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = "${orders.size} order${if (orders.size != 1) "s" else ""} in your history",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("my_orders_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, modifier = Modifier.testTag("my_orders_refresh_btn")) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Orders",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Order ID or item name...", fontSize = 13.sp, color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("orders_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BlackSurface,
                    unfocusedContainerColor = BlackSurfaceSecondary,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BlackBorder,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain
                ),
                singleLine = true
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(OrderFilter.entries) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            val count = when (filter) {
                                OrderFilter.ALL -> orders.size
                                OrderFilter.ACTIVE -> orders.count { !it.isDelivered && !it.isCancelled }
                                OrderFilter.DELIVERED -> orders.count { it.isDelivered }
                                OrderFilter.CANCELLED -> orders.count { it.isCancelled }
                            }
                            Text(
                                text = "${filter.label} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = BlackSurfaceSecondary,
                            labelColor = TextSecondary,
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BlackBorder,
                            selectedBorderColor = GoldHighlight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Content based on state
            when {
                isLoading && orders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Loading your orders...", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }

                errorMessage != null && orders.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = errorMessage, color = ColorError, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                            ) {
                                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                filteredOrders.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Default.LocalShipping,
                        title = if (searchQuery.isNotBlank()) "No Matching Orders" else "No Orders Yet",
                        message = if (searchQuery.isNotBlank()) {
                            "No orders matched your search '$searchQuery'."
                        } else when (selectedFilter) {
                            OrderFilter.ACTIVE -> "You have no active orders in transit."
                            OrderFilter.DELIVERED -> "No delivered orders found in your history."
                            OrderFilter.CANCELLED -> "No cancelled orders."
                            OrderFilter.ALL -> "You have not placed any orders yet with SK Mobile Udgir."
                        },
                        actionLabel = "Browse Shop & Accessories",
                        onAction = onBrowseShop,
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders, key = { it.orderId }) { order ->
                            CustomerOrderCard(
                                order = order,
                                onClick = { onSelectOrder(order) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerOrderCard(
    order: ShopOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stage = order.trackingStage
    val statusColor = when {
        order.isCancelled -> ColorError
        order.isDelivered -> ColorSuccess
        stage == OrderTrackingStage.OUT_FOR_DELIVERY -> ShopPrimaryBlue
        else -> GoldPrimary
    }

    val statusBg = when {
        order.isCancelled -> ColorError.copy(alpha = 0.15f)
        order.isDelivered -> ColorSuccess.copy(alpha = 0.15f)
        stage == OrderTrackingStage.OUT_FOR_DELIVERY -> ShopPrimaryBlue.copy(alpha = 0.15f)
        else -> GoldPrimary.copy(alpha = 0.15f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("order_card_${order.orderId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        border = BorderStroke(1.dp, if (order.isDelivered) BlackBorder else GoldBorder.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID, Date & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderId,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary
                    )
                    Text(
                        text = "Placed on ${order.orderDate}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = order.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini Progress Bar (5 stages)
            if (!order.isCancelled) {
                OrderMiniProgressBar(currentStage = stage)
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Surface(
                    color = ColorError.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (!order.cancellationReason.isNullOrBlank()) {
                            "Order Cancelled: ${order.cancellationReason}"
                        } else {
                            "Order has been cancelled."
                        },
                        color = ColorError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = BlackBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Items Summary
            val itemsCount = order.items.sumOf { it.quantity }
            val firstItem = order.items.firstOrNull()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BlackSurfaceSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Item",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (firstItem != null) {
                            if (order.items.size > 1) {
                                "${firstItem.product.name} +${order.items.size - 1} more item(s)"
                            } else {
                                "${firstItem.quantity}x ${firstItem.product.name}"
                            }
                        } else {
                            "$itemsCount Item(s)"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMain,
                        maxLines = 1
                    )
                    Text(
                        text = "Payment: ${order.paymentMethod} (${order.paymentStatus})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BlackBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Total Amount & Track Details Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = formatInr(order.totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMain
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Track Order",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Details",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderMiniProgressBar(
    currentStage: OrderTrackingStage,
    modifier: Modifier = Modifier
) {
    val totalSteps = 5
    val currentStep = currentStage.stageIndex.coerceIn(0, totalSteps - 1)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentStage.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentStage == OrderTrackingStage.DELIVERED) ColorSuccess else GoldHighlight
            )
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until totalSteps) {
                val isCompleted = i <= currentStep
                val isFinal = currentStage == OrderTrackingStage.DELIVERED
                val barColor = when {
                    isFinal -> ColorSuccess
                    isCompleted -> GoldPrimary
                    else -> BlackSurfaceSecondary
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }
        }
    }
}
