package com.fit2cloud.devops.dto.request;

import com.fit2cloud.devops.base.domain.DevopsJenkinsJob;
import com.fit2cloud.devops.base.domain.DevopsJenkinsView;

import java.util.List;

public class JobViewRequest {
    private List<DevopsJenkinsJob> jobs;
    private List<DevopsJenkinsView> views;

    public List<DevopsJenkinsJob> getJobs() {
        return jobs;
    }

    public void setJobs(List<DevopsJenkinsJob> jobs) {
        this.jobs = jobs;
    }

    public List<DevopsJenkinsView> getViews() {
        return views;
    }

    public void setViews(List<DevopsJenkinsView> views) {
        this.views = views;
    }
}
