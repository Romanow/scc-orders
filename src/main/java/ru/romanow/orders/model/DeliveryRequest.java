package ru.romanow.orders.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Access;

@Data
@Accessors(chain = true)
public class DeliveryRequest {
    private String firstName;
    private String lastName;
    private String address;
}
