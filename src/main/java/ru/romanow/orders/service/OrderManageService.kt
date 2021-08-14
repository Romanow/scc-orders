package ru.romanow.orders.service

import ru.romanow.orders.model.OrderInfoResponse
import ru.romanow.orders.model.OrderRequest
import java.util.*
import javax.annotation.Nonnull

interface OrderManageService {
    fun makeOrder(request: OrderRequest): UUID
    fun status(orderUid: UUID): OrderInfoResponse
    fun process(orderUid: UUID): OrderInfoResponse
}