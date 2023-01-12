package com.fit2cloud.devops.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/11/30
 */
@Data
public class DevopsPipelineMessage {
    private String id;
    private String useName;
    private String appName;
    private String envName;
    private String url;
    private String createTime;
    private List<BasicPipelineEvent> events;
}
