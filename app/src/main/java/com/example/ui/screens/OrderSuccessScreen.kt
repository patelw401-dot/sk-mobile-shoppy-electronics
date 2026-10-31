package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShopOrder
import com.example.ui.components.ProductVisualBox
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@Composable
fun OrderSuccessScreen(
    order: ShopOrder?,
    onContinueShopping: () -> Unit,
    onTrackOrder: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (order == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onContinueShopping,
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
            ) {
                Text("Continue Shopping")
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
            .testTag("order_success_screen"),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Celebration Banner
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(BlackSurfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = GoldPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Order Placed Successfully!",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ShopTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Thank you for shopping at SK Mobile Shoppy and Electronics, Udgir.",
                fontSize = 13.sp,
                color = ShopTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Order Key Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackSurface),
                border = BorderStroke(1.dp, BlackBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order Number",
                                fontSize = 11.sp,
                                color = ShopTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = order.orderId,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = ShopPrimaryBlue
                            )
                        }

                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = order.status,
                                color = Color(0xFF166534),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ShopOutlineLight)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Customer Name & Phone
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Customer Name: ",
                            fontSize = 13.sp,
                            color = ShopTextSecondary
                        )
                        Text(
                            text = order.customerName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Contact Phone: ",
                            fontSize = 13.sp,
                            color = ShopTextSecondary
                        )
                        Text(
                            text = "+91 ${order.mobileNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ShopTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Delivery Option & Address
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Address",
                            tint = ShopPrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Delivery Address:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ShopTextPrimary
                            )
                            Text(
                                text = "${order.address}, ${order.city}, ${order.state} - ${order.pinCode}",
                                fontSize = 12.sp,
                                color = ShopTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "Speed",
                            tint = ShopSuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Option: ${order.deliveryOption}",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Ordered Products List
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlackSurface),
                border = BorderStroke(1.dp, BlackBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ordered Products (${order.items.sumOf { it.quantity }} items)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    order.items.forEach { cartItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductVisualBox(
                                product = cartItem.product,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cartItem.product.brand.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopPrimaryBlue
                                )
                                Text(
                                    text = cartItem.product.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Qty: ${cartItem.quantity} × ${formatInr(cartItem.product.price)}",
                                    fontSize = 11.sp,
                                    color = ShopTextSecondary
                                )
                            }

                            Text(
                                text = formatInr(cartItem.product.price * cartItem.quantity),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = ShopOutlineLight)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Total Amount Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Amount Paid (${order.paymentMethod})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )
                        Text(
                            text = formatInr(order.totalAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ShopPrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Track Order & Continue Shopping Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onTrackOrder(order.orderId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("order_success_track_order_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Track",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Track Order Progress",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                OutlinedButton(
                    onClick = onContinueShopping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("order_success_continue_shopping_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BlackBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Shop",
                        tint = TextMain,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue Shopping",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
