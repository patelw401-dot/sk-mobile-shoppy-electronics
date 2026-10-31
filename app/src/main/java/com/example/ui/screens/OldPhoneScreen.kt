package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.SellRequest
import com.example.ui.components.ProductCard
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.ui.theme.ShopSuccessGreen
import com.example.ui.theme.ShopTextPrimary
import com.example.ui.theme.ShopTextSecondary

@Composable
fun OldPhoneScreen(
    initialTab: Int, // 0 = Buy Old, 1 = Sell Old
    preOwnedProducts: List<Product>,
    defaultName: String,
    defaultPhone: String,
    defaultAddress: String,
    activeSellRequests: List<SellRequest>,
    lastConfirmedSellRequest: SellRequest?,
    onSelectProduct: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onBuyNow: (Product) -> Unit,
    onSubmitSellRequest: (
        customerName: String,
        mobileNumber: String,
        brand: String,
        model: String,
        storage: String,
        condition: String,
        expectedPrice: String,
        address: String
    ) -> Unit,
    onDismissSellDialog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    // Valuation Confirmation Dialog
    if (lastConfirmedSellRequest != null) {
        AlertDialog(
            onDismissRequest = onDismissSellDialog,
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
                        text = "Valuation Generated!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
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
                                text = "Request ID: ${lastConfirmedSellRequest.requestId}",
                                fontWeight = FontWeight.Black,
                                color = ShopPrimaryBlue,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${lastConfirmedSellRequest.brand} ${lastConfirmedSellRequest.model} (${lastConfirmedSellRequest.storage})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopTextPrimary
                            )
                            Text(
                                text = "Condition: ${lastConfirmedSellRequest.condition}",
                                fontSize = 12.sp,
                                color = ShopTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Estimated Instant Cash Quote:\n${formatInr(lastConfirmedSellRequest.estimatedQuoteMin)} - ${formatInr(lastConfirmedSellRequest.estimatedQuoteMax)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "SK Mobile Shoppy and Electronics technician will inspect your phone at ${lastConfirmedSellRequest.address} in Udgir and hand over instant cash or UPI payment on the spot!",
                        fontSize = 12.sp,
                        color = ShopTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissSellDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color(0xFF0B0B0B)),
                    modifier = Modifier.fillMaxWidth().testTag("sell_dialog_ok_btn")
                ) {
                    Text("OK, View Tracking", fontWeight = FontWeight.Bold, color = Color(0xFF0B0B0B))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BlackBackground)
            .testTag("old_phone_screen")
    ) {
        // Top App Bar
        Surface(
            color = com.example.ui.theme.BlackSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
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
                    Text(
                        text = "Buy & Sell Old Mobiles",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = com.example.ui.theme.BlackSurface,
                    contentColor = ShopPrimaryBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ShopPrimaryBlue
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Buy Certified Old Phones",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == 0) com.example.ui.theme.GoldHighlight else ShopTextSecondary
                            )
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Sell Your Phone (Instant Cash)",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) com.example.ui.theme.GoldHighlight else ShopTextSecondary
                            )
                        }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // Tab 0: Buy Pre-Owned Mobiles
            val filteredPreOwned = preOwnedProducts.filter { it.category == ProductCategory.OLD_PHONES || it.isPreOwned }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = com.example.ui.theme.GoldHighlight,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SK Certified Pre-Owned Assurance",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Every refurbished phone is inspected via a 32-point technical diagnostic test. Comes with 6 Months SK Mobile Shoppy and Electronics warranty card & invoice.",
                                fontSize = 12.sp,
                                color = com.example.ui.theme.TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(filteredPreOwned) { product ->
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        ProductCard(
                            product = product,
                            onClick = { onSelectProduct(product) },
                            onAddToCart = { onAddToCart(product) },
                            onBuyNow = { onBuyNow(product) }
                        )
                    }
                }
            }
        } else {
            // Tab 1: Sell Old Phone Form
            SellOldPhoneForm(
                defaultName = defaultName,
                defaultPhone = defaultPhone,
                defaultAddress = defaultAddress,
                activeSellRequests = activeSellRequests,
                onSubmit = onSubmitSellRequest
            )
        }
    }
}

