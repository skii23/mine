package com.fit2cloud.devops.dao.ext;

import com.fit2cloud.devops.dto.ScriptImplementLogDto;

import java.util.List;
import java.util.Map;

public interface ExtScriptImplementLogMapper {

    List<ScriptImplementLogDto> selectImplementScriptLogs(Map params);
}
