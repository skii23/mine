ProjectApp.controller('ServerCtrl', function ($scope, $window, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils, eyeService) {
    $scope.cluster = angular.fromJson(sessionStorage.getItem("cluster"));
    sessionStorage.removeItem("cluster");

    $scope.clusterRole = angular.fromJson(sessionStorage.getItem("clusterRole"));
    sessionStorage.removeItem("clusterRole");
    // 定义搜索条件
    $scope.conditions = $scope.conditions = [
        {key: "instanceName", name: "名称", directive: "filter-contains"},
        {
            key: "accountId",
            name: "云账号",
            directive: "filter-select-icon",
            url: "server/cloudAccount/all",
            convert: {value: "id", label: "name", icon: 'icon'}
        },
        {
            key: "instanceStatus",
            name: "实例状态",
            directive: "filter-select", // 使用哪个指令
            selects: [
                {value: "Running", label: "Running"},
                {value: "Stopped", label: "Stopped"}
            ]
        },
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
            key: "ipAddress",
            name: "IP地址",
            directive: "filter-contains"
        }
    ];

    // 用于传入后台的参数
    $scope.filters = [
        {
            key: "instanceStatus",
            name: "实例状态",
            value: 'Running',
            label: 'Running',
            default: false,
            operator: "="
        }
    ];

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

    if ($scope.clusterRole) {
        $scope.filters.push({
            key: "clusterRoleId",
            name: "主机组名称",
            value: $scope.clusterRole.id,
            label: $scope.clusterRole.name,
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
        $scope.infoUrl = 'project/html/server/server-information.html' + '?_t=' + Math.random();
        $scope.toggleInfoForm(true);
        $scope.$broadcast('showDetail');
    };

    $scope.implement = function (item) {
        $scope.item = item;
        $scope.formUrl = 'project/html/script/script-implement.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.columns = [
        $scope.first,
        {value: "主机名称", key: "instance_name", width: "10%"},
        {value: "集群名称", key: "cluster_name"},
        {value: "主机组名称", key: "cluster_role_name", width: "10%"},
        {value: "云账号", key: "cloudAccountName", width: "10%"},
        {value: "主机型号", key: "instance_type", checked: false},
        {value: "主机配置", key: "instance_type_description"},
        {value: "操作系统", key: "manage_os"},
        {value: "管理IP", key: "management_ip"},
        {value: "代理IP", key: "proxy_id"},
        {value: "IP地址", key: "ip_array", sort: false, checked: false},
        {value: "用户名", key: "username"},
        {value: "主机状态", key: "instance_status"},
        {value: "连通性", key: "connectable"}
    ];

    $scope.showCloudServerImport = function () {
        $mdDialog.show({
            templateUrl: 'project/html/server/cloud-server-import.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false
        });
    };

    $scope.closeDetail = function () {
        $mdDialog.cancel();
    };

    $scope.group = function (item) {
        $scope.formUrl = 'project/html/server/server-group.html' + '?_t=' + Math.random();
        let groupList = [];
        if (item) {
            groupList.push(item.id)
            $scope.groupClusterId = item.clusterId;
            $scope.groupClusterName = item.clusterName;
        } else {
            // 1.2版本概念，同一个集群下主机进行主机组变更
            let clusterId;
            let clusterName;
            let flag = true;
            for (let i = 0; i < $scope.items.length; i++) {
                if ($scope.items[i].enable === true) {
                    if (clusterId) {
                        if (clusterId != $scope.items[i].clusterId) {
                            Notification.info("批量操作只能批量处理同一集群下的主机！");
                            flag = false;
                            return;
                        }
                    } else {
                        clusterId = $scope.items[i].clusterId;
                        clusterName = $scope.items[i].clusterName;
                    }
                }
            }
            if (flag) {
                $scope.items.forEach(function (item) {
                    if (item.enable === true) {
                        groupList.push(item.id);
                    }
                });
                $scope.groupClusterId = clusterId;
                $scope.groupClusterName = clusterName;
            }
        }
        if (groupList.length === 0) {
            Notification.info("请选择要加入主机组的主机!");
        } else {
            $scope.toggleForm();
        }
        $scope.groupItems = groupList;
    };

    $scope.proxy = function (item) {
        $scope.formUrl = 'project/html/server/server-proxy.html' + '?_t=' + Math.random();

        let proxyList = [];
        if (item) {
            proxyList.push(item.id)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    proxyList.push(item.id);
                }
            });
        }
        if (proxyList.length === 0) {
            Notification.info("请选择要设置代理的主机!");
        } else {
            $scope.toggleForm();
        }
        $scope.proxyItems = proxyList;
    };

    $scope.unGroup = function (item) {
        let unGroupList = [];
        if (item) {
            unGroupList.push(item.id)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    unGroupList.push(item.id)
                }
            });
        }
        Notification.confirm("确定移出主机组？", function () {
            HttpUtils.post("server/ungroup", unGroupList, function (data) {
                if (data.success) {
                    Notification.success("移除主机组成功");
                    $scope.list();
                }
            });
        }, function () {
            Notification.info("已取消");
        });
    };

    $scope.unProxy = function (item) {
        let unGroupList = [];
        if (item) {
            unGroupList.push(item.id)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    unGroupList.push(item.id)
                }
            });
        }
        Notification.confirm("确定取消代理？", function () {
            HttpUtils.post("server/unproxy", unGroupList, function (data) {
                if (data.success) {
                    Notification.success("取消代理成功！");
                    $scope.list();
                }
            });
        }, function () {
            Notification.info("已取消");
        });

    };

    $scope.scriptImplement = function (item) {
        $scope.params = {};
        $scope.params.clusterId = item.clusterId;
        $scope.params.clusterRoleId = item.clusterRoleId;
        $scope.params.serverId = item.id;

        $scope.formUrl = 'project/html/script/script_implement_public.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, 'server/list', condition)
    };
    $scope.list();

    $scope.getIp = function (item) {
        let ipList = [];
        if (item.ipArray) {
            let ips = angular.fromJson(item.ipArray);
            ips.forEach(function (value) {
                if (value === item.remoteIp) {
                    ipList.push({value: value, label: '(公)'})
                } else {
                    ipList.push({value: value})
                }
            });
        }
        return ipList;
    };


    //添加/更新时检查连接性
    $scope.checkConnect = function (item) {
        $scope.loadingLayer = HttpUtils.post("server/credential/connect", item, function (data) {
            if (data.success) {
                Notification.info(data.message);
            } else {
                Notification.warn(data.message);
            }
        });
    };

    $scope.connect = function (item) {
        let serverList = [];
        if (item) {
            serverList.push(item);
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    serverList.push(item);
                }
            });
        }
        if (serverList.length === 0) {
            Notification.confirm("确定测试所有主机连接？", function () {
                Notification.info("正在测试连接，请稍后...");
                $scope.loadingLayer = HttpUtils.post("server/connect", serverList, function (data) {
                    if (data.success) {
                        $scope.result = data.data;
                        $scope.formUrl = 'project/html/server/server-connect-result.html' + '?_t=' + Math.random();
                        $scope.toggleForm();
                    } else {
                        Notification.warn(data.message);
                    }
                });
            });
        } else {
            Notification.info("正在测试连接，请稍后...");
            $scope.loadingLayer = HttpUtils.post("server/connect", serverList, function (data) {
                if (data.success) {
                    $scope.result = data.data;
                    $scope.formUrl = 'project/html/server/server-connect-result.html' + '?_t=' + Math.random();
                    $scope.toggleForm();
                } else {
                    Notification.warn(data.message);
                }
            });
        }

    };

    $scope.deleteServer = function (item) {
        let deleteList = [];
        if (item) {
            deleteList.push(item.id)
        } else {
            $scope.items.forEach(function (item) {
                if (item.enable === true) {
                    deleteList.push(item.id);
                }
            });
        }

        if (deleteList.length === 0) {
            Notification.info("请选择主机");
            return;
        }

        Notification.confirm("确定删除主机？", function () {
            HttpUtils.post("server/delete", deleteList, function (data) {
                if (data.success) {
                    Notification.success("删除主机成功");
                    $scope.list();
                }
            });
        });
    };

    //创建主机
    $scope.getServerAddDefault = function () {
        $scope.item = {};
        $scope.formUrl = 'project/html/server/server-add.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    //导入主机
    $scope.getImportBulletBox = function () {
        $scope.formUrl = 'project/html/server/server-import.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    //编辑主机
    $scope.getServerEditDefault = function (item) {
        $scope.item = angular.copy(item);
        $scope.formUrl = 'project/html/server/server-edit.html' + '?_t=' + Math.random();
        $scope.toggleForm();
    };

    $scope.view = function (password, eye) {
        eyeService.view("#" + password, "#" + eye);
    };
});