@Composable
fun SellOldPhoneForm(
    defaultName: String,
    defaultPhone: String,
    defaultAddress: String,
    activeSellRequests: List<SellRequest>,
    onSubmit: (
        customerName: String,
        mobileNumber: String,
        brand: String,
        model: String,
        storage: String,
        condition: String,
        expectedPrice: String,
        address: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf(defaultName) }
    var mobileNumber by remember { mutableStateOf(defaultPhone) }
    var selectedBrand by remember { mutableStateOf("Apple") }
    var modelName by remember { mutableStateOf("") }
    var selectedStorage by remember { mutableStateOf("128GB") }
    var selectedCondition by remember { mutableStateOf("Like New (Flawless)") }
    var expectedPrice by remember { mutableStateOf("") }
    var address by remember { mutableStateOf(defaultAddress) }
    var simulatedPhotosCount by remember { mutableIntStateOf(2) }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val storageOptions = listOf("64GB", "128GB", "256GB", "512GB", "1TB")
    val conditionOptions = listOf(
        "Like New (Flawless)" to "Zero scratches, 100% battery & functions working",
        "Good (Minor usage)" to "Light hairline marks, no deep dents or cracks",
        "Fair (Scratches/Dents)" to "Visible wear & tear, screen intact and functional",
        "Needs Repair (Cracked)" to "Cracked screen or battery issues"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Step 1: Customer Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Customer Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            nameError = it.isBlank()
                        },
                        label = { Text("Your Name *") },
                        isError = nameError,
                        modifier = Modifier.fillMaxWidth().testTag("sell_customer_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = {
                            mobileNumber = it
                            phoneError = it.isBlank()
                        },
                        label = { Text("Mobile Number *") },
                        isError = phoneError,
                        modifier = Modifier.fillMaxWidth().testTag("sell_mobile_number"),
                        singleLine = true
                    )
                }
            }
        }

        // Step 2: Device Specs
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
                        text = "2. Phone Specifications",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Brand:", fontSize = 12.sp, color = ShopTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))

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
                        label = { Text("Model Name (e.g. iPhone 12, Galaxy S20 FE, OnePlus 8T) *") },
                        placeholder = { Text("e.g. iPhone 12") },
                        isError = modelError,
                        modifier = Modifier.fillMaxWidth().testTag("sell_model_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Storage Capacity:", fontSize = 12.sp, color = ShopTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(storageOptions) { storage ->
                            val isSelected = selectedStorage == storage
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStorage = storage },
                                label = { Text(storage, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ShopPrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Step 3: Condition & Expected Price
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
                        text = "3. Phone Condition & Price",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    conditionOptions.forEach { (cond, note) ->
                        val isSelected = selectedCondition == cond
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedCondition = cond },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ShopPrimaryLightBlue else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ShopPrimaryBlue else ShopOutlineLight
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = cond,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ShopPrimaryBlue else ShopTextPrimary
                                )
                                Text(
                                    text = note,
                                    fontSize = 11.sp,
                                    color = ShopTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = expectedPrice,
                        onValueChange = { expectedPrice = it },
                        label = { Text("Your Expected Price (₹)") },
                        placeholder = { Text("e.g. 18000") },
                        modifier = Modifier.fillMaxWidth().testTag("sell_expected_price"),
                        singleLine = true
                    )
                }
            }
        }

        // Step 4: Address & Photo Upload UI
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
                        text = "4. Inspection Address & Photos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address in Udgir for Pickup / Verification *") },
                        modifier = Modifier.fillMaxWidth().testTag("sell_address_input"),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Upload Device Photos ($simulatedPhotosCount/4 added):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Photo Upload UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(4) { idx ->
                            val isAdded = idx < simulatedPhotosCount
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clickable {
                                        simulatedPhotosCount = if (isAdded) simulatedPhotosCount - 1 else simulatedPhotosCount + 1
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAdded) ShopPrimaryLightBlue else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdded) ShopPrimaryBlue else ShopOutlineLight)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isAdded) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Photo",
                                        tint = if (isAdded) ShopPrimaryBlue else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (isAdded) "Photo ${idx + 1}" else "Add",
                                        fontSize = 9.sp,
                                        color = if (isAdded) ShopPrimaryBlue else Color.Gray
                                    )
                                }
                            }
                        }
                    }
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
                        isSubmitting = true
                        onSubmit(
                            customerName,
                            mobileNumber,
                            selectedBrand,
                            modelName,
                            selectedStorage,
                            selectedCondition,
                            expectedPrice,
                            address
                        )
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_sell_phone_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generating Valuation...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Get Instant Quote & Sell",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Existing Valuation Requests
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Your Sell Requests (${activeSellRequests.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ShopTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (activeSellRequests.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text(
                        text = "No sell requests generated yet. Fill the valuation form above for instant quote!",
                        fontSize = 12.sp,
                        color = ShopTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                activeSellRequests.forEach { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = req.requestId,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ShopPrimaryBlue
                                )
                                Text(
                                    text = req.status,
                                    fontSize = 11.sp,
                                    color = ShopSuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${req.brand} ${req.model} (${req.storage}) • ${req.condition}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Estimated Value: ${formatInr(req.estimatedQuoteMin)} - ${formatInr(req.estimatedQuoteMax)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            }
        }
    }
}
