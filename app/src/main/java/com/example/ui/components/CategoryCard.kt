package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProductCategory
import com.example.ui.theme.ShopPrimaryBlue

data class CategoryMeta(
    val category: ProductCategory,
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val iconTint: Color
)

object CategoryData {
    val items = listOf(
        CategoryMeta(
            category = ProductCategory.MOBILE_PHONES,
            title = "Mobile Phones",
            icon = Icons.Default.PhoneAndroid,
            gradientColors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
            iconTint = Color(0xFF0284C7)
        ),
        CategoryMeta(
            category = ProductCategory.OLD_PHONES,
            title = "Old Phones",
            icon = Icons.Default.SwapHoriz,
            gradientColors = listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)),
            iconTint = Color(0xFFEA580C)
        ),
        CategoryMeta(
            category = ProductCategory.REPAIR_SERVICES,
            title = "Mobile Repair",
            icon = Icons.Default.Build,
            gradientColors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)),
            iconTint = Color(0xFFD97706)
        ),
        CategoryMeta(
            category = ProductCategory.CHARGERS,
            title = "Chargers",
            icon = Icons.Default.Power,
            gradientColors = listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)),
            iconTint = Color(0xFF16A34A)
        ),
        CategoryMeta(
            category = ProductCategory.DATA_CABLES,
            title = "Data Cables",
            icon = Icons.Default.Cable,
            gradientColors = listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE)),
            iconTint = Color(0xFF7C3AED)
        ),
        CategoryMeta(
            category = ProductCategory.HEADPHONES,
            title = "Headphones",
            icon = Icons.Default.Headphones,
            gradientColors = listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8)),
            iconTint = Color(0xFFDB2777)
        ),
        CategoryMeta(
            category = ProductCategory.BLUETOOTH,
            title = "Bluetooth",
            icon = Icons.Default.VolumeUp,
            gradientColors = listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE)),
            iconTint = Color(0xFF4F46E5)
        ),
        CategoryMeta(
            category = ProductCategory.ACCESSORIES,
            title = "Accessories",
            icon = Icons.Default.Watch,
            gradientColors = listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4)),
            iconTint = Color(0xFF0D9488)
        )
    )
}

@Composable
fun CategoryCardItem(
    categoryMeta: CategoryMeta,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(82.dp)
            .clickable(onClick = onClick)
            .testTag("category_chip_${categoryMeta.category.name}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) com.example.ui.theme.BlackSurfaceSecondary else com.example.ui.theme.BlackSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.GoldPrimary) else androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BlackBorder)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(categoryMeta.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryMeta.icon,
                    contentDescription = categoryMeta.title,
                    tint = categoryMeta.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = categoryMeta.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) com.example.ui.theme.GoldHighlight else com.example.ui.theme.TextMain,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}
