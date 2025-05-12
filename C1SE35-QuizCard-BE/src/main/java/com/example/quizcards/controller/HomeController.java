package com.example.quizcards.controller;

import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IHomeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/home")
public class HomeController {
    @Autowired
    private IHomeService homeService;

    @GetMapping("/data")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getHomeData(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        return homeService.getHomeData(up.getId(), response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getHomeDataAdmin(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return homeService.getHomeDataAdmin(authentication, response);
    }

    @GetMapping("/anonymous")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> getHomeDataFreerUser(HttpServletResponse response) {
        return homeService.getHomeDataGuest(response);
    }
}
