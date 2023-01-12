package com.fit2cloud.devops.service;

import com.fit2cloud.commons.server.base.domain.Dictionary;
import com.fit2cloud.commons.server.base.domain.DictionaryExample;
import com.fit2cloud.commons.server.base.mapper.DictionaryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictionaryService {

    @Resource
    private DictionaryMapper dictionaryMapper;

    public List<Map> getOsList() {
        DictionaryExample dictionaryExample = new DictionaryExample();
        dictionaryExample.createCriteria().andCategoryEqualTo("vm_os");
        return dictionaryMapper.selectByExample(dictionaryExample).stream().map(this::genMap).collect(Collectors.toList());
    }

    public List<String> getOsVersions(String os) {
        DictionaryExample dictionaryExample = new DictionaryExample();
        dictionaryExample.createCriteria().andCategoryEqualTo("vm_os_version").andDictionaryKeyEqualTo(os);
        dictionaryExample.setOrderByClause("dictionary_value");
        return dictionaryMapper.selectByExample(dictionaryExample).stream().map(Dictionary::getDictionaryValue).collect(Collectors.toList());
    }




    private Map genMap(Dictionary dictionary) {
        Map map = new HashMap();
        map.put("key", dictionary.getDictionaryKey());
        map.put("value", dictionary.getDictionaryValue());
        return map;
    }


}
