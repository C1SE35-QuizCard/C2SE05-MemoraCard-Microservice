package com.microservices.securityservice.model;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Table("app_role_permissions")
public class AppRolePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column("role_permission_id")
    private Long rolePermissionId;

    @Column("role_id")
    private Long roleId;

    @Column("permission_id")
    private Long permissionId;
}
