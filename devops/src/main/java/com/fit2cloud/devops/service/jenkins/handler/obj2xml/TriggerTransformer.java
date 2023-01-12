package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.GitLabPushTrigger;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.ReverseBuildTrigger;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.TimerTrigger;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TriggerTransformer extends AbstractConvertor<JSONObject, Document> {

    public TriggerTransformer() {
        super("triggerTransformer","triggers");
    }

    public TriggerTransformer(String name) {
        super(name);
    }

    public TriggerTransformer(String name, String sourceName) {
        super(name, sourceName);
    }

    public TriggerTransformer(String name, String sourceName,String targetName) {
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
                JSONObject triggerObj;
                if (trigger instanceof Map) {
                    triggerObj = new JSONObject(((Map) trigger));
                }else {
                    triggerObj = (JSONObject) trigger;
                }
                String type = triggerObj.getString("type");
                Class<?> clazz = TriggerTypeMapHolder.TYPE_MAP.get(type);
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

    public static final class TriggerTypeHolder {
        public static final String REVERSE_BUILD_TRIGGER = "REVERSE_BUILD_TRIGGER";
        public static final String TIMER_TRIGGER = "TIMER_TRIGGER";
        public static final String GITLAB_PUSH_TRIGGER = "GITLAB_PUSH_TRIGGER";
    }

    public static final class TriggerTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(TriggerTypeHolder.REVERSE_BUILD_TRIGGER, ReverseBuildTrigger.class);
            TYPE_MAP.put(TriggerTypeHolder.TIMER_TRIGGER, TimerTrigger.class);
            TYPE_MAP.put(TriggerTypeHolder.GITLAB_PUSH_TRIGGER, GitLabPushTrigger.class);
        }
    }
}
