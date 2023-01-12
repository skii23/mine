package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.ApplicationSetting;

public class ApplicationSettingDTO extends ApplicationSetting {

    private String envValue;
    private String repositoryName;
    private String repositoryType;
    private String repositoryAddr;
    private String accessId;
    private String accessPassword;



    public String getEnvValue() {
        return envValue;
    }

    public void setEnvValue(String envValue) {
        this.envValue = envValue;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public String getRepositoryAddr() {
        return repositoryAddr;
    }

    public void setRepositoryAddr(String repositoryAddr) {
        this.repositoryAddr = repositoryAddr;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getAccessPassword() {
        return accessPassword;
    }

    public void setAccessPassword(String accessPassword) {
        this.accessPassword = accessPassword;
    }
}
