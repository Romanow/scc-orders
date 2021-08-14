package ru.romanow.orders.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class OrderRequest {

    @NotEmpty(message = "{field.is.empty")
    private List<UUID> itemUids;

    @NotEmpty(message = "{field.is.empty")
    private String firstName;

    @NotEmpty(message = "{field.is.empty")
    private String lastName;

    @NotEmpty(message = "{field.is.empty")
    private String address;
}
