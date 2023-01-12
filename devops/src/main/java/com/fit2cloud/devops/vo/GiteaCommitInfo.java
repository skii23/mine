package com.fit2cloud.devops.vo;

import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/4
 */
@Data
public class GiteaCommitInfo {
    private String url;
    private String created;
    private String author;
}
