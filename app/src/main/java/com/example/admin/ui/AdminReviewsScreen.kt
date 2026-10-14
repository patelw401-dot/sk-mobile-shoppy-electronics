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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.model.AdminReviewRecord
import com.example.ui.theme.ShopAccentAmber
import com.example.ui.theme.ShopPrimaryBlue

@Composable
fun AdminReviewsScreen(
    reviews: List<AdminReviewRecord>,
    onToggleApproval: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_reviews_screen")
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Customer Reviews Moderation (${reviews.size})", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = "Approve or hide customer testimonials shown on homepage", fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(reviews) { item ->
                val rev = item.review
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("admin_review_${rev.id}"),
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
                            Text(text = rev.customerName, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Surface(
                                color = if (item.isApproved) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (item.isApproved) "Approved" else "Hidden",
                                    color = if (item.isApproved) Color(0xFF166534) else Color(0xFF991B1B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(rev.rating) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = ShopAccentAmber, modifier = Modifier.size(13.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${rev.location} • ${rev.date}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "\"${rev.comment}\"", fontSize = 12.sp, color = Color(0xFF334155))

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (item.isApproved) {
                                OutlinedButton(
                                    onClick = { onToggleApproval(rev.id) },
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Hide Review", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            } else {
                                Button(
                                    onClick = { onToggleApproval(rev.id) },
                                    modifier = Modifier.height(30.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Approve Review", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
