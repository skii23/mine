package com.fit2cloud.devops.service.jenkins.model.multibranch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

/**
 * @author caiwzh
 * @date 2022/8/19
 */
@XStreamAlias("sources")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class BranchSourceList {

    @XStreamAlias("class")
    @XStreamAsAttribute
    private String clazz= "jenkins.branch.MultiBranchProject$BranchSourceList";

    @XStreamAsAttribute
    private String plugin = "branch-api@2.1046.v0ca_37783ecc5";

    private Owner owner;

    private Data data;

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    @XStreamAlias("data")
    public static class Data{

        private List<BranchSource> branchSource;

        public List<BranchSource> getBranchSource() {
            return branchSource;
        }

        public void setBranchSource(List<BranchSource> branchSource) {
            this.branchSource = branchSource;
        }
    }
}
