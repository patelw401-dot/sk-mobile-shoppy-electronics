package com.example.model

internal fun ShopOrder.isOwnedByFirebaseUid(uid: String?): Boolean =
    !uid.isNullOrBlank() && customerUid == uid
