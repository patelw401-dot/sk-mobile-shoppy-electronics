package com.example.admin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.style.TextOverflow
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

    var showAddServiceDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<RepairService?>(null) }
    var serviceToDelete by remember { mutableStateOf<RepairService?>(null) }

    LaunchedEffect(repairServices) {
        val validIds = repairServices.map { it.id }.toSet()
        priceInputs.keys.toList().forEach { id ->
            if (id !in validIds) priceInputs.remove(id)
        }
        repairServices.forEach { svc ->
            priceInputs[svc.id] = svc.effectivePriceInput
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

    // Add / Edit Repair Category Dialog
    if (showAddServiceDialog || serviceToEdit != null) {
        RepairServiceFormDialog(
            initialService = serviceToEdit,
            onDismiss = {
                showAddServiceDialog = false
                serviceToEdit = null
            },
            onSave = { savedSvc ->
                val updatedList = if (serviceToEdit == null) {
                    repairServices + savedSvc
                } else {
                    repairServices.map { if (it.id == savedSvc.id) savedSvc else it }
                }
                priceInputs[savedSvc.id] = savedSvc.effectivePriceInput
                onSaveRepairPrices(updatedList)
                showAddServiceDialog = false
                serviceToEdit = null
            }
        )
    }

    // Delete Repair Category Confirmation Dialog
    if (serviceToDelete != null) {
        val target = serviceToDelete!!
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Delete Repair Category?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = LightTextPrimary
                )
            },
            text = {
                Text(
                    text = "Remove \"${target.title}\" from Repair Price Management, Home Screen, and Booking options?",
                    fontSize = 13.sp,
                    color = LightTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedList = repairServices.filterNot { it.id == target.id }
                        priceInputs.remove(target.id)
                        onSaveRepairPrices(updatedList)
                        serviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { serviceToDelete = null },
                    border = BorderStroke(1.dp, LightInputBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary)
                ) {
                    Text("Cancel", color = LightTextPrimary)
                }
            }
        )
    }

    // Cost Edit Modal for individual booking
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
                    border = BorderStroke(1.dp, LightInputBorder),
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
        item(key = "repair_search_header") {
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
        item(key = "repair_price_manager") {
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
                                    text = "Repair Price Management (${repairServices.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightTextPrimary
                                )
                                Text(
                                    text = "Add, edit, or delete services & rates (numbers or custom text)",
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

                        // Add New Repair Service / Category Button
                        OutlinedButton(
                            onClick = { showAddServiceDialog = true },
                            border = BorderStroke(1.dp, ShopPrimaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopPrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                                .testTag("add_repair_service_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Repair Service",
                                tint = ShopPrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+ Add Repair Service / Category",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopPrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (repairServices.isEmpty()) {
                            Text(
                                text = "No repair services configured. Tap \"+ Add Repair Service / Category\" above to add one.",
                                fontSize = 12.sp,
                                color = LightTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                repairServices.forEach { service ->
                                    key(service.id) {
                                        val currentValue = priceInputs[service.id] ?: service.effectivePriceInput
                                        val previewText = remember(currentValue, service.estimatedStartingPrice) {
                                            service.copy(priceText = currentValue).formattedPriceDisplay
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = service.title,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = LightTextPrimary,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "${service.turnaroundTime} • Shows: $previewText",
                                                            fontSize = 11.sp,
                                                            color = LightTextSecondary
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconButton(
                                                            onClick = { serviceToEdit = service },
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .testTag("edit_repair_service_btn_${service.id}")
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Edit ${service.title}",
                                                                tint = ShopPrimaryBlue,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { serviceToDelete = service },
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .testTag("delete_repair_service_btn_${service.id}")
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete ${service.title}",
                                                                tint = Color(0xFFDC2626),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                OutlinedTextField(
                                                    value = currentValue,
                                                    onValueChange = { raw ->
                                                        priceInputs[service.id] = raw
                                                    },
                                                    label = { Text("Price / Rate Text", fontSize = 11.sp) },
                                                    placeholder = { Text("e.g. 1499, Free Checkup, As per model", fontSize = 12.sp) },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                                    colors = lightOutlinedTextFieldColors(),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("repair_price_input_${service.id}")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val updatedList = repairServices.map { svc ->
                                    val rawInput = (priceInputs[svc.id] ?: svc.effectivePriceInput).trim()
                                    val resolvedText = rawInput.ifBlank { svc.effectivePriceInput }
                                    val numericFallback = RepairService.extractNumericFallback(resolvedText, svc.estimatedStartingPrice)
                                    svc.copy(
                                        estimatedStartingPrice = numericFallback,
                                        priceText = resolvedText
                                    )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepairServiceFormDialog(
    initialService: RepairService?,
    onDismiss: () -> Unit,
    onSave: (RepairService) -> Unit
) {
    val isEdit = initialService != null
    var title by remember { mutableStateOf(initialService?.title ?: "") }
    var turnaroundTime by remember { mutableStateOf(initialService?.turnaroundTime ?: "30 - 45 Minutes") }
    var priceText by remember { mutableStateOf(initialService?.effectivePriceInput ?: "") }
    var description by remember { mutableStateOf(initialService?.description ?: "") }
    var iconType by remember { mutableStateOf(initialService?.iconType ?: "repair") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val iconOptions = listOf(
        "display" to "Display",
        "battery" to "Battery",
        "charging" to "Charging",
        "audio" to "Audio/Mic",
        "software" to "Software",
        "water" to "Water",
        "repair" to "General"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (isEdit) "Edit Repair Category" else "Add Repair Service / Category",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = LightTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationError = null
                    },
                    label = { Text("Service Name *") },
                    placeholder = { Text("e.g. Back Glass Replacement") },
                    colors = lightOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_service_title_input")
                )

                OutlinedTextField(
                    value = turnaroundTime,
                    onValueChange = { turnaroundTime = it },
                    label = { Text("Estimated Time") },
                    placeholder = { Text("e.g. 30 - 45 Minutes") },
                    colors = lightOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_service_time_input")
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        validationError = null
                    },
                    label = { Text("Price / Custom Text *") },
                    placeholder = { Text("e.g. 1499, Free Checkup, As per model") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = lightOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repair_service_price_text_input")
                )

                val previewDisplay = remember(priceText) {
                    RepairService(
                        id = "preview",
                        title = "",
                        estimatedStartingPrice = RepairService.extractNumericFallback(priceText, 0),
                        priceText = priceText.trim(),
                        turnaroundTime = ""
                    ).formattedPriceDisplay
                }
                if (priceText.isNotBlank()) {
                    Text(
                        text = "Customer Display Preview: $previewDisplay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightBlueText
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Category Icon",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightTextPrimary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        iconOptions.forEach { (key, label) ->
                            val selected = iconType == key
                            FilterChip(
                                selected = selected,
                                onClick = { iconType = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShopPrimaryBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = LightTextPrimary
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Short details about parts or repair quality") },
                    colors = lightOutlinedTextFieldColors(),
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedPrice = priceText.trim()
                    if (trimmedTitle.isEmpty()) {
                        validationError = "Please enter a service name."
                        return@Button
                    }
                    if (trimmedPrice.isEmpty()) {
                        validationError = "Please enter a price or custom rate text."
                        return@Button
                    }
                    val trimmedTime = turnaroundTime.trim().ifBlank { "30 - 60 Minutes" }
                    val numericFallback = RepairService.extractNumericFallback(
                        trimmedPrice,
                        initialService?.estimatedStartingPrice ?: 0
                    )
                    val serviceId = initialService?.id ?: "rep-${System.currentTimeMillis()}"
                    onSave(
                        RepairService(
                            id = serviceId,
                            title = trimmedTitle,
                            estimatedStartingPrice = numericFallback,
                            priceText = trimmedPrice,
                            turnaroundTime = trimmedTime,
                            warrantyDays = initialService?.warrantyDays ?: 90,
                            description = description.trim(),
                            iconType = iconType
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color.White),
                modifier = Modifier.testTag("save_repair_service_dialog_btn")
            ) {
                Text(if (isEdit) "Save Changes" else "Add Service", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, LightInputBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary)
            ) {
                Text("Cancel", color = LightTextPrimary)
            }
        }
    )
}
