package com.example.automatedtrafficsystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            details.put(fieldName, error.getDefaultMessage());
        });

        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                request,
                details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetails> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Constraint validation failed", request, details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request, null);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorDetails> handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
        String message;
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            String expectedType = mismatch.getRequiredType() != null
                    ? mismatch.getRequiredType().getSimpleName()
                    : "unknown";
            message = "Invalid value '" + mismatch.getValue() + "' for parameter '" + mismatch.getName()
                    + "'. Expected type: " + expectedType;
        } else {
            message = "Invalid value '" + ex.getValue() + "'. " + ex.getLocalizedMessage();
        }
        return buildError(HttpStatus.BAD_REQUEST, "INVALID_INPUT", message, request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetails> handleMissingParams(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = "Required parameter '" + ex.getParameterName() + "' is not present";
        return buildError(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Malformed JSON request", request, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDetails> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        HttpStatus resolvedStatus = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        return buildError(
                resolvedStatus,
                "HTTP_" + resolvedStatus.value(),
                ex.getReason() != null ? ex.getReason() : resolvedStatus.getReasonPhrase(),
                request,
                null
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetails> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        log.error("Illegal state while processing request", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error occurred", ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request,
                null
        );
    }

    private ResponseEntity<ErrorDetails> buildError(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            Map<String, Object> details
    ) {
        ErrorDetails body = ErrorDetails.of(
                status,
                errorCode,
                message,
                request.getRequestURI(),
                resolveRequestId(request),
                details
        );
        return ResponseEntity.status(status).body(body);
    }

    private String resolveRequestId(HttpServletRequest request) {
        String fromHeader = request.getHeader(REQUEST_ID_HEADER);
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader;
        }
        String fromMdc = MDC.get("requestId");
        return (fromMdc != null && !fromMdc.isBlank()) ? fromMdc : null;
    }
}
