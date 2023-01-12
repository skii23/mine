ALTER TABLE devops_jenkins_job CHANGE status sync_status VARCHAR(50);
ALTER TABLE devops_jenkins_job ADD COLUMN build_status VARCHAR(50);
ALTER TABLE devops_jenkins_job_history CHANGE status build_status VARCHAR(50);
ALTER TABLE devops_jenkins_job_history ADD COLUMN sync_status VARCHAR(50);