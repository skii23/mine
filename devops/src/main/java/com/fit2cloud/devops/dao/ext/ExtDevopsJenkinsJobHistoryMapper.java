package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.DevopsJenkinsJobHistoryDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ExtDevopsJenkinsJobHistoryMapper {
    List<DevopsJenkinsJobHistoryDto> listDevopsJenkinsHistoryJob(Map params);

    @Select("select count(1) from devops_jenkins_job_history where job_id in(select id from devops_jenkins_job where parent_id = #{parentId})")
    int countHistoryByparentId(@Param("parentId") String parentId);
}
