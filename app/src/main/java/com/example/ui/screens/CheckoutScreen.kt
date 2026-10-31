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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    subtotal: Int,
    total: Int,
    totalSavings: Int,
    deliveryFee: Int,
    defaultName: String,
    defaultPhone: String,
    defaultAddress: String,
    isCustomerAuthenticated: Boolean = true,
    onSignInRequired: () -> Unit = {},
    onPlaceOrder: (
        customerName: String,
        mobileNumber: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        deliveryOption: String,
        paymentMethod: String
    ) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(defaultName) }
    var phone by remember { mutableStateOf(defaultPhone) }
    var address by remember { mutableStateOf(defaultAddress) }
    var city by remember { mutableStateOf("Udgir") }
    var state by remember { mutableStateOf("Maharashtra") }
    var pinCode by remember { mutableStateOf("413517") }

    var selectedDeliveryOption by remember { mutableStateOf("Standard Free Delivery in Udgir") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash on Delivery") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
            .testTag("checkout_screen")
    ) {
        // Header
        Surface(
            color = BlackSurface,
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
                Text(
                    text = "Checkout & Delivery",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Step 1: Customer Contact Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Customer Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = it.isBlank()
                            },
                            label = { Text("Customer Name *") },
                            isError = nameError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = it.isBlank()
                            },
                            label = { Text("Mobile Number (10 digits) *") },
                            isError = phoneError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_phone_input"),
                            singleLine = true
                        )
                    }
                }
            }

            // Step 2: Complete Delivery Address
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Complete Delivery Address",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                addressError = it.isBlank()
                            },
                            label = { Text("House / Street / Landmark Address *") },
                            placeholder = { Text("Near Nagar Parishad Office, Station Road...") },
                            isError = addressError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_address_input"),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f).testTag("checkout_city_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State") },
                                modifier = Modifier.weight(1f).testTag("checkout_state_input"),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("PIN Code (e.g. 413517)") },
                            modifier = Modifier.fillMaxWidth().testTag("checkout_pin_input"),
                            singleLine = true
                        )
                    }
                }
            }

            // Step 3: Delivery Option
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. Delivery Option",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val deliveryOptions = listOf(
                            "Standard Free Delivery in Udgir" to "Delivered to your doorstep within 4-6 hours (Free)",
                            "Express 2-Hour Delivery in Udgir" to "Priority dispatch from SK Mobile Shoppy and Electronics store (Free promo)",
                            "Store Pickup at SK Mobile Shoppy and Electronics" to "Collect directly from SK Mobile Shoppy and Electronics Nagar Parishad store in 30 mins"
                        )

                        deliveryOptions.forEach { (title, subtitle) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDeliveryOption = title }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDeliveryOption == title,
                                    onClick = { selectedDeliveryOption = title },
                                    colors = RadioButtonDefaults.colors(selectedColor = ShopPrimaryBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ShopTextPrimary)
                                    Text(text = subtitle, fontSize = 11.sp, color = ShopTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Step 4: Payment Method Selection UI
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "4. Payment Method",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ShopTextPrimary
                        )

                        Text(
                            text = "Select payment mode for this demo order",
                            fontSize = 11.sp,
                            color = ShopTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val paymentMethods = listOf(
                            Triple("UPI", "Instant payment via Google Pay, PhonePe, Paytm or UPI ID", Icons.Default.QrCode2),
                            Triple("Debit / Credit Card", "Visa, Mastercard, RuPay & Maestro cards", Icons.Default.CreditCard),
                            Triple("Net Banking", "All major Indian banks supported (SBI, HDFC, ICICI, etc.)", Icons.Default.AccountBalance),
                            Triple("Cash on Delivery", "Pay cash or scan UPI QR upon doorstep delivery in Udgir", Icons.Default.Money)
                        )

                        paymentMethods.forEach { (name, note, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = name }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == name,
                                    onClick = { selectedPaymentMethod = name },
                                    colors = RadioButtonDefaults.colors(selectedColor = ShopPrimaryBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = ShopPrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ShopTextPrimary)
                                    Text(text = note, fontSize = 11.sp, color = ShopTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Step 5: Order Summary
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackSurface),
                    border = BorderStroke(1.dp, BlackBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "5. Order Summary (${cartItems.sumOf { it.quantity }} items)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.product.name.take(28)}...",
                                    fontSize = 12.sp,
                                    color = ShopTextSecondary
                                )
                                Text(
                                    text = formatInr(item.product.price * item.quantity),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = ShopOutlineLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Payable Amount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            Text(
                                text = formatInr(total + deliveryFee),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ShopPrimaryBlue
                            )
                        }
                    }
                }
            }
        }

        // Bottom Place Order Bar
        Surface(
            color = BlackSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                if (!isCustomerAuthenticated) {
                    Surface(
                        color = BlackSurfaceSecondary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sign in with Google required to place order",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorError,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = onSignInRequired,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackBackground),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("checkout_signin_google_btn")
                            ) {
                                Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatInr(total + deliveryFee),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ShopTextPrimary
                        )
                        Text(
                            text = selectedPaymentMethod,
                            fontSize = 11.sp,
                            color = ShopTextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            if (!isCustomerAuthenticated) {
                                onSignInRequired()
                                return@Button
                            }
                            val isNameValid = name.isNotBlank()
                            val isPhoneValid = phone.isNotBlank()
                            val isAddressValid = address.isNotBlank()

                        nameError = !isNameValid
                        phoneError = !isPhoneValid
                        addressError = !isAddressValid

                        if (isNameValid && isPhoneValid && isAddressValid) {
                            isSubmitting = true
                            onPlaceOrder(
                                name,
                                phone,
                                address,
                                city,
                                state,
                                pinCode,
                                selectedDeliveryOption,
                                selectedPaymentMethod
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("place_order_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Placing Order...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    } else {
                        Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
}
