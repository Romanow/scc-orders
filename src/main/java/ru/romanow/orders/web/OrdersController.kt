package ru.romanow.orders.web

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.web.bind.annotation.*
import ru.romanow.orders.model.OrderInfoResponse
import ru.romanow.orders.model.OrderRequest
import ru.romanow.orders.service.OrderManageService
import java.net.URI
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/orders")
class OrdersController(
    private val orderService: OrderManageService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun makeOrder(@Valid @RequestBody request: OrderRequest): ResponseEntity<Void> {
        val orderUid = orderService.makeOrder(request)
        return created(URI.create("/api/v1/$orderUid/status")).build()
    }

    @GetMapping(value = ["/{orderUid}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun status(@PathVariable orderUid: UUID): OrderInfoResponse {
        return orderService.status(orderUid)
    }

    @PostMapping(value = ["/{orderUid}/process"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun process(@PathVariable orderUid: UUID): OrderInfoResponse {
        return orderService.process(orderUid)
    }
}