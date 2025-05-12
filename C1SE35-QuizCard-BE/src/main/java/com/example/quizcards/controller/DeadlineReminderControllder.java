package com.example.quizcards.controller;

import com.example.quizcards.dto.DeadlineReminderListDTO;
import com.example.quizcards.dto.IDeadlineReminderDTO;
import com.example.quizcards.dto.request.DeadlineReminderRequest;
import com.example.quizcards.dto.response.ErrorDetail;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IDeadlineReminderService;
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

import java.sql.Timestamp;
import java.time.*;
import java.util.List;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/deadline")
public class DeadlineReminderControllder {

    @Autowired
    private IDeadlineReminderService deadlineReminderService;
    private static final String FETCH_ERROR_MESSAGE = "An error occurred while fetching deadline reminders.";

    @GetMapping("/detail/{id}")
    public ResponseEntity<Object> getDeadlineReminderById(@PathVariable("id") Long id) {
        try {
            if (deadlineReminderService.getDeadlineReminderById(id) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deadline reminders not found");
            }
            IDeadlineReminderDTO reminder = deadlineReminderService.getDeadlineReminderById(id);
            return ResponseEntity.ok(reminder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/set/{set_id}")
    public ResponseEntity<Object> getDeadlineReminderBySetId(@PathVariable("set_id") Long setId) {
        try {
            if (deadlineReminderService.getDeadlineReminderBySetId(setId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deadline reminders not found");
            }
            List<IDeadlineReminderDTO> reminder = deadlineReminderService.getDeadlineReminderBySetId(setId);
            return ResponseEntity.ok(reminder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getDeadlineReminderByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        try {
            if (deadlineReminderService.getDeadlineReminderByUserId(up.getId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deadline reminders not found");
            }
            List<DeadlineReminderListDTO> reminder = deadlineReminderService.getDeadlineReminderByUserId(up.getId());
            return ResponseEntity.ok(reminder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FETCH_ERROR_MESSAGE);
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('NO_ROLE')")
    public ResponseEntity<Object> createDeadlineReminder(@RequestBody @Validated DeadlineReminderRequest request, BindingResult bindingResult) {
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
            if (request.getReminderTime().before(Timestamp.valueOf(LocalDateTime.now()))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reminder time must be at least the current time.");
            }
//            if (deadlineReminderService.existsByUserIdAndSetId(request.getUserId(), request.getSetId())) {
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("A reminder for this user and set already exists.");
//            }
            deadlineReminderService.addDeadlineReminder(request.getReminderTime(), request.getUserId(), request.getSetId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Deadline reminder created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the deadline reminder");
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('NO_ROLE')")
    public ResponseEntity<Object> deleteDeadlineReminder(@PathVariable("id") Long deadlineRemindersId) {
        if (deadlineReminderService.getDeadlineReminderById(deadlineRemindersId) != null) {
            try {
                deadlineReminderService.deleteDeadlineReminder(deadlineRemindersId);
                return new ResponseEntity<>("Flashcard deleted successfully", HttpStatus.OK);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the deadline reminder");
            }
        } else {
            return new ResponseEntity<>("Deadline reminder not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('NO_ROLE')")
    public ResponseEntity<Object> updateDeadlineReminder(@Validated @RequestBody DeadlineReminderRequest request, BindingResult bindingResult) {
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
            if (deadlineReminderService.getDeadlineReminderById(request.getDeadlineRemindersId()) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deadline reminder not found");
            }
            if (request.getReminderTime().before(Timestamp.valueOf(LocalDateTime.now()))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reminder time must be at least the current time.");
            }
//            if (deadlineReminderService.existsByUserIdAndSetIdAndNotId(request.getUserId(), request.getSetId(), request.getDeadlineRemindersId())) {
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("A reminder for this user and set already exists.");
//            }
            deadlineReminderService.updateDeadlineReminder(request);
            return new ResponseEntity<>("Deadline reminder updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the Deadline reminder");
        }
    }


    @PostMapping("/create-deadline")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> createDeadlineReminder_2(@Valid @RequestBody DeadlineReminderRequest request) {
        deadlineReminderService.addDeadlineReminder_2(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Deadline reminder created successfully");
    }

    @DeleteMapping("/delete-deadline")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> deleteDeadlineReminder_2(@Valid @RequestBody DeadlineReminderRequest request) {
        deadlineReminderService.deleteDeadlineReminder_2(request.getSetId());
        return new ResponseEntity<>("Deadline reminder deleted successfully", HttpStatus.OK);
    }

    @PutMapping("/update-deadline")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> updateDeadlineReminder_2(@Valid @RequestBody DeadlineReminderRequest request) {
        deadlineReminderService.updateDeadlineReminder_2(request);
        return new ResponseEntity<>("Deadline reminder updated successfully", HttpStatus.OK);
    }
}
