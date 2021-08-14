package ru.romanow.orders.service

import com.google.gson.Gson
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import ru.romanow.orders.config.DatabaseTestConfiguration
import ru.romanow.orders.domain.Order
import ru.romanow.orders.exceptions.RestRequestException
import ru.romanow.orders.model.ErrorResponse
import ru.romanow.orders.model.OrderRequest
import ru.romanow.orders.model.enums.OrderState
import java.util.*
import java.util.UUID.randomUUID
import javax.persistence.EntityNotFoundException

@Disabled
@ActiveProfiles("contract-tests")
@SpringBootTest(classes = [OrderManageServiceTest.OrderManageServiceConfiguration::class])
@AutoConfigureStubRunner(
    ids = [
        "ru.romanow.scc:warehouse:[1.0.0,2.0.0):8070",
        "ru.romanow.scc:delivery:[1.0.0,2.0.0):8090"
    ],
    repositoryRoot = "https://dl.bintray.com/ronin/scc-microservices",
    stubsMode = StubRunnerProperties.StubsMode.REMOTE
)
@Import(DatabaseTestConfiguration::class)
internal class OrderManageServiceTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderManageService: OrderManageService

    @Value("\${warehouse.service.url}")
    private lateinit var warehouseUrl: String

    private val gson = Gson()

    @Test
    fun makeOrderSuccess() {
        mockStatic(UUID::class.java).use {
            it.`when`<UUID> { randomUUID() }.thenReturn(ORDER_UID_SUCCESS)

            val items = listOf(randomUUID(), randomUUID())
            val request = OrderRequest(
                firstName = randomAlphabetic(10),
                lastName = randomAlphabetic(10),
                address = randomAlphabetic(10),
                itemUids = items
            )
            val orderUid = orderManageService.makeOrder(request)

            assertThat(ORDER_UID_SUCCESS).isEqualTo(orderUid)
            verify(orderService, times(1)).createOrder(eq(orderUid), eq(request))
        }
    }

    @Test
    fun makeOrderAlreadyExists() {
        mockStatic(UUID::class.java).use {
            it.`when`<UUID> { randomUUID() }.thenReturn(ORDER_UID_ALREADY_EXISTS)

            val items = listOf(randomUUID(), randomUUID())
            val request = OrderRequest(
                firstName = randomAlphabetic(10),
                lastName = randomAlphabetic(10),
                address = randomAlphabetic(10),
                itemUids = items
            )

            assertThrows(RestRequestException::class.java) { orderManageService.makeOrder(request) }
        }
    }

    @Test
    fun makeOrderItemsNotAvailable() {
        mockStatic(UUID::class.java).use {
            it.`when`<UUID> { randomUUID() }.thenReturn(ORDER_UID_NOT_AVAILABLE)

            val items = listOf(randomUUID(), randomUUID())
            val request = OrderRequest(
                firstName = randomAlphabetic(10),
                lastName = randomAlphabetic(10),
                address = randomAlphabetic(10),
                itemUids = items
            )
            assertThrows(RestRequestException::class.java) { orderManageService.makeOrder(request) }
        }
    }

    @Test
    fun makeOrderItemsNotFound() {
        mockStatic(UUID::class.java).use {
            it.`when`<UUID> { randomUUID() }.thenReturn(ORDER_UID_NOT_FOUND)

            val items = listOf(randomUUID(), randomUUID())
            val request = OrderRequest(
                firstName = randomAlphabetic(10),
                lastName = randomAlphabetic(10),
                address = randomAlphabetic(10),
                itemUids = items
            )
            assertThrows(RestRequestException::class.java) { orderManageService.makeOrder(request) }
        }
    }

    @Test
    fun statusSuccess() {
        val itemUids = listOf(randomUUID(), randomUUID())
        `when`(orderService.getOrderByUid(ORDER_UID_SUCCESS))
            .thenReturn(buildOrder(ORDER_UID_SUCCESS, itemUids))

        val status = orderManageService.status(ORDER_UID_SUCCESS)
        assertThat(ORDER_UID_SUCCESS).isEqualTo(status.orderUid)
    }

    @Test
    fun statusOrderNotFound() {
        val orderUid = randomUUID()
        `when`(orderService.getOrderByUid(orderUid))
            .thenThrow(EntityNotFoundException("Order '$orderUid' not found"))
        assertThrows(EntityNotFoundException::class.java) { orderManageService.status(orderUid) }
    }

    @Test
    fun processOrderNotFound() {
        val orderUid = randomUUID()
        `when`(orderService.getOrderByUid(orderUid))
            .thenThrow(EntityNotFoundException("Order '$orderUid' not found"))
        assertThrows(EntityNotFoundException::class.java) { orderManageService.process(orderUid) }
    }

    @Test
    fun processRequestWarehouseError() {
        val orderUid = ORDER_UID_NOT_FOUND
        mockStatic(UUID::class.java).use {
            it.`when`<UUID> { randomUUID() }.thenReturn(orderUid)

            val items = listOf(randomUUID(), randomUUID())
            `when`(orderService.getOrderByUid(orderUid)).thenReturn(buildOrder(orderUid, items))
            try {
                orderManageService.process(orderUid)
            } catch (exception: RestRequestException) {
                val url = "$warehouseUrl/$orderUid/state"
                val responseMessage = gson.toJson(ErrorResponse("OrderItem '$orderUid' not found"))
                val message = "Error request to '$url': ${HttpStatus.NOT_FOUND.value()}:$responseMessage"
                assertThat(message).isEqualTo(exception.message)
                return
            }
            Assertions.fail<Any>()
        }
    }

    @Test
    fun processRequestSuccess() {
        val orderUid = ORDER_UID_SUCCESS
        mockStatic(UUID::class.java).use {
            it.`when`<UUID> { randomUUID() }.thenReturn(orderUid)

            val items = listOf(randomUUID(), randomUUID())
            val order = buildOrder(orderUid, items)
            `when`(orderService.getOrderByUid(orderUid)).thenReturn(order)

            val process = orderManageService.process(orderUid)
            assertThat(ORDER_UID_SUCCESS).isEqualTo(process.orderUid)
            assertThat(OrderState.READY_FOR_DELIVERY).isEqualTo(process.state)
            assertThat(order.firstName).isEqualTo(process.firstName)
            assertThat(order.lastName).isEqualTo(process.lastName)
            assertThat(order.address).isEqualTo(process.address)
            assertThat(items.size).isEqualTo(process.items?.size)
        }
    }

    private fun buildOrder(orderUid: UUID, itemUids: List<UUID>) =
        Order(
            uid = orderUid,
            firstName = randomAlphabetic(10),
            lastName = randomAlphabetic(10),
            address = randomAlphabetic(10),
            items = itemUids
        )

    companion object {
        private val ORDER_UID_SUCCESS = UUID.fromString("1a1f775c-4f31-4256-bec1-c3d4e9bf1b52")
        private val ORDER_UID_NOT_FOUND = UUID.fromString("36856fc6-d6ec-47cb-bbee-d20e78299eb9")
        private val ORDER_UID_NOT_AVAILABLE = UUID.fromString("37bb4049-1d1e-449f-8ada-5422f8886231")
        private val ORDER_UID_ALREADY_EXISTS = UUID.fromString("45142058-60e6-4cde-ad13-b968180f0367")
    }

    @Configuration
    class OrderManageServiceConfiguration {
        @Bean
        fun restTemplate(): RestTemplate = mock(RestTemplate::class.java)

        @Bean
        fun orderService(): OrderService = mock(OrderService::class.java)
    }
}