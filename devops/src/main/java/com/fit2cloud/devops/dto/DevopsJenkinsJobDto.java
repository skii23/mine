package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;

public class DevopsJenkinsJobDto extends DevopsJenkinsJob {
    private String workspaceName;
    private String organizationName;
    private int childNum;

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public int getChildNum() {
        return childNum;
    }

    public void setChildNum(int childNum) {
        this.childNum = childNum;
    }
}
