package com.example.admin.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.admin.model.CategoryRecord
import com.example.model.Product
import com.example.model.ProductCategory

private val GoldPrimary = Color(0xFFD4AF37)
private val GoldLight = Color(0xFFF6E05E)
private val DarkBackground = Color(0xFF0A0A0A)
private val DarkCard = Color(0xFF141414)
private val DarkSurface = Color(0xFF1A1A1A)
private val DarkBorder = Color(0xFF262626)

private data class PresetImage(val name: String, val url: String)

private val PRESET_IMAGES = listOf(
    PresetImage("Smartphones", "https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=500&auto=format&fit=crop&q=80"),
    PresetImage("Pre-Owned", "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=500&auto=format&fit=crop&q=80"),
    PresetImage("Chargers", "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&auto=format&fit=crop&q=80"),
    PresetImage("Cables", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80"),
    PresetImage("Audio", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80"),
    PresetImage("Accessories", "https://images.unsplash.com/photo-1584438784894-089d6a62b8fa?w=500&auto=format&fit=crop&q=80")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(
    categories: List<CategoryRecord>,
    products: List<Product> = emptyList(),
    onSaveCategory: (CategoryRecord) -> Unit,
    onToggleCategoryActive: (String) -> Unit,
    onDeleteCategory: (categoryId: String, reassignToSlug: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, INACTIVE

    var showFormDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryRecord?>(null) }

    // Form fields
    var formName by remember { mutableStateOf("") }
    var formSlug by remember { mutableStateOf("") }
    var formImageUrl by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formOrder by remember { mutableStateOf("1") }
    var formIsActive by remember { mutableStateOf(true) }

    // Delete confirmation state
    var categoryToDelete by remember { mutableStateOf<CategoryRecord?>(null) }
    var reassignTargetSlug by remember { mutableStateOf("") }

    val filteredCategories = categories.filter { cat ->
        val matchesQuery = cat.name.contains(searchQuery, ignoreCase = true) ||
                cat.slug.contains(searchQuery, ignoreCase = true) ||
                cat.description.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (selectedFilter) {
            "ACTIVE" -> cat.isActive
            "INACTIVE" -> !cat.isActive
            else -> true
        }
        matchesQuery && matchesStatus
    }.sortedBy { it.displayOrder }

    fun openAddDialog() {
        editingCategory = null
        val nextOrder = ((categories.maxOfOrNull { it.displayOrder } ?: 0) + 1).toString()
        formName = ""
        formSlug = ""
        formImageUrl = PRESET_IMAGES[0].url
        formDescription = ""
        formOrder = nextOrder
        formIsActive = true
        showFormDialog = true
    }

    fun openEditDialog(cat: CategoryRecord) {
        editingCategory = cat
        formName = cat.name
        formSlug = cat.slug
        formImageUrl = cat.imageUrl
        formDescription = cat.description
        formOrder = cat.displayOrder.toString()
        formIsActive = cat.isActive
        showFormDialog = true
    }

    // -------------------------------------------------------------
    // ADD / EDIT CATEGORY DIALOG
    // -------------------------------------------------------------
    if (showFormDialog) {
        AlertDialog(
            onDismissRequest = { showFormDialog = false },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingCategory != null) "Edit Category" else "Add New Category",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = { showFormDialog = false }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = formName,
                        onValueChange = {
                            formName = it
                            if (editingCategory == null) {
                                formSlug = it.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                            }
                        },
                        label = { Text("Category Name *", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("category_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formSlug,
                            onValueChange = { formSlug = it.lowercase().replace(" ", "-") },
                            label = { Text("Slug / Key *", color = Color.Gray) },
                            modifier = Modifier.weight(1f).testTag("category_slug_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder
                            )
                        )

                        OutlinedTextField(
                            value = formOrder,
                            onValueChange = { formOrder = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Display Order", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(110.dp).testTag("category_order_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder
                            )
                        )
                    }

                    OutlinedTextField(
                        value = formImageUrl,
                        onValueChange = { formImageUrl = it },
                        label = { Text("Category Image URL", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("category_image_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    // Quick image preset chips
                    Text(text = "Or choose quick preset image:", fontSize = 11.sp, color = Color.Gray)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PRESET_IMAGES) { preset ->
                            val isSelected = formImageUrl == preset.url
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else DarkSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GoldPrimary else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { formImageUrl = preset.url }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = preset.name,
                                    fontSize = 10.sp,
                                    color = if (isSelected) GoldLight else Color.LightGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formDescription,
                        onValueChange = { formDescription = it },
                        label = { Text("Description (Optional)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("category_desc_input"),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )

                    // Active Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Category Status", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (formIsActive) "Visible to customers" else "Hidden from frontend",
                                fontSize = 10.sp,
                                color = if (formIsActive) Color(0xFF4ADE80) else Color(0xFFF87171)
                            )
                        }
                        Switch(
                            checked = formIsActive,
                            onCheckedChange = { formIsActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = GoldPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formName.isNotBlank()) {
                            val parsedOrder = formOrder.toIntOrNull() ?: 1
                            val slugClean = formSlug.ifBlank {
                                formName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-")
                            }
                            val id = editingCategory?.id ?: "cat-$slugClean"
                            val matchedEnum = try {
                                ProductCategory.valueOf(slugClean.uppercase().replace("-", "_"))
                            } catch (e: Exception) {
                                ProductCategory.entries.find { it.name.equals(slugClean, ignoreCase = true) } ?: ProductCategory.ACCESSORIES
                            }

                            val record = CategoryRecord(
                                id = id,
                                name = formName.trim(),
                                slug = slugClean,
                                category = matchedEnum,
                                isActive = formIsActive,
                                productCount = editingCategory?.productCount ?: 0,
                                imageUrl = formImageUrl.trim(),
                                description = formDescription.trim(),
                                displayOrder = parsedOrder,
                                createdAt = editingCategory?.createdAt ?: System.currentTimeMillis()
                            )
                            onSaveCategory(record)
                            showFormDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_category_btn")
                ) {
                    Text("Save Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFormDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }

    // -------------------------------------------------------------
    // SAFE DELETE CATEGORY DIALOG (WITH REASSIGNMENT WARNING)
    // -------------------------------------------------------------
    categoryToDelete?.let { cat ->
        val assignedProducts = products.count { p -> p.categoryMatches(cat.slug) || (p.category == cat.category) }
        val otherCategories = categories.filter { it.id != cat.id && it.slug != cat.slug && it.isActive }

        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFEF4444))
                    Text(text = "Delete Category?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Category: ${cat.name}",
                        fontWeight = FontWeight.SemiBold,
                        color = GoldPrimary,
                        fontSize = 13.sp
                    )

                    if (assignedProducts > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "⚠️ Warning: $assignedProducts products are assigned to this category!",
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Products will NEVER be deleted. Please choose a destination category to safely reassign them:",
                                    color = Color(0xFFFEE2E2),
                                    fontSize = 11.sp
                                )

                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = otherCategories.find { it.slug == reassignTargetSlug }?.name ?: (otherCategories.firstOrNull()?.name ?: "Select Category"),
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = DarkBorder
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(DarkCard)
                                    ) {
                                        otherCategories.forEach { dest ->
                                            DropdownMenuItem(
                                                text = { Text(dest.name, color = Color.White) },
                                                onClick = {
                                                    reassignTargetSlug = dest.slug
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "This category has 0 assigned products. Are you sure you want to permanently delete '${cat.name}'?",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetSlug = if (assignedProducts > 0) {
                            reassignTargetSlug.ifBlank { otherCategories.firstOrNull()?.slug }
                        } else null
                        onDeleteCategory(cat.id, targetSlug)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_category_btn")
                ) {
                    Text(
                        text = if (assignedProducts > 0) "Reassign & Delete" else "Confirm Delete",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { categoryToDelete = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }

    // -------------------------------------------------------------
    // MAIN SCREEN CONTENT
    // -------------------------------------------------------------
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("admin_categories_screen")
    ) {
        // Top Header & Controls Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Layers, contentDescription = "Categories", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Category Management (${categories.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Add, reorder, edit and toggle store taxonomy",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = { openAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_category_header_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search category by name or slug...", fontSize = 12.sp, color = Color.Gray) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("category_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                // Status Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    val filters = listOf("ALL" to "All (${categories.size})", "ACTIVE" to "Active (${categories.count { it.isActive }})", "INACTIVE" to "Hidden (${categories.count { !it.isActive }})")
                    filters.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else DarkSurface)
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // Category Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCategories, key = { it.id }) { cat ->
                val assignedCount = products.count { p -> p.categoryMatches(cat.slug) || (p.category == cat.category) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_row_${cat.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (cat.isActive) DarkBorder else Color(0xFF451A1A)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Thumbnail + Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cat.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = cat.imageUrl,
                                        contentDescription = cat.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = GoldPrimary)
                                }

                                // Display order tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "#${cat.displayOrder}",
                                        fontSize = 9.sp,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Details
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = cat.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (cat.isActive) Color(0xFF064E3B) else Color(0xFF451A1A))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (cat.isActive) "Active" else "Hidden",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cat.isActive) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
                                        )
                                    }
                                }

                                Text(
                                    text = "slug: ${cat.slug} • $assignedCount products",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                if (cat.description.isNotBlank()) {
                                    Text(
                                        text = cat.description,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA1A1AA),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Right: Actions (Active Switch, Edit, Delete)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Switch(
                                checked = cat.isActive,
                                onCheckedChange = { onToggleCategoryActive(cat.id) },
                                modifier = Modifier.testTag("category_toggle_${cat.id}"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = DarkSurface
                                )
                            )

                            IconButton(
                                onClick = { openEditDialog(cat) },
                                modifier = Modifier.size(32.dp).testTag("edit_category_${cat.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Category",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    reassignTargetSlug = otherCategories(cat, categories).firstOrNull()?.slug ?: ""
                                    categoryToDelete = cat
                                },
                                modifier = Modifier.size(32.dp).testTag("delete_category_${cat.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Category",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun otherCategories(target: CategoryRecord, all: List<CategoryRecord>): List<CategoryRecord> {
    return all.filter { it.id != target.id && it.slug != target.slug && it.isActive }
}
