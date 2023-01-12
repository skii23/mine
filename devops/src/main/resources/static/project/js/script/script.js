ProjectApp.controller('ScriptController', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state, $document) {

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
        $scope.infoUrl = 'project/html/script/script-infomation.html' + '?_t=' + Math.random();
        $scope.toggleInfoForm(true);
    };

    $scope.columns = [
        {value: "脚本名称", key: "name", width: "15%"},
        {value: "执行路径", key: "execute_path", width: "15%"},
        {value: "操作系统", key: "os", width: "10%"},
        {value: "系统版本", key: "os_version", width: "10%"},
        {value: "可见级别", key: "scope", width: "10%"},
        {value: "创建时间", key: "created_time", width: "20%"},
    ];


    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.item = {};
        $scope.formUrl = 'project/html/script/script-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };

    $scope.implement = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/script/script-implement.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };
    $scope.delete = function (item) {
        Notification.confirm("确定删除？", function () {
            $scope.loading = $http.post("script/delete", item.id).then(function (data) {
                var data = data.data;
                if (data.success) {
                    Notification.success("删除成功");
                    $scope.list();
                } else {
                    Notification.warn(data.message)
                }
            })
        });
    };


    $scope.isEdit = function (item) {
        let role = $scope.currentRole;
        switch (role) {
            case "USER":
                if (item.scope === "workspace") {
                    return true;
                }
                return false;
            case "ADMIN":
                if (item.scope === "global") {
                    return true;
                }
                return false;
            case "ORGADMIN":
                if (item.scope === "org") {
                    return true;
                }
                return false;
        }
    };
    $scope.isDisable = function (item) {
        let role = $scope.currentRole;
        switch (role) {
            case "USER":
                return !$scope.isEdit(item) && false;

            case "ADMIN":
                return !$scope.isEdit(item) && true;

            case "ORGADMIN":
                return !$scope.isEdit(item) && true;
        }
    };


    $scope.enableClick = function (event) {
        event.stopPropagation();
    };

    $scope.edit = function (item) {
        $scope.item = angular.copy(item);
        $scope.formUrl = 'project/html/script/script-edit.html' + '?_t=' + Math.random();
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
        HttpUtils.paging($scope, 'script/list', condition)
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
    }
});


ProjectApp.controller('ScriptAddController', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $timeout) {
    $scope.loading = HttpUtils.get("script/os/list", function (data) {
        $scope.oslist = data.data;
    });

    $scope.getOsVersions = function () {
        $scope.loading = HttpUtils.get("script/os/version/list?os=" + $scope.item.os, function (data) {
            $scope.osVersions = data.data;
        })
    };


    $scope.submit = function () {
        //处理osversion
        let version = "";

        for (let i = 0; i < $scope.item.osVersionList.length; i++) {
            if (i !== 0) {
                version += ",";
            }
            version += $scope.item.osVersionList[i];
        }
        $scope.item.osVersion = version;


        $scope.loading = HttpUtils.post("script/save", $scope.item, function (data) {
            Notification.info("添加成功！");
            $scope.list();
            $scope.toggleForm();
        })
    };

    $timeout(function () {
        $scope.cmOption = {
            lineNumbers: true,
            indentWithTabs: true,
            theme: 'bespin',
            mode: 'shell'
        };
    });
});

ProjectApp.controller('ScriptEditController', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $timeout) {

    let versions = $scope.item.osVersion.split(',');
    let versionObjs = [];
    angular.forEach(versions, function (version) {
        versionObjs.push(version);
    });
    $scope.item.osVersionList = versionObjs;


    $scope.init = function () {
        $scope.getOsList();
        $scope.getOsVersions();
    };

    $scope.getOsList = function () {
        HttpUtils.get("script/os/list", function (data) {
            $scope.oslist = data.data;
        });
    };

    $scope.osChange = function () {
        $scope.item.osVersion = null;
        $scope.getOsVersions();
    };

    $scope.getOsVersions = function () {
        HttpUtils.get("script/os/version/list?os=" + $scope.item.os, function (data) {
            $scope.osVersions = data.data;
        })
    };

    $scope.submit = function () {

        let version = "";

        for (let i = 0; i < $scope.item.osVersionList.length; i++) {
            if (i !== 0) {
                version += ",";
            }
            version += $scope.item.osVersionList[i];
        }
        $scope.item.osVersion = version;

        HttpUtils.post("script/update", $scope.item, function (data) {
            Notification.success("修改成功!");
            $scope.list();
            $scope.toggleForm();
        })
    };


    $timeout(function () {
        $scope.cmOption = {
            lineNumbers: true,
            indentWithTabs: true,
            theme: 'bespin',
            mode: 'shell'
        };
    });

    $scope.init();

});

