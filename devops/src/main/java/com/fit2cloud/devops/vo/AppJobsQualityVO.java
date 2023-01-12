package com.fit2cloud.devops.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AppJobsQualityVO {
    private Double score;
    private Integer jobCount;
    private List<AppJobsQualityMetricsVO> metrics;

    public AppJobsQualityVO() {
        this.score = 1.5;
        this.jobCount = 0;
        this.metrics = new ArrayList<AppJobsQualityMetricsVO>();
    }
}
