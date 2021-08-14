package ru.romanow.orders.model

import ru.romanow.orders.model.enums.OrderState
import java.util.*

data class OrderInfoResponse(
    val orderUid: UUID,
    val firstName: String?,
    val lastName: String?,
    val address: String?,
    val state: OrderState,
    val items: List<ItemsInfo>?
)