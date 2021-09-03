package ru.romanow.orders.web

import com.google.gson.Gson
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.transaction.annotation.Transactional
import ru.romanow.orders.config.DatabaseTestConfiguration
import ru.romanow.orders.domain.Order
import ru.romanow.orders.model.OrderRequest
import ru.romanow.orders.model.enums.OrderState
import ru.romanow.orders.repository.OrdersRepository
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureStubRunner(
    ids = [
        "ru.romanow.scc:warehouse:[1.0.0,2.0.0):stubs:8070",
        "ru.romanow.scc:delivery:[1.0.0,2.0.0):stubs:8090"
    ],
    repositoryRoot = "https://romanow.jfrog.io/artifactory/scc-libs-release/",
    mappingsOutputFolder = "build/mappings",
    stubsMode = StubRunnerProperties.StubsMode.REMOTE
)
@Transactional
@AutoConfigureTestEntityManager
@AutoConfigureMockMvc
@Import(DatabaseTestConfiguration::class)
internal class OrdersControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var ordersRepository: OrdersRepository

    @BeforeEach
    fun init() {
        val order = Order(
            uid = ORDER_UID_SUCCESS,
            items = listOf(
                LEGO_TECHNIC_42082_ITEM_UID,
                LEGO_TECHNIC_42115_ITEM_UID
            ),
            firstName = FIRST_NAME,
            lastName = LAST_NAME,
            address = DELIVERY_ADDRESS
        )
        ordersRepository.save(order)
    }

    @Test
    fun makeOrder() {
        val request = OrderRequest(
            itemUids = listOf(
                LEGO_TECHNIC_42082_ITEM_UID,
                LEGO_TECHNIC_42115_ITEM_UID
            ),
            firstName = FIRST_NAME,
            lastName = LAST_NAME,
            address = DELIVERY_ADDRESS
        )
        mockMvc.perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Gson().toJson(request))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(header().exists(HttpHeaders.LOCATION))
    }

    @Test
    fun status() {
        mockMvc
            .perform(get("/api/v1/orders/$ORDER_UID_SUCCESS/status"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.state").value(OrderState.READY_FOR_DELIVERY.name))
            .andExpect(jsonPath("$.orderUid").value(ORDER_UID_SUCCESS.toString()))
            .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(LAST_NAME))
            .andExpect(jsonPath("$.address").value(DELIVERY_ADDRESS))
            .andExpect(jsonPath("$.items").isArray)
            .andExpect(
                jsonPath(
                    "$.items[*].itemUid",
                    containsInAnyOrder(
                        LEGO_TECHNIC_42082_ITEM_UID.toString(),
                        LEGO_TECHNIC_42115_ITEM_UID.toString()
                    )
                )
            )
    }

    @Test
    fun process() {
        mockMvc
            .perform(post("/api/v1/orders/$ORDER_UID_SUCCESS/process"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.state").value(OrderState.READY_FOR_DELIVERY.name))
            .andExpect(jsonPath("$.orderUid").value(ORDER_UID_SUCCESS.toString()))
            .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(LAST_NAME))
            .andExpect(jsonPath("$.address").value(DELIVERY_ADDRESS))
            .andExpect(jsonPath("$.items").isArray)
            .andExpect(
                jsonPath(
                    "$.items[*].itemUid",
                    containsInAnyOrder(
                        LEGO_TECHNIC_42082_ITEM_UID.toString(),
                        LEGO_TECHNIC_42115_ITEM_UID.toString()
                    )
                )
            )
    }

    companion object {
        private val ORDER_UID_SUCCESS = UUID.fromString("3affedc8-7338-4f5c-9462-b3579ec84652")
        private val LEGO_TECHNIC_42082_ITEM_UID = UUID.fromString("667c15c8-09eb-4a53-8d4c-69ce70ba2ba9")
        private val LEGO_TECHNIC_42115_ITEM_UID = UUID.fromString("61b6fff3-6192-4488-8622-3bd6402ee49f")
        private const val FIRST_NAME = "Alex"
        private const val LAST_NAME = "Romanow"
        private const val DELIVERY_ADDRESS = "Moscow"
    }
}