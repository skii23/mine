package com.fit2cloud.devops.common;

public class PermissionConstants {


    //start cluster
    public static final String CLUSTER_READ = "CLUSTER:READ";
    public static final String CLUSTER_CREATE = "CLUSTER:READ+CREATE";
    public static final String CLUSTER_UPDATE = "CLUSTER:READ+UPDATE";
    public static final String CLUSTER_DELETE = "CLUSTER:READ+DELETE";
    //end cluster

    //start cluster_role
    public static final String CLUSTER_ROLE_READ = "CLUSTER_ROLE:READ";
    public static final String CLUSTER_ROLE_CREATE = "CLUSTER_ROLE:READ+CREATE";
    public static final String CLUSTER_ROLE_UPDATE = "CLUSTER_ROLE:READ+UPDATE";
    public static final String CLUSTER_ROLE_DELETE = "CLUSTER_ROLE:READ+DELETE";
    //end cluster_role


    //start cluster_role
    public static final String VARIABLE_READ = "VARIABLE:READ";
    public static final String VARIABLE_CREATE = "VARIABLE:READ+CREATE";
    public static final String VARIABLE_UPDATE = "VARIABLE:READ+UPDATE";
    public static final String VARIABLE_DELETE = "VARIABLE:READ+DELETE";
    //end cluster_role


    //start server
    public static final String CLOUD_SERVER_READ = "CLOUD_SERVER:READ";
    public static final String CLOUD_SERVER_GROUP = "CLOUD_SERVER:READ+GROUP";
    public static final String CLOUD_SERVER_PROXY = "CLOUD_SERVER:READ+PROXY";

    //end server

    //start application
    public static final String APPLICATION_READ = "APPLICATION:READ";
    public static final String APPLICATION_CREATE = "APPLICATION:READ+CREATE";
    public static final String APPLICATION_UPDATE = "APPLICATION:READ+UPDATE";
    public static final String APPLICATION_DELETE = "APPLICATION:READ+DELETE";
    public static final String APPLICATION_SETTING = "APPLICATION:READ+SETTING";


    //end application

    //start application_version
    public static final String APPLICATION_VERSION_READ = "APPLICATION_VERSION:READ";
    public static final String APPLICATION_VERSION_CREATE = "APPLICATION_VERSION:READ+CREATE";
    public static final String APPLICATION_VERSION_DELETE = "APPLICATION_VERSION:READ+DELETE";
    //end application_version

    //start application_deploy
    public static final String APPLICATION_DEPLOY_READ = "APPLICATION_DEPLOY:READ";
    public static final String APPLICATION_DEPLOY_CREATE = "APPLICATION_DEPLOY:READ+CREATE";
    //end application_deploy


    //start application_deploy_analysis
    public static final String APPLICATION_DEPLOY_ANALYSIS_READ = "APPLICATION_DEPLOY_ANALYSIS:READ";
    //end application_deploy_analysis


    //start application_repository
    public static final String APPLICATION_REPOSITORY_READ = "APPLICATION_REPOSITORY:READ";
    public static final String APPLICATION_REPOSITORY_CREATE = "APPLICATION_REPOSITORY:READ+CREATE";
    public static final String APPLICATION_REPOSITORY_UPDATE = "APPLICATION_REPOSITORY:READ+UPDATE";
    public static final String APPLICATION_REPOSITORY_DELETE = "APPLICATION_REPOSITORY:READ+DELETE";
    //end application_repository

    //start
    public static final String PROXY_READ = "PROXY:READ";
    public static final String PROXY_CREATE = "PROXY:READ+CREATE";
    public static final String PROXY_UPDATE = "PROXY:READ+UPDATE";
    public static final String PROXY_DELETE = "PROXY:READ+DELETE";
    //end

    //start script
    public static final String SCRIPT_READ = "SCRIPT:READ";
    public static final String SCRIPT_CREATE = "SCRIPT:READ+CREATE";
    public static final String SCRIPT_UPDATE = "SCRIPT:READ+UPDATE";
    public static final String SCRIPT_DELETE = "SCRIPT:READ+DELETE";
    public static final String SCRIPT_IMPLEMENT = "SCRIPT:READ+IMPLEMENT";
    public static final String SCRIPT_IMPLEMENT_LOG_READ = "SCRIPT_IMPLEMENT_LOG:READ";
    //end script

    public static final String DASHBOARD_READ = "DASHBOARD:READ";
    public static final String DASHBOARD_GROUP_ANALYSIS_READ = "DASHBOARD_GROUP_ANALYSIS:READ";

