package com.fit2cloud.devops.service.jenkins.handler;

import com.fit2cloud.devops.service.jenkins.handler.xml2obj.FlowDefinitionParser;
import com.fit2cloud.devops.service.jenkins.handler.xml2obj.PropertiesParser;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.flow.WorkFlow;
import org.jdom2.Document;

import java.util.HashMap;

public class WorkFlowJobParser extends AbstractJobConvertor<Document, BaseJobModel>{

    public WorkFlowJobParser(Document document, WorkFlow baseJobModel) {
        this();
        this.source = document;
        this.target = baseJobModel;
        this.addConvertor(new PropertiesParser())
                .addConvertor(new FlowDefinitionParser());
    }

    public WorkFlowJobParser() {
        this.convertors = new HashMap<>();
        this.contextHolder = new HashMap<>();
    }
}
