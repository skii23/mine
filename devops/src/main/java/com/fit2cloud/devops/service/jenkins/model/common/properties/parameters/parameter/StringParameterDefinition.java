package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.model.StringParameterDefinition")
public class StringParameterDefinition extends BaseParameterDefinitionModel{

    private String defaultValue;
    private Boolean trim;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean isTrim() {
        return trim;
    }

    public void setTrim(Boolean trim) {
        this.trim = trim;
    }

    public StringParameterDefinition(){
        super.type = "STRING_PARAMETER";
    }


    @Override
    public String toString() {
        return "StringParameterDefinition{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
