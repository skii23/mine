package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.VariableDTO;

import java.util.List;
import java.util.Map;

public interface ExtVariableMapper {
    List<VariableDTO> selectVariables(Map<String, Object> params);
}
