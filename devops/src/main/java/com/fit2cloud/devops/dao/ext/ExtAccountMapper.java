package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.CloudAccountDTO;

import java.util.List;
import java.util.Map;

public interface ExtAccountMapper {

    List<CloudAccountDTO> getAccountList(Map<String, Object> param);
}
