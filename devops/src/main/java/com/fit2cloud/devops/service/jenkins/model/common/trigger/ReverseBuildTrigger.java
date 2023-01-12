package com.fit2cloud.devops.service.jenkins.model.common.trigger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.TriggerTransformer;
import com.fit2cloud.devops.service.jenkins.model.common.Threshold;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("jenkins.triggers.ReverseBuildTrigger")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class ReverseBuildTrigger extends BaseTriggerModel {

    {
        this.type = TriggerTransformer.TriggerTypeHolder.REVERSE_BUILD_TRIGGER;
    }

    private String spec = "";
    private String upstreamProjects;
    private Threshold threshold;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getUpstreamProjects() {
        return upstreamProjects;
    }

    public void setUpstreamProjects(String upstreamProjects) {
        this.upstreamProjects = upstreamProjects;
    }

    public Threshold getThreshold() {
        return threshold;
    }

    public void setThreshold(Threshold threshold) {
        this.threshold = threshold;
    }

}
