package com.fit2cloud.devops.controller.script;

import com.alibaba.fastjson.JSON;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.Script;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.ScriptDTO;
import com.fit2cloud.devops.service.DevopsScriptService;
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
@Api(tags = "脚本库")
@RequestMapping("script")
public class ScriptController {

    @Resource
    private DevopsScriptService devopsScriptService;

    @ApiOperation("查看脚本")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SCRIPT_READ)
    public Pager getScripts(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map params) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, devopsScriptService.selectScripts(params));
    }

    @ApiOperation("创建脚本")
    @PostMapping("save")
    @RequiresPermissions(PermissionConstants.SCRIPT_CREATE)
    public Script saveScript(@RequestBody Script script) {
        return devopsScriptService.saveScript(script);
    }

    @ApiOperation("修改脚本")
    @PostMapping("update")
    @RequiresPermissions(PermissionConstants.SCRIPT_UPDATE)
    public Script updateScript(@RequestBody String body) {
        Script script = JSON.parseObject(body, Script.class);
        return devopsScriptService.saveScript(script);
    }

    @ApiOperation("删除脚本")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.SCRIPT_DELETE)
    public void deleteScript(@RequestBody String scriptId) {
        devopsScriptService.deleteScripts(scriptId);
    }


    @PostMapping("list")
    public List<ScriptDTO> getScripts() {
        return devopsScriptService.selectScripts(new HashMap<>());
    }


    @GetMapping("os/list")
    public List<Map> getPlatforms() {
        return devopsScriptService.getOsList();
    }


    @GetMapping("os/version/list")
    public List<String> getVersion(@RequestParam String os) {
        return devopsScriptService.getOsVersions(os);
    }


}