    //start jenkins
    public static final String JENKINS_READ = "JENKINS:READ";
    public static final String JENKINS_CREATE = "JENKINS:READ+CREATE";
    public static final String JENKINS_UPDATE = "JENKINS:READ+UPDATE";
    public static final String JENKINS_DELETE = "JENKINS:READ+DELETE";
    //end jenkins
    //start jenkins_job
    public static final String JENKINS_JOB_READ = "JENKINS_JOB:READ";
    public static final String JENKINS_JOB_CREATE = "JENKINS_JOB:READ+CREATE";
    public static final String JENKINS_JOB_UPDATE = "JENKINS_JOB:READ+UPDATE";
    public static final String JENKINS_JOB_DELETE = "JENKINS_JOB:READ+DELETE";
    public static final String JENKINS_JOB_GRANT = "JENKINS_JOB:READ+GRANT";
    public static final String JENKINS_JOB_BUILD = "JENKINS_JOB:READ+BUILD";
    //end jenkins_job
    //start jenkins_job_history
    public static final String JENKINS_JOB_HISTORY_READ = "JENKINS_JOB_HISTORY:READ";
    public static final String JENKINS_JOB_HISTORY_UPDATE = "JENKINS_JOB_HISTORY:READ+UPDATE";
    public static final String JENKINS_JOB_HISTORY_DELETE = "JENKINS_JOB_HISTORY:READ+DELETE";
    //end jenkins_job_history

//    start jenkins_params
    public static final String JENKINS_PARAMS_READ = "JENKINS_PARAMS:READ";
    public static final String JENKINS_PARAMS_CREATE = "JENKINS_PARAMS:READ+CREATE";
    public static final String JENKINS_PARAMS_UPDATE = "JENKINS_PARAMS:READ+UPDATE";
    public static final String JENKINS_PARAMS_DELETE = "JENKINS_PARAMS:READ+DELETE";
//    end jenkins_params

//        start jenkins_credential
    public static final String JENKINS_CREDENTIAL_READ = "JENKINS_CREDENTIAL:READ";
    public static final String JENKINS_CREDENTIAL_UPDATE = "JENKINS_CREDENTIAL:READ+UPDATE";
    public static final String JENKINS_CREDENTIAL_CREATE = "JENKINS_CREDENTIAL:READ+CREATE";
    public static final String JENKINS_CREDENTIAL_DELETE = "JENKINS_CREDENTIAL:READ+DELETE";
    public static final String JENKINS_CREDENTIAL_GRANT = "JENKINS_CREDENTIAL:READ+GRANT";
    public static final String JENKINS_CREDENTIAL_SYNC = "JENKINS_CREDENTIAL:READ+SYNC";
//   end jenkins_credential

//    start jenkins view
    public static final String JENKINS_VIEW_READ = "JENKINS_VIEW:READ";
    public static final String JENKINS_VIEW_CREATE = "JENKINS_VIEW:READ+CREATE";
    public static final String JENKINS_VIEW_DELETE = "JENKINS_VIEW:READ+DELETE";
    public static final String JENKINS_VIEW_UPDATE = "JENKINS_VIEW:READ+UPDATE";
//    end jenkins view

    //    start application deploy settings
    public static final String APPLICATION_DEPLOY_SETTINGS_READ = "APPLICATION_DEPLOY_SETTINGS:READ";
    public static final String APPLICATION_DEPLOY_SETTINGS_UPDATE = "APPLICATION_DEPLOY_SETTINGS:READ+UPDATE";
    public static final String APPLICATION_DEPLOY_SETTINGS_CREATE = "APPLICATION_DEPLOY_SETTINGS:READ+CREATE";
    public static final String APPLICATION_DEPLOY_SETTINGS_DELETE = "APPLICATION_DEPLOY_SETTINGS:READ+DELETE";
    //    end application DEPLOY SETTINGS
//    start structure
    public static final String STRUCTURE_ORGS_READ = "STRUCTURE_ORGS:READ";
    public static final String STRUCTURE_WORKSPACES_READ = "STRUCTURE_WORKSPACES:READ";
    //    end structure
//    start script filter
    public static final String SCRIPT_FILTER_READ = "SCRIPT_FILTER:READ";
    public static final String SCRIPT_FILTER_CREATE = "SCRIPT_FILTER:READ+CREATE";
    public static final String SCRIPT_FILTER_UPDATE = "SCRIPT_FILTER:READ+UPDATE";
    public static final String SCRIPT_FILTER_DELETE = "SCRIPT_FILTER:READ+DELETE";
//    end script filter
}
