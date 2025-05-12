package com.example.quizcards.controller;

import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.dto.OnUpdate;
import com.example.quizcards.dto.request.QueryDTO;
import com.example.quizcards.dto.request.SetFlashcardInitializeRequest;
import com.example.quizcards.dto.request.SetFlashcardRequest;
import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.dto.response.SearchSetFlashResponse;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ISetFlashcardService;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/set")
public class SetFlashcardController {
    @Autowired
    private ISetFlashcardService setFlashcardService;
    private static final String FETCH_ERROR_MESSAGE = "An error occurred while fetching set flashcards";
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/list")
    public ResponseEntity<Object> findAllSetFlashcard() {
        try {
            if (setFlashcardService.getAll().isEmpty()) {
                return new ResponseEntity<>("No set flashcard found", HttpStatus.NO_CONTENT);
            } else {
                List<ISetFlashcardDTO> set = setFlashcardService.getAll();
                return ResponseEntity.ok(set);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Hoặc logger.error("Error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<Object> findAllFlashcardBySetId(@PathVariable("id") Long setId,
                                                          @RequestParam(required = false) String requestPassword) {
//        try {
//            if (!setFlashcardService.getAllFlashcardBySetId(setId).isEmpty()) {
//                List<IFlashcardDTO> flashcards = setFlashcardService.getAllFlashcardBySetId(setId);
//                return ResponseEntity.ok(flashcards);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No flashcards found for set ID " + setId);
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        return ResponseEntity.ok(setFlashcardService.getAllFlashcardBySetId(setId, requestPassword));

    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Object> detailSetFlashcardById(@PathVariable("id") Long setId) {
        try {
            if (setFlashcardService.findBySetId(setId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Set Flashcard not found");
            }
            ISetFlashcardDTO setFlashcard = setFlashcardService.findBySetId(setId);
            return ResponseEntity.ok(setFlashcard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/set-detail/{id}")
    public ResponseEntity<?> detailSetFlashcardById_2(@PathVariable("id") Long setId) {
        ISetFlashcardDTO result = setFlashcardService.findBySetId_2(setId);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse(false, "This set does not exist or this set does not contain any cards.",
                            HttpStatus.NOT_FOUND, null)
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(true, "Get data ok.",
                        HttpStatus.OK, result)
        );
    }

    @GetMapping("/count-public-set/{id}")
    public ResponseEntity<?> countSetFlashcardCreatedPublicByUserName(@PathVariable("id") Long userId) {
        return ResponseEntity.ok().body(
                new ApiResponse(true, "...", HttpStatus.OK,
                        setFlashcardService.countSetFlashcardCreatedPublic(userId))
        );
    }

    @GetMapping("/count-set")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> countSetFlashcardCreated() {
        ApiResponse apiResponse = new ApiResponse(true, "ok", HttpStatus.OK,
                setFlashcardService.countSetFlashcardCreatedInCurrentUser());
        return ResponseEntity.status(200).body(apiResponse);
    }

    @GetMapping("/count-set-in-current-date")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> countSetFlashcardCreatedPerDate() {
        ApiResponse apiResponse = new ApiResponse(true, "ok", HttpStatus.OK,
                setFlashcardService.countSetFlashcardCreatedPerDayInCurrentUser());

        return ResponseEntity.status(200).body(apiResponse);
    }

    @GetMapping("/get-current-sets-by-settings")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getSetSettingsByIdOnCurrentUser(@RequestParam Long limit) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        if (limit == null || limit < 1) {
            limit = Long.MAX_VALUE;
        }
        return ResponseEntity.ok(setFlashcardService.getListFlashcardsByNearbySetting(up.getId(), limit));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Object> createSetFlashcard(
            @RequestBody
            @Validated
            SetFlashcardRequest request) {
        try {
            String hashedPassword = (request.getHashPassword() == null || request.getHashPassword().trim().isEmpty())
                    ? null
                    : passwordEncoder.encode(request.getHashPassword());
            setFlashcardService.addSetFlashcard(request.getTitle(),
                    request.getDescriptionSet(),
                    request.getIsApproved(),
                    request.getIsAnonymous(),
                    request.getSharingMode(),
                    hashedPassword,
                    request.getUserId(),
                    request.getCategoryId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Set Flashcard created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the set flashcard" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Object> deleteSetFlashcardById(@PathVariable("id") Long setId) {
        if (setFlashcardService.findBySetId(setId) != null) {
            try {
                setFlashcardService.deleteSetFlashcardAdmin(setId);
                return new ResponseEntity<>("Set Flashcard deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the set flashcard");
            }
        } else {
            return new ResponseEntity<>("Set Flashcard not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Object> updateSetFlashcard(
            @RequestBody
            @Validated({OnUpdate.class, Default.class})
            SetFlashcardRequest request, BindingResult bindingResult) {
        try {
            if (setFlashcardService.findBySetId(request.getSetId()) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Set Flashcard not found");
            }
            setFlashcardService.updateSetFlashcardAdmin(request);
            return new ResponseEntity<>("Set Flashcard updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the set flashcard");
        }
    }

    @PostMapping("/create-new-set")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createSetFlashcard_2(
            @RequestBody
            @Validated
            SetFlashcardInitializeRequest request) {
        Long setId = setFlashcardService.createNewSetFlashcards(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Created set successfully",
                        HttpStatus.OK, setId));
    }

    @PutMapping("/update-set")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> updateSetFlashcard_2(
            @RequestBody
            @Validated({OnUpdate.class, Default.class})
            SetFlashcardRequest request) {
        setFlashcardService.updateSetFlashcard(request);
        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Set Flashcard updated successfully"));
    }

    @DeleteMapping("/delete-set/{id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> deleteSetFlashcardById_2(@PathVariable("id") Long setId) {
        setFlashcardService.deleteSetFlashcard(setId);
        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Set Flashcard deleted successfully"));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Page<ISetFlashcardDTO>> filterByUserIdAndCategoryName(@RequestParam(value = "user_id", defaultValue = "") Long userId,
                                                                                @RequestParam(value = "category_name", defaultValue = "") String categoryName,
                                                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(setFlashcardService.filterByUserIdAndCategoryName(userId, categoryName, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchByTitle(@RequestParam("query") String query) {
        List<SearchSetFlashResponse> set = setFlashcardService.searchByTitleAndCategory(query);
        return ResponseEntity.ok(set);
    }

    @GetMapping("/search-my-course")
    public ResponseEntity<?> searchMyCourse(@RequestParam("query") QueryDTO query) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        List<SearchSetFlashResponse> set = setFlashcardService.searchByMyCourse(query, up.getId());
        return ResponseEntity.ok(set);
    }


    @GetMapping("/sort")
    public ResponseEntity<Object> sortByUpdatedDate() {
        try {
            if (setFlashcardService.sortByUpdatedDate().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No flashcard sets found");
            } else {
                List<ISetFlashcardDTO> set = setFlashcardService.sortByUpdatedDate();
                return ResponseEntity.ok(set);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/list/sets")
    public ResponseEntity<Object> getAllSetByUserId() {
        try {
            if (setFlashcardService.getAllSetByUserId().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("The user does not have any flashcard sets");
            } else {
                List<ISetFlashcardDTO> set = setFlashcardService.getAllSetByUserId();
                return ResponseEntity.ok(set);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/public")
    public ResponseEntity<Object> getAllSetPublic() {
        try {
            if (setFlashcardService.getAllSetPublic().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No flashcard sets found");
            } else {
                List<ISetFlashcardDTO> set = setFlashcardService.getAllSetPublic();
                return ResponseEntity.ok(set);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/public/{userId}")
    public ResponseEntity<Object> getAllSetPublicByUserId(@PathVariable("userId") Long userId) {
        try {
            if (setFlashcardService.getAllSetPublicByUserId(userId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("The user does not have any public flashcard sets");
            } else {
                List<ISetFlashcardDTO> set = setFlashcardService.getAllSetPublicByUserId(userId);
                return ResponseEntity.ok(set);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }
}
