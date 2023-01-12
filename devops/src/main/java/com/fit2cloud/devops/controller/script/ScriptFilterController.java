package com.fit2cloud.devops.controller.script;

import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.DevopsScriptFilter;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.service.DevopsScriptFilterService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author WiSoniC
 */
@RequestMapping("script/filter")
@RestController
@Api(tags = "脚本过滤")
public class ScriptFilterController {

    @Resource
    private DevopsScriptFilterService devopsScriptFilterService;

    @ApiOperation("查看脚本过滤规则")
    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SCRIPT_FILTER_READ)
    public Pager listJenkins(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Map<String, Object> params) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page,devopsScriptFilterService.getScriptFilters(params));
    }

    @ApiOperation("保存脚本过滤规则")
    @PostMapping("save")
    @RequiresPermissions(value = {PermissionConstants.SCRIPT_FILTER_UPDATE, PermissionConstants.SCRIPT_FILTER_CREATE},logical = Logical.OR)
    public void saveScriptFilter(@RequestBody(required = false) DevopsScriptFilter devopsScriptFilter) {
        devopsScriptFilterService.save(devopsScriptFilter);
    }

    @ApiOperation("删除脚本过滤规则")
    @PostMapping("delete")
    @RequiresPermissions(PermissionConstants.SCRIPT_FILTER_DELETE)
    public void deleteScriptFilters(@RequestBody(required = false)List<DevopsScriptFilter> devopsScriptFilters) {
        devopsScriptFilterService.deleteScriptFilter(devopsScriptFilters);
    }


}
