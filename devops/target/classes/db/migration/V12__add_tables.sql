CREATE TABLE IF NOT EXISTS devops_proxy
(
    id VARCHAR(50) PRIMARY KEY NOT NULL,
    port INT DEFAULT 22,
    username VARCHAR(64),
    password VARCHAR(64),
    scope VARCHAR(32),
    organization_id VARCHAR(50),
    ip VARCHAR(128)
);

ALTER TABLE devops_cloud_server_devops ADD proxy_id VARCHAR(50) DEFAULT NULL  NULL;