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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.model.ShopSettings
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminSettingsScreen(
    settings: ShopSettings,
    onSaveSettings: (ShopSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var shopName by remember { mutableStateOf(settings.shopName) }
    var address by remember { mutableStateOf(settings.address) }
    var phone by remember { mutableStateOf(settings.phoneNumber) }
    var whatsapp by remember { mutableStateOf(settings.whatsappNumber) }
    var openTime by remember { mutableStateOf(settings.openingTime) }
    var closeTime by remember { mutableStateOf(settings.closingTime) }

    var pickupAvailable by remember { mutableStateOf(settings.pickupAvailable) }
    var homeDeliveryAvailable by remember { mutableStateOf(settings.homeDeliveryAvailable) }
    var deliveryChargeText by remember { mutableStateOf(settings.deliveryCharge.toString()) }

    var upiEnabled by remember { mutableStateOf(settings.upiEnabled) }
    var codEnabled by remember { mutableStateOf(settings.codEnabled) }
    var onlinePaymentEnabled by remember { mutableStateOf(settings.onlinePaymentEnabled) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_settings_screen"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Section 1: Shop Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Shop Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop Name") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_shop_name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Physical Store Address") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_address"),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.weight(1f).testTag("settings_phone"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp Number") },
                            modifier = Modifier.weight(1f).testTag("settings_whatsapp"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = { openTime = it },
                            label = { Text("Opening Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = { closeTime = it },
                            label = { Text("Closing Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
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
                    Text(text = "Delivery & Pickup Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Store Pickup Available", fontSize = 13.sp)
                        Switch(checked = pickupAvailable, onCheckedChange = { pickupAvailable = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Doorstep Home Delivery (Udgir)", fontSize = 13.sp)
                        Switch(checked = homeDeliveryAvailable, onCheckedChange = { homeDeliveryAvailable = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = deliveryChargeText,
                        onValueChange = { deliveryChargeText = it },
                        label = { Text("Standard Delivery Charge (₹) [Free on ₹499+]") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
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
                    Text(text = "Payment Gateways & Modes (Demo)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("UPI & QR Payments Enabled", fontSize = 13.sp)
                        Switch(checked = upiEnabled, onCheckedChange = { upiEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cash on Delivery (COD) Enabled", fontSize = 13.sp)
                        Switch(checked = codEnabled, onCheckedChange = { codEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cards & Net Banking Enabled", fontSize = 13.sp)
                        Switch(checked = onlinePaymentEnabled, onCheckedChange = { onlinePaymentEnabled = it })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = settings.copy(
                        shopName = shopName,
                        address = address,
                        phoneNumber = phone,
                        whatsappNumber = whatsapp,
                        openingTime = openTime,
                        closingTime = closeTime,
                        pickupAvailable = pickupAvailable,
                        homeDeliveryAvailable = homeDeliveryAvailable,
                        deliveryCharge = deliveryChargeText.toIntOrNull() ?: 49,
                        upiEnabled = upiEnabled,
                        codEnabled = codEnabled,
                        onlinePaymentEnabled = onlinePaymentEnabled
                    )
                    onSaveSettings(updated)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
            ) {
                Text("Save Store Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
