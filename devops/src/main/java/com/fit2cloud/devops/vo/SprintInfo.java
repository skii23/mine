package com.fit2cloud.devops.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @author caiwzh
 * @date 2022/11/1
 */
@Data
public class SprintInfo {
    private String id;
    private String name;
    private String status;
    @JsonIgnore
    private String originSpritId;
    @JsonIgnore
    private String originBoardId;
}
