ALTER TABLE devops_jenkins_job
    ADD COLUMN `parameterized_build` tinyint(4) DEFAULT '0' COMMENT '是否可参数化构建';
ALTER TABLE devops_jenkins_job_history
    ADD COLUMN `actions` text NULL COMMENT '构建信息';
ALTER TABLE devops_proxy
    ADD COLUMN `tag` varchar(50) DEFAULT NULL COMMENT '用途标签';