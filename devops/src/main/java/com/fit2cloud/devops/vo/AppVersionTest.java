package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/7
 */
@Data
public class AppVersionTest {
    private String id;
    private String name;
    private int testCount;
    private int testSuccessRate;
    private long testAvgTime;
}
