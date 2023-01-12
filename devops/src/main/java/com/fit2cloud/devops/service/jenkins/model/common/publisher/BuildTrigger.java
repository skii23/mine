package com.fit2cloud.devops.service.jenkins.model.common.publisher;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.model.common.Threshold;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.tasks.BuildTrigger")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class BuildTrigger extends BasePublisherModel {

    {
        this.type = PublisherType.BUILD_TRIGGER_PUBLISHER.getJavaType();
    }

    private String childProjects;

    private Threshold threshold;

    public String getChildProjects() {
        return childProjects;
    }

    public void setChildProjects(String childProjects) {
        this.childProjects = childProjects;
    }

    public Threshold getThreshold() {
        return threshold;
    }

    public void setThreshold(Threshold threshold) {
        this.threshold = threshold;
    }
}
