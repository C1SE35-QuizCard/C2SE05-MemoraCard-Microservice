package com.example.quizcards.exception;

import com.example.quizcards.dto.response.ApiResponse;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateValueException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    @Setter
    private ApiResponse apiResponse;

    private String message;

    public DuplicateValueException(ApiResponse apiResponse) {
        super();
        this.apiResponse = apiResponse;
    }

    public DuplicateValueException(String message) {
        super(message);
        this.message = message;
        apiResponse = new ApiResponse(false, message);
    }

    public DuplicateValueException(String message, Throwable cause) {
        super(message, cause);
        apiResponse = new ApiResponse(false, message);
    }

    public ApiResponse getApiResponse() {
        return apiResponse;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        setApiResponse(new ApiResponse(false, message));
    }
}
