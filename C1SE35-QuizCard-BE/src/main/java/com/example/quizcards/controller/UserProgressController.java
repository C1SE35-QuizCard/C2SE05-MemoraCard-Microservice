package com.example.quizcards.controller;

import com.example.quizcards.dto.IFlashcardProgressDTO;
import com.example.quizcards.dto.request.GetFlashcardRequest;
import com.example.quizcards.dto.request.UserProgressRequest;
import com.example.quizcards.dto.response.ErrorDetail;
import com.example.quizcards.dto.IProgressAnalysisDTO;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IUserProgressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/progress/user")
public class UserProgressController {

    @Autowired
    private IUserProgressService userProgressService;
    private static final String FETCH_ERROR_MESSAGE = "An error occurred while fetching user progress";

//    @GetMapping("/{user_id}")
//    public ResponseEntity<Object> findUserSetProgress(@PathVariable("user_id") Long userId) {
//        try {
//            if (userProgressService.findUserSetProgress(userId).isEmpty()) {
//                return new ResponseEntity<>("No user progress found", HttpStatus.NO_CONTENT);
//            } else {
//                List<IUserProgressDTO> userProgress = userProgressService.findUserSetProgress(userId);
//                return ResponseEntity.ok(userProgress);
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
//        }
//    }

    @GetMapping("/set/{set_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> findFlashcardsProgressBySetId(@PathVariable("set_id") Long setId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        try {
            if (userProgressService.findFlashcardsProgressBySetId(setId, up.getId()).isEmpty()) {
                return new ResponseEntity<>("No flashcard progress found", HttpStatus.NO_CONTENT);
            } else {
                List<IFlashcardProgressDTO> cardProgress = userProgressService.findFlashcardsProgressBySetId(setId, up.getId());
                return ResponseEntity.ok(cardProgress);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @PostMapping("/get-by-card-ids")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> findFlashcardsProgressByCardIdsIn(@Valid @RequestBody GetFlashcardRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        return ResponseEntity.ok().body(userProgressService.findCardProgressesByCardIdsIn(
                up.getId(), request.getCardIds(), request.getCardIds().size()
        ));
    }

    @GetMapping("/analysis/set/{set_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAnalysisProgressBySetId(@PathVariable("set_id") Long setId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        IProgressAnalysisDTO analysisDTO = userProgressService.findAnalysisProgressBySetId(setId, up.getId());
        if (analysisDTO == null) {
            throw new ResourceNotFoundException("Analysis", "set id", setId);
        }
        return ResponseEntity.ok(analysisDTO);
    }

    @Deprecated
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('NO_ROLE')")
    public ResponseEntity<Object> addUserProgress(@RequestBody @Validated UserProgressRequest request, BindingResult bindingResult) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Invalid request: request cannot be null");
        }
        if (bindingResult.hasErrors()) {
            ErrorDetail errorDetail = new ErrorDetail("Validation errors");
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorDetail.addError(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errorDetail);
        }
        try {
            if (userProgressService.existsByUserIdAndCardId(request.getUserId(), request.getCardId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A progress for this user and card already exists.");
            }
            userProgressService.addUserProgress(request.getProgressType(),
                    request.getIsAttention(),
                    request.getUserId(),
                    request.getCardId());
            return ResponseEntity.status(HttpStatus.CREATED).body("User Progress created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the User Progress");
        }
    }

    @Deprecated
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('NO_ROLE')")
    public ResponseEntity<Object> deleteUserProgressById(@PathVariable("id") Long progressId) {
        if (userProgressService.findUserProgressById(progressId) != null) {
            try {
                userProgressService.deleteUserProgressById(progressId);
                return new ResponseEntity<>("User Progress deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the User Progress");
            }
        } else {
            return new ResponseEntity<>("User Progress not found", HttpStatus.NOT_FOUND);
        }
    }

    @Deprecated
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('NO_ROLE')")
    public ResponseEntity<Object> updateUserProgress(@Validated @RequestBody UserProgressRequest request, BindingResult bindingResult) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Invalid request: request cannot be null");
        }
        if (bindingResult.hasErrors()) {
            ErrorDetail errorDetail = new ErrorDetail("Validation errors");
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorDetail.addError(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errorDetail);
        }
        try {

            if (userProgressService.existsByUserIdAndCardIdAndNotId(request.getUserId(), request.getCardId(), request.getProgressId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A progress for this user and card already exists.");
            }
            userProgressService.updateUserProgress(request);
            return new ResponseEntity<>("User Progress updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the User Progress");
        }
    }

    @PatchMapping("/assign-progress")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> assignUserProgress(@Valid @RequestBody UserProgressRequest request) {
        return ResponseEntity.ok().body(userProgressService.assignUserProgress(request));
    }

    @DeleteMapping("/reset-progress/{set_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> resetUserProgress(@PathVariable("set_id") Long setId) {
        userProgressService.resetUserProgress(setId);
        return ResponseEntity.ok().build();
    }
}
