package com.example.quizcards.helpers.SetFlashcardHelpers;

import com.example.quizcards.dto.request.FlashcardRequest;
import com.example.quizcards.dto.request.SetFlashcardInitializeRequest;
import com.example.quizcards.dto.request.SetFlashcardRequest;
import com.example.quizcards.entities.CategorySubscription;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.entities.role.RoleName;
import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.AuthenticationHelpers;
import com.example.quizcards.repository.ISetFlashcardRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ICategorySubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SetFlashcardHelperImpl implements ISetFlashcardHelpers {
    ISetFlashcardRepository setRepository;

    AuthenticationHelpers authenticationHelpers;

    ICategorySubscriptionService categorySubscriptionService;

    private void checkSetFlashcardOwner(Long setId, UserPrincipal up) throws AccessDeniedException, ResourceNotFoundException {
        if (setId == null) {
            throw new BadRequestException("Set id not null");
        }

        SetFlashcard set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set", "id", setId));

        if (!up.getId().equals(set.getUser().getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this set flashcard");
        }
    }

    private void checkAddForFreeUser(SetFlashcardInitializeRequest request, Long userId,
                                     CategorySubscription currentCs) {
        if (userId == null) {
            throw new BadRequestException("Set id not null");
        }
        if (setRepository.countNumberOfSetCreated(userId) >= currentCs.getMaxSetsFlashcards()) {
            throw new BadRequestException(String.format("The number of card sets is %d sets per user.",
                    currentCs.getMaxSetsFlashcards()));
        }
        if (setRepository.countNumberOfSetCreatedInCurrentDay(userId) >= currentCs.getMaxSetsPerDay()) {
            throw new BadRequestException(String.format("Maximum of %d sets can be created in one day per user.",
                    currentCs.getMaxSetsPerDay()));
        }
        if (request.getIsAnonymous()) {
            throw new BadRequestException("Self-created card set must be public creator identity, cannot be anonymous.");
        }
        if (request.getFlashcards().size() > currentCs.getMaxFlashcardsPerSet()) {
            throw new BadRequestException(String.format("The maximum number of cards that can be created in a set is %d per user.",
                    currentCs.getMaxFlashcardsPerSet()));
        }
        for (FlashcardRequest flashcards : request.getFlashcards()) {
            if (flashcards.getImageLink() != null && !flashcards.getImageLink().isEmpty()) {
                throw new BadRequestException("User cannot be update any images.");
            }
        }
    }

    private void checkAddForRemainingUser(SetFlashcardInitializeRequest request, Long userId,
                                          CategorySubscription currentCs) {
        if (userId == null) {
            throw new BadRequestException("Set id not null");
        }
        if (request.getFlashcards().size() > currentCs.getMaxFlashcardsPerSet()) {
            throw new BadRequestException(String.format("The maximum number of cards that can be created in a set is %d per user.",
                    currentCs.getMaxFlashcardsPerSet()));
        }
    }

    private void checkUpdateForFreeUser(SetFlashcardRequest request, UserPrincipal up) {
        SetFlashcard set = setRepository.findById(request.getSetId())
                .orElseThrow(() -> new ResourceNotFoundException("Set", "id", request.getSetId()));
        if (request.getIsAnonymous() != null && request.getIsAnonymous()) {
            throw new BadRequestException("Self-created card set must be public creator identity, cannot be anonymous.");
        }
        request.setIsAnonymous(request.getIsAnonymous() == null ? set.getIsAnonymous() : false);
    }

    @Override
    public void handleAddSetFlashcard(SetFlashcardInitializeRequest request) {
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        CategorySubscription currentCs = this.categorySubscriptionService.getCategorySubscriptionBaseOfRoles();
        if (up.getRolesBaseAuthorities().contains(RoleName.ROLE_FREE_USER.name())) {
            checkAddForFreeUser(request, up.getId(), currentCs);
        } else if (up.getRolesBaseAuthorities().contains(RoleName.ROLE_PREMIUM_USER.name())) {
            checkAddForRemainingUser(request, up.getId(), currentCs);
        }
    }

    @Override
    public void handleUpdateSetFlashcard(SetFlashcardRequest request) {
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        if (up.getRolesBaseAuthorities().contains(RoleName.ROLE_FREE_USER.name())) {
            checkSetFlashcardOwner(request.getSetId(), up);
            checkUpdateForFreeUser(request, up);
        } else if (up.getRolesBaseAuthorities().contains(RoleName.ROLE_PREMIUM_USER.name())) {
            checkSetFlashcardOwner(request.getSetId(), up);
        }
    }

    @Override
    public void handleDeleteSetFlashcard(Long setId) {
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        if (!up.getRolesBaseAuthorities().contains(RoleName.ROLE_ADMIN.name())) {
            checkSetFlashcardOwner(setId, up);
        }
    }

    @Override
    public void handleAdminAddSetFlashcard(SetFlashcardInitializeRequest request, Long userId) {

    }

    @Override
    public void handleAdminUpdateSetFlashcard(SetFlashcardRequest request, Long userId) {

    }

    @Override
    public void handleAdminDeleteSetFlashcard(Long setId, Long userId) {

    }


}
