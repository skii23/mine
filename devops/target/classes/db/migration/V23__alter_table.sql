UPDATE devops_jenkins_job SET buildable =
CASE
    WHEN buildable = 'true' THEN
    buildable = '1'
    WHEN buildable = 'false' THEN
    buildable = '0'
END;

ALTER TABLE devops_jenkins_job MODIFY COLUMN buildable tinyint(1) default null;