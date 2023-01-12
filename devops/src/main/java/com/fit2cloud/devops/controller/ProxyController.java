package com.fit2cloud.devops.controller;

import com.fit2cloud.commons.server.service.TagService;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.Proxy;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.ProxyDTO;
import com.fit2cloud.devops.service.ProxyService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "代理")
@RequestMapping("proxy")
@RestController
public class ProxyController {

    @Resource
    private ProxyService proxyService;


    @ApiOperation("代理列表")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROXY_READ)
    public Pager getProxys(@PathVariable int goPage, @PathVariable int pageSize) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, proxyService.selectProxys());
    }

    @ApiOperation("创建代理")
    @PostMapping("create")
    @RequiresPermissions(PermissionConstants.PROXY_CREATE)
    public void saveProxy(@RequestBody Proxy proxy) {
        proxyService.saveProxy(proxy);
    }

    @ApiOperation("修改代理")
    @PostMapping("update")
    @RequiresPermissions(PermissionConstants.PROXY_UPDATE)
    public void updateProxy(@RequestBody Proxy proxy) {
        proxyService.saveProxy(proxy);
    }

    @ApiOperation("删除代理")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.PROXY_DELETE)
    public void deleteProxy(@RequestBody String proxyId) {
        proxyService.deleteProxy(proxyId);
    }

    @GetMapping("listAll")
    public List<ProxyDTO> listAllProxy() {
        return proxyService.selectProxys();
    }


}
