package ru.romanow.orders.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.created
import org.springframework.web.bind.annotation.*
import ru.romanow.orders.model.ErrorResponse
import ru.romanow.orders.model.OrderInfoResponse
import ru.romanow.orders.model.OrderRequest
import ru.romanow.orders.model.ValidationErrorResponse
import ru.romanow.orders.service.OrderManageService
import java.net.URI
import java.util.*
import javax.validation.Valid

@Tag(name = "Order service")
@RestController
@RequestMapping("/api/v1/orders")
class OrdersController(
    private val orderService: OrderManageService
) {

    @Operation(
        summary = "Create new order",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Order created",
                headers = [Header(name = "Location", description = "Path to newly created order")]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Wrong request",
                content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Request to external system failed",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun makeOrder(@Valid @RequestBody request: OrderRequest): ResponseEntity<Void> {
        val orderUid = orderService.makeOrder(request)
        return created(URI.create("/api/v1/$orderUid/status")).build()
    }

    @Operation(
        summary = "Get information about the order",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Order information",
                content = [Content(schema = Schema(implementation = OrderInfoResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Request to external system failed",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping(value = ["/{orderUid}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun status(@PathVariable orderUid: UUID): OrderInfoResponse {
        return orderService.status(orderUid)
    }

    @Operation(
        summary = "Send order to delivery",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Order information",
                content = [Content(schema = Schema(implementation = OrderInfoResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Request to external system failed",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping(value = ["/{orderUid}/process"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun process(@PathVariable orderUid: UUID): OrderInfoResponse {
        return orderService.process(orderUid)
    }
}