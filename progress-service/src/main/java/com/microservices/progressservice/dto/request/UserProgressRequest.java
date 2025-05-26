package com.microservices.progressservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressRequest {
    private Long progressId;

    private Boolean progressType;

    private Boolean isAttention;

    private Long userId;

    @NotNull
    private Long cardId;

    public record SimpleModeUserProgressRequest(
            @NotNull
            @Size(min = 1, max = 200)
            List<@Valid UserProgressRequest> progresses,

            @NotNull
            Long setId,

            Long userId
    ) {}
}
