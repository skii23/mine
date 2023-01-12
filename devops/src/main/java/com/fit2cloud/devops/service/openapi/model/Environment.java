package com.fit2cloud.devops.service.openapi.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author caiwzh
 * @date 2022/9/8
 */
public class Environment {

    @JSONField(name = "default_env")
    private String defaultEnv;

    private String delete;

    private String status;

    private String id;

    private String name;

    @JSONField(name = "create_time")
    private String createTime;

    @JSONField(name = "modify_time")
    private String modifyTime;

    @JSONField(name = "refer_prod_id")
    private String referProdId;

    @JSONField(name = "owner_id")
    private String ownerId;

    private Property property;

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getDefaultEnv() {
        return defaultEnv;
    }

    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getReferProdId() {
        return referProdId;
    }

    public void setReferProdId(String referProdId) {
        this.referProdId = referProdId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
