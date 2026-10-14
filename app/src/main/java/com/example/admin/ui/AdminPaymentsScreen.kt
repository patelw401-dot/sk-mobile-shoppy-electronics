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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.admin.model.PaymentRecord
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminPaymentsScreen(
    payments: List<PaymentRecord>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_payments_screen")
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Payment Transactions (${payments.size})", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = "Demo transactions across UPI, Cash on Delivery and Cards", fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(payments) { pay ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("payment_row_${pay.paymentId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = pay.paymentId, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ShopPrimaryBlue)
                                Text(text = "Order: ${pay.orderId} • ${pay.date}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Payment Status Badge
                                Surface(
                                    color = when (pay.status) {
                                        "Successful", "Paid" -> Color(0xFFDCFCE7)
                                        "Pending" -> Color(0xFFFEF3C7)
                                        "Refunded" -> Color(0xFFE0E7FF)
                                        "Cancelled" -> Color(0xFFF1F5F9)
                                        else -> Color(0xFFFEE2E2)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = pay.status,
                                        color = when (pay.status) {
                                            "Successful", "Paid" -> Color(0xFF166534)
                                            "Pending" -> Color(0xFF92400E)
                                            "Refunded" -> Color(0xFF3730A3)
                                            "Cancelled" -> Color(0xFF475569)
                                            else -> Color(0xFF991B1B)
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                // Bank Settlement Status Badge
                                Surface(
                                    color = when (pay.settlementStatus) {
                                        "Settled to Bank" -> Color(0xFFDBEAFE)
                                        "Pending Settlement" -> Color(0xFFF3E8FF)
                                        "Reversed" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFF1F5F9)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = pay.settlementStatus,
                                        color = when (pay.settlementStatus) {
                                            "Settled to Bank" -> Color(0xFF1E40AF)
                                            "Pending Settlement" -> Color(0xFF6B21A8)
                                            "Reversed" -> Color(0xFF991B1B)
                                            else -> Color(0xFF475569)
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Customer: ${pay.customerName} (${pay.method})", fontSize = 12.sp, color = Color(0xFF334155))
                                if (pay.transactionRef.isNotBlank()) {
                                    Text(text = "Gateway Ref: ${pay.transactionRef}", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                            Text(text = formatInr(pay.amount), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        }
                    }
                }
            }
        }
    }
}
