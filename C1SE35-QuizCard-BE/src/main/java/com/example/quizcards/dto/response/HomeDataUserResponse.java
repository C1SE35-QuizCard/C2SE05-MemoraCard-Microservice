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
public class HomeDataUserResponse {
    private List<ISetFlashcardDTO> setsRecentAccessed;
    private List<ISetFlashcardDTO> setsRelevantCategory;
    private List<FlashcardSetDTO> setsPopular;
    private List<ITopCreatorsResponse> topCreators;

    private FreeUserProfileResponse personData;

    private String roleName;
    private String relevantCategory;
}
