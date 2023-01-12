package com.fit2cloud.devops.service.jenkins.model;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 抽象的jenkins的基础模型，包含基础属性
 * @author wisonic
 * @date 20.1.10
 */
public abstract class AbstractBaseModel extends XmlNode {
    /**
     * 类型信息，例如不同的构建步骤和发布器
     * 注意，此标识注解此属性不会写到xml中
     */
    @XStreamOmitField
    protected String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
