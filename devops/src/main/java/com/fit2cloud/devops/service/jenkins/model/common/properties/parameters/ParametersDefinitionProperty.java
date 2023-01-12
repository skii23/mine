package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.PropertiesTransformer;
import com.fit2cloud.devops.service.jenkins.model.common.properties.BasePropertyModel;
import com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter.*;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("hudson.model.ParametersDefinitionProperty")
@XStreamInclude({StringParameterDefinition.class, PasswordParameterDefinition.class, BooleanParameterDefinition.class, FileParameterDefinition.class, TextParameterDefinition.class, RunParameterDefinition.class, ChoiceParameterDefinition.class})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class ParametersDefinitionProperty extends BasePropertyModel{

    @XStreamAlias(value="parameterDefinitions",impl = BaseParameterDefinitionModel.class)
    private List<BaseParameterDefinitionModel> parameterDefinitions;

    public ParametersDefinitionProperty() {
        this.parameterDefinitions = new ArrayList<>();
        this.type = PropertiesTransformer.PropertyTypeHolder.PARAMETERS_DEFINITION;
    }

    public List<BaseParameterDefinitionModel> getParameterDefinitions() {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(List<BaseParameterDefinitionModel> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    public void addParameterDefinition(BaseParameterDefinitionModel baseParameterDefinitionModel){
        this.parameterDefinitions.add(baseParameterDefinitionModel);
    }
}
