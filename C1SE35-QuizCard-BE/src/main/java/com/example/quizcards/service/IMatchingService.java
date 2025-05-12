
package com.example.quizcards.service;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.entities.Matching;

import java.util.List;

public interface IMatchingService {
    List<Matching> save(List<IFlashcardDTO> flashcards);

    List<Matching> findAll();

    List<Matching> getAll();

    void changeIsCorrect(Long matchingId);
}