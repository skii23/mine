/**
 * 启动app，加载菜单
 */

var ProjectApp = angular.module('ProjectApp', ['f2c.common','ngDraggable','ui.codemirror','ngSanitize']);
ProjectApp.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

ProjectApp.controller('IndexCtrl', function ($scope) {
    $scope.menus = [
        {
            name: "application_deployment_log",
            url: "/application/deployment/log",
            params: {
                applicationDeploymentId: null
            },
            templateUrl: "project/html/application/deploy/application-deployment-log-list.html" + '?_t=' + window.appversion
        },{
            name: "jenkins_job_history_list",
            url: "/jenkins/job/history/list",
            templateUrl: "project/html/jenkins/job/jenkins-job-history-list.html" + '?_t=' + Math.random()
        },{
            name: "create_freestyle_job",
            templateUrl: "project/html/jenkins/job/jenkins-job-add-freestyle.html" + '?_t=' + Math.random()
        },{
            name: "create_maven_job",
            templateUrl: "project/html/jenkins/job/jenkins-job-add-maven.html" + '?_t=' + Math.random()
        },{
            name: "create_flow_job",
            templateUrl: "project/html/jenkins/job/jenkins-job-add-flow.html" + '?_t=' + Math.random()
        },{
            name: "create_multibranch_job",
            templateUrl: "project/html/jenkins/job/jenkins-job-add-multibranch.html" + '?_t=' + Math.random()
        }
    ];
});

