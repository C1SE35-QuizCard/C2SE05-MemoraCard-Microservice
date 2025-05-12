package com.example.quizcards.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

// Validator implementation
public class TimeRangeValidator implements ConstraintValidator<TimeRange, LocalTime> {
    private LocalTime minTime;
    private LocalTime maxTime;

    @Override
    public void initialize(TimeRange constraintAnnotation) {
        this.minTime = LocalTime.of(
                constraintAnnotation.minHour(),
                constraintAnnotation.minMinute()
        );
        this.maxTime = LocalTime.of(
                constraintAnnotation.maxHour(),
                constraintAnnotation.maxMinute()
        );
    }

    @Override
    public boolean isValid(LocalTime time, ConstraintValidatorContext context) {
        if (time == null) {
            return true; // Let @NotNull handle null validation
        }
        return !time.isBefore(minTime) && !time.isAfter(maxTime);
    }
}