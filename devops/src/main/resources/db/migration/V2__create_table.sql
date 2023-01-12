CREATE TABLE IF NOT EXISTS `devops_application` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `name` varchar(64) NOT NULL DEFAULT '',
  `created_time` bigint(13) NOT NULL,
  `description` varchar(64) DEFAULT NULL,
  `organization_id` varchar(50) DEFAULT NULL,
  `scope` varchar(45) DEFAULT 'global' COMMENT '可见范围',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS `devops_application_deployment` (
  `id` varchar(50) NOT NULL,
  `application_version_id` varchar(50) DEFAULT NULL,
  `cluster_id` varchar(50) DEFAULT NULL,
  `cluster_role_id` varchar(50) DEFAULT NULL,
  `cloud_server_id` varchar(50) DEFAULT NULL,
  `policy` varchar(32) DEFAULT NULL,
  `start_time` bigint(16) DEFAULT NULL,
  `end_time` bigint(16) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `progress` double(6,2) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `workspace_id` varchar(50) DEFAULT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_application_deployment_event_log` (
  `id` varchar(50) NOT NULL,
  `deployment_log_id` varchar(50) DEFAULT NULL,
  `event_name` varchar(32) DEFAULT NULL,
  `start_time` bigint(16) DEFAULT NULL,
  `end_time` bigint(16) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `s_order` int(11) DEFAULT NULL,
  `stdout` text,
  `application_version_id` varchar(50) DEFAULT NULL,
  `cloud_server_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_application_deployment_log` (
  `id` varchar(50) NOT NULL,
  `deployment_id` varchar(50) DEFAULT NULL,
  `cloud_server_id` varchar(50) DEFAULT NULL,
  `start_time` bigint(16) DEFAULT NULL,
  `end_time` bigint(16) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `progress` double(6,2) DEFAULT NULL,
  `stdout` text,
  `application_version_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_application_repository` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `name` varchar(45) NOT NULL DEFAULT '',
  `type` varchar(45) NOT NULL DEFAULT '',
  `created_time` bigint(13) NOT NULL,
  `repository` varchar(255) NOT NULL DEFAULT '',
  `access_id` varchar(128) DEFAULT NULL,
  `access_password` varchar(500) DEFAULT NULL,
  `status` varchar(45) NOT NULL DEFAULT '',
  `scope` varchar(45) DEFAULT 'global',
  `organization_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_application_setting` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `repository_id` varchar(50) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `application_id` varchar(50) DEFAULT NULL,
  `name_rule` varchar(255) DEFAULT NULL,
  `created_time` bigint(13) DEFAULT NULL,
  `environment_value_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_application_version` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `application_id` varchar(50) NOT NULL DEFAULT '',
  `name` varchar(64) NOT NULL DEFAULT '',
  `location` text NOT NULL,
  `created_time` bigint(13) DEFAULT NULL,
  `description` varchar(64) DEFAULT NULL,
  `application_repository_id` varchar(50) DEFAULT NULL,
  `last_deployment_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_cloud_server_devops` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `cluster_id` varchar(50) NOT NULL DEFAULT '',
  `cluster_role_id` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_cluster` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `name` varchar(64) NOT NULL DEFAULT '',
  `workspace_id` varchar(50) NOT NULL DEFAULT '',
  `created_time` bigint(13) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_cluster_role` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `name` varchar(64) NOT NULL DEFAULT '',
  `cluster_id` varchar(50) NOT NULL DEFAULT '',
  `created_time` bigint(13) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_script` (
  `id` varchar(50) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `workspace_id` varchar(50) DEFAULT NULL,
  `content` mediumtext,
  `created_time` bigint(16) DEFAULT NULL,
  `os` varchar(64) DEFAULT NULL,
  `scope` varchar(50) DEFAULT NULL,
  `organization_id` varchar(50) DEFAULT NULL,
  `os_version` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `devops_script_implement_log` (
  `id` varchar(50) NOT NULL,
  `script_id` varchar(50) DEFAULT NULL,
  `created_time` bigint(16) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `completed_time` bigint(16) DEFAULT NULL,
  `cloud_server_id` varchar(50) DEFAULT NULL,
  `script_exec_content` text,
  `stdout_content` text,
  `workspace_id` varchar(50) DEFAULT NULL,
  `ansible_task_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `module` (
  `id` varchar(50) NOT NULL COMMENT '模块ID',
  `name` varchar(50) NOT NULL COMMENT '模块名称',
  `type` varchar(20) DEFAULT NULL COMMENT '模块类型',
  `license` varchar(50) DEFAULT NULL,
  `auth` tinyint(1) DEFAULT '1',
  `summary` varchar(128) DEFAULT NULL COMMENT '模块概要',
  `module_url` varchar(255) DEFAULT NULL COMMENT '模块跳转URL',
  `port` bigint(10) DEFAULT NULL COMMENT '模块端口',
  `status` varchar(20) DEFAULT NULL COMMENT '模块状态',
  `active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `icon` varchar(255) DEFAULT 'link' COMMENT '模块icon',
  `sort` int(10) DEFAULT '0' COMMENT '排序',
  `open` varchar(20) DEFAULT 'current' COMMENT '模块打开方式',
  `version` VARCHAR(100) DEFAULT NULL  COMMENT '版本',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `ext1` varchar(255) DEFAULT NULL,
  `ext2` varchar(255) DEFAULT NULL COMMENT '是否授权和license ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `system_parameter` (
  `param_key` varchar(64) CHARACTER SET utf8mb4 NOT NULL COMMENT '参数索引键',
  `param_name` varchar(255) DEFAULT NULL COMMENT '参数名称',
  `param_value` varchar(3000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '参数值',
  `type` longtext CHARACTER SET utf8mb4 NOT NULL COMMENT '用户类型',
  `sort` int(5) DEFAULT NULL COMMENT '排序',
  `changeable` tinyint(1) DEFAULT NULL,
  `Comment` LONGTEXT DEFAULT NULL,
  PRIMARY KEY (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
