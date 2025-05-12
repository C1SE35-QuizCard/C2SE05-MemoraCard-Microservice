package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KQRequest {
    @NotNull
    private Long setId;

    @NotNull
    private Long numQuiz;
}
