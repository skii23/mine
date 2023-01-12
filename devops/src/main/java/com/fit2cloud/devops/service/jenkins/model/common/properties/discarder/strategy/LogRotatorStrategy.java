package com.fit2cloud.devops.service.jenkins.model.common.properties.discarder.strategy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fit2cloud.devops.service.jenkins.handler.obj2xml.PropertiesTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * General模块 丢弃旧有构建 策略
 */
@XStreamAlias("strategy")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LogRotatorStrategy extends BaseStrategyModel{

    {
        this.classStr = "hudson.tasks.LogRotator";
        this.type = PropertiesTransformer.StrategyTypeHolder.LOG_ROTATOR_STRATEGY;
    }

    /**
     * 保留天数
     */
    private Integer daysToKeep = -1;
    /**
     * 保留条数
     */
    private Integer numToKeep = -1;
    /**
     * 发包保留天数
     */
    private Integer artifactDaysToKeep = -1;
    /**
     * 最大保留构建
     */
    private Integer artifactNumToKeep = -1;

    public Integer getDaysToKeep() {
        return daysToKeep;
    }

    public void setDaysToKeep(Integer daysToKeep) {
        this.daysToKeep = daysToKeep;
    }

    public Integer getNumToKeep() {
        return numToKeep;
    }

    public void setNumToKeep(Integer numToKeep) {
        this.numToKeep = numToKeep;
    }

    public Integer getArtifactDaysToKeep() {
        return artifactDaysToKeep;
    }

    public void setArtifactDaysToKeep(Integer artifactDaysToKeep) {
        this.artifactDaysToKeep = artifactDaysToKeep;
    }

    public Integer getArtifactNumToKeep() {
        return artifactNumToKeep;
    }

    public void setArtifactNumToKeep(Integer artifactNumToKeep) {
        this.artifactNumToKeep = artifactNumToKeep;
    }
}
