package ru.romanow.orders.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.romanow.orders.domain.Order
import ru.romanow.orders.model.OrderRequest
import ru.romanow.orders.repository.OrdersRepository
import java.util.*
import javax.annotation.Nonnull
import javax.persistence.EntityNotFoundException

@Service
class OrderServiceImpl(
    private val ordersRepository: OrdersRepository
) : OrderService {

    @Transactional
    override fun createOrder(orderUid: UUID, request: OrderRequest) {
        val order = Order(
            uid = orderUid,
            items = request.itemUids,
            firstName = request.firstName,
            lastName = request.lastName,
            address = request.address
        )
        ordersRepository.save(order)
    }

    @Nonnull
    @Transactional(readOnly = true)
    override fun getOrderByUid(orderUid: UUID): Order =
        ordersRepository.findByUid(orderUid)
            .orElseThrow { EntityNotFoundException("Order '$orderUid' not found") }
}