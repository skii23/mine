package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.ApplicationRepositorySetting;

public class ApplicationRepositorySettingDTO extends ApplicationRepositorySetting {

    private String envName;
    private String repositoryName;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
}
