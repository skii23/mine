ProjectApp.controller('ApplicationDeploymentLogCtrl', function ($scope, $state, $mdDialog, FilterSearch, Notification, HttpUtils, $document, $interval) {
    // 定义搜索条件
    $scope.conditions = [
        // {key: "no", name: "集群名称", directive: "filter-input"},
    ];
    $scope.applicationDeployment = angular.fromJson(sessionStorage.getItem("applicationDeployment"));
    sessionStorage.removeItem("applicationDeployment");

    // 用于传入后台的参数
    $scope.filters = [
        {
            key: "deploymentId",
            name: "应用",
            value: $scope.applicationDeployment.id,
            label: $scope.applicationDeployment.name,
            default: true,
            operator: "="
        }
    ];

    $scope.goDeployList = function () {
        $state.go('application_deploy')
    };

    $scope.closeProgress = function () {
        $scope.selected = "";
        $scope.toggleForm();
    };

    $scope.showProgress = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/application/deploy/application-deployment-event.html' + '?_t=' + Math.random();
        $scope.toggleForm();

    };

    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.item = {};
        $scope.formUrl = 'project/html/application/application-add.html' + '?_t=' + window.appversion;
        $scope.toggleForm();
    };

    $scope.columns = [
        {value: "主机名称", key: "server_name", width: "30%"},
        {value: "管理IP", key: "manage_ip_address"},
        {value: "开始时间", key: "start_time"},
        {value: "结束时间", key: "end_time"},
        {value: "耗时", key: "cost_time"},
        {value: "部署进度", key: "progress", width: "15%"},
        {value: "状态", key: "status", width: "5%"},
    ];


    $scope.timer = $interval(function () {
        var condition = FilterSearch.convert($scope.filters);
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }

        HttpUtils.post("application/deploy/log/list/" + $scope.pagination.page + "/" + $scope.pagination.limit, condition, function (response) {
            let items = response.data.listObject;
            angular.forEach(items, function (newItem) {
                angular.forEach($scope.items, function (item) {
                    if (newItem.id === item.id) {
                        item.progress = newItem.progress;
                        item.status = newItem.status;
                        item.startTime = newItem.startTime;
                        item.endTime = newItem.endTime;
                    }
                })
            });


            if (!$scope.checkStatus()) {
                $interval.cancel($scope.timer);
            }
        });
    }, 5000);


    $scope.checkStatus = function () {
        let sum = 0;
        angular.forEach($scope.items, function (item) {
            if (item.status !== 'success' && item.status !== 'fail') {
                sum++;
            }
        });
        return sum > 0;
    };


    $scope.$on("$destroy", function () {
        //清除配置,不然scroll会重复请求
        $document.off('click');
        $interval.cancel($scope.timer);
    });

    $scope.list = function (sortObj) {
        var condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, 'application/deploy/log/list', condition)
    };
    $scope.sideCloseEvent = true;
    $scope.list();

}).controller("ApplicationDeploymentEventCtrl", function ($scope, Notification, HttpUtils, $timeout, $interval,$mdDialog) {

    $scope.stdout = "";

    $scope.init = function () {
        $timeout(function () {
            $scope.cmOption = {
                lineNumbers: true,
                indentWithTabs: true,
                theme: 'bespin',
                readOnly: true
            };
        });
        $scope.getEvents();
    };

    $scope.getEvents = function () {
        HttpUtils.get("application/deploy/log/events/" + $scope.item.id, function (data) {
            $scope.steps = data.data;
            if ($scope.item.status !== 'running') {
                $interval.cancel($scope.timer);
            }
        });
    };

    $scope.showEventLog = function (eventLog) {
        $scope.eventLog = eventLog;
        $scope.cmOptionNoLine = {
            lineNumbers: false,
            indentWithTabs: true,
            theme: 'bespin',
            readOnly: true
        };
        $mdDialog.show({
            templateUrl: 'project/html/application/deploy/application-deployment-event-log-detail.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false
        });
    };

    $scope.closeDetail = function () {
        $mdDialog.cancel();
    };

    $scope.codeMirrorContentChangeCallback = function (instance, left, top) {
        instance.scrollTo(left, top);
    };

    $scope.init();
    //update
    $scope.timer = $interval(function () {
        $scope.getEvents();
    }, 2000);

    $scope.$on("$destroy", function () {
        $interval.cancel($scope.timer);
    });

});