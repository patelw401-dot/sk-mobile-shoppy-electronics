package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun rememberImageModel(src: String): Any {
    return remember(src) {
        resolveImageModel(src)
    }
}

fun resolveImageModel(src: String): Any {
    val trimmed = src.trim()
    return if (trimmed.startsWith("data:image/", ignoreCase = true) && trimmed.contains("base64,", ignoreCase = true)) {
        try {
            val base64Part = trimmed.substringAfter("base64,")
            Base64.decode(base64Part, Base64.DEFAULT)
        } catch (_: Exception) {
            trimmed
        }
    } else {
        trimmed
    }
}

suspend fun compressUriToDataUri(
    context: Context,
    uri: Uri,
    maxDimension: Int = 512,
    jpegQuality: Int = 80
): String? = withContext(Dispatchers.IO) {
    try {
        val resolver = context.contentResolver

        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        val rawWidth = boundsOptions.outWidth
        val rawHeight = boundsOptions.outHeight
        if (rawWidth <= 0 || rawHeight <= 0) {
            return@withContext null
        }

        var inSampleSize = 1
        while ((rawWidth / inSampleSize) >= maxDimension * 2 && (rawHeight / inSampleSize) >= maxDimension * 2) {
            inSampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val decodedBitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return@withContext null

        val currentMax = max(decodedBitmap.width, decodedBitmap.height)
        val finalBitmap = if (currentMax > maxDimension) {
            val scale = maxDimension.toFloat() / currentMax.toFloat()
            val targetWidth = max(1, (decodedBitmap.width * scale).roundToInt())
            val targetHeight = max(1, (decodedBitmap.height * scale).roundToInt())
            Bitmap.createScaledBitmap(decodedBitmap, targetWidth, targetHeight, true).also {
                if (it !== decodedBitmap) {
                    decodedBitmap.recycle()
                }
            }
        } else {
            decodedBitmap
        }

        val outputStream = ByteArrayOutputStream()
        var quality = jpegQuality
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var compressedBytes = outputStream.toByteArray()
        while (compressedBytes.size > 220_000 && quality > 45) {
            quality -= 15
            outputStream.reset()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
        }
        finalBitmap.recycle()

        val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    } catch (_: Exception) {
        null
    }
}
