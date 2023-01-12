package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.builder.AntBuilder;
import com.fit2cloud.devops.service.jenkins.model.common.builder.Maven;
import com.fit2cloud.devops.service.jenkins.model.common.builder.Shell;
import com.fit2cloud.devops.service.jenkins.model.common.builder.SonarRunnerBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BuilderTransformer extends AbstractConvertor<JSONObject, Document> {

    public BuilderTransformer() {
        super("builderTransformer","builders");
    }

    public BuilderTransformer(String name) {
        super(name);
    }

    public BuilderTransformer(String name, String sourceName) {
        super(name, sourceName);
    }

    public BuilderTransformer(String name, String sourceName,String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();
        this.convertMap.forEach((fieldName, xmlNodeName) -> {
            rootElement.removeChild(xmlNodeName);
            Element buildersElem = new Element(xmlNodeName);
            JSONArray buildersArray = source.getJSONArray(fieldName);
            //排序
            if (CollectionUtils.isNotEmpty(buildersArray) && buildersArray.size() > 1) {
                buildersArray.sort((o1, o2) -> {
                    JSONObject json = new JSONObject(((Map) o1));
                    if (StringUtils.equalsIgnoreCase(json.getString("type"), "SONAR")) {
                        return -1;
                    }
                    return 1;
                });
            }
            Optional.ofNullable(buildersArray).ifPresent(builders -> builders.forEach(builder -> {
                JSONObject builderObj;
                if (builder instanceof Map) {
                    builderObj = new JSONObject(((Map) builder));
                }else {
                    builderObj = (JSONObject) builder;
                }
                String type = builderObj.getString("type");
                Class<?> clazz = BuilderTypeMapHolder.TYPE_MAP.get(type);
                if (clazz != null) {
                    Object o = builderObj.toJavaObject(clazz);
                    Element builderElem = XmlUtils.objToXmlElement(o);
                    buildersElem.addContent(builderElem);
                } else {
                    String xmlNodeData = builderObj.getString("xmlNodeData");
                    if (StringUtils.isNotBlank(xmlNodeData)) {
                        Element builderElem = XmlUtils.stringToXmlElement(xmlNodeData);
                        buildersElem.addContent(builderElem);
                    }
                }
            }));
            rootElement.addContent(buildersElem);
        });
    }

    public static final class BuilderTypeHolder {
        public static final String MAVEN = "MAVEN";
        public static final String SHELL = "SHELL";
        public static final String SONAR = "SONAR";
        public static final String ANT = "ANT";
    }

    public static final class BuilderTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(BuilderTypeHolder.MAVEN, Maven.class);
            TYPE_MAP.put(BuilderTypeHolder.SHELL, Shell.class);
            TYPE_MAP.put(BuilderTypeHolder.SONAR, SonarRunnerBuilder.class);
            TYPE_MAP.put(BuilderTypeHolder.ANT, AntBuilder.class);
        }
    }
}
