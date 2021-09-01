package ru.romanow.orders.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic
import ru.romanow.orders.domain.Order
import ru.romanow.orders.model.OrderRequest
import ru.romanow.orders.repository.OrdersRepository
import java.util.*
import java.util.UUID.randomUUID
import kotlin.random.Random.Default.nextInt

internal class OrderServiceImplTest {
    private val ordersRepository: OrdersRepository = mock(OrdersRepository::class.java)
    private val orderService = OrderServiceImpl(ordersRepository)

    @Test
    fun createOrder() {
        val orderUid = randomUUID()
        val request = OrderRequest(
            firstName = randomAlphabetic(8),
            lastName = randomAlphabetic(8),
            address = randomAlphabetic(8),
            itemUids = listOf(randomUUID())
        )
        orderService.createOrder(orderUid, request)
        verify(ordersRepository).save(any(Order::class.java))
    }

    @Test
    fun getOrderByUid() {
        val orderUid = randomUUID()
        val order = Order(
            id = nextInt(),
            uid = orderUid,
            firstName = randomAlphabetic(8),
            lastName = randomAlphabetic(8),
            address = randomAlphabetic(8)
        )
        `when`(ordersRepository.findByUid(orderUid)).thenReturn(Optional.of(order))
        val orderByUid = orderService.getOrderByUid(orderUid)
        assertThat(orderByUid).isEqualTo(order)
    }

    private fun <T> any(cls: Class<T>): T = Mockito.any(cls)
}