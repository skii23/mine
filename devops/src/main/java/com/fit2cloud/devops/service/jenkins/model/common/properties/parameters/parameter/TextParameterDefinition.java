package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.model.TextParameterDefinition")
public class TextParameterDefinition extends BaseParameterDefinitionModel {
    private String defaultValue;
    private boolean trim;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public TextParameterDefinition(){
        super.type = "TEXT_PARAMETER";
    }

    @Override
    public String toString() {
        return "TextParameterDefinition{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", trim=" + trim +
                '}';
    }
}
