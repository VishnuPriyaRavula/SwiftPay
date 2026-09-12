package com.swiftpay.api;

import com.swiftpay.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PaymentService.PaymentNotFoundException.class)
    ResponseEntity<ApiError> notFound(RuntimeException ex, HttpServletRequest request) { return error(HttpStatus.NOT_FOUND, ex.getMessage(), request); }
    @ExceptionHandler({PaymentService.DuplicatePaymentException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> conflict(RuntimeException ex, HttpServletRequest request) { return error(HttpStatus.CONFLICT, ex.getMessage(), request); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }
    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
    public record ApiError(Instant timestamp, int status, String error, String message, String path) { }
}
