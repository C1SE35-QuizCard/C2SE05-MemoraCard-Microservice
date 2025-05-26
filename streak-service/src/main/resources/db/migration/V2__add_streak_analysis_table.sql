CREATE TABLE IF NOT EXISTS `streak_analysis` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `current_streak` bigint NOT NULL,
                                   `day_learned` bigint NOT NULL,
                                   `last_updated` date DEFAULT NULL,
                                   `longest_streak` bigint NOT NULL,
                                   `user_id` bigint NOT NULL,
                                   PRIMARY KEY (`id`),
    UNIQUE KEY `UKcqjem5te4awrhddmyx3hrl3km` (`user_id`),
    KEY `idx_streak_analysis_user_id` (`user_id`),
    CONSTRAINT `FKiw7rryiee22nx0x8sldkl9wtb_cascade` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`user_id`) ON DELETE CASCADE
    ) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
