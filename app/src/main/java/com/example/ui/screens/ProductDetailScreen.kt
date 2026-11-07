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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.ProductCard
import com.example.ui.components.ProductVisualBox
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopAccentAmber
import com.example.ui.theme.ShopDiscountBadgeBg
import com.example.ui.theme.ShopDiscountGreen
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.ui.theme.ShopSuccessGreen
import com.example.ui.theme.ShopTextMuted
import com.example.ui.theme.ShopTextPrimary
import com.example.ui.theme.ShopTextSecondary

fun getCategoryAwareDefaultSpecs(categorySlug: String?, brand: String): Map<String, String> {
    val cat = (categorySlug ?: "").lowercase().replace("_", "-")
    return when {
        cat.contains("charger") -> mapOf(
            "Charging Power" to "65W Max Power Delivery (GaN III)",
            "Input" to "100-240V ~ 50/60Hz 1.5A Universal",
            "Output Ports" to "1x Type-C (65W) + 1x USB-A (18W)",
            "Fast Protocols" to "Supports VOOC, Warp, QC 4.0, PD 3.0 & PPS",
            "Compatibility" to "Universal for Android, iPhone, Laptops & Tablets",
            "Safety Cut-off" to "Over-voltage, Over-heating & Short-circuit Protection"
        )
        cat.contains("cable") || cat.contains("wire") -> mapOf(
            "Length" to "1.5 Meters (Extra Long)",
            "Connector Type" to "Type-C to Type-C / Fast Sync",
            "Charging Speed" to "100W (20V/5A) Power Delivery",
            "Data Transfer" to "480 Mbps High Speed Sync",
            "Build Material" to "Double Ballistic Nylon Braided",
            "Bend Lifespan" to "30,000+ Bends Lab Tested"
        )
        cat.contains("audio") || cat.contains("headphone") || cat.contains("earphone") || cat.contains("neckband") -> mapOf(
            "Type" to "Wireless In-Ear Neckband / Bluetooth Headset",
            "Driver Size" to "12.4mm Deep Bass Dynamic Drivers",
            "Connectivity" to "Bluetooth v5.3 (10m Range)",
            "Battery Life" to "Up to 30 Hours Playtime",
            "Fast Charge" to "10 mins charge = 20 hours music",
            "Microphone" to "Dual Mic AI Noise Cancellation",
            "Water Resistance" to "IP55 Sweat & Splash Proof"
        )
        cat.contains("speaker") -> mapOf(
            "Output Power" to "16W RMS High-Fidelity Audio",
            "Playtime" to "12 Hours Continuous Music",
            "Battery Capacity" to "2600 mAh Built-in Rechargeable",
            "Bluetooth Version" to "Bluetooth 5.3 + AUX Support",
            "Waterproof Rating" to "IPX7 Waterproof Protection"
        )
        cat.contains("power-bank") || cat.contains("powerbank") || cat.contains("battery") -> mapOf(
            "Battery Capacity" to "20,000 mAh Li-Polymer",
            "Output Power" to "22.5W Fast Charging (QC + PD)",
            "Input Ports" to "Type-C + Micro-USB Dual Input",
            "Output Ports" to "2x USB-A + 1x Type-C Fast Output",
            "Protection" to "12-Layer Advanced Smart Circuit Chip"
        )
        cat.contains("cover") || cat.contains("case") -> mapOf(
            "Material" to "Military-Grade TPU + Acrylic Hybrid",
            "Finish" to "Matte Frosted Anti-Fingerprint Grip",
            "Shock Protection" to "Air-Cushion Impact Absorbing Corners",
            "Raised Edges" to "1.5mm Screen Lip & 2.0mm Camera Ring",
            "Wireless Charging" to "100% Compatible with Qi & MagSafe Chargers"
        )
        cat.contains("screen") || cat.contains("guard") || cat.contains("protector") || cat.contains("glass") -> mapOf(
            "Hardness" to "9H Hardness Shatterproof Tempered Glass",
            "Glass Type" to "0.33mm 2.5D Curved Edge-to-Edge",
            "Coating" to "Oleophobic Anti-Fingerprint Coating",
            "Clarity" to "99.9% Ultra HD Transparency & True Touch",
            "Installation" to "Bubble-Free Auto-Adhesive Alignment"
        )
        cat.contains("old") || cat.contains("pre-owned") -> mapOf(
            "Condition Grade" to "SK Certified Grade A+ (Like New)",
            "Quality Check" to "32-Point Rigorous Hardware Diagnosis",
            "Battery Health" to "Tested ≥ 90% Original Capacity",
            "Included In Box" to "Device + Fast Charging Cable + Store Warranty Slip",
            "IMEI Verification" to "100% Clean Verified IMEI with GST Billing"
        )
        else -> mapOf(
            "Display" to "6.7\" FHD+ 120Hz Fluid AMOLED",
            "Processor" to "High-Efficiency Multi-Core Chipset",
            "Camera" to "Ultra-Clear High-Resolution Sensor with OIS",
            "Battery & Charging" to "5000+ mAh with Rapid Super Fast Charge",
            "Operating System" to "Latest Android with Official Updates",
            "Connectivity" to "5G Dual SIM, Wi-Fi 6, Bluetooth 5.3"
        )
    }
}

