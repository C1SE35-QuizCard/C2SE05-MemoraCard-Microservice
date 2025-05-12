package com.example.quizcards.helpers.FolderHelpers;

import com.example.quizcards.dto.request.FolderRequest;
import com.example.quizcards.entities.Folder;
import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.AuthenticationHelpers;
import com.example.quizcards.repository.IFolderRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ICustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class FolderHelpersImpl implements IFolderHelpers {
    private ICustomUserDetailsService customUserDetailsService;

    private AuthenticationHelpers authenticationHelpers;

    private IFolderRepository folderRepository;

    public FolderHelpersImpl(ICustomUserDetailsService customUserDetailsService,
                             AuthenticationHelpers authenticationHelpers,
                             IFolderRepository folderRepository) {
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationHelpers = authenticationHelpers;
        this.folderRepository = folderRepository;
    }

    private void checkFolderOwner(Long folderId, UserPrincipal up) throws AccessDeniedException, ResourceNotFoundException {
        if (folderId == null) {
            throw new BadRequestException("Folder id cannot be null");
        }
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder", "id", folderId));

        if (!up.getId().equals(folder.getUser().getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this folder");
        }
    }

    private void checkCurrentUserOwnerFolder(Long folderId) {
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        checkFolderOwner(folderId, up);
    }

    @Override
    public void handleFolderOwner(Long folderId) {
        checkCurrentUserOwnerFolder(folderId);
    }

    @Override
    public void handleAdminDeleteFolder(Long folderId, Long userId) {
        if (folderId == null) {
            throw new BadRequestException("Folder id cannot be null");
        }
        if (userId == null) {
            throw new BadRequestException("User id cannot be null");
        }
    }

    @Override
    public void handleAdminUpdateFolder(FolderRequest request, Long userId) {
        if (request.getFolderId() == null) {
            throw new BadRequestException("Folder id cannot be null");
        }
        if (userId == null) {
            throw new BadRequestException("User id cannot be null");
        }
    }
}
