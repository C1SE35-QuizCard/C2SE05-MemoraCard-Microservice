package com.example.quizcards.service.impl;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.Flashcard;
import com.example.quizcards.entities.Matching;
import com.example.quizcards.repository.IMatchingRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IMatchingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService implements IMatchingService {
    @Autowired
    private IMatchingRepository iMatchingRepository;

    @Override
    @Transactional
    public List<Matching> save(List<IFlashcardDTO> flashcards) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        Long userId = user.getId();
        iMatchingRepository.deleteByUser_UserId(userId);
        int size = flashcards.size();
        // Validate the number of flashcards (between 2 and 15)
        if (size < 2 || size > 15) {
            throw new IllegalArgumentException("The number of flashcards must be from 2 to 15.");
        }

        // Shuffle the flashcards list
        Collections.shuffle(flashcards);

        List<Matching> matchings = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Matching matching = new Matching();

            // Set the flashcard for the matching
            Flashcard flashcard = new Flashcard();
            flashcard.setCardId(flashcards.get(i).getCardId());
            matching.setFlashcard(flashcard);
            matching.setIsCorrect(false);
//            matching.setSetCode(String.valueOf(flashcard.getSet().getSetId()));
            matching.setUser(new AppUser(userId));

            // Assign round number based on index
            if (i < 5) {
                matching.setRoundNumber(Matching.RoundNumber.ROUND_1);
            } else if (i < 10) {
                matching.setRoundNumber(Matching.RoundNumber.ROUND_2);
            } else {
                matching.setRoundNumber(Matching.RoundNumber.ROUND_3);
            }

            matchings.add(matching);
        }

        return iMatchingRepository.saveAll(matchings);
    }

    @Override
    public List<Matching> findAll() {
        return iMatchingRepository.findAll();
    }

    @Override
    public List<Matching> getAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        Long userId = user.getId();

        List<Matching> matchings = iMatchingRepository.findByUser_UserId(userId);

        return matchings.stream().map(matching -> {
            Flashcard originalFlashcard = matching.getFlashcard();
            Flashcard simplifiedFlashcard = Flashcard.builder()
                    .cardId(originalFlashcard.getCardId())
                    .question(originalFlashcard.getQuestion())
                    .answer(originalFlashcard.getAnswer())
                    .imageLink(originalFlashcard.getImageLink())
                    .isApproved(originalFlashcard.getIsApproved())
                    .createdAt(originalFlashcard.getCreatedAt())
                    .updatedAt(originalFlashcard.getUpdatedAt())
                    .build();
            matching.setFlashcard(simplifiedFlashcard);

            return matching;
        }).collect(Collectors.toList());
    }

    @Override
    public void changeIsCorrect(Long matchingId) {
        iMatchingRepository.updateIsCorrectByMatchingId(matchingId);
    }
}