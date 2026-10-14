package com.example.admin.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.admin.model.CustomerRecord
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue

@Composable
fun AdminCustomersScreen(
    customers: List<CustomerRecord>,
    modifier: Modifier = Modifier
) {
    var selectedCustomerToView by remember { mutableStateOf<CustomerRecord?>(null) }

    if (selectedCustomerToView != null) {
        val c = selectedCustomerToView!!
        AlertDialog(
            onDismissRequest = { selectedCustomerToView = null },
            containerColor = Color.White,
            title = {
                Text(text = "Customer Profile • ${c.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(text = "Mobile: +91 ${c.mobileNumber}", fontSize = 13.sp)
                    Text(text = "Address: ${c.address}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Order History: ${c.totalOrders} Completed Orders", fontSize = 13.sp)
                    Text(text = "Repair History: ${c.totalRepairs} Phone Repairs", fontSize = 13.sp)
                    Text(text = "Lifetime Spending: ${formatInr(c.totalSpending)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ShopPrimaryBlue)
                    Text(text = "Last Store Interaction: ${c.lastActivity}", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Button(onClick = { selectedCustomerToView = null }, colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_customers_screen")
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Registered Customers (${customers.size})", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = "Udgir store customer records, loyalty and spending", fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(customers) { customer ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("customer_card_${customer.id}"),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(ShopPrimaryLightBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = "User", tint = ShopPrimaryBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = customer.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "+91 ${customer.mobileNumber}", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                            }

                            OutlinedButton(
                                onClick = { selectedCustomerToView = customer },
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Details", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${customer.totalOrders} Orders • ${customer.totalRepairs} Repairs", fontSize = 11.sp, color = Color(0xFF334155))
                            Text(text = "Spent: ${formatInr(customer.totalSpending)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ShopPrimaryBlue)
                        }
                    }
                }
            }
        }
    }
}
