package com.fit2cloud.devops.service;

import com.fit2cloud.commons.utils.UUIDUtil;
import com.fit2cloud.devops.base.domain.ApplicationDeploySettings;
import com.fit2cloud.devops.base.domain.ApplicationDeploySettingsExample;
import com.fit2cloud.devops.base.mapper.ApplicationDeploySettingsMapper;
import com.fit2cloud.devops.common.consts.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wisonic
 */
@Service
public class ApplicationDeploySettingsService {

    public final static String EXPECTED_DAYS = "expected_days";

    @Resource
    private ApplicationDeploySettingsMapper applicationDeploySettingsMapper;

    public List<ApplicationDeploySettings> getWeekdaySettings() {
        ApplicationDeploySettingsExample applicationDeploySettingsExample = new ApplicationDeploySettingsExample();
        applicationDeploySettingsExample.createCriteria().andNameIn(ApplicationConstants.WEEKDAY_LIST);
        applicationDeploySettingsExample.setOrderByClause("order_num asc");
        return applicationDeploySettingsMapper.selectByExample(applicationDeploySettingsExample);
    }

    public List<ApplicationDeploySettings> getExpectedSettings() {
        ApplicationDeploySettingsExample applicationDeploySettingsExample = new ApplicationDeploySettingsExample();
        applicationDeploySettingsExample.createCriteria().andNameEqualTo(EXPECTED_DAYS);
        applicationDeploySettingsExample.setOrderByClause("date asc");
        return applicationDeploySettingsMapper.selectByExample(applicationDeploySettingsExample);
    }


    public void saveWeekdaySettings(List<ApplicationDeploySettings> applicationDeploySettings) {
        Optional.ofNullable(applicationDeploySettings).ifPresent(settings -> settings.forEach(setting ->{
            if (StringUtils.isNotBlank(setting.getId()) && ApplicationConstants.WEEKDAY_LIST.contains(setting.getName())) {
                applicationDeploySettingsMapper.updateByPrimaryKeySelective(setting);
            }
        }));
    }

    public void saveExpectedDaySettings(List<ApplicationDeploySettings> applicationDeploySettings) {
        ApplicationDeploySettingsExample applicationDeploySettingsExample = new ApplicationDeploySettingsExample();
        applicationDeploySettingsExample.createCriteria().andNameEqualTo(EXPECTED_DAYS);
        applicationDeploySettingsMapper.deleteByExample(applicationDeploySettingsExample);
        Optional.ofNullable(applicationDeploySettings).ifPresent(settings -> settings.forEach(setting -> {
            if (StringUtils.equals(setting.getName(), EXPECTED_DAYS)) {
                if (StringUtils.isBlank(setting.getId())) {
                    setting.setId(UUIDUtil.newUUID());
                }
                applicationDeploySettingsMapper.insertSelective(setting);
            }
        }));
    }
}
