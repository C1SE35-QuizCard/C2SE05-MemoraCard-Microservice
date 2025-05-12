package com.example.quizcards.validation;

import com.example.quizcards.exception.BadRequestException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class SRSTimeClientValidator implements ConstraintValidator<ValidSRSTimeClient, OffsetDateTime> {

    @Override
    public boolean isValid(OffsetDateTime offsetDateTime, ConstraintValidatorContext context) {
        // Nếu client không gửi giá trị (null) thì coi là hợp lệ
        if (offsetDateTime == null) {
            return true;
        }

        int maxOffsetDays = 2;
        // Lấy thời điểm hiện tại ở UTC
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        // Chuyển thời gian client về UTC
        OffsetDateTime clientUtc = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);

        if (clientUtc.isBefore(nowUtc.minusDays(maxOffsetDays))
                || clientUtc.isAfter(nowUtc.plusDays(maxOffsetDays))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            String.format("timeFromClient must be within ±%d days of server (UTC)", maxOffsetDays))
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
