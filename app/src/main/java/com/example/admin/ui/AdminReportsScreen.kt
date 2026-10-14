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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.viewmodel.AdminUiState
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminReportsScreen(
    uiState: AdminUiState,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("Today", "7 Days", "30 Days", "Custom")

    // Dynamic multiplier based on chosen date filter
    val multiplier = when (uiState.selectedReportFilter) {
        "Today" -> 0.15
        "7 Days" -> 0.45
        "30 Days" -> 1.0
        else -> 1.3
    }

    val totalSales = (uiState.totalSalesAmount * multiplier).toInt()
    val estProfit = (uiState.estimatedProfitAmount * multiplier).toInt()
    val ordersCount = (uiState.totalOrdersCount * multiplier).toInt().coerceAtLeast(1)
    val repairsCount = (uiState.totalRepairsCount * multiplier).toInt().coerceAtLeast(1)
    val repairRev = (repairsCount * 1450)
    val accessoriesRev = (totalSales - repairRev).coerceAtLeast(3500)
    val oldPhoneTrans = (uiState.oldPhonesPurchasedCount * multiplier).toInt().coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_reports_screen")
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Sales & Operations Reports", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = "Financial performance & repair metrics for SK Mobile Udgir", fontSize = 11.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { filter ->
                        val isSelected = filter == uiState.selectedReportFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ShopPrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Revenue Summary (${uiState.selectedReportFilter})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        ReportMetricRow(label = "Gross Sales Revenue", value = formatInr(totalSales), isBold = true)
                        ReportMetricRow(label = "Estimated Net Profit (28%)", value = formatInr(estProfit), isHighlight = true)
                        ReportMetricRow(label = "Accessories & Mobile Revenue", value = formatInr(accessoriesRev))
                        ReportMetricRow(label = "Service & Repair Revenue", value = formatInr(repairRev))

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "Operations Volume", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        ReportMetricRow(label = "Total Customer Orders", value = "$ordersCount Orders")
                        ReportMetricRow(label = "Completed Phone Repairs", value = "$repairsCount Repair Jobs")
                        ReportMetricRow(label = "Old Phone Trades & Purchases", value = "$oldPhoneTrans Devices")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = if (isBold) Color(0xFF0F172A) else Color(0xFF475569), fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(
            text = value,
            fontSize = if (isHighlight || isBold) 14.sp else 13.sp,
            fontWeight = if (isHighlight || isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isHighlight) Color(0xFF166534) else if (isBold) ShopPrimaryBlue else Color(0xFF0F172A)
        )
    }
}
