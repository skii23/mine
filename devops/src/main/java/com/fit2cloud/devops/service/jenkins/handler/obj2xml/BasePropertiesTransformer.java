package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.XmlNode;
import com.fit2cloud.devops.service.jenkins.model.common.scm.ScmNull;
import com.fit2cloud.devops.service.jenkins.model.common.scm.git.ScmGit;
import com.fit2cloud.devops.service.jenkins.model.common.scm.svn.ScmSvn;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BasePropertiesTransformer extends AbstractConvertor<JSONObject, Document> {

    public BasePropertiesTransformer() {
        super("basePropertiesTransformer");
    }

    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();
//        处理支持的基础属性节点
        JenkinsConstants.FieldMapHolder.BASE_JOB_MODEL_FIELDS_MAP.forEach((str, clazz) -> Optional
                .ofNullable(source.getObject(str, clazz))
                .ifPresent(value -> {
                    rootElement.removeChild(str);
                    Element child = new Element(str);
                    child.setText(value.toString());
                    rootElement.addContent(child);
                }));
//        处理SCM
        JSONObject scm = source.getJSONObject("scm");
        rootElement.removeChild("scm");
        if (scm != null) {
            String type = scm.getString("type");
            Class<?> clazz = ScmTypeMapHolder.TYPE_MAP.get(type);
            if (clazz != null) {
                Object o = scm.toJavaObject(clazz);
                Element scmElem = XmlUtils.objToXmlElement(o);
                rootElement.addContent(scmElem);
            } else {
                String xmlNodeData = scm.getString("xmlNodeData");
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
            JSONObject nodeObj;
            if (node instanceof Map) {
                nodeObj = new JSONObject(((Map) node));
            }else {
                nodeObj = (JSONObject) node;
            }
            XmlNode xmlNode = nodeObj.toJavaObject(XmlNode.class);
            if (rootElement.getChild(xmlNode.getXmlNodeName()) == null) {
                Element element = XmlUtils.stringToXmlElement(xmlNode.getXmlNodeData());
                rootElement.addContent(element);
            }
        }));
    }

    public static final class ScmTypeHolder {
        public static final String SCM_NULL = "SCM_NULL";
        public static final String SCM_GIT = "SCM_GIT";
        public static final String SCM_SVN = "SCM_SVN";
    }

    public static final class ScmTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(ScmTypeHolder.SCM_NULL, ScmNull.class);
            TYPE_MAP.put(ScmTypeHolder.SCM_GIT, ScmGit.class);
            TYPE_MAP.put(ScmTypeHolder.SCM_SVN, ScmSvn.class);
        }
    }

}
