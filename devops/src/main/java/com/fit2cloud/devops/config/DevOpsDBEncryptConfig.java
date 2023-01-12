package com.fit2cloud.devops.config;

import com.fit2cloud.commons.utils.EncryptConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yankaijun
 * @date 2019-07-29 21:07
 */
@Component
public class DevOpsDBEncryptConfig implements com.fit2cloud.commons.server.config.DBEncryptConfig {
    @Override
    public List<EncryptConfig> encryptConfig() {
        List<EncryptConfig> list = new ArrayList() {{

            add((new EncryptConfig("com.fit2cloud.devops.dto.ServerDTO", "password")));
        }};
        return list;
    }
}
