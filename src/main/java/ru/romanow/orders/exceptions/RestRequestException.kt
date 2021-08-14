package ru.romanow.orders.exceptions;

public class RestRequestException
        extends RuntimeException {
    public RestRequestException(String message) {
        super(message);
    }
}
