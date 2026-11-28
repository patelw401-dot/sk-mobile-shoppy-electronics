package com.example.admin.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.ProductVisualBox
import com.example.ui.components.compressUriToDataUri
import com.example.ui.components.formatInr
import com.example.ui.components.rememberImageModel
import com.example.ui.theme.LightInputBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopSuccessGreen
import com.example.ui.theme.lightOutlinedTextFieldColors
import kotlinx.coroutines.launch

@Composable
fun AdminProductsScreen(
    products: List<Product>,
    categories: List<com.example.admin.model.CategoryRecord> = emptyList(),
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
            categories = categories,
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
                Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LightTextPrimary)
            },
            text = {
                Column {
                    Text(text = "Brand: ${p.brand}", fontSize = 13.sp, color = LightTextPrimary)
                    Text(text = "Category: ${p.category.displayName}", fontSize = 13.sp, color = LightTextPrimary)
                    Text(text = "Selling Price: ${formatInr(p.price)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary)
                    Text(text = "Original MRP: ${formatInr(p.originalPrice)}", fontSize = 13.sp, color = LightTextSecondary)
                    Text(text = "Stock: ${p.stockStatus}", fontSize = 13.sp, color = if (p.inStock) Color(0xFF166534) else Color(0xFFDC2626))
                    Text(text = "Warranty: ${p.warranty}", fontSize = 13.sp, color = LightTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Description:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary)
                    Text(text = p.description, fontSize = 12.sp, color = LightTextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { productToView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color.White)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_products_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Scrollable Compact Header & Search Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Product Inventory (${filteredList.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Manage store catalog, pricing & stock",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ShopPrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("admin_add_product_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search product name or brand...", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = LightTextSecondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search", tint = LightTextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        } else null,
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("admin_product_search"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Products List or Empty State
        if (filteredList.isEmpty()) {
            item {
                com.example.ui.components.EmptyStateView(
                    icon = Icons.Default.Inventory2,
                    title = "No Products Found",
                    message = if (searchQuery.isNotBlank()) "No products match '$searchQuery'." else "No products found in this category.",
                    actionLabel = "Add New Product",
                    onAction = { showAddDialog = true },
                    isDarkSurface = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }
        } else {
            items(filteredList) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product.brand.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ShopPrimaryBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        color = if (product.inStock) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (product.inStock) "Active" else "Inactive",
                                            color = if (product.inStock) Color(0xFF166534) else Color(0xFF991B1B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
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
                                        color = Color(0xFF0F172A),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "MRP: ${formatInr(product.originalPrice)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${product.stockStatus}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF475569),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
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
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LightInputBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = "View", tint = LightTextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View", fontSize = 11.sp, color = LightTextPrimary, maxLines = 1, softWrap = false)
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
                                modifier = Modifier.height(34.dp).testTag("admin_toggle_active_${product.id}"),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (product.inStock) Color(0xFFD97706) else ShopSuccessGreen
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (product.inStock) Color(0xFFB45309) else Color(0xFF166534)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (product.inStock) "Deactivate" else "Activate",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { productToEdit = product },
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontSize = 11.sp, color = Color.White, maxLines = 1, softWrap = false)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { onDeleteProduct(product.id) },
                                modifier = Modifier.size(34.dp).testTag("admin_delete_product_${product.id}")
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

@Composable
fun ProductFormDialog(
    initialProduct: Product?,
    categories: List<com.example.admin.model.CategoryRecord> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember(initialProduct?.id) { mutableStateOf(initialProduct?.name ?: "") }
    var brand by remember(initialProduct?.id) { mutableStateOf(initialProduct?.brand ?: "") }
    var priceText by remember(initialProduct?.id) { mutableStateOf(initialProduct?.price?.toString() ?: "") }
    var origPriceText by remember(initialProduct?.id) { mutableStateOf(initialProduct?.originalPrice?.toString() ?: "") }
    var description by remember(initialProduct?.id) { mutableStateOf(initialProduct?.description ?: "") }
    var stockQuantity by remember(initialProduct?.id) { mutableStateOf(initialProduct?.stock?.toString() ?: "15") }
    var sku by remember(initialProduct?.id) {
        mutableStateOf(
            initialProduct?.sku?.takeIf { it.isNotBlank() } ?: "SKM-PROD-${(100..999).random()}"
        )
    }
    var inStock by remember(initialProduct?.id) { mutableStateOf(initialProduct?.inStock ?: true) }
    var isFeatured by remember(initialProduct?.id) { mutableStateOf(initialProduct?.isFeatured ?: false) }
    var selectedCategory by remember(initialProduct?.id) { mutableStateOf(initialProduct?.category ?: ProductCategory.MOBILE_PHONES) }
    var selectedCategorySlug by remember(initialProduct?.id) { mutableStateOf(initialProduct?.categorySlug ?: "smartphones") }
    var imageUrl by remember(initialProduct?.id) { mutableStateOf(initialProduct?.primaryImage ?: "") }
    var hasWarranty by remember(initialProduct?.id) { mutableStateOf(initialProduct?.hasWarranty ?: true) }
    var warrantyDuration by remember(initialProduct?.id) {
        mutableStateOf(
            if (initialProduct?.warrantyDuration.isNullOrBlank()) (initialProduct?.warranty ?: "1 Year Brand Warranty") else initialProduct.warrantyDuration
        )
    }
    var isCompressingImage by remember { mutableStateOf(false) }
    var imagePickError by remember { mutableStateOf<String?>(null) }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isCompressingImage = true
            imagePickError = null
            coroutineScope.launch {
                val dataUri = compressUriToDataUri(
                    context = context,
                    uri = uri,
                    maxDimension = 512,
                    jpegQuality = 80
                )
                if (dataUri != null) {
                    imageUrl = dataUri
                } else {
                    imagePickError = "Unable to read the selected photo. Try another image."
                }
                isCompressingImage = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (initialProduct != null) "Edit Product" else "Add New Product",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = LightTextPrimary
            )
        },
        text = {
            val isDataUri = imageUrl.trim().startsWith("data:image/", ignoreCase = true)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    colors = lightOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("product_form_name"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand * (e.g. Apple, Samsung, boAt)") },
                    colors = lightOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("product_form_brand"),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Selling Price (₹) *") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.weight(1f).testTag("product_form_price"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = origPriceText,
                        onValueChange = { origPriceText = it },
                        label = { Text("Original Price (₹)") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.weight(1f).testTag("product_form_orig_price"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("Stock Quantity") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Code") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                var categoryExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentCategoryLabel = if (categories.isNotEmpty()) {
                        categories.find { it.slug.equals(selectedCategorySlug, true) }?.name ?: selectedCategory.displayName
                    } else {
                        selectedCategory.displayName
                    }
                    Text(
                        text = "Category: $currentCategoryLabel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LightTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { categoryExpanded = true },
                            border = androidx.compose.foundation.BorderStroke(1.dp, LightInputBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary),
                            modifier = Modifier.heightIn(min = 38.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Select Category", fontSize = 11.sp, color = LightTextPrimary)
                        }
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            if (categories.isNotEmpty()) {
                                categories.filter { it.isActive }.forEach { catRecord ->
                                    DropdownMenuItem(
                                        text = { Text(catRecord.name, color = LightTextPrimary) },
                                        onClick = {
                                            selectedCategory = catRecord.category
                                            selectedCategorySlug = catRecord.slug
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            } else {
                                ProductCategory.entries.filterNot { it == ProductCategory.ALL }.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.displayName, color = LightTextPrimary) },
                                        onClick = {
                                            selectedCategory = cat
                                            selectedCategorySlug = cat.name.lowercase().replace("_", "-")
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Specifications") },
                    colors = lightOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Live Thumbnail Preview + Phone Gallery Upload Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, LightInputBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                            .border(1.dp, LightInputBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = rememberImageModel(imageUrl),
                                contentDescription = "Product photo preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "No product photo",
                                tint = LightTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isCompressingImage,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopPrimaryBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ShopPrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("product_upload_gallery_btn")
                        ) {
                            if (isCompressingImage) {
                                CircularProgressIndicator(
                                    color = ShopPrimaryBlue,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Compressing...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopPrimaryBlue
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload Photo from Gallery",
                                    tint = ShopPrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (imageUrl.isNotBlank()) "Replace Photo from Gallery" else "Upload Photo from Gallery",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopPrimaryBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (imageUrl.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDataUri) "Gallery photo ready" else "Image URL active",
                                    fontSize = 11.sp,
                                    color = if (isDataUri) Color(0xFF166534) else LightTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            imagePickError = null
                                            imageUrl = ""
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("product_clear_image_btn")
                                )
                            }
                        }

                        imagePickError?.let { errMsg ->
                            Text(
                                text = errMsg,
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = if (isDataUri) "" else imageUrl,
                    onValueChange = {
                        imagePickError = null
                        imageUrl = it
                    },
                    label = { Text("Product Image URL (https://...)") },
                    placeholder = {
                        Text(
                            text = if (isDataUri) "Gallery photo active (paste URL to replace)" else "https://...",
                            fontSize = 11.sp
                        )
                    },
                    colors = lightOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("product_form_image_url"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Has Store / Brand Warranty", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LightTextPrimary)
                    Switch(checked = hasWarranty, onCheckedChange = { hasWarranty = it })
                }
                if (hasWarranty) {
                    OutlinedTextField(
                        value = warrantyDuration,
                        onValueChange = { warrantyDuration = it },
                        label = { Text("Warranty Duration (e.g. 1 Year Brand Warranty)") },
                        colors = lightOutlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active & In Stock", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LightTextPrimary)
                    Switch(checked = inStock, onCheckedChange = { inStock = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured on Homepage", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = LightTextPrimary)
                    Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pPrice = (priceText.toIntOrNull() ?: 999).coerceAtLeast(1)
                    val oPrice = (origPriceText.toIntOrNull() ?: (pPrice + 400)).coerceAtLeast(pPrice)
                    val parsedStock = (stockQuantity.toIntOrNull() ?: 15).coerceAtLeast(0)
                    val effectiveInStock = inStock && parsedStock > 0

                    val resolvedSpecs = initialProduct?.specifications ?: emptyMap()

                    val finalImageUrl = imageUrl.trim()
                    val imagesList = if (finalImageUrl.isNotBlank()) {
                        listOf(finalImageUrl)
                    } else {
                        emptyList()
                    }

                    val newProduct = Product(
                        id = initialProduct?.id ?: "prod-${System.currentTimeMillis()}",
                        name = if (name.isBlank()) "New Product" else name,
                        brand = if (brand.isBlank()) "SK Mobile" else brand,
                        category = selectedCategory,
                        categorySlug = selectedCategorySlug,
                        price = pPrice,
                        originalPrice = oPrice,
                        rating = initialProduct?.rating ?: 4.8f,
                        reviewCount = initialProduct?.reviewCount ?: 12,
                        description = if (description.isBlank()) "Genuine product backed by SK Mobile warranty." else description,
                        specifications = resolvedSpecs,
                        stock = parsedStock,
                        sku = sku,
                        imageUrl = finalImageUrl,
                        images = imagesList,
                        hasWarranty = hasWarranty,
                        warrantyDuration = if (hasWarranty) warrantyDuration.trim() else "",
                        warrantyDescription = if (hasWarranty) "Official warranty backed with GST invoice" else "",
                        warranty = if (hasWarranty) (warrantyDuration.trim().ifBlank { "1 Year Brand Warranty" }) else "No Warranty",
                        active = effectiveInStock,
                        inStock = effectiveInStock,
                        stockStatus = if (effectiveInStock) "In Stock ($parsedStock units)" else "Out of Stock",
                        isFeatured = isFeatured,
                        featured = isFeatured
                    )
                    onSave(newProduct)
                },
                enabled = !isCompressingImage,
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue, contentColor = Color.White),
                modifier = Modifier.testTag("save_product_btn")
            ) {
                Text("Save Product", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, LightInputBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightTextPrimary)
            ) {
                Text("Cancel", color = LightTextPrimary)
            }
        }
    )
}
