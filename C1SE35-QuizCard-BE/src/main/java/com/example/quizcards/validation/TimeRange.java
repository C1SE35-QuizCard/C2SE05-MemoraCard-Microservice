package com.example.quizcards.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeRangeValidator.class)
public @interface TimeRange {
    String message() default "Time must be between min and max time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // Định nghĩa min time
    int minHour() default 0;
    int minMinute() default 0;

    // Định nghĩa max time
    int maxHour() default 23;
    int maxMinute() default 59;
}
