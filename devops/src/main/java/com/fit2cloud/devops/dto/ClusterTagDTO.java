package com.fit2cloud.devops.dto;

import com.fit2cloud.commons.server.base.domain.TagMapping;
import com.fit2cloud.devops.base.domain.Cluster;

import java.util.List;

public class ClusterTagDTO extends Cluster {

    private List<TagMapping> tagMappings;

    public List<TagMapping> getTagMappings() {
        return tagMappings;
    }

    public void setTagMappings(List<TagMapping> tagMappings) {
        this.tagMappings = tagMappings;
    }
}
