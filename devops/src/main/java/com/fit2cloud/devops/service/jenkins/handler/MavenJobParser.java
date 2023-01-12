package com.fit2cloud.devops.service.jenkins.handler;

import com.fit2cloud.devops.service.jenkins.handler.xml2obj.*;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import org.jdom2.Document;

import java.util.HashMap;

public class MavenJobParser extends AbstractJobConvertor<Document,BaseJobModel> {

    public MavenJobParser(Document document, BaseJobModel baseJobModel) {
        this();
        this.source = document;
        this.target = baseJobModel;
        this.addConvertor(new BasePropertiesParser())
                .addConvertor(new MavenPropertiesParser())
                .addConvertor(new PropertiesParser())
                .fluentAddConvertor(new BuilderParser("builderParser"))
                .addMap("prebuilders","preBuilders")
                .addMap("postbuilders","postBuilders")
                .done(this)
                .addConvertor(new TriggerParser())
                .addConvertor(new PublisherParser())
                .addConvertor(new BuildWrapperParser());
    }

    public MavenJobParser() {
        this.convertors = new HashMap<>();
        this.contextHolder = new HashMap<>();
    }
}
