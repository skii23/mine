package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.base.domain.Organization;
import com.fit2cloud.commons.server.base.domain.OrganizationExample;
import com.fit2cloud.commons.server.base.domain.Workspace;
import com.fit2cloud.commons.server.base.domain.WorkspaceExample;
import com.fit2cloud.commons.server.base.mapper.OrganizationMapper;
import com.fit2cloud.commons.server.base.mapper.WorkspaceMapper;
import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StructureService {

    @Resource
    private OrganizationMapper organizationMapper;

    @Resource
    private WorkspaceMapper workspaceMapper;


    public List<Organization> getOrganizations() {
        if (StringUtils.equals(SessionUtils.getUser().getParentRoleId(), RoleConstants.Id.ADMIN.name())) {
            return organizationMapper.selectByExample(null);
        }
        return null;
    }

    public List<Workspace> getWorkspaces(String orgId) {
        String role = SessionUtils.getUser().getParentRoleId();
        switch (role) {
            case "ADMIN":{
                if (orgId != null) {
                    WorkspaceExample workspaceExample = new WorkspaceExample();
                    workspaceExample.createCriteria().andOrganizationIdEqualTo(orgId);
                    return workspaceMapper.selectByExample(workspaceExample);
                }else {
                    return workspaceMapper.selectByExample(null);
                }
            }
            case "ORGADMIN":{
                WorkspaceExample workspaceExample = new WorkspaceExample();
                workspaceExample.createCriteria().andOrganizationIdEqualTo(SessionUtils.getOrganizationId());
                return workspaceMapper.selectByExample(workspaceExample);
            }
            default:{
                return null;
            }
        }
    }
}
