package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.model.RunParameterDefinition")
public class RunParameterDefinition extends BaseParameterDefinitionModel {
    private String projectName;
    private String filter;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public RunParameterDefinition(){
        super.type = "RUN_PARAMETER";
    }

    @Override
    public String toString() {
        return "RunParameterDefinition{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", projectName='" + projectName + '\'' +
                ", filter='" + filter + '\'' +
                '}';
    }
}
