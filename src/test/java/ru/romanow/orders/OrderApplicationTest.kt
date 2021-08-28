package ru.romanow.orders

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import ru.romanow.orders.config.DatabaseTestConfiguration
import ru.romanow.orders.web.OrdersController

@ActiveProfiles("test")
@SpringBootTest
@Import(DatabaseTestConfiguration::class)
internal class OrderApplicationTest {

    @Autowired
    private lateinit var ordersController: OrdersController

    @Test
    fun test() {
        assertThat(ordersController).isNotNull
    }
}