package com.example.model

/**
 * Represents a short-form vertical video Reel showcasing smartphones,
 * electronics, repair tutorials, and customer reviews.
 */
data class ReelItem(
    val id: String = "",
    val title: String = "",
    val caption: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val productId: String? = null,
    val productName: String? = null,
    val productPrice: Int? = null,
    val productImage: String? = null,
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Int = 0,
    val isPublished: Boolean = true,
    val uploadedAt: Long = System.currentTimeMillis(),
    val uploadedBy: String = "skmobileshoppyandelectronics@gmail.com",
    val durationSeconds: Int = 15,
    val isLikedByMe: Boolean = false
) {
    val isProductLinked: Boolean
        get() = !productId.isNullOrBlank() && !productName.isNullOrBlank()
}
