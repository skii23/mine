package com.fit2cloud.devops.service.openapi.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author caiwzh
 * @date 2022/9/8
 */
public class Product {

    private String id;

    private String name;

    @JSONField(name = "prod_id")
    private String prodId;

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

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }
}
