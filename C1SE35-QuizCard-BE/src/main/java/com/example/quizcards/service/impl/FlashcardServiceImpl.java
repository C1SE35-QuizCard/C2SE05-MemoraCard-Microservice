package com.example.quizcards.service.impl;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.dto.request.FlashcardRequest;
import com.example.quizcards.entities.Flashcard;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.FlashcardHelpers.IFlashcardHelpers;
import com.example.quizcards.repository.IFlashcardRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IFlashcardService;
import com.example.quizcards.service.ISetFlashcardService;
import com.example.quizcards.utils.HandleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FlashcardServiceImpl implements IFlashcardService {

    @Autowired
    private IFlashcardRepository flashcardRepository;

    @Autowired
    private ISetFlashcardService setFlashcardService;

    @Autowired
    private IFlashcardHelpers flashcardHelpers;

    @Override
    public List<IFlashcardDTO> getAllBySetId(Long id, String requestPassword) {
        setFlashcardService.checkAccess(id, requestPassword);
        return flashcardRepository.findAllFlashcardsBySetId(id);
    }

    @Override
    public List<IFlashcardDTO> getAll() {
        return flashcardRepository.findAllFlashcards();
    }

    @Override
    public List<IFlashcardDTO> getInfoFlashcardByIdsIn(Set<Long> ids) {
        return flashcardRepository.findInfoCardByIdsIn(ids);
    }

    @Override
    public List<IFlashcardDTO> getInfoFlashcardBySetIdAndIdsIn(Long setId, Set<Long> ids) {
        return flashcardRepository.findInfoCardBySetIdAndIdsIn(setId, ids);
    }

    @Override
    public void addFlashcard(String question, String answer, String imageLink, Boolean isApproved, Long setId) {
        flashcardRepository.createFlashcards(question, answer, imageLink, isApproved, setId);
    }

    @Override
    public void deleteFlashcard(Long cardId) {
        flashcardRepository.deleteFlashcardById(cardId);
    }

    @Override
    public void updateFlashcard(FlashcardRequest request) {
        flashcardHelpers.handleUpdateFlashcard(request);

        flashcardRepository.updateFlashcards(request.getCardId(), request.getQuestion(), request.getAnswer(), request.getImageLink(), request.getIsApproved(), request.getSetId());
    }

    @Override
    public ResponseEntity<?> addFlashcard_2(FlashcardRequest request) {
        flashcardHelpers.handleAddFlashcard(request);
        Flashcard newCard = Flashcard.builder()
                .question(HandleString.popExtraNewLineAndSpace(request.getQuestion()))
                .answer(HandleString.popExtraNewLineAndSpace(request.getAnswer()))
                .imageLink(request.getImageLink())
                .isApproved(true)
                .set(SetFlashcard.builder().setId(request.getSetId()).build())
                .build();
        newCard = flashcardRepository.save(newCard);
        return ResponseEntity.ok().body(getResponseFromCard(newCard, request.getSetId()));
    }

    @Override
    public ResponseEntity<?> updateFlashcard_2(FlashcardRequest request) {
        flashcardHelpers.handleUpdateFlashcard(request);
        Flashcard card = flashcardRepository.findById(request.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", request.getCardId()));
        card.setQuestion(request.getQuestion() == null ? card.getQuestion() :
                HandleString.popExtraNewLineAndSpace(request.getQuestion()));
        card.setAnswer(request.getAnswer() == null ? card.getAnswer() :
                HandleString.popExtraNewLineAndSpace(request.getAnswer()));
        card.setImageLink(request.getImageLink());
        card.setIsApproved(true);
        flashcardRepository.save(card);
        return ResponseEntity.ok().body(getResponseFromCard(card, request.getSetId()));
    }

    @Override
    public void deleteFlashcard_2(Long cardId, Long setId) {
        flashcardHelpers.handleDeleteFlashcard(cardId, setId);
        flashcardRepository.deleteFlashcardById(cardId);
    }

    private Map<String, Object> getResponseFromCard(Flashcard card, Long setId) {
        Map<String, Object> response = new HashMap<>();
        response.put("cardId", card.getCardId());
        response.put("setId", setId);
        response.put("userId",
                ((UserPrincipal) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getId());
        response.put("imageUrl", card.getImageLink());
//        response.put("createdAt", card.getCreatedAt());
//        response.put("updatedAt", card.getUpdatedAt());
        return response;
    }

    @Override
    public IFlashcardDTO findByCardId(Long cardId) {
        return flashcardRepository.findFlashcardByCardId(cardId);
    }


    @Override
    public List<IFlashcardDTO> getRandomFlashcardsBySetId(Long setId) {
        return flashcardRepository.findRandomFlashcardsBySetId(setId);
    }

    @Override
    public List<IFlashcardDTO> getFlashcardsByCardIdsIn(List<Long> cardIds) {
        return flashcardRepository.findFlashcardByIdsIn(cardIds);
    }
}
