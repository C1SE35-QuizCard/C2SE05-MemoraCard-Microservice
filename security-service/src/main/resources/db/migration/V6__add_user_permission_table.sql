CREATE TABLE IF NOT EXISTS app_user_permissions
(
    user_permission_id bigint auto_increment primary key,
    user_id            bigint,
    permission_id      bigint,
    constraint fk_user_permission_user foreign key (user_id) references app_users (user_id) on delete cascade,
    constraint fk_user_permission_permission foreign key (permission_id) references app_permissions (permission_id) on delete cascade
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;