package com.fit2cloud.devops.service.jenkins.model.common.builder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.BuilderTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/9/26
 */
@XStreamAlias("hudson.plugins.sonar.SonarRunnerBuilder")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
@Data
public class SonarRunnerBuilder extends BaseBuilderModel{

    {
        this.type = BuilderTransformer.BuilderTypeHolder.SONAR;
    }

    @XStreamAsAttribute
    private String plugin = "sonar@2.14";

    private String installationName;

    private String properties;

    private String project;

    private String javaOpts;

    private String additionalArguments;

    private String jdk;

    private String task;

    private String sonarScannerName;
}
