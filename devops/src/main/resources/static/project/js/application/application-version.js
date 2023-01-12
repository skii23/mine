ProjectApp.controller('ApplicationVersionCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state) {

    $scope.application = angular.fromJson(sessionStorage.getItem("application"));
    sessionStorage.removeItem("application");
// 定义搜索条件
    $scope.conditions = [];
    $scope.filters = [];
    if ($scope.application){
        $scope.filters.push({
            key: "applicationId",
            name: "应用",
            value: $scope.application.id,
            label: $scope.application.name,
            default: false,
        });
        $scope.conditions.push({
            key: "name",
            name: "版本号",
            directive: "filter-contains"
        });
    } else {
        $scope.conditions.push({
            key: "applicationId",
            name: "应用",
            directive: "filter-select-virtual",
            url: "condition/application",
            convert: {value: "id", label: "name"}
        },{
            key: "name",
            name: "版本号",
            directive: "filter-contains"
        });
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
        $scope.infoUrl = 'project/html/application/version/application-version-infomation.html' + '?_t=' + Math.random();
        $scope.toggleInfoForm(true);
    };

    $scope.columns = [
        {value: "版本号", key: "name", width: "20%"},
        {value: "应用", key: "application_name", width: "20%"},
        {value: "创建时间", key: "created_time", width: "15%"},
        {value: "描述", key: "description", width: "15%"},
        {value: "环境信息", key: "envInfo", width: "15%"},
        {value: "最后一次部署", key: "last_deployment_time", width: "25%"},
    ];


    $scope.goApplication = function () {
        $state.go("application")
    };
    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.formUrl = 'project/html/application/version/application-version-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };

    $scope.deploy = function (item) {
        $scope.loadingLayer = HttpUtils.get('application/deploy/checkDeploy', function (data) {
            if (data.data){
                HttpUtils.get("repository/" + item.applicationRepositoryId, function (version) {
                    if (version.data && version.data.type.toLowerCase() === "harbor") {
                        let tmp = item.location.split(":")[0].split("/");
                        window.open($scope.ocpPortal + "workload/deployments/" + tmp[2] + "?ns=" + tmp[1]);
                    }else {
                        $scope.item = item;
                        $scope.formUrl = 'project/html/application/version/application-version-deploy.html' + '?_t=' + Math.random();
                        $scope.toggleForm();
                    }
                });
            }else {
                Notification.danger('当前时间无法部署应用！如需部署请联系管理员或组织管理员解决！');
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


    $scope.enableClick = function (event) {
        event.stopPropagation();
    };

    $scope.edit = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/demo/cluster-role-add.html' + '?_t=' + window.appversion;
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
        HttpUtils.paging($scope, 'application/version/list', condition);
    };
    $scope.init = function () {
        $scope.list();
        HttpUtils.post('jenkins/params/getParams', {paramKey: 'ocp_portal'}, function (data) {
            $scope.ocpPortal = data.data[0].paramValue;
            if (!$scope.ocpPortal.endsWith("/")) {
                $scope.ocpPortal += "/";
            }
        });
    };

    if (sessionStorage.getItem('versionName')) {
        $scope.$evalAsync(
            function ($scope) {
                $scope.filters = [{ key: 'name', name: "版本号", value: sessionStorage.getItem('versionName') }];
                sessionStorage.removeItem('versionName');
                $scope.list();
            }
        );
    } else {
        $scope.list();
    }
});
ProjectApp.controller('ApplicationVersionAddCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http) {
    $scope.init = function () {
        $scope.item = {};
        $scope.getApplications();
    };


    $scope.getApplications = function () {
        $scope.loading = HttpUtils.get("application/list", function (data) {
            $scope.applications = data.data;
            if ($scope.application) {
                $scope.item['applicationId'] = $scope.application.id;
                $scope.appChange();
            }
        })
    };


    $scope.appChange = function () {
        $scope.item.environmentValueId = null;
        $scope.repo = null;
        $scope.item.location = null;
        $scope.getEnvValueIds();
    };


    $scope.onEnvChange = function () {
        $scope.fileTreeUrl = "application/version/file/tree?envId=" + $scope.item.environmentValueId + "&" + "applicationId=" + $scope.item.applicationId;
        let url = "repository/query?envId=" + $scope.item.environmentValueId + "&applicationId=" + $scope.item.applicationId;
        HttpUtils.get(url, function (data) {
            $scope.repo = data.data;
        });
        $scope.item.location = null;
    };
    $scope.checkLocationType = function (type) {
        if(type == 'fromArtifact'){
            $scope.fileTreeUrl = "application/version/file/tree?envId=" + $scope.item.environmentValueId + "&" + "applicationId=" + $scope.item.applicationId;
            $scope.repo.locationType = 'fromArtifact';
        }else if(type == 'fromProxy'){
            $scope.fileTreeUrl = "application/version/file/proxytree?envId=" + $scope.item.environmentValueId + "&" + "applicationId=" + $scope.item.applicationId;
            $scope.repo.locationType = 'fromProxy';
        }
    };


    $scope.getEnvValueIds = function () {
        $scope.loading = HttpUtils.get("application/env/list?applicationId=" + $scope.item.applicationId, function (data) {
            $scope.envs = data.data;
        })
    };


    $scope.submit = function () {
        $scope.loading = HttpUtils.post("application/version/save", $scope.item, function (data) {
            Notification.success("保存成功!");
            $scope.toggleForm();
            $scope.list();
        }, function (data) {
            Notification.warn(data.message);
        })
    };
    $scope.init();


});
ProjectApp.controller('ApplicationVersionInformationController', function ($scope, $filter) {
    $scope.info = [
        {label: "名称", name: "name"},
        {label: "创建时间", name: "createdTime", date: true},
        {label: "存放路径", name: "location"},
        {label: "描述", name: "description"}

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

ProjectApp.controller('ApplicationVersionTagCtrl', function ($scope, $filter, $http, HttpUtils, Notification) {
    $scope.listTags = function () {
        $scope.loading = $http.post("tag/listAll", ["environment"]).then(function (response) {
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
                resourceType: "APPLICATION_VERSION",
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
ProjectApp.controller('ApplicationVersionDeployCtrl', function ($scope, $mdDialog, FilterSearch, Notification, HttpUtils, $state, $mdSidenav) {

    $scope.toggleForm = function () {
        $mdSidenav('_side_form').close();
        if (angular.element('#deployForm').length) {
            $mdSidenav('deployForm').close();
        }
    };

    $scope.loadClusterRoles = function () {

        $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.deployment.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            if ($scope.clusterRoles.length !== 0) {
                $scope.clusterRoles = [{name: "全部主机组", id: "ALL"}].concat($scope.clusterRoles);
            }
        });
    };

    $scope.changeProduct = function (testProdId) {
        $scope.deployment.testPlanId = null;
        $scope.loading = HttpUtils.get("application/test/plan/list?prodId=" + testProdId, function (data) {
            $scope.plans = data.data;
        });
        $scope.deployment.testEvn = null;
        $scope.loading = HttpUtils.get("application/test/env/list?prodId=" + testProdId, function (data) {
            $scope.testEnvs = data.data;
        });
    };

    $scope.checkApiTest = function () {
        //校验应用绑定的信息是否存在
        $scope.valiApi = false;
        HttpUtils.get("application/test/check?id="+$scope.item.applicationId, function (data) {
            if (data.data) {
                $scope.checkhost = data.data.checkhost;
                if(data.data.flag){
                    $scope.valiApi = true;
                    $scope.testHost = data.data.testHost;
                    $scope.checkTestHostAvailable();
                }else{
                    $scope.tip = data.data.tip;
                }
            }
        });
    };

    $scope.loadCloudServers = function () {
        $scope.loading = HttpUtils.post("server/list", {
            "clusterId": $scope.deployment.clusterId,
            "clusterRoleId": $scope.deployment.clusterRoleId
        }, function (data) {
            $scope.servers = data.data;
            if ($scope.servers.length !== 0) {
                $scope.servers = [{"instanceName": "全部主机", "id": "ALL"}].concat($scope.servers);
            }
        });
    };
    //1.根据标签查询集群,appId,versionId
    $scope.init = function () {
        $scope.deployment = {
            applicationVersionId: $scope.item.id,
            clusterId: $scope.item.clusterId,
            clusterRoleId: $scope.item.clusterRoleId,
            cloudServerId: [],
            policy: 'all'
        };

        $scope.loading = HttpUtils.get("application/deploy/clusters/" + $scope.item.id, function (data) {
            $scope.clusters = data.data;
        });

        $scope.loading = HttpUtils.get("application/deploy/policies", function (data) {
            $scope.policies = data.data;
        });
        
        $scope.loading = HttpUtils.get("application/test/prod/list", function (data) {
            $scope.products = data.data;
        },function (data) {
            Notification.warn(data.message)
        });

        if ($scope.deployment.clusterId) {
            $scope.loadClusterRoles();
            if ($scope.deployment.clusterRoleId) {
                $scope.loadCloudServers();
            }
        };
        // $scope.checkApiTest();
        if(!$scope.deployment.pollingTimeoutSec){
            $scope.deployment.pollingTimeoutSec = 300;
        }
        if(!$scope.deployment.testPollingTimeoutSec){
            $scope.deployment.testPollingTimeoutSec = 150;
        }
        if(!$scope.deployment.testPollingFreqSec){
            $scope.deployment.testPollingFreqSec = 5;
        }
    };

    $scope.clusterChange = function () {
        $scope.deployment.clusterRoleId = null;
        $scope.deployment.cloudServerId = null;
        $scope.servers = null;
        $scope.loadClusterRoles();
    };

    $scope.clusterRoleChange = function () {
        $scope.deployment.cloudServerId = null;
        $scope.loadCloudServers();
    };

    $scope.checkSelect = function (ev) {
        if(ev.target.localName !== 'md-select') {
            angular.element('div.md-select-menu-container.md-active.md-clickable').removeClass('md-active').removeClass('md-clickable');
        }
    };

    $scope.submit = function () {
        $scope.deployment.cloudServerId = $scope.deployment.cloudServerId.join(',');
        $scope.loading = HttpUtils.post("application/deploy/save", $scope.deployment, function (data) {
            Notification.info("部署任务提交成功！");
            $scope.toggleForm();
            Notification.confirm('是否跳转到部署详情页面？', function () {
                $mdDialog.cancel();
                sessionStorage.setItem('applicationDeployment', angular.toJson(data.data));
                $state.go('application_deployment_log');
            });
        })
    };

    $scope.renderHtml = function (servers) {
        if (!servers || servers.length === 0) {
            return '';
        }
        let html = '';
        servers.forEach(server => {
            html += server.instanceName;
            html += server.managementIp ? '(' + server.managementIp + ')' : '';
            html += server.accountId ? server.connectable ? '可连接' : '不可连接' : '';
            html += '</br>';
        });
        return html;
    };

    $scope.setCloudServerId = function () {
        $scope.deployment.cloudServerId = [];
        if ($scope.selectedCloudServers && $scope.selectedCloudServers.length > 0) {
            $scope.selectedCloudServers.forEach(server => {
                $scope.deployment.cloudServerId.push(server.id);
            });
        }
        $scope.checkTestHostAvailable();
    };

    $scope.checkTestHostAvailable = function () {
        $scope.testHostAvailable = false;
        $scope.findHost = false;
        if ($scope.valiApi) {
            $scope.servers.forEach(s => {
                if ($scope.deployment.cloudServerId.indexOf("ALL") >= 0 || $scope.deployment.cloudServerId.indexOf(s.id) >= 0) {
                    if (s.managementIp === $scope.testHost) {
                        $scope.findHost = true;
                        $scope.tip = '目标主机已匹配测试平台环境主机('+$scope.testHost+')';
                        if(s.connectable){
                            $scope.testHostAvailable = true;
                            $scope.tip +='可连接';
                        }else{
                            $scope.tip +='不可连接';
                        }
                    }
                }
            });
            if(!$scope.findHost && !$scope.testHostAvailable){
                $scope.tip = '目标主机未匹配测试平台环境主机('+$scope.testHost+')';
            }
        }
    };

    $scope.checkConnect = function () {
        let servers = [];
        let tmpServers = $scope.deployment.cloudServerId;
        if (tmpServers) {
            for (let i = 0; i < tmpServers.length; i++) {
                if (tmpServers[i] !== 'ALL') {
                    $scope.servers.forEach(function (item) {
                        if (item.id === tmpServers[i]) {
                            servers.push(item);
                        }
                    });
                } else {
                    servers = angular.copy($scope.servers);
                    servers.shift();
                    break;
                }
            }
        }

        if (servers.length !== 0) {
            $scope.loading = HttpUtils.post('server/connect', servers, function (data) {
                data.data.results.forEach(function (item) {
                    $scope.servers.forEach(function (innerItem) {
                        if (item.id === innerItem.id) {
                            innerItem.connectable = item.connectable;
                        }
                    });
                });
            });
        } else {
            Notification.warn("请选择主机之后再点击测试！");
        }
    };

    $scope.init();
});



