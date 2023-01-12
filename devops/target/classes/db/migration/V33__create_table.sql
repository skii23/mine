CREATE TABLE IF NOT EXISTS `devops_measure_customize`
(
    `id`            int NOT NULL AUTO_INCREMENT,
    `user_id`       varchar(64) NULL,
    `app_id`        varchar(64) NULL,
    `create_time`   bigint(20) NULL,
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