ProjectApp.controller('ServerMetricCtrl', function ($scope) {
    $scope.metrics = [
        "SERVER_CPU_USAGE",
        "SERVER_MEMORY_USAGE",
        "SERVER_DISK_READ_AVERAGE",
        "SERVER_DISK_WRITE_AVERAGE",
        "SERVER_BYTES_TRANSMITTED_PER_SECOND",
        "SERVER_VIRTUAL_DISK_WRITE_LATENCY_AVERAGE",
        "SERVER_VIRTUAL_DISK_READ_LATENCY_AVERAGE"
    ];
    $scope.buildQueries = function () {
        let metrics = $scope.metrics ? $scope.metrics : [];
        let metricQueries = [];
        for (let i = 0; i < metrics.length; i++) {
            metricQueries.push({
                resourceId: $scope.detail.id,
                resourceName: $scope.detail.name,
                metricSource: "API",
                metric: metrics[i]
            });
        }
        return metricQueries;
    };

    $scope.request = {
        metricDataQueries: $scope.buildQueries()
    };
    $scope.execute = function () {
        $scope.request.metricDataQueries = $scope.buildQueries();
        if ($scope.request.execute) {
            $scope.request.execute();
        }
    };

    $scope.$on('showDetail', $scope.execute);
});


ProjectApp.controller('ProxyServerCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils) {

    $scope.loading = HttpUtils.get("proxy/listAll", function (data) {
        $scope.proxys = data.data;
    });

    $scope.submit = function () {
        $scope.loading = HttpUtils.post("server/proxy", {
            cloudServerIds: $scope.proxyItems,
            proxyId: $scope.proxyId
        }, function () {
            Notification.success("保存成功！");
            $scope.list();
            $scope.toggleForm();
        });
    };
});


