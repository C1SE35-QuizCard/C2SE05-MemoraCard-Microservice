package com.example.quizcards.controller;

import com.example.quizcards.dto.IExamDetailDTO;
import com.example.quizcards.dto.request.ExamDetailRequest;
import com.example.quizcards.service.IExamDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/exam-detail")
public class ExamDetailController {

    @Autowired
    private IExamDetailService examDetailService;

    @PostMapping("/create-update")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> createOrUpdateExamDetail(@RequestBody @Validated ExamDetailRequest request){
        return examDetailService.addAndUpdateExamDetail(request);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> deleteExamDetail(@PathVariable("id") Long examDetailId) {
        return examDetailService.deleteExamDetail(examDetailId);
    }

    @GetMapping("/test/{id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public List<IExamDetailDTO> findExamDetailsByTestId(@PathVariable("id") Long testId) {
        return examDetailService.findExamDetailsByTestId(testId);
    }
}
