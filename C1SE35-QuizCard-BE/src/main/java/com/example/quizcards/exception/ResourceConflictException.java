package com.example.quizcards.exception;

import com.example.quizcards.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ApiResponse apiResponse;

    public ResourceConflictException(ApiResponse apiResponse) {
        super();
        this.apiResponse = apiResponse;
    }

    public ResourceConflictException(String message) {
        super(message);
        apiResponse = new ApiResponse(false, message);
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
        apiResponse = new ApiResponse(false, message);
    }

    public ApiResponse getApiResponse() {
        return apiResponse;
    }
}
