CREATE TABLE IF NOT EXISTS `devops_jenkins_params`
(
    `id`          varchar(50) NOT NULL,
    `param_key`   varchar(50) NOT NULL,
    `alias`       varchar(255) DEFAULT NULL,
    `param_value` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;