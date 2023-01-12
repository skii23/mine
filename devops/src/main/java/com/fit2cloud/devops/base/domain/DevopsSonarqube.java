package com.fit2cloud.devops.base.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "devops_sonarqube")
public class DevopsSonarqube implements Serializable {
    @Id
    private String id;    
    private Long createTime;
    private String jobHistoryId;
    private String projectKey;
    @JSONField(name = "projectUrl")
    private String url;
    @JSONField(name = "codeLines")
    private Long ncloc; 
    private Long bugs;
    private Long debt;
    private Long vulnerabilities; 
    private Float testCoverage; 
    private Long testLine; 
    private Float duplicatedRate; 
    private Long issues; 
    private Long openIssue;
    @JSONField(name = "confirmedIssue")
    private Long confiredIssue;
    private Long falsePositionIssue;
    private Float newTestCoverage; 
    private Long newTestLine;
    private Long newVulnerabilities;
    private Long newLines;
}