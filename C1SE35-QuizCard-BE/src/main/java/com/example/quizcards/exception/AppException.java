package com.example.quizcards.exception;

import com.example.quizcards.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AppException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ApiResponse apiResponse;

    public AppException(String message) {
        super(message);
        apiResponse = new ApiResponse(false, message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
        apiResponse = new ApiResponse(false, message);
    }
}