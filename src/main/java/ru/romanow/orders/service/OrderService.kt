package ru.romanow.orders.service

import ru.romanow.orders.domain.Order
import ru.romanow.orders.model.OrderRequest
import java.util.*
import javax.annotation.Nonnull

interface OrderService {
    fun createOrder(orderUid: UUID, request: OrderRequest)
    fun getOrderByUid(orderUid: UUID): Order
}