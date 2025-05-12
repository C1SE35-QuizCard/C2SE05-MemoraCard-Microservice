package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetFlashcardRequest {
    @NotNull
    @Size(min = 1, max = 500)
    List<Long> cardIds;

    Long limit;
}
