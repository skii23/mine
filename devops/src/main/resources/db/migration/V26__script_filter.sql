CREATE TABLE IF NOT EXISTS `devops_script_filter`
(
    `id`     varchar(50)  NOT NULL,
    `name`   varchar(100) DEFAULT NULL,
    `type`   varchar(20)  DEFAULT NULL,
    `active` tinyint(1)   DEFAULT NULL,
    `value`  text,
    `create_time` bigint(16) DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;