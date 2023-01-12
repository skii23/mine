package com.fit2cloud.devops.vo;

import lombok.Data;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/11/7
 */
@Data
public class AppApiTestInfo {
    private Double score;
    private int testCount;
    private Integer deployAvgSuccessRate;
    private List<AppVersionTest> version;
}
