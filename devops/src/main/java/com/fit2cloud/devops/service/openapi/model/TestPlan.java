package com.fit2cloud.devops.service.openapi.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author caiwzh
 * @date 2022/9/8
 */
public class TestPlan {

    private String name;

    @JSONField(name = "plan_id")
    private String planId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }
}
