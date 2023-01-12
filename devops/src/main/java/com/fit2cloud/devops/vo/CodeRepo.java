package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/4
 */
@Data
public class CodeRepo {
    private String id;
    private String name;
    private Integer branchCount;
    private Integer tagCount;
    private Integer mrCount;
    private Integer commitCount;
    private Integer committerCount;
}
