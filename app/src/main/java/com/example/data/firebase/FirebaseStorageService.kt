package com.example.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FirebaseStorageService {
    private const val TAG = "FirebaseStorageService"
    const val MAX_VIDEO_BYTES = 50L * 1024L * 1024L // 50 MB limit

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    /**
     * Validates video size before attempting upload.
     */
    fun validateVideoSize(context: Context, videoUri: Uri): Result<Long> {
        return try {
            val cursor = context.contentResolver.query(videoUri, null, null, null, null)
            val size = cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1) it.getLong(sizeIndex) else null
                } else null
            } ?: context.contentResolver.openInputStream(videoUri)?.use { it.available().toLong() } ?: 0L

            if (size > MAX_VIDEO_BYTES) {
                Result.failure(IllegalArgumentException("Video exceeds 50MB limit (${size / (1024 * 1024)}MB). Please choose a shorter clip."))
            } else {
                Result.success(size)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not check file size directly, allowing upload to proceed: ${e.message}")
            Result.success(0L)
        }
    }

    /**
     * Uploads video to Firebase Storage at `reels/{reelId}.mp4` with progress callback.
     */
    suspend fun uploadReelVideo(
        context: Context,
        videoUri: Uri,
        reelId: String = UUID.randomUUID().toString(),
        onProgress: (percentage: Int, bytesTransferred: Long, totalBytes: Long) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First extract content type or default to video/mp4
            val mimeType = context.contentResolver.getType(videoUri) ?: "video/mp4"
            val ref = storage.reference.child("reels").child("${reelId}.mp4")

            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .setCustomMetadata("reelId", reelId)
                .setCustomMetadata("uploadedBy", "SK Mobile Admin")
                .build()

            val uploadTask = ref.putFile(videoUri, metadata)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val total = taskSnapshot.totalByteCount
                val transferred = taskSnapshot.bytesTransferred
                if (total > 0) {
                    val progress = ((transferred * 100) / total).toInt()
                    onProgress(progress, transferred, total)
                }
            }

            // Wait for completion
            uploadTask.await()

            // Fetch permanent HTTPS download URL
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.i(TAG, "Reel video uploaded successfully: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Storage upload failed: ${e.message}", e)
            val cleanMsg = when {
                e.message?.contains("permission", ignoreCase = true) == true ->
                    "Firebase Storage Permission Denied. Ensure you are signed in with an authorized admin account."
                e.message?.contains("quota", ignoreCase = true) == true || e.message?.contains("billing", ignoreCase = true) == true ->
                    "Firebase Storage quota or billing limit reached. Upgrade to Blaze plan in Firebase Console."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network failure during video upload. Please check connection and try again."
                else ->
                    "Video upload failed: ${e.localizedMessage ?: "Unknown error"}. You can enter a direct video URL as fallback."
            }
            Result.failure(Exception(cleanMsg, e))
        }
    }

    /**
     * Extracts first frame thumbnail from video Uri.
     */
    suspend fun generateVideoThumbnail(context: Context, videoUri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            Log.w(TAG, "Failed to generate thumbnail: ${e.message}")
            null
        }
    }
}
