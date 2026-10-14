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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
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
import com.example.model.ShopOrder
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminOrdersScreen(
    orders: List<ShopOrder>,
    onUpdateOrderStatus: (orderId: String, newStatus: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedOrderToView by remember { mutableStateOf<ShopOrder?>(null) }

    val allStatuses = listOf("Pending", "Confirmed", "Packed", "Out for Delivery", "Delivered", "Cancelled")

    val filteredOrders = orders.filter {
        searchQuery.isBlank() ||
                it.orderId.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.mobileNumber.contains(searchQuery)
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
                Column {
                    Text(text = "Customer: ${o.customerName} (+91 ${o.mobileNumber})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Delivery Address: ${o.address}, ${o.city} - ${o.pinCode}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(text = "Option: ${o.deliveryOption}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(text = "Payment: ${o.paymentMethod}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Status: ${o.status}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ShopPrimaryBlue)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Items Ordered (${o.items.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    o.items.forEach { item ->
                        Text(text = "• ${item.quantity}x ${item.product.name} (${formatInr(item.product.price)})", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Total Amount: ${formatInr(o.totalAmount)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
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
                var showStatusMenu by remember { mutableStateOf(false) }

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

                            // Status Dropdown Menu Pill
                            Box {
                                Surface(
                                    color = when (order.status) {
                                        "Delivered" -> Color(0xFFDCFCE7)
                                        "Cancelled" -> Color(0xFFFEE2E2)
                                        "Out for Delivery" -> Color(0xFFE0E7FF)
                                        else -> Color(0xFFFEF3C7)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { showStatusMenu = true }
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
                                                "Out for Delivery" -> Color(0xFF3730A3)
                                                else -> Color(0xFF92400E)
                                            }
                                        )
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Change", modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showStatusMenu,
                                    onDismissRequest = { showStatusMenu = false }
                                ) {
                                    allStatuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                onUpdateOrderStatus(order.orderId, status)
                                                showStatusMenu = false
                                            }
                                        )
                                    }
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
