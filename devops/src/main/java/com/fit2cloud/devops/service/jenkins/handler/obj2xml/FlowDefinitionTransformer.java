package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.XmlNode;
import com.fit2cloud.devops.service.jenkins.model.common.actions.Actions;
import com.fit2cloud.devops.service.jenkins.model.common.actions.DeclarativeJobAction;
import com.fit2cloud.devops.service.jenkins.model.common.actions.DeclarativeJobPropertyTrackerAction;
import com.fit2cloud.devops.service.jenkins.model.common.definition.CpsFlowDefinition;
import com.fit2cloud.devops.service.jenkins.model.common.definition.CpsScmFlowDefinition;
import com.fit2cloud.devops.service.jenkins.model.common.scm.ScmNull;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FlowDefinitionTransformer extends AbstractConvertor<JSONObject, Document> {

    private List<String> flowbase = Lists.newArrayList("description", "keepDependencies", "disabled");

    public FlowDefinitionTransformer() {
        super("definition");
    }

    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();

        //处理flow暂时支持的基础属性节点description、keepDependencies、disabled
        flowbase.forEach(str ->
                Optional.ofNullable(source.getObject(str, JenkinsConstants.FieldMapHolder.BASE_JOB_MODEL_FIELDS_MAP.get(str)))
                        .ifPresent(value -> {
                            rootElement.removeChild(str);
                            Element child = new Element(str);
                            child.setText(value.toString());
                            rootElement.addContent(child);
                        }));
        //默认actions
        Actions actions = new Actions();
        actions.setDeclarativeJobAction(new DeclarativeJobAction());
        actions.setDeclarativeJobPropertyTrackerAction(new DeclarativeJobPropertyTrackerAction());
        Element actionsElem = XmlUtils.objToXmlElement(actions);
        rootElement.addContent(actionsElem);

        //处理definition
        JSONObject definition = source.getJSONObject("definition");
        rootElement.removeChild("definition");
        if (definition != null) {
            String type = definition.getString("type");
            Class<?> clazz = ScmTypeMapHolder.TYPE_MAP.get(type);
            if (clazz != null) {
                Object o = definition.toJavaObject(clazz);
                Element definitionElem = XmlUtils.objToXmlElement(o);
                rootElement.addContent(definitionElem);
            } else {
                String xmlNodeData = definition.getString("xmlNodeData");
                if (StringUtils.isNotBlank(xmlNodeData)) {
                    Element scmElem = XmlUtils.stringToXmlElement(xmlNodeData);
                    rootElement.addContent(scmElem);
                }
            }
        } else {
            rootElement.addContent(XmlUtils.objToXmlElement(new ScmNull()));
        }
//        然后处理不支持的节点
        JSONArray unknownNodes = source.getJSONArray("unknownNodes");
        Optional.ofNullable(unknownNodes).ifPresent(nodes -> nodes.forEach(node -> {
            JSONObject nodeObj = (JSONObject) node;
            XmlNode xmlNode = nodeObj.toJavaObject(XmlNode.class);
            if (rootElement.getChild(xmlNode.getXmlNodeName()) == null) {
                Element element = XmlUtils.stringToXmlElement(xmlNode.getXmlNodeData());
                rootElement.addContent(element);
            }
        }));
    }

    public static final class DefinitionTypeHolder {
        public static final String CPS_NULL = "CPS_NULL";
        public static final String CPS_SCM = "CPS_SCM";
    }

    public static final class ScmTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(DefinitionTypeHolder.CPS_NULL, CpsFlowDefinition.class);
            TYPE_MAP.put(DefinitionTypeHolder.CPS_SCM, CpsScmFlowDefinition.class);
        }
    }

}
