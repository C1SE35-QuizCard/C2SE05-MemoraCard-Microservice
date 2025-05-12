package com.example.quizcards.exception;

import com.example.quizcards.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ApiResponse apiResponse;

    public TokenRefreshException(String token, String message) {
        buildApiResponse(token, message);
    }

    private void buildApiResponse(String token, String message) {
        ApiResponse apiResponse = new ApiResponse(false, message);
        apiResponse.setSuccess(false);
        apiResponse.setMessage(String.format("Failed for [%s]: [%s]", token, message));
    }

    public ApiResponse getApiResponse() {
        return apiResponse;
    }

    public void setApiResponse(String token, String message) {
        buildApiResponse(token, message);
    }
}
