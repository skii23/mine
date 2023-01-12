package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.model.BooleanParameterDefinition")
public class BooleanParameterDefinition extends BaseParameterDefinitionModel {
    private String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public BooleanParameterDefinition(){
        super.type = "BOOLEAN_PARAMETER";
    }

    @Override
    public String toString() {
        return "BooleanParameterDefinition{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
