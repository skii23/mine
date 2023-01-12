package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.model.FileParameterDefinition")
public class FileParameterDefinition extends BaseParameterDefinitionModel {

    public FileParameterDefinition(){
        super.type = "FILE_PARAMETER";
    }


}
