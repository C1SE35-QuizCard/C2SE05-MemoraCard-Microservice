package com.example.quizcards.dto.response;

import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorDetail {
    private String message;
    private Map<String, String> errors;

    public ErrorDetail(String message) {
        this.message = message;
        this.errors = new HashMap<>();
    }

    public ErrorDetail(String message, List<FieldError> fieldErrors) {
        this.message = message;
        this.errors = new HashMap<>();
        fieldErrors.forEach(error -> this.errors.put(error.getField(), error.getDefaultMessage()));
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void addError(String field, String message) {
        errors.put(field, message);
    }

    public String getMessage() {
        return message;
    }
}
