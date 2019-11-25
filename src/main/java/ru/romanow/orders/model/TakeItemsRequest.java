package ru.romanow.orders.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TakeItemsRequest {
    private final List<UUID> itemsUid;
}
