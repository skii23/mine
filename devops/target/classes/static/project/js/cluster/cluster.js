ProjectApp.controller('ClusterCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state,UserService) {

    // 定义搜索条件
    $scope.conditions = [
        {key: "name", name: "名称", directive: "filter-contains"},
        {
            key: "envValueId",
            name: "环境",
            directive: "filter-select-virtual",
            url: "condition/envTag",
            convert: {value: "id", label: "tagValueAlias"}
        },
        {
            key: "systemValueId",
            name: "业务",
            directive: "filter-select-virtual",
            url: "condition/bizTag",
            convert: {value: "id", label: "tagValueAlias"}
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

    $scope.showDetail = function (item, tag, event) {

        // 点击2次关闭
        if ($scope.selected === item.$$hashKey) {
            $scope.closeInformation();
            return;
        }
        $scope.selected = item.$$hashKey;
        $scope.detail = item;
        $scope.active = tag;
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
        {value: "集群名称", key: "name", width: "25%", sort: false},
        {value: "主机组数量", key: "countClusterRole", width: "15%"},
        {value: "主机数量", key: "countServer", width: "15%"},
        {value: "环境信息", key: "envValueId", sort: false, width: "15%"},
        {value: "描述", key: "description", width: "15%", sort: false},
        {value: "创建时间", key: "created_time"}
    ];


    if (UserService.isAdmin()) {
        $scope.columns.splice(3, 0, {value: "组织", key: "organizationName", width: "15%"});
        $scope.columns.splice(4, 0, {value: "工作空间", key: "workspaceName", width: "15%"});

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
        $scope.formUrl = 'project/html/cluster/cluster-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };
    $scope.delete = function (item) {
        Notification.confirm("确定删除？", function () {
            HttpUtils.post("cluster/delete", item.id, function (data) {
                if (data.success) {
                    Notification.success("删除成功");
                    $scope.list();
                }
            });
        });
    };


    $scope.edit = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/cluster/cluster-edit.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.goClusterRole = function (item) {
        sessionStorage.setItem("cluster", angular.toJson(item));
        $state.go("clusterRole");
    };
    $scope.goServer = function (item) {
        sessionStorage.setItem("cluster", angular.toJson(item));
        $state.go("server");
    };

    $scope.scriptImplement = function (item) {
        $scope.params = {};
        $scope.params.clusterId = item.id;
        $scope.formUrl = 'project/html/script/script_implement_public.html' + '?_t=' + Math.random();
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
        HttpUtils.paging($scope, 'cluster/list', condition)
    };
    $scope.list();

});

ProjectApp.controller('ClusterAddController', function ($scope, $http, HttpUtils, Notification) {

    $scope.item = {
        tagMappings: []
    };
    $scope.init = function () {
        $scope.getOrganizations();
    };

    $scope.listTags = function () {
        $scope.loading = $http.post("tag/listAll", ["environment", "business"]).then(function (response) {
                $scope.tags = response.data.data;
            if ($scope.tags) {
                $scope.tags.forEach(item => {
                    if (item.tagKey === 'environment') {
                        if (!item.tagValues || item.tagValues.length === 0) {
                            item.tagValues = [{
                                    id:'all',
                                    tagValueAlias:'全部环境'
                                }];
                        }else {
                            item.tagValues.splice(0,0,{
                                id: 'all',
                                tagValueAlias: '全部环境'
                            });
                        }
                    }
                });
            }
            }, function (response) {
                Notification.danger(response);
            }
        );
    };

    $scope.getOrganizations = function () {
        $scope.loading = HttpUtils.get("condition/organization", function (data) {
            $scope.organizations = data.data;
        });
    };

    $scope.orgChange = function () {
        $scope.item.workspaceId = null;
        $scope.getWorkspaces();
    };

    $scope.getWorkspaces = function () {
        $scope.loading = HttpUtils.get("condition/workspace?organizationId=" + $scope.item.organizationId, function (data) {
            $scope.workspaces = data.data;
        });
    };


    $scope.submit = function () {
        let originItem = angular.copy($scope.item);
        if ($scope.item.tagMappings[0].tagValueId === 'all') {
            $scope.item.tagMappings = [];
        }
        $scope.loading = HttpUtils.post("cluster/save", $scope.item, function (data) {
            if (data.success) {
                $scope.toggleForm();
                $scope.list();
                Notification.success("保存成功");
            } else {
                $scope.item = originItem;
                Notification.warn(data.message)
            }
        }, function (data) {
            $scope.item = originItem;
            Notification.warn(data.message)
        });
    };

    $scope.init();
    $scope.listTags();
});

ProjectApp.controller('ClusterEditController', function ($scope, $http, HttpUtils, Notification) {
    $scope.editItem = angular.copy($scope.item);

    $scope.submit = function () {
        HttpUtils.post("cluster/update", $scope.editItem, function (data) {
            if (data.success) {
                $scope.list();
                $scope.toggleForm();
                Notification.success("保存成功");
            } else {
                Notification.warn(data.message)
            }
        }, function (data) {
            Notification.warn(data.message)
        });
    };

});

ProjectApp.controller('ClusterInfoController', function ($scope, $filter) {
    $scope.info = [
        {label: "名称", name: "name"},
        {label: "创建时间", name: "createdTime", date: true},
        {label: "描述", name: "description"},

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


ProjectApp.controller('ClusterTagController', function ($scope, $http, $log, HttpUtils, Notification, CommonService) {
    $scope.listTags = function () {
        $scope.loading = $http.post("tag/listAll", ["environment", "business"]).then(function (response) {
                $scope.tags = response.data.data;
                $scope.getValues();
            }, function (response) {
                $log.error(response);
            }
        );
    };

    $scope.addItem = [];
    $scope.getValues = function () {
        var data = {
            resourceId: $scope.detail.id
        };
        $scope.loading = $http.post("tag/mapping/list", data, null).then(function (response) {
                var addItem = response.data.data;
                var tags = $scope.tags;
                for (var i = 0; i < addItem.length; i++) {
                    var item = addItem[i];
                    for (var j = 0; j < tags.length; j++) {
                        if (tags[j].tagKey === item.tagKey) {
                            item.values = tags[j].tagValues;
                            break;
                        }
                    }
                }
                $scope.addItem = addItem;
                $scope.originItem = angular.copy($scope.addItem);
                $scope.editing = false;
            }, function (response) {
                $log.error(response);
            }
        );
    };
    $scope.$on('showDetail', $scope.getValues);

    $scope.addTag = function () {
        $scope.editing = true;
        $scope.addItem.push({});
    };

    $scope.changeTag = function (item) {
        var tags = $scope.tags;

        for (var i = 0; i < tags.length; i++) {
            if (tags[i].tagKey === item.tagKey) {
                item.values = tags[i].tagValues;
                break;
            }
        }
        $scope.checkDuplicate();
        $scope.checkDifferent();
    };

    $scope.deleteTag = function (index) {
        $scope.addItem.splice(index, 1);
        $scope.editing = true;

        if ($scope.addItem.length === $scope.originItem.length) {
            if ($scope.addItem.length === 0) {
                $scope.editing = false;
                return;
            }
            $scope.checkDifferent();
        }
        $scope.checkDuplicate();
    };

    $scope.checkDifferent = function () {
        var different = false;
        if ($scope.addItem.length !== $scope.originItem.length) {
            $scope.editing = true;
            return;
        }
        for (var i = 0; i < $scope.addItem.length; i++) {
            var item = $scope.addItem[i];
            var found = false;
            var originItem = null;
            for (var j = 0; j < $scope.originItem.length; j++) {
                originItem = $scope.originItem[j];
                if (item.tagKey === originItem.tagKey) {
                    found = true;
                    break;
                }
            }
            if (!found || (found && item.tagValueId !== originItem.tagValueId)) {
                different = true;
                break;
            }
        }
        if (!different) {
            $scope.editing = false;
        }
    };

    $scope.checkDuplicate = function () {
        var addItem = $scope.addItem;

        for (var i = 0; i < addItem.length; i++) {
            addItem[i].duplicate = false;
        }
        $scope.duplicate = false;
        for (var i = 0; i < addItem.length - 1; i++) {
            addItem[i].duplicates = false;
            for (var j = i + 1; j < addItem.length; j++) {
                if (addItem[i].tagKey === addItem[j].tagKey) {
                    addItem[i].duplicate = true;
                    addItem[j].duplicate = true;
                    $scope.duplicate = true;
                }
            }
        }
    };

    $scope.checkRequired = function (tag) {
        for (var i = 0; i < $scope.tags.length; i++) {
            if (tag.tagKey === $scope.tags[i].tagKey) {
                return $scope.tags[i].required;
            }
        }
        return false;
    };

    $scope.changeTagValue = function () {
        $scope.editing = true;
        $scope.checkDifferent();
    };

    $scope.reset = function () {
        $scope.addItem = angular.copy($scope.originItem);
        $scope.editing = false;
    };

    $scope.getDiff = function () {
        var itemToDelete = [];

        var addItem = angular.copy($scope.addItem);
        var originItem = angular.copy($scope.originItem);

        for (var i = 0; i < originItem.length; i++) {
            var item = originItem[i];
            var deleted = true;
            for (var j = 0; j < addItem.length; j++) {
                if (item.tagKey === addItem[j].tagKey && item.tagValueId === addItem[j].tagValueId) {
                    addItem.splice(j, 1);
                    deleted = false;
                    break;
                }
            }
            if (deleted) {
                itemToDelete.push(item);
            }
        }

        var data = [];
        for (var i = 0; i < addItem.length; i++) {
            var item = addItem[i];
            data.push({
                resourceId: $scope.detail.id,
                resourceType: "DEVOPS_CLUSTER",
                tagKey: item.tagKey,
                tagValueId: item.tagValueId
            });
        }
        for (var i = 0; i < itemToDelete.length; i++) {
            var item = itemToDelete[i];
            data.push({
                resourceId: $scope.detail.id,
                resourceType: "DELETE",
                tagKey: item.tagKey,
                tagValueId: item.tagValueId
            });
        }
        return data;
    };

    $scope.saveTag = function () {
        var data = $scope.getDiff();
        $scope.loading = HttpUtils.post("tag/mapping/save", data, function () {
            Notification.success("保存成功");
            $scope.getValues();
            $scope.editing = false;
        }, function (response) {
            Notification.alert(response.message);
        })
    };

    $scope.listTags();


});
