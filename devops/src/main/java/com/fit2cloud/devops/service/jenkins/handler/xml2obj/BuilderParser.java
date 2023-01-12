package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.builder.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.*;

public class BuilderParser extends AbstractConvertor<Document,BaseJobModel> {

    public BuilderParser() {
        super("builderParser","builders");
    }

    public BuilderParser(String name) {
        super(name);
    }

    public BuilderParser(String name, String sourceName) {
        super(name, sourceName);
    }

    public BuilderParser(String name, String sourceName,String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
        this.convertMap.forEach((xmlNodeName,fieldName) -> {
            Element buildersElem = rootElement.getChild(xmlNodeName);
            Optional.ofNullable(buildersElem).ifPresent(builders -> {
                Field field = FieldUtils.getField(target.getClass(), fieldName, true);
                try {
                    Object buildersListObj = field.get(target);
                    if (buildersListObj == null) {
                        FieldUtils.writeField(field,target,new ArrayList<BaseBuilderModel>(),true);
                    }
                    final List<BaseBuilderModel> buildersList = ((List<BaseBuilderModel>) field.get(target));
                    builders.getChildren().forEach(builder -> {
                        Class<?> clazz = BuilderTypeMapHolder.TYPE_MAP.get(builder.getName());
                        String builderXml = XmlUtils.outputXml(builder);
                        if (clazz != null) {
                            Object o = XmlUtils.fromXml(builderXml, clazz);
                            buildersList.add((BaseBuilderModel) o);
                        } else {
                            BaseBuilderModel builderNode = new BaseBuilderModel();
                            builderNode.setXmlNodeName(builder.getName());
                            builderNode.setXmlNodeData(builderXml);
                            buildersList.add(builderNode);
                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static final class BuilderTypeHolder {
        public static final String MAVEN_BUILDER = "hudson.tasks.Maven";
        public static final String SHELL_BUILDER = "hudson.tasks.Shell";
        public static final String SONAR_BUILDER = "hudson.plugins.sonar.SonarRunnerBuilder";
        public static final String ANT_BUILDER = "hudson.tasks.Ant";
    }

    public static final class BuilderTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(BuilderTypeHolder.MAVEN_BUILDER, Maven.class);
            TYPE_MAP.put(BuilderTypeHolder.SHELL_BUILDER, Shell.class);
            TYPE_MAP.put(BuilderTypeHolder.SONAR_BUILDER, SonarRunnerBuilder.class);
            TYPE_MAP.put(BuilderTypeHolder.ANT_BUILDER, AntBuilder.class);
        }
    }
}
