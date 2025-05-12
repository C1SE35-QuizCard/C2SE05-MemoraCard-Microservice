package com.example.quizcards.dto;

import com.example.quizcards.entities.AppUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IAppUserDTO {
    Long getUserId();
    String getAddress();
    String getAvatar();
    LocalDateTime getDateCreate();
    LocalDate getDateOfBirth();
    String getEmail();
    Boolean getEnabled();
    String getFirstName();
    Boolean getGender();
    //    String getHashPassword();
    String getLastName();
    String getPhoneNumber();
    String getUsername();
    Long getRoleId();
    String getRoleName();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class AppUserDTO implements IAppUserDTO {
        private Long userId;
        private String address;
        private String avatar;
        private LocalDateTime dateCreate;
        private LocalDate dateOfBirth;
        private String email;
        private Boolean enabled;
        private String firstName;
        private Boolean gender;
        private String lastName;
        private String phoneNumber;
        private String username;
        private Long roleId;
        private String roleName;

        public static IAppUserDTO from(AppUser appUser) {
            return AppUserDTO.builder()
                    .userId(appUser.getUserId())
                    .address(appUser.getAddress())
                    .avatar(appUser.getAvatar())
                    .dateOfBirth(appUser.getDateOfBirth())
                    .email(appUser.getEmail())
                    .enabled(appUser.getEnabled())
                    .username(appUser.getUsername())
                    .gender(appUser.getGender() != null && appUser.getGender())
                    .phoneNumber(appUser.getPhoneNumber())
                    .firstName(appUser.getFirstName())
                    .lastName(appUser.getLastName())
                    .roleId(appUser.getRole().getRoleId())
                    .roleName(appUser.getRole().getRoleName())
                    .build();
        }
    }
}