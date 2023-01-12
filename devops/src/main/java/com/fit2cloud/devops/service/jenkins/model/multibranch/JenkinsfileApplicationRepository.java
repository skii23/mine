package com.fit2cloud.devops.service.jenkins.model.multibranch;

/**
 * @author caiwzh
 * @date 2022/8/19
 */
public class JenkinsfileApplicationRepository {
    private String name;
    private String type;
    private String accessId;
    private String accessPassword;
    private String repository;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }
}
