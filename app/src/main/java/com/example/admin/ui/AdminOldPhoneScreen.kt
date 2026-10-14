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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SellRequest
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminOldPhoneScreen(
    sellRequests: List<SellRequest>,
    onUpdateSellRequestStatus: (requestId: String, newStatus: String, offerMin: Int?, offerMax: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sell Requests, 1 = Buy History / Inventory
    var requestToSetOffer by remember { mutableStateOf<SellRequest?>(null) }
    var offerMinText by remember { mutableStateOf("") }
    var offerMaxText by remember { mutableStateOf("") }

    val sellStatuses = listOf("New Request", "Inspection", "Offer Sent", "Accepted", "Rejected", "Purchased")

    // Offer Price Setter Dialog
    if (requestToSetOffer != null) {
        val req = requestToSetOffer!!
        AlertDialog(
            onDismissRequest = { requestToSetOffer = null },
            containerColor = Color.White,
            title = {
                Text(text = "Set Admin Cash Offer Price", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(text = "${req.brand} ${req.model} (${req.storage})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Customer Expected: ${req.expectedPrice} • Condition: ${req.condition}", fontSize = 12.sp, color = Color(0xFF64748B))

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = offerMinText,
                        onValueChange = { offerMinText = it },
                        label = { Text("Minimum Offer (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = offerMaxText,
                        onValueChange = { offerMaxText = it },
                        label = { Text("Maximum Offer (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val min = offerMinText.toIntOrNull()
                        val max = offerMaxText.toIntOrNull()
                        onUpdateSellRequestStatus(req.requestId, "Offer Sent", min, max)
                        requestToSetOffer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
                ) {
                    Text("Send Offer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { requestToSetOffer = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_old_phones_screen")
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Old Mobiles Management",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Manage customer sell valuations & certified pre-owned purchases",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = ShopPrimaryBlue) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Customer Sell Requests (${sellRequests.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Purchased Inventory (${sellRequests.count { it.status == "Purchased" }})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        val displayList = if (selectedTab == 0) {
            sellRequests
        } else {
            sellRequests.filter { it.status == "Purchased" || it.status == "Accepted" }
        }

        if (displayList.isEmpty()) {
            com.example.ui.components.EmptyStateView(
                icon = Icons.Default.PhoneAndroid,
                title = if (selectedTab == 0) "No Sell Requests" else "No Purchased Phones",
                message = if (selectedTab == 0) "No customers have requested to sell an old phone yet." else "No pre-owned phones have been marked as purchased yet.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(displayList) { req ->
                var showStatusMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("admin_sell_request_${req.requestId}"),
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
                                text = req.requestId,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ShopPrimaryBlue
                            )

                            // Status Dropdown Menu
                            Box {
                                Surface(
                                    color = when (req.status) {
                                        "Purchased", "Accepted" -> Color(0xFFDCFCE7)
                                        "Rejected" -> Color(0xFFFEE2E2)
                                        "Offer Sent" -> Color(0xFFE0E7FF)
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
                                            text = req.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (req.status) {
                                                "Purchased", "Accepted" -> Color(0xFF166534)
                                                "Rejected" -> Color(0xFF991B1B)
                                                "Offer Sent" -> Color(0xFF3730A3)
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
                                    sellStatuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                onUpdateSellRequestStatus(req.requestId, status, null, null)
                                                showStatusMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Customer: ${req.customerName} • +91 ${req.mobileNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Phone: ${req.brand} ${req.model} (${req.storage}) • Condition: ${req.condition}",
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Expected: ${req.expectedPrice} • Address: ${req.address}",
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
                                text = "Offered Quote: ${formatInr(req.estimatedQuoteMin)} - ${formatInr(req.estimatedQuoteMax)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )

                            OutlinedButton(
                                onClick = {
                                    offerMinText = req.estimatedQuoteMin.toString()
                                    offerMaxText = req.estimatedQuoteMax.toString()
                                    requestToSetOffer = req
                                },
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Offer", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
