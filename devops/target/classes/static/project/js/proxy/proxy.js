ProjectApp.controller('ProxyCtrl', function ($scope, $mdDialog, UserService, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state) {

    // 定义搜索条件
    $scope.conditions = [];


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


    $scope.columns = [
        {value: "代理ip", key: "ip"},
        {value: "端口", key: "port"},
        {value: "标签说明", key: "tag", width: "10%"},
        {value: "用户名", key: "username", sort: false, width: "20%"},
        {value: "可见级别", key: "scope", width: "10%"},
        {value: "创建时间", key: "created_time"},

    ];


    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.formUrl = 'project/html/proxy/proxy-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };


    $scope.checkRole = function (item) {

        if (item.scope === 'global') {
            if (UserService.isOrgAdmin()) {
                return false;
            }

            if (UserService.isAdmin()) {
                return true;
            }
        } else {
            return true;
        }

    };

    $scope.delete = function (item) {
        Notification.confirm("确定删除？", function () {
            HttpUtils.post("proxy/delete", item.id, function (data) {
                Notification.success("删除成功");
                $scope.list();
            });
        });
    };
    $scope.edit = function (item) {
        $scope.item = angular.copy(item);
        $scope.formUrl = 'project/html/proxy/proxy-edit.html' + '?_t=' + Math.random();
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
        HttpUtils.paging($scope, 'proxy/list', condition)
    };
    $scope.list();


});


ProjectApp.controller('ProxyEditCtrl', function ($scope, $http, HttpUtils, Notification, eyeService) {


    $scope.view = function (password, eye) {
        eyeService.view("#" + password, "#" + eye);
    };

    $scope.submit = function () {
        $scope.loading = HttpUtils.post("proxy/create", $scope.item, function () {
            Notification.success("保存成功");
            $scope.toggleForm();
            $scope.list();
        });
    };


});


ProjectApp.controller('ProxyAddCtrl', function ($scope, $http, HttpUtils, Notification, eyeService) {
    $scope.item = {};
    $scope.view = function (password, eye) {
        eyeService.view("#" + password, "#" + eye);
    };

    $scope.submit = function () {
        $scope.loading = HttpUtils.post("proxy/create", $scope.item, function () {
            Notification.success("保存成功");
            $scope.toggleForm();
            $scope.list();
        });
    };
});

