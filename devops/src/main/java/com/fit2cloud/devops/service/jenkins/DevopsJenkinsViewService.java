package com.fit2cloud.devops.service.jenkins;

import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsView;
import com.fit2cloud.devops.base.domain.DevopsViewJobMapping;
import com.fit2cloud.devops.base.domain.DevopsViewJobMappingExample;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsViewMapper;
import com.fit2cloud.devops.base.mapper.DevopsViewJobMappingMapper;
import com.fit2cloud.devops.common.util.CommonUtils;
import com.fit2cloud.devops.dao.ext.ExtDevopsJenkinsViewMapper;
import com.fit2cloud.devops.dto.DevopsJenkinsViewDTO;
import com.fit2cloud.devops.dto.request.JobViewRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DevopsJenkinsViewService {

    @Resource
    private ExtDevopsJenkinsViewMapper extDevopsJenkinsViewMapper;

    @Resource
    private DevopsJenkinsViewMapper devopsJenkinsViewMapper;

    @Resource
    private DevopsViewJobMappingMapper devopsViewJobMappingMapper;

    public List<DevopsJenkinsViewDTO> getViews(Map<String, Object> params) {
        CommonUtils.filterPermission(params);
        return extDevopsJenkinsViewMapper.getViewList(params);
    }


    public DevopsJenkinsViewDTO saveJobView(DevopsJenkinsViewDTO devopsJenkinsViewDTO) {
        if (devopsJenkinsViewDTO.getId() != null) {
            return updateJobView(devopsJenkinsViewDTO);
        }else {
            return createJobView(devopsJenkinsViewDTO);
        }
    }

    private DevopsJenkinsViewDTO createJobView(DevopsJenkinsViewDTO devopsJenkinsViewDTO) {
        String organizationId = SessionUtils.getOrganizationId();
        String workspaceId = SessionUtils.getWorkspaceId();
        SessionUser user = SessionUtils.getUser();
        devopsJenkinsViewDTO.setOrganization(organizationId);
        devopsJenkinsViewDTO.setWorkspace(workspaceId);
        devopsJenkinsViewDTO.setCreateTime(System.currentTimeMillis());
        devopsJenkinsViewDTO.setId(UUIDUtil.newUUID());
        devopsJenkinsViewDTO.setCreator(user.getId());
        if (devopsJenkinsViewMapper.insertSelective(devopsJenkinsViewDTO) == 1) {
            devopsJenkinsViewDTO.getJobIdSet().forEach(id -> {
                DevopsViewJobMapping devopsViewJobMapping = new DevopsViewJobMapping();
                devopsViewJobMapping.setId(UUIDUtil.newUUID());
                devopsViewJobMapping.setViewId(devopsJenkinsViewDTO.getId());
                devopsViewJobMapping.setJobId(id);
                devopsViewJobMappingMapper.insert(devopsViewJobMapping);
            });
            return extDevopsJenkinsViewMapper.getViewById(devopsJenkinsViewDTO.getId());
        }
        return null;
    }

    private DevopsJenkinsViewDTO updateJobView(DevopsJenkinsViewDTO devopsJenkinsViewDTO) {
        DevopsViewJobMappingExample devopsViewJobMappingExample = new DevopsViewJobMappingExample();
        devopsViewJobMappingExample.createCriteria().andViewIdEqualTo(devopsJenkinsViewDTO.getId());
        devopsViewJobMappingMapper.deleteByExample(devopsViewJobMappingExample);
        devopsJenkinsViewDTO.getJobIdSet().forEach(id -> {
            DevopsViewJobMapping devopsViewJobMapping = new DevopsViewJobMapping();
            devopsViewJobMapping.setId(UUIDUtil.newUUID());
            devopsViewJobMapping.setJobId(id);
            devopsViewJobMapping.setViewId(devopsJenkinsViewDTO.getId());
            devopsViewJobMappingMapper.insert(devopsViewJobMapping);
        });
        devopsJenkinsViewDTO.setUpdateTime(System.currentTimeMillis());
        devopsJenkinsViewMapper.updateByPrimaryKeySelective(devopsJenkinsViewDTO);
        return devopsJenkinsViewDTO;
    }

    public void deleteViews(List<DevopsJenkinsView> devopsJenkinsViews) {
        Optional.ofNullable(devopsJenkinsViews).ifPresent(views -> views.forEach(view -> {
            if (devopsJenkinsViewMapper.deleteByPrimaryKey(view.getId()) == 1) {
                DevopsViewJobMappingExample devopsViewJobMappingExample = new DevopsViewJobMappingExample();
                devopsViewJobMappingExample.createCriteria().andViewIdEqualTo(view.getId());
                devopsViewJobMappingMapper.deleteByExample(devopsViewJobMappingExample);
            }
        }));
    }

    public void addJobsToViews(JobViewRequest jobViewRequest) {
        jobViewRequest.getViews().forEach(view -> {
            DevopsJenkinsViewDTO currentView = extDevopsJenkinsViewMapper.getViewById(view.getId());
            jobViewRequest.getJobs().forEach(job -> {
                if (!currentView.getJobIdSet().contains(job.getId())) {
                    DevopsViewJobMapping devopsViewJobMapping = new DevopsViewJobMapping();
                    devopsViewJobMapping.setId(UUIDUtil.newUUID());
                    devopsViewJobMapping.setViewId(view.getId());
                    devopsViewJobMapping.setJobId(job.getId());
                    devopsViewJobMappingMapper.insert(devopsViewJobMapping);
                }
            });
        });
    }
}
