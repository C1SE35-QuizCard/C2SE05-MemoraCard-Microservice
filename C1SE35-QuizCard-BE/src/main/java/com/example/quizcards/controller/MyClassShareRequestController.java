package com.example.quizcards.controller;

import com.example.quizcards.entities.MyClassShareRequest;
import com.example.quizcards.entities.ShareRequestStatus;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IMyClassShareRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/share-requests")
public class MyClassShareRequestController {

    @Autowired
    private IMyClassShareRequestService shareRequestService;

    @PostMapping("/{classCode}")
    public ResponseEntity<?> createShareRequest(@PathVariable String classCode) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            Long userId = user.getId();

            MyClassShareRequest request = shareRequestService.createShareRequestByClassCode(classCode, userId);
            return new ResponseEntity<>(request, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MyClassShareRequest>> getPendingRequests() {
        List<MyClassShareRequest> requests = shareRequestService.getPendingRequests();
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @GetMapping("/my-class/{myClassId}")
    public ResponseEntity<List<MyClassShareRequest>> getRequestsByMyClass(@PathVariable Long myClassId) {
        List<MyClassShareRequest> requests = shareRequestService.getRequestsByMyClass(myClassId);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @GetMapping("/requester")
    public ResponseEntity<List<MyClassShareRequest>> getRequestsByRequester() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        Long userId = user.getId();

        List<MyClassShareRequest> requests = shareRequestService.getRequestsByRequester(userId);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @PutMapping("/{requestId}/process")
    public ResponseEntity<?> processShareRequest(
            @PathVariable Long requestId,
            @RequestParam ShareRequestStatus status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            Long adminId = user.getId();

            boolean success = shareRequestService.processShareRequest(requestId, status, adminId);
            if (success) {
                return new ResponseEntity<>("Request processed successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to process request", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}