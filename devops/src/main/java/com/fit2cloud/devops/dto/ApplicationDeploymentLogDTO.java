package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.ApplicationDeployment;

public class ApplicationDeploymentLogDTO extends ApplicationDeployment {
    private String serverName;
    private String manageIpAddress;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getManageIpAddress() {
        return manageIpAddress;
    }

    public void setManageIpAddress(String manageIpAddress) {
        this.manageIpAddress = manageIpAddress;
    }
}
