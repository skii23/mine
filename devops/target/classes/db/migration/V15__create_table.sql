CREATE TABLE IF NOT EXISTS `devops_jenkins` (
  `id` varchar(50) NOT NULL,
  `name` varchar(512) NOT NULL COMMENT 'jenkins名称',
  `ip` varchar(50) DEFAULT NULL COMMENT 'jenkinsIP',
  `url` varchar(512) DEFAULT NULL COMMENT 'jenkins访问url',
  `username` varchar(50) DEFAULT NULL COMMENT 'jenkins登陆用户名',
  `password` varchar(128) DEFAULT NULL COMMENT 'jenkins登陆密码',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(13) DEFAULT NULL COMMENT '修改时间',
  `last_sync_time` bigint(13) DEFAULT NULL COMMENT '最近同步时间',
  `status` varchar(50) DEFAULT NULL COMMENT '状态',
  `active` varchar(50) DEFAULT NULL COMMENT '是否自动同步',
  `cron` varchar(50) DEFAULT NULL COMMENT '定时同步表达式',
  `job_size` int(13) DEFAULT NULL,
  `sync_status` varchar(50) DEFAULT NULL COMMENT '同步状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='jenkins账号表';

CREATE TABLE IF NOT EXISTS `devops_jenkins_job` (
  `id` varchar(50) NOT NULL,
  `name` varchar(512) NOT NULL COMMENT '构建任务名称',
  `jenkins_name` varchar(512) DEFAULT NULL COMMENT '所属jenkins配置',
  `jenkins_id` varchar(50) DEFAULT NULL COMMENT '所属jenkinsID',
  `status` varchar(50) DEFAULT NULL COMMENT '状态',
  `buildable` varchar(50) DEFAULT NULL COMMENT '是否可构建（true：false）',
  `job_xml` mediumtext COMMENT 'job配置信息',
  `build_size` int(13) DEFAULT NULL,
  `url` varchar(512) DEFAULT NULL COMMENT '构建任务url',
  `description` varchar(512) DEFAULT '' COMMENT '构建任务描述',
  `create_time` bigint(13) DEFAULT NULL,
  `update_time` bigint(13) DEFAULT NULL,
  `sync_time` bigint(13) DEFAULT NULL,
  `source` varchar(50) DEFAULT NULL COMMENT '来源',
  `workspace` varchar(2048) DEFAULT NULL COMMENT '可见工作空间',
  `organization` varchar(2048) DEFAULT NULL COMMENT '可见组织',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='构建任务表';


CREATE TABLE IF NOT EXISTS `devops_jenkins_job_history` (
  `id` varchar(50) NOT NULL,
  `job_name` varchar(1024) DEFAULT NULL COMMENT '所属job名称',
  `job_id` varchar(50) DEFAULT NULL COMMENT '所属jobID',
  `name` varchar(1024) DEFAULT NULL COMMENT '名称',
  `order_num` int(20) DEFAULT NULL COMMENT '序号',
  `url` varchar(1024) DEFAULT NULL COMMENT 'url',
  `build_time` bigint(13) DEFAULT NULL COMMENT '构建开始时间',
  `duration_time` bigint(13) DEFAULT NULL COMMENT '持续时间',
  `is_building` varchar(20) DEFAULT NULL COMMENT '是否正在构建',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `out_put_text` mediumtext COMMENT '构建日志',
  `status` varchar(50) DEFAULT NULL COMMENT '构建结果',
  `sync_time` bigint(13) DEFAULT NULL COMMENT '同步时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;