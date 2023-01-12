package com.fit2cloud.devops.common.consts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ERepositoryType {
    NEXUS("Nexus"), NEXUS3("Nexus3"), OSS("阿里云OSS"), S3("亚马逊S3"), ARTIFACTORY("Artifactory"),HARBOR("Harbor");

    private String value;

    ERepositoryType(String value) {
        this.value = value;
    }

    public static List<String> listAllValue() {
        List<String> values = new ArrayList<>();
        ERepositoryType[] eRepositoryTypes = ERepositoryType.values();
        Arrays.stream(eRepositoryTypes).forEach(eRepositoryType -> {
            values.add(eRepositoryType.getValue());
        });
        return values;
    }

    public static ERepositoryType fromValue(String value) {
        ERepositoryType eRepositoryType = null;
        for (ERepositoryType eRepositoryType1 : ERepositoryType.values()) {
            if (eRepositoryType1.getValue().equals(value)) {
                eRepositoryType = eRepositoryType1;
            }
        }
        return eRepositoryType;
    }

    public String getValue() {
        return this.value;
    }
}
