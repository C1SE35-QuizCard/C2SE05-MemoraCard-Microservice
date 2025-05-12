create table if not exists app_user_roles
(
    user_role_id bigint auto_increment primary key,
    user_id      bigint,
    role_id      bigint,
    constraint fk_user_role_user foreign key (user_id) references app_users (user_id) on delete cascade,
    constraint fk_user_role_role foreign key (role_id) references app_roles (role_id) on delete cascade
)
    engine = InnoDB
    default charset = utf8mb4
    collate = utf8mb4_0900_ai_ci;