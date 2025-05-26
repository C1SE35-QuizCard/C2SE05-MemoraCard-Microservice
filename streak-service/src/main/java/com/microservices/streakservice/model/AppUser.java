package com.microservices.streakservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_users")
public class AppUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column("user_id")
    private Long userId;

    @Column("address")
    private String address;

    @Column("avatar")
    private String avatar;

    @Column("date_create")
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

    @Column("user_tz")
    private String userTz;

    // khoá ngoại thay vì @ManyToOne
    @Column("role_id")
    private Long roleId;
}