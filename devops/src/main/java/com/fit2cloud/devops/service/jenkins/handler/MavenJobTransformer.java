package com.fit2cloud.devops.service.jenkins.handler;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.*;
import org.jdom2.Document;

import java.util.HashMap;

public class MavenJobTransformer extends AbstractJobConvertor<JSONObject, Document> {

    public MavenJobTransformer(JSONObject source, Document target) {
        this();
        this.source = source;
        this.target = target;
        this.addConvertor(new BasePropertiesTransformer())
                .addConvertor(new PropertiesTransformer())
                .fluentAddConvertor(new BuilderTransformer("builderTransformer"))
                .addMap("preBuilders","prebuilders")
                .addMap("postBuilders","postbuilders")
                .done(this)
                .addConvertor(new MavenPropertiesTransformer())
                .addConvertor(new PublisherTransformer())
                .addConvertor(new TriggerTransformer())
                .addConvertor(new BuildWrapperTransformer());

    }

    public MavenJobTransformer() {
        this.contextHolder = new HashMap<>();
        this.convertors = new HashMap<>();
    }

}
