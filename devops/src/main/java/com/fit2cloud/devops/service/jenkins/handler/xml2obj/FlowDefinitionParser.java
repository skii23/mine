package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.actions.Actions;
import com.fit2cloud.devops.service.jenkins.model.common.actions.DeclarativeJobAction;
import com.fit2cloud.devops.service.jenkins.model.common.actions.DeclarativeJobPropertyTrackerAction;
import com.fit2cloud.devops.service.jenkins.model.common.definition.CpsFlowDefinition;
import com.fit2cloud.devops.service.jenkins.model.common.definition.CpsScmFlowDefinition;
import com.fit2cloud.devops.service.jenkins.model.common.definition.Definition;
import com.fit2cloud.devops.service.jenkins.model.flow.WorkFlow;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FlowDefinitionParser extends AbstractConvertor<Document, BaseJobModel> {

    public FlowDefinitionParser() {
        super("definition");
    }

    @Override
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
        //设置基础属性
        JenkinsConstants.FieldMapHolder.BASE_JOB_MODEL_FIELDS_MAP.forEach((name, clazz) -> {
            Element child = rootElement.getChild(name);
            Optional.ofNullable(child).ifPresent(childElem -> {
                try {
                    Field field = FieldUtils.getField(BaseJobModel.class, name, true);
                    FieldUtils.writeField(field, target, CommonUtils.castValue(childElem.getTextTrim(), clazz), true);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
        //默认actions
        Actions actions = new Actions();
        actions.setDeclarativeJobAction(new DeclarativeJobAction());
        actions.setDeclarativeJobPropertyTrackerAction(new DeclarativeJobPropertyTrackerAction());
        ((WorkFlow) target).setActions(actions);

        //设置definition
        Element definitionElem = rootElement.getChild("definition");
        Optional.ofNullable(definitionElem).ifPresent(definition -> {
            String classStr = definitionElem.getAttributeValue("class");
//            definitionElem.removeAttribute("class");
            Class<?> clazz = TypeMapHolder.TYPE_MAP.get(classStr);
            String scmXml = XmlUtils.outputXml(definition);
            if (clazz != null) {
                Object o = XmlUtils.fromXmlWithoutAttr(scmXml, clazz);
                ((WorkFlow) target).setDefinition((Definition) o);
            }
        });
    }

    public static final class TypeHolder {
        public static final String CPS_SCM = "org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition";
        public static final String CPS_NULL = "org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition";
    }

    public static final class TypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(TypeHolder.CPS_SCM, CpsScmFlowDefinition.class);
            TYPE_MAP.put(TypeHolder.CPS_NULL, CpsFlowDefinition.class);
        }
    }
}
