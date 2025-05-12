package com.microservices.securityservice.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Table("app_roles")
public class AppRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column("role_id")
    private Long roleId;

    @Column("role_name")
    private String roleName;
}
