package com.fit2cloud.devops.base.domain;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Table(name = "devops_measure_customize")
public class MeasureAppCustomize implements Serializable {
    @Id
    private Integer id;
    private String userId;
    private String appId;     
    private Long createTime;
}