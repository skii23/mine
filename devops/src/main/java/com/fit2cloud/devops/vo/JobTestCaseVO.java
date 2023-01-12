package com.fit2cloud.devops.vo;

import lombok.Data;

@Data
public class JobTestCaseVO {
    private Integer uniTestFailCount;
    private Integer uniTestCaseCount;
    private Integer uniTestSkipCount;
}
