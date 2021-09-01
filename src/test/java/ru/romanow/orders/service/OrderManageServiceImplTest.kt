package ru.romanow.orders.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphabetic
import ru.romanow.orders.domain.Order
import ru.romanow.orders.model.OrderRequest
import ru.romanow.orders.model.enums.OrderState
import java.util.*
import kotlin.random.Random.Default.nextInt

@SpringBootTest(
    classes = [OrderManageServiceImplTest.TestConfiguration::class],
    properties = [
        "warehouse.service.url=http://localhost:8070",
        "delivery.service.url=http://localhost:8090"
    ]
)
@AutoConfigureStubRunner(
    ids = [
        "ru.romanow.scc:warehouse:[1.0.0,2.0.0):stubs:8070",
        "ru.romanow.scc:delivery:[1.0.0,2.0.0):stubs:8090"
    ],
    mappingsOutputFolder = "build/mappings",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
internal class OrderManageServiceImplTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderManageService: OrderManageService

    @Test
    fun makeOrder() {
        val request = OrderRequest(
            itemUids = listOf(
                LEGO_TECHNIC_42082_ITEM_UID,
                LEGO_TECHNIC_42115_ITEM_UID
            ),
            firstName = randomAlphabetic(8),
            lastName = randomAlphabetic(8),
            address = randomAlphabetic(8)
        )
        val orderUid = orderManageService.makeOrder(request)
        assertThat(orderUid).isNotNull
    }

    @Test
    fun status() {
        val orderUid = ORDER_UID_SUCCESS
        val items = listOf(
            LEGO_TECHNIC_42082_ITEM_UID,
            LEGO_TECHNIC_42115_ITEM_UID
        )
        val order = Order(
            id = nextInt(),
            uid = orderUid,
            items = items,
            firstName = randomAlphabetic(10),
            lastName = randomAlphabetic(10),
            address = randomAlphabetic(10)
        )
        `when`(orderService.getOrderByUid(orderUid)).thenReturn(order)

        val response = orderManageService.status(orderUid)

        assertThat(response).isNotNull
        assertThat(response.state).isEqualTo(OrderState.READY_FOR_DELIVERY)
        assertThat(response.orderUid).isEqualTo(order.uid)
        assertThat(response.firstName).isEqualTo(order.firstName)
        assertThat(response.lastName).isEqualTo(order.lastName)
        assertThat(response.address).isEqualTo(order.address)
        assertThat(response.items?.map { it.itemUid })
            .containsExactlyInAnyOrder(* items.toTypedArray())
    }

    @Test
    fun process() {
        val orderUid = ORDER_UID_SUCCESS
        val items = listOf(
            LEGO_TECHNIC_42082_ITEM_UID,
            LEGO_TECHNIC_42115_ITEM_UID
        )
        val order = Order(
            id = nextInt(),
            uid = orderUid,
            items = items,
            firstName = randomAlphabetic(10),
            lastName = randomAlphabetic(10),
            address = randomAlphabetic(10)
        )
        `when`(orderService.getOrderByUid(orderUid)).thenReturn(order)

        val response = orderManageService.process(orderUid)
        assertThat(response).isNotNull
        assertThat(response.state).isEqualTo(OrderState.READY_FOR_DELIVERY)
        assertThat(response.orderUid).isEqualTo(order.uid)
        assertThat(response.firstName).isEqualTo(order.firstName)
        assertThat(response.lastName).isEqualTo(order.lastName)
        assertThat(response.address).isEqualTo(order.address)
        assertThat(response.items?.map { it.itemUid })
            .containsExactlyInAnyOrder(* items.toTypedArray())
    }

    companion object {
        private val ORDER_UID_SUCCESS = UUID.fromString("3affedc8-7338-4f5c-9462-b3579ec84652")
        private val LEGO_TECHNIC_42082_ITEM_UID = UUID.fromString("667c15c8-09eb-4a53-8d4c-69ce70ba2ba9")
        private val LEGO_TECHNIC_42115_ITEM_UID = UUID.fromString("61b6fff3-6192-4488-8622-3bd6402ee49f")
    }

    @Configuration
    internal class TestConfiguration {

        @Bean
        fun restTemplate() = RestTemplate()

        @Bean
        fun orderService(): OrderService = mock(OrderService::class.java)

        @Bean
        fun orderManageService(
            @Value("\${warehouse.service.url}") warehouseUrl: String,
            @Value("\${delivery.service.url}") deliveryUrl: String
        ) = OrderManageServiceImpl(restTemplate(), orderService(), warehouseUrl, deliveryUrl)
    }
}