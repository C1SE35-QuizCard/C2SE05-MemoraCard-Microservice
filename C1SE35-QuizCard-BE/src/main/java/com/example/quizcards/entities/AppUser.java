package com.example.quizcards.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_name"),
        @UniqueConstraint(columnNames = "user_code"),
        @UniqueConstraint(columnNames = "email")
})
public class AppUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "date_create", nullable = false)
    @CreationTimestamp
    private LocalDateTime dateCreate;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "enabled")
    private Boolean enabled;

    @NotNull
    @Column(name = "user_name", unique = true, length = 50)
    private String username;

    @Column(name = "gender")
    private Boolean gender;

    @NotNull
    @Column(name = "hash_password", columnDefinition = "TEXT")
    private String hashPassword;

    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    @Column(name = "user_code", unique = true)
    private String userCode;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_role"))
    private AppRole role;


    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    private Set<MyClass> joinedClasses = new HashSet<>();

    public AppUser(Long userId) {
        this.userId=userId;
    }
}