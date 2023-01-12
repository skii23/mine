package com.fit2cloud.devops.service.jenkins.model.common;

/**
 * @author caiwzh
 * @date 2022/10/12
 */
public interface JobStepType {

    String getXmlTag();

    String getJavaType();

    Class<?> getClazz();
}
