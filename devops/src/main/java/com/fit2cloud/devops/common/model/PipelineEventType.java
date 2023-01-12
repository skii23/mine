package com.fit2cloud.devops.common.model;

import lombok.Getter;

/**
 * @author caiwzh
 * @date 2022/11/28
 */
public enum PipelineEventType {
    CODE("code","代码信息"),
    JENKINS_BUILD("jenkinsBuild","流水线jenkins构建信息"),
    SONARQUBE("sonarqube","流水线sonarqube扫描数据"),
    JENKINS_UNIT_TEST("jenkinsUnitTest","流水线jenkins单元测试数据"),
    CMP_DEPLOY("cmpDeploy","流水线云管的部署信息"),
    API_TEST("APITest","自动化API测试数据"),
    ;
    @Getter
    private String name;
    @Getter
    private String describe;
    PipelineEventType(String name, String describe) {
        this.name = name;
        this.describe = describe;
    }
}
