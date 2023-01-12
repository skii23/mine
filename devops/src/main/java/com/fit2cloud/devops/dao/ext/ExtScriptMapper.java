package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ScriptDTO;

import java.util.List;
import java.util.Map;

public interface ExtScriptMapper {

    List<ScriptDTO> selectScripts(Map params);
}
