package com.fit2cloud.devops.controller.environment;


import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.ClusterRole;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.ClusterRoleDTO;
import com.fit2cloud.devops.dto.request.ClusterRoleRequest;
import com.fit2cloud.devops.service.ClusterRoleService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "主机组")
@RequestMapping("clusterRole")
public class ClusterRoleController {

    @Resource
    private ClusterRoleService clusterRoleService;

    @ApiOperation("保存主机组")
    @PostMapping("save")
    @RequiresPermissions(PermissionConstants.CLUSTER_ROLE_CREATE)
    public ClusterRole saveClusterRole(@RequestBody ClusterRole clusterRole) {
        return clusterRoleService.saveClusterRole(clusterRole);

    }

    @ApiOperation("修改主机组")
    @PostMapping("update")
    @RequiresPermissions(PermissionConstants.CLUSTER_ROLE_UPDATE)

    public ClusterRole updateClusterRole(@RequestBody ClusterRole clusterRole) {
        return clusterRoleService.saveClusterRole(clusterRole);
    }

    @ApiOperation("主机组列表")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.CLUSTER_ROLE_READ)

    public Pager getClusterRoleList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ClusterRoleRequest clusterRoleRequest) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, clusterRoleService.selectClusterRole(BeanUtils.objectToMap(clusterRoleRequest)));
    }

    @ApiOperation("删除主机组")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.CLUSTER_ROLE_DELETE)

    public void deleteCluster(@RequestBody String clusterRoleId) {
        clusterRoleService.deleteClusterRole(clusterRoleId);
    }

    @GetMapping("list")
    public List<ClusterRoleDTO> getClusterByClusterId(@RequestParam(required = false) String clusterId) {
        Map<String, Object> params = new HashMap<>();
        if (clusterId != null) {
            params.put("clusterId", clusterId);
        }
        return clusterRoleService.selectClusterRole(params);
    }

    @RequestMapping("getClusterRoles")
    public List<ClusterRoleDTO> getClusterRoles(@RequestBody(required = false) Map<String, Object> params) {
        return clusterRoleService.selectClusterRole(params);
    }

}
