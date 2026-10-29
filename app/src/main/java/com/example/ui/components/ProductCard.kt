package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.theme.ShopAccentAmber
import com.example.ui.theme.ShopDiscountBadgeBg
import com.example.ui.theme.ShopDiscountGreen
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.ui.theme.ShopTextMuted
import com.example.ui.theme.ShopTextPrimary
import com.example.ui.theme.ShopTextSecondary
import java.text.NumberFormat
import java.util.Locale

fun formatInr(amount: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount).replace(".00", "")
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BlackSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top Image / Visual Representation
            ProductVisualBox(
                product = product,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Brand & Highlight Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.brand.uppercase(),
                    color = ShopPrimaryBlue,
                    fontSize = 11.sp,
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
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product Name
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = ShopTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.height(36.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rating & Review Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFF16A34A),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.rating}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "(${product.reviewCount})",
                    fontSize = 11.sp,
                    color = ShopTextMuted
                )

                if (product.isPreOwned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Pre-Owned",
                        fontSize = 11.sp,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stock Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (product.inStock) com.example.ui.theme.ShopSuccessGreen else Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = product.stockStatus,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (product.inStock) Color(0xFF166534) else Color(0xFFB91C1C)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Pricing Row: Price, Strikethrough MRP, Discount %
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatInr(product.price),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ShopTextPrimary
                )

                if (product.originalPrice > product.price) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatInr(product.originalPrice),
                        fontSize = 12.sp,
                        color = ShopTextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = ShopDiscountBadgeBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = ShopDiscountGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Add to Cart and Buy Now
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("add_to_cart_btn_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShopPrimaryBlue),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ShopPrimaryBlue
                    )
                ) {
                    Text(
                        text = "Add to Cart",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onBuyNow,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("buy_now_btn_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShopPrimaryBlue,
                        contentColor = Color(0xFF0B0B0B)
                    )
                ) {
                    Text(
                        text = "Buy Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B0B0B),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ProductVisualBox(
    product: Product,
    modifier: Modifier = Modifier
) {
    val (gradientColors, iconVector) = getCategoryStyling(product.category)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = product.name,
                tint = ShopPrimaryBlue,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = product.brand,
                    color = ShopTextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Pre-owned badge corner if applicable
        if (product.isPreOwned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEA580C))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Refurbished",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getCategoryStyling(category: ProductCategory): Pair<List<Color>, ImageVector> {
    return when (category) {
        ProductCategory.MOBILE_PHONES, ProductCategory.OLD_PHONES -> Pair(
            listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
            Icons.Default.PhoneAndroid
        )
        ProductCategory.CHARGERS -> Pair(
            listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)),
            Icons.Default.Power
        )
        ProductCategory.DATA_CABLES -> Pair(
            listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)),
            Icons.Default.Cable
        )
        ProductCategory.HEADPHONES -> Pair(
            listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE)),
            Icons.Default.Headphones
        )
        ProductCategory.BLUETOOTH -> Pair(
            listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE)),
            Icons.Default.VolumeUp
        )
        ProductCategory.ACCESSORIES -> Pair(
            listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8)),
            Icons.Default.Watch
        )
        else -> Pair(
            listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
            Icons.Default.PhoneAndroid
        )
    }
}
