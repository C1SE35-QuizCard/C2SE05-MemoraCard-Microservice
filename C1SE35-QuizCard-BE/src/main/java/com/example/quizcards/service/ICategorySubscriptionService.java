package com.example.quizcards.service;


import com.example.quizcards.dto.response.BenefitPlanResponse;
import com.example.quizcards.entities.CategorySubscription;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.security.UserPrincipal;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ICategorySubscriptionService {
    CategorySubscription getCategorySubscriptionBaseOfRoles() throws ResourceNotFoundException;

    CategorySubscription getCategorySubscriptionBaseOfUserPrincipal(UserPrincipal up) throws ResourceNotFoundException;

    ResponseEntity<List<CategorySubscription>> getAll();

    ResponseEntity<?> getSubscriptionByRoles() throws ResourceNotFoundException;

    ResponseEntity<BenefitPlanResponse> getBenefitByRoles() throws ResourceNotFoundException;
}
