UPDATE devops_jenkins_job_history SET is_building =
CASE
WHEN is_building = 'true' THEN
    is_building = '1'
WHEN is_building = 'false' THEN
    is_building = '0'
END;

ALTER TABLE devops_jenkins_job_history MODIFY is_building tinyint(1) default null;