ProjectApp.controller('ScriptInfoController', function ($scope, $filter) {
    $scope.info = [
        {label: "名称", name: "name"},
        {label: "操作系统", name: "os"},
        {label: "系统版本", name: "osVersion"},
        {label: "创建时间", name: "createdTime", date: true},
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

ProjectApp.controller('ScriptImplementController', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, $timeout, CommonService) {
    //初始化数据
    HttpUtils.get("script/os/list", function (data) {
        $scope.oslist = data.data;
    });


    $scope.loading = CommonService.getClusters().then(function (data) {
        var result = data.data;
        $scope.clusters = result.data;
    });


    $scope.clusterChange = function () {
        $scope.item.clusterRoleId = null;
        $scope.item.serverId = null;
        $scope.getClusterRoles();
    };

    $scope.clusterRoleChange = function () {
        $scope.item.serverId = null;
        $scope.getRunningServer();
    };


    $scope.getRunningServer = function () {
        $scope.loading = HttpUtils.post("server/list", {
                "clusterId": $scope.item.clusterId,
                "clusterRoleId": $scope.item.clusterRoleId,
                "osKey": $scope.item.os,
                "osVersions": $scope.item.osVersion
            },
            function (data) {
                $scope.servers = data.data;
                console.log($scope.servers);
                if ($scope.servers.length !== 0){
                    $scope.servers=[{"instanceName": "全部主机", "id": "ALL"}].concat($scope.servers);
                }
            }
        )
    };
    $scope.getClusterRoles = function () {
        $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            if ($scope.clusterRoles.length !== 0)
                $scope.clusterRoles=[{"name": "全部主机组", "id": "ALL"}].concat($scope.clusterRoles);
        });
    };


    $scope.submit = function () {
        if ($scope.item.serverId === 'ALL' && $scope.servers.length >= 3) {
            Notification.prompt({
                title: '确认密码',
                text: '请输入登录密码确认执行',
                required: true
            }, function (result) {
                $scope.item.password = result;
                $scope.loading = HttpUtils.post("script/implement/create", $scope.item, function (data) {
                    Notification.info("任务提交成功！");
                    $scope.list();
                    $scope.toggleForm();
                });
            });
        }else {
            $scope.loading = HttpUtils.post("script/implement/create", $scope.item, function (data) {
                Notification.info("任务提交成功！");
                $scope.list();
                $scope.toggleForm();
            });
        }
    };
    $timeout(function () {
        $scope.cmOption = {
            lineNumbers: true,
            indentWithTabs: true,
            theme: 'bespin',

            onLoad: function (_cm) {
                // HACK to have the codemirror instance in the scope...
                $scope.typeChanged = function () {
                    console.log($scope.item.type);
                    _cm.setOption('mode', "shell");
                };
            }
        };
    });
});

ProjectApp.controller('ScriptImplementPublicCtrl', function ($scope, $state, Notification, HttpUtils, $timeout, CommonService) {
    $scope.item = {};
    $scope.loading = CommonService.getClusters().then(function (data) {
        var data = data.data;
        $scope.clusters = data.data;
        if ($scope.params) {
            $scope.item.clusterId = $scope.params.clusterId;
            $scope.clusterChange();
        }
    });

    $scope.getRunningServer = function () {
        $scope.loading = HttpUtils.post("server/list", {
            "clusterId": $scope.item.clusterId,
            "clusterRoleId": $scope.item.clusterRoleId
        }, function (data) {
            $scope.servers = data.data;
            if ($scope.servers.length !== 0)
                $scope.servers=[{"instanceName": "全部主机", "id": "ALL"}].concat($scope.servers);
            if ($scope.params.serverId) {
                $scope.item.serverId = $scope.params.serverId;
            }
        })
    };
    $scope.getClusterRoles = function () {

        $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            if ($scope.clusterRoles.length !== 0)
                $scope.clusterRoles=[{"name": "全部主机组", "id": "ALL"}].concat($scope.clusterRoles);
            if ($scope.params.clusterRoleId) {
                $scope.item.clusterRoleId = $scope.params.clusterRoleId;
                $scope.clusterRoleChange();
            }
        });
    };
    $scope.clusterChange = function () {
        $scope.item.clusterRoleId = null;
        $scope.item.serverId = null;
        $scope.getClusterRoles();
    };

    $scope.clusterRoleChange = function () {
        $scope.item.serverId = null;
        $scope.getRunningServer();
    };

    $scope.getScripts = function () {
        HttpUtils.post("script/list", {}, function (data) {
            $scope.scripts = data.data;
            $scope.scripts = [{id: null, name: '自定义', content: "", executePath: "/bin/bash"}].concat($scope.scripts);
            $scope.script = $scope.scripts[0];
        });
    };
    $scope.getScripts();
    $scope.interpreterChange = function () {
        $scope.getScripts();
    };


    $timeout(function () {
        $scope.cmOption = {
            lineNumbers: true,
            indentWithTabs: true,
            theme: 'bespin',
            onLoad: function (_cm) {
                // HACK to have the codemirror instance in the scope...
                $scope.typeChanged = function () {
                    console.log($scope.item.type);
                    _cm.setOption('mode', "shell");
                };
            }
        };
    });
    $scope.submit = function () {
        $scope.script.clusterId = $scope.item.clusterId;
        $scope.script.clusterRoleId = $scope.item.clusterRoleId;
        $scope.script.serverId = $scope.item.serverId;
        if ($scope.script.serverId === 'ALL' && $scope.servers.length >= 3) {
            Notification.prompt({
                title: '确认密码',
                text: '请输入登录密码确认执行',
                required: true
            }, function (result) {
                $scope.script.password = result;
                $scope.loading = HttpUtils.post("script/implement/create", $scope.script, function (data) {
                    Notification.info("任务提交成功！");
                    $state.go('script_implement_log');
                });
            });
        }else {
            $scope.loading = HttpUtils.post("script/implement/create", $scope.script, function (data) {
                Notification.info("任务提交成功！");
                $state.go('script_implement_log');
            })
        }
    };

});


