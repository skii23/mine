package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/3
 */
@Data
public class AppOverall {
    private Double score;
    private String name;
    private String description;
    private String env;
    private String repo;
    private Integer jobs;
    private Integer versions;
    private String latestVersion;
    private String deployVersion;
}
