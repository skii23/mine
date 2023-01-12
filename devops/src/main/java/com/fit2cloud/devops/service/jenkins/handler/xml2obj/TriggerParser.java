package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.BaseTriggerModel;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.GitLabPushTrigger;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.ReverseBuildTrigger;
import com.fit2cloud.devops.service.jenkins.model.common.trigger.TimerTrigger;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.*;

public class TriggerParser extends AbstractConvertor<Document,BaseJobModel> {

    public TriggerParser() {
        super("triggerParser","triggers");
    }

    public TriggerParser(String name) {
        super(name);
    }

    public TriggerParser(String name, String sourceName) {
        super(name, sourceName);
    }

    public TriggerParser(String name, String sourceName,String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
        this.convertMap.forEach((xmlNodeName,fieldName) -> {
            Element triggersElem = rootElement.getChild(xmlNodeName);
            Optional.ofNullable(triggersElem).ifPresent(triggers -> {
                Field field = FieldUtils.getField(target.getClass(), fieldName, true);
                try {
                    Object triggersListObj = field.get(target);
                    if (triggersListObj == null) {
                        FieldUtils.writeField(field, target, new ArrayList<BaseTriggerModel>(), true);
                    }
                    final List<BaseTriggerModel> triggersList = ((List<BaseTriggerModel>) field.get(target));
                    triggers.getChildren().forEach(trigger -> {
                        Class<?> clazz = TriggerParser.TriggerTypeMapHolder.TYPE_MAP.get(trigger.getName());
                        String publisherXml = XmlUtils.outputXml(trigger);
                        if (clazz != null) {
                            Object o = XmlUtils.fromXml(publisherXml, clazz);
                            triggersList.add((BaseTriggerModel) o);
                        } else {
                            BaseTriggerModel triggerNode = new BaseTriggerModel();
                            triggerNode.setXmlNodeName(trigger.getName());
                            triggerNode.setXmlNodeData(publisherXml);
                            triggersList.add(triggerNode);
                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static final class TriggerTypeHolder {
        public static final String REVERSE_BUILD_TRIGGER = "jenkins.triggers.ReverseBuildTrigger";
        public static final String TIMER_TRIGGER = "hudson.triggers.TimerTrigger";
        public static final String GITLAB_PUSH_TRIGGER = "com.dabsquared.gitlabjenkins.GitLabPushTrigger";

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
