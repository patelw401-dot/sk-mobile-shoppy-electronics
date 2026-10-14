package com.example.data.models

import com.example.model.CartItem
import com.example.model.ProductCategory

/**
 * Standardized Domain Entities for SK Mobile Backend/Database Integration
 */

data class ProductEntity(
    val id: String,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val description: String,
    val price: Int,
    val originalPrice: Int,
    val discount: Int = if (originalPrice > price) (((originalPrice - price).toDouble() / originalPrice) * 100).toInt() else 0,
    val stock: Int = 10,
    val sku: String = "SKM-PROD-${id.takeLast(4).uppercase()}",
    val images: List<String> = emptyList(),
    val specifications: Map<String, String> = emptyMap(),
    val rating: Float = 4.5f,
    val reviewCount: Int = 0,
    val featured: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class OrderEntity(
    val id: String,
    val customerId: String = "CUST-DEFAULT",
    val customerName: String,
    val phone: String,
    val items: List<CartItem>,
    val address: String,
    val deliveryMethod: String,
    val paymentMethod: String,
    val paymentStatus: String = "Paid",
    val orderStatus: String = "Order Confirmed",
    val subtotal: Int,
    val deliveryCharge: Int = 0,
    val total: Int,
    val createdAt: Long = System.currentTimeMillis()
)

data class RepairBookingEntity(
    val id: String,
    val customerName: String,
    val phone: String,
    val brand: String,
    val model: String,
    val problem: String,
    val pickupRequired: Boolean,
    val address: String,
    val preferredDate: String,
    val estimatedCost: String,
    val repairStatus: String = "Requested",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class CustomerEntity(
    val id: String,
    val name: String,
    val phone: String,
    val email: String = "customer@example.com",
    val address: String,
    val totalOrders: Int = 0,
    val totalRepairs: Int = 0,
    val totalSpent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class OldPhoneRequestEntity(
    val id: String,
    val customerName: String,
    val phone: String,
    val brand: String,
    val model: String,
    val storage: String,
    val condition: String,
    val expectedPrice: String,
    val offeredPrice: String = "",
    val requestType: String = "SELL", // "SELL" or "BUY"
    val status: String = "Valuation Generated",
    val address: String,
    val createdAt: Long = System.currentTimeMillis()
)
