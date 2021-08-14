package ru.romanow.orders.model;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.romanow.orders.model.enums.OrderState;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class OrderInfoResponse {
    private UUID orderUid;
    private String firstName;
    private String lastName;
    private String address;
    private OrderState state;
    private List<ItemsInfo> items;
}