@Composable
fun ProductDetailScreen(
    product: Product,
    relatedProducts: List<Product>,
    onAddToCart: (Product) -> Unit,
    onBuyNow: (Product) -> Unit,
    onSelectRelatedProduct: (Product) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    val galleryAngles = listOf("Front View", "Back View", "Side Angle", "Package Box")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FA))
            .testTag("product_detail_screen")
    ) {
        // Top Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ShopTextPrimary
                    )
                }

                Text(
                    text = product.brand,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ShopTextPrimary
                )

                Box(modifier = Modifier.size(48.dp)) // balance layout
            }
        }

        // Scrollable Body
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Main Product Gallery View
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProductVisualBox(
                            product = product,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gallery thumbnails
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            galleryAngles.forEachIndexed { index, angle ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedImageIndex = index }
                                        .border(
                                            width = if (selectedImageIndex == index) 2.dp else 1.dp,
                                            color = if (selectedImageIndex == index) ShopPrimaryBlue else ShopOutlineLight,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedImageIndex == index) ShopPrimaryLightBlue else Color(0xFFF8FAFC)
                                ) {
                                    Text(
                                        text = angle,
                                        fontSize = 9.sp,
                                        fontWeight = if (selectedImageIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedImageIndex == index) ShopPrimaryBlue else ShopTextSecondary,
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Title, Rating & Pricing Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Brand and Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.brand.uppercase(),
                                color = ShopPrimaryBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            if (product.highlightBadge != null) {
                                Surface(
                                    color = ShopPrimaryLightBlue,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = product.highlightBadge,
                                        color = ShopPrimaryBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Name
                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary,
                            lineHeight = 23.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = ShopSuccessGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${product.rating}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${product.reviewCount} Ratings & Reviews",
                                fontSize = 12.sp,
                                color = ShopTextSecondary
                            )
                        }

                        // Category & Quality Badges
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Category: ${product.category.displayName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            val qualityLabel = when {
                                product.conditionGrade != null -> product.conditionGrade
                                product.isPreOwned -> "SK Certified Pre-Owned"
                                product.category == ProductCategory.CHARGERS || product.category == ProductCategory.DATA_CABLES -> "100% Original OEM"
                                else -> "100% Original Brand New"
                            }
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = qualityLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ShopOutlineLight)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Price Row
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatInr(product.price),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = ShopTextPrimary
                            )

                            if (product.originalPrice > product.price) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatInr(product.originalPrice),
                                    fontSize = 15.sp,
                                    color = ShopTextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = ShopDiscountBadgeBg,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${product.discountPercent}% OFF",
                                        color = ShopDiscountGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (product.savingsAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You save ${formatInr(product.savingsAmount)} on this order!",
                                fontSize = 12.sp,
                                color = ShopDiscountGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Availability / Stock Status
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Availability: ",
                                fontSize = 12.sp,
                                color = ShopTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                color = if (product.inStock) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (product.inStock) ShopSuccessGreen else Color(0xFFEF4444))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = product.stockStatus,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (product.inStock) Color(0xFF166534) else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pre-owned condition badge card if applicable
            if (product.isPreOwned) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFED7AA), Color(0xFFFED7AA)))),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Pre-Owned",
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SK Certified Pre-Owned Assurance",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9A3412)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "• Condition: ${product.conditionGrade ?: "Grade A"}",
                                fontSize = 12.sp,
                                color = Color(0xFF7C2D12)
                            )
                            if (product.batteryHealth != null) {
                                Text(
                                    text = "• Tested Battery Health: ${product.batteryHealth}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7C2D12)
                                )
                            }
                            Text(
                                text = "• Warranty: ${product.warranty}",
                                fontSize = 12.sp,
                                color = Color(0xFF7C2D12)
                            )
                        }
                    }
                }
            }

            // Local Udgir Delivery Assurance Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "Delivery",
                            tint = ShopSuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Fast Doorstep Delivery in Udgir (413517)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Same-day delivery or pickup at Nagar Parishad store",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            // Description
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Product Overview",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = ShopTextSecondary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // Specifications Table
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Technical Specifications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val displaySpecs = if (product.specifications.isNotEmpty()) {
                            product.specifications
                        } else {
                            getCategoryAwareDefaultSpecs(product.categorySlug, product.brand)
                        }

                        displaySpecs.entries.forEachIndexed { index, (key, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (index % 2 == 0) Color(0xFFF8FAFC) else Color.White)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 12.sp,
                                    color = ShopTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = value,
                                    fontSize = 12.sp,
                                    color = ShopTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        }
                    }
                }
            }

            // Warranty & Replacement Policy Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Warranty",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Warranty",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.warranty,
                                fontSize = 11.sp,
                                color = Color(0xFF78350F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Return Policy",
                                    tint = ShopSuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "7-Day Return",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hassle-free replacement at Udgir counter",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Customer Reviews Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customer Reviews & Ratings",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopTextPrimary
                            )
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${product.rating} ★",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        listOf(
                            Triple("Rahul K. (Udgir)", "5.0 ★", "100% genuine product. Bought directly from Udgir shop, verified bill and warranty!"),
                            Triple("Sachin Patil", "5.0 ★", "Best pricing in town. Original seal packed with courteous service."),
                            Triple("Pooja Deshmukh", "4.5 ★", "Works great! Exactly as described with same day delivery in Udgir.")
                        ).forEach { (author, stars, comment) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = author, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ShopTextPrimary)
                                    Text(text = stars, fontSize = 10.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = comment, fontSize = 11.sp, color = ShopTextSecondary)
                            }
                        }
                    }
                }
            }

            // Related Products
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Related Products",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val filteredRelated = relatedProducts.filter { it.id != product.id }.take(5)
                    items(filteredRelated) { item ->
                        Box(modifier = Modifier.width(180.dp)) {
                            ProductCard(
                                product = item,
                                onClick = { onSelectRelatedProduct(item) },
                                onAddToCart = { onAddToCart(item) },
                                onBuyNow = { onBuyNow(item) }
                            )
                        }
                    }
                }
            }
        }

        // Sticky Bottom Bar: Add to Cart and Buy Now
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddToCart(product) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_add_to_cart_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopPrimaryBlue)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Add to Cart", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Cart", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { onBuyNow(product) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_buy_now_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
                ) {
                    Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
