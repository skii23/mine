package com.fit2cloud.devops.service.jenkins.handler.xml2obj;

import com.fit2cloud.devops.common.consts.JenkinsConstants;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.common.util.XmlUtils;
import com.fit2cloud.devops.service.jenkins.handler.AbstractConvertor;
import com.fit2cloud.devops.service.jenkins.model.XmlNode;
import com.fit2cloud.devops.service.jenkins.model.common.BaseJobModel;
import com.fit2cloud.devops.service.jenkins.model.maven.MavenModuleSet;
import com.offbytwo.jenkins.model.MavenJob;
import com.offbytwo.jenkins.model.MavenModule;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.lang.reflect.Field;
import java.util.Optional;

public class MavenPropertiesParser extends AbstractConvertor<Document, BaseJobModel> {

    public MavenPropertiesParser() {
        super("mavenPropertiesParser");
    }

    @Override
    public void doConvert(Document source, BaseJobModel target) {
        Element rootElement = source.getRootElement();
        JenkinsConstants.FieldMapHolder.MAVEN_JOB_MODEL_FIELDS_MAP.forEach((name,clazz) -> {
            Optional.ofNullable(rootElement.getChild(name)).ifPresent(property -> {
                try {
                    Field field = FieldUtils.getField(target.getClass(), name, true);
                    Object value = property.getValue();
                    if (clazz.isPrimitive() || clazz.equals(String.class)){
                        value = CommonUtils.castValue(value.toString(), clazz);
                    }else {
                        String propertyXml = XmlUtils.outputXml(property);
                        value = XmlUtils.fromXml(propertyXml, clazz);
                    }
                    FieldUtils.writeField(field, target, value, true);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

}
