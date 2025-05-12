package com.example.quizcards.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRequest {
    private Long id;
    private Long folderId;
    private Long setId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
