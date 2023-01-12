package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/1
 */
@Data
public class JiraIssue {
    private long total;
    private long avgSpendTime;
    private long inProcessCount;
    private long finishCount;
    private long featureCount;
    private long stroyCount;
    private long testCount;
    private long bugCount;
}
