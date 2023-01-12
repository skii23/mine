package com.fit2cloud.devops.common.consts;

import java.util.ArrayList;
import java.util.List;

public class CodeDeploymentSteps {

    public static final String APPLICATION_STOP = "ApplicationStop";
    public static final String INI_ENVIRONMENT = "InitEnvironment";
    public static final String BEFORE_INSTALL = "BeforeInstall";
    public static final String INSTALL = "Install";
    public static final String AFTER_INSTALL = "AfterInstall";
    public static final String APPLICATION_START = "ApplicationStart";
    public static final String VALIDATE_SERVICE = "ValidateService";


    public static List<String> supportEventTypes = new ArrayList<String>();

    static {
        supportEventTypes.add(INI_ENVIRONMENT);
        supportEventTypes.add(APPLICATION_STOP);
        supportEventTypes.add(BEFORE_INSTALL);
        supportEventTypes.add(INSTALL);
        supportEventTypes.add(AFTER_INSTALL);
        supportEventTypes.add(APPLICATION_START);
        supportEventTypes.add(VALIDATE_SERVICE);
    }

}
