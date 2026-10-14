package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.ProductCard
import com.example.ui.theme.ShopOutlineLight
import com.example.ui.theme.ShopPrimaryBlue
import com.example.ui.theme.ShopPrimaryLightBlue
import com.example.ui.theme.ShopTextMuted
import com.example.ui.theme.ShopTextPrimary
import com.example.ui.theme.ShopTextSecondary
import com.example.viewmodel.SortOption

@Composable
fun ProductsScreen(
    products: List<Product>,
    selectedCategory: ProductCategory,
    searchQuery: String,
    sortOption: SortOption,
    onCategorySelect: (ProductCategory) -> Unit,
    onSortSelect: (SortOption) -> Unit,
    onClearSearch: () -> Unit,
    onSelectProduct: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onBuyNow: (Product) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter by Category
    val filteredByCategory = if (selectedCategory == ProductCategory.ALL) {
        products
    } else {
        products.filter { it.category == selectedCategory }
    }

    // Filter by Search Query
    val filteredBySearch = if (searchQuery.isBlank()) {
        filteredByCategory
    } else {
        val query = searchQuery.trim().lowercase()
        filteredByCategory.filter {
            it.name.lowercase().contains(query) ||
            it.brand.lowercase().contains(query) ||
            it.description.lowercase().contains(query) ||
            it.category.displayName.lowercase().contains(query)
        }
    }

    // Sort Products
    val sortedProducts = when (sortOption) {
        SortOption.RECOMMENDED -> filteredBySearch.sortedByDescending { it.reviewCount }
        SortOption.PRICE_LOW_HIGH -> filteredBySearch.sortedBy { it.price }
        SortOption.PRICE_HIGH_LOW -> filteredBySearch.sortedByDescending { it.price }
        SortOption.HIGHEST_RATED -> filteredBySearch.sortedByDescending { it.rating }
        SortOption.NEWEST -> filteredBySearch.sortedByDescending { it.releaseOrder }
    }

    val categories = listOf(
        ProductCategory.ALL,
        ProductCategory.MOBILE_PHONES,
        ProductCategory.OLD_PHONES,
        ProductCategory.CHARGERS,
        ProductCategory.DATA_CABLES,
        ProductCategory.HEADPHONES,
        ProductCategory.BLUETOOTH,
        ProductCategory.ACCESSORIES
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FA))
            .testTag("products_catalog_screen")
    ) {
        // Top Action Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ShopTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Text(
                                text = if (selectedCategory == ProductCategory.ALL) "All Products" else selectedCategory.displayName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopTextPrimary
                            )
                            Text(
                                text = "${sortedProducts.size} items available",
                                fontSize = 11.sp,
                                color = ShopTextSecondary
                            )
                        }
                    }

                    // Sort Button & Dropdown
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ShopPrimaryLightBlue,
                            modifier = Modifier.clickable { showSortMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = ShopPrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sortOption.label,
                                    fontSize = 11.sp,
                                    color = ShopPrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal,
                                            color = if (option == sortOption) ShopPrimaryBlue else ShopTextPrimary
                                        )
                                    },
                                    onClick = {
                                        onSortSelect(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Active Search Pill if searching
                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Search: \"$searchQuery\"",
                                fontSize = 12.sp,
                                color = ShopTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable(onClick = onClearSearch)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(category) },
                            label = {
                                Text(
                                    text = category.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ShopPrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = ShopTextPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        // Product Grid Content
        if (sortedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No Products",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Products Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing your search or selecting a different category.",
                        fontSize = 12.sp,
                        color = ShopTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onClearSearch()
                            onCategorySelect(ProductCategory.ALL)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryBlue)
                    ) {
                        Text("Show All Products")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedProducts) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onSelectProduct(product) },
                        onAddToCart = { onAddToCart(product) },
                        onBuyNow = { onBuyNow(product) }
                    )
                }
            }
        }
    }
}
