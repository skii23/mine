package com.fit2cloud.devops.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class ScriptImplementLogRequest {

    @ApiModelProperty("脚本ID")
    private String scriptId;
    private String sort;

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }
}
