package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * @author wisonic
 */
@XStreamAlias("hudson.model.BaseParameterDefinition")
@XStreamInclude({StringParameterDefinition.class, PasswordParameterDefinition.class, BooleanParameterDefinition.class, FileParameterDefinition.class, TextParameterDefinition.class, RunParameterDefinition.class, ChoiceParameterDefinition.class})
public class BaseParameterDefinitionModel extends AbstractBaseModel {

    protected String name;
    protected String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
