package com.fit2cloud.devops.vo;

/**
 * @author WiSoniC
 */
public class GroupAnalysisVO {

    private String organizationName;
    private String workspaceName;
    private Integer jobCount;
    private Integer buildCount;
    private Integer successBuildCount;
    private Integer appCount;
    private Integer deployCount;
    private Integer successDeployCount;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Integer getJobCount() {
        return jobCount;
    }

    public void setJobCount(Integer jobCount) {
        this.jobCount = jobCount;
    }

    public Integer getBuildCount() {
        return buildCount;
    }

    public void setBuildCount(Integer buildCount) {
        this.buildCount = buildCount;
    }

    public Integer getSuccessBuildCount() {
        return successBuildCount;
    }

    public void setSuccessBuildCount(Integer successBuildCount) {
        this.successBuildCount = successBuildCount;
    }

    public Integer getAppCount() {
        return appCount;
    }

    public void setAppCount(Integer appCount) {
        this.appCount = appCount;
    }

    public Integer getDeployCount() {
        return deployCount;
    }

    public void setDeployCount(Integer deployCount) {
        this.deployCount = deployCount;
    }

    public Integer getSuccessDeployCount() {
        return successDeployCount;
    }

    public void setSuccessDeployCount(Integer successDeployCount) {
        this.successDeployCount = successDeployCount;
    }
}
