package com.example.quizcards.repository;

import com.example.quizcards.entities.MyClassShareRequest;
import com.example.quizcards.entities.ShareRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMyClassShareRequestRepository extends JpaRepository<MyClassShareRequest, Long> {
    List<MyClassShareRequest> findByStatus(ShareRequestStatus status);
    List<MyClassShareRequest> findByMyClass_MyClassId(Long myClassId);
    List<MyClassShareRequest> findByRequester_UserId(Long userId);
    boolean existsByMyClass_MyClassIdAndRequester_UserId(Long myClassId, Long userId);
}