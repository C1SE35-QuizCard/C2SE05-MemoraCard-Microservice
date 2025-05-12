package com.example.quizcards.helpers.DeadlineHelpers;

import com.example.quizcards.dto.request.DeadlineReminderRequest;
import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.entities.DeadlineReminder;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.AuthenticationHelpers;
import com.example.quizcards.repository.IDeadlineReminderRepository;
import com.example.quizcards.repository.ISetFlashcardRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ICustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;

@Component
public class DeadlineHelpersImpl implements IDeadlineHelpers {
    private IDeadlineReminderRepository deadlineRepository;

    private AuthenticationHelpers authenticationHelpers;

    private ICustomUserDetailsService customUserDetailsService;

    private ISetFlashcardRepository setRepository;

    public DeadlineHelpersImpl(IDeadlineReminderRepository deadlineRepository,
                               AuthenticationHelpers authenticationHelpers,
                               ISetFlashcardRepository setFlashcardRepository,
                               ICustomUserDetailsService customUserDetailsService) {
        this.deadlineRepository = deadlineRepository;
        this.authenticationHelpers = authenticationHelpers;
        this.setRepository = setFlashcardRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    private void checkSetExists(Long setId) {
        if (setId == null) {
            throw new BadRequestException("Set id cannot be null");
        }
        SetFlashcard set = setRepository.findById(setId).
                orElseThrow(() -> new ResourceNotFoundException("Set", "id", setId));
    }

    private void checkNotDeadlineValid(DeadlineReminderRequest request, UserPrincipal up) {
        checkSetExists(request.getSetId());
        if (deadlineRepository.existsDeadlineReminderGreaterThanNowBySetId(request.getSetId(), up.getId(),
                request.getReminderTime())) {
            throw new BadRequestException("Deadline reminder in set id: " + request.getSetId().toString() +
                    " already exists");
        }
//        if (deadlineRepository.existsDeadlineReminderGreaterThanNowBySetId(request.getSetId(), up.getId())) {
//            throw new BadRequestException("Deadline reminder in set id: " + request.getSetId().toString() +
//                    " already exists");
//        }
    }

    private void checkDeadlineOwner(Long deadlineId, UserPrincipal up) {
        if (deadlineId == null) {
            throw new BadRequestException("Deadline id cannot be null");
        }
        DeadlineReminder reminder = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", deadlineId));

        if (!up.getId().equals(reminder.getUser().getUserId())) {
            throw new BadRequestException("Deadline owner is not the user");
        }
    }

    private void checkTimeline(DeadlineReminderRequest request) {
        if (request.getReminderTime().toInstant().atZone(ZoneOffset.UTC).toInstant().compareTo(
                Instant.now().atZone(ZoneOffset.UTC).toInstant()
        ) < 0) {
            throw new BadRequestException(
                    new ApiResponse(false, "Cannot set deadline past")
            );
        }
    }

    @Override
    public void handleDeleteDeadline(Long deadlineId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        checkDeadlineOwner(deadlineId, up);
    }

    @Override
    public void handleUpdateDeadline(DeadlineReminderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        checkDeadlineOwner(request.getDeadlineRemindersId(), up);
        checkTimeline(request);
    }

    @Override
    public void handleAddDeadline(DeadlineReminderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        checkNotDeadlineValid(request, up);
        checkTimeline(request);
    }

    @Override
    public void handleAdminDeleteDeadline(Long deadlineId, Long userId) {

    }

    @Override
    public void handleAdminUpdateDeadline(DeadlineReminderRequest request, Long userId) {

    }

    @Override
    public void handleAdminAddDeadline(DeadlineReminderRequest request, Long userId) {

    }
}
