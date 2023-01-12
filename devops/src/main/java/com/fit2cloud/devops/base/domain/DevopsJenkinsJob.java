package com.fit2cloud.devops.base.domain;

import org.springframework.data.annotation.Transient;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

@Data
public class DevopsJenkinsJob implements Serializable {
    @Id
    private String id;
    private String appId;
    private String name;
    private String syncStatus;
    private Boolean buildable;
    private Integer buildSize;
    private String url;
    private String description;
    private Long createTime;
    private Long updateTime;
    private Long syncTime;
    private String source;
    private String workspace;
    private String organization;
    private String type;
    private String creator;
    private String buildStatus;
    private Boolean parameterizedBuild;
    private String extParam;
    private String parentId;
    private String jobXml;
    @Transient
    private HashMap<String, String> params; // 表示该属性并非是一个要映射到数据库表中的字段,只是起辅助作用
}