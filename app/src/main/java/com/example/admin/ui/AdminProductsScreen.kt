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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.ProductVisualBox
import com.example.ui.components.formatInr
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopSuccessGreen

@Composable
fun AdminProductsScreen(
    products: List<Product>,
    onAddProduct: (Product) -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onToggleActive: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(ProductCategory.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToView by remember { mutableStateOf<Product?>(null) }

    val filteredList = products.filter { product ->
        val matchesCategory = selectedCategoryFilter == ProductCategory.ALL || product.category == selectedCategoryFilter
        val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.brand.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Add or Edit Product Dialog
    if (showAddDialog || productToEdit != null) {
        ProductFormDialog(
            initialProduct = productToEdit,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onSave = { product ->
                if (productToEdit != null) {
                    onUpdateProduct(product)
                } else {
                    onAddProduct(product)
                }
                showAddDialog = false
                productToEdit = null
            }
        )
    }

    // View Product Details Dialog
    if (productToView != null) {
        val p = productToView!!
        AlertDialog(
            onDismissRequest = { productToView = null },
            containerColor = Color.White,
            title = {
                Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(text = "Brand: ${p.brand}", fontSize = 13.sp)
                    Text(text = "Category: ${p.category.displayName}", fontSize = 13.sp)
                    Text(text = "Selling Price: ${formatInr(p.price)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Original MRP: ${formatInr(p.originalPrice)}", fontSize = 13.sp)
                    Text(text = "Stock: ${p.stockStatus}", fontSize = 13.sp, color = if (p.inStock) ShopSuccessGreen else Color.Red)
                    Text(text = "Warranty: ${p.warranty}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Description:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = p.description, fontSize = 12.sp, color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Button(onClick = { productToView = null }, colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_products_screen")
    ) {
        // Top Action Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Product Inventory (${filteredList.size})",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Manage store catalog, pricing & stock status",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_add_product_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by product name or brand...", fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_product_search"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        // Products List
        if (filteredList.isEmpty()) {
            com.example.ui.components.EmptyStateView(
                icon = Icons.Default.Inventory2,
                title = "No Products Found",
                message = if (searchQuery.isNotBlank()) "No products match '$searchQuery'." else "No products found in this category.",
                actionLabel = "Add New Product",
                onAction = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .testTag("admin_product_row_${product.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductVisualBox(
                                product = product,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = product.brand.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ShopPrimaryBlue
                                    )

                                    Surface(
                                        color = if (product.inStock) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (product.inStock) "Active" else "Inactive",
                                            color = if (product.inStock) Color(0xFF166534) else Color(0xFF991B1B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = product.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatInr(product.price),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "MRP: ${formatInr(product.originalPrice)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${product.stockStatus}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Actions: View, Toggle Active/Deactivate, Edit, Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { productToView = product },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = "View", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View", fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedButton(
                                onClick = {
                                    if (onToggleActive != null) {
                                        onToggleActive(product.id)
                                    } else {
                                        val newInStock = !product.inStock
                                        onUpdateProduct(
                                            product.copy(
                                                inStock = newInStock,
                                                stockStatus = if (newInStock) "In Stock" else "Out of Stock"
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.height(32.dp).testTag("admin_toggle_active_${product.id}"),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (product.inStock) Color(0xFFD97706) else ShopSuccessGreen
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (product.inStock) "Deactivate" else "Activate",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { productToEdit = product },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { onDeleteProduct(product.id) },
                                modifier = Modifier.size(32.dp).testTag("admin_delete_product_${product.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ProductFormDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.toString() ?: "") }
    var origPriceText by remember { mutableStateOf(initialProduct?.originalPrice?.toString() ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var stockQuantity by remember { mutableStateOf("15") }
    var sku by remember { mutableStateOf("SKM-PROD-${(100..999).random()}") }
    var inStock by remember { mutableStateOf(initialProduct?.inStock ?: true) }
    var isFeatured by remember { mutableStateOf(initialProduct?.isFeatured ?: false) }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: ProductCategory.MOBILE_PHONES) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(text = if (initialProduct != null) "Edit Product" else "Add New Product", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("product_form_name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand * (e.g. Apple, Samsung, boAt)") },
                        modifier = Modifier.fillMaxWidth().testTag("product_form_brand"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Selling Price (₹) *") },
                            modifier = Modifier.weight(1f).testTag("product_form_price"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = origPriceText,
                            onValueChange = { origPriceText = it },
                            label = { Text("Original Price (₹)") },
                            modifier = Modifier.weight(1f).testTag("product_form_orig_price"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockQuantity,
                            onValueChange = { stockQuantity = it },
                            label = { Text("Stock Quantity") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    var categoryExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category: ${selectedCategory.displayName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Box {
                            OutlinedButton(
                                onClick = { categoryExpanded = true },
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text("Change", fontSize = 11.sp)
                            }
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                ProductCategory.entries.filterNot { it == ProductCategory.ALL }.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.displayName) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Specifications") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active & In Stock", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = inStock, onCheckedChange = { inStock = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Featured on Homepage", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pPrice = (priceText.toIntOrNull() ?: 999).coerceAtLeast(1)
                    val oPrice = (origPriceText.toIntOrNull() ?: (pPrice + 400)).coerceAtLeast(pPrice)
                    val parsedStock = (stockQuantity.toIntOrNull() ?: 15).coerceAtLeast(0)
                    val effectiveInStock = inStock && parsedStock > 0

                    val newProduct = Product(
                        id = initialProduct?.id ?: "prod-${System.currentTimeMillis()}",
                        name = if (name.isBlank()) "New Product" else name,
                        brand = if (brand.isBlank()) "SK Mobile" else brand,
                        category = selectedCategory,
                        price = pPrice,
                        originalPrice = oPrice,
                        rating = initialProduct?.rating ?: 4.8f,
                        reviewCount = initialProduct?.reviewCount ?: 12,
                        description = if (description.isBlank()) "Genuine product backed by SK Mobile warranty." else description,
                        specifications = mapOf("SKU" to sku, "Stock" to "$parsedStock Units", "Warranty" to "1 Year Brand Warranty"),
                        stock = parsedStock,
                        sku = sku,
                        images = initialProduct?.images ?: emptyList(),
                        active = effectiveInStock,
                        inStock = effectiveInStock,
                        stockStatus = if (effectiveInStock) "In Stock ($parsedStock units)" else "Out of Stock",
                        isFeatured = isFeatured,
                        featured = isFeatured
                    )
                    onSave(newProduct)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
