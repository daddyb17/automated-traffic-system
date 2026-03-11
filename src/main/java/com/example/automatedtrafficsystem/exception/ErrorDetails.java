package com.example.automatedtrafficsystem.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    private String requestId;
    private Map<String, Object> details;

    public static ErrorDetails of(
            HttpStatus status,
            String errorCode,
            String message,
            String path,
            String requestId,
            Map<String, Object> details
    ) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setTimestamp(LocalDateTime.now());
        errorDetails.setStatus(status.value());
        errorDetails.setError(status.getReasonPhrase());
        errorDetails.setErrorCode(errorCode);
        errorDetails.setMessage(message);
        errorDetails.setPath(path);
        errorDetails.setRequestId(requestId);
        errorDetails.setDetails(details != null ? details : Collections.emptyMap());
        return errorDetails;
    }
}
