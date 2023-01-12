CREATE TABLE `devops_jenkins_view`
(
    `id`           varchar(50) NOT NULL,
    `name`         varchar(255) DEFAULT NULL,
    `organization` varchar(50)  DEFAULT NULL,
    `workspace`    varchar(50)  DEFAULT NULL,
    `creator`      varchar(50)  DEFAULT NULL,
    `create_time`  bigint(13)   DEFAULT NULL,
    `update_time`  bigint(13)   DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `devops_jenkins_view_job_mapping`
(
    `id`      varchar(50) NOT NULL,
    `view_id` varchar(50) DEFAULT NULL,
    `job_id`  varchar(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;