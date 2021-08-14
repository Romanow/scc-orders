package ru.romanow.orders.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.romanow.orders.domain.Order
import java.util.*

interface OrdersRepository : JpaRepository<Order, Int> {
    fun findByUid(orderUid: UUID): Optional<Order>
}