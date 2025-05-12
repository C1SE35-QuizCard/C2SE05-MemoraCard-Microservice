package com.example.quizcards.controller;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.entities.Matching;
import com.example.quizcards.service.IFlashcardService;
import com.example.quizcards.service.IMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/matching")
public class MatchingController {

    @Autowired
    private IFlashcardService iFlashcardService;

    @Autowired
    private IMatchingService iMatchingService;

    @GetMapping("")
    public ResponseEntity<?> getMatchingQuestions() {
        List<Matching> matchings = iMatchingService.getAll();
        return new ResponseEntity<>(matchings, HttpStatus.OK);
    }

    @PostMapping("/{set_id}")
    public  ResponseEntity<?> createMatching(@PathVariable("set_id") Long setId){
        try {
            if (setId != null) {
                List<IFlashcardDTO> flashcards = iFlashcardService.getRandomFlashcardsBySetId(setId);
                List<Matching> matchings = iMatchingService.save(flashcards);
                return ResponseEntity.ok(matchings);

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No flashcards found for set ID " + setId);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching flashcards");
        }

    }

    @PutMapping("{matching_id}")
    public  ResponseEntity<?> updateIsCorrectMatching(@PathVariable("matching_id") Long matchingId) {
        try{
            iMatchingService.changeIsCorrect(matchingId);
            return ResponseEntity.ok("Matching updated successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}