package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.model.PasswordParameterDefinition")
public class PasswordParameterDefinition extends BaseParameterDefinitionModel {
    private String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public PasswordParameterDefinition(){
        super.type = "PASSWORD_PARAMETER";
    }

    @Override
    public String toString() {
        return "PasswordParameterDefinition{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
