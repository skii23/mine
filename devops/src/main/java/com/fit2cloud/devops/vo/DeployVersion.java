package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/10
 */
@Data
public class DeployVersion {
    private String id;
    private String name;
    private long deployCount;
    private long deploySuccessCount;
    private long deployRollbackCount;
    private long deployAvgTime;
}
