package com.fit2cloud.devops.service.jenkins.handler;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.*;
import org.jdom2.Document;

import java.util.HashMap;

public class WorkFlowJobTransformer extends AbstractJobConvertor<JSONObject, Document> {

    public WorkFlowJobTransformer(JSONObject source, Document target) {
        this();
        this.source = source;
        this.target = target;
        this.addConvertor(new PropertiesTransformer())
                .addConvertor(new FlowDefinitionTransformer());
    }

    public WorkFlowJobTransformer() {
        this.contextHolder = new HashMap<>();
        this.convertors = new HashMap<>();
    }

}
