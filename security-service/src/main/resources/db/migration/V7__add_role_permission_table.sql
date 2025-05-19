CREATE TABLE IF NOT EXISTS app_roles_permissions
(
    role_permission_id bigint auto_increment primary key,
    role_id            bigint,
    permission_id      bigint,
    constraint fk_role_permission_role foreign key (role_id) references app_roles (role_id) on delete cascade,
    constraint fk_role_permission_permission foreign key (permission_id) references app_permissions (permission_id) on delete cascade
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;