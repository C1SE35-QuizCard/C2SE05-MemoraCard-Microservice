package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressSrsRequest {
    @NotNull
    Long setId;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SrsRequest {
        Long userId;

        @NotNull
        Long cardId;

        @NotBlank
        @Pattern(
                regexp = "(?i)^(again|hard|good|easy)$",
                message = "userRating is one of: (again, hard, good, easy)"
        )
        String userRating;

        Instant reviewDurationMillis;
    }

    @NotNull
    @Size(max = 256)
    List<SrsRequest> batchRequests;
}
