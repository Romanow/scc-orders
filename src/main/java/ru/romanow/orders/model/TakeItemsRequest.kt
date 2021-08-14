package ru.romanow.orders.model

import java.util.*

data class TakeItemsRequest(
    val itemsUid: List<UUID>
)