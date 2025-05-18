package com.microservices.notificationservice.controller;

import com.microservices.dto.security.UserPrincipal;
import com.microservices.notificationservice.model.NotificationEntry;
import com.microservices.notificationservice.service.NotificationCRUDService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationCRUDService service;

//    @PostMapping
//    NO CODE: public Mono<NotificationEntry> create(
//            @RequestParam String userId,
//            @Valid @RequestBody NotificationEntry payload
//    ) {
//        return service.create(userId, payload);
//    }

    @GetMapping
    public Flux<NotificationEntry> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean preferUnread
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(UserPrincipal.class)
                .flatMapMany(principal -> {
                    String userId = principal.getId().toString();
                    return service.getByUser(userId, page, size, preferUnread);
                });
    }

//    @GetMapping("/batch")
//   NO CODE: public Flux<NotificationEntry> batch(
//            @RequestParam String userId,
//            @RequestParam List<String> ids
//    ) {
//        return service.getByIds(userId, ids);
//    }

    @PatchMapping("/read")
    public Flux<NotificationEntry> markRead(
            @RequestBody List<String> ids
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(UserPrincipal.class)
                .flatMapMany(principal ->
                        service.markAsRead(principal.getId().toString(), ids)
                );
    }
}
