ProjectApp.controller('RepositoryCtrl', function ($scope, $mdDialog, UserService, $mdBottomSheet, FilterSearch, Notification, HttpUtils, Loading, $http, $state) {

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
        $scope.infoUrl = 'project/html/repository/repository-infomation.html' + '?_t=' + window.appversion;
        $scope.toggleInfoForm(true);
    };

    $scope.columns = [
        {value: "制品库名称", key: "name"},
        {value: "制品库类型", key: "type"},
        {value: "制品库地址", key: "repository", sort: false, width: "20%"},
        {value: "可见级别", key: "scope", width: "10%"},
        {value: "创建时间", key: "created_time"},
        {value: "状态", key: "status"}

    ];


    $scope.goApplication = function () {
        $state.go("application")
    };
    $scope.create = function () {
        // $scope.formUrl用于side-form
        $scope.formUrl = 'project/html/repository/repository-add.html' + '?_t=' + Math.random();
        // toggleForm由side-form指令生成
        $scope.toggleForm();
    };
    $scope.checkStatus = function (item) {
        $scope.loadingLayer = HttpUtils.post("repository/check/" + item.id, {}, function (data) {
            $scope.list();
        }, function (data) {
            Notification.warn(data.message)
        })
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
            HttpUtils.post("repository/delete", item.id, function (data) {
                Notification.success("删除成功");
                $scope.list();
            });
        });
    };
    $scope.edit = function (item) {
        $scope.item = angular.copy(item);
        $scope.formUrl = 'project/html/repository/repository-edit.html' + '?_t=' + Math.random();
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
        HttpUtils.paging($scope, 'repository/list', condition)
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
            // User clicked outside or hit escape
        });
    }
});

