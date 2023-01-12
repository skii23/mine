package com.fit2cloud.devops.service.openapi.model;

/**
 * @author caiwzh
 * @date 2022/9/8
 */
public class SceneRunRequest {

    private String productId;

    private String planId;

    private String env;

    private Boolean onlyUnPassed;

    private Boolean needReport;

    private String operator;

    private String biz;

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Boolean getOnlyUnPassed() {
        return onlyUnPassed;
    }

    public void setOnlyUnPassed(Boolean onlyUnPassed) {
        this.onlyUnPassed = onlyUnPassed;
    }

    public Boolean getNeedReport() {
        return needReport;
    }

    public void setNeedReport(Boolean needReport) {
        this.needReport = needReport;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }
}
