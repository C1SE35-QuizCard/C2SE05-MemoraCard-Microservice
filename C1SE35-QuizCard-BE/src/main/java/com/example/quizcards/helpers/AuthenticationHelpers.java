package com.example.quizcards.helpers;

import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IAppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHelpers {
    @Autowired
    private IAppUserService appUserService;

    public Authentication getAuthenticationAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("Access denied");
        }
        return authentication;
    }
}
