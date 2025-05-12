package com.example.quizcards.exception;

import org.springframework.http.HttpStatus;

public class QuizCardApiException extends RuntimeException {
    private static final long serialVersionUID = -6593330219878485669L;

    private final HttpStatus status;
    private final String message;

    public QuizCardApiException(HttpStatus status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public QuizCardApiException(int statusCode, String message) {
        super();
        this.status = HttpStatus.valueOf(statusCode);
        this.message = message;
    }

    public QuizCardApiException(HttpStatus status, String message, Throwable exception) {
        super(exception);
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
