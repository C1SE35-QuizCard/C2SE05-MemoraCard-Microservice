package com.example.quizcards.service.impl;

import com.example.quizcards.dto.response.BenefitPlanResponse;
import com.example.quizcards.entities.CategorySubscription;
import com.example.quizcards.entities.plans.PlansName;
import com.example.quizcards.entities.role.RoleName;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.repository.ICategorySubscriptionRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ICategorySubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategorySubscriptionServiceImpl implements ICategorySubscriptionService {
    @Autowired
    private ICategorySubscriptionRepository categorySubscriptionRepository;


    @Override
    public CategorySubscription getCategorySubscriptionBaseOfRoles() throws ResourceNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        return getCategorySubscriptionResponse(up);
    }

    @Override
    public CategorySubscription getCategorySubscriptionBaseOfUserPrincipal(UserPrincipal up) throws ResourceNotFoundException {
        return getCategorySubscriptionResponse(up);
    }

    @Override
    public ResponseEntity<List<CategorySubscription>> getAll() {
        List<CategorySubscription> categorySubscriptions = categorySubscriptionRepository.findAll();
        return ResponseEntity.ok().body(categorySubscriptions);
    }

    @Override
    public ResponseEntity<?> getSubscriptionByRoles() throws ResourceNotFoundException {
        CategorySubscription cs = getCategorySubscriptionBaseOfRoles();

        Map<String, Object> currentSubscription = new HashMap<>();

        currentSubscription.put("id", cs.getId());
        currentSubscription.put("name", cs.getName());
        currentSubscription.put("expiredMonth", cs.getExpiredMonth());

        return ResponseEntity.ok().body(currentSubscription);
    }

    @Override
    public ResponseEntity<BenefitPlanResponse> getBenefitByRoles() throws ResourceNotFoundException {
        CategorySubscription cs = getCategorySubscriptionBaseOfRoles();

        BenefitPlanResponse response = new BenefitPlanResponse();
        response.setMaxSetsPerDay(cs.getMaxSetsPerDay());
        response.setMaxSetsFlashcards(cs.getMaxSetsFlashcards());
        response.setMaxFlashcardsPerSet(cs.getMaxFlashcardsPerSet());
        response.setMaxRoomsCreatePerDay(cs.getMaxRoomsCreatePerDay());
        response.setMaxTermsPerRoom(cs.getMaxTermsPerRoom());
        response.setExpiredMonth(cs.getExpiredMonth());

        return ResponseEntity.ok().body(response);
    }

    private CategorySubscription getCategorySubscriptionResponse(UserPrincipal up) {
        List<String> roles = up.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String plansName;
        if (roles.contains(RoleName.ROLE_PREMIUM_USER.name()) || roles.contains(RoleName.ROLE_ADMIN.name())) {
            plansName = PlansName.PREMIUM_PLAN.getMessage();
        } else if (roles.contains(RoleName.ROLE_FREE_USER.name())) {
            plansName = PlansName.FREE_PLAN.getMessage();
        } else {
            plansName = "";
        }
        return categorySubscriptionRepository.findByName(plansName)
                .orElseThrow(() -> new ResourceNotFoundException("CategorySubscription", "id", plansName));
    }
}
