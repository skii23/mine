package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.DevopsJenkinsJobHistory;

public class DevopsJenkinsJobHistoryDto extends DevopsJenkinsJobHistory {
    private String sonarqubeDashboardUrl;

    public String getSonarqubeDashboardUrl() {
        return sonarqubeDashboardUrl;
    }

    public void setSonarqubeDashboardUrl(String sonarqubeDashboardUrl) {
        this.sonarqubeDashboardUrl = sonarqubeDashboardUrl;
    }
}
