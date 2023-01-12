package com.fit2cloud.devops.dto.request;

import com.fit2cloud.commons.annotation.FuzzyQuery;
import io.swagger.annotations.ApiModelProperty;

public class ApplicationVersionRequest {

    @ApiModelProperty(value = "版本号,模糊匹配")
    @FuzzyQuery
    private String name;
    @ApiModelProperty("应用ID")
    private String applicationId;
    @ApiModelProperty(value = "排序key", hidden = true)
    private String sort;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
