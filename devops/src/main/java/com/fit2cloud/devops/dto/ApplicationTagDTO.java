package com.fit2cloud.devops.dto;

import com.fit2cloud.commons.server.base.domain.TagMapping;
import com.fit2cloud.devops.base.domain.Application;

import java.util.ArrayList;
import java.util.List;

public class ApplicationTagDTO extends Application {
    private List<TagMapping> tagMappings = new ArrayList<>();

    public List<TagMapping> getTagMappings() {
        return tagMappings;
    }

    public void setTagMappings(List<TagMapping> tagMappings) {
        this.tagMappings = tagMappings;
    }
}
