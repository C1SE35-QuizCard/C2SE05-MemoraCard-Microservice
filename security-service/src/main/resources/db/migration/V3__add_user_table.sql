CREATE TABLE IF NOT EXISTS `app_users` (
                             `user_id` bigint NOT NULL AUTO_INCREMENT,
                             `address` varchar(255) DEFAULT NULL,
                             `avatar` varchar(255) DEFAULT NULL,
                             `date_create` datetime(6) NOT NULL,
                             `date_of_birth` date DEFAULT NULL,
                             `email` varchar(255) NOT NULL,
                             `enabled` bit(1) DEFAULT NULL,
                             `first_name` varchar(50) DEFAULT NULL,
                             `gender` bit(1) DEFAULT NULL,
                             `hash_password` text NOT NULL,
                             `last_name` varchar(50) DEFAULT NULL,
                             `phone_number` varchar(255) DEFAULT NULL,
                             `user_code` varchar(255) DEFAULT NULL,
                             `user_name` varchar(50) NOT NULL,
                             `user_tz` varchar(9) default 'Z',
                             `role_id` bigint DEFAULT NULL,
                             PRIMARY KEY (`user_id`),
                             UNIQUE KEY `UKcdpifjw6dh83f6du6wxeo1xip` (`user_name`),
                             UNIQUE KEY `UK4vj92ux8a2eehds1mdvmks473` (`email`),
                             UNIQUE KEY `UKluegqhnh97oetaxjqdu6eqkpp` (`user_code`),
                             KEY `fk_user_role_cascade` (`role_id`),
                             CONSTRAINT `fk_user_role_cascade` FOREIGN KEY (`role_id`) REFERENCES `app_roles` (`role_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


ALTER TABLE `app_users`
MODIFY COLUMN `enabled` tinyint(1) DEFAULT TRUE;

ALTER TABLE `app_users`
    MODIFY COLUMN `gender` tinyint(1) DEFAULT TRUE;

-- ALTER TABLE `app_users`
--     ADD COLUMN `test_col` varchar(2);