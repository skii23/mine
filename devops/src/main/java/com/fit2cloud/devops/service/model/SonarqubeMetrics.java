package com.fit2cloud.devops.service.model;
import lombok.Data;

@Data
public class SonarqubeMetrics {
    private boolean isNew;
    private Long bugs;
    private Long debt;
    private Long vulnerabilities;
    private Long testCoverage;
    private Long duplicatedRate;
    private Long issue;
    private Long openIssue;
    private Long confiredIssue;
    private Long falsePositionIssue;
    private Long coverageCodeLine;
    private Long codeLines;

    public SonarqubeMetrics(){
        this.isNew = false;
        this.bugs = 0L;
        this.vulnerabilities = 0L;
        this.testCoverage = 0L;
        this.duplicatedRate = 0L;
        this.issue = 0L;
        this.openIssue = 0L;
        this.confiredIssue = 0L;
        this.falsePositionIssue = 0L;
        this.coverageCodeLine = 0L;
        this.codeLines = 0L;
    }
}
