package com.example.quizcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashCardDownloadDTO implements IFlashCardDownloadDTO {
    private Long cardId;
    private String question;
    private String answer;
    private String imageUrl;
}
