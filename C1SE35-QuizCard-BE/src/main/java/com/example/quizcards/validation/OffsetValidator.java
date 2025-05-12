package com.example.quizcards.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.OffsetDateTime;

public class OffsetValidator implements ConstraintValidator<ValidOffset, OffsetDateTime> {
    @Override
    public boolean isValid(OffsetDateTime value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true; // Nếu giá trị là null, không cần validate (bạn có thể tùy chỉnh theo nhu cầu)
        }

        // Lấy giá trị offset trong giờ (sử dụng getTotalSeconds để tính tổng số giây rồi chuyển sang giờ)
        int offsetHours = value.getOffset().getTotalSeconds() / 3600;

        // Kiểm tra xem offset có nằm trong phạm vi hợp lệ từ -12 đến +14 không
        return offsetHours >= -12 && offsetHours <= 14;
    }
}
