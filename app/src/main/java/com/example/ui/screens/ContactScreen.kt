package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShopAccentAmber
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.ui.theme.ShopSuccessGreen
import com.example.ui.theme.ShopTextPrimary
import com.example.ui.theme.ShopTextSecondary

@Composable
fun ContactScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FA))
            .testTag("contact_screen")
    ) {
        // Top App Bar
        Surface(
            color = Color.White,
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
                    text = "Contact SK Mobile",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Main Shop Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(ShopPrimaryLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = "Store",
                                    tint = ShopPrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SK Mobile",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ShopPrimaryBlue
                                )
                                Text(
                                    text = "Nagar Parishad, Udgir, Maharashtra - 413517",
                                    fontSize = 12.sp,
                                    color = ShopTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = ShopOutlineLight)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Timing
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Hours",
                                tint = ShopPrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Store Timings:",
                                    fontSize = 12.sp,
                                    color = ShopTextSecondary
                                )
                                Text(
                                    text = "9:30 AM – 9:30 PM (Open All 7 Days)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Phone",
                                tint = ShopPrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Phone & Customer Helpline:",
                                    fontSize = 12.sp,
                                    color = ShopTextSecondary
                                )
                                Text(
                                    text = "+91 9823109313",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = ShopPrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Official Customer Email:",
                                    fontSize = 12.sp,
                                    color = ShopTextSecondary
                                )
                                Text(
                                    text = "skmobileshoppyandelectronics@gmail.com",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919823109313"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).testTag("contact_call_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Now", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val url = "https://api.whatsapp.com/send?phone=919823109313&text=Hello%20SK%20Mobile%20Udgir"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).testTag("contact_whatsapp_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Location Map Visualizer Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map",
                                tint = ShopPrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How to Reach SK Mobile",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Simulated Map Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pin",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "SK Mobile • Nagar Parishad Road, Udgir",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopTextPrimary
                                )
                                Text(
                                    text = "Landmark: Opposite Nagar Parishad Municipal Complex",
                                    fontSize = 10.sp,
                                    color = ShopTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Business Services List
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Services Available at SK Mobile",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val allServices = listOf(
                            "Mobile repair (Display, Battery, Charging, Audio, Software)",
                            "New 5G mobile phones (All major brands)",
                            "Old mobile buy (Instant valuation & cash)",
                            "Old mobile sell (Certified refurbished with warranty)",
                            "Fast chargers & GaN power adapters",
                            "High-speed braided data cables",
                            "Wired & Bluetooth headphones",
                            "TWS earbuds & wireless Bluetooth speakers",
                            "Mobile accessories (Power banks, tempered glass, cases)",
                            "Home pickup and delivery across Udgir",
                            "Online repair booking with live tracking"
                        )

                        allServices.forEach { s ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Check",
                                    tint = ShopSuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = s, fontSize = 12.sp, color = ShopTextPrimary)
                            }
                        }
                    }
                }
            }

            // FAQ Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Frequently Asked Questions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val faqs = listOf(
                            "How long does a screen replacement take?" to "Most screen and battery replacements are completed in 30 to 45 minutes at our Udgir store.",
                            "Do you provide a warranty on repairs?" to "Yes, we provide up to 90 to 180 days replacement warranty on all replaced parts.",
                            "Is home pickup and drop free in Udgir?" to "Yes, we offer complimentary doorstep pickup and drop within the Udgir municipal area for repair bookings.",
                            "How is my old phone evaluated?" to "We check physical condition, battery health, and components using standard diagnostic tests to give the highest local market price."
                        )

                        faqs.forEach { (q, a) ->
                            Text(text = "Q: $q", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ShopTextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = a, fontSize = 12.sp, color = ShopTextSecondary, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}
