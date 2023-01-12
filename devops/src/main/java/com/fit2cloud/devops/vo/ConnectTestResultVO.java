package com.fit2cloud.devops.vo;

import com.fit2cloud.devops.dto.ServerDTO;

import java.util.List;

public class ConnectTestResultVO {
    private Integer total;
    private Integer success;
    private Integer failed;
    private Long startTime;
    private Long endTime;
    private List<ServerDTO> results;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<ServerDTO> getResults() {
        return results;
    }

    public void setResults(List<ServerDTO> results) {
        this.results = results;
    }
}
