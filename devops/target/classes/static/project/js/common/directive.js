ProjectApp.directive('selectFileTree', function ($mdDialog, Notification,HttpUtils) {
    return {
        restrict: 'E',
        template:
            '          <md-input-container flex class="md-block">\n' +
            '              <input type="text" placeholder="{{placeholder}}" ng-required="{{required}}" ng-model="bind" disabled>\n' +
            '          </md-input-container>\n' +
            '          <div>\n' +
            '              <md-button id="uploadButton" class="md-fab md-mini">\n' +
            '                  <md-icon class="material-icons">attach_file</md-icon>\n' +
            '              </md-button>\n' +
            '          </div>',
        scope: {
            placeholder: "@",
            file: "=",
            required: "@",
            select: "@",
            bind: "=",
            url: "=",
            repo: "="
        },
        link: function (scope, elem) {
            let button = elem.find('button');
            scope.option = {
                select: scope.select, //file, folder, all
                hasChildren: function (node) {
                    return (angular.isArray(node.children) && node.children.length > 0) || node.hasChildren;
                },
                clickCallback: function (node, collapsed) {
                    if (!node.children && !collapsed) {
                        let url = "application/version/file/tree/subNodes";
                        let body = angular.extend({node: node.obj}, scope.repo);
                        scope.dialogLoading = HttpUtils.post(url, body, function (data) {
                            node.children = data.data;
                        });
                    }
                },
                selectCallback: function (node) {
                    if (scope.repo.type.startsWith("Nexus") && !node.hasChildren) {
                        if (scope.repo.type === "Nexus") {
                            scope.radio.url = scope.repo.repository.replace("content/", "service/local/") + "/content" + node.obj;
                        } else {
                            scope.radio.url = scope.repo.repository + '/' + node.obj;
                        }
                    }
                }
            };
            scope.radio = {
                selected: "",
                onChange: function (node) {
                },
                url: ""
            };
            scope.closeDialog = function () {
                scope.radio.selected.obj = null;
                $mdDialog.cancel();
            };
            scope.submit = function () {
                if (!scope.repo.type.startsWith("Nexus")) {
                    scope.bind = scope.radio.selected.obj;
                } else {
                    scope.bind = scope.radio.url;
                }
                $mdDialog.cancel();
            };

            button.bind('click', function () {
                if(scope.repo){
                    scope.dialogLoading = HttpUtils.get(scope.url, function (data) {
                        scope.nodes = data.data;
                    });
                    $mdDialog.show({
                        templateUrl: "project/html/application/tree-dialog.html",
                        parent: angular.element(document.body),
                        scope: scope,
                        preserveScope: true,
                        clickOutsideToClose: false
                    });
                }else {
                    Notification.warn("请选择正确的应用和环境！");
                }

            });
        }
    };
}).directive('f2cPublisher', function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/jenkins-f2c-publisher.html?_t=' + Math.random(),
        replace: true,
        scope: {
            f2CPublisher: '=',
            closeable: '=',
            publishers: '='
        },
        controller: function ($scope, HttpUtils, $element,UserService) {
            $scope.repoTypes = ['Nexus', 'Nexus3', 'Artifactory', '阿里云OSS', '亚马逊S3'];
            $scope.policies = [{
                value: 'all',
                key: '全部同时部署'
            }, {
                value: 'half',
                key: '半数分批部署'
            }, {
                value: 'single',
                key: '单台依次部署'
            }];

            $scope.f2CPublisher.workspaceId = UserService.getWorkSpaceId();

            $element.find('.search-input').on('keydown', function (ev) {
                ev.stopPropagation();
            });

            $scope.clearSearch = function () {
                $scope.appName = '';
                $scope.serverIp = '';
            };

            let checkedDict = {
                'Nexus': 'nexusChecked',
                'Nexus3': 'nexus3Checked',
                'Artifactory': 'artifactoryChecked',
                '阿里云OSS': 'ossChecked',
                '亚马逊S3': 's3Checked'
            };

            $scope.$watch('f2CPublisher.artifactType', function (newVal, oldVal) {
                for (let checkedDictKey in checkedDict) {
                    $scope.f2CPublisher[checkedDict[checkedDictKey]] = $scope.f2CPublisher.artifactType === checkedDictKey;
                }
            });

            $scope.changeWorkspace = function () {
                $scope.f2CPublisher.applicationId = null;
                $scope.f2CPublisher.repositorySettingId = null;
                $scope.f2CPublisher.clusterId = null;
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                $scope.getApplications();
            };

            $scope.changeApp = function () {
                $scope.f2CPublisher.repositorySettingId = null;
                $scope.f2CPublisher.clusterId = null;
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                $scope.getEnvList();
                // $scope.valiApiTest();
                // $scope.checkTestHostAvailable();
                $scope.f2CPublisher.autoApiTest = false;
            };

            $scope.changeEnv = function () {
                $scope.f2CPublisher.clusterId = null;
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                $scope.getCluster();
                if ($scope.app && $scope.app.applicationRepositorySettings.length > 0) {
                    $scope.app.applicationRepositorySettings.forEach(function (appRepoSetting) {
                        $scope.repoList.forEach(function (appRepoItem) {
                            if (appRepoSetting.repositoryId === appRepoItem.id) {
                                $scope.f2CPublisher.artifactType = appRepoItem.type;
                            }
                        });
                    });
                }
            };

            $scope.changeCluster = function () {
                $scope.clusterRoles = [];
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                if ($scope.f2CPublisher.clusterId) {
                    $scope.getClusterRoles();
                }
            };

            $scope.changeClusterRole = function () {
                $scope.cloudServers = [];
                $scope.f2CPublisher.cloudServerId = null;
                if ($scope.f2CPublisher.clusterRoleId) {
                    $scope.getCloudServers();
                }
            };

            $scope.getWorkspaces = function () {
                $scope.workspaces = [];
                HttpUtils.get('user/source/list', function (data) {
                    data.data.forEach(function (item) {
                        if (item.type === 'workspace') {
                            $scope.workspaces.push(item);
                        }
                    });
                    $scope.getApplications();
                });
            };

            $scope.getApplications = function () {
                HttpUtils.get("application/list", function (data) {
                    $scope.applications = [];
                    data.data.forEach(function (item) {
                        if (item.scope === 'global' || item.workspaceId === $scope.f2CPublisher.workspaceId) {
                            $scope.applications.push(item);
                        }
                    });
                    $scope.getRepos();
                    // $scope.valiApiTest();
                });
            };

            $scope.getRepos = function () {
                HttpUtils.post('repository/listAll', {}, function (data) {
                    $scope.repoList = data.data;
                    $scope.getEnvList();
                })
            };

            $scope.getEnvList = function () {
                HttpUtils.get('application/setting/env/list', function (data) {
                    $scope.envList = data.data;
                    $scope.applications.forEach(function (item) {
                        if (item.id === $scope.f2CPublisher.applicationId) {
                            $scope.app = item;
                            $scope.businessValueId = $scope.app.businessValueId;
                        }
                    });
                    $scope.envs = [];
                    $scope.envValueIdList = [];
                    if ($scope.app && $scope.app.applicationRepositorySettings.length > 0) {
                        $scope.app.applicationRepositorySettings.forEach(function (appRepoSetting) {
                            let appRepo = null;
                            $scope.repoList.forEach(function (appRepoItem) {
                                if (appRepoSetting.repositoryId === appRepoItem.id) {
                                    appRepo = appRepoItem;
                                }
                            });
                            let envName = '';
                            $scope.envList.forEach(function (envItem) {
                                if (appRepoSetting.envId === envItem.id) {
                                    envName = envItem.tagValueAlias;
                                    $scope.envValueIdList.push(envItem.id);
                                }
                                if (appRepoSetting.envId.toLowerCase() === 'all') {
                                    // $scope.envValueId = 'all';
                                    $scope.envValueIdList.push('all');
                                    envName = '全部环境';
                                }
                            });
                            $scope.envs.push({id: appRepoSetting.id, name: envName + '(' + appRepo.type + ':' + appRepo.name + ')'});
                        });
                    }
                    $scope.getCluster();
                })
            };

            $scope.getCluster = function () {
                HttpUtils.get('cluster/list', function (data) {

                    $scope.clusters = [];

                    data.data.forEach(function (cluster) {
                        if ((!$scope.businessValueId || $scope.businessValueId === cluster.systemValueId)
                            && (($scope.envValueIdList && $scope.envValueIdList.indexOf('all') > -1 ) || $scope.envValueIdList.indexOf(cluster.envValueId) > -1 )
                            && cluster.workspaceId === $scope.f2CPublisher.workspaceId) {
                            $scope.clusters.push(cluster);
                        }
                        // $scope.clusters.push(cluster);
                    });
                    $scope.getClusterRoles();
                })
            };

            $scope.getClusterRoles = function () {
                HttpUtils.get('clusterRole/list?clusterId=' + $scope.f2CPublisher.clusterId, function (data) {
                    $scope.clusterRoles = [];
                    if (data.data.length > 0) {
                        $scope.clusterRoles.push({id: 'ALL', name: '所有主机组'});
                        data.data.forEach(function (item) {
                            if (item.clusterId === $scope.f2CPublisher.clusterId) {
                                $scope.clusterRoles.push(item);
                            }
                        });
                    }
                    $scope.getCloudServers();
                })
            };

            $scope.getCloudServers = function () {
                HttpUtils.post('server/list', {clusterRoleId: $scope.f2CPublisher.clusterRoleId}, function (data) {
                    $scope.cloudServers = [];
                    if (data.data.length > 0) {
                        $scope.cloudServers.push({id: 'ALL', instanceName: '所有主机'});
                        $scope.cloudServers = $scope.cloudServers.concat(data.data);
                    }
                });
            };

            $scope.valiApiTest = function () {
                //已绑定相关信息
                $scope.testHost = 'unknow host';
                $scope.valiApi = false;
                //校验应用绑定的信息是否存在
                if($scope.f2CPublisher.applicationId){
                    HttpUtils.get("application/test/check?id="+$scope.f2CPublisher.applicationId, function (data) {
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
                        if(!$scope.valiApi || !$scope.testHostAvailable){
                            if(!$scope.checkhost){
                                $scope.f2CPublisher.autoApiTest = false;
                            }
                        }
                    });
                }
            };
            $scope.checkTestHostAvailable = function () {
                $scope.testHostAvailable = false;
                $scope.findHost = false;
                if ($scope.valiApi) {
                    HttpUtils.post('server/list', {clusterRoleId: $scope.f2CPublisher.clusterRoleId}, function (data) {
                        if (data.data.length > 0) {
                            for (let i = 0; i < data.data.length; i++) {
                                let s = data.data[i];
                                if ($scope.f2CPublisher.cloudServerId.indexOf("ALL") >= 0 || $scope.f2CPublisher.cloudServerId.indexOf(s.id) >= 0) {
                                    if (s.managementIp === $scope.testHost) {
                                        $scope.findHost = true;
                                        $scope.tip = '目标主机已匹配测试平台环境主机('+$scope.testHost+')';
                                        if(s.connectable){
                                            $scope.testHostAvailable = true;
                                            $scope.tip +='可连接';
                                        }else{
                                            $scope.tip +='不可连接';
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    if(!$scope.findHost && !$scope.testHostAvailable){
                        $scope.tip = '目标主机未匹配测试平台环境主机('+$scope.testHost+')';
                    }
                }
            };

            $scope.changeProduct = function (testProdId) {
                $scope.f2CPublisher.planId = null;
                $scope.loading = HttpUtils.get("application/test/plan/list?prodId=" + testProdId, function (data) {
                    $scope.plans = data.data;
                });
                $scope.f2CPublisher.envName = null;
                $scope.loading = HttpUtils.get("application/test/env/list?prodId=" + testProdId, function (data) {
                    $scope.testEnvs = data.data;
                });
            };

            $scope.loading = HttpUtils.get("application/test/prod/list", function (data) {
                $scope.products = data.data;
            },function (data) {
                Notification.warn(data.message)
            });

            $scope.init = function () {
                $scope.getWorkspaces();
                //$scope.changeProduct(null);
                if(!$scope.f2CPublisher.pollingTimeoutSec){
                    $scope.f2CPublisher.pollingTimeoutSec = 600;
                }
                if(!$scope.f2CPublisher.testPollingTimeoutSec){
                    $scope.f2CPublisher.testPollingTimeoutSec = 150;
                }
                if(!$scope.f2CPublisher.testPollingFreqSec){
                    $scope.f2CPublisher.testPollingFreqSec = 5;
                }
            };

            $scope.init();
        }
    }
}).directive('multibranchF2cPublisher', function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/multibranch-f2c-publisher.html?_t=' + Math.random(),
        replace: true,
        scope: {
            f2CPublisher: '=',
            closeable: '=',
            publishers: '='
        },
        controller: function ($scope, HttpUtils, $element,UserService) {
            $scope.repoTypes = ['Nexus', 'Nexus3', 'Artifactory', '阿里云OSS', '亚马逊S3'];
            $scope.policies = [{
                value: 'all',
                key: '全部同时部署'
            }, {
                value: 'half',
                key: '半数分批部署'
            }, {
                value: 'single',
                key: '单台依次部署'
            }];

            $scope.f2CPublisher.workspaceId = UserService.getWorkSpaceId();

            $element.find('.search-input').on('keydown', function (ev) {
                ev.stopPropagation();
            });

            $scope.clearSearch = function () {
                $scope.appName = '';
                $scope.serverIp = '';
            };

            let checkedDict = {
                'Nexus': 'nexusChecked',
                'Nexus3': 'nexus3Checked',
                'Artifactory': 'artifactoryChecked',
                '阿里云OSS': 'ossChecked',
                '亚马逊S3': 's3Checked'
            };

            // $scope.$watch('f2CPublisher.artifactType', function (newVal, oldVal) {
            //     for (let checkedDictKey in checkedDict) {
            //         $scope.f2CPublisher[checkedDict[checkedDictKey]] = $scope.f2CPublisher.artifactType === checkedDictKey;
            //     }
            // });

            $scope.changeWorkspace = function () {
                $scope.f2CPublisher.applicationId = null;
                $scope.f2CPublisher.repositorySettingId = null;
                $scope.f2CPublisher.clusterId = null;
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                $scope.getApplications();
            };

            $scope.changeApp = function () {
                $scope.f2CPublisher.repositorySettingId = null;
                $scope.f2CPublisher.clusterId = null;
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                $scope.getEnvList();
                // $scope.valiApiTest();
                // $scope.checkTestHostAvailable();
                $scope.f2CPublisher.autoApiTest = false;
            };

            $scope.changeEnv = function () {
                $scope.f2CPublisher.clusterId = null;
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                $scope.getCluster();
                if ($scope.app && $scope.app.applicationRepositorySettings.length > 0) {
                    $scope.app.applicationRepositorySettings.forEach(function (appRepoSetting) {
                        $scope.repoList.forEach(function (appRepoItem) {
                            if (appRepoSetting.repositoryId === appRepoItem.id) {
                                $scope.f2CPublisher.artifactType = appRepoItem.type;
                            }
                        });
                    });
                }
            };

            $scope.changeCluster = function () {
                $scope.clusterRoles = [];
                $scope.f2CPublisher.clusterRoleId = null;
                $scope.f2CPublisher.cloudServerId = null;
                if ($scope.f2CPublisher.clusterId) {
                    $scope.getClusterRoles();
                }
            };

            $scope.changeClusterRole = function () {
                $scope.cloudServers = [];
                $scope.f2CPublisher.cloudServerId = null;
                if ($scope.f2CPublisher.clusterRoleId) {
                    $scope.getCloudServers();
                }
            };

            $scope.getWorkspaces = function () {
                $scope.workspaces = [];
                HttpUtils.get('user/source/list', function (data) {
                    data.data.forEach(function (item) {
                        if (item.type === 'workspace') {
                            $scope.workspaces.push(item);
                        }
                    });
                    $scope.getApplications();
                });
            };

            $scope.getApplications = function () {
                HttpUtils.get("application/list", function (data) {
                    $scope.applications = [];
                    data.data.forEach(function (item) {
                        if (item.scope === 'global' || item.workspaceId === $scope.f2CPublisher.workspaceId) {
                            $scope.applications.push(item);
                        }
                    });
                    $scope.getRepos();
                    // $scope.valiApiTest();
                });
            };

            $scope.valiApiTest = function () {
                //已绑定相关信息
                $scope.testHost = 'unknow host';
                $scope.valiApi = false;
                //校验应用绑定的信息是否存在
                if($scope.f2CPublisher.applicationId){
                    HttpUtils.get("application/test/check?id="+$scope.f2CPublisher.applicationId, function (data) {
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
                        if(!$scope.valiApi || !$scope.testHostAvailable){
                            if(!$scope.checkhost){
                                $scope.f2CPublisher.autoApiTest = false;
                            }
                        }
                    });
                }
            };
            $scope.checkTestHostAvailable = function () {
                $scope.testHostAvailable = false;
                $scope.findHost = false;
                if ($scope.valiApi) {
                    HttpUtils.post('server/list', {clusterRoleId: $scope.f2CPublisher.clusterRoleId}, function (data) {
                        if (data.data.length > 0) {
                            for (let i = 0; i < data.data.length; i++) {
                                let s = data.data[i];
                                if ($scope.f2CPublisher.cloudServerId.indexOf("ALL") >= 0 || $scope.f2CPublisher.cloudServerId.indexOf(s.id) >= 0) {
                                    if (s.managementIp === $scope.testHost) {
                                        $scope.findHost = true;
                                        $scope.tip = '目标主机已匹配测试平台环境主机('+$scope.testHost+')';
                                        if(s.connectable){
                                            $scope.testHostAvailable = true;
                                            $scope.tip +='可连接';
                                        }else{
                                            $scope.tip +='不可连接';
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    if(!$scope.findHost && !$scope.testHostAvailable){
                        $scope.tip = '目标主机未匹配测试平台环境主机('+$scope.testHost+')';
                    }
                }
            };

            $scope.getRepos = function () {
                HttpUtils.post('repository/listAll', {}, function (data) {
                    $scope.repoList = data.data;
                    $scope.getEnvList();
                })
            };

            $scope.getEnvList = function () {
                HttpUtils.get('application/setting/env/list', function (data) {
                    $scope.envList = data.data;
                    $scope.applications.forEach(function (item) {
                        if (item.id === $scope.f2CPublisher.applicationId) {
                            $scope.app = item;
                            $scope.businessValueId = $scope.app.businessValueId;
                        }
                    });
                    $scope.envs = [];
                    $scope.envValueIdList = [];
                    if ($scope.app && $scope.app.applicationRepositorySettings.length > 0) {
                        $scope.app.applicationRepositorySettings.forEach(function (appRepoSetting) {
                            let appRepo = null;
                            $scope.repoList.forEach(function (appRepoItem) {
                                if (appRepoSetting.repositoryId === appRepoItem.id) {
                                    appRepo = appRepoItem;
                                }
                            });
                            let envName = '';
                            $scope.envList.forEach(function (envItem) {
                                if (appRepoSetting.envId === envItem.id) {
                                    envName = envItem.tagValueAlias;
                                    $scope.envValueIdList.push(envItem.id);
                                }
                                if (appRepoSetting.envId.toLowerCase() === 'all') {
                                    $scope.envValueIdList.push('all');
                                    envName = '全部环境';
                                }
                            });
                            $scope.envs.push({id: appRepoSetting.id, name: envName + '(' + appRepo.type + ': ' + appRepo.name + ')'});
                        });
                    }
                    $scope.getCluster();
                })
            };

            $scope.getCluster = function () {
                HttpUtils.get('cluster/list', function (data) {

                    $scope.clusters = [];

                    data.data.forEach(function (cluster) {
                        if ((!$scope.businessValueId || $scope.businessValueId === cluster.systemValueId)
                            && (($scope.envValueIdList && $scope.envValueIdList.indexOf('all') > -1) || $scope.envValueIdList.indexOf(cluster.envValueId) > -1)
                            && cluster.workspaceId === $scope.f2CPublisher.workspaceId) {
                            $scope.clusters.push(cluster);
                        }
                    });
                    $scope.getClusterRoles();
                })
            };

            $scope.getClusterRoles = function () {
                HttpUtils.get('clusterRole/list?clusterId=' + $scope.f2CPublisher.clusterId, function (data) {
                    $scope.clusterRoles = [];
                    if (data.data.length > 0) {
                        $scope.clusterRoles.push({id: 'ALL', name: '所有主机组'});
                        data.data.forEach(function (item) {
                            if (item.clusterId === $scope.f2CPublisher.clusterId) {
                                $scope.clusterRoles.push(item);
                            }
                        });
                    }
                    $scope.getCloudServers();
                })
            };

            $scope.getCloudServers = function () {
                HttpUtils.post('server/list', {clusterRoleId: $scope.f2CPublisher.clusterRoleId}, function (data) {
                    $scope.cloudServers = [];
                    if (data.data.length > 0) {
                        $scope.cloudServers.push({id: 'ALL', instanceName: '所有主机'});
                        $scope.cloudServers = $scope.cloudServers.concat(data.data);
                    }
                });
            };

            $scope.changeProduct = function (testProdId) {
                $scope.f2CPublisher.planId = null;
                $scope.loading = HttpUtils.get("application/test/plan/list?prodId=" + testProdId, function (data) {
                    $scope.plans = data.data;
                });
                $scope.f2CPublisher.envName = null;
                $scope.loading = HttpUtils.get("application/test/env/list?prodId=" + testProdId, function (data) {
                    $scope.testEnvs = data.data;
                });
            };

            $scope.loading = HttpUtils.get("application/test/prod/list", function (data) {
                $scope.products = data.data;
            },function (data) {
                Notification.warn(data.message)
            });

            $scope.init = function () {
                $scope.getWorkspaces();
                //$scope.changeProduct(null);
                if(!$scope.f2CPublisher.pollingTimeoutSec){
                    $scope.f2CPublisher.pollingTimeoutSec = 600;
                }
                if(!$scope.f2CPublisher.testPollingTimeoutSec){
                    $scope.f2CPublisher.testPollingTimeoutSec = 150;
                }
                if(!$scope.f2CPublisher.testPollingFreqSec){
                    $scope.f2CPublisher.testPollingFreqSec = 5;
                }
            };

            $scope.init();
        }
    }
}).directive('mavenBuildStep', function (DeleteService) {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/maven-build-step.html?_t=' + Math.random(),
        replace: true,
        scope: {
            mavenBuildStep: '=',
            title: '@',
            closeable: '=',
            builders: '=',
            mavenVersions: '='
        },
        controller: function ($scope,HttpUtils) {
            $scope.mavenVersions = [{alias: '默认', paramValue: 'default'}].concat($scope.mavenVersions);
            $scope.settingsClasses = [{
                key: '使用默认 Maven 设置',
                value: 'jenkins.mvn.DefaultSettingsProvider'
            }, {
                key: 'Provided settings.xml',
                value: 'org.jenkinsci.plugins.configfiles.maven.job.MvnSettingsProvider'
            }, {
                key: '文件系统中的 settings 文件',
                value: 'jenkins.mvn.FilePathSettingsProvider'
            }];
            $scope.globalSettingsClasses = [{
                key: '使用默认 Maven 全局设置',
                value: 'jenkins.mvn.DefaultGlobalSettingsProvider'
            }, {
                key: 'provided global settings.xml',
                value: 'org.jenkinsci.plugins.configfiles.maven.job.MvnGlobalSettingsProvider'
            }, {
                key: '文件系统中的全局 settings 文件',
                value: 'jenkins.mvn.FilePathGlobalSettingsProvider'
            }];
            $scope.getProvidedSettings = function () {
                HttpUtils.post('jenkins/params/getParams', {paramKey: 'provided_settings'}, function (data) {
                    $scope.providedSettings = data.data;
                });
            };

            $scope.getProvidedGlobalSettings = function () {
                HttpUtils.post('jenkins/params/getParams', {paramKey: 'provided_global_settings'}, function (data) {
                    $scope.providedGlobalSettings = data.data;
                });
            };
            $scope.getProvidedSettings();
            $scope.getProvidedGlobalSettings();
            //设置默认false
            if(!$scope.mavenBuildStep.injectBuildVariables){
                $scope.mavenBuildStep.injectBuildVariables =false;
            }
            if(!$scope.mavenBuildStep.usePrivateRepository){
                $scope.mavenBuildStep.usePrivateRepository =false;
            }
        }
    }
}).directive('sonarBuildStep', function (DeleteService) {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/sonar-build-step.html?_t=' + Math.random(),
        replace: true,
        scope: {
            sonarBuildStep: '=',
            title: '@',
            closeable: '=',
            builders: '=',
        },
        controller: function ($scope,HttpUtils) {
            $scope.getSonarParams = function () {
                HttpUtils.get('jenkins/params/getSonarParams', function (data) {
                    if(data.data){
                        $scope.sonarInstallationNames = data.data.sonarInstallationNames;
                        if(!$scope.sonarBuildStep.installationName && $scope.sonarInstallationNames.length > 0){
                            $scope.sonarBuildStep.installationName = $scope.sonarInstallationNames[0];
                        }
                        $scope.jdkVersions = ['(Inherit From Job)'].concat(data.data.jdkVersions);
                        if(!$scope.sonarBuildStep.jdk && $scope.jdkVersions.length > 0){
                            $scope.sonarBuildStep.jdk = $scope.jdkVersions[0];
                        }
                        $scope.sonarRunnerInstallations = data.data.sonarRunnerInstallations;
                        if(!$scope.sonarBuildStep.sonarScannerName && $scope.sonarRunnerInstallations.length > 0){
                            $scope.sonarBuildStep.sonarScannerName = $scope.sonarRunnerInstallations[0];
                        }
                    }
                });
                HttpUtils.post('jenkins/params/getParams', {paramKey: 'help_for_sonar_properties'}, function (data) {
                    if(data.data && data.data.length > 0){
                        $scope.helpForAnalysis = data.data[0].paramValue;
                    }
                });
            };
            $scope.getSonarParams();
        }
    }
}).directive('antBuildStep', function (DeleteService) {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/ant-build-step.html?_t=' + Math.random(),
        replace: true,
        scope: {
            antBuildStep: '=',
            title: '@',
            closeable: '=',
            builders: '=',
            antNames: '='
        },
        controller: function ($scope,HttpUtils) {
            if(!$scope.antBuildStep.antName && $scope.antNames.length > 0){
                $scope.antBuildStep.antName = $scope.antNames[0].paramValue;
            }
        }
    }
}).directive("shellScript", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/execute-shell.html?_t=' + Math.random(),
        replace: true,
        scope: {
            shell: '=',
            title: '@',
            closeable: '=',
            builders: '='
        },
        controller: function ($scope, $timeout) {
            $timeout(function () {
                $scope.cmOption = {
                    lineNumbers: true,
                    indentWithTabs: true,
                    theme: 'bespin'
                };
            }, 500);
        }
    }
}).directive('buildTrigger', function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/build-trigger.html?_t=' + Math.random(),
        replace: true,
        scope: {
            otherJobs: '=',
            threshold: '=',
            jobItems: '=',
            title: '@',
            closeable: '=',
            builderTrigger: '=',
            publishers: '=',
            styleClass: '@',
            thresholds: '='
        },
        controller: function ($scope) {
            $scope.selectedJobs = $scope.otherJobs ? $scope.otherJobs.split(',') : [];
            $scope.querySearch = function (query) {
                let items = [];
                if ($scope.jobItems) {
                    $scope.jobItems.forEach(function (item) {
                        if (item.name.indexOf(query) > -1) {
                            items.push(item.name);
                        }
                    });
                }
                return items;
            };
            $scope.$watch('selectedJobs', function (newVal) {
                if (newVal) {
                    $scope.otherJobs = $scope.selectedJobs.join(',');
                }
            }, true);
            $scope.$watch("threshold.name", function (newVal) {
                $scope.threshold = $scope.thresholds[newVal];
            });
        }
    }
}).directive("deployToMavenRepo", function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'project/html/jenkins/template/deploy-to-meven-repo.html?_t=' + Math.random(),
        scope: {
            redeployPublisher: '=',
            title: '@',
            closeable: '=',
            publishers: '='
        }
    }
}).directive("emailPublisher", function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'project/html/jenkins/template/email-publisher.html?_t=' + Math.random(),
        scope: {
            emailPublisher: '=',
            title: '@',
            closeable: '=',
            publishers: '=',
            type: '='
        }
    }
}).directive("showMoreButton", function () {
    return {
        restrict: 'E',
        replace: true,
        template:
            '<div layout="row" layout-align="end center">\n' +
            '<md-button class="md-primary md-raised" ng-click="toggleElem()">{{btnName}}</md-button>\n' +
            '</div>',
        scope: {
            showBtnName: '@',
            hideBtnName: '@',
            selector: '@',
            initStatus: '='
        },
        controller: function ($scope) {
            if ($scope.initStatus) {
                angular.element($scope.selector).show();
                $scope.btnName = $scope.hideBtnName ? $scope.hideBtnName : '隐藏';
            } else {
                angular.element($scope.selector).hide();
                $scope.btnName = $scope.showBtnName ? $scope.showBtnName : '显示';
            }
            $scope.toggleElem = function () {
                let elem = angular.element($scope.selector);
                if (elem.css('display') === 'none') {
                    $scope.btnName = $scope.hideBtnName ? $scope.hideBtnName : '隐藏';
                } else {
                    $scope.btnName = $scope.showBtnName ? $scope.showBtnName : '显示';
                }
                angular.element($scope.selector).toggle();
            };
        }
    };
}).directive("patternDiv", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/pattern-div.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            title: '@',
            closeable: '='
        }
    }
}).directive("stringParameter", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/string-parameter.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            title: '@',
            closeable: '='
        }
    }
}).directive("passwordParameter", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/password-parameter.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            title: '@',
            closeable: '='
        }
    }
}).directive("booleanParameter", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/boolean-parameter.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            title: '@',
            closeable: '='
        }
    }
}).directive("fileParameter", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/file-parameter.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            title: '@',
            closeable: '='
        }
    }
}).directive("textParameter", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/text-parameter.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            title: '@',
            closeable: '='
        }
    }
}).directive("runParameter", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/run-parameter.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            filters: '=',
            filter: '@',
            title: '@',
            styleClass: '@',
            closeable: '='
        },
        controller: function ($scope, HttpUtils) {

            // $scope.filter='ALL'
            //用来暂存下拉可选的列表
            $scope.selectjobs=[]
            $scope.querySearch = function (searchText) {

                // 全量搜索构建项目
                let condition = {viewId:null}
                condition.name='%'+searchText+'%'
                HttpUtils.post('jenkins/job/list/' + 1 + "/" + 10, condition, function (response) {
                    // 每次查询前先清空原查询结果数组
                    $scope.selectjobs.splice(0,$scope.selectjobs.length);

                    response.data.listObject.forEach(function (item) {
                        $scope.selectjobs.push(item.name);
                    });

                });
                return $scope.selectjobs;
            };
        }

    }
}).directive("choiceParameter", function () {
    return {
        restrict: 'E',
        templateUrl: 'project/html/jenkins/template/choice-parameter.html?_t=' + Math.random(),
        replace: true,
        scope: {
            parameter: '=',
            parameters: '=',
            title: '@',
            closeable: '='
        }
    }
}).directive('parameterBuild', function () {
    return {
        restrict: 'E',
        replace: true,
        template: '<div></div>'
    };
}).directive('commonProperties', function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'project/html/jenkins/template/common-properties.html?_t=' + Math.random()
    };
}).directive('flowProperties', function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'project/html/jenkins/template/flow-properties.html?_t=' + Math.random()
    };
}).directive('multibranchProperties', function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'project/html/jenkins/template/multibranch-properties.html?_t=' + Math.random()
    };
}).directive('closeBtn', function () {
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'project/html/jenkins/template/close-btn.html?_t=' + Math.random(),
        scope: {
            items: '=',
            item: '='
        },
        controller: function ($scope, DeleteService) {
            $scope.close = DeleteService.deleteItem;
        }
    };
}).directive('chartElem', function ($timeout) {
    return {
        restrict: 'E',
        templateUrl: 'project/html/dashboard/template/echarts-template.html?_t=' + Math.random(),
        replace: true,
        scope: {
            title: '@',
            option: '=',
            elementId: '@'
        },
        link:function ($scope, element) {
            $scope.option.execute = function () {
                $timeout(function () {
                    $scope.echart = echarts.getInstanceByDom(element.find('#' + $scope.elementId)[0]);
                    if ($scope.echart) {
                        $scope.echart.setOption($scope.option, true);
                    }else {
                        $scope.echart = echarts.init(element.find('#' + $scope.elementId)[0], 'fit2cloud-echarts-theme');
                        $scope.echart.setOption($scope.option, true);
                    }
                    $scope.echart.resize();
                });
            };
            $scope.option.execute();
        }
    }
}).directive('h2Anchor', function ($timeout) {
    /**
     * 将<h2-anchor></h2-anchor>的下一个HTML标签内的全部的<h2></h2>标签当做页面锚点
     **/
    return {
        restrict: 'E',
        replace: true,
        templateUrl: 'project/html/jenkins/template/h2-anchor.html?_t=' + Math.random(),
        scope: {},
        link: function (scope, element) {
            $timeout(function () {
                scope.rootElement = element[0].nextElementSibling;
                let h2_elements = [];
                let getAllH2Element = function (dom) {
                    for (let i = 0; i < dom.length; i++) {
                        if (dom[i].nodeName === "H2") {
                            h2_elements = [...h2_elements, {element: dom[i], name: dom[i].innerText}];
                        }
                        if (dom[i].children && dom[i].children.length) {
                            getAllH2Element(dom[i].children)
                        }
                    }
                };
                if (scope.rootElement.children) {
                    getAllH2Element(scope.rootElement.children);
                }
                scope.h2_elements = h2_elements;
                if (h2_elements.length) {
                    scope.active_item = h2_elements[0].name;
                }
                scope.rootElement.onscroll = function (ev) {
                    if (scope.tigger_scroll) {
                        let offsetTop = ev.target.scrollTop;
                        let active_h2 = null;
                        scope.h2_elements.forEach(h2 => {
                            if (h2.element.offsetTop <= offsetTop) {
                                active_h2 = h2;
                            } else {
                                return true;
                            }
                        });
                        if (active_h2 && scope.active_item !== active_h2.name) {
                            scope.active_item = active_h2.name;
                            scope.$apply();
                        }
                    } else {
                        if (scope.timer) {
                            $timeout.cancel(scope.timer);
                        }
                        scope.timer = $timeout(() => {
                            scope.tigger_scroll = true;
                        }, 100)
                    }
                }
            }, 1000);
            scope.tigger_scroll = true;
            scope.touchAnchor = function (item) {
                scope.rootElement.scrollTo({top: item.element.offsetTop, behavior: 'smooth'});
                scope.active_item = item.name;
                scope.tigger_scroll = false;
                if (scope.timer) {
                    $timeout.cancel(scope.timer);
                }
                scope.timer = $timeout(() => {
                    scope.tigger_scroll = true;
                }, 100)
            }
        }
    };
});