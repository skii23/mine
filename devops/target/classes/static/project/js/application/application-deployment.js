ProjectApp.controller('ApplicationDeploymentCtrl', function ($scope, $state, $mdDialog, FilterSearch, Notification, HttpUtils, CommonService, $document, $interval) {
    // 定义搜索条件
    $scope.conditions = [
        {
            key: "applicationId",
            name: "应用列表",
            directive: "filter-select-virtual",
            url: "condition/application",
            convert: {value: "id", label: "name"}
        },
        {
            key: "applicationNameQuery",
            name: "应用搜索",
            directive: "filter-contains",
        }
    ];

    // 用于传入后台的参数
    $scope.filters = [];

    //初始化查询条件,根据页面跳转方向判断是否保留查询条件
    var storage = angular.fromJson(sessionStorage.getItem("deploycondition"));
    console.info(storage);
    console.info(sessionStorage.getItem('url'));
    $scope.sortslq =null;
    if(storage !=null && sessionStorage.getItem('url')=='/application/deploy') {
        sessionStorage.removeItem("deploycondition");
        //设置排序
        if (storage.sort) {
            $scope.sortslq = storage.sort;
        }
        //设置分页条件
        if (storage.page) {
            $scope.pagination = angular.extend({
                page: 1,
                limit: 10,
                limitOptions: [10, 20, 50, 100]
            }, $scope.pagination);
            $scope.pagination.page = storage.page;
            $scope.pagination.limit = storage.limit;
        }
        //设置查询条件
        if (storage.applicationId) {
            $scope.filters.push({
                key: "applicationId",
                name: "应用",
                value: storage.applicationId,
                label: storage.label,
                default: false,
                operator: "="
            });
        }
    }

    $scope.goLog = function (item) {
        sessionStorage.setItem('applicationDeployment', angular.toJson(item));
        $state.go('application_deployment_log');
    };

    $scope.showTest = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/application/deploy/application-test-event.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.application = angular.fromJson(sessionStorage.getItem("application"));
    sessionStorage.removeItem("application");
    // 定义搜索条件
    if ($scope.application) {
        $scope.filters.push({
            key: "applicationId",
            name: "应用",
            value: $scope.application.id,
            label: $scope.application.name,
            default: false,
            operator: "="
        })
    }

    $scope.showDetail = function (item, event) {
        // 点击2次关闭
        if ($scope.selected === item.$$hashKey) {
            $scope.closeInformation();
            return;
        }
        $scope.selected = item.$$hashKey;
        $scope.detail = item;
        $scope.showInformation();
    };

    $scope.closeInformation = function () {
        $scope.selected = "";
        $scope.toggleInfoForm(false);
    };


    $scope.showInformation = function () {
        $scope.infoUrl = 'project/html/application/application-deployment-list.html' + '?_t=' + Math.random();
        $scope.toggleInfoForm(true);
    };


    $scope.columns = [
        {value: "应用", key: "application_name"},
        {value: "应用版本", key: "application_version_name"},
        {value: "API测试(成功率)", key: "test_report_url"},
        {value: "集群", key: "cluster_name"},
        {value: "主机组", key: "cluster_role_name"},
        {value: "主机范围", key: "cloud_server_name"},
        {value: "部署策略", key: "policy"},
        {value: "起始时间", key: "start_time"},
        {value: "结束时间", key: "end_time"},
        {value: "状态", key: "status"},
        {value: "部署用户", key: "user_name"},
        {value: "部署原因",key: "description"}
    ];



    $scope.enableClick = function (event) {
        event.stopPropagation();
    };

    $scope.$on("$destroy", function () {
        //清除配置,不然scroll会重复请求
        $document.off('click');
        $interval.cancel($scope.timer);

    });

    $scope.list = function (sortObj) {
        var condition = FilterSearch.convert($scope.filters);
        if($scope.filters.length>0){
            condition.label = $scope.filters[0].label;
        }

        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
            $scope.sortslq = $scope.sort.sql;
        }else if($scope.sortslq){
            condition.sort = $scope.sortslq;
        }
        HttpUtils.paging($scope, 'application/deploy/list', condition);
        //存储查询条件
        condition.page = $scope.pagination.page;
        condition.limit = $scope.pagination.limit;
        sessionStorage.setItem("deploycondition",JSON.stringify(condition));
    };
    $scope.list();


    $scope.help = function () {
        $scope.msg = "Bottom Sheep Demo";
        $mdBottomSheet.show({
            templateUrl: 'project/html/demo/bottom-sheet.html',
            scope: $scope,
            preserveScope: true
        }).then(function (clickedItem) {
            $scope.msg = clickedItem['name'] + ' clicked!';
        }).catch(function (error) {
            console.log(error)
        });
    };
    // $scope.timer = $interval(function () {
    //     let condition = FilterSearch.convert($scope.filters);
    //     if($scope.filters.length>0){
    //         condition.label = $scope.filters[0].label;
    //     }
    //     if ($scope.sort) {
    //         condition.sort = $scope.sort.sql;
    //     }else if($scope.sortslq){
    //         condition.sort = $scope.sortslq;
    //     }

    //     HttpUtils.post("application/deploy/list/" + $scope.pagination.page + "/" + $scope.pagination.limit, condition, function (response) {
    //         let items = response.data.listObject;
    //         angular.forEach(items, function (newItem) {
    //             angular.forEach($scope.items, function (item) {
    //                 if (newItem.id === item.id) {
    //                     item.status = newItem.status;
    //                     item.endTime = newItem.endTime;
    //                     item.testLogId = newItem.testLogId;
    //                     item.testStatus = newItem.testStatus;
    //                     item.testReportUrl = newItem.testReportUrl;
    //                 }
    //             })
    //         });
    //         if (!$scope.checkStatus()) {
    //             $interval.cancel($scope.timer);
    //         }
    //     });
    //     //存储查询条件
    //     condition.page = $scope.pagination.page;
    //     condition.limit = $scope.pagination.limit;
    //     sessionStorage.setItem("deploycondition",JSON.stringify(condition));
    // }, 5000);

    $scope.checkStatus = function () {
        let sum = 0;
        angular.forEach($scope.items, function (item) {
            if (item.status !== 'success' && item.status !== 'fail') {
                sum++;
            }
            if (item.testStatus !== 'success' && item.testStatus !== 'fail' && item.testStatus !== 'timeout') {
                sum++;
            }
        });
        return sum > 0;
    };
}).controller("ApplicationTestEventCtrl", function ($scope, Notification, HttpUtils, $timeout, $interval,$mdDialog) {

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
        $scope.getTestEvent();
        console.info($scope.item.id);
    };

    $scope.getTestEvent = function () {
        HttpUtils.get("application/deploy/log/events/" + $scope.item.id, function (data) {
            $scope.steps = data.data;
            for (let i = 0; i < data.data.length; i++) {
                if (data.data[i].status !== 'running' && data.data[i].status !== 'pending') {
                    $interval.cancel($scope.timer);
                }
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
        $scope.getTestEvent();
    }, 2000);

    $scope.$on("$destroy", function () {
        $interval.cancel($scope.timer);
    });

});