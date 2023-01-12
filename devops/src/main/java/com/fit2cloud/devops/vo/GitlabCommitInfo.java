package com.fit2cloud.devops.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/3
 */
@Data
public class GitlabCommitInfo {
    private String id;
    private String title;
    @JSONField(name = "author_name")
    private String authorName;
    @JSONField(name = "committed_date")
    private String committedDate;
    @JSONField(name = "committer_name")
    private String committerName;
    private String message;
    @JSONField(name = "created_at")
    private String createdAt;
    @JSONField(name = "web_url")
    private String webUrl;
}
