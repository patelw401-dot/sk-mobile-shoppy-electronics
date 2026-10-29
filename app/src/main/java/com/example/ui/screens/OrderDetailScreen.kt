package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderTrackingStage
import com.example.model.ShopOrder
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: ShopOrder?,
    onCancelOrder: (orderId: String, reason: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var selectedReasonPreset by remember { mutableStateOf("Changed my mind") }

    if (order == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BlackBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Order not found", color = TextMain, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                    Text("Go Back", color = Color.Black)
                }
            }
        }
        return
    }

    val currentStage = order.trackingStage
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val lastUpdatedText = dateFormat.format(Date(order.lastUpdated))

    // Cancellation Dialog
    if (showCancelDialog) {
        val presets = listOf(
            "Changed my mind",
            "Found a better price",
            "Ordered wrong item or model",
            "Delivery address needs update",
            "Other reason"
        )

        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = BlackSurface,
            title = {
                Text(
                    text = "Cancel Order #${order.orderId}",
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to cancel this order? This action cannot be undone.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Reason:",
                        color = GoldHighlight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    presets.forEach { preset ->
                        val isSelected = selectedReasonPreset == preset
                        Surface(
                            color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else BlackSurfaceSecondary,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) GoldPrimary else BlackBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { selectedReasonPreset = preset }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (isSelected) GoldPrimary else BlackBorder, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = preset,
                                    fontSize = 12.sp,
                                    color = if (isSelected) GoldHighlight else TextMain
                                )
                            }
                        }
                    }

                    if (selectedReasonPreset == "Other reason") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cancelReason,
                            onValueChange = { cancelReason = it },
                            placeholder = { Text("Specify your reason...", fontSize = 12.sp, color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 2
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val effectiveReason = if (selectedReasonPreset == "Other reason" && cancelReason.isNotBlank()) {
                            cancelReason.trim()
                        } else {
                            selectedReasonPreset
                        }
                        onCancelOrder(order.orderId, effectiveReason)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError),
                    modifier = Modifier.testTag("confirm_cancel_order_btn")
                ) {
                    Text("Confirm Cancellation", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Order", color = TextMain)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("order_detail_screen"),
        containerColor = BlackBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Order Tracking",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = "#${order.orderId}",
                            fontSize = 12.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("order_detail_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackSurface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status Hero Card
            item {
                OrderStatusHeroCard(
                    order = order,
                    lastUpdatedText = lastUpdatedText
                )
            }

            // 5-Stage Visual Timeline
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Timeline",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Order Progress Timeline",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (order.isCancelled) {
                            OrderCancelledTimeline(order = order)
                        } else {
                            OrderActiveTimeline(
                                currentStage = currentStage,
                                history = order.statusHistory
                            )
                        }
                    }
                }
            }

            // Ordered Items
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Items",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Items in this Order (${order.items.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        order.items.forEachIndexed { index, cartItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BlackSurfaceSecondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cartItem.product.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextMain,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = "Qty: ${cartItem.quantity} • ${formatInr(cartItem.product.price)} each",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = formatInr(cartItem.product.price * cartItem.quantity),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldHighlight
                                )
                            }
                            if (index < order.items.size - 1) {
                                HorizontalDivider(color = BlackBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // Delivery & Address Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Address",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delivery & Shipping Address",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = order.customerName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = "+91 ${order.mobileNumber}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${order.address}, ${order.city} - ${order.pinCode}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = BlackSurfaceSecondary,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Delivery Method: ",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = order.deliveryOption,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldHighlight
                                )
                            }
                        }
                    }
                }
            }

            // Payment & Price Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "Payment",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Summary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal", fontSize = 13.sp, color = TextSecondary)
                            Text(text = formatInr(order.subtotal), fontSize = 13.sp, color = TextMain)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Delivery Fee", fontSize = 13.sp, color = TextSecondary)
                            Text(
                                text = if (order.deliveryFee == 0) "FREE" else formatInr(order.deliveryFee),
                                fontSize = 13.sp,
                                color = if (order.deliveryFee == 0) ColorSuccess else TextMain
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BlackBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Amount", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                            Text(text = formatInr(order.totalAmount), fontSize = 17.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Payment Method", fontSize = 12.sp, color = TextMuted)
                            Text(text = "${order.paymentMethod} (${order.paymentStatus})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                        }
                    }
                }
            }

            // Status History Audit Log
            if (order.statusHistory.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BlackSurface),
                        border = BorderStroke(1.dp, BlackBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Status History Log (${order.statusHistory.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val historyDateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            order.statusHistory.forEachIndexed { idx, entry ->
                                Surface(
                                    color = BlackSurfaceSecondary,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = entry.status,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldHighlight
                                            )
                                            Text(
                                                text = historyDateFormat.format(Date(entry.timestamp)),
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        if (entry.note.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = entry.note,
                                                fontSize = 11.sp,
                                                color = TextMain
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Updated by: ${entry.updatedBy}",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Customer Actions: Cancel & Help
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Show Cancel button only if order is not delivered and not already cancelled
                    if (!order.isDelivered && !order.isCancelled) {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("cancel_order_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ColorError.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorError)
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Cancel This Order", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    // Contact store support
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handled gracefully
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("contact_store_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlackSurfaceSecondary),
                        border = BorderStroke(1.dp, GoldBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Need Help? Call SK Mobile Udgir", color = GoldHighlight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusHeroCard(
    order: ShopOrder,
    lastUpdatedText: String,
    modifier: Modifier = Modifier
) {
    val stage = order.trackingStage
    val statusColor = when {
        order.isCancelled -> ColorError
        order.isDelivered -> ColorSuccess
        stage == OrderTrackingStage.OUT_FOR_DELIVERY -> ShopPrimaryBlue
        else -> GoldPrimary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Updated: $lastUpdatedText",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.status,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = statusColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (order.isCancelled) {
                    if (!order.cancellationReason.isNullOrBlank()) {
                        "Cancellation Reason: ${order.cancellationReason}"
                    } else {
                        "This order has been cancelled."
                    }
                } else {
                    stage.subtitle
                },
                fontSize = 13.sp,
                color = if (order.isCancelled) ColorError else TextSecondary
            )
        }
    }
}

@Composable
private fun OrderActiveTimeline(
    currentStage: OrderTrackingStage,
    history: List<com.example.model.OrderStatusHistoryEntry>,
    modifier: Modifier = Modifier
) {
    val stages = listOf(
        OrderTrackingStage.ORDER_PLACED,
        OrderTrackingStage.ORDER_CONFIRMED,
        OrderTrackingStage.PREPARING,
        OrderTrackingStage.OUT_FOR_DELIVERY,
        OrderTrackingStage.DELIVERED
    )

    val currentStageIndex = currentStage.stageIndex.coerceIn(0, stages.size - 1)
    val historyDateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Column(modifier = modifier.fillMaxWidth()) {
        stages.forEachIndexed { index, stage ->
            val isCompleted = index < currentStageIndex
            val isCurrent = index == currentStageIndex
            val isUpcoming = index > currentStageIndex

            // Find matching history timestamp if available
            val historyEntry = history.find { OrderTrackingStage.fromStatus(it.status) == stage }

            Row(modifier = Modifier.fillMaxWidth()) {
                // Left Column: Circle & Line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> ColorSuccess
                                    isCurrent -> GoldPrimary
                                    else -> BlackSurfaceSecondary
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }

                    if (index < stages.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(38.dp)
                                .background(
                                    if (index < currentStageIndex) ColorSuccess else BlackBorder
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Column: Title, Subtitle, and Timestamp
                Column(modifier = Modifier.weight(1f).padding(bottom = if (index < stages.size - 1) 18.dp else 0.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stage.title,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = when {
                                isCompleted -> ColorSuccess
                                isCurrent -> GoldHighlight
                                else -> TextMuted
                            }
                        )
                        if (historyEntry != null) {
                            Text(
                                text = historyDateFormat.format(Date(historyEntry.timestamp)),
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = stage.subtitle,
                        fontSize = 11.sp,
                        color = if (isUpcoming) TextMuted else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCancelledTimeline(
    order: ShopOrder,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(ColorError),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Cancelled",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Order Cancelled",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorError
                )
                Text(
                    text = if (!order.cancellationReason.isNullOrBlank()) {
                        "Reason: ${order.cancellationReason}"
                    } else {
                        "Cancelled by user or store administrator."
                    },
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