ProjectApp.controller('GroupServerCtrl', function ($scope, $mdDialog, $mdBottomSheet, FilterSearch, Notification, HttpUtils) {

    $scope.loading = HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
    });

    $scope.clusterChange = function () {
        $scope.currentClusterRole = null;
        $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            if ($scope.clusterRoles.length > 0) {
                $scope.currentClusterRole = $scope.clusterRoles[0];
            }
        });
    };
    $scope.submit = function () {
        //1.2版本概念，变更主机组信息的时候，不能跨换集群
        if ($scope.groupClusterId && $scope.groupClusterId != $scope.clusterId) {
            Notification.info("不能跨集群加入主机组,原集群为 " + $scope.groupClusterName);
        } else {
            $scope.loading = HttpUtils.post("server/group", {
                cloudServerIds: $scope.groupItems,
                clusterRoleId: $scope.currentClusterRole.toString()
            }, function () {
                Notification.success("保存成功！");
                $scope.list();
                $scope.toggleForm();
            });
        }
    };


    /*
    //原submit
    $scope.submit = function () {
        $scope.loading = HttpUtils.post("server/group", {
            cloudServerIds: $scope.groupItems,
            clusterRoleId: $scope.currentClusterRole
        }, function () {
            Notification.success("保存成功！");
            $scope.list();
            $scope.toggleForm();
        });
    };
    */
});

