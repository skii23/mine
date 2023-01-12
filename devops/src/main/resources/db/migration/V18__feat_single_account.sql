drop table if exists `devops_jenkins`;

update devops_jenkins_job set organization = null,workspace = null;

alter table devops_jenkins_job drop column jenkins_name;
alter table devops_jenkins_job drop column jenkins_id;
alter table devops_jenkins_job add `creator` varchar(50) default null;
alter table devops_jenkins_job modify `organization` varchar(50) default null;
alter table devops_jenkins_job modify `workspace` varchar(50) default null;

alter table devops_jenkins_job_history drop column out_put_text;
alter table devops_jenkins_job_history add `trigger_user` varchar(50) default null;

CREATE TABLE IF NOT EXISTS `system_parameter` (
  `param_key` varchar(64) CHARACTER SET utf8mb4 NOT NULL COMMENT '参数名称',
  `param_value` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '参数值',
  `type` longtext CHARACTER SET utf8mb4 NOT NULL COMMENT '用户类型',
  `sort` int(5) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

insert into `system_parameter` ( `param_key`, `param_value`, `type`, `sort`) values ('jenkins.address','http://www.jenkins.com','text',1);
insert into `system_parameter` ( `param_key`, `param_value`, `type`, `sort`) values ('jenkins.username','admin','text',2);
insert into `system_parameter` ( `param_key`, `param_value`, `type`, `sort`) values ('jenkins.password','admin123','password',3);
insert into `system_parameter` ( `param_key`, `param_value`, `type`, `sort`) values ('jenkins.enableCronSync','false','text',4);
insert into `system_parameter` ( `param_key`, `param_value`, `type`, `sort`) values ('jenkins.cronSyncSpec','* 1 * * * ?','text',5);
insert into `system_parameter` ( `param_key`, `param_value`, `type`, `sort`) values ('jenkins.syncStatus','UN_SYNC','text',6);