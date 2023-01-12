package com.fit2cloud.devops.vo;

import lombok.Data;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/11/4
 */
@Data
public class AppCodeInfo {
    private Double score;
    private int repoCount;
    private List<CodeRepo> repos;
}
