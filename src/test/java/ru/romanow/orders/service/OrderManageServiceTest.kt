package ru.romanow.orders.service;

import groovy.json.JsonOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.romanow.orders.OrdersTestConfiguration;
import ru.romanow.orders.domain.Order;
import ru.romanow.orders.exceptions.RestRequestException;
import ru.romanow.orders.model.ErrorResponse;
import ru.romanow.orders.model.OrderInfoResponse;
import ru.romanow.orders.model.OrderRequest;
import ru.romanow.orders.model.enums.OrderState;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static groovy.json.JsonOutput.toJson;
import static java.lang.String.format;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OrdersTestConfiguration.class)
@AutoConfigureStubRunner(
        ids = {
                "ru.romanow.scc:warehouse:[1.0.0,2.0.0):8070",
                "ru.romanow.scc:delivery:[1.0.0,2.0.0):8090"
        },
        repositoryRoot = "https://dl.bintray.com/ronin/scc-microservices",
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
class OrderManageServiceImplTest {
    private static final String WAREHOUSE_URL = "http://warehouse:8070/api/v1/items/";
    private static final String STATE_PATH = "/state";

    private static final UUID ORDER_UID_SUCCESS = UUID.fromString("1a1f775c-4f31-4256-bec1-c3d4e9bf1b52");
    private static final UUID ORDER_UID_NOT_FOUND = fromString("36856fc6-d6ec-47cb-bbee-d20e78299eb9");
    private static final UUID ORDER_UID_NOT_AVAILABLE = fromString("37bb4049-1d1e-449f-8ada-5422f8886231");
    private static final UUID ORDER_UID_ALREADY_EXISTS = fromString("45142058-60e6-4cde-ad13-b968180f0367");

    @MockBean
    private OrderService orderService;

    @MockBean
    private UUIDGenerator uuidGenerator;

    @Autowired
    private OrderManageService orderManageService;

    @Test
    void makeOrderSuccess() {
        when(uuidGenerator.generate()).thenReturn(ORDER_UID_SUCCESS);

        final List<UUID> items = newArrayList(randomUUID(), randomUUID());
        final OrderRequest request =
                new OrderRequest()
                        .setAddress(randomAlphanumeric(10))
                        .setFirstName(randomAlphabetic(10))
                        .setLastName(randomAlphabetic(10))
                        .setItemUids(items);
        final UUID orderUid = orderManageService.makeOrder(request);

        assertEquals(ORDER_UID_SUCCESS, orderUid);
        verify(orderService, times(1)).createOrder(eq(orderUid), eq(request));
    }

    @Test
    void makeOrderAlreadyExists() {
        when(uuidGenerator.generate()).thenReturn(ORDER_UID_ALREADY_EXISTS);

        final List<UUID> items = newArrayList(randomUUID(), randomUUID());
        final OrderRequest request =
                new OrderRequest()
                        .setAddress(randomAlphanumeric(10))
                        .setFirstName(randomAlphabetic(10))
                        .setLastName(randomAlphabetic(10))
                        .setItemUids(items);

        assertThrows(RestRequestException.class, () -> orderManageService.makeOrder(request));
    }

    @Test
    void makeOrderItemsNotAvailable() {
        when(uuidGenerator.generate()).thenReturn(ORDER_UID_NOT_AVAILABLE);

        final List<UUID> items = newArrayList(randomUUID(), randomUUID());
        final OrderRequest request =
                new OrderRequest()
                        .setAddress(randomAlphanumeric(10))
                        .setFirstName(randomAlphabetic(10))
                        .setLastName(randomAlphabetic(10))
                        .setItemUids(items);

        assertThrows(RestRequestException.class, () -> orderManageService.makeOrder(request));
    }

    @Test
    void makeOrderItemsNotFound() {
        when(uuidGenerator.generate()).thenReturn(ORDER_UID_NOT_FOUND);

        final List<UUID> items = newArrayList(randomUUID(), randomUUID());
        final OrderRequest request =
                new OrderRequest()
                        .setAddress(randomAlphanumeric(10))
                        .setFirstName(randomAlphabetic(10))
                        .setLastName(randomAlphabetic(10))
                        .setItemUids(items);

        assertThrows(RestRequestException.class, () -> orderManageService.makeOrder(request));
    }

    @Test
    void statusSuccess() {
        final List<UUID> itemUids = newArrayList(randomUUID(), randomUUID());
        when(orderService.getOrderByUid(ORDER_UID_SUCCESS)).thenReturn(buildOrder(ORDER_UID_SUCCESS, itemUids));
        final OrderInfoResponse status = orderManageService.status(ORDER_UID_SUCCESS);

        assertEquals(ORDER_UID_SUCCESS, status.getOrderUid());
    }

    @Test
    void statusOrderNotFound() {
        final UUID orderUid = randomUUID();
        when(orderService.getOrderByUid(orderUid))
                .thenThrow(new EntityNotFoundException(format("Order '%s' not found", orderUid)));

        assertThrows(EntityNotFoundException.class, () -> orderManageService.status(orderUid));
    }

    @Test
    void processOrderNotFound() {
        final UUID orderUid = randomUUID();
        when(orderService.getOrderByUid(orderUid))
                .thenThrow(new EntityNotFoundException(format("Order '%s' not found", orderUid)));

        assertThrows(EntityNotFoundException.class, () -> orderManageService.process(orderUid));
    }

    @Test
    void processRequestWarehouseError() {
        final UUID orderUid = ORDER_UID_NOT_FOUND;
        when(uuidGenerator.generate()).thenReturn(orderUid);
        final List<UUID> items = newArrayList(randomUUID(), randomUUID());
        when(orderService.getOrderByUid(orderUid))
                .thenReturn(buildOrder(orderUid, items));

        try {
            orderManageService.process(orderUid);
        } catch (RestRequestException exception) {
            final String url = format("%s%s%s", WAREHOUSE_URL, orderUid, STATE_PATH);
            final String responseMessage = JsonOutput.toJson(new ErrorResponse(format("OrderItem '%s' not found", orderUid)));
            final String message = format("Error request to '%s': %d:%s", url, NOT_FOUND.value(), responseMessage);
            assertEquals(message, exception.getMessage());
            return;
        }
        Assertions.fail();
    }

    @Test
    void processRequestSuccess() {
        final UUID orderUid = ORDER_UID_SUCCESS;
        when(uuidGenerator.generate()).thenReturn(orderUid);
        final List<UUID> items = newArrayList(randomUUID(), randomUUID());
        final Order order = buildOrder(orderUid, items);
        when(orderService.getOrderByUid(orderUid)).thenReturn(order);

        final OrderInfoResponse process = orderManageService.process(orderUid);
        assertEquals(ORDER_UID_SUCCESS, process.getOrderUid());
        assertEquals(OrderState.READY_FOR_DELIVERY, process.getState());
        assertEquals(order.getFirstName(), process.getFirstName());
        assertEquals(order.getLastName(), process.getLastName());
        assertEquals(order.getAddress(), process.getAddress());
        assertEquals(items.size(), process.getItems().size());
    }

    private Order buildOrder(UUID orderUid, List<UUID> itemUids) {
        return new Order()
                .setUid(orderUid)
                .setFirstName(randomAlphabetic(10))
                .setLastName(randomAlphabetic(10))
                .setAddress(randomAlphanumeric(10))
                .setItems(on(",").join(itemUids));
    }
}