package com.example.quizcards.dto.response;

import com.example.quizcards.dto.FlashCardDownloadDTO;
import com.example.quizcards.dto.IFlashCardDownloadDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetDownloadResponse {
    private Long setId;
    private String title;
    private String descriptionSet;
    private int totalCard;
    private LocalDateTime downloadDate;

    @JsonProperty("flashcards")
    private List<FlashCardDownloadDTO> flashcards;
}