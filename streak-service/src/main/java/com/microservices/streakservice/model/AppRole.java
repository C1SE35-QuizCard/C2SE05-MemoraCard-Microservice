package com.microservices.streakservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_roles")
public class AppRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column("role_id")
    private Long roleId;

    @Column("role_name")
    private String roleName;
}