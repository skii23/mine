package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.common.properties.BasePropertyModel;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.BuildDiscarderProperty;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy.BaseStrategyModel;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy.LogRotatorStrategy;
import com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.ParametersDefinitionProperty;
import com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.*;

public class PropertiesParser extends AbstractConvertor<Document,BaseJobModel> {

    public PropertiesParser() {
        super("propertiesParser","properties");
    }

    public PropertiesParser(String name) {
        super(name);
    }

    public PropertiesParser(String name, String sourceName) {
        super(name, sourceName);
    }

    public PropertiesParser(String name, String sourceName,String targetName) {
        super(name, sourceName,targetName);
    }

    @Override
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
        this.convertMap.forEach((xmlNodeName,fieldName) -> {
            Element propertiesElem = rootElement.getChild(xmlNodeName);
            Optional.ofNullable(propertiesElem).ifPresent(properties -> {
                Field field = FieldUtils.getField(target.getClass(),fieldName, true);
                try {
                    Object propertiesObj = field.get(target);
                    if (propertiesObj == null) {
                        FieldUtils.writeField(field, target, new ArrayList<BasePropertyModel>(), true);
                    }
                    List<BasePropertyModel> propertiesList = (List<BasePropertyModel>) field.get(target);
                    properties.getChildren().forEach(property -> {
                        switch (property.getName()) {
                            case PropertiesTypeHolder.DISCARDER_PROPERTY: {
                                BuildDiscarderProperty buildDiscarderProperty = new BuildDiscarderProperty();
                                Element strategy = property.getChild("strategy");
                                String strategyXml = XmlUtils.outputXml(strategy);
                                String classStr = strategy.getAttributeValue("class");
                                Class<?> clazz = StrategyTypeMapHolder.TYPE_MAP.get(classStr);
                                if (clazz != null) {
                                    BaseStrategyModel strategyObj = (BaseStrategyModel) XmlUtils.fromXml(strategyXml, clazz);
                                    buildDiscarderProperty.setStrategy(strategyObj);
                                } else {
                                    BaseStrategyModel strategyNode = new BaseStrategyModel();
                                    strategyNode.setXmlNodeName(strategy.getName());
                                    strategyNode.setXmlNodeData(strategyXml);
                                    buildDiscarderProperty.setStrategy(strategyNode);
                                }
                                propertiesList.add(buildDiscarderProperty);
                                break;
                            }
                            case PropertiesTypeHolder.PARAMETERS_PROPERTY: {
                                ParametersDefinitionProperty parametersDefinitionProperty = new ParametersDefinitionProperty();
                                Element parameterDefinitionsElem = property.getChild("parameterDefinitions");
                                Optional.ofNullable(parameterDefinitionsElem.getChildren()).ifPresent(parameterDefinitions ->
                                        parameterDefinitions.forEach(parameterDefinition -> {
                                            Class<?> clazz = ParameterTypeMapHolder.TYPE_MAP.get(parameterDefinition.getName());
                                            String parameterXml = XmlUtils.outputXml(parameterDefinition);

                                            if (clazz != null) {
                                                BaseParameterDefinitionModel baseParameterDefinitionModel = (BaseParameterDefinitionModel) XmlUtils.fromXml(parameterXml, clazz);
                                                // 转换为java对象后，额外的type字段并没有依据子类来填值，如果要保持全环节类型特征名称一致，可以启用下方，注意要和前端判断类型保持一致。
//                                              baseParameterDefinitionModel.setType(parameterDefinition.getName());
//                                                System.out.println("===============================================");
//                                                System.out.println(baseParameterDefinitionModel.toString());

                                                parametersDefinitionProperty.addParameterDefinition(baseParameterDefinitionModel);

                                            } else {
                                                BaseParameterDefinitionModel baseParameterDefinitionModel = new BaseParameterDefinitionModel();
                                                baseParameterDefinitionModel.setXmlNodeName(parameterDefinition.getName());
                                                baseParameterDefinitionModel.setXmlNodeData(parameterXml);
                                                parametersDefinitionProperty.addParameterDefinition(baseParameterDefinitionModel);
                                            }

                                        }));
                                propertiesList.add(parametersDefinitionProperty);
                                break;
                            }
                            default: {
                                BasePropertyModel basePropertyModel = new BasePropertyModel();
                                basePropertyModel.setXmlNodeName(property.getName());
                                basePropertyModel.setXmlNodeData(XmlUtils.outputXml(property));
                                propertiesList.add(basePropertyModel);
                            }
                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static final class PropertiesTypeHolder {
        public static final String DISCARDER_PROPERTY = "jenkins.model.BuildDiscarderProperty";
        public static final String PARAMETERS_PROPERTY = "hudson.model.ParametersDefinitionProperty";
    }

    public static final class PropertiesTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(PropertiesTypeHolder.DISCARDER_PROPERTY, BuildDiscarderProperty.class);
            TYPE_MAP.put(PropertiesTypeHolder.PARAMETERS_PROPERTY, ParametersDefinitionProperty.class);
        }
    }

    public static final class StrategyTypeHolder {
        public static final String LOG_ROTATOR = "hudson.tasks.LogRotator";
    }

    public static final class StrategyTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(StrategyTypeHolder.LOG_ROTATOR, LogRotatorStrategy.class);
        }
    }

    public static final class ParametersTypeHolder {
        public static final String BOOLEAN_DEFINITION = "hudson.model.BooleanParameterDefinition";
        public static final String CHOICE_DEFINITION = "hudson.model.ChoiceParameterDefinition";
        public static final String FILE_DEFINITION = "hudson.model.FileParameterDefinition";
        public static final String PASSWORD_DEFINITION = "hudson.model.PasswordParameterDefinition";
        public static final String RUN_DEFINITION = "hudson.model.RunParameterDefinition";
        public static final String STRING_DEFINITION = "hudson.model.StringParameterDefinition";
        public static final String TEXT_DEFINITION = "hudson.model.TextParameterDefinition";
    }

    public static final class ParameterTypeMapHolder {
        public static final Map<String, Class<?>> TYPE_MAP = new HashMap<>();

        static {
            TYPE_MAP.put(ParametersTypeHolder.BOOLEAN_DEFINITION, BooleanParameterDefinition.class);
            TYPE_MAP.put(ParametersTypeHolder.CHOICE_DEFINITION, ChoiceParameterDefinition.class);
            TYPE_MAP.put(ParametersTypeHolder.FILE_DEFINITION, FileParameterDefinition.class);
            TYPE_MAP.put(ParametersTypeHolder.PASSWORD_DEFINITION, PasswordParameterDefinition.class);
            TYPE_MAP.put(ParametersTypeHolder.RUN_DEFINITION, RunParameterDefinition.class);
            TYPE_MAP.put(ParametersTypeHolder.STRING_DEFINITION, StringParameterDefinition.class);
            TYPE_MAP.put(ParametersTypeHolder.TEXT_DEFINITION, TextParameterDefinition.class);
        }
    }

}
