CREATE TABLE IF NOT EXISTS `app_roles` (
                             `role_id` bigint NOT NULL AUTO_INCREMENT,
                             `role_name` varchar(50) NOT NULL,
                             PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO app_roles (role_name)
SELECT v.role_name
FROM (
         SELECT 'ROLE_GUEST'        AS role_name
         UNION ALL SELECT 'ROLE_FREE_USER'
         UNION ALL SELECT 'ROLE_PREMIUM_USER'
         UNION ALL SELECT 'ROLE_ADMIN'
     ) AS v
WHERE NOT EXISTS (
    SELECT 1
    FROM app_roles ar
    WHERE ar.role_name = v.role_name
);