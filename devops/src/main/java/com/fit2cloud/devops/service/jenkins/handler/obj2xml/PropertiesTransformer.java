package com.fit2cloud.devops.service.jenkins.handler.obj2xml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.BuildDiscarderProperty;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy.LogRotatorStrategy;
import com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.ParametersDefinitionProperty;
import com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter.*;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PropertiesTransformer extends AbstractConvertor<JSONObject, Document> {

    public PropertiesTransformer() {
        super("propertiesTransformer","properties");
    }

    public PropertiesTransformer(String name) {
        super(name);
    }

    public PropertiesTransformer(String name, String sourceName) {
        super(name, sourceName);
    }

    public PropertiesTransformer(String name, String sourceName,String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    public void doConvert(JSONObject source, Document target) {
        Element rootElement = target.getRootElement();
        this.convertMap.forEach((fieldName, xmlNodeName) -> {
            rootElement.removeChild(xmlNodeName);
            Element propertiesElem = new Element(xmlNodeName);
            JSONArray propertiesArray = source.getJSONArray(fieldName);
            Optional.ofNullable(propertiesArray).ifPresent(properties -> properties.forEach(property -> {
                JSONObject propertyObj;
                if (property instanceof Map) {
                    propertyObj = new JSONObject(((Map) property));
                }else {
                    propertyObj = (JSONObject) property;
                }
                String propType = propertyObj.getString("type");
                if (StringUtils.isNotBlank(propType)) {
                    switch (propType) {
                        case PropertyTypeHolder.BUILD_DISCARDER: {
                            BuildDiscarderProperty buildDiscarderProperty = new BuildDiscarderProperty();
                            JSONObject strategyObj = propertyObj.getJSONObject("strategy");
                            Optional.ofNullable(strategyObj).ifPresent(strategy -> {
                                String type = strategy.getString("type");
                                if (StringUtils.isNotBlank(type)) {
                                    switch (type) {
                                        case StrategyTypeHolder.LOG_ROTATOR_STRATEGY: {
                                            Class<?> clazz = StrategyTypeMapHolder.TYPE_MAP.get(type);
                                            Object o = strategyObj.toJavaObject(clazz);
                                            buildDiscarderProperty.setStrategy((LogRotatorStrategy) o);
                                            Element element = XmlUtils.objToXmlElement(buildDiscarderProperty);
                                            propertiesElem.addContent(element);
                                            break;
                                        }
                                        default: {
                                            String xmlNodeData = strategyObj.getString("xmlNodeData");
                                            if (StringUtils.isNotBlank(xmlNodeData)) {
                                                Element propElem = XmlUtils.objToXmlElement(buildDiscarderProperty);
                                                Element strategyElem = XmlUtils.stringToXmlElement(xmlNodeData);
                                                propElem.addContent(strategyElem);
                                                propertiesElem.addContent(propElem);
                                            }
                                        }
                                    }
                                } else {
                                    String xmlNodeData = strategyObj.getString("xmlNodeData");
                                    if (StringUtils.isNotBlank(xmlNodeData)) {
                                        Element propElem = XmlUtils.objToXmlElement(buildDiscarderProperty);
                                        Element strategyElem = XmlUtils.stringToXmlElement(xmlNodeData);
                                        propElem.addContent(strategyElem);
                                        propertiesElem.addContent(propElem);
                                    }
                                }
                            });
                            break;
                        }
                        case PropertyTypeHolder.PARAMETERS_DEFINITION: {
                            ParametersDefinitionProperty parametersDefinitionProperty = new ParametersDefinitionProperty();
                            JSONArray parameterDefinitionArrays = propertyObj.getJSONArray("parameterDefinitions");

                            Optional.ofNullable(parameterDefinitionArrays).ifPresent(parameterDefinitions -> parameterDefinitions.forEach(parameterDefinition -> {
                                JSONObject parameterDefinitionObj = (JSONObject) parameterDefinition;
                                // json ?????????????????????type??????????????????
                                String type = parameterDefinitionObj.getString("type");
                                // ?????????type???????????????xml????????? eg.StringParameterDefinition.class ChoiceParameterDefinition.class
                                Class<?> clazz = PropertiesTransformer.ParametersTypeMapHolder.TYPE_MAP.get(type);
                                // ??????????????????
                                if (clazz != null) {
                                    BaseParameterDefinitionModel o = (BaseParameterDefinitionModel)parameterDefinitionObj.toJavaObject(clazz);
                                    if (o instanceof ChoiceParameterDefinition) {
                                        // ?????????Xstream?????? ?????????????????????????????????????????????????????? String[] ???????????????????????????????????????
                                        ChoiceParameterDefinition cloneObj = ((ChoiceParameterDefinition) o).clone();
                                        parametersDefinitionProperty.addParameterDefinition(cloneObj);
                                    } else {
                                        // ?????? ????????????????????????????????? ????????????xstream ???????????????????????????
                                        // ?????? ???????????????java?????????????????????????????? ???xstream.processAnnotations(??????.class);
                                        // ????????? ??????????????????????????????????????? ????????????????????????@XStreamInclude({ChoiceParameterDefinition.class})
                                        parametersDefinitionProperty.addParameterDefinition(o);
                                    }
                                }
//                                else {
//                                    String xmlNodeData = parameterDefinitionObj.getString("xmlNodeData");
//                                    if (StringUtils.isNotBlank(xmlNodeData)) {
//                                        Element builderElem = XmlUtils.stringToXmlElement(xmlNodeData);
//                                        propertiesElem.addContent(builderElem);
//                                    }
//                                }
                            }));

                            Element parametersDefinitionElem = XmlUtils.objToElement(parametersDefinitionProperty);
                            propertiesElem.addContent(parametersDefinitionElem);
                        }
                        default: {
                            String xmlNodeData = propertyObj.getString("xmlNodeData");
                            if (StringUtils.isNotBlank(xmlNodeData)) {
                                Element propElem = XmlUtils.stringToXmlElement(xmlNodeData);
                                propertiesElem.addContent(propElem);
                            }
                        }
                    }
                } else {
                    String xmlNodeData = propertyObj.getString("xmlNodeData");
                    if (StringUtils.isNotBlank(xmlNodeData)) {
                        Element propElem = XmlUtils.stringToXmlElement(xmlNodeData);
                        propertiesElem.addContent(propElem);
                    }
                }
            }));
            rootElement.addContent(propertiesElem);
        });
    }

    public static final class PropertyTypeHolder {
        public static final String BUILD_DISCARDER = "BUILD_DISCARDER";
        public static final String PARAMETERS_DEFINITION = "PARAMETERS_DEFINITION";
    }

    public static final class StrategyTypeHolder {
        public static final String LOG_ROTATOR_STRATEGY = "LOG_ROTATOR";
    }

    public static final class StrategyTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(StrategyTypeHolder.LOG_ROTATOR_STRATEGY, LogRotatorStrategy.class);
        }
    }

    public static final class ParameterTypeHolder {
        public static final String STRING_PARAMETER = "STRING_PARAMETER";
        public static final String PASSWORD_PARAMETER = "PASSWORD_PARAMETER";
        public static final String BOOLEAN_PARAMETER = "BOOLEAN_PARAMETER";
        public static final String FILE_PARAMETER = "FILE_PARAMETER";
        public static final String TEXT_PARAMETER = "TEXT_PARAMETER";
        public static final String RUN_PARAMETER = "RUN_PARAMETER";
        public static final String CHOICE_PARAMETER = "CHOICE_PARAMETER";
    }

    public static final class ParametersTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(ParameterTypeHolder.STRING_PARAMETER, StringParameterDefinition.class);
            TYPE_MAP.put(ParameterTypeHolder.PASSWORD_PARAMETER, PasswordParameterDefinition.class);
            TYPE_MAP.put(ParameterTypeHolder.BOOLEAN_PARAMETER, BooleanParameterDefinition.class);
            TYPE_MAP.put(ParameterTypeHolder.FILE_PARAMETER, FileParameterDefinition.class);
            TYPE_MAP.put(ParameterTypeHolder.TEXT_PARAMETER, TextParameterDefinition.class);
            TYPE_MAP.put(ParameterTypeHolder.RUN_PARAMETER, RunParameterDefinition.class);
            TYPE_MAP.put(ParameterTypeHolder.CHOICE_PARAMETER, ChoiceParameterDefinition.class);
        }
    }
}
