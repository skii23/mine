package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.constants.RoleConstants;
import com.fit2cloud.commons.server.exception.F2CException;
import com.fit2cloud.commons.server.model.SessionUser;
import com.fit2cloud.commons.server.utils.SessionUtils;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ScriptImplementLog;
import com.fit2cloud.devops.base.domain.ScriptImplementLogExample;
import com.fit2cloud.devops.base.domain.ScriptImplementLogWithBLOBs;
import com.fit2cloud.devops.base.mapper.ScriptImplementLogMapper;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.dao.ext.ExtScriptImplementLogMapper;
import com.fit2cloud.devops.dto.ScriptImplementLogDto;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.fit2cloud.devops.common.consts.StatusConstants.*;

@Service
public class ScriptImplementLogService {

    @Resource
    private ExtScriptImplementLogMapper extScriptImplementLogMapper;
    @Resource
    private ScriptImplementLogMapper scriptImplementLogMapper;


    public List<ScriptImplementLogDto> selectScriptImplementLogs(Map<String, Object> params) {

        SessionUser user = SessionUtils.getUser();
        String roleId = user.getParentRoleId();
        if (roleId.equals(RoleConstants.Id.ORGADMIN.name())) {
            params.put("organizationId", user.getOrganizationId());
        } else if (roleId.equals(RoleConstants.Id.USER.name())) {
            params.put("workspaceId", user.getWorkspaceId());
        }
        return extScriptImplementLogMapper.selectImplementScriptLogs(params);
    }

    public void saveScriptImplementLog(ScriptImplementLogWithBLOBs scriptImplementLogWithBLOBs) {
        if (scriptImplementLogWithBLOBs.getId() != null) {
            scriptImplementLogMapper.updateByPrimaryKeySelective(scriptImplementLogWithBLOBs);
        } else {
            scriptImplementLogWithBLOBs.setId(UUIDUtil.newUUID());
            scriptImplementLogWithBLOBs.setCreatedTime(System.currentTimeMillis());
            scriptImplementLogMapper.insert(scriptImplementLogWithBLOBs);
        }
    }


    public ScriptImplementLogWithBLOBs getScriptImplementLogByTaskId(String taskId) {
        ScriptImplementLogExample scriptImplementLogExample = new ScriptImplementLogExample();
        scriptImplementLogExample.createCriteria().andAnsibleTaskIdEqualTo(taskId);
        List<ScriptImplementLogWithBLOBs> scriptImplementLogs = scriptImplementLogMapper.selectByExampleWithBLOBs(scriptImplementLogExample);
        if (CollectionUtils.isEmpty(scriptImplementLogs)) {
            F2CException.throwException("task is not exists!");
        }
        return scriptImplementLogs.get(0);
    }


    public List<ScriptImplementLogWithBLOBs> selectScriptImplementLogs(List<String> status) {
        ScriptImplementLogExample scriptImplementLogExample = new ScriptImplementLogExample();
        scriptImplementLogExample.createCriteria().andStatusIn(status);
        return scriptImplementLogMapper.selectByExampleWithBLOBs(scriptImplementLogExample);
    }

    public ScriptImplementLogWithBLOBs getLogById(String logId) {
        return scriptImplementLogMapper.selectByPrimaryKey(logId);
    }

    public void cleanScriptTask() {
        ScriptImplementLogExample scriptImplementLogExample = new ScriptImplementLogExample();
        scriptImplementLogExample.createCriteria()
                .andStatusNotIn(Arrays.asList(FAIL, SUCCESS, ERROR, TIMEOUT));
        List<ScriptImplementLogWithBLOBs> scriptImplementLogs = scriptImplementLogMapper.selectByExampleWithBLOBs(scriptImplementLogExample);
        scriptImplementLogs.forEach(scriptImplementLog -> {
            scriptImplementLog.setCompletedTime(System.currentTimeMillis());
            scriptImplementLog.setStatus(StatusConstants.FAIL);
            scriptImplementLog.setStdoutContent("系统异常中断！");
            scriptImplementLogMapper.updateByPrimaryKeyWithBLOBs(scriptImplementLog);
        });


    }

}
