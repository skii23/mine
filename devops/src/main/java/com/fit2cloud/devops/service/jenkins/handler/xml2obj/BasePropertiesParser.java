package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.XmlNode;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.scm.BaseScmModel;
import com.fit2cloud.devops.service.jenkins.model.common.scm.ScmNull;
import com.fit2cloud.devops.service.jenkins.model.common.scm.git.ScmGit;
import com.fit2cloud.devops.service.jenkins.model.common.scm.svn.ScmSvn;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BasePropertiesParser extends AbstractConvertor<Document, BaseJobModel> {

    public BasePropertiesParser() {
        super("basePropertiesParser");
    }

    @Override
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
//        先过滤不支持的节点
        rootElement.getChildren().forEach(child -> {
            if (!JenkinsConstants.SUPPORTED_NODE_SET.contains(child.getName())) {
                XmlNode xmlNode = new XmlNode();
                xmlNode.setXmlNodeName(child.getName());
                xmlNode.setXmlNodeData(XmlUtils.outputXml(child));
                target.getUnknownNodes().add(xmlNode);
            }
        });
//        设置基础属性
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
//        设置scm
        Element scmElem = rootElement.getChild("scm");
        Optional.ofNullable(scmElem).ifPresent(scm -> {
            String classStr = scm.getAttributeValue("class");
            Class<?> clazz = ScmTypeMapHolder.TYPE_MAP.get(classStr);
            String scmXml = XmlUtils.outputXml(scm);
            if (clazz != null) {
                Object o = XmlUtils.fromXml(scmXml, clazz);
                target.setScm((BaseScmModel) o);
            } else {
                BaseScmModel scmModel = new BaseScmModel();
                scmModel.setXmlNodeName("scm");
                scmModel.setXmlNodeData(scmXml);
                target.setScm(scmModel);
            }
        });
    }

    public static final class ScmTypeHolder {
        public static final String SCM_NULL = "hudson.scm.NullSCM";
        public static final String SCM_GIT = "hudson.plugins.git.GitSCM";
        public static final String SCM_SVN = "hudson.scm.SubversionSCM";
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
