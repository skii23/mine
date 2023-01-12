package com.fit2cloud.devops.controller;

import com.fit2cloud.commons.server.base.domain.Organization;
import com.fit2cloud.commons.server.base.domain.Workspace;
import com.fit2cloud.devops.service.StructureService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("structure")
public class StructureController {

    @Resource
    private StructureService structureService;

    @RequestMapping("getOrganizations")
    public List<Organization> getOrganizations() {
        return structureService.getOrganizations();
    }

    @RequestMapping("getWorkspaces")
    public List<Workspace> getWorkspaces(@RequestParam(required = false) String orgId) {
        return structureService.getWorkspaces(orgId);
    }

}
