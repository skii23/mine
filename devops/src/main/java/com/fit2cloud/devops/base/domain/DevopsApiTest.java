package com.fit2cloud.devops.base.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "devops_api_test")
public class DevopsApiTest implements Serializable {
    @Id
    @JSONField(serialize = false)
    private String id;
    @JSONField(serialize = false)
    private Long startTime;
    @JSONField(serialize = false)
    private Long endTime;
    @JSONField(serialize = false)
    private String deployId;
    private String productId;
    private String planId;
    private String runId; 
    private String env;
    @JSONField(serialize = false)
    private String operator;
    @JSONField(serialize = false)
    private String result; 
    private String reportUrl; 
    private Long totalCount; 
    private Long untestedCount; 
    private Long passCount; 
    private Long failCount;

    public DevopsApiTest() {
        this.startTime = 0L;
        this.endTime = 0L;
        this.totalCount = 0L;
        this.untestedCount = 0L;
        this.passCount = 0L;
        this.failCount = 0L;
    }
}