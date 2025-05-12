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
@Table("app_user_permissions")
public class AppUserPermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column("user_permission_id")
    private Long userPermissionId;

    @Column("user_id")
    private Long userId;

    @Column("permission_id")
    private Long permissionId;
}
