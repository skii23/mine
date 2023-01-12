package com.fit2cloud.devops.service.jenkins.handler;

import com.fit2cloud.devops.service.jenkins.handler.xml2obj.*;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import org.jdom2.Document;

import java.util.HashMap;

public class FreestyleJobParser extends AbstractJobConvertor<Document,BaseJobModel> {

    public FreestyleJobParser(Document document, BaseJobModel baseJobModel) {
        this();
        this.source = document;
        this.target = baseJobModel;
        this.addConvertor(new BasePropertiesParser())
                .addConvertor(new PropertiesParser())
                .addConvertor(new BuilderParser())
                .addConvertor(new TriggerParser())
                .addConvertor(new PublisherParser())
                .addConvertor(new BuildWrapperParser());
    }

    public FreestyleJobParser() {
        this.convertors = new HashMap<>();
        this.contextHolder = new HashMap<>();
    }

}
