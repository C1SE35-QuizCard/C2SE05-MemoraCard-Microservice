package com.example.quizcards.controller;

import com.example.quizcards.dto.IFolderDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.dto.request.CreateFolderRequest;
import com.example.quizcards.dto.request.FolderRequest;
import com.example.quizcards.dto.request.UpdateFolderRequest;
import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.dto.response.ErrorDetail;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IFolderService;
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

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/api/v1/folder")
public class FolderController {

    @Autowired
    private IFolderService folderService;
    private static final String FETCH_ERROR_MESSAGE = "An error occurred while fetching folder";

    @GetMapping("/{id}")
    public ResponseEntity<Object> getFolderById(@PathVariable("id") Long folderId) {
        try {
            if (folderService.getFolderById(folderId) == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No folder found for folder ID " + folderId);
            } else {
                IFolderDTO folder = folderService.getFolderById(folderId);
                return ResponseEntity.ok(folder);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Object> getFoldersByUserId() {
        try {
            if (folderService.getFoldersByUserId() == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No folder found for user ID " + up.getId());
            } else {
                List<IFolderDTO> folder = folderService.getFoldersByUserId();
                return ResponseEntity.ok(folder);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/search-folder")
    public ResponseEntity<Object> searchFolderByTitle(@RequestParam("query") String title) {
        try {
            if (folderService.searchFolderByTitle(title).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No folder found for title " + title);
            } else {
                List<IFolderDTO> folders = folderService.searchFolderByTitle(title);
                return ResponseEntity.ok(folders);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/set/{folder_id}")
    public ResponseEntity<Object> getSetByFolderId(@PathVariable("folder_id") Long folderId) {
        try {
            if (folderService.findSetByFolderIdAndUserId(folderId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No set found for folder ID " + folderId);
            } else {
                List<ISetFlashcardDTO> sets = folderService.findSetByFolderIdAndUserId(folderId);
                return ResponseEntity.ok(sets);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<Object> addFolder(@RequestBody @Validated CreateFolderRequest request, BindingResult bindingResult) {
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
            folderService.addFolder(request.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body("Folder created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the folder");
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<Object> deleteFolder(@PathVariable("id") Long folderId) {
        if (folderService.getFolderById(folderId) != null) {
            try {
                folderService.deleteFolder(folderId);
                return new ResponseEntity<>("Folder deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the Folder");
            }
        } else {
            return new ResponseEntity<>("Folder not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<Object> updateFolder(@Validated @RequestBody UpdateFolderRequest request, BindingResult bindingResult) {
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
            if (folderService.getFolderById(request.getFolderId()) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder not found");
            }
            folderService.updateFolder(request);
            return new ResponseEntity<>("Folder updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the Folder");
        }
    }


    @PostMapping("/create-new-folder")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<Object> addFolder_2(@Valid @RequestBody FolderRequest request) {
        folderService.addFolder_2(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Folder created successfully"));
    }

    @DeleteMapping("/delete-folder")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<Object> deleteFolder_2(@Valid @RequestBody FolderRequest request) {
        folderService.deleteFolder_2(request.getFolderId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Folder deleted successfully"));
    }

    @PutMapping("/update-folder")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<Object> updateFolder_2(@Valid @RequestBody FolderRequest request) {
        folderService.updateFolder_2(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Folder updated successfully"));
    }
}
