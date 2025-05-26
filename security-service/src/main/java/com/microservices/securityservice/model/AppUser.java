package com.microservices.securityservice.model;

import com.microservices.dto.security.IAppUserInfo;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Table("app_users")
public class AppUser implements Serializable, IAppUserInfo {
    private static final long serialVersionUID = 1L;

    @Id
    @Column("user_id")
    private Long userId;

    @Column("address")
    private String address;

    @Column("avatar")
    private String avatar;

    @Column("date_create")
    @CreatedDate
    private LocalDateTime dateCreate;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @Column("email")
    private String email;

    @Column("enabled")
    private Boolean enabled;

    @Column("user_name")
    private String username;

    @Column("gender")
    private Boolean gender;

    @Column("hash_password")
    private String hashPassword;

    @Column("phone_number")
    private String phoneNumber;

    @Column("user_code")
    private String userCode;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("role_id")
    private Long roleId;

    @Column("user_tz")
    private String userTz;

    @Transient
    private List<String> roles = new ArrayList<>();

    @Transient
    private List<String> permissions = new ArrayList<>();

    @Override
    public String getUserName() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return hashPassword;
    }

    @Override
    public Boolean getIsEnabled() {
        return enabled;
    }

    @Override
    public Boolean getConfirmed() {
        return getIsEnabled();
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public List<String> getPermissions() {
        return permissions;
    }
}
