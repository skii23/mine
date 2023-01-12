package com.fit2cloud.devops.controller.environment;


import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.Cluster;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.ClusterDTO;
import com.fit2cloud.devops.dto.ClusterTagDTO;
import com.fit2cloud.devops.dto.request.ClusterRequest;
import com.fit2cloud.devops.service.ClusterService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "集群")
@RequestMapping("cluster")
public class ClusterController {
    @Resource
    private ClusterService clusterService;

    @PostMapping("save")
    @ApiOperation("保存集群")
    @RequiresPermissions(PermissionConstants.CLUSTER_CREATE)
    public Cluster saveCluster(@RequestBody ClusterTagDTO cluster) {
       return clusterService.saveCluster(cluster);
    }

    @PostMapping("update")
    @ApiOperation("修改集群")
    @RequiresPermissions(PermissionConstants.CLUSTER_UPDATE)
    public Cluster updateCluster(@RequestBody ClusterTagDTO cluster) {
       return clusterService.saveCluster(cluster);
    }

    @PostMapping("delete")
    @ApiOperation("删除集群")
    @RequiresPermissions(PermissionConstants.CLUSTER_DELETE)
    public void deleteCluster(@RequestBody String clusterId) {
        clusterService.deleteCluster(clusterId);
    }

    @PostMapping("list/{goPage}/{pageSize}")
    @ApiOperation("查看集群列表")
    @RequiresPermissions(PermissionConstants.CLUSTER_READ)
    public Pager getClusterList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ClusterRequest clusterRequest) {

        Page page = PageHelper.startPage(goPage, pageSize, true);

        return PageUtils.setPageInfo(page, clusterService.selectCluster(BeanUtils.objectToMap(clusterRequest)));
    }

    @RequestMapping("list")
    public List<ClusterDTO> getClusters(@RequestBody(required = false) Map<String,Object> params) {
        return clusterService.listAll(params);
    }
}
