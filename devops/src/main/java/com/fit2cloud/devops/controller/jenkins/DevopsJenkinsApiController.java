package com.fit2cloud.devops.controller.jenkins;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.devops.service.jenkins.DevopsJenkinsSystemConfigService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author caiwzh
 * @date 2022/8/17
 */
@RestController
@Api(tags = "提供给Jenkins相关接口")
@RequestMapping("/anonymous/api")
public class DevopsJenkinsApiController {

    @Resource
    private DevopsJenkinsSystemConfigService devopsJenkinsSystemConfigService;

    /**
     * @param response
     * @throws Exception
     */
    @GetMapping("/casc/jenkins.yaml")
    public void getSystemConfig(HttpServletResponse response, String uuid) throws Exception {
        //if (StringUtils.equals(uuid, DevopsJenkinsSystemConfigService.YAML_UUID)) {
        //}
        JSONObject defaultConfig = devopsJenkinsSystemConfigService.getConfigYaml();
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml wyaml = new Yaml(dumperOptions);
        wyaml.dump(defaultConfig, response.getWriter());
    }
}
