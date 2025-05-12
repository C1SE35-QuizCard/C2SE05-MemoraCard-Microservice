package com.example.quizcards.helpers.ExamDetailHelpers;

import com.example.quizcards.dto.request.ExamDetailRequest;
import com.example.quizcards.dto.request.FlashcardRequest;
import com.example.quizcards.entities.ExamDetail;
import com.example.quizcards.entities.Test;
import com.example.quizcards.entities.role.RoleName;
import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.AuthenticationHelpers;
import com.example.quizcards.repository.IExamDetailRepository;
import com.example.quizcards.repository.ITestRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ICustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ExamDetailHelpersImpl implements IExamDetailHelpers{

    @Autowired
    private ICustomUserDetailsService customUserDetailsService;

    @Autowired
    private AuthenticationHelpers authenticationHelpers;

    @Autowired
    private IExamDetailRepository examDetailRepository;

    @Autowired
    private ITestRepository testRepository;

    public ExamDetailHelpersImpl(ICustomUserDetailsService customUserDetailsService,
                           AuthenticationHelpers authenticationHelpers,
                                 IExamDetailRepository examDetailRepository) {
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationHelpers = authenticationHelpers;
        this.examDetailRepository = examDetailRepository;
    }

    private void checkExamDetailOwner(Long examDetailId, UserPrincipal up) throws AccessDeniedException, ResourceNotFoundException {
        ExamDetail examDetail = examDetailRepository.findById(examDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam Detail", "id", examDetailId));

        Test test = testRepository.findTestId(examDetail.getTest().getTestId());
        if (!up.getId().equals(test.getUser().getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this exam Detail");
        }
    }

    private void checkCurrentUserOwnerExamDetail(Long examDetailId) {
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        ExamDetail examDetail = examDetailRepository.findById(examDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam Detail", "id", examDetailId));

        Test test = testRepository.findTestId(examDetail.getTest().getTestId());

        checkExamDetailOwner(examDetailId, up);
    }

    private void checkExamDetailExists(Long examDetailId) throws ResourceNotFoundException {
        Integer examDetailExists = examDetailRepository.countExamDetailById(examDetailId);

        if (examDetailExists == null || examDetailExists == 0) {
            throw new ResourceNotFoundException("Exam Detail", "id", examDetailId);
        }
    }

    @Override
    public void handleDeleteExamDetail(Long examDetailId){
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        if (!up.getRolesBaseAuthorities().contains(RoleName.ROLE_ADMIN.name())) {
            checkExamDetailOwner(examDetailId, up);
            checkExamDetailExists(examDetailId);
        }
        checkCurrentUserOwnerExamDetail(examDetailId);
    }

    @Override
    public void handleUpdateExamDetail(Long examDetailId){
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        checkExamDetailOwner(examDetailId, up);
    }

    @Override
    public void handleAdminDeleteExamDetail(Long examDetailId, Long userId){

    }
}
