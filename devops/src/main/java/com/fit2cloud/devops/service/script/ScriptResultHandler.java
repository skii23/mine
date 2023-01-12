package com.fit2cloud.devops.service.script;

import com.fit2cloud.ansible.AnsibleService;
import com.fit2cloud.ansible.model.TaskResult;
import com.fit2cloud.commons.server.redis.queue.AbstractQueue;
import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.base.domain.ScriptImplementLogWithBLOBs;
import com.fit2cloud.devops.common.consts.StatusConstants;
import com.fit2cloud.devops.service.ScriptImplementLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ScriptResultHandler extends AbstractQueue<String> {

    @Resource
    private AnsibleService ansibleService;
    @Resource
    private ScriptImplementLogService scriptImplementLogService;

    public ScriptResultHandler() {
        super("script-result", 5);
    }

    @Override
    public void handleMessage(String taskId) {
        LogUtil.info("receive message script-result:" + taskId);
        //收到消息启动线程同步状态
        new Thread(() -> {
            try {
                TaskResult taskResult = ansibleService.getTaskResult(taskId);
                ScriptImplementLogWithBLOBs scriptImplementLog = scriptImplementLogService.getScriptImplementLogByTaskId(taskId);
                scriptImplementLog.setCompletedTime(System.currentTimeMillis());
                scriptImplementLog.setStdoutContent(getOutText(taskResult));

                if (taskResult.getResult().isSuccess()) {
                    scriptImplementLog.setStatus(StatusConstants.SUCCESS);
                } else {
                    scriptImplementLog.setStatus(StatusConstants.FAIL);
                }
                scriptImplementLogService.saveScriptImplementLog(scriptImplementLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private String getOutText(TaskResult taskResult) {
        String stdout = ansibleService.getStdOut(taskResult);
        String msg = ansibleService.getMsg(taskResult);
        StringBuilder buffer = new StringBuilder();
        buffer.append("\n");
        if (StringUtils.isNotBlank(stdout)) {
            buffer.append(stdout);
        }
        buffer.append("\n");
        if (StringUtils.isNotBlank(msg)) {
            buffer.append(msg);
        }
        buffer.append("\n");

        return buffer.toString();
    }
}
