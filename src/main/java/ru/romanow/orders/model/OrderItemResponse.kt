package ru.romanow.orders.model

import ru.romanow.orders.model.enums.OrderState
import java.util.*

data class OrderItemResponse(
    val orderUid: UUID,
    val state: OrderState,
    val items: List<ItemsInfo>
)