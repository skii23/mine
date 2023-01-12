ProjectApp.controller('ClusterRoleCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $state,UserService) {

    // 定义搜索条件
    $scope.conditions = [
        {key: "name", name: "名称", directive: "filter-contains"},
        {
            key: "clusterId",
            name: "集群",
            directive: "filter-select-virtual",
            url: "condition/cluster",
            convert: {value: "id", label: "name"}
        }

    ];

    $scope.cluster = angular.fromJson(sessionStorage.getItem("cluster"));
    sessionStorage.removeItem("cluster");
    // 用于传入后台的参数
    $scope.filters = [];

    if ($scope.cluster) {
        $scope.filters.push({
            key: "clusterId",
            name: "集群名称",
            value: $scope.cluster.id,
            label: $scope.cluster.name,
            default: false,
            operator: "="
        })
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

    $scope.showDetail = function (item) {
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
        $scope.infoUrl = 'project/html/cluster-role/cluster-role-infomation.html' + '?_t=' + window.appversion;
        $scope.toggleInfoForm(true);
    };
    $scope.goServer = function (item) {
        sessionStorage.setItem("clusterRole", angular.toJson(item));
        $state.go("server");
    };


    $scope.columns = [
        {value: "主机组名称", key: "name", width: "20%"},
        {value: "集群名称", key: "cluster_name", width: "20%"},
        {value: "主机数量", key: "countServer", width: "10%"},
        {value: "创建时间", key: "created_time"},
    ];

    if (UserService.isAdmin()) {

        $scope.columns.splice(3, 0, {value: "工作空间", key: "workspaceName", width: "15%"});
        $scope.columns.splice(3, 0, {value: "组织", key: "organizationName", width: "15%"});

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
        $scope.columns.splice(3, 0, {value: "工作空间", key: "workspaceName", width: "15%"});
        $scope.conditions.push({
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: {value: "id", label: "name"}
        })
    }

    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.item = {};
        $scope.formUrl = 'project/html/cluster-role/cluster-role-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };
    $scope.scriptImplement = function (item) {
        $scope.params = {};
        $scope.params.clusterId = item.clusterId;
        $scope.params.clusterRoleId = item.id;
        $scope.formUrl = 'project/html/script/script_implement_public.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };
    $scope.delete = function (item) {
        Notification.confirm("确定删除？", function () {
            HttpUtils.post("clusterRole/delete", item.id, function (data) {
                if (data.success) {
                    Notification.success("删除成功");
                    $scope.list();
                }
            });
        });
    };
    $scope.edit = function (item) {
        $scope.item = angular.copy(item);
        $scope.formUrl = 'project/html/cluster-role/cluster-role-edit.html' + '?_t=' + window.appversion;
        $scope.toggleForm();
    };


    $scope.list = function (sortObj) {
        var condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, 'clusterRole/list', condition)
    };
    $scope.list();

});


ProjectApp.controller('ClusterRoleAddCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $http, CommonService) {

    $scope.loading = HttpUtils.get("cluster/list",function (data) {
        let result = data.data;
        $scope.clusters = data.data;
    });

    $scope.submit = function () {
        $scope.loading = $http.post("clusterRole/save", $scope.item).then(function (data) {
            var data = data.data;
            if (data.success) {
                Notification.success("保存成功");
                $scope.toggleForm();
                $scope.list();
            } else {
                Notification.warn(data.message)
            }
        })
    }

});

ProjectApp.controller('ClusterRoleEditCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $http, CommonService) {
    $scope.loading = CommonService.getClusters().then(function (data) {
        var data = data.data;
        $scope.clusters = data.data;
    });

    $scope.submit = function () {
        $scope.loading = $http.post("clusterRole/update", $scope.item).then(function (data) {
            var data = data.data;
            if (data.success) {
                Notification.success("保存成功");
                $scope.toggleForm();
                $scope.list();
            } else {
                Notification.warn(data.message)
            }
        })
    }

});

ProjectApp.controller('ClusterRoleInfoController', function ($scope, $filter) {

    $scope.info = [
        {label: "名称", name: "name"},
        {label: "集群名称", name: "clusterName"},
        {label: "描述", name: "description"},
        {label: "创建时间", name: "createdTime", date: true}
    ];

    $scope.getValueOrDefault = function (item, info, defaultValue) {
        if (item && item[info.name] && item[info.name] !== '') {
            return info.date ? $scope.formatTime(item[info.name]) : item[info.name];
        } else {
            return defaultValue;
        }
    };

    $scope.formatTime = function (time, format) {
        return $filter('date')(time, format || 'yyyy-MM-dd HH:mm');
    };

});

ProjectApp.controller('ClusterRoleServerAddController', function ($scope, $filter) {

});




