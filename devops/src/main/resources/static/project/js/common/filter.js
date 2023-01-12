ProjectApp.filter("ScopeFilter", function () {
    return function (str) {
        if (str) {
            switch (str) {
                case "org" :
                    return "组织";
                case "global" :
                    return "全局";
                case "workspace":
                    return "工作空间";
                default:
                    return null;
            }
        }
    }
}).filter("StatusFilter", function () {
    return function (str) {
        if (str) {
            switch (str) {
                case  "valid" :
                    return "有效";
                case "invalid" :
                    return "无效";
                case "pending" :
                    return "等待中";
                case "running" :
                    return "执行中";
                case "complete" :
                    return "已完成";
                case "error" :
                    return "错误";
                case "success":
                    return "成功";
                case "fail" :
                    return "失败";
                default:
                    return null;
            }
        }
    }
}).filter("ResultFilter", function () {
    return function (str) {
        if (str) {
            switch (str) {
                case "success":
                    return "成功";
                case "fail" :
                    return "失败";
                case "unknown":
                    return "未知";
                default:
                    return null;
            }
        }
    }
}).filter("DeployPolicyFilter", function () {
    return function (str) {
        if (str) {
            switch (str) {
                case "all":
                    return "全部同时部署";
                case "half" :
                    return "半数分批部署";
                case "single" :
                    return "单台依次部署";
                default:
                    return null;
            }
        }
    }
}).filter('AllFilter', function () {
    return function (str) {
        if (str) {
            switch (str) {
                case "ALL":
                    return "全部";
                default:
                    return str;
            }
        }
    }
}).filter('ScriptFilter', function () {
    return function (str) {
        if (str !== undefined) {
            if (str == null) {
                return "自定义";
            } else {
                return str;
            }
        }
    }
}).filter('NullFilter', function () {
    return function (str) {
        if (str == null) {
            return "无";
        }
        return str;
    }
}).filter('ServerNameFilter', function () {
    return function (item) {
        if (item) {
            if (item.instanceName === '全部主机') {
                return "全部主机"
            }
            return item.instanceName + "  [" + item.managementIp + "] ";
        }
    }
}).filter('ResourceFilter', function () {
    return function (str) {
        if (str) {
            switch (str) {
                case "cluster":
                    return "集群";
                case "clusterRole" :
                    return "主机组";
                case "cloudServer" :
                    return "主机";
                default:
                    return null;
            }
        }
    }
}).filter("EnvFilter", function () {
    return function (collection, items, item) {
        let output = [];
        angular.forEach(collection, function (c) {
            let flag = true;
            angular.forEach(items, function (it) {
                if ((item !== it) && it.envId && it.envId === c.id) {
                    flag = false;
                }
            });
            if (flag) {
                output.push(c);
            }
        });

        return output;
    }
}).filter('ServerStatusFilter', function () {
    return function (item) {
        if (item) {
            switch (item) {
                case "Running":
                    return "运行中";
                case "Stopped" :
                    return "停止";
                case "Deleted" :
                    return "已删除";
            }
        }
    }
}).filter('StrEmptyFilter', function () {
    return function (item) {
        if (item) {
            return item;
        } else {
            return "N/A";
        }
    }
}).filter('ActiveFilter', function () {
    return function (item) {
        if (item === 'false' || !item) {
            return '否';
        } else if (item === 'true' || item) {
            return '是';
        }
    }
}).filter('JenkinsStatusFilter', function () {
    return function (item) {
        if (item) {
            switch (item) {
                case "NO_CHECK":
                    return "未校验";
                case "VALID" :
                    return "有效";
                case "INVALID" :
                    return "无效";
                default:
                    return "未校验";
            }
        }
    }
}).filter('JenkinsSyncStatusFilter', function () {
    return function (item) {
        if (item) {
            switch (item) {
                case "NO_SYNC":
                    return "未同步";
                case "IN_SYNC" :
                    return "正在同步";
                case "END_SYNC" :
                    return "同步完成";
                case "ERROR_SYNC" :
                    return "同步错误";
                default:
                    return "未同步";
            }
        }
    }
}).filter('BuildResultFilter', function () {
    return function (item) {
        if (item) {
            switch (item) {
                case "SUCCESS":
                    return "成功";
                case "FAILURE" :
                    return "失败";
                case "BUILDING" :
                    return "构建中";
                case "REBUILDING" :
                    return "重构中";
                case "NOT_BUILT" :
                    return "未构建";
                case "ABORTED":
                    return "已中止";
                case "UNSTABLE":
                    return "存在错误";
                case "UNKNOWN":
                    return "未知";
                case "CANCELLED":
                    return "已取消";
                default:
                    return item;
            }
        }
    }
}).filter('TestFilter', function () {
    return function (item) {
        if (item.length === 0) {
            return item;
        }
        return item;
    }
}).filter('ResourcePathFilter', function () {
    return function (items) {
        if (items) {
            let resourcePath = items;
            if (items.length > 20) {
                resourcePath = items.substring(0, 20) + " ...";
                return resourcePath;
            }
            return resourcePath;
        } else {
            return "";
        }
    }
}).filter('SourceFilter', function () {
    return function (item) {
        switch (item) {
            case 'sync':
                return '服务器同步';
            case 'local':
                return '本地创建';
            default:
                return '未知';
        }
    }
}).filter('ChildNameFilter', function () {
    return function (item) {
        if(item.indexOf("/") > 0){
            let split = item.split("/");
            return split[split.length-1];
        }
        return item;
    }
}).filter('ConnectableFilter', function () {
    return function (item) {
        switch (item) {
            case undefined:
                return '';
            case true:
                return '(可连接)';
            case false:
                return '(不可连接)';
            default:
                return '(不可连接)';
        }
    };
}).filter('ScriptFilterTypeFilter', function () {
    return function (type) {
        switch (type) {
            case 'REGEX':
                return '正则表达式';
            case 'KEYWORD':
                return '关键字过滤';
            default:
                return '未知类型';
        }
    };
}).filter('ImageEnvFilter', function () {
    return function (envs,isImageEnv) {
        if (!envs || envs.length === 0) {
            return [];
        }
        if (isImageEnv) {
            return envs.filter(env => env.name.toLowerCase().indexOf("harbor") !== -1);
        }else {
            return envs.filter(env => env.name.toLowerCase().indexOf("harbor") === -1);
        }
    };
});
