ProjectApp.controller('VariableCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state) {

    // 定义搜索条件
    $scope.conditions = [
        {key: "name", name: "名称", directive: "filter-contains"},
        {
            key: "clusterId",
            name: "集群",
            directive: "filter-select-virtual",
            url: "condition/cluster",
            convert: {value: "id", label: "name"}
        },
        {
            key: "clusterRoleId",
            name: "主机组",
            directive: "filter-select-virtual",
            url: "condition/clusterRole",
            convert: {value: "id", label: "name"}
        },
        {
            key: "cloudServerId",
            name: "主机",
            directive: "filter-select-virtual",
            url: "condition/cloudServer",
            convert: {value: "id", label: "instanceName"}
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
        $scope.infoUrl = 'project/html/cluster/cluster-infomation.html' + '?_t=' + Math.random();
        $scope.toggleInfoForm(true);
    };

    $scope.columns = [
        {value: "名称", key: "name", width: "15%"},
        {value: "适用范围", key: "resource_type", width: "15%"},
        {value: "适用资源", key: "resource_name", width: "15%"},
        {value: "值", key: "value", width: "25%"},
        {value: "创建时间", key: "created_time"},
    ];


    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.item = {};
        $scope.formUrl = 'project/html/variable/variable-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };
    $scope.delete = function (item) {
        Notification.confirm("确定删除？", function () {
            HttpUtils.post("variable/delete", item.id, function (data) {
                if (data.success) {
                    Notification.success("删除成功");
                    $scope.list();
                }
            });
        });
    };
    $scope.edit = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/variable/variable-edit.html' + '?_t=' + Math.random();
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
        HttpUtils.paging($scope, 'variable/list', condition)
    };
    $scope.list();
});

ProjectApp.controller('VariableAddCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $timeout) {


    $scope.resourceTypes = [
        {"id": "cluster", "name": "集群"},
        {"id": "clusterRole", "name": "主机组"},
        {"id": "cloudServer", "name": "主机"}
    ];


    $scope.loading = HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
    });

    $scope.loading = HttpUtils.get("clusterRole/list", function (data) {
        $scope.clusterRoles = data.data;
    });

    $scope.loading = HttpUtils.post("server/list", {}, function (data) {
        $scope.cloudServers = data.data;
    });


    $scope.submit = function () {
        $scope.loading = HttpUtils.post("variable/save", $scope.item, function () {
            Notification.info("添加成功！");
            $scope.list();
            $scope.toggleForm();
        })
    };

});

ProjectApp.controller('VariableEditCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $timeout) {
    $scope.editItem = angular.copy($scope.item);
    $scope.resourceTypes = [
        {"id": "cluster", "name": "集群"},
        {"id": "clusterRole", "name": "主机组"},
        {"id": "cloudServer", "name": "主机"}
    ];


    $scope.loading = HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
    });

    $scope.loading = HttpUtils.get("clusterRole/list", function (data) {
        $scope.clusterRoles = data.data;
    });

    $scope.loading = HttpUtils.post("server/list", {}, function (data) {
        $scope.cloudServers = data.data;
    });


    $scope.submit = function () {
        $scope.loading = HttpUtils.post("variable/save", $scope.editItem, function () {
            Notification.info("修改成功！");
            $scope.list();
            $scope.toggleForm();
        })
    };

});
