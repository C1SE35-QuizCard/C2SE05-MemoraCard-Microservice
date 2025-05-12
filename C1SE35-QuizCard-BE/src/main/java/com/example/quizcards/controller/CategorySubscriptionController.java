package com.example.quizcards.controller;

import com.example.quizcards.service.ICategorySubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/category-subscription")
public class CategorySubscriptionController {
    @Autowired
    private ICategorySubscriptionService categorySubscriptionService;

    @GetMapping("/current-benefit")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getCurrentBenefit() {
        return categorySubscriptionService.getBenefitByRoles();
    }

    @GetMapping("/current-subscription")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getCurrentSubscription() {
        return categorySubscriptionService.getSubscriptionByRoles();
    }

    @GetMapping("/all-subscriptions")
    public ResponseEntity<?> getAllSubscriptions() {
        return categorySubscriptionService.getAll();
    }
}
