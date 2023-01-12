ProjectApp.controller('ScriptFilterCtrl', function ($scope, Notification, FilterSearch, HttpUtils) {
// 定义搜索条件
    $scope.conditions = [
        {key: "name", name: "脚本名称", directive: "filter-contains"}
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

    $scope.filterTypes = [
        {name: '关键字过滤', value: 'KEYWORD'},
        {name: '正则表达式', value: 'REGEX'}
    ];

    $scope.columns = [
        $scope.first,
        {value: "规则名称", key: "name", width: "15%"},
        {value: "规则类型", key: "type", width: "15%"},
        {value: "规则内容", key: "value", width: "10%", sort: false},
        {value: "创建时间", key: "create_time", width: "10%"},
        {value: "描述", key: "description", width: "10%", sort: false},
        {value: "是否激活", key: "active", width: "10%", sort: false}
    ];

    $scope.edit = function (item) {
        if (item.value) {
            item.values = item.value.split(',');
        }else {
            item.values = [];
        }
        $scope.item = angular.copy(item);
        $scope.isEdit = true;
        $scope.formUrl = 'project/html/script/script-filter-add.html?t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.create = function () {
        $scope.isEdit = false;
        $scope.item = {type: 'REGEX', values: [], active: true};
        $scope.formUrl = 'project/html/script/script-filter-add.html?t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.delete = function (item) {
        let items = [];
        if (item) {
            items.push(item);
        } else {
            $scope.items.forEach(function (obj) {
                if (obj.enable) {
                    items.push(obj);
                }
            });
        }
        if (items.length === 0) {
            Notification.info('请选择要删除的规则！');
        } else {
            Notification.confirm('确定要删除选中的规则？', function () {
                $scope.loading = HttpUtils.post('script/filter/delete', items, function (data) {
                    Notification.info('删除成功!');
                    $scope.list();
                });
            });
        }
    };

    $scope.changeFilterType = function () {
        if ($scope.item.type === 'KEYWORD') {
            $scope.item.values = [];
        } else {
            $scope.item.value = '';
        }
    };

    $scope.changeState = function (item, state) {
        item.active = state === 'true';
        $scope.loading = HttpUtils.post('script/filter/save', item, function () {
            $scope.list();
        });
    };

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, 'script/filter/list', condition);
    };

    $scope.list();
});

ProjectApp.controller('ScriptFilterAddCtrl', function ($scope, HttpUtils, Notification) {
    $scope.submit = function () {
        if ($scope.item.type === 'KEYWORD') {
            $scope.item.value = $scope.item.values.join(",");
        }
        $scope.loading = HttpUtils.post('script/filter/save', $scope.item, function (data) {
            Notification.info('保存成功！');
            $scope.list();
            $scope.toggleForm();
        });
    };
});