package com.microservices.securityservice.service;


import com.microservices.dto.security.UserPrincipal;
import com.microservices.securityservice.model.AppUser;
import com.microservices.securityservice.repository.AppRoleRepository;
import com.microservices.securityservice.repository.AppUserRepository;
import com.microservices.utils.ReactiveRedisUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReactiveCustomUserDetailsService implements ReactiveUserDetailsService, ICustomUserDetailsService {
    private static final String CACHE_KEY_PREFIX = "appuser:username:";

    AppRoleRepository roleRepository;

    AppUserRepository userRepository;

    ReactiveRedisUtils redisUtils;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return null;
    }

    @Override
    public Mono<UserDetails> findByUsernameOnly(String username) {
        String key = CACHE_KEY_PREFIX + username + ":reactive";

        // 1) Thử lấy AppUser từ Redis
        return redisUtils.getFromRedis(key, AppUser.class)
                .flatMap(this::buildUserDetails)         // hit cache → build UserDetails
                .switchIfEmpty(                       // miss cache → load từ DB
                        userRepository.findByUsername(username)
                                .switchIfEmpty(Mono.error(
                                        new UsernameNotFoundException("User not found: " + username)))
                                // 2) lưu vào Redis TTL 10 phút
                                .flatMap(this::assignRolesAndPermissionToUser)
                                .flatMap(u ->
                                        redisUtils
                                                .saveToRedis(key, u, 10, TimeUnit.MINUTES)
                                                .thenReturn(u)
                                )
                                .flatMap(this::buildUserDetails)
                );
    }

    private Mono<UserDetails> buildUserDetails(AppUser user) {
        // 3) Lấy role & build authorities
        return Mono.just(user)
                .map(us -> {
                    Collection<GrantedAuthority> auths = new ArrayList<>(List.of());
                    us.getRoles().forEach(
                            roleName -> auths.add(new SimpleGrantedAuthority(roleName.trim().toUpperCase()))
                    );
                    UserPrincipal up = UserPrincipal.create(user);
                    up.setAuthorities(auths);
                    return up;
                });
//      NO CODE:  return roleRepository.findByRoleId(user.getRoleId())
//                .map(role -> {
//                    Collection<GrantedAuthority> auths = List.of(
//                            new SimpleGrantedAuthority(role.getRoleName().trim().toUpperCase())
//                    );
//                    UserPrincipal up = UserPrincipal.create(user);
//                    up.setAuthorities(auths);
//                    return up;
//                });
    }

    private Mono<AppUser> assignRolesAndPermissionToUser(AppUser user) {
        return roleRepository.findByRoleId(user.getRoleId())
                .map(role -> {
                    user.getRoles().add(role.getRoleName().toUpperCase());
                    return user;
                });
    }
}
