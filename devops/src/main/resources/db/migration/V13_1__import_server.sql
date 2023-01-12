CREATE TABLE IF NOT EXISTS `devops_cloud_server` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `instance_uuid` varchar(50) DEFAULT NULL COMMENT '实例唯一标识',
  `workspace_id` varchar(50) NOT NULL DEFAULT '' COMMENT '工作空间 ID',
  `account_id` varchar(50) NOT NULL COMMENT '云账号 ID',
  `instance_id` varchar(256) DEFAULT NULL COMMENT '实例 ID',
  `instance_name` varchar(256) DEFAULT NULL COMMENT '实例名称',
  `image_id` varchar(128) DEFAULT NULL COMMENT '镜像 ID',
  `instance_status` varchar(45) DEFAULT NULL COMMENT '实例状态',
  `instance_type` varchar(64) DEFAULT NULL COMMENT '实例类型',
  `instance_type_description` varchar(64) DEFAULT NULL COMMENT '实例类型描述',
  `region` varchar(256) DEFAULT NULL COMMENT '数据中心/区域',
  `zone` varchar(256) DEFAULT NULL COMMENT '可用区/集群',
  `host` varchar(256) DEFAULT NULL COMMENT '宿主机',
  `remote_ip` varchar(64) DEFAULT NULL COMMENT '公网 IP',
  `ip_array` varchar(500) DEFAULT NULL COMMENT 'IP 地址',
  `os` varchar(50) DEFAULT NULL COMMENT '操作系统Key',
  `os_version` varchar(50) DEFAULT NULL COMMENT '操作系统版本',
  `cpu` int(8) DEFAULT '0' COMMENT 'CPU 核数',
  `memory` int(8) DEFAULT '0' COMMENT '内存容量',
  `disk` int(16) DEFAULT '0' COMMENT '磁盘容量',
  `create_time` bigint(13) DEFAULT NULL COMMENT '创建时间',
  `last_sync_timestamp` bigint(13) NOT NULL COMMENT '上次同步时间',
  `proxy_id` varchar(50) DEFAULT NULL COMMENT '代理 ID',
  `hostname` varchar(256) DEFAULT NULL COMMENT '主机名',
  `management_ip` varchar(64) DEFAULT NULL COMMENT '管理 IP',
  `management_port` int(11) DEFAULT NULL COMMENT '管理端口',
  `os_info` varchar(128) DEFAULT NULL COMMENT '云平台操作系统信息',
  `update_time` bigint(13) DEFAULT NULL COMMENT '主机更新时间',
  `source` varchar(512) DEFAULT 'LOCAL' COMMENT '主机来源',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_SERVER` (`instance_uuid`,`account_id`),
  KEY `IDX_WORKSPACE` (`workspace_id`),
  KEY `IDX_ACCOUNT` (`account_id`),
  KEY `IDX_STATUS` (`instance_status`),
  KEY `IDX_REGION` (`region`),
  KEY `IDX_ZONE` (`zone`),
  KEY `IDX_HOST` (`host`),
  KEY `IDX_UUID` (`instance_uuid`),
  KEY `IDX_LAST_SYNC` (`last_sync_timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;





ALTER TABLE devops_cloud_server_devops MODIFY COLUMN `cluster_role_id` VARCHAR(1000);
