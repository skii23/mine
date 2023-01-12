package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.commons.server.model.WorkspaceOrganization;
import com.fit2cloud.devops.dto.CloudAccountDTO;

import java.util.List;
import java.util.Map;

public interface ExtWorkspaceMapper {

    List<WorkspaceOrganization> getWorkspaceList(Map<String, Object> params);
}
