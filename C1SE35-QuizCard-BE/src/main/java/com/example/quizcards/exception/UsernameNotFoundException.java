package com.example.quizcards.exception;

import com.example.quizcards.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class UsernameNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ApiResponse apiResponse;

    private String message;

    public UsernameNotFoundException(ApiResponse apiResponse) {
        super();
        this.apiResponse = apiResponse;
    }

    public UsernameNotFoundException(String message) {
        super(message);
        this.message = message;
        apiResponse = new ApiResponse(false, message);
    }

    public UsernameNotFoundException(String message, Throwable cause) {
        super(message, cause);
        apiResponse = new ApiResponse(false, message);
    }

    public ApiResponse getApiResponse() {
        return apiResponse;
    }

    public void setApiResponse(ApiResponse apiResponse) {
        this.apiResponse = apiResponse;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
