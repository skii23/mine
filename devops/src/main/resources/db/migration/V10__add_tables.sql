CREATE TABLE IF NOT EXISTS devops_application_repository_setting
(
    id VARCHAR(50) PRIMARY KEY,
    application_id VARCHAR(50),
    repository_id VARCHAR(50),
    env_id VARCHAR(50)
);