package com.fit2cloud.devops.vo;
import lombok.Data;

@Data
public class AppMetricsVO {
    private String id;
    private String name;
    private Boolean isCustomized;
    private Long commitDaily;
    private Integer buildFailRate;
    private Long buildAvgPeriod;
    private Integer unitTestSuccessRate;
    private Integer unitTestCoverageRate;
    private Integer deploySuccesRate;
    private Integer apiTestSuccesRate;

    public AppMetricsVO(){
        this.id = "";
        this.name = "";
        this.isCustomized = false;
        this.commitDaily = 0L;
        this.buildFailRate = 0;
        this.buildAvgPeriod = 0L;
        this.unitTestSuccessRate = 0;
        this.unitTestCoverageRate = 0;
        this.deploySuccesRate = 0;
        this.apiTestSuccesRate = 0;
    }
}
