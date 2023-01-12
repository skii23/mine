CREATE TABLE IF NOT EXISTS `devops_sonarqube`
(
    `id`                varchar(64) NOT NULL COMMENT 'ID',
    `create_time`       bigint(13) DEFAULT 0 COMMENT '创建时间',
    `job_history_id`             varchar(64) NOT NULL COMMENT '构建历史id',
    `project_key`        varchar(128) NOT NULL COMMENT 'sonarqube项目索引ID',
    `url`               varchar(256) DEFAULT NULL COMMENT '报告链接',
    `ncloc`              bigint(20) DEFAULT 0 COMMENT '总代码行数',
    `bugs`              bigint(10) DEFAULT 0 COMMENT '总bugs数量',
    `debt`              bigint(10) DEFAULT 0 COMMENT '总技术栈(分)',
    `vulnerabilities`   bigint(10) DEFAULT 0 COMMENT '总漏洞数量',
    `test_coverage`      float(5) DEFAULT 0 COMMENT '总测试覆盖率',
    `test_line`          bigint(20) DEFAULT 0 COMMENT '总测试可覆盖行数',
    `duplicated_rate`    float(5) DEFAULT 0 COMMENT '总重复率',
    `issues`            bigint(10) DEFAULT 0 COMMENT '总的问题数',
    `open_issue`         bigint(10) DEFAULT 0 COMMENT '总open问题数',
    `confired_issue`     bigint(10) DEFAULT 0 COMMENT '总已确认的问题数',
    `false_position_issue`bigint(10) DEFAULT 0 COMMENT '总误判的问题数',
    `new_test_coverage`      float(5) DEFAULT 0 COMMENT '新代码测试覆盖率',
    `new_test_line`          bigint(20) DEFAULT 0 COMMENT '新代码测试可覆盖行数',
    `new_vulnerabilities`   bigint(10) DEFAULT 0 COMMENT '新代码漏洞数量',
    `new_lines`         bigint(20) DEFAULT 0 COMMENT '新增码行数',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_unittest`
(
    `id`                varchar(64) NOT NULL COMMENT 'ID',
    `create_time`       bigint(13) DEFAULT 0 COMMENT '创建时间',
    `job_history_id`    varchar(64) NOT NULL COMMENT '任务id',
    `url`               varchar(256) DEFAULT NULL COMMENT '报告链接',
    `fail_count`              bigint(10) DEFAULT 0 COMMENT '失败的测试用例',
    `skip_count`              bigint(10) DEFAULT 0 COMMENT '跳过不执行的测试用例',
    `all_count`              bigint(10) DEFAULT 0 COMMENT '总的单测用例',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `devops_api_test`
(
    `id`                varchar(64) NOT NULL COMMENT 'ID',
    `start_time`        bigint(13) DEFAULT 0 COMMENT '开始时间',
    `end_time`          bigint(13) DEFAULT 0 COMMENT '结束时间',
    `deploy_id`         varchar(64)  NOT NULL COMMENT '部署任务记录id',
    `product_id`        varchar(64)  DEFAULT NULL COMMENT '产品id',
    `plan_id`           varchar(64)  DEFAULT NULL COMMENT '测试计划id',
    `run_id`            varchar(64) DEFAULT NULL COMMENT '运行id',
    `env`               varchar(128) DEFAULT NULL COMMENT '测试计划环境',
    `operator`          varchar(128)  DEFAULT NULL COMMENT '调度者',
    `result`            varchar(32) DEFAULT NULL COMMENT '执行结果',
    `report_url`        varchar(256) DEFAULT NULL COMMENT '报告地址',
    `total_count`       bigint(10) DEFAULT 0 COMMENT '总用例',
    `untested_count`    bigint(10) DEFAULT 0 COMMENT '未测试的用例',
    `pass_count`        bigint(10) DEFAULT 0 COMMENT '跳过不执行的测试用例',
    `fail_count`        bigint(10) DEFAULT 0 COMMENT '测试失败的用例',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


