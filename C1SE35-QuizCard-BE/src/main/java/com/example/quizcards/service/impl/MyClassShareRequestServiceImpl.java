package com.example.quizcards.service.impl;

import com.example.quizcards.entities.*;
import com.example.quizcards.repository.IMyClassRepository;
import com.example.quizcards.repository.IMyClassShareRequestRepository;
import com.example.quizcards.service.IMyClassShareRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MyClassShareRequestServiceImpl implements IMyClassShareRequestService {

    @Autowired
    private IMyClassShareRequestRepository shareRequestRepository;

    @Autowired
    private IMyClassRepository myClassRepository;


    @Override
    public List<MyClassShareRequest> getPendingRequests() {
        return shareRequestRepository.findByStatus(ShareRequestStatus.PENDING);
    }

    @Override
    public List<MyClassShareRequest> getRequestsByMyClass(Long myClassId) {
        return shareRequestRepository.findByMyClass_MyClassId(myClassId);
    }

    @Override
    public List<MyClassShareRequest> getRequestsByRequester(Long userId) {
        return shareRequestRepository.findByRequester_UserId(userId);
    }

    @Override
    public boolean processShareRequest(Long requestId, ShareRequestStatus status, Long adminId) {
        MyClassShareRequest request = shareRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != ShareRequestStatus.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }

        request.setStatus(status);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(new AppUser(adminId));

        if (status == ShareRequestStatus.APPROVED) {
            MyClass myClass = request.getMyClass();
            myClass.getMembers().add(request.getRequester());
            myClassRepository.save(myClass);
        }

        shareRequestRepository.save(request);
        return true;
    }

    @Override
    public boolean hasPendingRequest(Long myClassId, Long userId) {
        return shareRequestRepository.existsByMyClass_MyClassIdAndRequester_UserId(myClassId, userId);
    }

    @Override
    public MyClassShareRequest createShareRequestByClassCode(String classCode, Long requesterId) {
        MyClass myClass = myClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Class not found with code: " + classCode));

        if (hasPendingRequest(myClass.getMyClassId(), requesterId)) {
            throw new RuntimeException("You already have a pending request for this class");
        }

        // Kiểm tra nếu người dùng là owner của lớp
        if (myClass.getOwners() != null && myClass.getOwners().getUserId().equals(requesterId)) {
            throw new RuntimeException("You are already the owner of this class");
        }

        // Kiểm tra nếu người dùng đã là thành viên của lớp
        if (myClass.getMembers().stream().anyMatch(member -> member.getUserId().equals(requesterId))) {
            throw new RuntimeException("You are already a member of this class");
        }

        MyClassShareRequest request = MyClassShareRequest.builder()
                .myClass(myClass)
                .requester(new AppUser(requesterId))
                .status(ShareRequestStatus.PENDING)
                .build();

        return shareRequestRepository.save(request);
    }
}