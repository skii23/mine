ProjectApp.controller("AnsibleRoleCtrl", function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state, $document) {
    // 定义搜索条件
    $scope.conditions = [
        // {key: "no", name: "集群名称", directive: "filter-input"},
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
        $scope.infoUrl = 'project/html/role/script-role-infomation.html' + '?_t=' + Math.random();
        $scope.toggleInfoForm(true);
    };


    $scope.columns = [
        $scope.first,
        {value: "play名称", key: "name", width: "20%", sort: false},
        {value: "git地址", key: "url", width: "20%"},
        {value: "版本", key: "version", width: "10%"},
        {value: "创建时间", key: "created_time", width: "15%"},
        {value: "描述", key: "comment", sort: false},
    ];


    //绑定click事件
    $document.on("click", function (event) {
            var inTable = false, inSidenav = false, isDialog = false;
            $.each($(event.target).parents(), function (index, item) {
                if (item.nodeName.toUpperCase() === 'TABLE') {
                    inTable = true;
                }
                if (item.nodeName.toUpperCase() === 'MD-SIDENAV') {
                    inSidenav = true;
                }
                if (item.nodeName.toUpperCase() === 'MD-DIALOG') {
                    isDialog = true;
                }
            });
            $.each($(event.target), function (index, item) {
                if (item.nodeName.toUpperCase() === 'TABLE') {
                    inTable = true;
                }
                if (item.id.toUpperCase() === 'INFO_FORM') {
                    inSidenav = true;
                }
                if (item.nodeName.toUpperCase() === 'MD-DIALOG') {
                    isDialog = true;
                }
                if ($(item).attr('name') === 'deleteTagNode') {
                    isDialog = true;
                }
            });
            if (!inTable && !inSidenav && !isDialog) {
                $scope.closeInformation();
            }
        }
    );
    $scope.enableClick = function (event) {
        event.stopPropagation();
    };

    $scope.$on("$destroy", function () {
        //清除配置,不然scroll会重复请求
        $document.off('click');
    });


    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.item = {};
        $scope.formUrl = 'project/html/role/script-role-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };
    $scope.delete = function (item) {
        var deleteList = [];
        if (item) {
            deleteList.push(item.id)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    deleteList.push(item.id)
                }
            });
        }

        if (deleteList.length === 0) {
            Notification.info("请选择要删除的对象！");
        }

        Notification.confirm("确定删除？", function () {
            HttpUtils.post("role/delete", deleteList, function (data) {
                if (data.success) {
                    Notification.success("删除成功");
                    $scope.list();
                }
            });
        }, function () {
            Notification.info("已取消");
        });
    };


    $scope.edit = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/role/script-role-edit.html' + '?_t=' + Math.random();
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
        HttpUtils.paging($scope, 'role/list', condition)
    };
    $scope.list();
}).controller('AnsibleRoleAddCtrl', function ($scope, Notification, HttpUtils) {
    $scope.item = {};

    $scope.Loading = $scope.submit = function () {
        $scope.loading = HttpUtils.post("role/create", $scope.item, function (data) {
            Notification.success("保存成功！");
            $scope.toggleForm();
            $scope.list();
        })
    }
}).controller('AnsibleRoleEditCtrl', function ($scope, Notification, HttpUtils) {
    $scope.Loading = $scope.submit = function () {
        $scope.loading = HttpUtils.post("role/create", $scope.item, function (data) {
            Notification.success("保存成功！");
            $scope.toggleForm();
            $scope.list();
        })
    }
});