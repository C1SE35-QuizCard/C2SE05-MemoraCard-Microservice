package com.example.quizcards.service.impl;

import com.example.quizcards.dto.DeadlineReminderListDTO;
import com.example.quizcards.dto.IDeadlineReminderDTO;
import com.example.quizcards.dto.request.DeadlineReminderRequest;
import com.example.quizcards.helpers.DeadlineHelpers.IDeadlineHelpers;
import com.example.quizcards.repository.IDeadlineReminderRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IDeadlineReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DeadlineReminderServiceImpl implements IDeadlineReminderService {

    @Autowired
    private IDeadlineReminderRepository deadlineReminderRepository;

    @Autowired
    private IDeadlineHelpers deadlineHelpers;

    @Override
    public IDeadlineReminderDTO getDeadlineReminderById(Long id) {
        return deadlineReminderRepository.findDeadlineReminderById(id);
    }

    @Override
    public List<DeadlineReminderListDTO> getDeadlineReminderByUserId(Long userId) {
        return deadlineReminderRepository.findDeadlineReminderByUserId(userId);
    }

    @Override
    public List<IDeadlineReminderDTO> getDeadlineReminderBySetId(Long setId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        return deadlineReminderRepository.findDeadlineReminderBySetId(setId,up.getId());
    }

    @Override
    public void addDeadlineReminder(Timestamp reminderTime, Long userId, Long setId) {
        deadlineReminderRepository.createDeadlineReminder(reminderTime, userId, setId);
    }

    @Override
    public void deleteDeadlineReminder(Long deadlineRemindersId) {
        deadlineReminderRepository.deleteDeadlineReminder(deadlineRemindersId);
    }

    @Override
    public void updateDeadlineReminder(DeadlineReminderRequest request) {
        deadlineReminderRepository.updateDeadlineReminder(request.getDeadlineRemindersId(), request.getReminderTime(), request.getUserId(), request.getSetId());
    }

    @Override
    public void addDeadlineReminder_2(DeadlineReminderRequest request) {
        deadlineHelpers.handleAddDeadline(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
//        Timestamp utcTimestamp = Timestamp.from(request.getReminderTime().toInstant().minusSeconds(
//                ZoneId.systemDefault().getRules().getOffset(request.getReminderTime().toInstant())
//                        .getTotalSeconds())
//        );
        deadlineReminderRepository.createDeadlineReminder(request.getReminderTime(), up.getId(), request.getSetId());
    }

    @Override
    public void deleteDeadlineReminder_2(Long deadlineRemindersId) {
        deadlineHelpers.handleDeleteDeadline(deadlineRemindersId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        deadlineReminderRepository.deleteDeadlineReminder(deadlineRemindersId);
    }

    @Override
    public void updateDeadlineReminder_2(DeadlineReminderRequest request) {
        deadlineHelpers.handleUpdateDeadline(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
//        Timestamp utcTimestamp = Timestamp.from(request.getReminderTime().toInstant().minusSeconds(
//                ZoneId.systemDefault().getRules().getOffset(request.getReminderTime().toInstant())
//                        .getTotalSeconds())
//        );
        deadlineReminderRepository.updateDeadlineReminder(request.getDeadlineRemindersId(), request.getReminderTime(),
                up.getId(), request.getSetId());
    }

//    @Override
//    public boolean existsByUserIdAndSetId(Long userId, Long setId){
//        return deadlineReminderRepository.existsByUserIdAndSetId(userId, setId) != 0;
//    }
//
//    @Override
//    public boolean existsByUserIdAndSetIdAndNotId(Long userId, Long setId, Long deadlineRemindersId) {
//        return deadlineReminderRepository.existsByUserIdAndSetIdAndNotId(userId, setId, deadlineRemindersId) > 0;
//    }
}
