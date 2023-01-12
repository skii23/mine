package com.fit2cloud.devops.service.jenkins.model.sysconfig;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author caiwzh
 * @date 2022/8/19
 */
public interface Config {

    @JSONField(serialize = false)
    String getAlias();

    default void replaceYamlConfig(){

    }
}
