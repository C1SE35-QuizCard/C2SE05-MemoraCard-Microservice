CREATE TABLE IF NOT EXISTS `streak_details` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `date_learned` date DEFAULT NULL,
                                  `user_id` bigint NOT NULL,
                                  PRIMARY KEY (`id`),
    UNIQUE KEY `idx_streak_details_user_date` (`user_id`,`date_learned`),
    KEY `idx_streak_details_user_id` (`user_id`),
    KEY `idx_streak_details_date_learned` (`date_learned`),
    CONSTRAINT `FKol0b4vuxd0m53yr5y1ot8d3nj_cascade` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`user_id`) ON DELETE CASCADE
    ) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
