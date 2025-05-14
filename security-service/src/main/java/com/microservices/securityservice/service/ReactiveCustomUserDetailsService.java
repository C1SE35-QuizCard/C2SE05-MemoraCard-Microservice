package com.microservices.securityservice.service;


import com.microservices.dto.security.UserPrincipal;
import com.microservices.securityservice.model.AppRole;
import com.microservices.securityservice.repository.AppRoleRepository;
import com.microservices.securityservice.repository.AppUserRepository;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReactiveCustomUserDetailsService implements ReactiveUserDetailsService, ICustomUserDetailsService {
    AppRoleRepository roleRepository;

    AppUserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return null;
    }

    @Override
    public Mono<UserDetails> findByUsernameOnly(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with username: " + username)))
                .flatMap(user -> {
                    Mono<AppRole> monoRole = roleRepository.findByRoleId(user.getRoleId());
                    return monoRole.map(appRole -> {
                        List<AppRole> roles = List.of(appRole);
                        
                        Collection<GrantedAuthority> authorities = new ArrayList<>();

                        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getRoleName().trim().toUpperCase())));

                        UserPrincipal up = UserPrincipal.create(user);
                        up.setAuthorities(authorities);

                        return up;
                    });
                });
    }
}
