package com.example.quizcards.service;

import com.example.quizcards.dto.FlashcardSetDTO;
import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.dto.request.QueryDTO;
import com.example.quizcards.dto.request.SetFlashcardInitializeRequest;
import com.example.quizcards.dto.request.SetFlashcardRequest;
import com.example.quizcards.dto.response.SearchSetFlashResponse;
import com.example.quizcards.dto.response.ITopCreatorsResponse;
import com.example.quizcards.entities.SetFlashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ISetFlashcardService {
    SetFlashcard findById(Long setId);

    List<IFlashcardDTO> getAllFlashcardBySetId(Long setId, String requestPassword);

    List<ISetFlashcardDTO> getAll();

    List<ISetFlashcardDTO> getAllLimit(int limitData);

    List<ISetFlashcardDTO> getListFlashcardsByNearbySetting(Long userId, Long limit);

    void addSetFlashcard(String title,
                         String descriptionSet,
                         Boolean isApproved,
                         Boolean isAnonymous,
                         Boolean sharingMode,
                         String hashPassword,
                         Long userId,
                         Long categoryId);

    void deleteSetFlashcardAdmin(Long setId);

    void updateSetFlashcardAdmin(SetFlashcardRequest request);

    void deleteSetFlashcard(Long setId);

    void updateSetFlashcard(SetFlashcardRequest request);

    Long createNewSetFlashcards(SetFlashcardInitializeRequest request);

    ISetFlashcardDTO findBySetId(Long setId);

    ISetFlashcardDTO findBySetId_2(Long setId);

    Integer countSetFlashcardCreatedPublic(Long userId);

    Integer countSetFlashcardCreatedPublicByUserName(String userName);

    Integer countSetFlashcardCreatedInCurrentUser();

    Integer countSetFlashcardCreatedPerDayInCurrentUser();

    Integer countNumberOfSetCreated(Long userId);

    Integer countNumberOfSetCreatedInCurrentDay(Long userId);

    Page<ISetFlashcardDTO> filterByUserIdAndCategoryName(Long userId, String categoryName,
                                                         int page, int size);

    List<SearchSetFlashResponse> searchByTitleAndCategory(String title);

    List<ISetFlashcardDTO> sortByUpdatedDate();

    List<ISetFlashcardDTO> getAllSetByUserId();

    List<ISetFlashcardDTO> getAllSetPublic();

    List<ISetFlashcardDTO> getAllSetPublicByUserId(Long userId);

    List<ISetFlashcardDTO> loadTop10RecentSetFlashcards(Long userId);

    List<ISetFlashcardDTO> loadTop10RelevantByCategory(Long categoryId, Long userId);

    List<FlashcardSetDTO> loadTop10PopularFlashcardSets(Long userId);

    List<ITopCreatorsResponse> loadTop10PopularCreators();
    boolean existsBySetFlashcard_SetIdAndSetFlashcard_SharingMode(Long setId);

    long countFlashcardsBySetId(Long setId);
    List<SearchSetFlashResponse> searchByMyCourse(QueryDTO queryDTO,Long userId);

    public void checkAccess(Long setId, String requestPassword);;;
}
