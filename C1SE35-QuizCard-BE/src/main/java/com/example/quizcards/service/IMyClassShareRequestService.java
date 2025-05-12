package com.example.quizcards.service;

import com.example.quizcards.entities.MyClassShareRequest;
import com.example.quizcards.entities.ShareRequestStatus;

import java.util.List;

public interface IMyClassShareRequestService {
    MyClassShareRequest createShareRequestByClassCode(String classCode, Long requesterId);
    List<MyClassShareRequest> getPendingRequests();
    List<MyClassShareRequest> getRequestsByMyClass(Long myClassId);
    List<MyClassShareRequest> getRequestsByRequester(Long userId);
    boolean processShareRequest(Long requestId, ShareRequestStatus status, Long adminId);
    boolean hasPendingRequest(Long myClassId, Long userId);
}