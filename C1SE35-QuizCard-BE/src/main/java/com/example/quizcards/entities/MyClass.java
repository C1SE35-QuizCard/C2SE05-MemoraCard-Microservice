package com.example.quizcards.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "myclasses")
@EqualsAndHashCode(exclude = {"folders", "members"})
@ToString(exclude = {"folders", "members"})
public class MyClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "myclass_id")
    private Long myClassId;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "description")
    private String description;

    @Column(name = "class_code", unique = true, length = 8)
    private String classCode;

    @ManyToMany(mappedBy = "myClasses")
    private Set<Folder> folders = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private AppUser owners;

    // Mối quan hệ nhiều-nhiều với AppUser (members)
    @ManyToMany
    @JoinTable(
            name = "myclass_members",
            joinColumns = @JoinColumn(name = "myclass_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> members = new HashSet<>();

    // Gán giá trị mặc định trước khi lưu vào database
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (classCode == null) {
            classCode = generateClassCode();
        }
    }

    private String generateClassCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}