ProjectApp.controller('RepositoryInfoController', function ($scope, $filter) {

    $scope.info = [
        {label: "名称", name: "name"},
        {label: "制品库类型", name: "type"},
        {label: "制品库地址", name: "repository"},
        {label: "可见级别", name: "scope", scope: true},
        {label: "创建时间", name: "createdTime", date: true},
        {label: "状态", name: "status", status: true}
    ];

    $scope.getValueOrDefault = function (item, info, defaultValue) {
        if (item && item[info.name] && item[info.name] !== '' && info.date) {
            return $scope.formatTime(item[info.name]);
        } else if (item && item[info.name] && item[info.name] !== '' && info.scope) {
            return $scope.formatScope(item[info.name])
        } else if (item && item[info.name] && item[info.name] !== '' && info.status) {
            return $scope.formatStatus(item[info.name])
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
    };

    $scope.formatStatus = function (scope) {
        return $filter('StatusFilter')(scope)
    }
});
ProjectApp.controller('RepositoryEditController', function ($scope, $http, HttpUtils, Notification, eyeService) {

    $scope.repositorys = $http.get("repository/types").then(function (data) {
        var data = data.data;
        if (data.success) {
            $scope.repositoryTypes = data.data;
        } else {
            Notification.warn(data.message)
        }
    });


    $scope.getBuckets = function () {
        $scope.loading = HttpUtils.post("repository/bucket/list", {
            accessKey: $scope.item.accessId,
            secretKey: $scope.item.accessPassword,
            type: $scope.item.type
        }, function (data) {
            $scope.buckets = data.data;
        }, function (data) {
            Notification.warn(data.message)
        });
    };

    $scope.view = function (password, eye) {
        eyeService.view("#" + password, "#" + eye);
    };

    $scope.getRepositories = function () {
        $scope.loading = HttpUtils.post("repository/rep/list", {
            accessKey: $scope.item.accessId,
            secretKey: $scope.item.accessPassword,
            path: $scope.item.path,
            type: $scope.item.type
        }, function (data) {
            $scope.repositories = data.data;
        }, function (data) {
            Notification.warn(data.message)
        });
    };
    $scope.typeChange = function () {
        $scope.item.accessId = null;
        $scope.item.accessPassword = null;
        $scope.repositories = null;
        $scope.item.path = null;
        $scope.buckets = null;
    };

    $scope.clearRep = function () {
        $scope.buckets = null;
        $scope.repositories = null;
    };
    let http_prefix = 'http://';
    let https_prefix = 'https://';
    switch ($scope.item.type) {
        case "Nexus": {
            $scope.item.path = $scope.item.repository.split('content')[0];
            $scope.getRepositories();
            break;
        }
        case "Nexus3": {
            $scope.item.path = $scope.item.repository.split('repository')[0];
            $scope.getRepositories();
            break;
        }
        case "Artifactory": {
            $scope.item.path = $scope.item.repository.split('artifactory')[0];
            $scope.getRepositories();
            break;
        }
        case "Harbor": {
            $scope.item.path = $scope.item.repository.split('api')[0];
            $scope.getRepositories();
            break;
        }
        default: {
            $scope.getBuckets();
        }
    }

    $scope.submit = function () {
        $scope.loading = $http.post("repository/update", $scope.item).then(function (data) {
            var data = data.data;
            if (data.success) {
                Notification.success("保存成功");
                $scope.toggleForm();
                $scope.list();
            } else {
                Notification.warn(data.message);
            }
        });
    }
});


ProjectApp.controller('RepositoryAddController', function ($scope, $http, HttpUtils, Notification, eyeService) {
    $scope.item = {};
    $scope.tagMappings = [];

    $scope.repositorys = $http.get("repository/types").then(function (data) {
        var data = data.data;
        if (data.success) {
            $scope.repositoryTypes = data.data;
        } else {
            Notification.warn(data.message)
        }
    });

    $scope.view = function (password, eye) {
        eyeService.view("#" + password, "#" + eye);
    };


    $scope.getBuckets = function () {
        $scope.loading = HttpUtils.post("repository/bucket/list", {
            accessKey: $scope.item.accessId,
            secretKey: $scope.item.accessPassword,
            type: $scope.item.type
        }, function (data) {
            $scope.buckets = data.data;
        }, function (data) {
            Notification.warn(data.message)
        });
    };

    $scope.getRepositories = function () {
        $scope.loading = HttpUtils.post("repository/rep/list", {
            accessKey: $scope.item.accessId,
            secretKey: $scope.item.accessPassword,
            path: $scope.item.path,
            type: $scope.item.type
        }, function (data) {
            $scope.repositories = data.data;
        }, function (data) {
            Notification.warn(data.message)
        });
    };

    $scope.clearRep = function () {
        $scope.buckets = null;
        $scope.repositories = null;
    };


    $scope.wizard = {
        setting: {
            title: "标题",
            subtitle: "子标题",
            closeText: "取消",
            submitText: "保存",
            nextText: "下一步",
            prevText: "上一步",
        },
        // 按顺序显示,id必须唯一并需要与页面中的id一致，select为分步初始化方法，next为下一步方法(最后一步时作为提交方法)
        steps: [
            {
                id: "1",
                name: "基础设置",
                select: function () {
                    console.log("第一步select")
                },
                next: function () {
                    if (!$scope.item.name) {
                        Notification.info("请输入制品库名称");
                        return false;
                    }

                    if (!$scope.item.type) {
                        Notification.info("请选择制品库类型");
                        return false;
                    }
                    return true;
                }
            }, {
                id: "2",
                name: "制品库设置",
                select: function () {
                },
                next: function () {

                    $scope.loading = $http.post("repository/create", $scope.item).then(function (data) {
                        var data = data.data;
                        if (data.success) {
                            Notification.success("保存成功");
                            $scope.toggleForm();
                            $scope.list();
                        } else {
                            Notification.warn(data.message);
                        }
                    });
                    return true;
                }
            }
        ],
        // 嵌入页面需要指定关闭方法
        close: function () {
            $scope.toggleForm();
        }
    };

    $scope.pass = false;
    $scope.check = function () {
        $scope.pass = !$scope.pass;
    };

    $scope.show = true;

    $scope.open = function () {
        $scope.show = true;
    }
});

