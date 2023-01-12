package com.fit2cloud.devops.service.jenkins.model.common.builder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BuilderTransformer;
import com.fit2cloud.devops.service.jenkins.handler.xml2obj.BuilderParser;
import com.fit2cloud.devops.service.jenkins.model.AbstractBaseModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hudson.tasks.Shell")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class Shell extends BaseBuilderModel {

    {
        this.type = BuilderTransformer.BuilderTypeHolder.SHELL;
    }

    private String command;
    private Integer unstableReturn;

    public Integer getUnstableReturn() {
        return unstableReturn;
    }

    public void setUnstableReturn(Integer unstableReturn) {
        this.unstableReturn = unstableReturn;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}
