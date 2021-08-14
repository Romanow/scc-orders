package ru.romanow.orders.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.romanow.orders.model.OrderInfoResponse;
import ru.romanow.orders.model.OrderRequest;
import ru.romanow.orders.service.OrderManageService;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrdersController {
    private final OrderManageService orderService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Void> makeOrder(@Valid @RequestBody OrderRequest request) {
        final UUID orderUid = orderService.makeOrder(request);
        return ResponseEntity.created(URI.create("/api/v1/" + orderUid + "/status")).build();
    }

    @GetMapping(value = "/{orderUid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrderInfoResponse status(@PathVariable UUID orderUid) {
        return orderService.status(orderUid);
    }

    @PostMapping(value = "/{orderUid}/process", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public OrderInfoResponse process(@PathVariable UUID orderUid) {
        return orderService.process(orderUid);
    }
}
