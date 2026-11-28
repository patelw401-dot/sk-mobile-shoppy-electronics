package com.example.data.service

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun orderTimestampMillis(value: Any?): Long? = when (value) {
    is Timestamp -> value.toDate().time
    is Number -> value.toLong()
    else -> null
}

internal fun firstOrderText(primary: Any?, alternate: Any?): String =
    (primary as? String)?.takeIf { it.isNotBlank() } ?: (alternate as? String).orEmpty()

internal fun formatOrderDate(orderDate: String?, createdAt: Long?): String =
    orderDate?.takeIf { it.isNotBlank() } ?: createdAt?.let {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it))
    } ?: "Today"
