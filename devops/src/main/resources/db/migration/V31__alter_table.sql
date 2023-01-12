ALTER TABLE `devops_jenkins_params`
    MODIFY COLUMN `param_value`  varchar(5000);

ALTER TABLE `devops_jenkins_job`
    ADD COLUMN `ext_param`  varchar(512) NULL;
ALTER TABLE `devops_jenkins_job`
    ADD COLUMN `parent_id`  varchar(50) NULL;
ALTER TABLE `devops_jenkins_job`
    MODIFY COLUMN `type`  varchar(50)