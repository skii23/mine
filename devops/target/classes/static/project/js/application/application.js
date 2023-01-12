ProjectApp.controller('ApplicationCtrl', function ($scope, UserService, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state, UserService, $document) {

    //重置部署页面的url
    delete $scope.deployUrl;

    // 定义搜索条件
    $scope.conditions = [
        { key: "name", name: "名称", directive: "filter-contains" },
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
        $scope.infoUrl = 'project/html/application/application-infomation.html' + '?_t=' + Math.random();
        $scope.toggleInfoForm(true);
    };

    $scope.export = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj || $scope.sort) {
            $scope.sort = sortObj || $scope.sort;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        let columns = $scope.columns.filter(function (c) {
            return c.checked !== false && c.key;
        });

        $scope.loadingLayer = HttpUtils.download('application/export', {
            columns: columns,
            params: condition
        }, '应用列表.xlsx', 'application/octet-stream');
    };

    $scope.columns = [
        { value: "应用名称", key: "name", width: "15%" },
        { value: "版本数", key: "versionCount", width: "15%" },
        { value: "部署次数", key: "deployCount", width: "15%" },
        { value: "环境信息", key: "systemTagValueAlias", width: "15%" },
        { value: "可见范围", key: "scope", width: "10%" },
        { value: "描述", key: "scope", width: "10%" },
        { value: "创建时间", key: "createdTime", width: "10%" }
    ];

    if (UserService.isAdmin()) {
        $scope.columns.splice(8, 0, { value: "组织", key: "organizationName", width: "15%", sort: false });
        $scope.columns.splice(9, 0, { value: "工作空间", key: "workspaceName", width: "15%", sort: false });

        $scope.conditions.push({
            key: "organizationId",
            name: "组织",
            directive: "filter-select-virtual",
            url: "condition/organization",
            convert: { value: "id", label: "name" }
        });
        $scope.conditions.push({
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: { value: "id", label: "name" }
        });
    }

    if (UserService.isOrgAdmin()) {
        $scope.columns.splice(5, 0, { value: "工作空间", key: "organizationName", width: "15%", sort: false });
        $scope.conditions.push({
            key: "workspaceId",
            name: "工作空间",
            directive: "filter-select-virtual",
            url: "condition/workspace",
            convert: { value: "id", label: "name" }
        });
    }


    $scope.checkRole = function (scope) {
        let flag = false;
        if (UserService.isAdmin() && scope === 'global') {
            flag = true;
        } else if (UserService.isOrgAdmin() && scope === 'org') {
            flag = true;
        } else if (UserService.isUser() && scope === 'workspace') {
            flag = true;
        }
        return flag;
    };


    $scope.goVersions = function (item) {
        if (!item.versionCount) {
            return;
        }
        $scope.applicationId = item.id;
        $mdDialog.show({
            templateUrl: 'project/html/application/version/application-version-list-dialog.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false,
            fixedGutter: false,
            bindToController: true
        });
    };

    $scope.getDeployHistory = function (item) {
        sessionStorage.setItem('application', angular.toJson(item));
        $state.go("application_deploy");
    };


    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.item = {};
        $scope.formUrl = 'project/html/application/application-add.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.edit = function (item) {
        $scope.item = angular.copy(item);
        $scope.formUrl = 'project/html/application/application-edit.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };


    $scope.delete = function (item) {

        Notification.confirm("删除应用将删除所有相关联应用版本，是否确认？", function () {
            HttpUtils.post("application/delete", item.id, function (data) {
                Notification.success("删除成功");
                $scope.list();
            });
        });
    };

    $scope.list = function (sortObj) {
        var condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, 'application/list', condition)
    };

    if (sessionStorage.getItem('appName')) {
        $scope.$evalAsync(
            function ($scope) {
                $scope.filters = [{ key: 'name', name: "名称", value: sessionStorage.getItem('appName') }];
                sessionStorage.removeItem('appName');
                $scope.list();
            }
        );
    } else {
        $scope.list();
    }
}).controller('ApplicationAddCtrl', function ($scope, $http, HttpUtils, Notification) {

    $scope.item = {
        application: {},
        applicationRepositorySettings: []
    };

    $scope.addSetting = function () {
        $scope.item.applicationRepositorySettings.push({});
    };

    $scope.deleteSetting = function (index) {
        $scope.item.applicationRepositorySettings.splice(index, 1);
    };

    $scope.disable = function disable() {

        if ($scope.item.applicationRepositorySettings && $scope.envs) {
            if ($scope.item.applicationRepositorySettings.length >= $scope.envs.length) {
                return false;
            }
        }
        return true;
    };


    $scope.checkSave = function () {

        if ($scope.item.applicationRepositorySettings.length < 1) {
            return true
        }
        return false;
    };

    $scope.clusterChange = function () {
        $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
            $scope.item.clusterRoleId = null;
            $scope.clusterRoles = data.data;
            if ($scope.clusterRoles.length !== 0)
                $scope.clusterRoles = [{ name: "全部主机组", id: "ALL" }].concat($scope.clusterRoles);
        });
    };

    $scope.loading = HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
    }, function (data) {
        Notification.warn(data.message)
    });

    $scope.loading = HttpUtils.get("application/test/prod/list", function (data) {
        $scope.products = data.data;
    }, function (data) {
        Notification.warn(data.message)
    });

    $scope.loading = $http.get("application/setting/env/list").then(function (data) {
        var data = data.data;
        if (data.success) {
            $scope.envs = data.data;
            $scope.envs.push({ "id": "all", "tagValueAlias": "全部环境" })
        } else {
            Notification.warn(data.message)
        }
    });

    $scope.loading = HttpUtils.post("repository/listAll", {}, function (data) {
        $scope.repositories = data.data;
    }, function (data) {
        Notification.warn(data.message)
    });


    $scope.submit = function () {
        HttpUtils.post("application/save", $scope.item, function (data) {
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

}).controller('ApplicationEditCtrl', function ($scope, $http, HttpUtils, Notification) {
    $scope.addSetting = function () {
        $scope.item.applicationRepositorySettings.push({});
    };

    $scope.deleteSetting = function (index) {
        $scope.item.applicationRepositorySettings.splice(index, 1);
    };

    $scope.loadClusterRoles = function () {
        $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            if ($scope.clusterRoles.length !== 0) {
                $scope.clusterRoles = [{ name: "全部主机组", id: "ALL" }].concat($scope.clusterRoles);
            }
        }, function (data) {
            Notification.warn(data.message)
        });
    };

    $scope.clusterChange = function () {
        $scope.item.clusterRoleId = null;
        $scope.loadClusterRoles();
    };

    $scope.loading = HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
        $scope.loadClusterRoles();
    });

    $scope.loading = $http.get("application/setting/env/list").then(function (data) {
        var data = data.data;
        if (data.success) {
            $scope.envs = data.data;
            $scope.envs.push({ "id": "all", "tagValueAlias": "全部环境" })
        } else {
            Notification.warn(data.message)
        }
    });

    $scope.loading = HttpUtils.post("repository/listAll", {}, function (data) {
        $scope.repositories = data.data;
    }, function (data) {
        Notification.warn(data.message)
    });

    $scope.disable = function disable() {

        if ($scope.item.applicationRepositorySettings && $scope.envs) {
            if ($scope.item.applicationRepositorySettings.length >= $scope.envs.length) {
                return false;
            }
        }
        return true;
    };

    $scope.submit = function () {
        HttpUtils.post("application/update", $scope.item, function (data) {
            if (data.success) {
                $scope.list();
                $scope.toggleForm();
                Notification.success("保存成功");
            } else {
                Notification.warn(data.message);
            }
        })
    }
}).controller('ApplicationInfoCtrl', function ($scope, $http, HttpUtils, Notification, $filter) {
    $scope.info = [
        { label: "名称", name: "name" },
        { label: "可见范围", name: "scope", scope: true },
        { label: "创建时间", name: "createdTime", date: true },
        { label: "描述", name: "description" },
    ];

    $scope.getValueOrDefault = function (item, info, defaultValue) {
        if (item && item[info.name] && item[info.name] !== '' && info.date) {
            return $scope.formatTime(item[info.name]);
        } else if (item && item[info.name] && item[info.name] !== '' && info.scope) {
            return $scope.formatScope(item[info.name]);
        } else if (item && item[info.name] && item[info.name] !== '') {
            return item[info.name];
        } else {
            return defaultValue;
        }
    };

    $scope.formatTime = function (time, format) {
        return $filter('date')(time, format || 'yyyy-MM-dd HH:mm');
    };

    $scope.formatScope = function (scope) {
        return $filter('ScopeFilter')(scope)
    }
}).controller('ApplicationTagCtrl', function ($scope, $http, $log, HttpUtils, Notification) {
    $scope.listTags = function () {
        $scope.loading = $http.post("tag/listAll", ["business"]).then(function (response) {
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
                resourceType: "APPLICATION",
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
ProjectApp.controller('ApplicationVersionDialogCtrl', function ($scope, HttpUtils, FilterSearch, $mdDialog, Notification, $mdSidenav, $rootScope) {

    $scope.conditions = [];
    $scope.filters = [];
    $scope.conditions.push({
        key: "name",
        name: "版本号",
        directive: "filter-contains"
    });

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
        { value: "版本号", key: "name", width: "20%" },
        { value: "存放路径", key: "location", width: "15%" },
        { value: "创建时间", key: "created_time", width: "15%" },
        { value: "描述", key: "description", width: "15%" },
        { value: "环境信息", key: "envInfo", width: "15%" },
        { value: "最后一次部署", key: "last_deployment_time", width: "25%" },
    ];

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        condition.applicationId = $scope.applicationId;
        HttpUtils.paging($scope, 'application/version/list', condition)
    };

    $scope.init = function () {
        $scope.list();
        HttpUtils.get('application/version/getLatestVersion?applicationId=' + $scope.applicationId, function (data) {
            $scope.latestVersion = data.data;
        });
        HttpUtils.post('jenkins/params/getParams', { paramKey: 'ocp_portal' }, function (data) {
            $scope.ocpPortal = data.data[0].paramValue;
            if (!$scope.ocpPortal.endsWith("/")) {
                $scope.ocpPortal += "/";
            }
        });
    };

    $scope.delete = function (item) {
        Notification.confirm("确定删除？", function () {
            HttpUtils.post("application/version/delete", item.id, function (data) {
                Notification.success("删除成功");
                $scope.list();
            });
        });
    };

    $scope.deploy = function (item) {
        $scope.loadingLayer = HttpUtils.get('application/deploy/checkDeploy', function (data) {
            if (data.data) {
                HttpUtils.get("repository/" + item.applicationRepositoryId, function (version) {
                    if (version.data && version.data.type.toLowerCase() === "harbor") {
                        let tmp = item.location.split(":")[0].split("/");
                        window.open($scope.ocpPortal + "workload/deployments/" + tmp[2] + "?ns=" + tmp[1]);
                    } else {
                        $rootScope.deployUrl = 'project/html/application/version/application-version-deploy.html' + '?_t=' + Math.random();
                        $rootScope.item = item;
                        $mdSidenav('deployForm').open();
                        angular.element('#deployForm').css('z-index', 81);
                    }
                });
            } else {
                Notification.danger('当前时间无法部署应用！如需部署请联系管理员或组织管理员解决！');
            }
        });
    };

    $scope.close = function () {
        $mdDialog.cancel();
    };

    $scope.init();
});


