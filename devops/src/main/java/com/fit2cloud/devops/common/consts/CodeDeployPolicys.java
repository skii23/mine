package com.fit2cloud.devops.common.consts;

import java.util.ArrayList;
import java.util.List;

public class CodeDeployPolicys {

    public static final String ALL = "all";
    public static final String HALF = "half";
    public static final String SINGLE = "single";
    public static List<String> list = new ArrayList<>();

    static {
        list.add(ALL);
        list.add(HALF);
        list.add(SINGLE);
    }

    public static List<String> getAll() {
        return list;
    }
}
