package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MockData
import com.example.model.RepairBooking
import com.example.ui.theme.ShopAccentAmber
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.ui.theme.ShopSuccessGreen
import com.example.ui.theme.ShopTextPrimary
import com.example.ui.theme.ShopTextSecondary

@Composable
fun RepairBookingScreen(
    initialProblem: String,
    initialBrand: String,
    defaultName: String,
    defaultPhone: String,
    defaultAddress: String,
    activeBookings: List<RepairBooking>,
    lastConfirmedBooking: RepairBooking?,
    isCustomerAuthenticated: Boolean = true,
    isSubmitting: Boolean = false,
    onSubmitBooking: (
        customerName: String,
        mobileNumber: String,
        brand: String,
        model: String,
        problem: String,
        pickupRequired: Boolean,
        address: String,
        preferredDate: String,
        additionalDetails: String
    ) -> Unit,
    onDismissConfirmDialog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customerName by remember { mutableStateOf(defaultName) }
    var mobileNumber by remember { mutableStateOf(defaultPhone) }
    var selectedBrand by remember { mutableStateOf(initialBrand) }
    var modelName by remember { mutableStateOf("") }
    var selectedProblem by remember { mutableStateOf(initialProblem) }
    var pickupRequired by remember { mutableStateOf(true) }
    var address by remember { mutableStateOf(defaultAddress) }
    var preferredDate by remember { mutableStateOf("Today / Earliest Slot") }
    var additionalDetails by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf(false) }

    LaunchedEffect(lastConfirmedBooking) {
        if (lastConfirmedBooking != null) {
            modelName = ""
            additionalDetails = ""
        }
    }

    val problemsList = listOf(
        "Display Replacement",
        "Battery Replacement",
        "Charging Problem",
        "Speaker/Mic Problem",
        "Software Problem",
        "Water Damage",
        "Camera / Glass",
        "Dead / Motherboard"
    )

    val dateSlots = listOf(
        "Today / Earliest Slot",
        "Tomorrow Morning",
        "Tomorrow Evening",
        "Weekend Slot"
    )

    // Confirmation Dialog
    if (lastConfirmedBooking != null) {
        AlertDialog(
            onDismissRequest = onDismissConfirmDialog,
            containerColor = Color.White,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = ShopSuccessGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your repair request has been submitted successfully.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = ShopTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = ShopPrimaryLightBlue,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Repair ID: ${lastConfirmedBooking.bookingId}",
                                fontWeight = FontWeight.Black,
                                color = ShopPrimaryBlue,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Device: ${lastConfirmedBooking.brand} ${lastConfirmedBooking.model}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ShopTextPrimary
                            )
                            Text(
                                text = "Issue: ${lastConfirmedBooking.problem}",
                                fontSize = 12.sp,
                                color = ShopTextSecondary
                            )
                            Text(
                                text = "Status: ${lastConfirmedBooking.status}",
                                fontSize = 12.sp,
                                color = ShopSuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (lastConfirmedBooking.pickupRequired) {
                            "Our technician will visit ${lastConfirmedBooking.address} for doorstep collection. Estimated repair time: 45-60 mins."
                        } else {
                            "Please bring your device to SK Mobile Shoppy and Electronic Shop, Nagar Parishad, Udgir. Your slot has been reserved!"
                        },
                        fontSize = 12.sp,
                        color = ShopTextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Need urgent help? Call SK Mobile Shoppy and Electronic: +91 98765 43210",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShopPrimaryBlue
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissConfirmDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color(0xFF0B0B0B)),
                    modifier = Modifier.fillMaxWidth().testTag("repair_dialog_ok_btn")
                ) {
                    Text("Got It, Track My Repair", fontWeight = FontWeight.Bold, color = Color(0xFF0B0B0B))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BlackBackground)
            .testTag("repair_booking_screen")
    ) {
        // Top Bar
        Surface(
            color = com.example.ui.theme.BlackSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ShopTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Mobile Repair Booking",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )
                    Text(
                        text = "SK Mobile Shoppy and Electronic Service Center • Udgir",
                        fontSize = 11.sp,
                        color = ShopTextSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Repair Trust Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurfaceSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ShopPrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Warranty",
                                tint = Color(0xFF0B0B0B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "90-Day Warranty on All Repairs",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopPrimaryBlue
                            )
                            Text(
                                text = "100% genuine parts & free doorstep diagnosis in Udgir",
                                fontSize = 11.sp,
                                color = com.example.ui.theme.TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Customer Contact Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customer Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                nameError = it.isBlank()
                            },
                            label = { Text("Your Name *") },
                            isError = nameError,
                            modifier = Modifier.fillMaxWidth().testTag("repair_customer_name"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = {
                                mobileNumber = it
                                phoneError = it.isBlank()
                            },
                            label = { Text("Mobile Number (for updates) *") },
                            isError = phoneError,
                            modifier = Modifier.fillMaxWidth().testTag("repair_mobile_number"),
                            singleLine = true
                        )
                    }
                }
            }

            // Device Details
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Device Information",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Select Mobile Brand:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ShopTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MockData.popularBrands) { brand ->
                                val isSelected = brand.equals(selectedBrand, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedBrand = brand },
                                    label = { Text(brand, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ShopPrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = modelName,
                            onValueChange = {
                                modelName = it
                                modelError = it.isBlank()
                            },
                            label = { Text("Mobile Model (e.g. iPhone 13, OnePlus Nord CE, Galaxy S21) *") },
                            placeholder = { Text("e.g. Redmi Note 12 Pro") },
                            isError = modelError,
                            modifier = Modifier.fillMaxWidth().testTag("repair_model_name"),
                            singleLine = true
                        )
                    }
                }
            }

            // Repair Issue Selection
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Problem / Fault",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val chunkedProblems = problemsList.chunked(2)
                        chunkedProblems.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { problem ->
                                    val isSelected = selectedProblem == problem
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                            .clickable { selectedProblem = problem },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) com.example.ui.theme.BlackSurfaceSecondary else com.example.ui.theme.BlackSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) ShopPrimaryBlue else com.example.ui.theme.BlackBorder
                                        )
                                    ) {
                                        Text(
                                            text = problem,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) com.example.ui.theme.GoldHighlight else ShopTextPrimary,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pickup & Address Options
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pickup & Drop Service",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pickupRequired = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = pickupRequired,
                                onClick = { pickupRequired = true },
                                colors = RadioButtonDefaults.colors(selectedColor = ShopPrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Yes, Home Pickup in Udgir (Free)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary
                                )
                                Text(
                                    text = "Executive visits your location to collect and return device",
                                    fontSize = 11.sp,
                                    color = ShopTextSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pickupRequired = false }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !pickupRequired,
                                onClick = { pickupRequired = false },
                                colors = RadioButtonDefaults.colors(selectedColor = ShopPrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "No, I will visit SK Mobile Shoppy and Electronic shop",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary
                                )
                                Text(
                                    text = "Nagar Parishad, Udgir (Get express 30-min counter service)",
                                    fontSize = 11.sp,
                                    color = ShopTextSecondary
                                )
                            }
                        }

                        if (pickupRequired) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Pickup Address in Udgir *") },
                                placeholder = { Text("House/Shop No., Street, Landmark...") },
                                modifier = Modifier.fillMaxWidth().testTag("repair_address_input"),
                                minLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Preferred Date & Time:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ShopTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dateSlots) { slot ->
                                val isSelected = preferredDate == slot
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { preferredDate = slot },
                                    label = { Text(slot, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ShopPrimaryBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = additionalDetails,
                            onValueChange = { additionalDetails = it },
                            label = { Text("Additional Fault Details (Optional)") },
                            placeholder = { Text("e.g. Phone fell in water, display flickering, touch delay...") },
                            modifier = Modifier.fillMaxWidth().testTag("repair_notes_input"),
                            minLines = 2
                        )
                    }
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        val isNameValid = customerName.isNotBlank()
                        val isPhoneValid = mobileNumber.isNotBlank()
                        val isModelValid = modelName.isNotBlank()

                        nameError = !isNameValid
                        phoneError = !isPhoneValid
                        modelError = !isModelValid

                        if (isNameValid && isPhoneValid && isModelValid) {
                            onSubmitBooking(
                                customerName,
                                mobileNumber,
                                selectedBrand,
                                modelName,
                                selectedProblem,
                                pickupRequired,
                                if (pickupRequired) address else "Drop-off at Shop",
                                preferredDate,
                                additionalDetails
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_repair_booking_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color(0xFF0B0B0B))
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color(0xFF0B0B0B),
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Booking Repair...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B0B0B)
                        )
                    } else {
                        Text(
                            text = "Book Repair Now",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B0B0B)
                        )
                    }
                }
                if (!isCustomerAuthenticated) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Note: You will be prompted to sign in with your Google account to confirm booking.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Active Repair Trackers Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Track Your Repair Bookings (${activeBookings.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (activeBookings.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No active repairs tracked yet. Book above to get instant tracking ID!",
                            fontSize = 12.sp,
                            color = ShopTextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    activeBookings.forEach { booking ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("active_booking_card_${booking.bookingId}"),
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
                                        text = booking.bookingId,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ShopPrimaryBlue
                                    )

                                    Surface(
                                        color = ShopSuccessGreen,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = booking.status,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${booking.brand} ${booking.model} • ${booking.problem}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary
                                )

                                Text(
                                    text = "Estimated: ${booking.estimatedCost} • Slot: ${booking.preferredDate}",
                                    fontSize = 11.sp,
                                    color = ShopTextSecondary
                                )

                                if (booking.address.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Address: ${booking.address}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
