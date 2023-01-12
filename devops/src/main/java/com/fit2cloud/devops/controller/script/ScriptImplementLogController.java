package com.fit2cloud.devops.controller.script;

import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.commons.utils.PageUtils;
import com.fit2cloud.commons.utils.Pager;
import com.fit2cloud.devops.base.domain.ScriptImplementLogWithBLOBs;
import com.fit2cloud.devops.common.PermissionConstants;
import com.fit2cloud.devops.dto.request.ScriptImplementLogRequest;
import com.fit2cloud.devops.service.ScriptImplementLogService;
import com.github.pagehelper.PageHelper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("script/implement/log")
public class ScriptImplementLogController {

    @Resource
    private ScriptImplementLogService scriptImplementLogService;


    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SCRIPT_IMPLEMENT_LOG_READ)
    public Pager getScriptImplementLogs(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ScriptImplementLogRequest scriptImplementLogRequest) {
        com.github.pagehelper.Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, scriptImplementLogService.selectScriptImplementLogs(BeanUtils.objectToMap(scriptImplementLogRequest)));
    }

    @GetMapping("detail/{logId}")
    public ScriptImplementLogWithBLOBs getDetail(@PathVariable String logId) {
        return scriptImplementLogService.getLogById(logId);
    }
}
