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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.model.MockData
import com.example.model.RepairBooking
import com.example.model.RepairService
import com.example.ui.theme.LightBlueBg
import com.example.ui.theme.LightBlueText
import com.example.ui.theme.LightInputBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.lightOutlinedTextFieldColors

@Composable
fun AdminRepairsScreen(
    repairs: List<RepairBooking>,
    repairServices: List<RepairService> = MockData.repairServices,
    onUpdateRepairStatus: (bookingId: String, newStatus: String, newCost: String?) -> Unit,
    onSaveRepairPrices: (List<RepairService>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var repairToEditCost by remember { mutableStateOf<RepairBooking?>(null) }
    var newCostInput by remember { mutableStateOf("") }
    var isPriceManagerExpanded by remember { mutableStateOf(true) }
    val priceInputs = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(repairServices) {
        repairServices.forEach { svc ->
            priceInputs[svc.id] = svc.estimatedStartingPrice.toString()
        }
    }

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
                Text(text = "Update Estimated Repair Cost", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
            },
            text = {
                Column {
                    Text(text = "Repair: ${r.bookingId} • ${r.brand} ${r.model}", fontSize = 13.sp, color = LightTextPrimary)
                    Text(text = "Issue: ${r.problem}", fontSize = 12.sp, color = LightTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newCostInput,
                        onValueChange = { newCostInput = it },
                        label = { Text("Estimated Cost (e.g. ₹1,499)") },
                        colors = lightOutlinedTextFieldColors(),
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
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color.White)
                ) {
                    Text("Save Cost", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { repairToEditCost = null },
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightInputBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary)
                ) {
                    Text("Cancel", color = LightTextPrimary)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_repairs_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Scrollable Search Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Repair Jobs & Bookings (${repairs.size})",
                        fontSize = 16.sp,
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
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = LightTextSecondary, modifier = Modifier.size(18.dp)) },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Dynamic Repair Price Management Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_repair_price_management_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPriceManagerExpanded = !isPriceManagerExpanded }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = LightBlueBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = "Repair Prices",
                                    tint = LightBlueText,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Repair Price Management",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightTextPrimary
                                )
                                Text(
                                    text = "Update live starting rates shown on Home & Booking",
                                    fontSize = 11.sp,
                                    color = LightTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isPriceManagerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isPriceManagerExpanded) "Collapse Repair Prices" else "Expand Repair Prices",
                            tint = LightTextPrimary
                        )
                    }

                    if (isPriceManagerExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            repairServices.forEach { service ->
                                val currentValue = priceInputs[service.id] ?: service.estimatedStartingPrice.toString()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 10.dp)
                                    ) {
                                        Text(
                                            text = service.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LightTextPrimary
                                        )
                                        Text(
                                            text = service.turnaroundTime,
                                            fontSize = 11.sp,
                                            color = LightTextSecondary
                                        )
                                    }

                                    OutlinedTextField(
                                        value = currentValue,
                                        onValueChange = { raw ->
                                            val digitsOnly = raw.filter { it.isDigit() }
                                            priceInputs[service.id] = digitsOnly
                                        },
                                        prefix = {
                                            Text(
                                                text = "₹",
                                                color = LightTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = lightOutlinedTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .width(124.dp)
                                            .testTag("repair_price_input_${service.id}")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val updatedList = repairServices.map { svc ->
                                    val parsed = priceInputs[svc.id]?.toIntOrNull()
                                    if (parsed != null && parsed >= 0) {
                                        svc.copy(estimatedStartingPrice = parsed)
                                    } else {
                                        svc
                                    }
                                }
                                onSaveRepairPrices(updatedList)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShopPrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("save_repair_prices_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Repair Prices",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save Repair Prices",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Repairs List
        if (filteredRepairs.isEmpty()) {
            item {
                com.example.ui.components.EmptyStateView(
                    icon = Icons.Default.Build,
                    title = "No Repair Bookings Found",
                    message = if (searchQuery.isNotBlank()) "No repair bookings matching '$searchQuery'." else "No active repair bookings registered yet.",
                    isDarkSurface = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }
        } else {
            items(filteredRepairs) { repair ->
                var showStatusMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                color = ShopPrimaryBlue,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

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
                                        val statusTextColor = when (repair.status) {
                                            "Delivered", "Ready" -> Color(0xFF166534)
                                            "Cancelled" -> Color(0xFF991B1B)
                                            "Repairing", "Diagnosing" -> Color(0xFF92400E)
                                            else -> Color(0xFF0369A1)
                                        }
                                        Text(
                                            text = repair.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false,
                                            color = statusTextColor
                                        )
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Change", tint = statusTextColor, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showStatusMenu,
                                    onDismissRequest = { showStatusMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    allRepairStatuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status, color = LightTextPrimary) },
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
                                color = Color(0xFF166534),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = {
                                    newCostInput = repair.estimatedCost
                                    repairToEditCost = repair
                                },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LightInputBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Cost", tint = LightTextPrimary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Cost", fontSize = 11.sp, color = LightTextPrimary, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }
}
