package com.fit2cloud.devops.service.jenkins.model.common.properties.parameters.parameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XStreamAlias("hudson.model.ChoiceParameterDefinition")
public class ChoiceParameterDefinition extends BaseParameterDefinitionModel {


    // 简化层级并调整xml结构-与需求不符弃用
    // @XStreamImplicit
    // @XStreamImplicit(itemFieldName="string")
    private List<String> choices;

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }


    public ChoiceParameterDefinition(){
        super.type = "CHOICE_PARAMETER";
    }


//    public ChoiceParameterDefinition(String type, String name,String description) {
//        super.type = type;
//        super.name = name;
//        super.description = description;
//    }

    // 一定只有这种构造方法才能解析为XStream 的正确表达结构  String ...  =  String[]
    public ChoiceParameterDefinition(String type, String name, String description, String ... choices) {
        this.type = "CHOICE_PARAMETER";
        this.name = name;
        this.description = description;
        this.choices = Arrays.asList(choices);
    }

    @Override
    public ChoiceParameterDefinition clone(){
        ChoiceParameterDefinition target = new ChoiceParameterDefinition(this.type,this.name,this.description, this.choices.toArray(new String[this.choices.size()]));
        return target;
    }

    @Override
    public String toString() {
        return "ChoiceParameterDefinition{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
