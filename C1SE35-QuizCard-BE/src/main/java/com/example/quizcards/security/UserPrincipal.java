package com.example.quizcards.security;

import com.example.quizcards.entities.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String firstName;

    private String lastName;

    private String userName;

    private String avatar;

    private boolean gender;

    @JsonIgnore
    private String userCode;

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String phoneNumber;

    @JsonIgnore
    private String address;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private boolean isEnabled;

    @JsonIgnore
    private LocalDate dateOfBirth;

    private Collection<? extends GrantedAuthority> authorities;


    public static UserPrincipal create(AppUser user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getRoleName()));

        return UserPrincipal.builder()
                .id(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(user.getUsername())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .userCode(user.getUserCode())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .password(user.getHashPassword())
                .isEnabled(user.getEnabled())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? null : new ArrayList<>(authorities);
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public List<String> getRolesBaseAuthorities() {
        return authorities == null ? new ArrayList<>() :
                authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }
}
