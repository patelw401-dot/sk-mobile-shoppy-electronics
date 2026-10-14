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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
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
import com.example.model.RepairBooking
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminRepairsScreen(
    repairs: List<RepairBooking>,
    onUpdateRepairStatus: (bookingId: String, newStatus: String, newCost: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var repairToEditCost by remember { mutableStateOf<RepairBooking?>(null) }
    var newCostInput by remember { mutableStateOf("") }

    val allRepairStatuses = listOf(
        "Requested",
        "Pickup Scheduled",
        "Received",
        "Diagnosing",
        "Estimate Sent",
        "Repairing",
        "Ready",
        "Delivered",
        "Cancelled"
    )

    val filteredRepairs = repairs.filter {
        searchQuery.isBlank() ||
                it.bookingId.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.brand.contains(searchQuery, ignoreCase = true) ||
                it.problem.contains(searchQuery, ignoreCase = true)
    }

    // Cost Edit Modal
    if (repairToEditCost != null) {
        val r = repairToEditCost!!
        AlertDialog(
            onDismissRequest = { repairToEditCost = null },
            containerColor = Color.White,
            title = {
                Text(text = "Update Estimated Repair Cost", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(text = "Repair: ${r.bookingId} • ${r.brand} ${r.model}", fontSize = 13.sp)
                    Text(text = "Issue: ${r.problem}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newCostInput,
                        onValueChange = { newCostInput = it },
                        label = { Text("Estimated Cost (e.g. ₹1,499)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCostInput.isNotBlank()) {
                            onUpdateRepairStatus(r.bookingId, r.status, newCostInput)
                        }
                        repairToEditCost = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
                ) {
                    Text("Save Cost")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { repairToEditCost = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_repairs_screen")
    ) {
        // Search Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Repair Jobs & Bookings (${repairs.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Level-4 technician workflow: diagnosing, parts & delivery",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Repair ID, device or customer...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        // Repairs List
        if (filteredRepairs.isEmpty()) {
            com.example.ui.components.EmptyStateView(
                icon = Icons.Default.Build,
                title = "No Repair Bookings Found",
                message = if (searchQuery.isNotBlank()) "No repair bookings matching '$searchQuery'." else "No active repair bookings registered yet.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredRepairs) { repair ->
                var showStatusMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("admin_repair_card_${repair.bookingId}"),
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
                                text = repair.bookingId,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ShopPrimaryBlue
                            )

                            // Status Dropdown Menu
                            Box {
                                Surface(
                                    color = when (repair.status) {
                                        "Delivered", "Ready" -> Color(0xFFDCFCE7)
                                        "Cancelled" -> Color(0xFFFEE2E2)
                                        "Repairing", "Diagnosing" -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFE0F2FE)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { showStatusMenu = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = repair.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (repair.status) {
                                                "Delivered", "Ready" -> Color(0xFF166534)
                                                "Cancelled" -> Color(0xFF991B1B)
                                                "Repairing", "Diagnosing" -> Color(0xFF92400E)
                                                else -> Color(0xFF0369A1)
                                            }
                                        )
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Change", modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showStatusMenu,
                                    onDismissRequest = { showStatusMenu = false }
                                ) {
                                    allRepairStatuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                onUpdateRepairStatus(repair.bookingId, status, null)
                                                showStatusMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Customer: ${repair.customerName} • +91 ${repair.mobileNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Device: ${repair.brand} ${repair.model} • Fault: ${repair.problem}",
                            fontSize = 12.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Pickup Required: ${if (repair.pickupRequired) "Yes (Doorstep Udgir)" else "No (Drop at Store)"} • Date: ${repair.preferredDate}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        if (repair.address.isNotBlank()) {
                            Text(
                                text = "Address: ${repair.address}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cost: ${repair.estimatedCost}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534)
                            )

                            OutlinedButton(
                                onClick = {
                                    newCostInput = repair.estimatedCost
                                    repairToEditCost = repair
                                },
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Cost", modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Cost", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
