package com.microservices.streakservice.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OffsetValidator.class)
public @interface ValidOffset {
    String message() default "Invalid time zone. Range must be -12 to +14 hours.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
