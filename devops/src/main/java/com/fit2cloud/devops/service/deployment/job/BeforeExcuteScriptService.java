package com.fit2cloud.devops.service.deployment.job;

import com.fit2cloud.commons.server.base.domain.CloudServer;
import com.fit2cloud.commons.utils.BeanUtils;
import com.fit2cloud.devops.base.domain.DevopsCloudServer;
import com.fit2cloud.devops.service.DevopsCloudServerCommonService;
import com.fit2cloud.devops.service.VariableService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class BeforeExcuteScriptService {

    @Resource
    private VariableService variableService;
    @Resource
    private DevopsCloudServerCommonService devopsCloudServerCommonService;

    public String generateExportScript(String cloudServerId) {
        String cmd = null;
        StringBuffer buffer = new StringBuffer();
        Map<String, String> params = variableService.getHostVariable(cloudServerId);
        DevopsCloudServer devopsCloudServer = devopsCloudServerCommonService.get(cloudServerId);
        CloudServer cloudServer = new CloudServer();
        BeanUtils.copyBean(devopsCloudServer, cloudServer);

        if (StringUtils.containsIgnoreCase(cloudServer.getOs(), "windows")) {
            if (params.size() > 0) {
                params.forEach((key, value) -> {
                    buffer.append("$env:").append(key);
                    buffer.append("=");
                    buffer.append("'").append(value).append("'");
                    buffer.append("\n");
                });
                cmd = buffer.toString();
            }
        } else {
            if (params.size() > 0) {
                params.forEach((key, value) -> {
                    buffer.append("export ");
                    buffer.append(key);
                    buffer.append("=");
                    buffer.append(value);
                    buffer.append("\n");
                });
                cmd = buffer.toString();
            }
        }
        return cmd;
    }

}
