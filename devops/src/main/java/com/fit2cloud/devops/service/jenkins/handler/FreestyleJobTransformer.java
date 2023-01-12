package com.fit2cloud.devops.service.jenkins.handler;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.*;
import org.jdom2.Document;

import java.util.HashMap;

public class FreestyleJobTransformer extends AbstractJobConvertor<JSONObject, Document> {

    public FreestyleJobTransformer(JSONObject source, Document target) {
        this();
        this.source = source;
        this.target = target;
        this.addConvertor(new BasePropertiesTransformer())
                .addConvertor(new BuilderTransformer())
                .addConvertor(new PropertiesTransformer())
                .addConvertor(new PublisherTransformer())
                .addConvertor(new TriggerTransformer())
                .addConvertor(new BuildWrapperTransformer());
    }

    public FreestyleJobTransformer() {
        this.contextHolder = new HashMap<>();
        this.convertors = new HashMap<>();
    }

}
