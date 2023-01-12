ALTER TABLE `devops_application`
    ADD COLUMN `test_prod_id` varchar(255) NULL,
ADD COLUMN `test_plan_id`  varchar(255) NULL,
ADD COLUMN `test_evn`  varchar(255) NULL;

ALTER TABLE `devops_application_deployment`
    ADD COLUMN `test_report_url`  varchar(255) NULL;

ALTER TABLE `devops_jenkins_job`
    MODIFY COLUMN `ext_param`  mediumtext NULL;

