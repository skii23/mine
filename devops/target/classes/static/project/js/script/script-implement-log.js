ProjectApp.controller('ScriptImplementLogController', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $timeout,$interval,UserService) {

    // 定义搜索条件
    $scope.conditions = [
        {
            key: "scriptId",
            name: "脚本",
            directive: "filter-select-virtual",
            url: "condition/script",
            convert: {value: "id", label: "name"}
        }];

    if (UserService.isAdmin()) {
        $scope.conditions.push({
            key: "organizationId",
            name: "组织",
            directive: "filter-select-virtual",
            url: "condition/organization",
            convert: {value: "id", label: "name"}
        });
        $scope.conditions.push({
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: {value: "id", label: "name"}
        })
    }
    if (UserService.isOrgAdmin()) {
        $scope.conditions.push({
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: {value: "id", label: "name"}
        })
    }

    // 用于传入后台的参数
    $scope.filters = [];

    // 全选按钮，添加到$scope.columns


    $scope.columns = [
        {value: "集群", key: "cluster_name"},
        {value: "主机组", key: "cluster_role_name"},
        {value: "主机", key: "instance_name"},
        {value: "脚本名称", key: "script_name"},
        {value: "开始执行时间", key: "created_time"},
        {value: "结束执行时间", key: "completed_Time"},
        {value: "执行状态", key: "status"},
    ];


    $scope.timer = $interval(function () {
        let condition = FilterSearch.convert($scope.filters);
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }

        HttpUtils.post("script/implement/log/list/" + $scope.pagination.page + "/" + $scope.pagination.limit, condition, function (response) {
            let items = response.data.listObject;
            angular.forEach(items, function (newItem) {
                angular.forEach($scope.items, function (item) {
                    if (newItem.id === item.id) {
                        item.status = newItem.status;
                        item.completedTime=newItem.completedTime;
                        item.result=newItem.result;
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
            if (item.status !== 'complete') {
                sum++;
            }
        });
        return sum > 0;
    };

    $scope.$on("$destroy", function () {
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
        HttpUtils.paging($scope, 'script/implement/log/list', condition)
    };

    $scope.showDetail = function (item, type) {
        $scope.detail = null;
        $scope.type = type;

        $scope.title = type === 'content' ? '脚本内容' : '输出结果';


        $mdDialog.show({
            templateUrl: 'project/html/script/script-log-detail.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false
        });

        $scope.detailLoding = HttpUtils.get("script/implement/log/detail/" + item.id, function (data) {
            var result = data.data;
            $timeout(function () {
                $scope.cmOption = {
                    lineNumbers: true,
                    indentWithTabs: true,
                    theme: 'bespin',
                    mode: 'shell',
                    readOnly: true
                };
            }, 1000).then(function () {
                $scope.detail = result;
            });
            $timeout.cancel(a);
        });


    };
    $scope.closeDetail = function () {
        $mdDialog.cancel();
    };
    $scope.list();




});
