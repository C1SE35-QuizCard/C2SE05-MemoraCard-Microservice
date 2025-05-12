package com.example.quizcards.service;

import com.example.quizcards.dto.IExamDetailDTO;
import com.example.quizcards.dto.request.ExamDetailRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IExamDetailService {
    ResponseEntity<?> addAndUpdateExamDetail(ExamDetailRequest request);

    ResponseEntity<?> deleteExamDetail(Long ExamDetailId);

    List<IExamDetailDTO> findExamDetailsByTestId(Long testId);
}
