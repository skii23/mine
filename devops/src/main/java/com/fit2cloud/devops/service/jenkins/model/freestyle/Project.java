package com.fit2cloud.devops.service.jenkins.model.freestyle;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.service.jenkins.model.common.builder.BaseBuilderModel;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("project")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Project extends BaseJobModel {

    {
        this.type = JenkinsConstants.JobType.FREE_STYLE.name();
    }

    public Project() {
        super();
        this.builders = new ArrayList<>();
    }

    public List<BaseBuilderModel> getBuilders() {
        return builders;
    }

    public void setBuilders(List<BaseBuilderModel> builders) {
        this.builders = builders;
    }

    @XStreamAlias("builders")
    private List<BaseBuilderModel> builders;

}
