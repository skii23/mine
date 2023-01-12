package com.fit2cloud.devops.controller;

import com.fit2cloud.commons.server.base.domain.TagMapping;
import com.fit2cloud.commons.server.base.domain.TagValue;
import com.fit2cloud.commons.server.model.TagDTO;
import com.fit2cloud.commons.server.service.TagMappingService;
import com.fit2cloud.commons.server.service.TagService;
import io.swagger.annotations.Api;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("tag")
@Api
public class TagController {

    @Resource
    private TagService tagService;
    @Resource
    private TagMappingService tagMappingService;

    @RequestMapping("listAll")
    public List<TagDTO> selectAllTags(@RequestBody List<String> tagKeys) {
        return tagService.selectAllTags().stream().filter(tagDTO -> {
            for (String tagKey : tagKeys) {
                if (tagKey.equals(tagDTO.getTagKey())) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    @RequestMapping("mapping/save")
    public void saveTagMapping(@RequestBody List<TagMapping> tagMappings) throws Exception {
        List<TagMapping> deleteTagMapping = null;
        List<TagMapping> saveTagMapping = null;
        if (CollectionUtils.isNotEmpty(tagMappings)) {
            deleteTagMapping = tagMappings.stream().filter(tagMapping -> tagMapping.getResourceType().equalsIgnoreCase("DELETE"))
                    .collect(Collectors.toList());
            saveTagMapping = tagMappings.stream().filter(tagMapping -> !tagMapping.getResourceType().equalsIgnoreCase("DELETE"))
                    .collect(Collectors.toList());
        }
        tagMappingService.saveTagMappings(saveTagMapping);
        tagMappingService.deleteTagMappings(deleteTagMapping);
    }

    @GetMapping("getValues/{tagKey}")
    public List<TagValue> getTagValues(@PathVariable String tagKey) {
        Map params = new HashMap();
        params.put("tagKey", tagKey);
        return tagService.selectTagValues(params);
    }

    @RequestMapping(value = "mapping/list")
    public List<TagMapping> listTagMapping(@RequestBody Map<String, String> params) {
        return tagMappingService.selectTagMappings(params);
    }

}
