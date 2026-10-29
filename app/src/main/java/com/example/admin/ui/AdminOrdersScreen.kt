package com.example.admin.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderTrackingStage
import com.example.model.ShopOrder
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopPrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminOrdersScreen(
    orders: List<ShopOrder>,
    onUpdateOrderStatus: (orderId: String, newStatus: String, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedOrderToView by remember { mutableStateOf<ShopOrder?>(null) }
    var orderToUpdateStatus by remember { mutableStateOf<ShopOrder?>(null) }

    val filteredOrders = orders.filter {
        searchQuery.isBlank() ||
                it.orderId.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.mobileNumber.contains(searchQuery)
    }

    // Status Transition & Audit Note Dialog
    if (orderToUpdateStatus != null) {
        val targetOrder = orderToUpdateStatus!!
        val permittedStatuses = OrderTrackingStage.getPermittedNextStatuses(targetOrder.status)
        var selectedNextStatus by remember { mutableStateOf(permittedStatuses.firstOrNull() ?: "") }
        var updateNote by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { orderToUpdateStatus = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Update Status • ${targetOrder.orderId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Current Status: ${targetOrder.status}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (permittedStatuses.isEmpty()) {
                        Text(
                            text = "This order is in a final state (${targetOrder.status}) and cannot be transitioned further.",
                            fontSize = 13.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Select Valid Next Status:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        permittedStatuses.forEach { statusOption ->
                            val isSelected = selectedNextStatus == statusOption
                            Surface(
                                color = if (isSelected) ShopPrimaryBlue.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ShopPrimaryBlue else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selectedNextStatus = statusOption }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                if (isSelected) ShopPrimaryBlue else Color(0xFFCBD5E1),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = statusOption,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ShopPrimaryBlue else Color(0xFF1E293B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = updateNote,
                            onValueChange = { updateNote = it },
                            label = { Text("Audit Note / Delivery Details (Optional)", fontSize = 11.sp) },
                            placeholder = { Text("e.g., Dispatched with delivery partner / tracking #...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )

                        if (errorText != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = errorText!!, color = Color(0xFFDC2626), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (permittedStatuses.isNotEmpty()) {
                    Button(
                        onClick = {
                            if (selectedNextStatus.isBlank()) {
                                errorText = "Please select a status."
                                return@Button
                            }
                            if (!OrderTrackingStage.canTransition(targetOrder.status, selectedNextStatus)) {
                                errorText = "Invalid transition from ${targetOrder.status} to $selectedNextStatus."
                                return@Button
                            }
                            onUpdateOrderStatus(targetOrder.orderId, selectedNextStatus, updateNote)
                            orderToUpdateStatus = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
                    ) {
                        Text("Apply Update")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToUpdateStatus = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Order Details Modal
    if (selectedOrderToView != null) {
        val o = selectedOrderToView!!
        AlertDialog(
            onDismissRequest = { selectedOrderToView = null },
            containerColor = Color.White,
            title = {
                Text(text = "Order Details • ${o.orderId}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = "Customer: ${o.customerName} (+91 ${o.mobileNumber})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Delivery Address: ${o.address}, ${o.city} - ${o.pinCode}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(text = "Option: ${o.deliveryOption}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(text = "Payment: ${o.paymentMethod} (${o.paymentStatus})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Current Status: ${o.status}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ShopPrimaryBlue)

                    if (!o.cancellationReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cancellation Reason: ${o.cancellationReason}",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Items Ordered (${o.items.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    o.items.forEach { item ->
                        Text(text = "• ${item.quantity}x ${item.product.name} (${formatInr(item.product.price)})", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Total Amount: ${formatInr(o.totalAmount)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = ShopPrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Status History Audit Log:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (o.statusHistory.isEmpty()) {
                        Text(text = "No recorded transitions yet.", fontSize = 11.sp, color = Color(0xFF64748B))
                    } else {
                        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        o.statusHistory.forEach { entry ->
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = entry.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ShopPrimaryBlue)
                                        Text(text = dateFormat.format(Date(entry.timestamp)), fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                    if (entry.note.isNotBlank()) {
                                        Text(text = entry.note, fontSize = 11.sp, color = Color(0xFF334155))
                                    }
                                    Text(text = "By: ${entry.updatedBy}", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedOrderToView = null }, colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_orders_screen")
    ) {
        // Header Search
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Customer Orders (${orders.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Track, pack and manage store deliveries in Udgir",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Order ID, customer or phone...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        // Orders Table List
        if (filteredOrders.isEmpty()) {
            com.example.ui.components.EmptyStateView(
                icon = Icons.Default.ReceiptLong,
                title = "No Orders Found",
                message = if (searchQuery.isNotBlank()) "No orders matching '$searchQuery'." else "No customer orders have been placed yet.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredOrders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .testTag("admin_order_card_${order.orderId}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.orderId,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ShopPrimaryBlue
                                )

                                // Clickable status pill opens validated transition dialog
                                Surface(
                                    color = when (order.status) {
                                        "Delivered" -> Color(0xFFDCFCE7)
                                        "Cancelled" -> Color(0xFFFEE2E2)
                                        "Out for Delivery", "Shipped / Out for Delivery" -> Color(0xFFE0E7FF)
                                        else -> Color(0xFFFEF3C7)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { orderToUpdateStatus = order }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = order.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (order.status) {
                                                "Delivered" -> Color(0xFF166534)
                                                "Cancelled" -> Color(0xFF991B1B)
                                                "Out for Delivery", "Shipped / Out for Delivery" -> Color(0xFF3730A3)
                                                else -> Color(0xFF92400E)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Status",
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Customer: ${order.customerName} • +91 ${order.mobileNumber}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${order.items.size} item(s) • ${order.orderDate} • ${order.paymentMethod}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Amount: ${formatInr(order.totalAmount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                Row {
                                    OutlinedButton(
                                        onClick = { orderToUpdateStatus = order },
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Update", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Update", fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedButton(
                                        onClick = { selectedOrderToView = order },
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "View", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Details", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
