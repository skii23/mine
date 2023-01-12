package com.fit2cloud.devops.listener;

import com.fit2cloud.devops.service.ApplicationDeploymentService;
import com.fit2cloud.devops.service.ScriptImplementLogService;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class DevopsListener implements ApplicationRunner {

    @Resource
    private ScriptImplementLogService scriptImplementLogService;
    @Resource
    private ApplicationDeploymentService applicationDeploymentService;
    @Resource
    private DevopsJenkinsService devopsJenkinsService;

    @Override
    public void run(ApplicationArguments args) {
        //1.处理未执行完成的脚本任务
        scriptImplementLogService.cleanScriptTask();
        //2.处理未完成的部署任务
        applicationDeploymentService.cleanApplicationDeployment();
//        3.重新添加自动同步
        devopsJenkinsService.reAddJenkinsAutoSyncJ();
    }
}
