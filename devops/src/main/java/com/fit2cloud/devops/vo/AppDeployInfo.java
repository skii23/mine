package com.fit2cloud.devops.vo;

import lombok.Data;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/11/10
 */
@Data
public class AppDeployInfo {
    private Double score;
    private String curDeployVersion;
    private int deployCount;
    private Integer deploySuccessRate;
    private List<DeployVersion> version;
}
