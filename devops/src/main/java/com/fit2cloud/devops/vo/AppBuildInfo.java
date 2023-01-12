package com.fit2cloud.devops.vo;

import lombok.Data;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/11/8
 */
@Data
public class AppBuildInfo {
    private Double score;
    private int jobCount;
    private List<JobBuildInfo> jobs;
}
