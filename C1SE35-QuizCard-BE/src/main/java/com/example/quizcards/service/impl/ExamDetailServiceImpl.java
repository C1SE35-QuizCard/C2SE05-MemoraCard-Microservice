package com.example.quizcards.service.impl;

import com.example.quizcards.dto.IExamDetailDTO;
import com.example.quizcards.dto.request.ExamDetailRequest;
import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.entities.*;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.ExamDetailHelpers.IExamDetailHelpers;
import com.example.quizcards.helpers.TestHelpers.ITestHelpers;
import com.example.quizcards.repository.IExamDetailRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IExamDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamDetailServiceImpl implements IExamDetailService {

    @Autowired
    IExamDetailRepository examDetailRepository;

    @Autowired
    IExamDetailHelpers examDetailHelpers;

    @Autowired
    ITestHelpers examTestHelpers;

    @Override
    public ResponseEntity<?> addAndUpdateExamDetail(ExamDetailRequest request){
        if(examDetailRepository.findExamDetailByTestIdAndCardId(request.getTestId(), request.getCardId()) == null){
            Flashcard flashcard = Flashcard.builder().cardId(request.getCardId()).build();
            Test test = Test.builder().testId(request.getTestId()).build();
            ExamDetail examDetail = new ExamDetail();
            examDetail.setTest(test);
            examDetail.setFlashcard(flashcard);
            examDetail.setYourAnswer(request.getYourAnswer());
            examDetail.setIsTrue(request.getIsTrue());

            examDetailRepository.save(examDetail);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true,
                            "Exam Detail created successfully",
                            HttpStatus.CREATED,
                            examDetail.getExId()));
        }

        Test test = Test.builder().testId(request.getTestId()).build();
        Flashcard fl = Flashcard.builder().cardId(request.getCardId()).build();

        ExamDetail examDetail = examDetailRepository.findExamDetailByTestIdAndCardId(request.getTestId(), request.getCardId());

        examDetailHelpers.handleUpdateExamDetail(examDetail.getExId());

        examDetail.setFlashcard(fl);
        examDetail.setYourAnswer(request.getYourAnswer());
        examDetail.setIsTrue(request.getIsTrue());
        examDetail.setTest(test);
        examDetailRepository.save(examDetail);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Exam Detail updated successfully"));
    }

    @Override
    public ResponseEntity<?> deleteExamDetail(Long ExamDetailId){
        ExamDetail examDetail = examDetailRepository.findById(ExamDetailId)
                .orElseThrow(() -> new RuntimeException("ExamDetail not found for ID: " + ExamDetailId));
        examDetailHelpers.handleDeleteExamDetail(ExamDetailId);
        examDetailRepository.delete(examDetail);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Test deleted successfully"));
    }

    @Override
    public List<IExamDetailDTO> findExamDetailsByTestId(Long testId){
        examTestHelpers.handleAccessTest(testId);
        return examDetailRepository.findExamDetailsByTestId(testId);
    }
}
