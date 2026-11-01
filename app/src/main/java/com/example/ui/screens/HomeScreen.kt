package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CustomerReview
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.RepairService
import com.example.ui.theme.*
import com.example.ui.components.CategoryData
import com.example.ui.components.ProductCard
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopAccentAmber
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.ui.theme.ShopSuccessGreen
import com.example.ui.theme.ShopTextMuted
import com.example.ui.theme.ShopTextPrimary
import com.example.ui.theme.ShopTextSecondary
import com.example.viewmodel.AppScreen

@Composable
fun HomeScreen(
    featuredProducts: List<Product>,
    repairServices: List<RepairService>,
    customerReviews: List<CustomerReview>,
    categories: List<com.example.admin.model.CategoryRecord> = emptyList(),
    onSelectProduct: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onBuyNow: (Product) -> Unit,
    onCategoryClick: (ProductCategory) -> Unit,
    onCategoryRecordClick: ((com.example.admin.model.CategoryRecord) -> Unit)? = null,
    onNavigate: (AppScreen) -> Unit,
    onBookRepairProblem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BlackBackground)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Promotional Banner
        item {
            PromoHeroBanner(
                onShopNow = { onNavigate(AppScreen.PRODUCTS) },
                onBookRepair = { onNavigate(AppScreen.REPAIR_BOOKING) }
            )
        }

        // 2. Categories Horizontal Scroll
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shop by Category",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )

                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShopPrimaryBlue,
                        modifier = Modifier
                            .clickable { onNavigate(AppScreen.PRODUCTS) }
                            .testTag("view_all_categories_btn")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (categories.isNotEmpty()) {
                        items(categories) { cat ->
                            com.example.ui.components.DynamicCategoryCardItem(
                                category = cat,
                                onClick = {
                                    if (onCategoryRecordClick != null) {
                                        onCategoryRecordClick(cat)
                                    } else {
                                        onCategoryClick(cat.category)
                                    }
                                }
                            )
                        }
                    } else {
                        items(CategoryData.items) { meta ->
                            com.example.ui.components.CategoryCardItem(
                                categoryMeta = meta,
                                onClick = { onCategoryClick(meta.category) }
                            )
                        }
                    }
                }
            }
        }

        // SK Reels Showcase Banner
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                color = BlackSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigate(AppScreen.REELS) }
                    .testTag("home_reels_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Watch Reels",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GoldHighlight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "NEW",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SK Reels & Video Showcase",
                                color = TextMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Swipe through unboxings, repair tutorials & smartphone camera tests!",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Reels",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Featured Mobile Phones & Deals Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Trending Mobiles & Best Deals",
                subtitle = "Latest 5G smartphones with official brand warranty",
                onSeeAll = { onNavigate(AppScreen.PRODUCTS) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val mobileProducts = featuredProducts.filter { it.category == ProductCategory.MOBILE_PHONES }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mobileProducts) { product ->
                    Box(modifier = Modifier.width(220.dp)) {
                        ProductCard(
                            product = product,
                            onClick = { onSelectProduct(product) },
                            onAddToCart = { onAddToCart(product) },
                            onBuyNow = { onBuyNow(product) }
                        )
                    }
                }
            }
        }

        // 3.5 Best Sellers Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Best Sellers at SK Mobile Shoppy and Electronics",
                subtitle = "Top customer favorites and hot sellers in Udgir",
                onSeeAll = { onNavigate(AppScreen.PRODUCTS) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val bestSellers = featuredProducts.filter {
                it.isBestSeller ||
                it.highlightBadge == "Best Seller" ||
                it.highlightBadge == "Top Rated" ||
                it.highlightBadge == "Customer Favorite"
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bestSellers) { product ->
                    Box(modifier = Modifier.width(210.dp)) {
                        ProductCard(
                            product = product,
                            onClick = { onSelectProduct(product) },
                            onAddToCart = { onAddToCart(product) },
                            onBuyNow = { onBuyNow(product) }
                        )
                    }
                }
            }
        }

        // 4. Dedicated "Need Mobile Repair?" Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            RepairSectionCard(
                services = repairServices,
                onServiceClick = { problemTitle -> onBookRepairProblem(problemTitle) },
                onBookRepairFull = { onNavigate(AppScreen.REPAIR_BOOKING) }
            )
        }

        // 5. Dedicated "Buy & Sell Old Mobiles" Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            OldMobilesBannerSection(
                onBuyOld = { onNavigate(AppScreen.OLD_PHONE_BUY) },
                onSellOld = { onNavigate(AppScreen.SELL_OLD_PHONE) }
            )
        }

        // 6. Audio, Chargers & Accessories Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Popular Accessories & Chargers",
                subtitle = "Fast chargers, cables, earbuds & protection",
                onSeeAll = { onNavigate(AppScreen.PRODUCTS) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val accessoryProducts = featuredProducts.filter {
                it.category == ProductCategory.CHARGERS ||
                it.category == ProductCategory.DATA_CABLES ||
                it.category == ProductCategory.BLUETOOTH ||
                it.category == ProductCategory.HEADPHONES ||
                it.category == ProductCategory.ACCESSORIES
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accessoryProducts) { product ->
                    Box(modifier = Modifier.width(200.dp)) {
                        ProductCard(
                            product = product,
                            onClick = { onSelectProduct(product) },
                            onAddToCart = { onAddToCart(product) },
                            onBuyNow = { onBuyNow(product) }
                        )
                    }
                }
            }
        }

        // 7. Why Choose SK Mobile Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            WhyChooseUsSection()
        }

        // 8. Customer Reviews Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            CustomerReviewsSection(reviews = customerReviews)
        }

        // 9. Comprehensive Footer
        item {
            Spacer(modifier = Modifier.height(24.dp))
            ShopFooterSection(
                onNavigate = onNavigate,
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                    context.startActivity(intent)
                },
                onWhatsApp = {
                    val url = "https://api.whatsapp.com/send?phone=919876543210&text=Hello%20SK%20Mobile%20Udgir%2C%20I%20have%20an%20inquiry"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun PromoHeroBanner(
    onShopNow: () -> Unit,
    onBookRepair: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("promo_hero_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.GoldPrimary.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF221E14), com.example.ui.theme.BlackBackground)
                    )
                )
        ) {
            // Background Artwork
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner_1791267089819),
                contentDescription = "SK Mobile Shoppy and Electronics Banner Artwork",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.30f
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Surface(
                    color = ShopAccentAmber,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "UDGIR'S #1 TRUSTED STORE",
                        color = Color(0xFF0B0B0B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mobile Repair • Accessories\nOld Phone Buy & Sell",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 25.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Fast doorstep pickup & same-day delivery across Udgir (413517)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onShopNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShopPrimaryBlue,
                            contentColor = Color(0xFF0B0B0B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("hero_shop_now_button")
                    ) {
                        Text(
                            text = "Shop Now",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B0B0B),
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onBookRepair,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShopPrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ShopPrimaryBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("hero_book_repair_button")
                    ) {
                        Text(
                            text = "Book Repair",
                            fontWeight = FontWeight.Bold,
                            color = ShopPrimaryBlue,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ShopTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ShopTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "See All",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShopPrimaryBlue,
            modifier = Modifier.clickable(onClick = onSeeAll)
        )
    }
}

@Composable
fun RepairSectionCard(
    services: List<RepairService>,
    onServiceClick: (String) -> Unit,
    onBookRepairFull: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("need_mobile_repair_section"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Repair",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Need Mobile Repair?",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )
                        Text(
                            text = "Quick diagnosis & 90-day service warranty",
                            fontSize = 12.sp,
                            color = ShopTextSecondary
                        )
                    }
                }

                Button(
                    onClick = onBookRepairFull,
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("book_repair_quick_btn")
                ) {
                    Text(text = "Book Repair", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6 Repair problem cards in grid layout (2 rows x 3 columns)
            val chunked = services.chunked(3)
            chunked.forEach { rowServices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowServices.forEach { service ->
                        RepairGridItem(
                            service = service,
                            onClick = { onServiceClick(service.title) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Doorstep pickup note
            Surface(
                color = Color(0xFF14241B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F432B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = "Delivery",
                        tint = ShopSuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Free home pickup & drop available across Udgir city",
                        fontSize = 12.sp,
                        color = Color(0xFF4ADE80),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RepairGridItem(
    service: RepairService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (service.iconType) {
        "display" -> Icons.Default.PhoneAndroid
        "battery" -> Icons.Default.BatteryAlert
        "charging" -> Icons.Default.Power
        "audio" -> Icons.Default.GraphicEq
        "software" -> Icons.Default.Smartphone
        "water" -> Icons.Default.WaterDrop
        else -> Icons.Default.Build
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("repair_service_card_${service.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(com.example.ui.theme.BlackSurfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = service.title,
                    tint = ShopPrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = service.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = ShopTextPrimary,
                maxLines = 2,
                lineHeight = 13.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "From ${formatInr(service.estimatedStartingPrice)}",
                fontSize = 10.sp,
                color = ShopPrimaryBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OldMobilesBannerSection(
    onBuyOld: () -> Unit,
    onSellOld: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("buy_sell_old_mobiles_section"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFEA580C),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "UDGIR SPECIAL",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buy & Sell Old Mobiles",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upgrade your phone with maximum exchange value or choose certified pre-owned phones with 6 months shop warranty.",
                color = com.example.ui.theme.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onBuyOld,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("buy_old_phone_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color(0xFF0B0B0B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Buy Old Phone", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B0B0B))
                }

                Button(
                    onClick = onSellOld,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sell_your_phone_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sell Your Phone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WhyChooseUsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("why_choose_sk_mobile_section")
    ) {
        Text(
            text = "Why Choose SK Mobile Shoppy and Electronics?",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = ShopTextPrimary
        )
        Text(
            text = "Nagar Parishad, Udgir's most trusted smartphone destination",
            fontSize = 12.sp,
            color = ShopTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        val pillars = listOf(
            Triple(Icons.Default.Security, "Trusted Service", "10+ years serving thousands of happy mobile customers across Udgir & Latur region."),
            Triple(Icons.Default.ThumbUp, "Genuine Products", "100% authentic mobile accessories with brand warranty and guaranteed durability."),
            Triple(Icons.Default.Build, "Expert Technicians", "Chip-level motherboard & display specialists for Apple, Samsung, OnePlus & Android."),
            Triple(Icons.Default.LocalShipping, "Home Pickup & Delivery", "Doorstep repair pickup and delivery anywhere in Udgir municipal area."),
            Triple(Icons.Default.Check, "Easy Booking & Fair Prices", "Transparent repair estimates without hidden costs or sudden price hikes.")
        )

        pillars.forEach { (icon, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BlackSurfaceSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = ShopPrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = ShopTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerReviewsSection(reviews: List<CustomerReview>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("customer_reviews_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Customer Reviews",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextPrimary
                )
                Text(
                    text = "What Udgir locals say about SK Mobile Shoppy and Electronics",
                    fontSize = 12.sp,
                    color = ShopTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = ShopAccentAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "4.9 / 5.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ShopTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reviews) { review ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .testTag("review_card_${review.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = review.customerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ShopTextPrimary
                            )

                            Row {
                                repeat(review.rating) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = ShopAccentAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${review.location} • ${review.date}",
                            fontSize = 10.sp,
                            color = ShopTextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "\"${review.comment}\"",
                            fontSize = 11.sp,
                            color = com.example.ui.theme.TextSecondary,
                            lineHeight = 15.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = com.example.ui.theme.BlackSurfaceSecondary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Service: ${review.serviceOrProduct}",
                                fontSize = 9.sp,
                                color = ShopPrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopFooterSection(
    onNavigate: (AppScreen) -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Surface(
        color = com.example.ui.theme.BlackSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_footer_section")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Brand & Location
            Text(
                text = "SK Mobile Shoppy and Electronics",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.ui.theme.TextMain
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = ShopAccentAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nagar Parishad, Udgir, Maharashtra - 413517",
                    fontSize = 12.sp,
                    color = com.example.ui.theme.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Opening Hours: 9:30 AM – 9:30 PM (All 7 Days Open)",
                fontSize = 11.sp,
                color = com.example.ui.theme.TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Call & WhatsApp Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color(0xFF0B0B0B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("footer_call_btn")
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF0B0B0B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Call Shop", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B0B0B))
                }

                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("footer_whatsapp_btn")
                ) {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = com.example.ui.theme.BlackBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Quick Links
            Text(
                text = "Business Services:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            val services = listOf(
                "Mobile Repair" to AppScreen.REPAIR_BOOKING,
                "Old Mobile Buy/Sell" to AppScreen.OLD_PHONE_BUY,
                "Accessories & Chargers" to AppScreen.PRODUCTS,
                "Contact & Directions" to AppScreen.CONTACT
            )

            services.forEach { (label, screen) ->
                Text(
                    text = "• $label",
                    fontSize = 12.sp,
                    color = com.example.ui.theme.TextSecondary,
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .clickable { onNavigate(screen) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = com.example.ui.theme.BlackBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Legal & Copyright
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terms & Conditions",
                    fontSize = 11.sp,
                    color = com.example.ui.theme.TextMuted
                )
                Text(
                    text = "Privacy Policy",
                    fontSize = 11.sp,
                    color = com.example.ui.theme.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "© 2026 SK Mobile Shoppy and Electronics, Udgir. All Rights Reserved. Designed for Udgir mobile shoppers.",
                fontSize = 10.sp,
                color = com.example.ui.theme.TextMuted
            )
        }
    }
}