ProjectApp.controller('ServerInfoController', function ($scope, $filter) {
    $scope.info = [
        {label: "名称", name: "instanceName"},
        {label: "工作空间", name: "workspaceName"},
        {label: "云账号", name: "cloudAccountName"},
        {label: "虚拟机所用镜像ID", name: "imageId"},
        {label: "虚机ID", name: "instanceUuid"},
        {label: "虚机状态", name: "instanceStatus"},
        {label: "所属区域", name: "region"},
        {label: "可用区", name: "zone"},
        {label: "hostname", name: "hostname"},
        {label: "IP", name: "ipArray", sort: false},
        {label: "管理IP", name: "managementIp"},
        {label: "操作系统", name: "manageOs"},
        {label: "宿主机", name: "host"},
        {label: "创建时间", name: "createTime", date: true}
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

ProjectApp.controller('ServerAddCtrl', function ($scope, $element, HttpUtils, Notification, $timeout) {

    $scope.tooltip = {
        hide: true
    };
    $scope.clickEvent = function (event) {
        if (event.type === 'click') {
            $scope.tooltip.hide = true;
            $element.off('click', $scope.clickEvent);
            $scope.$apply();
        }
    };
    $scope.toggleTooltip = function () {
        $scope.tooltip.hide = !$scope.tooltip.hide;
        $timeout(function () {
            if ($scope.tooltip.hide) {
                $element.off('click', $scope.clickEvent);
            } else {
                $element.on('click', $scope.clickEvent)
            }
        });
    };
    $scope.clipboard = new window.ClipboardJS('.copy-btn');

    $scope.clipboard.on('success', function () {
        Notification.success("复制成功！");
        $scope.$apply();
    });

    $scope.clipboard.on('error', function () {
        Notification.danger("复制失败！");
        $scope.$apply();
    });
    HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
        $scope.item.clusterId = data.data[0].id;
        HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            $scope.item.clusterRoleId = data.data[0].id;
        });

    });

    $scope.clusterChange = function () {
        console.warn(123123);
        HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            $scope.item.clusterRoleId = data.data[0].id;
        });
    };

    $scope.loading = HttpUtils.get("proxy/listAll", function (data) {
        $scope.proxys = data.data;
    });

    $scope.loading = HttpUtils.get("script/os/list", function (data) {
        $scope.oslist = data.data;
        for (let i = 0; i < $scope.oslist.length; i++) {
            if ($scope.oslist[i].key.indexOf("centos") != -1) {
                $scope.item.os = $scope.oslist[i].key;
                break;
            }
        }

        HttpUtils.get("script/os/version/list?os=" + $scope.item.os, function (data) {
            $scope.osVersions = data.data;
            $scope.item.osVersion = data.data[2];
        })

    });

    $timeout(function () {
        $scope.cmOption = {
            lineNumbers: true,
            indentWithTabs: true,
            theme: 'bespin'
        };
    });

    $scope.osVersionsChange = function () {
        HttpUtils.get("script/os/version/list?os=" + $scope.item.os, function (data) {
            $scope.osVersions = data.data;
            $scope.item.osVersion = data.data[0];
        })
    };

    HttpUtils.get("server/instance/all", function (data) {
        $scope.instanceTypes = data.data;
    });

    $scope.submit = function () {
        if ($scope.item.secretKey || $scope.item.password) {
            $scope.loading = HttpUtils.post("server/save", $scope.item, function (data) {
                if (data.success == true && "添加成功" == data.data) {
                    Notification.success("添加成功！");
                    $scope.toggleForm();
                    $scope.list();
                } else {
                    if (data.data) {
                        Notification.danger(data.data);
                    } else {
                        Notification.danger(data.message);
                    }
                }
            });
        } else {
            Notification.info("请填写秘钥或者密码！");
        }
    };
});


ProjectApp.controller('ServerImportCtrl', function ($scope, HttpUtils, Notification) {
    $scope.uploadFile = function (file) {
        // if (!file.files) {
        //     Notification.info('请选择文件');
        //     return;
        // }
        let url = "server/import";
        let formData = new FormData();//使用FormData进行文件上传
        // formData.append("tagKey", $scope.tag.tagKey);
        formData.append('file', file[0]);
        HttpUtils.post(url, formData, function (data) {
            if (data.success == true && !isNaN(parseInt(data.data))) {
                Notification.success("导入成功. 共导入" + data.data + "条记录");
                $scope.toggleForm();
                $scope.list();
            } else {
                Notification.danger("导入失败！" + data.data);
            }

        }, function (data) {
            Notification.danger("导入失败！" + data.message);
        }, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        });
    };


});

ProjectApp.controller('ServerEditCtrl', function ($scope, HttpUtils, Notification,$timeout) {
    //$scope.item.instanceType = "vm."+$scope.item.instanceType.replace("核","c").replace("G","g");
    //multi cluster role
    // if ($scope.item.clusterRoleIds) {
    //     $scope.item.clusterRoleId = $scope.item.clusterRoleIds.split(",");
    // }
    $scope.loading = HttpUtils.get("server/credential/" + $scope.item.id, function (data) {
        if (data.data) {
            $scope.item.secretKey = data.data.secretKey;
        }
    });
    $timeout(function () {
        $scope.cmOption = {
            lineNumbers: true,
            indentWithTabs: true,
            theme: 'bespin'
        };
    });
    $scope.loading = HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
    });

    $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
        $scope.clusterRoles = data.data;
    });

    $scope.clusterChange = function () {
        HttpUtils.get("clusterRole/list?clusterId=" + $scope.item.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            $scope.item.clusterRoleId = data.data[0].id;
        });
    };

    $scope.loading = HttpUtils.get("proxy/listAll", function (data) {
        $scope.proxys = data.data;
    });

    $scope.loading = HttpUtils.get("script/os/list", function (data) {
        $scope.oslist = data.data;
        HttpUtils.get("script/os/version/list?os=" + $scope.item.os, function (data) {
            $scope.osVersions = data.data;
        })

    });

    $scope.osVersionsChange = function () {
        HttpUtils.get("script/os/version/list?os=" + $scope.item.os, function (data) {
            $scope.osVersions = data.data;
        })
    };


    $scope.loading = HttpUtils.get("server/instance/all", function (data) {
        $scope.instanceTypes = data.data;
    });

    $scope.submit = function () {
        if ($scope.item.password || $scope.item.secretKey){
            $scope.loading = HttpUtils.post("server/update", $scope.item, function (data) {
                if (data.success == true && "修改成功" == data.data) {
                    Notification.success("修改成功！");
                    $scope.toggleForm();
                    $scope.list();
                } else {
                    Notification.danger(data.data);
                }
            })
        }else {
            Notification.info("请填写秘钥或者密码！");
        }
    };

});

