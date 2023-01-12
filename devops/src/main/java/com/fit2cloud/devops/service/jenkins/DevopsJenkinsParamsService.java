package com.fit2cloud.devops.service.jenkins;

import com.alibaba.fastjson.JSONObject;
import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.DevopsJenkinsParams;
import com.fit2cloud.devops.base.domain.DevopsJenkinsParamsExample;
import com.fit2cloud.devops.base.mapper.DevopsJenkinsParamsMapper;
import com.fit2cloud.devops.service.jenkins.model.sysconfig.JenkinsSystemConfigParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DevopsJenkinsParamsService {

    @Resource
    private DevopsJenkinsParamsMapper devopsJenkinsParamsMapper;

    @Resource
    private DevopsJenkinsSystemConfigService devopsJenkinsSystemConfigService;

    private List<String> serverKeys = Stream.of(JenkinsSystemConfigParser.values()).map(JenkinsSystemConfigParser::getServerKey).collect(Collectors.toList());

    public List<DevopsJenkinsParams> getParams(DevopsJenkinsParams devopsJenkinsParams) {
        DevopsJenkinsParamsExample devopsJenkinsParamsExample = new DevopsJenkinsParamsExample();
        if (devopsJenkinsParams.getParamKey() != null) {
            devopsJenkinsParamsExample.createCriteria().andParamKeyLike(devopsJenkinsParams.getParamKey());
        }
        return devopsJenkinsParamsMapper.selectByExample(devopsJenkinsParamsExample);
    }

    public void saveParams(List<JSONObject> data) {
        for (JSONObject json : data) {
            if (json.containsKey("yaml")) {
                if (StringUtils.isNotBlank(json.getString("id"))) {
                    devopsJenkinsSystemConfigService.updateGitServer(JenkinsSystemConfigParser.parse(json), json.getString("id"));
                } else {
                    devopsJenkinsSystemConfigService.addGitServer(JenkinsSystemConfigParser.parse(json));
                }
            } else {
                DevopsJenkinsParams param = json.toJavaObject(DevopsJenkinsParams.class);
                if (param.getId() != null) {
                    updateParam(param);
                } else {
                    addParam(param);
                }
            }
        }
    }

    public void addParam(DevopsJenkinsParams param) {
        param.setId(UUIDUtil.newUUID());
        devopsJenkinsParamsMapper.insertSelective(param);
    }

    public void updateParam(DevopsJenkinsParams param) {
        devopsJenkinsParamsMapper.updateByPrimaryKeySelective(param);
    }

    public void deleteParams(List<DevopsJenkinsParams> params) {
        for (DevopsJenkinsParams param : params) {
            if (serverKeys.contains(param.getAlias())) {
                devopsJenkinsSystemConfigService.deleteGitServer(param.getId());
            } else {
                devopsJenkinsParamsMapper.deleteByPrimaryKey(param.getId());
            }
        }
    }

}
