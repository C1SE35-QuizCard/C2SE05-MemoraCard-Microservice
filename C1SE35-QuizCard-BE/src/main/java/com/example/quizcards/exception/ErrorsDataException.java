package com.example.quizcards.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorsDataException extends RuntimeException {
    private String message;
    private Map<String, Object> errors;
    private HttpStatus httpStatus;
}
