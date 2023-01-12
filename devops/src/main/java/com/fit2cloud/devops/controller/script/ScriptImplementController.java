package com.fit2cloud.devops.controller.script;

import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.common.model.ScriptJob;
import com.fit2cloud.devops.service.DevopsScriptService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("script/implement")
public class ScriptImplementController {

    @Resource
    private DevopsScriptService devopsScriptService;

    @PostMapping("create")
    @RequiresPermissions(PermissionConstants.SCRIPT_IMPLEMENT)
    public void implementScript(@RequestBody ScriptJob scriptJob) {
        devopsScriptService.implementScript(scriptJob);
    }
}
