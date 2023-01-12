package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.buildWrapper.AntWrapper;
import com.fit2cloud.devops.service.jenkins.model.common.buildWrapper.BaseBuildWrapperModel;

import com.fit2cloud.devops.service.jenkins.model.common.buildWrapper.PreBuildCleanup;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.*;

public class BuildWrapperParser extends AbstractConvertor<Document,BaseJobModel> {

    public BuildWrapperParser() {
        super("buildWrapperParser","buildWrappers");
    }

    public BuildWrapperParser(String name) {
        super(name);
    }

    public BuildWrapperParser(String name, String sourceName) {
        super(name, sourceName);
    }

    public BuildWrapperParser(String name, String sourceName, String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
        this.convertMap.forEach((xmlNodeName,fieldName) -> {
            Element buildWrappersElem = rootElement.getChild(xmlNodeName);
            Optional.ofNullable(buildWrappersElem).ifPresent(buildWrappers -> {
                Field field = FieldUtils.getField(target.getClass(), fieldName, true);
                try {
                    Object buildWrappersListObj = field.get(target);
                    if (buildWrappersListObj == null) {
                        FieldUtils.writeField(field, target, new ArrayList<BaseBuildWrapperModel>(), true);
                    }
                    final List<BaseBuildWrapperModel> buildWrappersList = ((List<BaseBuildWrapperModel>) field.get(target));
                    buildWrappers.getChildren().forEach(buildWrapper -> {
                        Class<?> clazz = BuildWrapperParser.BuildWrapperTypeMapHolder.TYPE_MAP.get(buildWrapper.getName());
                        String publisherXml = XmlUtils.outputXml(buildWrapper);
                        if (clazz != null) {
                            Object o = XmlUtils.fromXml(publisherXml, clazz);
                            buildWrappersList.add((BaseBuildWrapperModel) o);
                        } else {
                            BaseBuildWrapperModel buildWrapperNode = new BaseBuildWrapperModel();
                            buildWrapperNode.setXmlNodeName(buildWrapper.getName());
                            buildWrapperNode.setXmlNodeData(publisherXml);
                            buildWrappersList.add(buildWrapperNode);
                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static final class BuildWrapperTypeHolder {
        public static final String ANT_WRAPPER = "hudson.tasks.AntWrapper";
        public static final String PRE_BUILD_CLEANUP = "hudson.plugins.ws__cleanup.PreBuildCleanup";
    }

    public static final class BuildWrapperTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(BuildWrapperTypeHolder.ANT_WRAPPER, AntWrapper.class);
            TYPE_MAP.put(BuildWrapperTypeHolder.PRE_BUILD_CLEANUP, PreBuildCleanup.class);
        }
    }
}
