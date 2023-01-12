package com.fit2cloud.devops.controller.environment;

import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.Variable;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.request.VariableRequest;
import com.fit2cloud.devops.service.VariableService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("variable")
@Api(tags = "环境变量")
public class VariableController {

    @Resource
    private VariableService variableService;

    @PostMapping("list/{goPage}/{pageSize}")
    @ApiOperation("查看环境变量")
    @RequiresPermissions(PermissionConstants.VARIABLE_READ)
    public Pager getVariables(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody VariableRequest variableRequest) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, variableService.selectVariables(BeanUtils.objectToMap(variableRequest)));
    }

    @PostMapping("save")
    @ApiOperation("保存环境变量")
    @RequiresPermissions(PermissionConstants.VARIABLE_CREATE)
    public Variable saveVariable(@RequestBody Variable variable) {
        return  variableService.saveVariable(variable);
    }

    @PostMapping("update")
    @ApiOperation("修改环境变量")
    @RequiresPermissions(PermissionConstants.VARIABLE_UPDATE)
    public Variable updateVariable(@RequestBody Variable variable) {
        return variableService.saveVariable(variable);
    }

    @PostMapping("delete")
    @ApiOperation("删除环境变量")
    @RequiresPermissions(PermissionConstants.VARIABLE_DELETE)
    public void deleteVariable(@RequestBody String variableId) {
        variableService.deleteVariable(variableId);
    }
}
