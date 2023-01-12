CREATE TABLE IF NOT EXISTS devops_variable
(
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255),
    value VARCHAR(255),
    created_time BIGINT,
    resource_type VARCHAR(64),
    resource_id VARCHAR(50)
);