package com.fit2cloud.devops.base.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "devops_unittest")
public class DevopsUnitTest implements Serializable {
    @Id
    private String id;    
    private Long createTime;
    private String jobHistoryId;
    @JSONField(name = "logUrl")
    private String url;
    private Long failCount;
    private Long skipCount;
    private Long allCount;
    public DevopsUnitTest() {
        this.allCount = 0L;
        this.skipCount = 0L;
        this.failCount = 0L;
        this.url = "";
    }
}