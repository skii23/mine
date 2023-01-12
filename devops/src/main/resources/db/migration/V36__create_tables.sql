CREATE TABLE IF NOT EXISTS `devops_application_pipeline`
(
    `id` varchar(64) NOT NULL COMMENT 'ID',
    `create_time` bigint(13) DEFAULT '0' COMMENT '创建时间',
    `job_history_id` varchar(64) DEFAULT NULL COMMENT '构建历史id',
    `job_id` varchar(64) DEFAULT NULL COMMENT '构建任务id',
    `deployment_id` varchar(64) DEFAULT NULL COMMENT '部署id',
    `sonarqube_id` varchar(64) DEFAULT NULL COMMENT 'sonarqube',
    `unittest_id` varchar(64) DEFAULT NULL COMMENT 'unittest',
    `trigger_name` varchar(64) DEFAULT NULL COMMENT '归属用户,流水线触发者，keyanggui。如果是自动触发，则写auto',
    `app_name` varchar(64) DEFAULT NULL COMMENT '应用名',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


