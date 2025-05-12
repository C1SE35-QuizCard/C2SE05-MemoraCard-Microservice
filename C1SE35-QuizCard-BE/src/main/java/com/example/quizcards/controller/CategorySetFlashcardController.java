package com.example.quizcards.controller;

import com.example.quizcards.dto.ICategorySetFlashcardDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.dto.request.CategorySetFlashcardAdminRequest;
import com.example.quizcards.dto.response.ErrorDetail;
import com.example.quizcards.service.ICategorySetFlashcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/category")
public class CategorySetFlashcardController {

    @Autowired
    private ICategorySetFlashcardService categorySetFlashcardService;
    private static final String FETCH_ERROR_MESSAGE = "An error occurred while fetching category set flashcards";

    @GetMapping("/list")
    public ResponseEntity<Object> findAllCategorySetFlashcard() {
        try {
            if (categorySetFlashcardService.getAll().isEmpty()) {
                return new ResponseEntity<>("No Categories Found", HttpStatus.NO_CONTENT);
            } else {
                List<ICategorySetFlashcardDTO> categories = categorySetFlashcardService.getAll();
                return ResponseEntity.ok(categories);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<Object> findAllSetFlashcardsByCategoryId(@PathVariable("id") Long categoryId) {
        try {
            if (!categorySetFlashcardService.findAllSetFlashcardsByCategoryId(categoryId).isEmpty()) {
                List<ISetFlashcardDTO> setFlashcards = categorySetFlashcardService. findAllSetFlashcardsByCategoryId(categoryId);
                return ResponseEntity.ok(setFlashcards);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No set flashcards found for category ID " + categoryId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Object> detailCategorySetFlashcardById(@PathVariable("id") Long categoryId) {
        try {
            if (categorySetFlashcardService.getCategorySetFlashcardById(categoryId) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category Set Flashcard not found");
            }
            ICategorySetFlashcardDTO category = categorySetFlashcardService.getCategorySetFlashcardById(categoryId);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createCategorySetFlashcard(@RequestBody @Validated CategorySetFlashcardAdminRequest request, BindingResult bindingResult) {
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
            categorySetFlashcardService.addCategorySetFlashcard(request.getCategoryName());
            return ResponseEntity.status(HttpStatus.CREATED).body("Category Set Flashcard created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the Category set flashcard");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteCategorySetFlashcardById(@PathVariable("id") Long categoryId) {
        if (categorySetFlashcardService.getCategorySetFlashcardById(categoryId) != null) {
            try {
                categorySetFlashcardService.deleteCategorySetFlashcard(categoryId);
                return new ResponseEntity<>("Category Set Flashcard deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the Category set flashcard:");
            }
        } else {
            return new ResponseEntity<>("Category Set Flashcard not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Object> updateCategorySetFlashcard(@Validated @RequestBody CategorySetFlashcardAdminRequest request, BindingResult bindingResult) {
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
            if (categorySetFlashcardService.getCategorySetFlashcardById(request.getCategoryId()) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category Set Flashcard not found");
            }
            categorySetFlashcardService.updateCategorySetFlashcard(request);
            return new ResponseEntity<>("Category Set Flashcard updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the Category set flashcard");
        }
    }

    @GetMapping("/list-pagination")
    public ResponseEntity<Object> getAllCategoryWithPagination(@RequestParam(value = "page", defaultValue = "0") int page,
                                                               @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Page<ICategorySetFlashcardDTO> data = categorySetFlashcardService.getAllCategoryWithPagination(page, size);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "message", "An unknown error occurred."
                    ));
        }
    }

    @GetMapping("/list2/{id}")
    public ResponseEntity<Object> findAllSetFlashcardsByCategoryId2(@PathVariable("id") Long categoryId,
                                                                    @RequestParam(value = "page", required = false) Integer pages,
                                                                    PagedResourcesAssembler<ISetFlashcardDTO> assembler) {
        try {
            if (pages == null) { pages = 0; }
            Page<ISetFlashcardDTO> data = categorySetFlashcardService.findAllSetFlashcardsByCategoryId2(categoryId, pages);
            return ResponseEntity.ok(assembler.toModel(data));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "message", "An unknown error occurred."
                    ));
        }
    }
}
