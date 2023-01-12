package com.fit2cloud.devops.service.jenkins.model.common.properties.discarder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.PropertiesTransformer;
import com.fit2cloud.devops.service.jenkins.model.common.properties.BasePropertyModel;
import com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy.BaseStrategyModel;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * General模块 丢弃旧有构建
 */
@XStreamAlias("jenkins.model.BuildDiscarderProperty")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class BuildDiscarderProperty extends BasePropertyModel {
    /**
     * General模块 丢弃旧有构建 策略
     */
    @XStreamAlias("strategy")
    private BaseStrategyModel strategy;

    {
        this.type = PropertiesTransformer.PropertyTypeHolder.BUILD_DISCARDER;
    }

    public BaseStrategyModel getStrategy() {
        return strategy;
    }

    public void setStrategy(BaseStrategyModel strategy) {
        this.strategy = strategy;
    }
}
