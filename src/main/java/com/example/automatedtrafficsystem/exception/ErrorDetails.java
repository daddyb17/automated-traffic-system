package com.example.automatedtrafficsystem.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String details;
    private String errorCode;
    private Map<String, Object> additionalDetails;

    public ErrorDetails(LocalDateTime timestamp, String message, String details, String errorCode) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.errorCode = errorCode;
    }

    public void setDetails(Map<String, Object> details) {
        this.additionalDetails = details;
    }

    public static ErrorDetailsBuilder builder() {
        return new ErrorDetailsBuilder();
    }

    public static class ErrorDetailsBuilder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private String message;
        private String details;
        private String errorCode;
        private Map<String, Object> additionalDetails;

        public ErrorDetailsBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorDetailsBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorDetailsBuilder details(String details) {
            this.details = details;
            return this;
        }

        public ErrorDetailsBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorDetailsBuilder additionalDetails(Map<String, Object> additionalDetails) {
            this.additionalDetails = additionalDetails;
            return this;
        }

        public ErrorDetails build() {
            ErrorDetails errorDetails = new ErrorDetails(timestamp, message, details, errorCode);
            if (additionalDetails != null) {
                errorDetails.setAdditionalDetails(additionalDetails);
            }
            return errorDetails;
        }
    }
}
