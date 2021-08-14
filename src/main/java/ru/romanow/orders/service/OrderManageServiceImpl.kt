package ru.romanow.orders.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import ru.romanow.orders.domain.Order
import ru.romanow.orders.exceptions.RestRequestException
import ru.romanow.orders.model.*
import java.util.*

@Service
class OrderManageServiceImpl(
    private val restTemplate: RestTemplate,
    private val orderService: OrderService,
    @Value("\${warehouse.service.url}")
    private val warehouseUrl: String,
    @Value("\${delivery.service.url}")
    private val deliveryUrl: String,
) : OrderManageService {

    override fun makeOrder(request: OrderRequest): UUID {
        val orderUid: UUID = UUID.randomUUID()
        val takeItemRequest = TakeItemsRequest(request.itemUids)
        val url = "$warehouseUrl/api/v1/items/$orderUid/take"
        try {
            restTemplate.postForObject(url, takeItemRequest, OrderItemResponse::class.java)
        } catch (exception: RestClientResponseException) {
            throw RestRequestException("Error request to '$url': ${exception.rawStatusCode}:${exception.responseBodyAsString}")
        }
        orderService.createOrder(orderUid, request)
        return orderUid
    }

    override fun status(orderUid: UUID): OrderInfoResponse {
        val order = orderService.getOrderByUid(orderUid)
        val orderInfo: OrderItemResponse = makeWarehouseStateRequest(orderUid)
        return buildOrderInfoResponse(order, orderInfo)
    }

    override fun process(orderUid: UUID): OrderInfoResponse {
        val order = orderService.getOrderByUid(orderUid)
        val orderInfo: OrderItemResponse = makeWarehouseStateRequest(orderUid)
        makeDeliveryRequest(orderUid, order.firstName!!, order.lastName!!, order.address!!)
        return buildOrderInfoResponse(order, orderInfo)
    }

    private fun makeWarehouseStateRequest(orderUid: UUID?): OrderItemResponse {
        val url = "$warehouseUrl/api/v1/items/$orderUid/state"
        return try {
            Optional.ofNullable(restTemplate.getForObject(url, OrderItemResponse::class.java))
                .orElseThrow { RestRequestException("Warehouse returned empty response") }
        } catch (exception: RestClientResponseException) {
            throw RestRequestException("Error request to '$url': ${exception.rawStatusCode}:${exception.responseBodyAsString}")
        }
    }

    private fun makeDeliveryRequest(orderUid: UUID, firstName: String, lastName: String, address: String) {
        val url = "$deliveryUrl/api/v1/items/$orderUid/deliver"
        try {
            val deliveryRequest = DeliveryRequest(firstName, lastName, address)
            restTemplate.postForObject(url, deliveryRequest, Void::class.java)
        } catch (exception: RestClientResponseException) {
            throw RestRequestException("Error request to '$url': ${exception.rawStatusCode}:${exception.responseBodyAsString}")
        }
    }

    private fun buildOrderInfoResponse(order: Order, orderInfo: OrderItemResponse) =
        OrderInfoResponse(
            orderUid = order.uid!!,
            state = orderInfo.state,
            items = orderInfo.items,
            firstName = order.firstName,
            lastName = order.lastName,
            address = order.address
        )
}