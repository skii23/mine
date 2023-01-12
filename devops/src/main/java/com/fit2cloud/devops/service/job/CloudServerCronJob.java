package com.fit2cloud.devops.service.job;

import com.fit2cloud.commons.utils.CommonThreadPool;
import com.fit2cloud.commons.utils.LogUtil;
import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.base.mapper.DevopsCloudServerMapper;
import com.fit2cloud.devops.dao.ext.ExtDevopsServerMapper;
import com.fit2cloud.devops.service.CredentialService;
import com.fit2cloud.quartz.anno.QuartzScheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CloudServerCronJob {

    @Resource(name = "connectCheckPool")
    private CommonThreadPool commonThreadPool;
    @Resource
    private CredentialService credentialService;
    @Resource
    private ExtDevopsServerMapper extDevopsServerMapper;
    @Resource
    private DevopsCloudServerMapper devopsCloudServerMapper;

    @QuartzScheduled(cron = "${server.connect.check.cron}")
    public void checkConnect() {
        commonThreadPool.addTask(() -> {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = dateTimeFormatter.format(LocalDateTime.now());
            LogUtil.info("--------开始检查虚拟机连通性--------");
            LogUtil.info("--------" + now + "--------");
//          筛选devops用的主机
            List<DevopsCloudServer> servers = extDevopsServerMapper.getConnectCheckList();
            LogUtil.info("-----待检查主机数量为：" + servers.size() + "-----");
            servers.forEach(server -> {
                boolean flag;
                try {
                    credentialService.findDevOpsCloudServerCredential(server.getId(), 2);
                    flag = true;
                } catch (Exception e) {
                    flag = false;
                }
//                只有未更新过和连通性结果与之前不同的才更新
                if (server.getConnectable() == null || flag != server.getConnectable()) {
                    server.setConnectable(flag);
                    devopsCloudServerMapper.updateByPrimaryKeySelective(server);
                }
            });
            now = dateTimeFormatter.format(LocalDateTime.now());
            LogUtil.info("--------" + now + "--------");
            LogUtil.info("--------虚拟机连通性检查结束--------");
        });
    }
}
