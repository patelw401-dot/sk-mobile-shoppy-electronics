package com.example.data.service

import com.example.model.ShopOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Admin auth and snapshot callbacks update this cache on the main thread. */
internal class AdminOrderCache {
    private val currentOrders = MutableStateFlow<List<ShopOrder>>(emptyList())
    private var session = 0L

    val orders = currentOrders.asStateFlow()

    fun reset(): Long {
        session++
        currentOrders.value = emptyList()
        return session
    }

    fun isCurrent(session: Long): Boolean = this.session == session

    fun replace(session: Long, orders: List<ShopOrder>) {
        if (isCurrent(session)) currentOrders.value = orders
    }
}
