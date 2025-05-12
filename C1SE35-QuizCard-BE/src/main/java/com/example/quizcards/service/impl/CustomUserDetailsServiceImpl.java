package com.example.quizcards.service.impl;

import com.example.quizcards.entities.AppUser;
import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.repository.IAppUserRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ICustomUserDetailsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomUserDetailsServiceImpl implements UserDetailsService, ICustomUserDetailsService {
    IAppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException, AccessDeniedException {
        AppUser user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with this username or email: %s", usernameOrEmail)));
//        throwAccessDeniedException(user);
        return UserPrincipal.create(user);
    }

    @Override
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException, AccessDeniedException {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with id: %s", id)));
//        throwAccessDeniedException(user);
        return UserPrincipal.create(user);
    }

    @Override
    public UserDetails loadUserByUsernameOnly(String username)
            throws UsernameNotFoundException, AccessDeniedException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with this username: %s", username)));
//        throwAccessDeniedException(user);
        return UserPrincipal.create(user);
    }

    @Override
    public UserDetails loadUserByEmailOnly(String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with this email: %s", email)));
//        throwAccessDeniedException(user);
        return UserPrincipal.create(user);
    }

    @Override
    public UserDetails loadUserByUserCodeOnly(String userCode)
            throws UsernameNotFoundException, AccessDeniedException {
        AppUser user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found with this code: %s", userCode)));
//        throwAccessDeniedException(user);
        return UserPrincipal.create(user);
    }

    private void throwAccessDeniedException(AppUser user) throws AccessDeniedException {
        if (!user.getEnabled()) {
            throw new AccessDeniedException(String.format("User with username: %s is banned", user.getUsername()));
        }
    }
}