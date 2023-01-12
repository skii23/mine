package com.fit2cloud.devops.controller.environment;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.ansible.model.TaskResult;
import com.fit2cloud.commons.server.base.domain.CloudServer;
import com.fit2cloud.commons.server.base.domain.CloudServerCredential;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.base.domain.InstanceType;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.CloudAccountDTO;
import com.fit2cloud.devops.dto.ServerDTO;
import com.fit2cloud.devops.dto.request.CloudServerGroupRequest;
import com.fit2cloud.devops.dto.request.CloudServerProxyRequest;
import com.fit2cloud.devops.dto.request.CloudServerRequest;
import com.fit2cloud.devops.service.CredentialService;
import com.fit2cloud.devops.service.ServerService;
import com.fit2cloud.devops.vo.ConnectTestResultVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("server")
@Api(tags = "主机")
public class ServerController {

    @Resource
    private ServerService serverService;
    @Resource
    private CredentialService credentialService;

    @ApiOperation("查看主机")
    @RequiresPermissions(PermissionConstants.CLOUD_SERVER_READ)
    @PostMapping("list/{goPage}/{pageSize}")
    public Pager getServerList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody CloudServerRequest cloudServerRequest) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, serverService.selectServerDto(BeanUtils.objectToMap(cloudServerRequest)));
    }

    @PostMapping("list")
    public List<ServerDTO> getServerList(@RequestBody CloudServerRequest cloudServerRequest) {
        return serverService.selectServerDto(BeanUtils.objectToMap(cloudServerRequest));
    }

    @PostMapping("credential/connect")
    @ApiOperation("添加时连接测试")
    public ResultHolder connect(@RequestBody String serverJson) {
        JSONObject jsonObject = JSONObject.parseObject(serverJson);
        String clusterRoleId = serverService.toClusterRoleIdStr(jsonObject.getString("clusterRoleId"));
        ServerDTO serverDTO = JSONObject.parseObject(serverJson, ServerDTO.class);
        serverDTO.setClusterId(clusterRoleId);
        ResultHolder resultHolder = new ResultHolder();
        CloudServerCredential cloudServerCredential = new CloudServerCredential();
        BeanUtils.copyBean(cloudServerCredential, serverDTO);
        DevopsCloudServer devopsCloudServer = new DevopsCloudServer();
        BeanUtils.copyBean(devopsCloudServer, serverDTO);
        try {
            TaskResult taskResult = credentialService.find(cloudServerCredential, devopsCloudServer, 2);
            if (taskResult == null) {
                F2CException.throwException("主机无法连通，请检查管理信息是否正确和确定主机SSH/WINRM服务有效！");
            }
            if (taskResult.getResult().isSuccess()) {
                resultHolder.setMessage("验证通过");
            } else {
                resultHolder.setMessage("验证失败，请检查验证信息");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultHolder.setSuccess(false);
            resultHolder.setMessage(e.getMessage());
        }
        return resultHolder;
    }

    @PostMapping("connect")
    @ApiOperation("连接测试")
    public ConnectTestResultVO connect(@RequestBody List<ServerDTO> serverDTOs) {
        return serverService.connectTest(serverDTOs);
    }

    @PostMapping("delete")
    @ApiOperation("删除主机")
    public void deleteServer(@RequestBody List<String> cloudserverIds) {
        serverService.deleteServers(cloudserverIds);
    }

    @RequestMapping("instance/all")
    public List<InstanceType> getAllInstance() {
        return serverService.getAllInstance();
    }

    /**
     * 添加主机
     */
    @RequestMapping("save")
    public String saveServerAdd(@RequestBody String server) {
        return serverService.save(server);
    }

    /**
     * 修改主机
     */
    @RequestMapping("update")
    public String update(@RequestBody String server) {
        return serverService.update(server);
    }

    /**
     * 导入xlsx
     *
     * @param excelFile
     */
    @RequestMapping("import")
    public String saveImport(@RequestParam("file") MultipartFile excelFile) throws Exception {
        return serverService.getImportXlsx(excelFile);
    }

    @PostMapping("cloud/import")
    public void importCloudServer(@RequestBody Map<String, Object> params) {
        serverService.importCloudServer(params);
    }

    @PostMapping("proxy")
    @ApiOperation("设置代理")
    @RequiresPermissions(PermissionConstants.CLOUD_SERVER_PROXY)
    public void proxyServer(@RequestBody CloudServerProxyRequest cloudServerProxyRequest) {
        serverService.proxyServers(cloudServerProxyRequest.getCloudServerIds(), cloudServerProxyRequest.getProxyId());
    }

    @PostMapping("unproxy")
    @ApiOperation("取消代理")
    @RequiresPermissions(PermissionConstants.CLOUD_SERVER_PROXY)
    public void unproxyServer(@RequestBody List<String> cloudserverIds) {
        serverService.unProxyServer(cloudserverIds);
    }

    @PostMapping("group")
    @ApiOperation("加入主机组")
    @RequiresPermissions(PermissionConstants.CLOUD_SERVER_GROUP)
    public void groupServer(@RequestBody CloudServerGroupRequest cloudServerGroupRequest) {
        serverService.groupServer(cloudServerGroupRequest.getCloudServerIds(), cloudServerGroupRequest.getClusterRoleId());
    }

    @PostMapping("ungroup")
    @ApiOperation("移出主机组")
    @RequiresPermissions(PermissionConstants.CLOUD_SERVER_GROUP)
    public void ungroupServer(@RequestBody List<String> cloudserverIds) {
        serverService.unGroupServer(cloudserverIds);
    }

    @RequestMapping("cloudAccount/all")
    @RequiresPermissions(PermissionConstants.CLOUD_SERVER_READ)
    public List<CloudAccountDTO> getAllAccount() {
        return serverService.getAccountList(new HashMap<>());
    }

    @GetMapping("credential/{serverId}")
    @ApiOperation("获取管理信息")
    @RequiresPermissions(PermissionConstants.CLOUD_SERVER_READ)
    public CloudServerCredential getCredential(@PathVariable("serverId") String serverId) {
        return credentialService.selectByServerId(serverId);
    }
}
