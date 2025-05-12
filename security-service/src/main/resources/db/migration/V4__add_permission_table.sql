CREATE TABLE IF NOT EXISTS `app_permissions`
(
    `permission_id`   bigint      NOT NULL AUTO_INCREMENT,
    `permission_name` varchar(50) UNIQUE NOT NULL,
    PRIMARY KEY (`permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO app_permissions (permission_name)
SELECT v.permission_name
FROM (
         SELECT 'PERMISSION_READ'  AS permission_name
         UNION ALL SELECT 'PERMISSION_WRITE'
     ) AS v
WHERE NOT EXISTS (
    SELECT 1
    FROM app_permissions ap
    WHERE ap.permission_name = v.permission_name
);