package com.microservices.progressservice.dto.request;

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
    @Size(min = 1, max = 512)
    List<Long> cardIds;

    @NotNull
    Long setId;
}
