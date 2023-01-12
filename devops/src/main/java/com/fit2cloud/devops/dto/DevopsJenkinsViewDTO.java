package com.fit2cloud.devops.dto;

import com.fit2cloud.devops.base.domain.DevopsJenkinsView;

import java.util.Set;

public class DevopsJenkinsViewDTO extends DevopsJenkinsView {
    private Set<String> jobIdSet;

    public Set<String> getJobIdSet() {
        return jobIdSet;
    }

    public void setJobIdSet(Set<String> jobIdSet) {
        this.jobIdSet = jobIdSet;
    }
}
