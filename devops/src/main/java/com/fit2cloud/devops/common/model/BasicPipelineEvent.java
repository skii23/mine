package com.fit2cloud.devops.common.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/30
 */
@Data
public class BasicPipelineEvent {
    @JSONField(serialize = false)
    private PipelineEventType eventType;
    private String name;
    private String describe;
    private String startTime;
    private String endTime;
    private String result;
    private String url;
    private JSONObject data;

    public BasicPipelineEvent(PipelineEventType eventType) {
        this.eventType = eventType;
        this.name = eventType.getName();
        this.describe = eventType.getDescribe();
    }
}
