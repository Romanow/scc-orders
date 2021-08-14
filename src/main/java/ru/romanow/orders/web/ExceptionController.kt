package ru.romanow.orders.web

import org.slf4j.LoggerFactory
import ru.romanow.orders.service.OrderManageService
import org.springframework.http.ResponseEntity
import java.lang.Void
import java.util.UUID
import org.springframework.web.bind.MethodArgumentNotValidException
import ru.romanow.orders.web.ExceptionController
import org.springframework.validation.FieldError
import java.util.stream.Collectors
import javax.validation.constraints.NotEmpty
import ru.romanow.orders.model.enums.OrderState
import org.springframework.web.client.RestTemplate
import ru.romanow.orders.service.OrderService
import ru.romanow.orders.repository.OrdersRepository
import ru.romanow.orders.service.OrderServiceImpl
import javax.persistence.EntityNotFoundException
import ru.romanow.orders.service.OrderManageServiceImpl
import org.springframework.web.client.RestClientResponseException
import ru.romanow.orders.exceptions.RestRequestException
import java.lang.RuntimeException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.jvm.JvmStatic
import org.springframework.boot.SpringApplication
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import ru.romanow.orders.model.*
import java.lang.Exception

@RestControllerAdvice
class ExceptionController {
    private val logger = LoggerFactory.getLogger(ExceptionController::class.java)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun badRequest(exception: MethodArgumentNotValidException): ValidationErrorResponse {
        val bindingResult = exception.bindingResult
        return ValidationErrorResponse(buildMessage(bindingResult), buildErrors(bindingResult))
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException::class)
    fun handleException(exception: RuntimeException): ErrorResponse {
        logger.error("", exception)
        return ErrorResponse(exception.message!!)
    }

    private fun buildMessage(bindingResult: BindingResult): String {
        return String.format("Error on %s, rejected errors [%s]",
            bindingResult.target,
            bindingResult.allErrors
                .stream()
                .map { it.defaultMessage }
                .collect(Collectors.joining(",")))
    }

    private fun buildErrors(bindingResult: BindingResult): List<ErrorDescription> {
        return bindingResult.fieldErrors
            .stream()
            .map { ErrorDescription(it.field, it.defaultMessage!!) }
            .collect(Collectors.toList())
    }
}