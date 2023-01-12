ProjectApp.controller('JenkinsJobCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state, $interval,$timeout, UserService) {

    //重置一下应用部署页面的url，避免加载
    delete $scope.deployUrl;
    $scope.jobViewId = sessionStorage.getItem('jobViewId');
    sessionStorage.removeItem('jobViewId');

    // 定义搜索条件
    $scope.conditions = [
        {key: "name", name: "名称", directive: "filter-contains"},
        {key: "url", name: "url", directive: "filter-contains"},
        {key: "source", name: "来源", directive: "filter-contains"},
    ];

    // 用于传入后台的参数
    $scope.filters = [];
    //初始化查询条件,根据页面跳转方向判断是否保留查询条件
    var storage = angular.fromJson(sessionStorage.getItem("condition"));
    $scope.sortslq =null;
    if(storage !=null && sessionStorage.getItem('url')=='/jenkins-job') {
        sessionStorage.removeItem("condition");
        //设置排序
        if(storage.sort){
            $scope.sortslq = storage.sort;
        }
        //设置分页条件
        if(storage.page) {
            $scope.pagination = angular.extend({
                page: 1,
                limit: 10,
                limitOptions: [10, 20, 50, 100]
            }, $scope.pagination);
            $scope.pagination.page = storage.page;
            $scope.pagination.limit = storage.limit;
        }
        //设置查询条件
        if (storage.name) {
            $scope.filters.push({
                key: "name",
                name: "名称",
                value: storage.name,
                label: storage.name.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.url) {
            $scope.filters.push({
                key: "url",
                name: "url",
                value: storage.url,
                label: storage.url.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.source) {
            $scope.filters.push({
                key: "source",
                name: "来源",
                value: storage.source,
                label: storage.source.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.organizationId) {
            $scope.filters.push({
                key: "organizationId",
                name: "组织",
                value: storage.organizationId,
                label: storage.organizationId.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.workspaceId) {
            $scope.filters.push({
                key: "workspaceId",
                name: "工作空间",
                value: storage.workspaceId,
                label: storage.workspaceId.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
    }
    // 全选按钮，添加到$scope.columns
    $scope.first = {
        default: true,
        sort: false,
        type: "checkbox",
        checkValue: false,
        change: function (checked) {
            $scope.items.forEach(function (item) {
                item.enable = checked;
            });
        },
        width: "40px"
    };

    $scope.columns = [
        $scope.first,
        {value: "名称", key: "name", sort: false},
        {value: "子任务数", key: "child_num", sort: false},
        {value: "来源", key: "source", sort: false, checked: false},
        {value: "构建历史", key: "build_size"},
        {value: "Jenkins", key: "url", sort: false},
        {value: "同步状态", key: "sync_status", sort: false},
        {value: "构建状态", key: "build_status", sort: false},
        {value: "是否可构建", key: "buildable", sort: false},
        {value: "创建时间", key: "create_time", sort: true, checked: false},
        {value: "修改时间", key: "update_time", sort: true, checked: false},
        {value: "同步时间", key: "sync_time"},
        {value: "描述", key: "description", checked: false}
    ];

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
            $scope.sortslq = $scope.sort.sql;
        }else if($scope.sortslq){
            condition.sort = $scope.sortslq;
        }

        condition.viewId = $scope.jobView ? $scope.jobView.id : null;
        HttpUtils.paging($scope, 'jenkins/job/list', condition, function () {

            $scope.items.forEach(function (item) {
                if (item.active === 'true') {
                    item.active = true;
                }
                if (item.active === 'false') {
                    item.active = false;
                }

            });
        });
        //存储查询条件
        condition.page = $scope.pagination.page;
        condition.limit = $scope.pagination.limit;
        sessionStorage.setItem("condition",JSON.stringify(condition));
    };

    if (UserService.isAdmin()) {
        $scope.columns.splice(5, 0, {value: "组织", key: "organizationName", width: "15%", sort: false});
        $scope.columns.splice(6, 0, {value: "工作空间", key: "workspaceName", width: "15%", sort: false});

        $scope.conditions.push({
            key: "organizationId",
            name: "组织",
            directive: "filter-select-virtual",
            url: "condition/organization",
            convert: {value: "id", label: "name"}
        }, {
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: {value: "id", label: "name"}
        });
    }

    if (UserService.isOrgAdmin()) {
        $scope.columns.splice(5, 0, {value: "工作空间", key: "workspaceName", width: "15%", sort: false});
        $scope.conditions.push({
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: {value: "id", label: "name"}
        })
    }

    $scope.freshViews = function (viewId) {
        $scope.loadingLayer = HttpUtils.post('jenkins/view/getViews', {}, function (data) {
            $scope.jobViews = [{name: "所有任务", id: "all"}].concat(data.data);
            if (viewId) {
                $scope.jobViews.forEach(function (item) {
                    if (item.id === viewId) {
                        item.isActive = true;
                        $scope.jobView = item;
                        $scope.list();
                    }
                });
            }
        });
    };

    $scope.createJobView = function () {
        $scope.item = {};
        $scope.formUrl = "project/html/jenkins/job/view/jenkins-job-view-add.html" + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.editJobView = function () {
        $scope.item = angular.copy($scope.jobView);
        $scope.formUrl = "project/html/jenkins/job/view/jenkins-job-view-add.html" + '?_t=' + Math.random();
        $scope.isEdit = true;
        $scope.toggleForm();
    };

    $scope.changeView = function (item) {
        $scope.jobView = item;
        $scope.list();
    };

    $scope.deleteView = function () {
        Notification.confirm("确定删除视图 " + $scope.jobView.name + " ?", function () {
            $scope.loadingLayer = HttpUtils.post('jenkins/view/delete', [$scope.jobView], function (data) {
                $scope.jobViews.splice($scope.tabIndex, 1);
                $scope.jobView = $scope.jobViews[$scope.tabIndex - 1];
                $scope.jobView.isActive = true;
                Notification.info("删除成功！");
                $scope.list();
            });
        });
    };

    $scope.addJobsToViews = function (jobItem) {
        $scope.jobs = [];
        if (jobItem) {
            $scope.jobs.push(jobItem);
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    $scope.jobs.push(item)
                }
            });
        }
        if ($scope.jobs.length !== 0) {
            $scope.formUrl = "project/html/jenkins/job/view/devops-add-jobs-to-views.html?_t=" + Math.random();
            $scope.toggleForm();
        } else {
            Notification.warn('请选择要添加的任务！');
        }
    };

    $scope.showWorkspace = function (job) {
        if ($scope.selecteed === job) {
            $scope.selecteed = null;
            $scope.toggleInfoForm();
        } else {
            $scope.selecteed = job;
            $scope.jobName = job.name;
            $scope.loadingLayer = HttpUtils.post('jenkins/job/getJobWorkspace', {
                'jobName': job.name,
                'fileNode': {'path': '/'}
            }, function (data) {
                $scope.fileNodes = data.data;
                $scope.navs = [];
                $scope.infoUrl = "project/html/jenkins/job/jenkins-job-workspace.html?_t=" + Math.random();
                $scope.toggleInfoForm(true);
            });
        }
    };

    $scope.goChild = function (item) {
        if (!item.childNum) {
            return;
        }
        $scope.parentId = item.id;
        $mdDialog.show({
            templateUrl: 'project/html/jenkins/job/jenkins-child-job-list.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false,
            fixedGutter: false,
            bindToController: true
        });
    };

    $scope.closeForm = function () {
        $scope.toggleInfoForm();
    };

    $scope.init = function () {
        if ($scope.jobViewId) {
            $scope.freshViews($scope.jobViewId);
        } else {
            $scope.freshViews();
        }
        if (sessionStorage.getItem('buildName')) {
            $scope.$evalAsync(
                function ($scope) {
                    $scope.filters = [{ key: 'name', name: "名称", value: sessionStorage.getItem('buildName') }];
                    sessionStorage.removeItem('buildName');
                    $scope.list();
                }
            );
        } else {
            $scope.list();
        }
    };

    $scope.init();

    const defaultJob = {
        blockTriggerWhenBuilding: true,
        runPostStepsIfResult: {name: 'FAILURE'},
        disabled: false,
        jdk: '(System)',
        mavenValidationLevel: '-1',
        scm: {type: 'SCM_NULL', classStr: 'hudson.scm.NullSCM'},
        settings: {classStr: 'jenkins.mvn.DefaultSettingsProvider'},
        globalSettings: {classStr: 'jenkins.mvn.DefaultGlobalSettingsProvider'},
        aggregatorStyleBuild: true,
        canRoam: true
    };

    $scope.createJob = function (jobType) {
        let item = angular.copy(defaultJob);
        item.type = jobType;
        sessionStorage.setItem("item", angular.toJson(angular.copy(item)));
        sessionStorage.setItem("isEdit", 'false');
        sessionStorage.setItem('jobId', item.id);
        if ($scope.jobView) {
            sessionStorage.setItem("jobViewId", $scope.jobView.id);
        }
        switch (jobType) {
            case 'FREE_STYLE': {
                $state.go('create_freestyle_job');
                break;
            }
            case 'MAVEN': {
                $state.go('create_maven_job');
                break;
            }
            case 'FLOW': {
                $state.go('create_flow_job');
                break;
            }
            case 'WORKFLOW_MULTI_BRANCH': {
                $state.go('create_multibranch_job');
                break;
            }
            default: {
            }
        }
    };

    $scope.copyJob = function () {
        $mdDialog.show({
            templateUrl: 'project/html/jenkins/job/jenkins-job-copy.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false,
            fixedGutter: false,
            bindToController: true
        });
    };

    // 编辑构建任务配置
    $scope.edit = function (item, isCopy) {
        let isEdit = !isCopy;
        if (item.type === 'UNKNOWN') {
            Notification.warn('暂不支持该类型的构建任务！');
        } else {
            HttpUtils.get('jenkins/job/' + item.id, function (data) {
                if (data.success) {
                    let jobItem = data.data;
                    jobItem.type = item.type;
                    if (isCopy) {
                        jobItem.name = item.name;
                        jobItem.id = null;
                    }
                    console.info(jobItem);
                    sessionStorage.setItem("item", angular.toJson(jobItem));

                    // console.warn('edit================>',jobItem)
                    sessionStorage.setItem('webhookUrl', item.url.replace('/job/','/project/'));

                    // console.warn('================>',angular.toJson(jobItem))

                    sessionStorage.setItem("isEdit", isEdit.toString());
                    sessionStorage.setItem("simpleJobItem", angular.toJson(item));
                    sessionStorage.setItem('jobId', item.id);
                    if ($scope.jobView) {
                        sessionStorage.setItem("jobViewId", $scope.jobView.id);
                    }
                    switch (item.type) {
                        case 'FREE_STYLE': {
                            $state.go('create_freestyle_job');
                            break;
                        }
                        case 'MAVEN': {
                            $state.go('create_maven_job');
                            break;
                        }
                        case 'FLOW': {
                            $state.go('create_flow_job');
                            break;
                        }
                        case 'WORKFLOW_MULTI_BRANCH': {
                            $state.go('create_multibranch_job');
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } else {
                    Notification.warn(data.message);
                }
            });
        }
    };

    $scope.updateStatus = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        } else if($scope.sortslq){
             condition.sort = $scope.sortslq;
         }
        condition.viewId = $scope.jobView ? $scope.jobView.id : null;
        HttpUtils.post('jenkins/job/list/' + $scope.pagination.page + "/" + $scope.pagination.limit, condition, function (response) {
            let items = response.data.listObject;
            items.forEach(function (remoteItem) {
                $scope.items.forEach(function (localItem) {
                    if (localItem.id === remoteItem.id) {
                        localItem.syncTime = remoteItem.syncTime;
                        localItem.buildStatus = remoteItem.buildStatus;
                        localItem.syncStatus = remoteItem.syncStatus;
                        localItem.updateTime = remoteItem.updateTime;
                        localItem.buildable = remoteItem.buildable;
                        localItem.buildSize = remoteItem.buildSize;
                        localItem.childNum = remoteItem.childNum;
                    }
                })
            });
        });
        //存储查询条件
        condition.page = $scope.pagination.page;
        condition.limit = $scope.pagination.limit;
        sessionStorage.setItem("condition",JSON.stringify(condition));
    };
    $scope.showDetail = function (item) {
        $scope.title = '构建日志';
        $mdDialog.show({
            templateUrl: 'project/html/jenkins/job/output-text-detail.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false,
            fixedGutter: false
        });

        $scope.detailLoding = HttpUtils.post("jenkins/job/history/output", item.id, function (data) {
            let result = data.data;
            $timeout(function () {
                $scope.cmOption = {
                    lineNumbers: true,
                    indentWithTabs: true,
                    theme: 'bespin',
                    readOnly: true
                };
            }, 1000).then(function () {
                $scope.detail = result;
            });
            $timeout.cancel();
        });

        $scope.updateOutput = function () {
            HttpUtils.post("jenkins/job/history/output", item.id, function (data) {
                $scope.detail = data.data;
                if (item.buildStatus !== 'BUILDING' && item.buildStatus !== 'REBUILDING') {
                    $interval.cancel($scope.outputTimer);
                }
            });
        }
        if (item.buildStatus === 'BUILDING' || item.buildStatus === 'REBUILDING') {
            $scope.outputTimer = $interval($scope.updateOutput, 5000);
        }
    };

    //添加定时刷新页面方法
    $scope.timer = $interval($scope.updateStatus, 5000);

    //离开当前页面取消定时器
    $scope.$on('$destroy', function () {
        if ($scope.timer) {
            $interval.cancel($scope.timer);
        }
    });

    /**
     * 删除JOB
     * @param item
     */
    $scope.deleteJobs = function (item) {
        let deleteJobs = [];
        if (item) {
            deleteJobs.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    deleteJobs.push(item)
                }
            });
        }
        if (deleteJobs.length !== 0) {
            Notification.confirm("确定删除已选构建任务？", function () {
                $scope.loadingLayer = HttpUtils.post("jenkins/job/deleteJob", deleteJobs, function (data) {
                    if (data.success) {
                        Notification.success("删除成功！");
                        $scope.list();
                    } else {
                        Notification.warn(data.message)
                    }
                });
            });
        } else {
            Notification.warn("请选择要删除的构建任务！");
        }
    };


    /**
     * 执行构建任务
     * @param item
     */
    $scope.buildJobs = function (item) {
        let buildJobs = [];
        if (item) {
            buildJobs.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    buildJobs.push(item)
                }
            });
        }
        if (buildJobs.length !== 0) {
            $scope.loadingLayer = HttpUtils.post("jenkins/job/buildJobs", buildJobs, function (data) {
                if (data.success) {
                    Notification.success("正在执行构建，若自动同步完成后仍无构建历史，请稍后手动同步···");
                } else {
                    Notification.warn(data.message)
                }
            });
        } else {
            Notification.warn("请选择要执行的构建任务！");
        }
    };

    const parameterTitles = [
        {
            type: "STRING_PARAMETER",
            title: "字符参数"
        }, {
            type: "PASSWORD_PARAMETER",
            title: "密码参数"
        }, {
            type: "BOOLEAN_PARAMETER",
            title: "布尔值参数"
        }, {
            type: "FILE_PARAMETER",
            title: "文件参数"
        }, {
            type: "TEXT_PARAMETER",
            title: "文本参数"
        }, {
            type: "RUN_PARAMETER",
            title: "运行时参数"
        }, {
            type: "CHOICE_PARAMETER",
            title: "选项参数"
        }
    ];


    $scope.closeDetail = function () {
        $mdDialog.cancel();
        if ($scope.outputTimer) {
            $interval.cancel($scope.outputTimer);
        }
    };


    // 打开参数化构建
    $scope.openBuildWithParameters = function (item) {
        HttpUtils.get('jenkins/job/' + item.id, function (data) {
            if (data.success) {


                let jobItem = data.data;
                jobItem.type = item.type;
                sessionStorage.setItem("item", angular.toJson(jobItem));
                sessionStorage.setItem('jobId', item.id);
                if ($scope.jobView) {
                    sessionStorage.setItem("jobViewId", $scope.jobView.id);
                }

                $scope.item = angular.fromJson(sessionStorage.getItem("item"));



                $scope.item.properties.forEach(function (property) {
                    switch (property.type) {
                        case "PARAMETERS_DEFINITION": {
                            // 存在参数化定义属性 开启展示div
                            $scope.item.parameterizedBuild = true;

                            let parameterDefinitions = property['parameterDefinitions']

                            let parametersMap = new Map();

                            parameterTitles.forEach(function (parameter){
                                parametersMap.set(parameter['type'], parameter['title']);
                            })

                            if (parameterDefinitions && parameterDefinitions.length > 0){
                                parameterDefinitions.forEach(function (parameter){
                                    if (parameter.type === 'STRING_PARAMETER' || parameter.type === 'PASSWORD_PARAMETER' || parameter.type === 'TEXT_PARAMETER' ){
                                        parameter.value = parameter.defaultValue
                                    }else if (parameter.type === 'BOOLEAN_PARAMETER'){
                                        // 这里要转换为字符串的true
                                        parameter.value = parameter.defaultValue
                                    }else if (parameter.type === 'FILE_PARAMETER'){
                                        // 文件参数的key未知 暂时无法实现
                                        // params[parameter.name]=parameter.value
                                    }else if (parameter.type === 'CHOICE_PARAMETER'){
                                        if(parameter.choices && parameter.choices.length > 0){
                                            parameter.value = parameter.choices[0];
                                        }
                                        // parameter.description = parameter.description.replace(/\r\n/g,'<br>');
                                        // parameter.description = parameter.description.replace(/\n/g,'<br>');

                                        // app.filter('trust2Html', ['$sce',function($sce) {
                                        //     return function(val) {
                                        //         return $sce.trustAsHtml(val);
                                        //     };
                                        // }])

                                    }else if (parameter.type === 'RUN_PARAMETER'){
                                        // parameter.value = parameter.defaultValue
                                        // parameter.filter
                                        condition={}
                                        condition.jobName = parameter.projectName;
                                        //buildStatus
                                        if (parameter.filter === 'ALL'){

                                        }else if (parameter.filter === 'SUCCESSFUL' || parameter.filter === 'COMPLETED' || parameter.filter === 'STABLE'){
                                            // TODO: 完成和稳定 未知对应状态 在构建历史中只看到 成功 失败 终止
                                            condition.buildStatus = 'SUCCESS';
                                        }

                                        $scope.jobHistories=[];

                                        // HttpUtils.paging($scope, 'jenkins/job/history/list', condition);
                                        HttpUtils.post('jenkins/job/history/list/' + 1 + "/" + 100, condition, function (response) {
                                            let items = response.data.listObject;
                                            items.forEach(function (item){
                                                let fullname = item.jobName+item.name;
                                                $scope.jobHistories.push(fullname);
                                            });
                                        });






                                    }
                                    // 设置titile
                                    parameter.title = parametersMap.get(parameter.type)

                                })
                            }else{
                                // map传参 [{"key":"value"}]
                                Notification.warn('待传入的参数个数为0 请检查输入');
                            }
                        }
                        default: {
                            break;
                        }
                    }
                });


            } else {
                Notification.warn(data.message);
            }
        });

        $scope.formUrl = 'project/html/jenkins/build-with-paramters.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    /**
     * 执行参数化构建任务
     *
     * @param item 参数化配置包含在属性中
     */
    $scope.buildWithParametersJob = function (item) {

        let params = {
            // key: 'value'
        }

        if (item.parameterizedBuild){
            for (let i = 0; i < $scope.item.properties.length; i++) {
                if (item.properties[i].type === 'PARAMETERS_DEFINITION'){
                    let parameterDefinitions = item.properties[i]['parameterDefinitions']
                    if (parameterDefinitions && parameterDefinitions.length > 0){
                        parameterDefinitions.forEach(function (parameter){
                            if (parameter.type === 'STRING_PARAMETER' || parameter.type === 'PASSWORD_PARAMETER' || parameter.type === 'RUN_PARAMETER' || parameter.type === 'TEXT_PARAMETER' || parameter.type === 'CHOICE_PARAMETER' ){
                                if (parameter.trim){
                                    params[parameter.name]=parameter.value.trim()
                                }else{
                                    params[parameter.name]=parameter.value
                                }
                            }else if (parameter.type === 'BOOLEAN_PARAMETER'){
                                // 这里要转换为字符串的true
                                params[parameter.name]=parameter.value+''
                            }else if (parameter.type === 'FILE_PARAMETER'){
                                // 文件参数的key未知 暂时无法实现
                                // params[parameter.name]=parameter.value
                            }

                        })
                    }else{
                        Notification.warn('待传入的参数个数为0 请检查输入');
                    }

                    break;
                }
            }

        }else {
            Notification.warn("当前非参数化构建任务 无法执行！");
            return
        }

        // 因为后端限制，需要把处理好的参数json放到job对象中params
        item.params = params

        $scope.loadingLayer = HttpUtils.post("jenkins/job/buildWithParametersJob", item, function (data) {
            if (data.success) {
                Notification.success("正在执行参数化构建，若自动同步完成后仍无构建历史，请稍后手动同步···");
            } else {
                Notification.warn(data.message)
            }
        });

        $scope.toggleForm();

    };

    /**
     * 同步构建任务
     * @param item
     */
    $scope.syncJobs = function (item) {
        let jobs = [];
        if (item) {
            jobs.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    jobs.push(item)
                }
            });
        }
        if (jobs.length !== 0) {
            Notification.confirm("确定同步所选构建任务？", function () {
                $scope.loading = HttpUtils.post("jenkins/job/syncJobs", jobs, function (data) {
                    if (data.success) {
                        Notification.success("正在同步，请稍后···");
                        jobs.forEach(function (item) {
                            item.status = 'IN_SYNC';
                        });
                        $scope.list();
                    } else {
                        Notification.warn(data.message)
                    }
                });
            });
        } else {
            Notification.info("请选择要同步的构建任务！")
        }
    };

    $scope.grant = function (item) {
        let grantJobs = [];
        if (item) {
            grantJobs.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    grantJobs.push(item)
                }
            });
        }
        if (grantJobs.length !== 0) {
            $scope.grantJobs = grantJobs;
            $scope.formUrl = 'project/html/jenkins/job/jenkins-job-grant.html' + '?_t=' + Math.random();
            $scope.toggleForm();
        } else {
            Notification.info("请选择要分配工作空间的构建任务！")
        }
    };

    $scope.goJobHistories = function (item) {
        sessionStorage.setItem('jobId', item.id);
        if ($scope.jobView) {
            sessionStorage.setItem("jobViewId", $scope.jobView.id);
        }
        $state.go("jenkins_job_history_list");
    };

    $scope.goAppVersion = function (item) {
        $scope.loadingLayer = HttpUtils.post('jenkins/job/getApplication', item, function (data) {
            if (data.data) {
                if (data.data.versionCount) {
                    $scope.applicationId = data.data.id;
                    $mdDialog.show({
                        templateUrl: 'project/html/application/version/application-version-list-dialog.html' + '?_t=' + Math.random(),
                        parent: angular.element(document.body),
                        scope: $scope,
                        preserveScope: true,
                        clickOutsideToClose: false,
                        fixedGutter: false,
                        bindToController: true
                    });
                } else {
                    Notification.warn("该应用暂时没有版本！");
                }
            } else {
                Notification.warn("找不到对应的应用！");
            }
        });
    };

});
ProjectApp.controller('JenkinsChildJobCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state, $interval, UserService) {

    //重置一下应用部署页面的url，避免加载
    delete $scope.deployUrl;
    $scope.jobViewId = sessionStorage.getItem('jobViewId');
    sessionStorage.removeItem('jobViewId');

    // 定义搜索条件
    $scope.conditions = [
        {key: "name", name: "名称", directive: "filter-contains"},
        {key: "url", name: "url", directive: "filter-contains"},
        {key: "source", name: "来源", directive: "filter-contains"},
    ];

    // 用于传入后台的参数
    $scope.filters = [{key: "parentId",
        name: "父任务id",
        value: $scope.parentId,}];
    //初始化查询条件,根据页面跳转方向判断是否保留查询条件
    var storage = angular.fromJson(sessionStorage.getItem("condition"));
    $scope.sortslq =null;
    if(storage !=null && sessionStorage.getItem('url')=='/jenkins-job') {
        sessionStorage.removeItem("condition");
        //设置排序
        if(storage.sort){
            $scope.sortslq = storage.sort;
        }
        //设置分页条件
        if(storage.page) {
            $scope.pagination = angular.extend({
                page: 1,
                limit: 10,
                limitOptions: [10, 20, 50, 100]
            }, $scope.pagination);
            $scope.pagination.page = storage.page;
            $scope.pagination.limit = storage.limit;
        }
        //设置查询条件
        if (storage.name) {
            $scope.filters.push({
                key: "name",
                name: "名称",
                value: storage.name,
                label: storage.name.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.url) {
            $scope.filters.push({
                key: "url",
                name: "url",
                value: storage.url,
                label: storage.url.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.source) {
            $scope.filters.push({
                key: "source",
                name: "来源",
                value: storage.source,
                label: storage.source.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.organizationId) {
            $scope.filters.push({
                key: "organizationId",
                name: "组织",
                value: storage.organizationId,
                label: storage.organizationId.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
        if (storage.workspaceId) {
            $scope.filters.push({
                key: "workspaceId",
                name: "工作空间",
                value: storage.workspaceId,
                label: storage.workspaceId.replace(/%/g, ""),
                default: false,
                operator: "="
            });
        }
    }
    // 全选按钮，添加到$scope.columns
    $scope.first = {
        default: true,
        sort: false,
        type: "checkbox",
        checkValue: false,
        change: function (checked) {
            $scope.items.forEach(function (item) {
                item.enable = checked;
            });
        },
        width: "40px"
    };

    $scope.columns = [
        $scope.first,
        {value: "名称", key: "name", sort: false},
        {value: "来源", key: "source", sort: false, checked: false},
        {value: "构建历史", key: "build_size"},
        {value: "Jenkins", key: "url", sort: false},
        {value: "同步状态", key: "sync_status", sort: false},
        {value: "构建状态", key: "build_status", sort: false},
        {value: "是否可构建", key: "buildable", sort: false},
        {value: "创建时间", key: "create_time", sort: true, checked: false},
        {value: "修改时间", key: "update_time", sort: true, checked: false},
        {value: "同步时间", key: "sync_time"},
        {value: "描述", key: "description", checked: false}
    ];

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
            $scope.sortslq = $scope.sort.sql;
        }else if($scope.sortslq){
            condition.sort = $scope.sortslq;
        }

        condition.viewId = $scope.jobView ? $scope.jobView.id : null;
        HttpUtils.paging($scope, 'jenkins/job/list', condition, function () {

            $scope.items.forEach(function (item) {
                if (item.active === 'true') {
                    item.active = true;
                }
                if (item.active === 'false') {
                    item.active = false;
                }

            });
        });
        //存储查询条件
        condition.page = $scope.pagination.page;
        condition.limit = $scope.pagination.limit;
        sessionStorage.setItem("condition",JSON.stringify(condition));
    };

    if (UserService.isAdmin()) {
        $scope.columns.splice(4, 0, {value: "组织", key: "organizationName", width: "15%", sort: false});
        $scope.columns.splice(5, 0, {value: "工作空间", key: "workspaceName", width: "15%", sort: false});

        $scope.conditions.push({
            key: "organizationId",
            name: "组织",
            directive: "filter-select-virtual",
            url: "condition/organization",
            convert: {value: "id", label: "name"}
        }, {
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: {value: "id", label: "name"}
        });
    }

    if (UserService.isOrgAdmin()) {
        $scope.columns.splice(4, 0, {value: "工作空间", key: "workspaceName", width: "15%", sort: false});
        $scope.conditions.push({
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: {value: "id", label: "name"}
        })
    }

    $scope.showWorkspace = function (job) {
        if ($scope.selecteed === job) {
            $scope.selecteed = null;
            $scope.toggleInfoForm();
        } else {
            $scope.selecteed = job;
            $scope.jobName = job.name;
            $scope.loadingLayer = HttpUtils.post('jenkins/job/getJobWorkspace', {
                'jobName': job.name,
                'fileNode': {'path': '/'}
            }, function (data) {
                $scope.fileNodes = data.data;
                $scope.navs = [];
                $scope.infoUrl = "project/html/jenkins/job/jenkins-job-workspace.html?_t=" + Math.random();
                $scope.toggleInfoForm(true);
            });
        }
    };

    $scope.closeForm = function () {
        $scope.toggleInfoForm();
    };

    $scope.init = function () {
        if ($scope.jobViewId) {
            $scope.freshViews($scope.jobViewId);
        } else {
            $scope.freshViews();
        }
        $scope.list();
    };

    $scope.init();


    // 编辑构建任务配置
    $scope.edit = function (item, isCopy) {
        let isEdit = !isCopy;
        Notification.warn('暂不支持该类型的构建任务！');
    };
    $scope.updateStatus = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        } else if($scope.sortslq){
            condition.sort = $scope.sortslq;
        }
        condition.viewId = $scope.jobView ? $scope.jobView.id : null;
        HttpUtils.post('jenkins/job/list/' + $scope.pagination.page + "/" + $scope.pagination.limit, condition, function (response) {
            let items = response.data.listObject;
            items.forEach(function (remoteItem) {
                $scope.items.forEach(function (localItem) {
                    if (localItem.id === remoteItem.id) {
                        localItem.syncTime = remoteItem.syncTime;
                        localItem.buildStatus = remoteItem.buildStatus;
                        localItem.syncStatus = remoteItem.syncStatus;
                        localItem.updateTime = remoteItem.updateTime;
                        localItem.buildable = remoteItem.buildable;
                        localItem.buildSize = remoteItem.buildSize;
                    }
                })
            });
        });
        //存储查询条件
        condition.page = $scope.pagination.page;
        condition.limit = $scope.pagination.limit;
        sessionStorage.setItem("condition",JSON.stringify(condition));
    };

    /**
     * 删除JOB
     * @param item
     */
    $scope.deleteJobs = function (item) {
        let deleteJobs = [];
        if (item) {
            deleteJobs.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    deleteJobs.push(item)
                }
            });
        }
        if (deleteJobs.length !== 0) {
            Notification.confirm("确定删除已选构建任务？", function () {
                $scope.loadingLayer = HttpUtils.post("jenkins/job/deleteJob", deleteJobs, function (data) {
                    if (data.success) {
                        Notification.success("删除成功！");
                        $scope.list();
                    } else {
                        Notification.warn(data.message)
                    }
                });
            });
        } else {
            Notification.warn("请选择要删除的构建任务！");
        }
    };

    $scope.closeDetail = function () {
        $mdDialog.cancel();
        if ($scope.outputTimer) {
            $interval.cancel($scope.outputTimer);
        }
    };

    /**
     * 执行构建任务
     * @param item
     */
    $scope.buildJobs = function (item) {
        let buildJobs = [];
        if (item) {
            buildJobs.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    buildJobs.push(item)
                }
            });
        }
        if (buildJobs.length !== 0) {
            $scope.loadingLayer = HttpUtils.post("jenkins/job/buildJobs", buildJobs, function (data) {
                if (data.success) {
                    Notification.success("正在执行构建，若自动同步完成后仍无构建历史，请稍后手动同步···");
                } else {
                    Notification.warn(data.message)
                }
            });
        } else {
            Notification.warn("请选择要执行的构建任务！");
        }
    };



    /**
     * 同步构建任务
     * @param item
     */
    $scope.syncJobs = function (item) {
        let jobs = [];
        if (item) {
            jobs.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    jobs.push(item)
                }
            });
        }
        if (jobs.length !== 0) {
            Notification.confirm("确定同步所选构建任务？", function () {
                $scope.loading = HttpUtils.post("jenkins/job/syncJobs", jobs, function (data) {
                    if (data.success) {
                        Notification.success("正在同步，请稍后···");
                        jobs.forEach(function (item) {
                            item.status = 'IN_SYNC';
                        });
                        $scope.list();
                    } else {
                        Notification.warn(data.message)
                    }
                });
            });
        } else {
            Notification.info("请选择要同步的构建任务！")
        }
    };

    $scope.goJobHistories = function (item) {
        sessionStorage.setItem('jobId', item.id);
        if ($scope.jobView) {
            sessionStorage.setItem("jobViewId", $scope.jobView.id);
        }
        $state.go("jenkins_job_history_list");
    };
    $scope.goAppVersion = function (item) {
        $scope.loadingLayer = HttpUtils.post('jenkins/job/getApplication', item, function (data) {
            if (data.data) {
                if (data.data.versionCount) {
                    $scope.applicationId = data.data.id;
                    $mdDialog.show({
                        templateUrl: 'project/html/application/version/application-version-list-dialog.html' + '?_t=' + Math.random(),
                        parent: angular.element(document.body),
                        scope: $scope,
                        preserveScope: true,
                        clickOutsideToClose: false,
                        fixedGutter: false,
                        bindToController: true
                    });
                } else {
                    Notification.warn("该应用暂时没有版本！");
                }
            } else {
                Notification.warn("找不到对应的应用！");
            }
        });
    };

    //添加定时刷新页面方法
    $scope.timer = $interval($scope.updateStatus, 5000);

    //离开当前页面取消定时器
    $scope.$on('$destroy', function () {
        if ($scope.timer) {
            $interval.cancel($scope.timer);
        }
    });

});