ProjectApp.controller('CloudServerImportController', function ($scope, $state, $mdDialog, HttpUtils, FilterSearch, Notification) {

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
        $scope.first,
        {value: "名称", key: "instanceName"},
        {value: "工作空间", key: "workspaceName"},
        {value: "操作系统信息", key: "osInfo", checked: false},
        {value: "实例状态", key: "instanceStatus"},
        {value: "实例类型", key: "instanceType"},
        {value: "磁盘大小(GB)", key: "disk", checked: false},
        {value: "IP地址", key: "managementIp"},
        {value: "申请人", key: "applyUser"},
        {value: "创建时间", key: "createTime", checked: true},
        {value: "过期时间", key: "expiredTime", checked: false}
    ];

    $scope.filters = [{key: "instanceStatus", name: "实例状态", value: ['Running', 'Stopped'], label: '运行, 关机'}];

    $scope.conditions = [
        {key: "instanceName", name: "名称", directive: "filter-contains"},
        {
            key: "accountId",
            name: "云账号",
            directive: "filter-select-icon",
            url: "/vm-service/account/list/all",
            convert: {value: "id", label: "name", icon: 'icon'}
        },
        {
            key: "instanceStatus",
            name: "实例状态",
            directive: "filter-select-multiple",
            selects: [
                {value: "Running", label: "运行"},
                {value: "Stopped", label: "关机"},
                {value: "Deleted", label: "删除"}
            ]
        },
        {
            key: "poolTagValue",
            name: "用途",
            directive: "filter-contains",
        },
        {
            key: "ipAddress",
            name: "IP地址",
            directive: "filter-input",
        }
    ];

    $scope.closeDetail = function () {
        $mdDialog.cancel();
        $state.go('server', null, {
            reload: true
        });
    };

    $scope.list = function (sortObj) {
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj || $scope.sort) {
            $scope.sort = sortObj || $scope.sort;
        } else {
            $scope.sort = {sql: 'cloud_server.create_time desc'}
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        HttpUtils.paging($scope, '/vm-service/server/list', condition);
    };
    $scope.list();

    $scope.import = function () {
        let params = {};
        params.servers = $scope.servers;
        params.clusterId = $scope.clusterId;
        params.currentClusterRole = $scope.currentClusterRole;
        HttpUtils.post("server/cloud/import", params, function (data) {
            if (data.success) {
                $scope.closeDetail();
                Notification.success("导入成功！");
            } else {
                $scope.closeDetail();
                Notification.warn("导入失败：" + data.message);
            }
        });
    };

    $scope.next = function () {
        let serverList = [];
        for (let i = 0; i < $scope.items.length; i++) {
            if ($scope.items[i].enable === true) {
                serverList.push($scope.items[i]);
            }
        }
        if (serverList.length === 0) {
            Notification.warn("请选择要导入的主机！");
            return;
        }
        $scope.servers = serverList;
        $mdDialog.show({
            templateUrl: 'project/html/server/cloud-server-group.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false
        });
    };

    $scope.loading = HttpUtils.get("cluster/list", function (data) {
        $scope.clusters = data.data;
    });

    $scope.clusterChange = function () {
        $scope.currentClusterRole = null;
        $scope.loading = HttpUtils.get("clusterRole/list?clusterId=" + $scope.clusterId, function (data) {
            $scope.clusterRoles = data.data;
            if ($scope.clusterRoles.length > 0) {
                $scope.currentClusterRole = $scope.clusterRoles[0];
            }
        });
    };

    $scope.showCloudServerImport = function () {
        $mdDialog.show({
            templateUrl: 'project/html/server/cloud-server-import.html' + '?_t=' + Math.random(),
            parent: angular.element(document.body),
            scope: $scope,
            preserveScope: true,
            clickOutsideToClose: false
        });
    };
});


