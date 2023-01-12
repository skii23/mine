package com.fit2cloud.devops.vo;
import lombok.Data;

@Data
public class AppJobsQualityMetricsVO {
    private String id;
    private String name;
    private Long bugs;
    private Long debt;
    private Long vulnerabilities;
    private Integer uniTestSuccessRate;
    private Integer uniTestFailRate;
    private Integer uniTestFailCount;
    private Integer uniTestCaseCount;
    private Long uniTestCoverage;
    private Long duplicatedRate;
    private Long issue;
    private Long openIssue;
    private Long confiredIssue;
    private Long falsePositionIssue;
    private Long coverageCodeLine;
    private Long codeLines;
    private Long newUniTestCoverage;
    private Long newCoverageCodeLine;
    private Long newCodeLines;
    public AppJobsQualityMetricsVO(){
        this.id = "";
        this.name = "";
        this.bugs = 0L;
        this.debt = 0L;
        this.vulnerabilities = 0L;
        this.uniTestSuccessRate = 0;
        this.uniTestFailRate = 0;
        this.uniTestFailCount = 0;
        this.uniTestCaseCount = 0;
        this.uniTestCoverage = 0L;
        this.duplicatedRate = 0L;
        this.issue = 0L;
        this.openIssue = 0L;
        this.confiredIssue = 0L;
        this.falsePositionIssue = 0L;
        this.coverageCodeLine = 0L;
        this.codeLines = 0L;
        this.newUniTestCoverage = 0L;
        this.newCoverageCodeLine = 0L;
        this.newCodeLines = 0L;
    }
}
