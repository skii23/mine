package com.fit2cloud.devops.common.model;

public class DeployTopData {
    private String applicationId;
    private String applicationName;
    private String resourceId;
    private String countDeploy;
    private String countVersion;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getCountDeploy() {
        return countDeploy;
    }

    public void setCountDeploy(String countDeploy) {
        this.countDeploy = countDeploy;
    }

    public String getCountVersion() {
        return countVersion;
    }

    public void setCountVersion(String countVersion) {
        this.countVersion = countVersion;
    }
}
