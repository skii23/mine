package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.buildWrapper.AntWrapper;
import com.fit2cloud.devops.service.jenkins.model.common.buildWrapper.PreBuildCleanup;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.GitLabPushTrigger;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.ReverseBuildTrigger;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.TimerTrigger;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BuildWrapperTransformer extends AbstractConvertor<JSONObject, Document> {

    public BuildWrapperTransformer() {
        super("buildWrapperTransformer","buildWrappers");
    }

    public BuildWrapperTransformer(String name) {
        super(name);
    }

    public BuildWrapperTransformer(String name, String sourceName) {
        super(name, sourceName);
    }

    public BuildWrapperTransformer(String name, String sourceName, String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();
        this.convertMap.forEach((fieldName, xmlNodeName) -> {
            rootElement.removeChild(xmlNodeName);
            Element triggersElem = new Element(xmlNodeName);
            JSONArray triggersArray = source.getJSONArray(fieldName);
            Optional.ofNullable(triggersArray).ifPresent(triggers -> triggers.forEach(trigger -> {
                JSONObject triggerObj = new JSONObject((Map) trigger);
                String type = triggerObj.getString("type");
                Class<?> clazz = BuildWrapperTypeMapHolder.TYPE_MAP.get(type);
                if (clazz != null) {
                    Object o = triggerObj.toJavaObject(clazz);
                    Element triggerElem = XmlUtils.objToXmlElement(o);
                    triggersElem.addContent(triggerElem);
                } else {
                    String xmlNodeData = triggerObj.getString("xmlNodeData");
                    if (StringUtils.isNotBlank(xmlNodeData)) {
                        Element triggerElem = XmlUtils.stringToXmlElement(xmlNodeData);
                        triggersElem.addContent(triggerElem);
                    }
                }
            }));
            rootElement.addContent(triggersElem);
        });
    }

    public static final class BuildWrapperTypeHolder {
        public static final String ANT_WRAPPER = "ANT_WRAPPER";
        public static final String PRE_BUILD_CLEANUP = "PRE_BUILD_CLEANUP";
    }

    public static final class BuildWrapperTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(BuildWrapperTypeHolder.ANT_WRAPPER, AntWrapper.class);
            TYPE_MAP.put(BuildWrapperTypeHolder.PRE_BUILD_CLEANUP, PreBuildCleanup.class);
        }
    }
}
