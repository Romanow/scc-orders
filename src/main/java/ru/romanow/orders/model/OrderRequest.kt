package ru.romanow.orders.model

import java.util.*
import javax.validation.constraints.NotEmpty

data class OrderRequest(
    @field:NotEmpty(message = "{field.is.empty")
    val itemUids: List<UUID>,

    @field:NotEmpty(message = "{field.is.empty")
    val firstName: String? = null,

    @field:NotEmpty(message = "{field.is.empty")
    val lastName: String? = null,

    @field:NotEmpty(message = "{field.is.empty")
    val address: String? = null
)