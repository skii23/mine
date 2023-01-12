INSERT INTO devops_application_repository_setting (id, application_id, repository_id, env_id)
  SELECT
    id,
    application_id,
    repository_id,
    environment_value_id
  FROM devops_application_setting