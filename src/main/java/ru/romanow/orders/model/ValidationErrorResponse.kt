package ru.romanow.orders.model

data class ValidationErrorResponse(
    val message: String,
    val errors: List<ErrorDescription>
)