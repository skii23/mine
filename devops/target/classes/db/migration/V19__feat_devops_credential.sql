CREATE TABLE `devops_jenkins_credential`(
    `id`           varchar(50) NOT NULL,
    `full_name`    varchar(255) DEFAULT NULL,
    `fingerprint`  varchar(255) DEFAULT NULL,
    `display_name` varchar(255) DEFAULT NULL,
    `description`  varchar(255) DEFAULT NULL,
    `type_name`    varchar(60)  DEFAULT NULL,
    `organization` mediumtext,
    `workspace`    mediumtext,
    `username`     varchar(255) DEFAULT NULL,
    `password`     varchar(255) DEFAULT NULL,
    `private_key`   text,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;