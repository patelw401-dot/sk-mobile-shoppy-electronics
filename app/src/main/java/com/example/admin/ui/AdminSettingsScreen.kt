package com.example.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.admin.model.ShopSettings
import com.example.ui.theme.ColorError
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.lightOutlinedTextFieldColors

@Composable
fun AdminSettingsScreen(
    settings: ShopSettings,
    onSaveSettings: (ShopSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var shopName by remember { mutableStateOf(settings.shopName) }
    var phone by remember { mutableStateOf(settings.phoneNumber) }
    var whatsapp by remember { mutableStateOf(settings.whatsappNumber) }
    var email by remember { mutableStateOf(settings.email) }
    var address by remember { mutableStateOf(settings.address) }
    var pincode by remember { mutableStateOf(settings.pincode) }
    var landmark by remember { mutableStateOf(settings.landmark) }
    var openTime by remember { mutableStateOf(settings.openingTime) }
    var closeTime by remember { mutableStateOf(settings.closingTime) }
    var openingHours by remember { mutableStateOf(settings.openingHours) }
    var googleMapsUrl by remember { mutableStateOf(settings.googleMapsUrl) }
    var servicesText by remember { mutableStateOf(settings.services.joinToString("\n")) }

    var pickupAvailable by remember { mutableStateOf(settings.pickupAvailable) }
    var homeDeliveryAvailable by remember { mutableStateOf(settings.homeDeliveryAvailable) }
    var deliveryChargeText by remember { mutableStateOf(settings.deliveryCharge.toString()) }
    var freeThresholdText by remember { mutableStateOf(settings.freeDeliveryThreshold.toString()) }

    var upiEnabled by remember { mutableStateOf(settings.upiEnabled) }
    var codEnabled by remember { mutableStateOf(settings.codEnabled) }
    var onlinePaymentEnabled by remember { mutableStateOf(settings.onlinePaymentEnabled) }

    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(settings) {
        shopName = settings.shopName
        phone = settings.phoneNumber
        whatsapp = settings.whatsappNumber
        email = settings.email
        address = settings.address
        pincode = settings.pincode
        landmark = settings.landmark
        openTime = settings.openingTime
        closeTime = settings.closingTime
        openingHours = settings.openingHours
        googleMapsUrl = settings.googleMapsUrl
        servicesText = settings.services.joinToString("\n")
        pickupAvailable = settings.pickupAvailable
        homeDeliveryAvailable = settings.homeDeliveryAvailable
        deliveryChargeText = settings.deliveryCharge.toString()
        freeThresholdText = settings.freeDeliveryThreshold.toString()
        upiEnabled = settings.upiEnabled
        codEnabled = settings.codEnabled
        onlinePaymentEnabled = settings.onlinePaymentEnabled
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_settings_screen"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Section 1: Shop Owner & Contact Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shop Information Management",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )
                    Text(
                        text = "Syncs live to Home footer, Call Shop, WhatsApp, Header & Contact screens",
                        fontSize = 11.sp,
                        color = LightTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it; validationError = null },
                        label = { Text("Shop Name *") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_shop_name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; validationError = null },
                            label = { Text("Calling Mobile (Call Shop) *") },
                            colors = lightOutlinedTextFieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f).testTag("settings_phone"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it; validationError = null },
                            label = { Text("WhatsApp Number *") },
                            colors = lightOutlinedTextFieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f).testTag("settings_whatsapp"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; validationError = null },
                        label = { Text("Shop Email") },
                        colors = lightOutlinedTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("settings_email"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it; validationError = null },
                        label = { Text("Full Physical Address *") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_address"),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it.filter { ch -> ch.isDigit() }.take(6); validationError = null },
                            label = { Text("Pincode *") },
                            colors = lightOutlinedTextFieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.4f).testTag("settings_pincode"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = landmark,
                            onValueChange = { landmark = it },
                            label = { Text("Landmark") },
                            colors = lightOutlinedTextFieldColors(),
                            modifier = Modifier.weight(0.6f).testTag("settings_landmark"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = { openTime = it },
                            label = { Text("Opening Time") },
                            colors = lightOutlinedTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = { closeTime = it },
                            label = { Text("Closing Time") },
                            colors = lightOutlinedTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = openingHours,
                        onValueChange = { openingHours = it },
                        label = { Text("Opening Hours Display Label") },
                        placeholder = { Text("e.g. 9:30 AM – 9:30 PM (Open All 7 Days)") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_opening_hours"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = googleMapsUrl,
                        onValueChange = { googleMapsUrl = it },
                        label = { Text("Google Maps URL") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_maps_url"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = servicesText,
                        onValueChange = { servicesText = it },
                        label = { Text("Business Services (one per line)") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("settings_services"),
                        minLines = 4,
                        maxLines = 8
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 2: Delivery Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Delivery & Pickup Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Store Pickup Available", fontSize = 13.sp, color = LightTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = pickupAvailable, onCheckedChange = { pickupAvailable = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Doorstep Home Delivery", fontSize = 13.sp, color = LightTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = homeDeliveryAvailable, onCheckedChange = { homeDeliveryAvailable = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deliveryChargeText,
                            onValueChange = { deliveryChargeText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Delivery Charge (₹)") },
                            colors = lightOutlinedTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = freeThresholdText,
                            onValueChange = { freeThresholdText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Free Delivery Over (₹)") },
                            colors = lightOutlinedTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 3: Payment Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Payment Gateways & Modes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("UPI & QR Payments Enabled", fontSize = 13.sp, color = LightTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = upiEnabled, onCheckedChange = { upiEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cash on Delivery (COD) Enabled", fontSize = 13.sp, color = LightTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = codEnabled, onCheckedChange = { codEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cards & Net Banking Enabled", fontSize = 13.sp, color = LightTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = onlinePaymentEnabled, onCheckedChange = { onlinePaymentEnabled = it })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Validation & Save Button
        item {
            if (!validationError.isNullOrBlank()) {
                Text(
                    text = validationError!!,
                    color = ColorError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    val cleanName = shopName.trim()
                    val cleanPhone = phone.trim()
                    val cleanWhatsapp = whatsapp.trim().ifBlank { cleanPhone }
                    val cleanAddress = address.trim()
                    val cleanPin = pincode.trim()

                    when {
                        cleanName.isBlank() -> validationError = "Shop Name cannot be empty."
                        cleanPhone.filter { it.isDigit() }.length < 10 -> validationError = "Enter a valid 10+ digit Calling Mobile Number."
                        cleanWhatsapp.filter { it.isDigit() }.length < 10 -> validationError = "Enter a valid 10+ digit WhatsApp Number."
                        cleanAddress.isBlank() -> validationError = "Shop Address cannot be empty."
                        else -> {
                            validationError = null
                            val parsedServices = servicesText
                                .lines()
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .ifEmpty { ShopSettings.DEFAULT_SERVICES }
                            val updatedHours = openingHours.trim().ifBlank {
                                "${openTime.trim()} – ${closeTime.trim()} (Open All 7 Days)"
                            }
                            val updated = settings.copy(
                                shopName = cleanName,
                                phoneNumber = cleanPhone,
                                whatsappNumber = cleanWhatsapp,
                                email = email.trim(),
                                address = cleanAddress,
                                pincode = cleanPin,
                                landmark = landmark.trim(),
                                openingTime = openTime.trim().ifBlank { "09:30 AM" },
                                closingTime = closeTime.trim().ifBlank { "09:30 PM" },
                                openingHours = updatedHours,
                                googleMapsUrl = googleMapsUrl.trim(),
                                services = parsedServices,
                                pickupAvailable = pickupAvailable,
                                homeDeliveryAvailable = homeDeliveryAvailable,
                                deliveryCharge = deliveryChargeText.toIntOrNull() ?: 49,
                                freeDeliveryThreshold = freeThresholdText.toIntOrNull() ?: 499,
                                upiEnabled = upiEnabled,
                                codEnabled = codEnabled,
                                onlinePaymentEnabled = onlinePaymentEnabled
                            )
                            onSaveSettings(updated)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color.White)
            ) {
                Text("Save & Sync Shop Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

