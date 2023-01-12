package com.fit2cloud.devops.service.model;
import lombok.Data;

@Data
public class JenkinsJobSonarqubeParams {
    private String projectKey;
    private String serverName;
    private String properties;
}
