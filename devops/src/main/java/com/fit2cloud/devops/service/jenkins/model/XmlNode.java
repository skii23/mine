package com.fit2cloud.devops.service.jenkins.model;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class XmlNode {
    /**
     * xml节点名称
     */
    @XStreamOmitField
    protected String xmlNodeName;
    /**
     * xml节点内容
     */
    @XStreamOmitField
    protected String xmlNodeData;
    /**
     * 排序用的index,主要用于处理排序信息，
     * 例如构建步骤等可排序的组件
     */
    @XStreamOmitField
    protected Integer sortNum;

    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }

    public String getXmlNodeName() {
        return xmlNodeName;
    }

    public void setXmlNodeName(String xmlNodeName) {
        this.xmlNodeName = xmlNodeName;
    }

    public String getXmlNodeData() {
        return xmlNodeData;
    }

    public void setXmlNodeData(String xmlNodeData) {
        this.xmlNodeData = xmlNodeData;
    }
}
