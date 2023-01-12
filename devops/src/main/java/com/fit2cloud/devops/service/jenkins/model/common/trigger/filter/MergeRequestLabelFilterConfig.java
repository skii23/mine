package com.fit2cloud.devops.service.jenkins.model.common.trigger.filter;

public class MergeRequestLabelFilterConfig {

    private String include;
    private String exclude;

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }
}
