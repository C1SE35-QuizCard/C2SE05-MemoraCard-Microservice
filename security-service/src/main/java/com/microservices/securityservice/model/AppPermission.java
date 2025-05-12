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
@Table("app_permissions")
public class AppPermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column("permission_id")
    private Long permissionId;

    @Column("permission_name")
    private String permissionName;
}
