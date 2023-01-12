package com.fit2cloud.devops.service.jenkins.model.common.publisher;

import com.fit2cloud.devops.service.jenkins.model.common.JobStepType;

/**
 * @author caiwzh
 * @date 2022/10/12
 */
public enum PublisherType implements JobStepType {

    BUILD_TRIGGER_PUBLISHER("hudson.tasks.BuildTrigger", "BUILDER_TRIGGER", BuildTrigger.class),

    F2C_PUBLISHER("com.fit2cloud.codedeploy2.F2CCodeDeploySouthPublisher", "F2C_PUBLISHER", F2CPublisher.class),

    F2C_PUBLISHER_OLD("com.fit2cloud.codedeploy.F2CCodeDeployPublisher", "F2C_PUBLISHER", F2CPublisher.class),

    REDEPLOY_PUBLISHER("hudson.maven.RedeployPublisher", "REDEPLOY_PUBLISHER", RedeployPublisher.class),

    EMAIL_PUBLISHER("hudson.tasks.Mailer", "EMAIL_PUBLISHER", MailerPublisher.class),
    ;
    String xmlTag;

    String javaType;

    Class<?> targetClazz;

    PublisherType(String xmlTag, String javaType, Class<?> targetClazz) {
        this.xmlTag = xmlTag;
        this.javaType = javaType;
        this.targetClazz = targetClazz;
    }

    @Override
    public String getXmlTag() {
        return xmlTag;
    }

    @Override
    public String getJavaType() {
        return javaType;
    }

    @Override
    public Class<?> getClazz() {
        return targetClazz;
    }
}
