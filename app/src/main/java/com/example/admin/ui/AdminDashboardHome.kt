package com.example.admin.ui

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.model.AdminTab
import com.example.admin.viewmodel.AdminUiState
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminDashboardHome(
    uiState: AdminUiState,
    onNavigateTab: (AdminTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_dashboard_home"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Top Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SK Mobile Business Overview",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Nagar Parishad, Udgir Store • Real-time Operations",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Store Open",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Metric Cards in 2-Column Grid
        item {
            Text(
                text = "Key Business Metrics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val metrics = listOf(
                MetricItem("Total Orders", "${uiState.totalOrdersCount}", Icons.Default.ShoppingBag, Color(0xFF0284C7), Color(0xFFE0F2FE), AdminTab.ORDERS),
                MetricItem("Pending Orders", "${uiState.pendingOrdersCount}", Icons.Default.PendingActions, Color(0xFFEA580C), Color(0xFFFFEDD5), AdminTab.ORDERS),
                MetricItem("Repair Jobs", "${uiState.totalRepairsCount}", Icons.Default.Build, Color(0xFFD97706), Color(0xFFFEF3C7), AdminTab.REPAIRS),
                MetricItem("Completed Repairs", "${uiState.completedRepairsCount}", Icons.Default.CheckCircle, Color(0xFF16A34A), Color(0xFFDCFCE7), AdminTab.REPAIRS),
                MetricItem("Old Phones Purchased", "${uiState.oldPhonesPurchasedCount}", Icons.Default.PhoneAndroid, Color(0xFF7C3AED), Color(0xFFEDE9FE), AdminTab.OLD_PHONE_BUY),
                MetricItem("Pending Requests", "${uiState.oldPhonesPendingCount}", Icons.Default.PhoneAndroid, Color(0xFF8B5CF6), Color(0xFFF3E8FF), AdminTab.SELL_REQUESTS),
                MetricItem("Active Products", "${uiState.products.size}", Icons.Default.Inventory2, Color(0xFF0D9488), Color(0xFFCCFBF1), AdminTab.PRODUCTS),
                MetricItem("Registered Customers", "${uiState.totalCustomersCount}", Icons.Default.People, Color(0xFF6366F1), Color(0xFFEEF2FF), AdminTab.CUSTOMERS),
                MetricItem("Total Sales", formatInr(uiState.totalSalesAmount), Icons.Default.AttachMoney, Color(0xFF2563EB), Color(0xFFDBEAFE), AdminTab.REPORTS),
                MetricItem("Estimated Profit", formatInr(uiState.estimatedProfitAmount), Icons.Default.TrendingUp, Color(0xFF059669), Color(0xFFD1FAE5), AdminTab.REPORTS)
            )

            val chunkedMetrics = metrics.chunked(2)
            chunkedMetrics.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { metric ->
                        MetricCard(
                            metric = metric,
                            onClick = { onNavigateTab(metric.targetTab) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Visual Summary Charts Section
        item {
            Text(
                text = "Performance Charts & Visual Summaries",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Daily Sales Visual Bar Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Sales Trend (Last 7 Days)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Average daily revenue in Udgir store: ₹18,400",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val days = listOf("Mon" to 14200, "Tue" to 18900, "Wed" to 16400, "Thu" to 22100, "Fri" to 25800, "Sat" to 31200, "Sun" to 27900)
                    val maxVal = days.maxOf { it.second }.toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEach { (day, amount) ->
                            val heightRatio = amount / maxVal
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "₹${amount / 1000}k",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0284C7)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((90 * heightRatio).dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(Color(0xFF0284C7))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Orders vs Repairs Volume Comparison
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Orders vs. Mobile Repairs Distribution",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "High demand for mobile repair and fast accessories",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar comparisons
                    DistributionBar(title = "Phone & Accessories Sales", count = "${uiState.orders.size} Orders", percentage = 0.65f, barColor = Color(0xFF0284C7))
                    Spacer(modifier = Modifier.height(10.dp))
                    DistributionBar(title = "Screen & Hardware Repairs", count = "${uiState.repairs.size} Jobs", percentage = 0.85f, barColor = Color(0xFFD97706))
                    Spacer(modifier = Modifier.height(10.dp))
                    DistributionBar(title = "Pre-Owned Buy / Sell Exchanges", count = "${uiState.sellRequests.size} Trades", percentage = 0.45f, barColor = Color(0xFF7C3AED))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Recent Orders Table
        item {
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
                            text = "Recent Store Orders",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopPrimaryBlue,
                            modifier = Modifier.clickable { onNavigateTab(AdminTab.ORDERS) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    uiState.orders.take(3).forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${order.orderId} • ${order.customerName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${order.items.size} item(s) • ${order.orderDate}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatInr(order.totalAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ShopPrimaryBlue
                                )
                                Text(
                                    text = order.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    metric: MetricItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("metric_${metric.title}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(metric.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = metric.icon,
                        contentDescription = metric.title,
                        tint = metric.iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = metric.value,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = metric.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun DistributionBar(
    title: String,
    count: String,
    percentage: Float,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
            Text(text = count, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

private data class MetricItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color,
    val targetTab: AdminTab
)
