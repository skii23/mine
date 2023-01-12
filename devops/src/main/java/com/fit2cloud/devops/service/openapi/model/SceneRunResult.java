package com.fit2cloud.devops.service.openapi.model;

/**
 * @author caiwzh
 * @date 2022/9/8
 */
public class SceneRunResult {

    private String passedCount;

    private String status;

    private String passRate;

    private String operator;

    private String reportUrl;

    private String biz;

    private String endTime;

    private String runId;

    private String startTime;

    private String planId;

    private String failedCount;

    private String productId;

    private String totalCount;

    private String untestedCount;

    public String getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(String passedCount) {
        this.passedCount = passedCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassRate() {
        return passRate;
    }

    public void setPassRate(String passRate) {
        this.passRate = passRate;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(String failedCount) {
        this.failedCount = failedCount;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getUntestedCount() {
        return untestedCount;
    }

    public void setUntestedCount(String untestedCount) {
        this.untestedCount = untestedCount;
    }
}