ProjectApp.controller('JenkinsJobAddCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, DeleteService,$timeout, $state,UserService) {
    /**
     * 通用的函数部分
     */
    $scope.getProvidedSettings = function () {
        HttpUtils.post('jenkins/params/getParams', {paramKey: 'provided_settings'}, function (data) {
            $scope.providedSettings = data.data;
        });
    };

    $scope.getProvidedGlobalSettings = function () {
        HttpUtils.post('jenkins/params/getParams', {paramKey: 'provided_global_settings'}, function (data) {
            $scope.providedGlobalSettings = data.data;
        });
    };
    $scope.getToolParams = function () {
        HttpUtils.get('jenkins/params/getToolParams', function (data) {
            if(data.data){
                const defaultValue = {'paramValue':'Default'};
                $scope.antJdkVersions =[defaultValue].concat(data.data.jdk);
                $scope.jdkVersions =data.data.jdk;
                $scope.antVersions = [defaultValue].concat(data.data.ant);
                $scope.mavenVersions = data.data.maven;
            }
        });
    };

    $scope.getJobs = function () {
        HttpUtils.post('jenkins/job/getJobs', {}, function (data) {
            $scope.jobItems = data.data;
        });
    };

    $scope.getCredentials = function () {
        HttpUtils.post('jenkins/credential/getCredentials', {}, function (data) {
            $scope.credentials = [{id: null, displayName: '- 无 -'}].concat(data.data);
            $scope.credentials = $scope.credentials.filter(c => c.typeName != 'Try sample Pipeline');
        });
    };

    $scope.getPipeline = function () {
        HttpUtils.post('jenkins/credential/getCredentials', {typeName :'Try sample Pipeline'}, function (data) {
            $scope.scriptTemplates = [{id: '', displayName: 'Try sample Pipeline',privateKey:''}].concat(data.data);
        });
    };
    $scope.getGitServers = function () {
        //Gitea Server
        HttpUtils.post('jenkins/params/gitea/getServers', {}, function (data) {
            $scope.giteaServers = data.data.servers;
        });
        //GitLab Server
        HttpUtils.post('jenkins/params/gitlab/getServers', {}, function (data) {
            $scope.gitlabServers = data.data.servers;
        });
    };

    $scope.generateSecretToken = function () {
        HttpUtils.get('jenkins/job/generateSecretToken', function (data) {
            for (let i = 0; i < $scope.item.triggers.length; i++) {
                if ($scope.item.triggers[i].type === 'GITLAB_PUSH_TRIGGER'){
                    $scope.item.triggers[i].secretToken = data.data
                    break;
                }
            }
        });

    };


    $scope.deleteItem = DeleteService.deleteItem;
    /**
     * 常量部分
     */

    // 源码管理类型
    $scope.scmTypes = [
        {
            id: "SCM_NULL",
            name: "无"
        }, {
            id: "SCM_GIT",
            name: "Git"
        }, {
            id: "SCM_SVN",
            name: "Subversion"
        }
    ];

    //svn中的 Repository depth
    $scope.depthOptions = [
        {
            id: "infinity",
            name: "infinity"
        }, {
            id: "empty",
            name: "empty"
        }, {
            id: "files",
            name: "files"
        }, {
            id: "immediates",
            name: "immediates"
        }, {
            id: "unknown",
            name: "as-it-is(checkout depth files)"
        }, {
            id: "as-it-is-infinity",
            name: "as-it-is(checkout depth infinity)"
        }
    ];

    //svn中的 Check-out Strategy
    $scope.workspaceUpdaters = [
        {
            id: "hudson.scm.subversion.UpdateUpdater",
            name: "Use‘svn update’ as much as possible"
        }, {
            id: "hudson.scm.subversion.CheckoutUpdater",
            name: "Alwayscheck out a fresh copy"
        }, {
            id: "hudson.scm.subversion.NoopUpdater",
            name: "Do not touch working copy, it is updated by other script."
        }, {
            id: "hudson.scm.subversion.UpdateWithCleanUpdater",
            name: "Emulateclean checkout by first deleting unversioned/ignored files，then ‘svn update’"
        }, {
            id: "hudson.scm.subversion.UpdateWithRevertUpdater",
            name: "Use‘svn update’ as much as possible，with ‘svn revert’ before update"
        }
    ];

    $scope.filters = [
        {
            id: "ALL",
            name: "所有构建"
        }, {
            id: "COMPLETED",
            name: "执行完成的构建"
        }, {
            id: "SUCCESSFUL",
            name: "成功的构建"
        }, {
            id: "STABLE",
            name: "稳定的构建"
        }
    ];

    // gitlab 开启合并请求后是否允许重新构建 的操作选项
    $scope.onPushOperations = [
        {
            id: "never",
            name: "Never"
        }, {
            id: "source",
            name: "On push to source branch"
        }, {
            id: "both",
            name: "On push to source or target branch"
        }
    ];
    //流水线
    const flowJob = {
        description:'',
        definition: {
            scriptPath: "",
            plugin: "",
            scm: {
                configVersion: "",
                userRemoteConfigs: [{
                    credentialsId: "",
                    url: ""
                }],
                branches: [{
                    name: "*/master"
                }],
                extensions: [{
                    relativeTargetDir: ""
                }],
                browser: {
                    url: ""
                },
                classStr: "hudson.plugins.git.GitSCM"
            },
        }
    };

    const multibranchJob = {
        name: '',
        description: '',
        sources: [],
        scriptPath:'Jenkinsfile'
    };

    //git仓库
    const userRemoteConfig = {
        url: '',
        credentialsId: null,
        name: null,
        refspec: null
    };

    //git分支
    const branch = {
        name: "*/master"
    };

    //svn模块
    const module = {
        remote: '',
        credentialsId: null,
        local: '.',
        depthOption: 'infinity',
        ignoreExternalsOption: true,
        cancelProcessOnExternalsFail: true
    };
    //
    const extension ={
        relativeTargetDir: ""
    }

    //默认maven builder
    const mavenBuilder = {
        type: 'MAVEN',
        mavenName: 'default',
        settings: {classStr: 'jenkins.mvn.DefaultSettingsProvider'},
        globalSettings: {classStr: 'jenkins.mvn.DefaultGlobalSettingsProvider'}
    };
    //默认shell
    const shellBuilder = {
        type: 'SHELL'
    };
    //默认sonar builder
    const sonarBuilder = {
        type: 'SONAR',
        properties:'sonar.projectKey=${JOB_NAME}\n' +
            'sonar.projectName=${JOB_NAME}\n' +
            'sonar.sources=src\n' +
            'sonar.java.binaries=target/classes\n' +
            'sonar.language=java\n'+
            'sonar.qualitygate.wait=true\n'+
            'sonar.qualitygate.timeout=600'
    };
    //默认ant builder
    const antBuilder = {
        type : 'ANT',
        targets : '',
        antName : '',
        antOpts : '',
        buildFile : '',
        properties : '',
    };
    //默认的F2Cpublisher
    const f2cPublisher = {
        type: 'F2C_PUBLISHER',
        appspecFilePath: 'appspec.yml',
        excludes: '*.class',
        includes: '**',
        pollingTimeoutSec: 600,
        pollingFreqSec: 15,
        applicationVersionName: 'V1.0-Build_${BUILD_NUMBER}',
        nexusGroupId: 'xyzq.com.cn',
        nexusArtifactId: '',
        nexusArtifactVersion: '1.0'
    };
    //默认的maven仓库发布
    const redeployPublisher = {
        type: 'REDEPLOY_PUBLISHER',
        uniqueVersion: true
    };
    //默认的构建触发器
    const builderTrigger = {
        type: 'BUILDER_TRIGGER',
        threshold: {name: 'SUCCESS'}
    };
    //默认的构建触发器
    const emailPublisher = {
        type: 'EMAIL_PUBLISHER',
        recipients: UserService.getUserInfo().email,
        dontNotifyEveryUnstableBuild: true,
        sendToIndividuals: false,
        perModuleEmail: true
    };

    //maven使用私有仓库选项
    $scope.localRepositoryClassStrs = [{
        key: 'Default (~/.m2/repository)',
        value: 'hudson.maven.local_repo.DefaultLocalRepositoryLocator'
    }, {
        key: 'Local to the executor',
        value: 'hudson.maven.local_repo.PerExecutorLocalRepositoryLocator'
    }, {
        key: 'Local to the workspace',
        value: 'hudson.maven.local_repo.PerJobLocalRepositoryLocator'
    }];
    //maven校验等级选项
    $scope.levelOptions = [{
        key: 'DEFAULT',
        value: '-1'
    }, {
        key: 'LEVEL_MINIMAL',
        value: '0'
    }, {
        key: 'LEVEL_MAVEN_2_0',
        value: '20'
    }, {
        key: 'LEVEL_MAVEN_3_0',
        value: '30'
    }, {
        key: 'LEVEL_MAVEN_3_1',
        value: '31'
    }];
    //maven默认设置选项
    $scope.settingsClasses = [{
        key: '使用默认 Maven 设置',
        value: 'jenkins.mvn.DefaultSettingsProvider'
    }, {
        key: 'Provided settings.xml',
        value: 'org.jenkinsci.plugins.configfiles.maven.job.MvnSettingsProvider'
    },{
        key: '文件系统中的 settings 文件',
        value: 'jenkins.mvn.FilePathSettingsProvider'
    }];
    //maven全局设置选项
    $scope.globalSettingsClasses = [{
        key: '使用默认 Maven 全局设置',
        value: 'jenkins.mvn.DefaultGlobalSettingsProvider'
    }, {
        key: 'provided global settings.xml',
        value: 'org.jenkinsci.plugins.configfiles.maven.job.MvnGlobalSettingsProvider'
    }, {
        key: '文件系统中的全局 settings 文件',
        value: 'jenkins.mvn.FilePathGlobalSettingsProvider'
    }];
    //构建阀值
    $scope.thresholds = {
        'SUCCESS': {
            name: 'SUCCESS',
            ordinal: 0,
            color: 'BLUE'
        }, 'UNSTABLE': {
            name: 'UNSTABLE',
            ordinal: 1,
            color: 'YELLOW'
        }, 'FAILURE': {
            name: 'FAILURE',
            ordinal: 2,
            color: 'RED'
        }
    };

    //添加仓库
    $scope.addRepo = function () {
        $scope.item.scm.userRemoteConfigs.push(angular.copy(userRemoteConfig));
    };

    //添加分支
    $scope.addBranch = function () {
        $scope.item.scm.branches.push(angular.copy(branch));
    };

    //添加流水线仓库
    $scope.addFlowRepo = function () {
        $scope.item.definition.scm.userRemoteConfigs.push(angular.copy(userRemoteConfig));
    };

    //添加流水线分支
    $scope.addFlowBranch = function () {
        $scope.item.definition.scm.branches.push(angular.copy(branch));
    };

    //新增模块
    $scope.addModule = function () {
        $scope.item.scm.locations.push(angular.copy(module));
    };

    $scope.changeScm = function () {
        switch ($scope.item.scm.type) {
            case "SCM_NULL": {
                $scope.item.scm.classStr = "hudson.scm.NullSCM";
                break;
            }
            case "SCM_SVN": {
                $scope.item.scm.classStr = "hudson.scm.SubversionSCM";
                if (!$scope.item.scm.locations) {
                    $scope.item.scm.locations = [angular.copy(module)];
                    $scope.item.scm.workspaceUpdater = {classStr: 'hudson.scm.subversion.UpdateUpdater'};
                    $scope.item.scm.quietOperation = true;
                }
                break;
            }
            case "SCM_GIT": {
                $scope.item.scm.classStr = "hudson.plugins.git.GitSCM";
                if (!$scope.item.scm.userRemoteConfigs) {
                    $scope.item.scm.userRemoteConfigs = [angular.copy(userRemoteConfig)];
                    $scope.item.scm.branches = [];
                    $scope.addBranch();
                }
                break;
            }
            default:
                break;
        }
    };

    $scope.changeFlowScm = function () {
        if($scope.item.definition.type == 'CPS_SCM'){
            $scope.item.definition.clazz = 'org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition';
            if (!$scope.item.definition.scriptPath) {
                $scope.item.definition.scriptPath = 'Jenkinsfile';
            }
            if (!$scope.item.definition.scm) {
                $scope.item.definition.scm = flowJob.definition.scm;
            }
        }
        if($scope.item.definition.type == 'CPS_NULL'){
            $scope.item.definition.clazz = 'org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition';
        }
    };
    //changeMultibranchScm
    const gitlabSource ={
        serverName: null,
        projectOwner: null,
        projectPath: null,
        credentialsId: null,
        sshRemote: null,
        httpRemote: null,
        projectId: null,
        type: 'gitlab'
    }
    const giteaSource ={
        serverUrl: null,
        repoOwner: null,
        repository: null,
        credentialsId: null,
        type: 'gitea'
    }
    //添加多分支流水线仓库
    $scope.addMultibranchRepo = function () {
        if($scope.item.scm.type == 'gitea'){
            $scope.item.sources.push(angular.copy(giteaSource));
        }
        if($scope.item.scm.type == 'gitlab'){
            $scope.item.sources.push(angular.copy(gitlabSource));
        }
    };

    $scope.changeMultibranchScm = function () {
        if($scope.item.scm.type == 'gitea'){
            // $scope.item.sources.push(angular.copy(giteaSource));
            $scope.item.sources=[angular.copy(giteaSource)];
        }
        if($scope.item.scm.type == 'gitlab'){
            $scope.item.sources=[angular.copy(gitlabSource)];
        }
    };
    $scope.changeMultibranchRepoOwner = function (s) {
        HttpUtils.post('jenkins/params/fillProjectPathItems', s , function (data) {
            if(s.type === 'gitea'){
                $scope.giteaServers.forEach(gs => {
                    if(gs.serverUrl === s.serverUrl){
                        s.serverUrl = gs.paramKey+'@'+gs.serverUrl;
                    }
                });
            }
            $scope.repositorys = [];
            if(data.data.values.length == 1 && !data.data.values[0].name){
                $scope.repositorys =[{value: null, name: '- 无 -'}];
            }else{
                $scope.repositorys = data.data.values;
            }
        });
    };
    // $element.find('.search-input').on('keydown', function (ev) {
    //     ev.stopPropagation();
    // });

    $scope.changeTemplate = function (){
        for (let i = 0; i < $scope.scriptTemplates.length; i++) {
            if($scope.scriptTemplates[i].id === $scope.item.scriptTemplate){
                $scope.item.definition.script = $scope.scriptTemplates[i].privateKey;
                break;
            }
        }
    };

    const parametersDefinition = {
        type: 'PARAMETERS_DEFINITION',
        parameterDefinitions: []
    };

    const stringParameterDefinition = {
        type: 'STRING_PARAMETER',
        name: undefined,
        defaultValue: undefined,
        description: undefined,
        trim: false
    };
    const passwordParameterDefinition = {
        type: 'PASSWORD_PARAMETER',
        name: undefined,
        defaultValue: undefined,
        description: undefined
    };
    const booleanParameterDefinition = {
        type: 'BOOLEAN_PARAMETER',
        name: undefined,
        defaultValue: false,
        description: undefined
    };
    const fileParameterDefinition = {
        type: 'FILE_PARAMETER',
        description: undefined
    };
    const textParameterDefinition = {
        type: 'TEXT_PARAMETER',
        name: undefined,
        defaultValue: undefined,
        description: undefined

    };
    const runParameterDefinition = {
        type: 'RUN_PARAMETER',
        name: undefined,
        projectName: undefined,
        description: undefined,
        filter: undefined
    };
    const choiceParameterDefinition = {
        type: 'CHOICE_PARAMETER',
        name: undefined,
        choices: undefined,
        description: undefined
    };

    const preCleanUpDefinition = {
        type: 'PRE_BUILD_CLEANUP',
        patterns: [
            {
                type: '',
                pattern: ''
            }
        ],
        deleteDirs: false,
        cleanupParameter: '',
        externalDelete: '',
        disableDeferredWipeout: false
    };


    // 参数定义设置 'STRING_PARAMETER','parameterDefinitions' item.properties[i].parameters.push {type: 'STRING'}
    $scope.addParameterDefinition = function (parameterType, parametersName) {

        let existParameterDefinitions = false
        let idx = -1;

        if ($scope.item.parameterizedBuild){
            for (let i = 0; i < $scope.item.properties.length; i++) {
                if ($scope.item.properties[i].type === 'PARAMETERS_DEFINITION'){

                    $scope.item.properties[i][parametersName] || ($scope.item.properties[i][parametersName] = []);

                    existParameterDefinitions = true;
                    idx = i
                    break;
                }
            }

        }else {
            $scope.item.properties.push(angular.copy(parametersDefinition));
            idx = $scope.item.properties.length - 1;
        }

        switch (parameterType) {
            case 'STRING_PARAMETER': {
                $scope.item.properties[idx][parametersName].push(angular.copy(stringParameterDefinition));
                break;
            }
            case 'PASSWORD_PARAMETER': {
                $scope.item.properties[idx][parametersName].push(angular.copy(passwordParameterDefinition));
                break;
            }
            case 'BOOLEAN_PARAMETER': {
                $scope.item.properties[idx][parametersName].push(angular.copy(booleanParameterDefinition));
                break;
            }
            case 'FILE_PARAMETER': {
                $scope.item.properties[idx][parametersName].push(angular.copy(fileParameterDefinition));
                break;
            }
            case 'TEXT_PARAMETER': {
                $scope.item.properties[idx][parametersName].push(angular.copy(textParameterDefinition));
                break;
            }
            case 'RUN_PARAMETER': {
                $scope.item.properties[idx][parametersName].push(angular.copy(runParameterDefinition));
                break;
            }
            case 'CHOICE_PARAMETER': {
                $scope.item.properties[idx][parametersName].push(angular.copy(choiceParameterDefinition));
                break;
            }
            default: {

            }
        }

    };


    //自由风格job相关设置 'SHELL','builders' item.builders.push
    $scope.addBuildStep = function (builderType, buildersName) {
        $scope.item[buildersName] || ($scope.item[buildersName] = []);
        switch (builderType) {
            case 'SHELL': {
                $scope.item[buildersName].push(angular.copy(shellBuilder));
                break;
            }
            case 'MAVEN': {
                $scope.item[buildersName].push(angular.copy(mavenBuilder));
                break;
            }
            case 'SONAR': {
                $scope.item[buildersName].push(angular.copy(sonarBuilder));
                break;
            }
            case 'ANT': {
                $scope.item[buildersName].push(angular.copy(antBuilder));
                break;
            }
            default: {

            }
        }

    };

    $scope.addPublisher = function (publisherType) {
        $scope.item.publishers || ($scope.item.publishers = []);
        switch (publisherType) {
            case 'F2C_PUBLISHER': {
                $scope.item.publishers.push(angular.copy(f2cPublisher));
                break;
            }
            case 'REDEPLOY_PUBLISHER': {
                $scope.item.publishers.push(angular.copy(redeployPublisher));
                break;
            }
            case 'BUILDER_TRIGGER': {
                $scope.item.publishers.push(angular.copy(builderTrigger));
                break;
            }
            case 'EMAIL_PUBLISHER': {
                $scope.item.publishers.push(angular.copy(emailPublisher));
                break;
            }
            default: {

            }
        }
    };
    //过滤publisher
    $scope.publishersDict = {};
    $scope.$watch('item.publishers', function () {
        $scope.publishersDict = {};
        $scope.item.publishers && $scope.item.publishers.forEach(function (item) {
            $scope.publishersDict[item.type] = true;
        });
    }, true);

    $scope.$watch("item.runPostStepsIfResult.name", function (newVal) {
        $scope.item.runPostStepsIfResult = angular.copy($scope.thresholds[newVal]);
    }, true);

    $scope.submit = function () {
        $scope.item.aggregatorStyleBuild = !$scope.item.aggregatorStyleBuild;

        // 初始化数据的冗余处理，在提交时做标记检测。未启用的用过滤方式删除结构 尽量和jenkins配置保持一致。
        $scope.item.triggers = $scope.item.triggers.filter(function(trigger) {

            let isRetain = true
            if (trigger.type === 'REVERSE_BUILD_TRIGGER' && !$scope.buildAfterOthers) {
                isRetain = false
            } else if (trigger.type === 'TIMER_TRIGGER' && !$scope.cronBuild) {
                isRetain = false
            } else if (trigger.type === 'GITLAB_PUSH_TRIGGER' && !$scope.buildOnPushGitLab) {
                isRetain = false
            }

            return isRetain
        });



        if ($scope.buildOnPushGitLab === true){
            $scope.item.triggers.forEach(function (item) {
                switch (item.type) {
                    case "GITLAB_PUSH_TRIGGER": {
                        if ($scope.labelFilterEnable === false || $scope.labelFilterEnable === undefined){
                            delete item.mergeRequestLabelFilterConfig;
                            // console.info("delete item.mergeRequestLabelFilterConfig")
                        }
                    }
                }
            })
        }

        // 判断页面上的参数配置没有启用前提下，删除这个参数配置对象 注意，不能for循环遍历删除可变数组
        $scope.item.properties = $scope.item.properties.filter(function(property) {

            let isRetain = true
            if (property.type === 'BUILD_DISCARDER' && !$scope.buildDiscard) {
                isRetain = false
            }else if (property.type === 'PARAMETERS_DEFINITION' ){
                if (!$scope.item.parameterizedBuild || property.parameterDefinitions.length === 0) {
                    $scope.item.parameterizedBuild = false;
                    isRetain = false
                }

            }

            return isRetain
        });

        $scope.item.buildWrappers = $scope.item.buildWrappers.filter(function(buildWrapper) {

            let isRetain = true
            if (buildWrapper.type === 'ANT_WRAPPER' && !$scope.withAnt) {
                isRetain = false
            }

            if (buildWrapper.type === 'PRE_BUILD_CLEANUP' && !$scope.withPreBuildCleanUp) {
                isRetain = false
            }

            return isRetain
        });

        if ($scope.withAnt){
            for (let i = 0; i < $scope.item.buildWrappers.length; i++) {
                if ($scope.item.buildWrappers[i].type === 'ANT_WRAPPER') {
                    antWrapper = $scope.item.buildWrappers[i]
                    if (antWrapper.installation === 'Default'){
                        antWrapper.installation = undefined
                    }
                    if (antWrapper.jdk === 'Default'){
                        antWrapper.jdk = undefined
                    }
                }
            }
        }
        if ($scope.item.assignedNode) {
            $scope.item.canRoam = false;
        }

        if ($scope.item.parameterizedBuild){
            for (let i = 0; i < $scope.item.properties.length; i++) {
                if ($scope.item.properties[i].type === 'PARAMETERS_DEFINITION'){
                    let parameterDefinitions = $scope.item.properties[i]['parameterDefinitions']
                    if (parameterDefinitions && parameterDefinitions.length > 0){

                        parameterDefinitions.forEach(function (parameter){
                            if (parameter.type === 'STRING_PARAMETER' || parameter.type === 'PASSWORD_PARAMETER' || parameter.type === 'RUN_PARAMETER' || parameter.type === 'TEXT_PARAMETER'  ){
                            }else if (parameter.type === 'BOOLEAN_PARAMETER'){
                            }else if (parameter.type === 'FILE_PARAMETER'){
                                // 文件参数的key未知 暂时无法实现
                                // params[parameter.name]=parameter.value
                            }else if (parameter.type === 'CHOICE_PARAMETER'){
                                // choices = "AAA\nBBB\nCCC"
                                const list = parameter.choices.split(/[\n]/);
                                parameter.choices = [...new Set(list)];

                            }

                        })
                    }else{
                        Notification.warn('待传入的参数个数为0 请检查输入');
                    }
                    break;
                }
            }

        }else {
            Notification.info("当前参数化构建配置已取消 保存后将丢失！");
        }
        if($scope.item.type == "WORKFLOW_MULTI_BRANCH"){
            $scope.item.sources.forEach(s => {
                if(s.type === 'gitea'){
                    if(s.serverUrl.indexOf('@') > 0){
                        s.serverUrl = s.serverUrl.split('@')[1];
                    }
                }
            })
        }

        console.info('submit =================>',$scope.item)
        $scope.loadingLayer = HttpUtils.post("jenkins/job/save", $scope.item, function (data) {
            if (data.success) {
                Notification.success("保存成功");
                $state.go('jenkinsJob');
            } else {
                Notification.danger(data.message);
            }
        });
    };

    $scope.showWorkspace = function (job) {
        $scope.jobName = job.name;
        $scope.loadingLayer = HttpUtils.post('jenkins/job/getJobWorkspace', {
            'jobName': job.name,
            'fileNode': {'path': '/'}
        }, function (data) {
            $scope.fileNodes = data.data;
            $scope.navs = [];
            $scope.formUrl = "project/html/jenkins/job/jenkins-job-workspace.html?_t=" + Math.random();
            $scope.toggleForm();
        });
    };

    $scope.closeForm = function () {
        $scope.toggleForm();
    };

    $scope.cancel = function () {
        $state.go('jenkinsJob');
    };

    //增加pattern输入框
    $scope.addPattern = function (buildWrapper) {
        if(!buildWrapper.patterns){
            buildWrapper.patterns = [];
        }
        buildWrapper.patterns.push({type: undefined, pattern: undefined})
    };

    $scope.init = function () {
        // console.info('init...')
        $scope.item = angular.fromJson(sessionStorage.getItem("item"));
        $scope.isEdit = sessionStorage.getItem("isEdit") === 'true';
        $scope.simpleJobItem = angular.fromJson(sessionStorage.getItem("simpleJobItem"));

        if ($scope.isEdit === true){
            $scope.webhookUrl = sessionStorage.getItem('webhookUrl');
        }else{
            $scope.webhookUrl = ''
        }

        $scope.item.aggregatorStyleBuild = !$scope.item.aggregatorStyleBuild;
        $scope.getJobs();
        // $scope.getMavens();
        // $scope.getJdkVersions();
        // $scope.getAntVersions();
        // $scope.getJdkVersionsForAnt();
        $scope.getProvidedSettings();
        $scope.getProvidedGlobalSettings();
        $scope.getCredentials();
        $scope.getPipeline();
        $scope.getGitServers();
        $scope.getToolParams();

        // 双初始化 首次是结构数组为空 所有结构均要初始赋值。
        if (!$scope.item.triggers || $scope.item.triggers.length === 0) {
            $scope.item.triggers = [];

            $scope.buildAfterOthers = false;
            $scope.cronBuild = false;
            $scope.buildOnPushGitLab = false;
            $scope.labelFilterEnable = false;

            $scope.item.triggers.push({
                type: 'REVERSE_BUILD_TRIGGER',
                threshold: {name: 'SUCCESS'}
            }, {type: 'TIMER_TRIGGER'},
                {
                    type: 'GITLAB_PUSH_TRIGGER',
                    spec:'',
                    triggerOnPush:true,
                    triggerToBranchDeleteRequest:false,
                    triggerOnMergeRequest:true,
                    triggerOnlyIfNewCommitsPushed:false,
                    triggerOnPipelineEvent:false,
                    triggerOnAcceptedMergeRequest:false,
                    triggerOnClosedMergeRequest:false,
                    triggerOnApprovedMergeRequest:true,
                    triggerOpenMergeRequestOnPush:'never',
                    triggerOnNoteRequest:true,
                    noteRegex:'Jenkins please retry a build',
                    ciSkip:true,
                    skipWorkInProgressMergeRequest:true,
                    labelsThatForcesBuildIfAdded:'',
                    setBuildDescription:true,
                    branchFilterType:'All',
                    includeBranchesSpec:'',
                    excludeBranchesSpec:'',
                    sourceBranchRegex:'',
                    targetBranchRegex:'',
                    secretToken:null,
                    pendingBuildName:'',
                    cancelPendingBuildsOnUpdate:false,
                    mergeRequestLabelFilterConfig:{include:undefined, exclude:undefined}
                });
        } else  {


            $scope.item.triggers.forEach(function (item) {
                switch (item.type) {
                    case 'REVERSE_BUILD_TRIGGER': {
                        if (item.upstreamProjects) {
                            $scope.buildAfterOthers = true;
                        }
                        break;
                    }
                    case "TIMER_TRIGGER": {
                        if (item.spec) {
                            $scope.cronBuild = true;
                        }
                        break;
                    }
                    case "GITLAB_PUSH_TRIGGER": {

                        // console.info('this type is GITLAB_PUSH_TRIGGER')
                        $scope.buildOnPushGitLab = true;


                        if (item.mergeRequestLabelFilterConfig != null){
                            if (item.mergeRequestLabelFilterConfig.include != null || item.mergeRequestLabelFilterConfig.exclude != null){
                                $scope.labelFilterEnable = true;
                            }
                        }else{
                            item.mergeRequestLabelFilterConfig = {include:undefined, exclude:undefined}
                            $scope.labelFilterEnable = false;
                        }


                        break;
                    }
                    default: {
                        break;
                    }
                }
            });

            // 结构数组不为空但指定结构不在数组中，需要把结构体初始化, 主要是为了包含type字段 否则界面显隐用的ng-if判断失效
            if (!$scope.buildAfterOthers){
                $scope.item.triggers.push({
                        type: 'REVERSE_BUILD_TRIGGER',
                        threshold: {name: 'SUCCESS'}
                    })
            }
            if (!$scope.cronBuild){
                $scope.item.triggers.push({type: 'TIMER_TRIGGER'})
            }

            if (!$scope.buildOnPushGitLab){
                $scope.item.triggers.push({
                    type: 'GITLAB_PUSH_TRIGGER',
                    spec:'',
                    triggerOnPush:true,
                    triggerToBranchDeleteRequest:false,
                    triggerOnMergeRequest:true,
                    triggerOnlyIfNewCommitsPushed:false,
                    triggerOnPipelineEvent:false,
                    triggerOnAcceptedMergeRequest:false,
                    triggerOnClosedMergeRequest:false,
                    triggerOnApprovedMergeRequest:true,
                    triggerOpenMergeRequestOnPush:'never',
                    triggerOnNoteRequest:true,
                    noteRegex:'Jenkins please retry a build',
                    ciSkip:true,
                    skipWorkInProgressMergeRequest:true,
                    labelsThatForcesBuildIfAdded:'',
                    setBuildDescription:true,
                    branchFilterType:'All',
                    includeBranchesSpec:'',
                    excludeBranchesSpec:'',
                    sourceBranchRegex:'',
                    targetBranchRegex:'',
                    secretToken:null,
                    pendingBuildName:'',
                    cancelPendingBuildsOnUpdate:false,
                    mergeRequestLabelFilterConfig:{include:undefined, exclude:undefined}
                })
            }


        }


        /*//修复其他任务构建后触发或者定时构建，其中一个有值，另外一个选中无法触发的问题
        if($scope.item.triggers.length === 1){
            if($scope.item.triggers[0].type=='REVERSE_BUILD_TRIGGER'){
                $scope.item.triggers.push({type: 'TIMER_TRIGGER'});
            }else if($scope.item.triggers[0].type=='TIMER_TRIGGER'){
                $scope.item.triggers.push({
                    type: 'REVERSE_BUILD_TRIGGER',
                    threshold: {name: 'SUCCESS'}
                });
            }
        }*/
        // if(!$scope.item.definition.scm.userRemoteConfigs || $scope.item.definition.scm.userRemoteConfigs === 0){
        //     $scope.item.definition.scm.userRemoteConfigs = [];
        // }
        // console.log($scope.item)
        if(!$scope.item.definition && $scope.item.type == "FLOW"){
            angular.extend($scope.item,angular.copy(flowJob));
        }
        if($scope.item.type == "WORKFLOW_MULTI_BRANCH"){
            if(!$scope.item.sources ){
                angular.extend($scope.item,angular.copy(multibranchJob));
            }
            //$scope.giteaServers
            $scope.item.sources.forEach(s => {
                $scope.changeMultibranchRepoOwner(s);
            })
            if(!$scope.item.f2CPublisher){
                $scope.item.f2CPublisher = {};
            }
        }

        // 属性均为空时
        if (!$scope.item.properties || $scope.item.properties.length === 0) {
            $scope.item.properties = [];
            $scope.buildDiscard = false;
            $scope.item.parameterizedBuild = false;
            // 事先初始化？
            $scope.item.properties.push({type: 'BUILD_DISCARDER', strategy: {type: 'LOG_ROTATOR'}});
            $scope.item.properties.push(angular.copy(parametersDefinition))
        } else {

            $scope.item.properties.forEach(function (item) {
                switch (item.type) {
                    case "BUILD_DISCARDER": {
                        $scope.buildDiscard = true;
                        break;
                    }
                    case "PARAMETERS_DEFINITION": {
                        // 存在参数化定义属性 开启展示div
                        $scope.item.parameterizedBuild = true;

                        // 针对不同的defaultValue字段特殊转换为前端需要的非字符型
                        item.parameterDefinitions.forEach(function (parameter){
                            if (parameter.type === 'STRING_PARAMETER' || parameter.type === 'PASSWORD_PARAMETER' || parameter.type === 'RUN_PARAMETER' || parameter.type === 'TEXT_PARAMETER' ){

                            }else if (parameter.type === 'BOOLEAN_PARAMETER'){
                                // 这里要转换字符串true/false为boolean
                                parameter.defaultValue=JSON.parse(parameter.defaultValue)
                            }else if (parameter.type === 'FILE_PARAMETER'){
                                // 文件参数的key未知 暂时无法实现
                                // params[parameter.name]=parameter.value
                            }else if (parameter.type === 'CHOICE_PARAMETER'){
                                parameter.choices = parameter.choices.join('\n')
                            }

                        })

                        break;
                    }
                    default: {
                        break;
                    }
                }
            });


            if (!$scope.buildDiscard){
                $scope.item.properties.push({type: 'BUILD_DISCARDER', strategy: {type: 'LOG_ROTATOR'}});
            }

            if (!$scope.item.parameterizedBuild){
                $scope.item.properties.push(angular.copy(parametersDefinition))
            }
        }

        //初始化时，如果不存在数组结构 赋值空数组及设置布尔变量
        if (!$scope.item.buildWrappers || $scope.item.buildWrappers.length === 0) {
            $scope.item.buildWrappers = [];
            $scope.withAnt = false;

            //未添加buildWrappers时的初始化 , installation:'Default', jdk:'Default'
            $scope.item.buildWrappers.push({type: 'ANT_WRAPPER', installation:undefined, jdk:undefined});
            $scope.item.buildWrappers.push(angular.copy(preCleanUpDefinition));
        } else {

            $scope.item.buildWrappers.forEach(function (item) {
                switch (item.type) {
                    case "ANT_WRAPPER": {
                        $scope.withAnt = true;
                        break;
                    }
                    case "PRE_BUILD_CLEANUP":{
                        $scope.withPreBuildCleanUp = true;
                        break;
                    }
                    default: {
                        break;
                    }
                }
            });


            if (!$scope.withAnt){
                $scope.item.buildWrappers.push({type: 'ANT_WRAPPER', installation:undefined, jdk:undefined});
            }

            if (!$scope.withPreBuildCleanUp){
                $scope.item.buildWrappers.push(angular.copy(preCleanUpDefinition));
            }
        }


        if ($scope.item.customWorkspace) {
            $scope.useCustomWorkspace = true;
        }
        if (!$scope.item.rootPOM) {
            $scope.item.rootPOM = 'pom.xml';
        }
        if ($scope.item.authToken) {
            $scope.triggerRemoteBuild = true;
        }

        $timeout(function () {
            $scope.cmOption = {
                lineNumbers: true,
                indentWithTabs: true
            };
        }, 500);

        // 初始化后建议打印出完整的结构体比对
        // console.info("init end = ...............",$scope.item)

    };



    $scope.init();

    $scope.triggerRemoteBuildChange = function () {
        if(!$scope.triggerRemoteBuild && $scope.item.authToken){
            $scope.item.authToken="";
        }
    }
});
ProjectApp.controller('JenkinsJobHistoryCtrl', function ($scope, $interval, $mdDialog, $timeout, FilterSearch, Notification, HttpUtils, Loading, $http, $state) {


    // 定义搜索条件
    $scope.conditions = [
        {key: "name", name: "名称", directive: "filter-contains"},
        {key: "url", name: "url", directive: "filter-contains"},
    ];

    // 用于传入后台的参数
    $scope.filters = [];

    $scope.first = {
        default: true,
        sort: false,
        type: "checkbox",
        checkValue: false,
        change: function (checked) {
            $scope.items.forEach(function (item) {
                item.enable = checked;
            });
        },
        width: "40px"
    };

    $scope.columns = [
        $scope.first,
        {value: "名称", key: "name", sort: false},
        {value: "所属JOB名称", key: "jobName", sort: false},
        {value: "序号", key: "orderNum", sort: false, checked: false},
        {value: "URL", key: "url"},
        {value: "Sonarqube", key: "sonarqubeDashboardUrl"},
        {value: "构建时间", key: "build_time", sort: true},
        {value: "构建结果", key: "build_status", sort: false},
        {value: "同步时间", key: "sync_time"},
        {value: "描述", key: "description"}
    ];
    //
    // $scope.columnRecoding = [
    //     $scope.first,
    //     {value: "ID", key: "id", sort: false},
    //     {value: "影响路径", key: "affectedPaths", sort: false},
    //     {value: "提交ID", key: "commitId", sort: false, checked: false},
    //     {value: "时间", key: "timestamp"},
    //     {value: "作者", key: "author", sort: true},
    //     {value: "信息", key: "msg", sort: false}
    // ];

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        $scope.jobId = sessionStorage.getItem("jobId");
        condition.jobId = $scope.jobId;
        HttpUtils.paging($scope, 'jenkins/job/history/list', condition);
    };

    $scope.list();

    $scope.updateStatus = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        $scope.jobId = sessionStorage.getItem("jobId");
        condition.jobId = $scope.jobId;
        HttpUtils.post('jenkins/job/history/list/' + $scope.pagination.page + "/" + $scope.pagination.limit, condition, function (response) {
            let items = response.data.listObject;
            angular.forEach(items, function (data) {
                angular.forEach($scope.items, function (item) {
                    if (item.id === data.id) {
                        item.buildStatus = data.buildStatus;
                        item.sonarqubeDashboardUrl = data.sonarqubeDashboardUrl;
                    }
                })
            });
        });
    };

    //添加定时刷新页面方法
    $scope.timer = $interval($scope.updateStatus, 5000);

    //离开当前页面取消定时器
    $scope.$on('$destroy', function () {
        if ($scope.timer) {
            $interval.cancel($scope.timer);
        }
    });

    $scope.showDetail = function (item) {
        $scope.title = '构建日志';
        $mdDialog.show({
            templateUrl: 'project/html/jenkins/job/output-text-detail.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false,
            fixedGutter: false
        });

        $scope.detailLoding = HttpUtils.post("jenkins/job/history/output", item.id, function (data) {
            let result = data.data;
            $timeout(function () {
                $scope.cmOption = {
                    lineNumbers: true,
                    indentWithTabs: true,
                    theme: 'bespin',
                    readOnly: true
                };
            }, 1000).then(function () {
                $scope.detail = result;
            });
            $timeout.cancel();
        });

        $scope.updateOutput = function () {
            HttpUtils.post("jenkins/job/history/output", item.id, function (data) {
                $scope.detail = data.data;
                if (item.buildStatus !== 'BUILDING' && item.buildStatus !== 'REBUILDING') {
                    $interval.cancel($scope.outputTimer);
                }
            });
        }
        if (item.buildStatus === 'BUILDING' || item.buildStatus === 'REBUILDING') {
            $scope.outputTimer = $interval($scope.updateOutput, 5000);
        }
    };

    $scope.showUpdateRecording = function (item) {
        $mdDialog.show({
            templateUrl: 'project/html/jenkins/job/jenkins-job-history-recoding.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false,
            fixedGutter: false
        });

        $scope.detailLoding = HttpUtils.post("jenkins/job/history/showUpdateRecording", item.id, function (data) {
            $scope.datas = data.data;
        });
    }

    $scope.showParameters = function (item) {
        $mdDialog.show({
            templateUrl: 'project/html/jenkins/job/jenkins-job-history-parameters.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false,
            fixedGutter: false
        });

        $scope.detailLoding = HttpUtils.get("jenkins/job/history/showActions?" + 'historyId='+item.id + '&' + 'buildNumber='+item.orderNum, function (data) {
            let actions = angular.fromJson(data.data)
            $scope.datas = actions.parameters;
        });
    }

    $scope.deleteHistories = function (item) {
        let deleteHistories = [];
        if (item) {
            deleteHistories.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    deleteHistories.push(item)
                }
            });
        }
        if (deleteHistories.length !== 0) {
            Notification.confirm("确定删除已选构建历史？", function () {
                $scope.loadingLayer = HttpUtils.post("jenkins/job/history/deleteHistories", deleteHistories, function (data) {
                    if (data.success) {
                        Notification.success("删除成功！");
                        $scope.list();
                    } else {
                        Notification.warn(data.message)
                    }
                });
            });
        } else {
            Notification.warn("请选择要删除的构建历史！");
        }
    };

    $scope.closeDetail = function () {
        $mdDialog.cancel();
        if ($scope.outputTimer) {
            $interval.cancel($scope.outputTimer);
        }
    };
});
ProjectApp.controller('JenkinsJobGrantCtrl', function ($scope, $mdDialog, $timeout, FilterSearch, Notification, HttpUtils, UserService, Loading, $http, $state) {

    $scope.orgChange = function () {
        if ($scope.organizationId) {
            HttpUtils.post("jenkins/job/getWorkspaces", [$scope.organizationId], function (result) {
                $scope.workspaces = result.data;
            });
        }
    };

    $scope.init = function () {
        if ($scope.grantJobs.length === 1) {
            if ($scope.grantJobs[0].organization) {
                $scope.organizationId = $scope.grantJobs[0].organization;
                $scope.orgChange();
            }
            if ($scope.grantJobs[0].workspace) {
                $scope.workspaceId = $scope.grantJobs[0].workspace;
            }
        }

        if (UserService.isOrgAdmin()) {
            $scope.organizationId = UserService.getOrganizationId();
            $scope.orgChange();
        }

        if (UserService.isAdmin()) {
            HttpUtils.get("jenkins/job/getOrganizations", function (data) {
                $scope.organizations = data.data;
            });
        }
    };

    $scope.submit = function () {
        $scope.grantJobs.forEach(function (item) {
            item.organization = $scope.organizationId;
            item.workspace = $scope.workspaceId;
        });
        $scope.loadingLayer = HttpUtils.post("jenkins/job/jobGrant", $scope.grantJobs, function (data) {
            if (data.success) {
                Notification.success("分配工作空间成功！");
                $scope.toggleForm();
            } else {
                Notification.warn(data.message);
            }
        });
    };
    $scope.init();

});
ProjectApp.controller("JenkinsParamsCtrl", function ($scope, HttpUtils, $http, eyeService, $interval, Notification, FilterSearch, Loading,DeleteService) {
    $scope.isEdit = false;

    $scope.toggleEdit = function () {
        $scope.isEdit = !$scope.isEdit;
        let passwordElement = angular.element("#password");
        let eyeElement = angular.element("#eye");
        passwordElement[0].type = 'password';
        eyeElement.addClass("fa fa-eye");
        $scope.params = angular.copy($scope.originParams);
        $scope.changCronSync();
    };

    $scope.getJenkinsParams = function () {
        $scope.loadingLayer = HttpUtils.get("jenkins/jenkinsParams/systemParams", function (response) {
            $scope.params = response.data;
            $scope.params.forEach(function (item) {
                if (item.paramKey === 'jenkins.enableCronSync' && item.paramValue === 'true') {
                    item.paramValue = true;
                    $scope.enableCronSync = true;
                }
                if (item.paramKey === 'jenkins.enableCronSync' && item.paramValue === 'false') {
                    $scope.enableCronSync = false;
                    item.paramValue = false;
                }
                if (item.paramKey === 'jenkins.cronSyncSpec') {
                    $scope.cronSyncSpec = item.paramValue;
                }
                if (item.paramKey === 'jenkins.syncStatus') {
                    $scope.syncStatus = item.paramValue;
                }
            });
            $scope.originParams = angular.copy(response.data);
        });
    };

    $scope.view = function () {
        eyeService.view("#password", "#eye");
    };

    $scope.validate = function () {
        $scope.loadingLayer = HttpUtils.post("jenkins/validate", $scope.params, function (response) {
            if (response.success) {
                Notification.success("验证成功");
            } else {
                Notification.danger("验证失败， " + response.message);
            }
        });
    };

    $scope.syncAllCredentials = function () {
        $scope.loadingLayer = HttpUtils.get('jenkins/credential/syncAllCredentials', function (data) {
            Notification.info('提交同步任务成功！');
        });
    };

    $scope.submit = function (data) {
        $scope.loadingLayer = HttpUtils.post("jenkins/jenkinsParams/systemParams", data, function () {
            Notification.success("编辑成功");
            $scope.getJenkinsParams();
            $scope.toggleEdit()
        });
    };

    $scope.changCronSync = function () {
        $scope.params.forEach(function (item) {
            if (item.paramKey === 'jenkins.enableCronSync' && item.paramValue) {
                $scope.enableCronSync = true;
            }
            if (item.paramKey === 'jenkins.enableCronSync' && !item.paramValue) {
                $scope.enableCronSync = false;
            }
        });
    };

    $scope.getSyncStatus = function () {
        HttpUtils.get('jenkins/syncStatus', function (data) {
            $scope.syncStatus = data.data;
        });
    };

    $scope.timer = $interval($scope.getSyncStatus, 3000);

    $scope.$on('$destroy', function () {
        if ($scope.timer) {
            $interval.cancel($scope.timer);
        }
    });

    $scope.$watch("cronSyncSpec", function (newVal, oldVal) {
        $scope.params && $scope.params.forEach(function (item) {
            if (item.paramKey === 'jenkins.cronSyncSpec') {
                item.paramValue = newVal;
            }
        });
    }, true);

    $scope.syncJobs = function () {
        $scope.loadingLayer = HttpUtils.post('jenkins/job/sync', null, function (data) {
            Notification.info('已提交同步任务！');
        })
    };

    $scope.getJenkinsParams();

//    额外参数部分
    $scope.conditions = [
        {key: "paramKey", name: "键", directive: "filter-contains"}
    ];

    // 用于传入后台的参数
    $scope.filters = [];

    // 全选按钮，添加到$scope.columns
    $scope.first = {
        default: true,
        sort: false,
        type: "checkbox",
        checkValue: false,
        change: function (checked) {
            $scope.items.forEach(function (item) {
                item.enable = checked;
            });
        },
        width: "40px"
    };

    $scope.columns = [
        $scope.first,
        {value: "名称", key: "alias", sort: false},
        {value: "键", key: "param_key"},
        {value: "值", key: "param_value", sort: false}
    ];

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, 'jenkins/params/list', condition, function () {
            $scope.items.forEach(function (item) {
                if (item.active === 'true') {
                    item.active = true;
                }
                if (item.active === 'false') {
                    item.active = false;
                }
            });
        });
    };
    $scope.list();

    $scope.editParam = function (item) {
        if (item) {
            $scope.item = angular.copy(item);
            $scope.isEdit = true;
        } else {
            $scope.item = {};
            $scope.isEdit = false;
        }
        $scope.formUrl = 'project/html/jenkins/jenkins-param-add.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    //GitLab Server
    const gitLabServer = {
        credentialsId: null,
        hooksRootUrl: null,
        manageSystemHooks: null,
        manageWebHooks: null,
        name: null,
        secretToken: null,
        serverUrl: null,
    };

    //Gitea Server
    const giteaServer = {
        credentialsId: null,
        displayName: null,
        manageHooks: null,
        serverUrl: null,
        aliasUrl: null,
    };
    $scope.getCredentials = function () {
        HttpUtils.post('jenkins/credential/getCredentials', {}, function (data) {
            $scope.credentials = [{id: null, displayName: '- 无 -'}].concat(data.data);
            $scope.credentials = $scope.credentials.filter(c => c.typeName != 'Try sample Pipeline');
        });
    };
    $scope.getCredentials();
    //添加仓库
    $scope.addRepo = function () {
        $scope.item.gitLabServers.push(angular.copy(gitLabServer));
    };

    $scope.editServerParam = function (item,type) {
        if (item) {
            $scope.item = angular.copy(item);
            $scope.isEdit = true;
            if(item.paramKey.indexOf('GitLab') > 0){
                $scope.item.type = 'GitLab';
                $scope.item.gitLabServer = angular.copy(JSON.parse(item.paramValue));
                if($scope.item.gitLabServer.manageWebHooks === 'true'){
                    $scope.item.gitLabServer.manageWebHooks = true;
                }
                if($scope.item.gitLabServer.manageSystemHooks === 'true'){
                    $scope.item.gitLabServer.manageSystemHooks = true;
                }
            }
            if(item.paramKey.indexOf('Gitea') > 0){
                $scope.item.type = 'Gitea';
                $scope.item.giteaServer = angular.copy(JSON.parse(item.paramValue));
                if($scope.item.giteaServer.manageHooks === 'true'){
                    $scope.item.giteaServer.manageHooks = true;
                }
            }
        } else {
            $scope.item = {};
            $scope.isEdit = false;
            $scope.item.type = type;
            if (!$scope.item.gitLabServer && type ==='GitLab') {
                $scope.item.gitLabServer = gitLabServer;
            }
            if (!$scope.item.giteaServer && type ==='Gitea') {
                $scope.item.giteaServer = giteaServer;
            }
        }
        $scope.item.yaml = true;
        $scope.formUrl = 'project/html/jenkins/jenkins-gitserver-add.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.deleteItem = DeleteService.deleteItem;
    $scope.deleteParams = function (item) {
        let params = [];
        if (item) {
            params.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    params.push(item)
                }
            });
        }
        if (params.length !== 0) {
            Notification.confirm('确定要删除已选参数？', function () {
                $scope.loadingLayer = HttpUtils.post('jenkins/params/delete', params, function (data) {
                    Notification.info('删除成功');
                    $scope.list();
                });
            });
        } else {
            Notification.info('请选择要删除的参数！');
        }
    };

});
ProjectApp.controller('JenkinsParamsAddCtrl', function ($scope, HttpUtils, Notification) {
    $scope.saveParam = function (item) {
        $scope.loadingLayer = HttpUtils.post('jenkins/params/save', [item], function (data) {
            Notification.info('保存成功！');
            $scope.toggleForm();
            $scope.list();
        });
    };
});
ProjectApp.controller('JenkinsCredentialCtrl', function ($scope, HttpUtils, FilterSearch, Notification) {
    // 定义搜索条件
    $scope.conditions = [
        {key: "displayName", name: "名称", directive: "filter-contains"},
        {
            key: "typeName",
            name: "凭据类型",
            directive: "filter-select",
            selects: [
                {value: "Username with password", label: "用户名和密码"},
                {value: "SSH Username with private key", label: "SSH用户名和秘钥"},
                {value: "Try sample Pipeline", label: "流水线模板"}
            ]
        }
    ];

    // 用于传入后台的参数
    $scope.filters = [];

    // 全选按钮，添加到$scope.columns
    $scope.first = {
        default: true,
        sort: false,
        type: "checkbox",
        checkValue: false,
        change: function (checked) {
            $scope.items.forEach(function (item) {
                item.enable = checked;
            });
        },
        width: "40px"
    };

    $scope.columns = [
        $scope.first,
        {value: "名称", key: "display_name"},
        {value: "类型", key: "type_name", sort: false},
        {value: "描述", key: "description"},
    ];

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, 'jenkins/credential/list', condition, function () {
            $scope.items.forEach(function (item) {
                if (item.active === 'true') {
                    item.active = true;
                }
                if (item.active === 'false') {
                    item.active = false;
                }
            })
        });
    };

    $scope.syncCredentials = function (item) {
        let credentials = [];
        if (item) {
            credentials.push(item);
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    credentials.push(item);
                }
            });
        }
        if (credentials.length === 0) {
            Notification.info('请选择需要同步的凭据！');
            return;
        }
        $scope.loadingLayer = HttpUtils.post('jenkins/credential/syncCredentials', credentials, function (data) {
            Notification.info('提交同步任务成功！');
        })
    };

    $scope.deleteCredentials = function (item) {
        let credentials = [];
        if (item) {
            credentials.push(item);
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    credentials.push(item)
                }
            });
        }
        if (credentials.length === 0) {
            Notification.warn("请选择要删除的凭据！");
        } else {
            Notification.confirm("确定要删除所选凭据吗？", function () {
                $scope.loadingLayer = HttpUtils.post('jenkins/credential/delete', credentials, function (data) {
                    Notification.info('删除成功!');
                    $scope.list();
                });
            });
        }
    };

    $scope.createCredential = function (type) {
        $scope.item = {};
        $scope.item.typeName = type;
        switch (type) {
            case "Username with password": {
                $scope.formUrl = "project/html/jenkins/credential/jenkins-credential-add-normal.html";
                $scope.toggleForm();
                break;
            }
            case "SSH Username with private key": {
                $scope.formUrl = "project/html/jenkins/credential/jenkins-credential-add-ssh.html";
                $scope.toggleForm();
                break;
            }
            case "Try sample Pipeline": {
                $scope.formUrl = "project/html/jenkins/credential/jenkins-credential-add-pipeline.html";
                $scope.toggleForm();
                break;
            }
            default: {
                break;
            }
        }
    };

    $scope.edit = function (item) {
        $scope.item = angular.copy(item);
        $scope.item.isReset = true;
        switch (item.typeName) {
            case 'Username with password': {
                $scope.formUrl = "project/html/jenkins/credential/jenkins-credential-add-normal.html";
                $scope.toggleForm();
                break;
            }
            case 'SSH Username with private key': {
                $scope.formUrl = "project/html/jenkins/credential/jenkins-credential-add-ssh.html";
                $scope.toggleForm();
                break;
            }
            case 'Try sample Pipeline': {
                $scope.formUrl = "project/html/jenkins/credential/jenkins-credential-add-pipeline.html";
                $scope.toggleForm();
                break;
            }
            default: {
                Notification.warn('暂不支持该类型的凭据！');
            }
        }
    };

    $scope.grant = function (item) {
        let grantCredentials = [];
        if (item) {
            grantCredentials.push(item)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    grantCredentials.push(item)
                }
            });
        }
        if (grantCredentials.length !== 0) {
            $scope.grantCredentials = grantCredentials;
            $scope.formUrl = 'project/html/jenkins/credential/jenkins-credential-grant.html' + '?_t=' + Math.random();
            $scope.toggleForm();
        } else {
            Notification.info("请选择要分配工作空间的构建任务！")
        }
    };

    $scope.list();
});
ProjectApp.controller('JenkinsCredentialGrantCtrl', function ($scope, HttpUtils, Notification, UserService) {

    $scope.orgChange = function () {
        if ($scope.organizationId) {
            if (!Array.isArray($scope.organizationId)) {
                $scope.organizationId = $scope.organizationId.split(",");
            }
            HttpUtils.post("jenkins/job/getWorkspaces", $scope.organizationId, function (result) {
                $scope.workspaces = result.data;
            });
        }
    };

    $scope.init = function () {
        if ($scope.grantCredentials.length === 1) {
            if ($scope.grantCredentials[0].organization) {
                $scope.organizationId = $scope.grantCredentials[0].organization.split(",");
                if (UserService.isAdmin()) {
                    $scope.orgChange();
                }
            }
            if ($scope.grantCredentials[0].workspace) {
                $scope.workspaceId = $scope.grantCredentials[0].workspace.split(",");
            }
        }

        if (UserService.isOrgAdmin()) {
            HttpUtils.post("jenkins/job/getWorkspaces", [UserService.getOrganizationId()], function (result) {
                $scope.workspaces = result.data;
            });
        }

        if (UserService.isAdmin()) {
            HttpUtils.get("jenkins/job/getOrganizations", function (data) {
                $scope.organizations = data.data;
            });
        }
    };
    $scope.init();


    $scope.submit = function () {
        $scope.grantCredentials.forEach(function (item) {
            //如果是组织管理员，那么就只能更改自己组织下的工作空间授权
            if (UserService.isOrgAdmin()) {
                let otherOrgWorkspaces = [];
                if (item.workspace) {
                    item.workspace.split(',').forEach(function (oldWorkspace) {
                        if ($scope.workspaces) {
                            let flag = true;
                            $scope.workspaces.forEach(function (newWorkspace) {
                                if (newWorkspace.id === oldWorkspace) {
                                    flag = false;
                                }
                            });
                            if (flag) {
                                otherOrgWorkspaces.push(oldWorkspace);
                            }
                        }
                    });
                    item.workspace = otherOrgWorkspaces.concat($scope.workspaceId).join(',');
                } else {
                    item.workspace = $scope.workspaceId ? $scope.workspaceId.join(',') : null;
                }
            } else {
                item.workspace = $scope.workspaceId.join(',');
                item.organization = $scope.organizationId.join(',');
            }
        });
        HttpUtils.post("jenkins/credential/grantCredential", $scope.grantCredentials, function (data) {
            if (data.success) {
                Notification.success("分配工作空间成功！");
                $scope.toggleForm();
            } else {
                Notification.warn(data.message);
            }
        });
    };
});
ProjectApp.controller('JenkinsCredentialAddCtrl', function ($scope, HttpUtils, eyeService, $timeout, Notification, Loading) {
    $timeout(function () {
        $scope.cmOption = {
            lineNumbers: true,
            indentWithTabs: true,
            theme: 'bespin'
        };
    }, 500);
    $scope.view = function () {
        eyeService.view("#password", "#eye");
    };
    $scope.submit = function () {
        $scope.loadingLayer = HttpUtils.post('jenkins/credential/save', $scope.item, function (data) {
            Notification.info('保存成功！');
            $scope.toggleForm();
            $scope.list();
        });
    };
});
ProjectApp.controller('JenkinsJobViewAddCtrl', function ($scope, HttpUtils, Notification) {

    $scope.getJobs = function () {
        $scope.loadingLayer = HttpUtils.post('jenkins/job/getJobs', {}, function (data) {
            $scope.jobItems = data.data;
        });
    };


    $scope.querySearch = function (query) {
        let items = [];
        if ($scope.jobItems) {
            $scope.jobItems.forEach(function (item) {
                if (item.name.indexOf(query) > -1) {
                    items.push(item.name);
                }
            });
        }
        return items;
    };

    $scope.submit = function () {
        $scope.item.jobIdSet = [];
        $scope.jobNameSet.forEach(function (nameItem) {
            $scope.jobItems.forEach(function (jobItem) {
                if (jobItem.name === nameItem) {
                    $scope.item.jobIdSet.push(jobItem.id);
                }
            });
        });
        $scope.loadingLayer = HttpUtils.post('jenkins/view/save', $scope.item, function (data) {
            Notification.info('保存成功！');
            $scope.toggleForm();
            $scope.freshViews(data.data.id);
        });
    };

    $scope.init = function () {
        $scope.loadingLayer = HttpUtils.post('jenkins/job/getJobs', {}, function (data) {
            $scope.jobItems = data.data;
            $scope.jobNameSet = [];
            if ($scope.item.jobIdSet) {
                $scope.item.jobIdSet.forEach(function (jobId) {
                    $scope.jobItems.forEach(function (jobItem) {
                        if (jobItem.id === jobId) {
                            $scope.jobNameSet.push(jobItem.name);
                        }
                    });
                });
            }
        });
    };

    $scope.init();
});
ProjectApp.controller('JenkinsAddJobToViewCtrl', function ($scope, HttpUtils, Notification, $element) {
    $scope.init = function () {
        $scope.loadingLayer = HttpUtils.post('jenkins/view/getViews', {}, function (data) {
            $scope.jobViews = data.data;
        });
    };

    $element.find('.search-input').on('keydown', function (ev) {
        ev.stopPropagation();
    });

    $scope.clearSearch = function () {
        $scope.viewName = '';
    };

    $scope.submit = function () {
        let body = {jobs: $scope.jobs, views: $scope.selectedViews};
        $scope.loadingLayer = HttpUtils.post('jenkins/view/addJobsToViews', body, function (data) {
            Notification.info('添加成功！');
            $scope.toggleForm();
            $scope.freshViews($scope.jobView.id);
        });
    };

    $scope.init();
});
ProjectApp.controller("JenkinsJobCopyCtrl", function ($scope, HttpUtils, $mdDialog, $element) {

    $scope.init = function () {
        $scope.loadingLayer = HttpUtils.post('jenkins/job/getJobs', {}, function (data) {
            $scope.jobs = data.data;
        });
    };

    $element.find('.search-input').on('keydown', function (ev) {
        ev.stopPropagation();
    });

    $scope.close = function () {
        $mdDialog.cancel();
    };

    $scope.submit = function () {
        $scope.selectedJob.name = $scope.newJobName;
        $mdDialog.cancel();
        $scope.edit($scope.selectedJob, true);
    };

    $scope.init();
});
ProjectApp.controller("JobWorkspaceCtrl", function ($scope, HttpUtils, $http, Notification) {

    $scope.enterFolder = function (item) {
        enterFolder(item, function () {
            $scope.navs.push(item);
        });
    };

    $scope.jumpToNav = function (item, index) {
        enterFolder(item, function () {
            $scope.navs.splice(index + 1, $scope.navs.length);
        });
    };

    $scope.getWorkspaceFile = function (item) {
        $http({
            method: 'POST',
            url: 'jenkins/job/getWorkspaceFile',
            data: {
                jobName: $scope.jobName,
                fileNode: item
            },
            responseType: 'arraybuffer'
        }).then(function (data) {
            let headers = data.headers();
            let contentType = headers['content-type'];
            let linkElement = document.createElement('a');
            try {
                let blob = new Blob([data.data], {type: contentType});
                let url = window.URL.createObjectURL(blob);
                linkElement.setAttribute('href', url);
                linkElement.setAttribute("download", item.name);
                let clickEvent = new MouseEvent("click", {
                    "view": window,
                    "bubbles": true,
                    "cancelable": false
                });
                linkElement.dispatchEvent(clickEvent);
            } catch (ex) {
                console.log(ex);
            }
        }, function (data) {
            console.log(data);
        });
    };

    $scope.cleanWorkspace = function (jobName) {
        HttpUtils.delete('jenkins/job/cleanWorkspace/' + jobName, function (data) {
            Notification.info('清除成功！');
            $scope.navs = [];
            $scope.fileNodes = [];
        });
    };

    function enterFolder(item, func) {
        $scope.loading = HttpUtils.post("jenkins/job/getJobWorkspace", {
            'jobName': $scope.jobName,
            fileNode: item
        }, function (data) {
            func();
            $scope.fileNodes = data.data;
        });
    }

});