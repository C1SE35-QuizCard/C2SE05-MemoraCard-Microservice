package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionParamRequest {
    @NotNull(message = "Missing folder id")
    private Long folderId;

    @NotNull(message = "Missing set id")
    private Long setId;
}
