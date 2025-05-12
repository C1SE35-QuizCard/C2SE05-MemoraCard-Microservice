package com.example.quizcards.dto.response;

import com.example.quizcards.dto.FlashcardSetDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeDataGuessUserResponse {
    private List<FlashcardSetDTO> setsPopular;

    //    private Map<String, List<ISetFlashcardDTO>> listSets;
    private List<ISetFlashcardDTO> listSets;

    public HomeDataGuessUserResponse(List<FlashcardSetDTO> flashcardSetDTOS) {
    }
}
