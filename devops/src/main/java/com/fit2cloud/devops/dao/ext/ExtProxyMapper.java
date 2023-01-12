package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ProxyDTO;

import java.util.List;
import java.util.Map;

public interface ExtProxyMapper {

    List<ProxyDTO> selectProxys(Map<String, Object> params);
}
