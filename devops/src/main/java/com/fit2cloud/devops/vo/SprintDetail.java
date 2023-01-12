package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/1
 */
@Data
public class SprintDetail extends SprintInfo{
    private Long startTime;
    private Long endTime;
    private JiraIssue issue;
}
