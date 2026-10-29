package com.example.admin.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.admin.viewmodel.AdminViewModel
import com.example.data.firebase.FirebaseStorageService
import com.example.model.Product
import com.example.model.ReelItem
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.BlackSurfaceSecondary
import com.example.ui.theme.ColorError
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Enterprise Admin Reel Management Screen.
 * Only authenticated administrators can access this view to:
 * - Upload new video files to Firebase Storage
 * - Publish, unpublish, edit captions, and link store products
 * - Delete reels from Firestore and cache
 */
@Composable
fun AdminReelsScreen(
    reels: List<ReelItem>,
    products: List<Product>,
    isUploading: Boolean,
    uploadProgress: Int,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var reelToEdit by remember { mutableStateOf<ReelItem?>(null) }
    var reelToDelete by remember { mutableStateOf<ReelItem?>(null) }
    var isUploadDialogOpen by remember { mutableStateOf(false) }

    val filteredReels = when (selectedFilter) {
        "PUBLISHED" -> reels.filter { it.isPublished }
        "DRAFT" -> reels.filter { !it.isPublished }
        "LINKED" -> reels.filter { it.isProductLinked }
        else -> reels
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .testTag("admin_reels_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Header stats
            item {
                ReelStatsCard(reels = reels)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "All (${reels.size})",
                        isSelected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        label = "Published (${reels.count { it.isPublished }})",
                        isSelected = selectedFilter == "PUBLISHED",
                        onClick = { selectedFilter = "PUBLISHED" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        label = "Drafts (${reels.count { !it.isPublished }})",
                        isSelected = selectedFilter == "DRAFT",
                        onClick = { selectedFilter = "DRAFT" },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Reels List
            if (filteredReels.isEmpty()) {
                item {
                    Surface(
                        color = BlackSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BlackBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No reels in this category",
                                color = TextMain,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Upload Reel' to add your first vertical video showcase.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredReels, key = { it.id }) { reel ->
                    AdminReelItemCard(
                        reel = reel,
                        onTogglePublish = { adminViewModel.toggleReelPublish(reel) },
                        onEdit = { reelToEdit = reel },
                        onDelete = { reelToDelete = reel }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Bottom breathing room
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Upload Reel
        FloatingActionButton(
            onClick = { isUploadDialogOpen = true },
            containerColor = GoldPrimary,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("admin_upload_reel_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload Reel",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Upload Reel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Upload / Create Dialog
        if (isUploadDialogOpen) {
            ReelEditDialog(
                reel = null,
                products = products,
                isUploading = isUploading,
                uploadProgress = uploadProgress,
                onDismiss = { isUploadDialogOpen = false },
                onSave = { newReel ->
                    adminViewModel.createReel(newReel)
                    isUploadDialogOpen = false
                },
                adminViewModel = adminViewModel
            )
        }

        // Edit Dialog
        reelToEdit?.let { reel ->
            ReelEditDialog(
                reel = reel,
                products = products,
                isUploading = isUploading,
                uploadProgress = uploadProgress,
                onDismiss = { reelToEdit = null },
                onSave = { updated ->
                    adminViewModel.updateReel(updated)
                    reelToEdit = null
                },
                adminViewModel = adminViewModel
            )
        }

        // Delete Confirmation Dialog
        reelToDelete?.let { reel ->
            AlertDialog(
                onDismissRequest = { reelToDelete = null },
                containerColor = BlackSurface,
                title = {
                    Text(
                        text = "Delete Reel?",
                        color = TextMain,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete '${reel.title}'? This will permanently remove the reel metadata from Firestore.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            adminViewModel.deleteReel(reel.id)
                            reelToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reelToDelete = null }) {
                        Text("Cancel", color = TextMain)
                    }
                }
            )
        }
    }
}

/**
 * Top Card summarizing Reel performance.
 */
@Composable
private fun ReelStatsCard(reels: List<ReelItem>) {
    Surface(
        color = BlackSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GoldBorder.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reels & Video Showcases",
                        color = GoldHighlight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cloud-synced via Firebase Storage & Firestore",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    color = GoldDark.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldPrimary)
                ) {
                    Text(
                        text = "${reels.size} Total",
                        color = GoldHighlight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BlackBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "Published", value = "${reels.count { it.isPublished }}")
                StatItem(label = "Total Views", value = "${reels.sumOf { it.viewsCount }}")
                StatItem(label = "Total Likes", value = "${reels.sumOf { it.likesCount }}")
                StatItem(label = "Linked Products", value = "${reels.count { it.isProductLinked }}")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = TextMain,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) GoldPrimary else BlackSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSelected) GoldHighlight else BlackBorder),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("filter_chip_$label")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.Black else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Single Reel card in the Admin list.
 */
@Composable
private fun AdminReelItemCard(
    reel: ReelItem,
    onTogglePublish: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = BlackSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BlackBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_reel_card_${reel.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thumbnail with Duration Overlay
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
            ) {
                if (reel.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = reel.thumbnailUrl,
                        contentDescription = reel.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Duration badge
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "${reel.durationSeconds}s",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Surface(
                        color = if (reel.isPublished) Color(0xFF14532D) else Color(0xFF374151),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (reel.isPublished) "PUBLISHED" else "DRAFT",
                            color = if (reel.isPublished) Color(0xFF86EFAC) else Color(0xFFD1D5DB),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Publish Switch
                    Switch(
                        checked = reel.isPublished,
                        onCheckedChange = { onTogglePublish() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BlackBorder
                        ),
                        modifier = Modifier.size(width = 36.dp, height = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reel.title,
                    color = TextMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Linked Product Tag
                if (reel.isProductLinked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = GoldHighlight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reel.productName ?: "Linked Product",
                            color = GoldHighlight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats: Likes & Shares
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${reel.likesCount}", color = TextSecondary, fontSize = 11.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Shares",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${reel.sharesCount}", color = TextSecondary, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Edit & Delete Icon Buttons
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Reel",
                            tint = TextMain,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Reel",
                            tint = ColorError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for uploading new reel or editing existing reel details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelEditDialog(
    reel: ReelItem?,
    products: List<Product>,
    isUploading: Boolean,
    uploadProgress: Int,
    onDismiss: () -> Unit,
    onSave: (ReelItem) -> Unit,
    adminViewModel: AdminViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(reel?.title ?: "") }
    var caption by remember { mutableStateOf(reel?.caption ?: "") }
    var videoUrl by remember { mutableStateOf(reel?.videoUrl ?: "") }
    var thumbnailUrl by remember { mutableStateOf(reel?.thumbnailUrl ?: "") }
    var isPublished by remember { mutableStateOf(reel?.isPublished ?: true) }
    var selectedProductId by remember { mutableStateOf(reel?.productId ?: "") }

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isExtractingThumbnail by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    // Android Photo/Video Picker (Google Play Zero-Permission Standard)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val sizeValidation = FirebaseStorageService.validateVideoSize(context, uri)
            if (sizeValidation.isFailure) {
                errorMessage = sizeValidation.exceptionOrNull()?.message
            } else {
                selectedVideoUri = uri
                errorMessage = null
                videoUrl = uri.toString()

                // Generate thumbnail from first frame
                isExtractingThumbnail = true
                coroutineScope.launch {
                    val thumb = FirebaseStorageService.generateVideoThumbnail(context, uri)
                    isExtractingThumbnail = false
                    if (thumb != null && thumbnailUrl.isBlank()) {
                        // Use a fallback or thumbnail indicator
                        thumbnailUrl = "generated://thumb_${UUID.randomUUID()}"
                    }
                }
            }
        }
    }

    val selectedProduct = products.find { it.id == selectedProductId }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        containerColor = BlackSurface,
        title = {
            Text(
                text = if (reel == null) "Upload New Reel" else "Edit Reel",
                color = GoldHighlight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Video File Selection or URL
                Surface(
                    color = BlackSurfaceSecondary,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BlackBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Video Source (Max 50MB)",
                                color = TextMain,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Button(
                                onClick = {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                enabled = !isUploading,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoFile,
                                    contentDescription = "Pick Video",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Choose Video", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (selectedVideoUri != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Selected: ${selectedVideoUri?.lastPathSegment?.takeLast(24) ?: "Local Video"}",
                                    color = Color(0xFF86EFAC),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Fallback URL input
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("Video URL / Path") },
                    placeholder = { Text("e.g. raw://videoplayback or https://...") },
                    singleLine = true,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("e.g. OnePlus 12 5G Unboxing") },
                    singleLine = true,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                // Caption
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption & Store Offers") },
                    placeholder = { Text("Describe the phone, features, store discounts...") },
                    maxLines = 3,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                // Thumbnail URL (optional)
                OutlinedTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    label = { Text("Thumbnail Poster Image URL (Optional)") },
                    singleLine = true,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogTextFieldColors()
                )

                // Linked Product Selector
                ExposedDropdownMenuBox(
                    expanded = productDropdownExpanded,
                    onExpandedChange = { if (!isUploading) productDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.let { "${it.name} (₹${it.price})" } ?: "None (General Showcase)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link Store Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = dialogTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = productDropdownExpanded,
                        onDismissRequest = { productDropdownExpanded = false },
                        modifier = Modifier.background(BlackSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (General Showcase)", color = TextMain) },
                            onClick = {
                                selectedProductId = ""
                                productDropdownExpanded = false
                            }
                        )
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(prod.name, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("₹${prod.price} • ${prod.brand}", color = GoldHighlight, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    selectedProductId = prod.id
                                    productDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Publish Status Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPublished) "Published to Customers" else "Save as Draft (Admin Only)",
                        color = if (isPublished) Color(0xFF86EFAC) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it },
                        enabled = !isUploading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GoldPrimary
                        )
                    )
                }

                // Error Notice if Any
                if (errorMessage != null) {
                    Surface(
                        color = ColorError.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ColorError)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ColorError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Upload Progress Bar
                if (isUploading) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Uploading to Firebase Storage...",
                                color = GoldHighlight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$uploadProgress%",
                                color = GoldHighlight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GoldPrimary,
                            trackColor = BlackBorder
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a title for the reel"
                        return@Button
                    }
                    if (videoUrl.isBlank() && selectedVideoUri == null) {
                        errorMessage = "Please select a video file or enter a video URL"
                        return@Button
                    }

                    // If a new local video was selected, initiate Firebase Storage upload
                    if (selectedVideoUri != null) {
                        adminViewModel.setUploadingProgress(true, 0)
                        coroutineScope.launch {
                            val uploadResult = FirebaseStorageService.uploadReelVideo(
                                context = context,
                                videoUri = selectedVideoUri!!,
                                reelId = reel?.id ?: "reel-${UUID.randomUUID()}"
                            ) { progress, _, _ ->
                                adminViewModel.setUploadingProgress(true, progress)
                            }

                            adminViewModel.setUploadingProgress(false, 0)

                            if (uploadResult.isSuccess) {
                                val downloadUrl = uploadResult.getOrThrow()
                                val newReel = (reel ?: ReelItem(id = "reel-${System.currentTimeMillis()}")).copy(
                                    title = title,
                                    caption = caption,
                                    videoUrl = downloadUrl,
                                    thumbnailUrl = thumbnailUrl.ifBlank { selectedProduct?.images?.firstOrNull() ?: "" },
                                    productId = selectedProduct?.id,
                                    productName = selectedProduct?.name,
                                    productPrice = selectedProduct?.price,
                                    productImage = selectedProduct?.images?.firstOrNull(),
                                    isPublished = isPublished
                                )
                                onSave(newReel)
                            } else {
                                // Provide fallback option using the current URL
                                errorMessage = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                                Toast.makeText(context, "Storage upload notice: saving with direct URL fallback", Toast.LENGTH_SHORT).show()
                                val fallbackReel = (reel ?: ReelItem(id = "reel-${System.currentTimeMillis()}")).copy(
                                    title = title,
                                    caption = caption,
                                    videoUrl = videoUrl.ifBlank { "raw://videoplayback" },
                                    thumbnailUrl = thumbnailUrl.ifBlank { selectedProduct?.images?.firstOrNull() ?: "" },
                                    productId = selectedProduct?.id,
                                    productName = selectedProduct?.name,
                                    productPrice = selectedProduct?.price,
                                    productImage = selectedProduct?.images?.firstOrNull(),
                                    isPublished = isPublished
                                )
                                onSave(fallbackReel)
                            }
                        }
                    } else {
                        // Direct URL update
                        val updated = (reel ?: ReelItem(id = "reel-${System.currentTimeMillis()}")).copy(
                            title = title,
                            caption = caption,
                            videoUrl = videoUrl,
                            thumbnailUrl = thumbnailUrl.ifBlank { selectedProduct?.images?.firstOrNull() ?: "" },
                            productId = selectedProduct?.id,
                            productName = selectedProduct?.name,
                            productPrice = selectedProduct?.price,
                            productImage = selectedProduct?.images?.firstOrNull(),
                            isPublished = isPublished
                        )
                        onSave(updated)
                    }
                },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Uploading...", color = Color.Black)
                } else {
                    Text(
                        text = if (reel == null) "Publish Reel" else "Save Changes",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (!isUploading) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextMain)
                }
            }
        }
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoldPrimary,
    unfocusedBorderColor = BlackBorder,
    focusedLabelColor = GoldHighlight,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextMain,
    unfocusedTextColor = TextMain,
    cursorColor = GoldPrimary
)
