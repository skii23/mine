-- 增加job与应用关联关系
SET @s = (SELECT IF(
    (SELECT COUNT(column_name)
          FROM INFORMATION_SCHEMA.COLUMNS
          WHERE table_name = 'devops_jenkins_job'
          AND column_name = 'app_id'
    ) > 0,
    "SELECT 1",
    "ALTER TABLE devops_jenkins_job ADD COLUMN app_id varchar(64) NULL comment '应用唯一ID' AFTER id;"
));
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

