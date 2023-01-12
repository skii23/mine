ProjectApp.controller('DashboardCtrl', function ($scope, $state, $mdDialog, $http, FilterSearch, Notification, HttpUtils, DateSelectService, UserService, $document, $timeout) {

    // 定义搜索条件
    $scope.conditions = [
        {
            key: 'workspaceId',
            name: '工作空间',
            directive: 'filter-select-virtual',
            url: 'structure/getWorkspaces',
            convert: { value: 'id', label: 'name' }
        }
    ];

    // 用于传入后台的参数
    $scope.filters = [];

    $scope.columns = [
        { value: '工作空间', key: 'workspaceName', width: '15%', sort: false },
        { value: '应用数', key: 'appCount', width: '15%', sort: false },
        { value: '部署次数', key: 'deployCount', width: '15%', sort: false },
        { value: '部署成功次数', key: 'successDeployCount', sort: false, width: '15%' },
        { value: '构建任务数', key: 'jobCount', width: '15%', sort: false },
        { value: '构建次数', key: 'buildCount', sort: false },
        { value: '构建成功次数', key: 'successBuildCount', sort: false }
    ];


    $scope.groupDatePeriod = DateSelectService.DATE_SELECT_TYPE.LAST_7_DAYS;
    $scope.groupDateParam = DateSelectService.calculateDate($scope.groupDatePeriod);

    $scope.list = function (sortObj) {
        // console.log('++++++++++ list', sortObj);
        let condition = FilterSearch.convert($scope.filters);
        if (sortObj) {
            $scope.sort = sortObj;
        }
        if ($scope.sort) {
            condition.sort = $scope.sort.sql;
        }
        condition.start = $scope.groupDateParam.start;
        condition.end = $scope.groupDateParam.end;
        HttpUtils.paging($scope, 'dashboard/getGroupAnalysis', condition)
    };

    if (UserService.isAdmin()) {
        $scope.conditions.push({
            key: 'organizationId',
            name: '组织',
            directive: 'filter-select-virtual',
            url: 'structure/getOrganizations',
            convert: { value: 'id', label: 'name' }
        });
        $scope.columns.splice(0, 0, { value: '组织', key: 'organizationName', width: '15%', sort: false })
    }

    if (UserService.isAdmin() || UserService.isOrgAdmin()) {
        $scope.list();
    }

    $scope.dateBox = [
        { key: DateSelectService.DATE_SELECT_TYPE.LAST_7_DAYS, value: '最近7天' },
        { key: DateSelectService.DATE_SELECT_TYPE.LAST_30_DAYS, value: '最近30天' },
        { key: DateSelectService.DATE_SELECT_TYPE.LAST_3_MONTHS, value: '最近3月' },
        { key: DateSelectService.DATE_SELECT_TYPE.LAST_6_MONTHS, value: '最近6月' },
        { key: DateSelectService.DATE_SELECT_TYPE.LAST_12_MONTHS, value: '最近12个月' },
        { key: DateSelectService.DATE_SELECT_TYPE.RANGE, value: '其他' }
    ];
    $scope.buildWorkspaces = [];
    $scope.deployWorkspaces = [];

    /***************** 度量指标tab开始 ***********************/
    // console.log('++++ 度量指标tab开始', UserService);
    $scope.dateRangeOptions = [
        { label: '近7天', value: 'lastweek' },
        { label: '近14天', value: 'last2week' },
        { label: '近1个月', value: 'lastmonth' }
    ];

    $scope.workspaceId = UserService.getWorkSpaceId();
    $scope.organizationId = UserService.getOrganizationId();
    $scope.showDetail = false; // 是否展示详情内容
    // 图表切换日期范围方法
    $scope.changeChartRange = function (type, range, chartName) {
        $scope.$evalAsync(
            function ($scope) {
                // console.log('changeChartRange +++', type, $scope);
                $scope[type]['select' + chartName + 'Range'] = range;
                $scope['set' + chartName + 'Data']();
            }
        );
    }
    $scope.sprint = {
        currentSprintId: null, // 当前冲刺id
        currentSprint: {}, // 当前冲刺
        sprintList: [], // 冲刺列表
        currentSprintDetail: {}, // 当前冲刺详情
        selectdailyIssueRange: 'lastmonth', // 当前选中的时间范围
        dailyIssueData: [] // 每日需求总数
    }
    $scope.statusBiz = {  // 状态字典值
        active: '进行中'
    };

    $scope.testIp = ''; // http://192.25.160.182:6606/devops/

    const defaultSprintDetail = {
        id: '0', // 'string, sprint id'
        name: '无', // “name”: 'string, 冲刺名称',
        status: null, // “status”： 'string， sprint状态，进行中，已完成等',
        originBoardId: '',
        startTime: null, // "startTime" "long , 开始时间",
        endTime: null, // "long, 结束时间，若未结束，可以为空",
        issue: {
            total: 'Na', // "long, 冲刺里的需求总数，包括特性、故事，任务，测试，bug",
            avgSpendTime: 'Na', // "long, 已完成的需求平均时长，只计算已完成的需求"，
            inProcessCount: 'Na', // "long, 进行中的需求数，包括新建、开发、测试"，
            finishCount: 'Na', // "long, 已完成的需求数",
            featureCount: 'Na', // “long, 属于特性类型的issue数量”，
            stroyCount: 'Na', // “long, 属于故事类型的issue数量”，
            testCount: 'Na', // "long, 属于测试类型的issue数量",，
            bugCount: 'Na' // “long, 属于bug或者故障类型的issue数量”，
        }
    };

    $scope.defaultChartData = function (range, value) {
        const now = +new Date();
        const day = 24 * 60 * 60000;
        const data = []
        for (let i = 0; i < range; i++) {
            data.push([now - (day * i), value]);
        }
        return data;
    }

    $scope.getSprintList = function () {
        $scope.sprint.sprintList = [];
        HttpUtils.get($scope.testIp + 'measure/sprint/list/1/500', function (res) {
            // console.log('getSprintList +++', res);
            $scope.$evalAsync(
                function ($scope) {
                    const data = res.data;
                    $scope.sprint.sprintList = data;
                    $scope.sprint.currentSprintId = data.length > 0 ? data[0].id : '';
                    $scope.sprint.currentSprint = data.length > 0 ? data[0] : {};
                    if (data.length > 0) {
                        $scope.getSprintDetail(data[0].id);
                        $scope.getDailyIssueData(data[0].id);
                    } else {
                        $scope.sprint.currentSprintDetail = angular.copy(defaultSprintDetail);
                        $scope.sprint.dailyIssueData = $scope.defaultChartData(30, 0);
                        $scope.setdailyIssueData();
                    }
                }
            );
        }, function (err) {
            $scope.sprint.currentSprintDetail = angular.copy(defaultSprintDetail);
            $scope.sprint.dailyIssueData = $scope.defaultChartData(30, 0);
            $scope.setdailyIssueData();
        });
    }


    $scope.getSprintDetail = function (sprintid) {
        HttpUtils.get($scope.testIp + 'measure/sprint/metrics/' + sprintid, function (res) {
            $scope.$evalAsync(
                function ($scope) {
                    $scope.sprint.currentSprintDetail = res.data;
                }
            );
        });
    }

    $scope.sprintChange = function () {
        // console.log('sprintChange +++', $scope.sprint.currentSprintId);
        $scope.$evalAsync(
            function ($scope) {
                $scope.sprint.currentSprint = $scope.sprint.sprintList.find(item => item.id === $scope.sprint.currentSprintId) || {};
                $scope.getSprintDetail($scope.sprint.currentSprintId);
                $scope.getDailyIssueData($scope.sprint.currentSprintId);
            });
    }

    $scope.toggleCnt = function () {
        // console.log('+++ toggleCnt', $scope);
        $scope.$evalAsync(
            function ($scope) {
                $scope.showDetail = !$scope.showDetail;
                if (!$scope.showDetail) $scope.currentAppId = null;
            }
        );
    }

    // 成功图表区域渐变设置
    const successAreaStyle = {
        opacity: 0.8,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
                offset: 0,
                color: 'rgba(38,151,255,0.25)'
            },
            {
                offset: 1,
                color: 'rgba(38,151,255,0)'
            }
        ])
    };

    // 失败图表区域渐变设置
    const errorAreaStyle = {
        opacity: 0.8,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
                offset: 0,
                color: 'rgba(255,100,115,0.25)'
            },
            {
                offset: 1,
                color: 'rgba(255,100,115,0)'
            }
        ])
    };

    // series通用配置
    const areaStackSradientSerie = {
        type: 'line',
        areaStyle: {},
        emphasis: {
            focus: 'series'
        },
        areaStyle: angular.copy(successAreaStyle)
    };

    // 折线图通用配置
    const areaStackSradientSetting = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            }
        },
        legend: {
            data: []
        },
        grid: {
            left: '0',
            right: '5%',
            bottom: '0',
            containLabel: true
        },
        xAxis: [
            {
                type: 'time',
                boundaryGap: false,
                splitLine: false,
                axisLabel: {
                    formatter: function (value, index) {
                        // 格式化成月/日，只在第一个刻度显示年份
                        var date = new Date(value);
                        var texts = [(date.getMonth() + 1), date.getDate()];
                        if (index === 0) {
                            texts.unshift(date.getFullYear());
                        }
                        return texts.join('-');
                    }
                },
                data: []
            }
        ],
        yAxis: [
            {
                type: 'value',
                splitLine: false
            }
        ],
        series: []
    };
    $scope.setdailyIssueData = function () {
        const allData = angular.copy($scope.sprint.dailyIssueData).sort(function (a, b) { return b[0] - a[0] });
        const filterData = $scope.sprint.selectdailyIssueRange === 'lastweek' ? allData.slice(0, 7) :
            $scope.sprint.selectdailyIssueRange === 'last2week' ? allData.slice(0, 14) : allData;
        $scope.dailyIssueSetting.series[0].data = filterData;
        $scope.dailyIssueSetting.execute && $scope.dailyIssueSetting.execute();
    }

    $scope.dailyIssueSetting = angular.copy(areaStackSradientSetting);
    $scope.dailyIssueSetting.series.push(angular.copy(areaStackSradientSerie));
    $scope.getDailyIssueData = function (sprintid) {
        sprintid || (sprintid = $scope.sprint.currentSprintId);
        HttpUtils.get($scope.testIp + 'measure/sprint/' + sprintid + '/issue/30', function (res) {
            $scope.$evalAsync(
                function ($scope) {
                    // $scope.sprint.dailyIssueData = getMockData();
                    const days = Object.keys(res.data);
                    const cnts = Object.values(res.data);
                    $scope.sprint.dailyIssueData = days.map((item, index) => { return [item - 0, cnts[index] - 0]; });
                    $scope.setdailyIssueData();
                }
            );
        });
    }

    /******************* 应用 *********************/
    // {
    //     “id”: "string, 应用唯一id",
    //     “name”: "string, 应用名称",
    //     “commitDaily”： "int， 每日构建",
    //     "buildFailRate" "int , 构建失败率，百分比",
    //     “buildAvgPeriod”: "int, 构建时长",
    //     “unitTestSuccessRate”: "int, 单测成功率",
    //     “unitTestCoverageRate”: "int, 单测覆盖率",
    //     “deploySuccesRate”: "int, 部署成功率",
    //     “apiTestSuccesRate”: "int, API测试成功率",
    //   },
    $scope.appFilters = {
        keyword: ''
    };
    $scope.app = {
        list: [], // 全部应用列表
        pagesize: 10,
        currentPage: 1,
        pagination: [],
        listTotal: 0,
        showPage: 0
    };
    $scope.customize = {
        list: [], // 自选应用列表
        pagesize: 10,
        currentPage: 1,
        pagination: [],
        listTotal: 0,
        showPage: 0
    };
    $scope.null20 = function (obj, fields) {
        fields.forEach(field => {
            (typeof obj[field] === 'undefined' || !obj[field]) &&
                (obj[field] = 0);
        });
        return angular.copy(obj);
    }
    $scope.getappList = function () {
        // measure/app/list/{go pages}/{page size}
        HttpUtils.get('measure/app/list/' + $scope.app.currentPage + '/' + $scope.app.pagesize, function (res) {
            $scope.$evalAsync(
                function ($scope) {
                    $scope.app.list = res.data.map(item => {
                        item.buildAvgPeriodFormat = $scope.formatDate(item.buildAvgPeriod, true);
                        return item;
                    });
                }
            );
        });
    }
    $scope.getappListTotal = function () {
        HttpUtils.get('measure/app/list/total', function (res) {
            $scope.$evalAsync(
                function ($scope) {
                    $scope.app.listTotal = res.data;
                    $scope.setPagination('app');
                }
            );
        });
    }
    $scope.getcustomizeList = function () {
        // measure/app/list/{go pages}/{page size}
        HttpUtils.get('measure/app/customize/list/' + $scope.customize.currentPage + '/' + $scope.customize.pagesize, function (res) {
            $scope.$evalAsync(
                function ($scope) {
                    $scope.customize.list = res.data.map(item => {
                        item.buildAvgPeriodFormat = $scope.formatDate(item.buildAvgPeriod, true);
                        return item;
                    });
                }
            );
        });
    }
    $scope.getcustomizeListTotal = function () {
        HttpUtils.get('measure/app/customize/list/total', function (res) {
            $scope.$evalAsync(
                function ($scope) {
                    $scope.customize.listTotal = res.data;
                    $scope.setPagination('customize');
                }
            );
        });
    }
    $scope.appcolumns = [
        { value: '应用', key: 'name', width: '30%', sort: false },
        { value: '每日commit', key: 'commitDaily', width: '10%', sort: false },
        { value: '构建失败率', key: 'buildFailRate', width: '10%', sort: false },
        { value: '构建时长', key: 'buildAvgPeriod', sort: false, width: '10%' },
        { value: '单测覆盖率', key: 'unitTestCoverageRate', width: '10%', sort: false },
        { value: '单测成功率', key: 'unitTestSuccessRate', width: '10%', sort: false },
        { value: '部署成功率', key: 'deploySuccesRate', width: '10%', sort: false },
        { value: 'API成功率', key: 'apiTestSuccesRate', width: '10%', sort: false },
        { value: '操作', key: 'operate', width: '20%', sort: false }
    ];

    $scope.columnsWarnValue = {
        buildFailRate: 50,
        buildAvgPeriod: 10 * 60 * 1000,
        unitTestCoverageRate: 50,
        unitTestSuccessRate: 50,
        deploySuccesRate: 50,
        apiTestSuccesRate: 50,
    };
    $scope.clearCnt = function (field) {
        $scope.$evalAsync(
            function ($scope) {
                $scope.appFilters[field] = '';
            });
    }
    $scope.setPagination = function (type) {
        if (!type) return;
        const pagination = [];
        const pages = Math.ceil($scope[type].listTotal / $scope[type].pagesize)
        for (let i = 0; i < pages; i++) {
            pagination.push(i + 1);
        }
        // console.log('setPagination+++', pagination);
        $scope.$evalAsync(
            function ($scope) {
                $scope[type].pagination = pagination;
            }
        );
    }

    $scope.prevPage = function (type) {
        if ($scope[type].showPage === 0) return;
        $scope.$evalAsync(
            function ($scope) {
                $scope[type].showPage = $scope[type].showPage - 5;
            }
        );
    }

    $scope.nextPage = function (type) {
        if ($scope[type].pagination.length <= $scope[type].showPage + 5) return;
        $scope.$evalAsync(
            function ($scope) {
                $scope[type].showPage = $scope[type].showPage + 5;
            }
        );
    }

    $scope.changePage = function (type, page) {
        // console.log('changePage ++++', type, page);
        $scope.$evalAsync(
            function ($scope) {
                $scope[type].currentPage = page;
                $scope['get' + type + 'List']();
            }
        );
    }

    $scope.jumpDetail = function (app) {
        $scope.$evalAsync(
            function ($scope) {
                $scope.showDetail = true;
                $scope.currentAppId = app.id;
                $scope.currentApp = app
            }
        );
    }

    $scope.addCustomize = function (type, app) {
        HttpUtils.post('measure/app/customize/add/' + app.id, {}, function (res) {
            $scope.$evalAsync(
                function ($scope) {
                    const list = angular.copy($scope[type].list);
                    list.find(item => item.id === app.id).isCustomized = true;
                    $scope[type].list = list;
                }
            );
            $scope.getcustomizeList();
            $scope.getcustomizeListTotal();
        });
    }
    $scope.removeCustomize = function (type, app) {
        HttpUtils.post('measure/app/customize/remove/' + app.id, {}, function (res) {
            $scope.getcustomizeList();
            $scope.getcustomizeListTotal();
            if (type === 'customize') {
                $scope.getappList();
                $scope.getappListTotal();
                return;
            }
            $scope.$evalAsync(
                function ($scope) {
                    const list = angular.copy($scope[type].list);
                    list.find(item => item.id === app.id).isCustomized = false;
                    $scope[type].list = list;
                }
            );
        });
    }

    $scope.formatDate = function (timestamp, isFull, suffix) {
        if (!timestamp || isNaN(timestamp - 0)) return timestamp;
        timestamp = Math.floor(timestamp / 1000);
        if (timestamp === 0) return '0秒';
        const day = { val: parseInt(timestamp / (24 * 60 * 60)), suffix: '天' };
        const hours = { val: parseInt(timestamp / (60 * 60) % 24), suffix: '小时' };
        const minutes = { val: parseInt(timestamp / 60 % 60), suffix: '分钟' };
        const seconds = { val: parseInt(timestamp % 60), suffix: '秒' };
        const arr = [day, hours, minutes, seconds];
        let str = '';
        for (let i = 0; i < arr.length; i++) {
            const item = arr[i];
            if (item.val > 0) { str += item.val + item.suffix; if (!isFull) break; }
        }
        suffix && (str += suffix);
        return str;
    }
    /***************** 度量指标tab结束 ***********************/

    $scope.init = function () {
        $scope.getSprintList();
        $scope.getappList();
        $scope.getappListTotal();
        $scope.getcustomizeList();
        $scope.getcustomizeListTotal();
    };

    $scope.init();

});

ProjectApp.directive('detail', function (HttpUtils, MenuRouter, $rootScope, $log, $controller, $timeout, $filter, $mdSidenav) {
    return {
        restrict: 'E',
        templateUrl: 'project/html/dashboard/measure-detail.html' + '?_t=' + window.appversion,
        link: function ($scope, element) {
            $scope.initRequestCnt = 0;
            $scope.initTotalRequest = 5;
            /**************************************** 应用概要 ****************************************/
            const defaultOverall = {
                score: 0, // float, 应用评分
                name: '', // 应用名称
                description: '', // 应用描述，不超30个字
                env: '', // 应用环境，开发环境，测试环境等
                repo: '', // 制品库，如harbor，nexsus等
                jobs: 0, // 构建任务数量
                versions: 0, // 版本数
                latestVersion: '', // 当前最新版本
                deployVersion: '', //  当前部署的版本
            };
            $scope.jumpToApp = function () {
                const url = '/devops/?banner=false#!/application';
                sessionStorage.setItem('module', url.split("#!")[0]);
                sessionStorage.setItem('url', url.split("#!")[1]);
                sessionStorage.setItem('appName', $scope.overall.name);
                window.open(window.parent.location.href);
            }
            $scope.overall = angular.copy(defaultOverall);
            $scope.overallScoreImages = [];
            $scope.setScoreImages = function (score, type) {
                // console.log('setScoreImages+++', score, type);
                const temp = [];
                for (let index = 1; index < 6; index++) {
                    const src = index <= score ? 'project/images/star-' + (type === 'overall' ? 'total-' : '') + 'full.png' :
                        index - 1 < score ? 'project/images/star-' + (type === 'overall' ? 'total-' : '') + 'half.png' : 'project/images/star-' + (type === 'overall' ? 'total-' : '') + 'empty.png';
                    temp.push({ src: src });
                }
                $scope.$evalAsync(
                    function ($scope) {
                        $scope[type + 'ScoreImages'] = temp;
                    }
                );
            };

            $scope.getOvaerAll = function () {
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/overall', function (response) {
                    $scope.$evalAsync(
                        function ($scope) {
                            delete response.data.score;
                            $scope.overall = Object.assign({}, $scope.overall, response.data);;
                            // $scope.setScoreImages($scope.overall.score, 'overall');
                        }
                    );
                });
            }
            $scope.setScoreImages($scope.overall.score, 'overall');

            $scope.setOverallScore = function () {
                let overallScore = Math.ceil(($scope.code.score * 10 + $scope.build.score * 10 + $scope.quality.score * 10 + $scope.deploy.score * 10 + $scope.apiTest.score * 10) / 25) * 5 / 10;
                $scope.$evalAsync(
                    function ($scope) {
                        $scope.overall.score = overallScore;
                        $scope.setScoreImages($scope.overall.score, 'overall');
                    }
                );
                // console.log('setOverallScore +++', overallScore);
            }

            /**************************************** 代码 ****************************************/
            const defaultCode = {
                score: 0, //float, 应用评分,3.5,
                repoCount: 0, //int, 仓库数,
                selectid: 'all', // 当前选中的仓库id, all 表示全部
                selectRepo: {}, // 当前选中的repo
                selectdailyCommitRange: 'lastmonth', // 当前选中的时间范围
                dailyCommitData: [], // 近一个月每日提交次数的数据
                dailyCommitterData: [], // 近一个月每日贡献者的数据
                repos: [
                    { id: 'all', name: '全部' }
                ]
            };
            const allRepo = {
                id: 'all', // string 仓库id
                name: '全部', // string, 仓库名称
                branchCount: 0, // long, 分支数
                tagCount: 0, // long, 标签数
                mrCount: 0, // long, MR数量
                commitCount: 0, // long, 代码提交次数
                committerCount: 0, // long, 代码贡献者人数

            };
            $scope.code = angular.copy(defaultCode)
            $scope.selectRepo = {};

            $scope.codeScoreImages = [];
            $scope.getCode = function () {
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/code/1/500', function (response) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const data = Object.assign({}, $scope.code, response.data);
                            const repo = angular.copy(allRepo);
                            $scope.null20(data, ['score', 'repoCount']);
                            data.repos.forEach(element => {
                                element.id === null && (element.id = element.name);
                                $scope.null20(element, ['branchCount', 'tagCount', 'mrCount', 'commitCount', 'committerCount']);
                                for (const key in repo) {
                                    if (Object.hasOwnProperty.call(repo, key)) {
                                        const value = repo[key];
                                        isNaN(value - 0) || (repo[key] = value + (!element[key] ? 0 : element[key]))
                                    }
                                }
                                return element;
                            });
                            // console.log('repo +++', repo);
                            data.repos.unshift(repo);
                            data.selectid = 'all';
                            $scope.code = data;
                            $scope.repoChange();
                            $scope.setScoreImages($scope.code.score, 'code');
                            $scope.initRequestCnt++;
                            $scope.initRequestCnt === $scope.initTotalRequest && $scope.setOverallScore();
                        }
                    );
                });
            }
            $scope.setScoreImages($scope.code.score, 'code');

            const getMockData = function (time) {
                time = time ? time : 30;
                let base = +new Date(2022, 9, 3);
                let oneDay = 24 * 3600 * 1000;
                let data = [[base, Math.random() * 300]];
                for (let i = 1; i < time; i++) {
                    let now = new Date((base += oneDay));
                    data.push([+now, Math.floor(Math.round((Math.random() - 0.5) * 50 + data[i - 1][1]))]);
                }
                return data;
            }
            // 成功图表区域渐变设置
            const successAreaStyle = {
                opacity: 0.8,
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    {
                        offset: 0,
                        color: 'rgba(38,151,255,0.25)'
                    },
                    {
                        offset: 1,
                        color: 'rgba(38,151,255,0)'
                    }
                ])
            };

            // 失败图表区域渐变设置
            const errorAreaStyle = {
                opacity: 0.8,
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    {
                        offset: 0,
                        color: 'rgba(255,100,115,0.25)'
                    },
                    {
                        offset: 1,
                        color: 'rgba(255,100,115,0)'
                    }
                ])
            };

            // series通用配置
            const areaStackSradientSerie = {
                type: 'line',
                areaStyle: {},
                emphasis: {
                    focus: 'series'
                },
                areaStyle: angular.copy(successAreaStyle)
            };

            // 折线图通用配置
            const areaStackSradientSetting = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'cross',
                        label: {
                            backgroundColor: '#6a7985'
                        }
                    }
                },
                legend: {
                    data: []
                },
                grid: {
                    left: '0',
                    right: '5%',
                    bottom: '0',
                    containLabel: true
                },
                xAxis: [
                    {
                        type: 'time',
                        boundaryGap: false,
                        splitLine: false,
                        axisLabel: {
                            formatter: function (value, index) {
                                // 格式化成月/日，只在第一个刻度显示年份
                                var date = new Date(value);
                                var texts = [(date.getMonth() + 1), date.getDate()];
                                if (index === 0) {
                                    texts.unshift(date.getFullYear());
                                }
                                return texts.join('-');
                            }
                        },
                        data: []
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        splitLine: false
                    }
                ],
                series: []
            };

            // $scope.code.dailyCommitData = getMockData();
            $scope.dailyCommitSetting = angular.copy(areaStackSradientSetting);
            $scope.dailyCommitSetting.series.push(angular.copy(areaStackSradientSerie));
            $scope.setdailyCommitData = function () {
                const allData = angular.copy($scope.code.dailyCommitData).sort(function (a, b) { return b[0] - a[0] });
                const filterData = $scope.code.selectdailyCommitRange === 'lastweek' ? allData.slice(0, 7) :
                    $scope.code.selectdailyCommitRange === 'last2week' ? allData.slice(0, 14) : allData;
                $scope.dailyCommitSetting.series[0].data = filterData;
                $scope.dailyCommitSetting.execute && $scope.dailyCommitSetting.execute();
            }
            $scope.getDailyCommitCnt = function (range) {
                range || (range = 1);
                const allData = angular.copy($scope.code.dailyCommitData).sort(function (a, b) { return b[0] - a[0] });
                let result = 0;
                if (allData.length < 1) return result;
                for (let i = 0; i < range; i++) {
                    result += allData[i][1];
                }
                return result;
            }
            $scope.getDailyCommitterCnt = function (range) {
                range || (range = 1);
                const allData = angular.copy($scope.code.dailyCommitterData).sort(function (a, b) { return b[0] - a[0] });
                let result = 0;
                if (allData.length < 1) return result;
                for (let i = 0; i < range; i++) {
                    result += allData[i][1];
                }
                return result;
            }
            $scope.getDailyCommit = function () {
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/code/dailyCommit/30', function (res) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const days = Object.keys(res.data);
                            const cnts = Object.values(res.data);
                            $scope.code.dailyCommitData = days.map((item, index) => { return [item - 0, cnts[index] - 0]; });
                            $scope.setdailyCommitData();
                        }
                    );
                }, function (err) {
                    $scope.$evalAsync(
                        function ($scope) {
                            $scope.code.dailyCommitData = $scope.defaultChartData(30, 0);;
                            $scope.setdailyCommitData();
                        }
                    );
                }, { headers: { repoId: $scope.code.selectid } });
            }
            $scope.getDailyCommitter = function () {
                // /devops/measure/app/{id}/code/{repoId}/dailyCommitter/{some days ago}
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/code/dailyCommitter/30', function (res) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const days = Object.keys(res.data);
                            const cnts = Object.values(res.data);
                            $scope.code.dailyCommitterData = days.map((item, index) => { return [item - 0, cnts[index] - 0]; });
                        }
                    );
                }, function (err) {
                    $scope.$evalAsync(
                        function ($scope) {
                            $scope.code.dailyCommitterData = $scope.defaultChartData(30, 0);;
                        }
                    );
                }, { headers: { repoId: $scope.code.selectid } });
            }
            $scope.repoChange = function () {
                const selectRepo = $scope.code.repos.find(item => $scope.code.selectid && item.id === $scope.code.selectid);
                if (!selectRepo) return;
                $scope.$evalAsync(
                    function ($scope) {
                        $scope.selectRepo = selectRepo;
                        $scope.getDailyCommit();
                        $scope.getDailyCommitter();
                    }
                );
            }
            $scope.setdailyCommitData();

            /**************************************** 构建 ****************************************/
            Object.assign($scope.columnsWarnValue, {
                build_buildFailRate: 20
            });
            const defaultBuild = {
                score: 0, // float, 应用评分,3.5,
                jobCount: 0,// int, 构建任务数,
                selectid: 'all', // 当前选中的构建任务id, -1表示全部
                selectJob: {}, // 当前选中的repo
                selectdailyBuildRange: 'lastmonth', // 当前选中的时间范围
                selectbuildTimeRange: 'lastmonth', // 当前选中的时间范围
                dailyBuildSucData: [], // 近一个月每日构建成功的数据
                dailyBuildErrData: [], // 近一个月每日构建失败的数据
                buildTimeData: [], // 近一个月平均构建时长
                jobs: [
                    { id: 'all', name: '全部' }
                ]
            };
            const allJob = {
                id: 'all', // string， 构建任务id
                name: '全部', // string, 名称
                latesStatus: '', // string, 最新构建状态（成功、失败），按时间排序
                latestJobSn: null, // long, 最新构建任务编号
                latesBuildTime: null, // int, 日期，最新构建时间
                buildCount: 0, // long, 构建次数
                buildTime: 0, // long, 构建平均时长,毫秒
                buildFailRate: 0, // long, 构建失败率
            };
            $scope.build = angular.copy(defaultBuild);
            $scope.selectJob = {};
            $scope.lastBuildList = [];
            $scope.buildCntList = [];
            $scope.getBuild = function () {
                $scope.lastBuildList = [];
                $scope.buildCntList = [];
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/build/1/500', function (response) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const data = Object.assign({}, $scope.build, response.data);
                            const job = angular.copy(allJob);
                            $scope.null20(data, ['score', 'jobCount']);
                            data.jobs.forEach(element => {
                                element.id === null && (element.id = element.name);
                                $scope.null20(element, ['buildFailRate', 'buildCount', 'buildTime']);
                                element.latesBuildTime && (element.latesBuildTime = +new Date() - element.latesBuildTime);
                                job.buildCount += element.buildCount;
                                job.buildTime += element.buildTime;
                                job.buildFailRate += element.buildFailRate;
                                // console.log('++++++', element.buildFailRate, job.buildFailRate);

                                $scope.lastBuildList.push({
                                    id: element.id,
                                    name: element.name,
                                    latesBuildTime: element.latesBuildTime,
                                    buildTime: element.buildTime
                                });
                                $scope.buildCntList.push({
                                    id: element.id,
                                    name: element.name,
                                    buildCount: element.buildCount,
                                    buildFailRate: element.buildFailRate
                                });
                                return element;
                            });
                            job.buildFailRate = data.jobs.length > 0 ? Math.floor(job.buildFailRate / data.jobs.length) : 0;
                            // console.log('++++++', job);
                            data.jobs.unshift(job);
                            data.selectid = 'all';
                            $scope.build = data;
                            $scope.jobChange();
                            $scope.setScoreImages($scope.build.score, 'build');
                            $scope.initRequestCnt++;
                            $scope.initRequestCnt === $scope.initTotalRequest && $scope.setOverallScore();
                        }
                    );
                }, function (err) {
                    $scope.$evalAsync(
                        function ($scope) {
                            $scope.build = Object.assign($scope.build, { jobs: [angular.copy(allJob)] });
                            $scope.initRequestCnt++;
                            $scope.selectJob = $scope.build.jobs[0];
                            $scope.initRequestCnt === $scope.initTotalRequest && $scope.setOverallScore();
                        }
                    )
                });
            }
            $scope.jumpToBuild = function (item) {
                const url = '/devops/?banner=false#!/jenkins-job';
                sessionStorage.setItem('module', url.split("#!")[0]);
                sessionStorage.setItem('url', url.split("#!")[1]);
                sessionStorage.setItem('buildName', item.name);
                window.open(window.parent.location.href);
            }
            // $scope.build.jobs.unshift({ id: -1, name: '全部' });
            $scope.getLastBuildList = function () {
                return $scope.lastBuildList.filter(item => $scope.selectJob.id === 'all' || item.id === $scope.selectJob.id);
            }
            $scope.getBuildCntList = function () {
                return $scope.buildCntList.filter(item => $scope.selectJob.id === 'all' || item.id === $scope.selectJob.id);
            }

            $scope.jobChange = function () {
                const matchItem = $scope.build.jobs.find(item => item.id === $scope.build.selectid);
                if (!matchItem) return;
                $scope.$evalAsync(
                    function ($scope) {
                        $scope.selectJob = matchItem;
                        $scope.getDailyBuild();
                        $scope.getBuildTime();
                    }
                );
            }

            // 构建评分
            $scope.buildScoreImages = [];
            $scope.setScoreImages($scope.build.score, 'build');

            // console.log('+++++ dailyBuildSetting start');
            // 每日构建次数表
            $scope.getDailyBuild = function () {
                // /devops/measure/app/{id}/build/{repoId}/dailyBuild/{some days ago}
                let dailyBuildrequestCnt = 0;
                HttpUtils.get($scope.testIp + 'measure/app/' + $scope.currentAppId + '/build/' + encodeURIComponent($scope.build.selectid) + '/dailyBuild/30', function (res) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const days = Object.keys(res.data);
                            const cnts = Object.values(res.data);
                            $scope.build.dailyBuildSucData = days.map((item, index) => { return [item - 0, cnts[index] - 0]; });
                            dailyBuildrequestCnt++;
                            dailyBuildrequestCnt === 2 && $scope.setdailyBuildData();
                        }
                    );
                }, function (err) {
                    $scope.$evalAsync(
                        function ($scope) {
                            $scope.build.dailyBuildSucData = $scope.defaultChartData(30, 0);;
                            dailyBuildrequestCnt++;
                            dailyBuildrequestCnt === 2 && $scope.setdailyBuildData();
                        }
                    );
                }, { params: { status: 'success' } });
                HttpUtils.get($scope.testIp + 'measure/app/' + $scope.currentAppId + '/build/' + encodeURIComponent($scope.build.selectid) + '/dailyBuild/30', function (res) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const days = Object.keys(res.data);
                            const cnts = Object.values(res.data);
                            $scope.build.dailyBuildErrData = days.map((item, index) => { return [item - 0, cnts[index] - 0]; });
                            dailyBuildrequestCnt++;
                            dailyBuildrequestCnt === 2 && $scope.setdailyBuildData();
                        }
                    );
                }, function (err) {
                    $scope.$evalAsync(
                        function ($scope) {
                            $scope.build.dailyBuildErrData = $scope.defaultChartData(30, 0);;
                            dailyBuildrequestCnt++;
                            dailyBuildrequestCnt === 2 && $scope.setdailyBuildData();
                        }
                    );
                }, { params: { status: 'fail' } });
            }
            $scope.dailyBuildSetting = Object.assign(angular.copy(areaStackSradientSetting), {
                legend: { data: ['成功', '失败'], left: 88, top: 25 },
                color: ['#2697FF', '#FF6473']
            });
            $scope.dailyBuildSetting.series.push(angular.copy(areaStackSradientSerie));
            $scope.dailyBuildSetting.series.push(angular.copy(areaStackSradientSerie));
            $scope.dailyBuildSetting.series[0].name = '成功';
            $scope.dailyBuildSetting.series[1].name = '失败';
            $scope.dailyBuildSetting.series[1].areaStyle = angular.copy(errorAreaStyle);


            $scope.setdailyBuildData = function () {
                // console.log('setdailyBuildData ++++');
                const allSucData = angular.copy($scope.build.dailyBuildSucData).sort(function (a, b) { return b[0] - a[0] });
                const allErrData = angular.copy($scope.build.dailyBuildErrData).sort(function (a, b) { return b[0] - a[0] });
                const filterLength = $scope.build.selectdailyBuildRange === 'lastweek' ? 7 :
                    $scope.build.selectdailyBuildRange === 'last2week' ? 14 : -1;
                $scope.dailyBuildSetting.series[0].data = filterLength > 0 ? allSucData.slice(0, filterLength) : allSucData;
                $scope.dailyBuildSetting.series[1].data = filterLength > 0 ? allErrData.slice(0, filterLength) : allErrData;
                $scope.dailyBuildSetting.execute && $scope.dailyBuildSetting.execute();
            }
            $scope.getDailyBuildCnt = function (range) {
                range || (range = 1);
                const allSucData = angular.copy($scope.build.dailyBuildSucData).sort(function (a, b) { return b[0] - a[0] });
                const allErrData = angular.copy($scope.build.dailyBuildErrData).sort(function (a, b) { return b[0] - a[0] });
                let result = 0;
                if (allSucData.length < 1) return result;
                for (let i = 0; i < range; i++) {
                    result += allSucData[i][1] + allErrData[i][1];
                }
                return result;
            }
            $scope.setdailyBuildData();

            // 平均构建时长表
            $scope.getBuildTime = function () {
                HttpUtils.get($scope.testIp + 'measure/app/' + $scope.currentAppId + '/build/' + encodeURIComponent($scope.build.selectid) + '/dailyBuildTime/30', function (res) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const days = Object.keys(res.data);
                            const cnts = Object.values(res.data);
                            // 转换为m
                            $scope.build.buildTimeData = days.map((item, index) => { return [item - 0, ((cnts[index] - 0) / 60000).toFixed(1)]; });
                            $scope.setbuildTimeData();
                        }
                    );
                }, function (err) {
                    $scope.$evalAsync(
                        function ($scope) {
                            $scope.build.buildTimeData = $scope.defaultChartData(30, 0).map(item => {
                                item[1] = ((item[1] - 0) / 60000).toFixed(0);
                                return item;
                            });;
                            $scope.setbuildTimeData();
                        }
                    );
                });
            }
            // 通用barserie配置
            const barDataSerie = {
                type: 'bar',
                name: '',
                data: [],
                itemStyle: {
                    barBorderRadius: [100, 100, 0, 0]
                }
            };
            // 通用柱状图配置
            const barDataSetting = {
                grid: {
                    left: '0',
                    right: '5%',
                    bottom: '0',
                    containLabel: true
                },
                xAxis: {
                    type: 'time',
                    splitLine: false,
                    axisLabel: {
                        formatter: function (value, index) {
                            // 格式化成月/日，只在第一个刻度显示年份
                            var date = new Date(value);
                            var texts = [(date.getMonth() + 1), date.getDate()];
                            if (index === 0) {
                                texts.unshift(date.getFullYear());
                            }
                            return texts.join('-');
                        }
                    }
                },
                yAxis: {
                    type: 'value',
                    splitLine: false
                },
                series: []
            };
            $scope.buildTimeSetting = Object.assign(angular.copy(barDataSetting), {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    },
                    formatter: function (params) {
                        var tar = params[0];
                        return $filter('date')(new Date(tar.value[0]), 'yyyyMMdd') + '<br/>' + tar.seriesName + ' : ' + tar.value[1] + ' 分钟';
                    }
                },
            });
            $scope.buildTimeSetting.series.push(Object.assign(angular.copy(barDataSerie), { name: '平均时长' }));
            $scope.setbuildTimeData = function () {
                const allData = angular.copy($scope.build.buildTimeData).sort(function (a, b) { return b[0] - a[0] });
                const filterLength = $scope.build.selectbuildTimeRange === 'lastweek' ? 7 :
                    $scope.build.selectbuildTimeRange === 'last2week' ? 14 : -1;
                $scope.buildTimeSetting.series[0].data = filterLength > 0 ? allData.slice(0, filterLength) : allData;
                $scope.buildTimeSetting.execute && $scope.buildTimeSetting.execute();
            }
            $scope.setbuildTimeData();

            /**************************************** 质量 ****************************************/
            const defaultQuality = {
                score: 0, // float, 应用评分,3.5,
                jobCount: 0,// int, 任务数量，每个任务存在一个质量配置结果，也可能没有
                selectid: 'all', // 当前选中的质量任务id, 'all'表示全部
                selectMetrics: {}, // 当前选中的quality
                metrics: [
                    { id: 'all', name: '全部' }
                ]
            };
            const allMetric = {
                id: 'all', // string job id
                name: '全部', // string, 构建任务名称
                debt: 0, // long, 技术债分钟
                debtFormat: '', // string, 技术债分钟 前端转换后
                duplicatedRate: 0, // int ,重复率, 0-100
                issue: 0, // long, 问题总数
                openIssue: 0, // long, 打开问题总数
                confiredIssue: 0, // long, 确认问题总数
                falsePositionIssue: 0, // long, 误判问题总数
                vulnerabilities: 0, // long, 漏洞数
                uniTestSuccessRate: 0, // long, 单测成功率
                uniTestFailRate: 0, // long, 单测失败率
                uniTestFailCount: 0, // long, 单测失败数量
                uniTestCaseCount: 0, // long, 单测用例数量
                uniTestCoverage: 0, // long, 单测覆盖率
                coverageCodeLine: 0, // long, 单测覆盖代码行
                newUniTestCoverage: 0, // long, 最新代码单测覆盖率
                newCoverageCodeLine: 0, // long, 最新代码单测可覆盖代码行
                newCodeLines: 0, // long, 最新代码行数
            }
            $scope.quality = angular.copy(defaultQuality);
            $scope.selectMetrics = {};
            $scope.getQuality = function () {
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/quality/1/500', function (response) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const data = Object.assign({}, $scope.quality, response.data);
                            const metric = angular.copy(allMetric);
                            $scope.null20(data, ['score', 'jobCount']);
                            data.metrics.forEach(element => {
                                $scope.null20(element, ['bugs', 'debt', 'duplicatedRate', 'issue', 'openIssue',
                                    'confiredIssue', 'falsePositionIssue', 'vulnerabilities', 'uniTestSuccessRate', 'uniTestFailRate',
                                    'uniTestFailCount', 'uniTestCaseCount', 'uniTestCoverage', 'coverageCodeLine', 'newUniTestCoverage', 'newCoverageCodeLine']);
                                // console.log('null20 metrics', element);
                                element.id === null && (element.id = element.name);
                                element.debtFormat = $scope.formatDate((element.debt - 0) * 60000);
                                for (const key in metric) {
                                    if (Object.hasOwnProperty.call(metric, key)) {
                                        const value = metric[key];
                                        // console.log('metrics ++++', key, value);
                                        isNaN(value - 0) || (metric[key] = value + (!element[key] ? 0 : element[key]))
                                    }
                                }
                                return element;
                            });
                            metric.duplicatedRate = data.metrics.length > 0 ? Math.floor(metric.duplicatedRate / data.metrics.length) : 0;
                            metric.uniTestSuccessRate = data.metrics.length > 0 ? Math.floor(metric.uniTestSuccessRate / data.metrics.length) : 0;
                            metric.uniTestFailRate = data.metrics.length > 0 ? Math.floor(metric.uniTestFailRate / data.metrics.length) : 0;
                            metric.newUniTestCoverage = data.metrics.length > 0 ? Math.floor(metric.newUniTestCoverage / data.metrics.length) : 0;
                            metric.uniTestCoverage = data.metrics.length > 0 ? Math.floor(metric.uniTestCoverage / data.metrics.length) : 0;
                            metric.debtFormat = $scope.formatDate(metric.debt ? (metric.debt - 0) * 60000 : 0);
                            data.metrics.unshift(metric);
                            data.selectid = 'all';
                            $scope.quality = data;
                            $scope.selectMetrics = metric;
                            $scope.qualityChange();
                            $scope.setScoreImages($scope.quality.score, 'quality');
                            $scope.initRequestCnt++;
                            $scope.initRequestCnt === $scope.initTotalRequest && $scope.setOverallScore();
                        }
                    );
                }, function (err) {
                    $scope.$evalAsync(
                        function ($scope) {
                            $scope.quality = Object.assign($scope.quality, { metrics: [angular.copy(allMetric)] });
                            $scope.selectMetrics = $scope.quality.metrics[0];
                            $scope.initRequestCnt++;
                            $scope.initRequestCnt === $scope.initTotalRequest && $scope.setOverallScore();
                        }
                    )
                });
            }
            $scope.qualityChange = function () {
                const matchItem = $scope.quality.metrics.find(item => item.id === $scope.quality.selectid);
                if (!matchItem) return;
                $scope.$evalAsync(
                    function ($scope) {
                        $scope.selectMetrics = matchItem;
                        $scope.uniTestSuccessRateSetting.series[0].data[0].value = matchItem.uniTestSuccessRate;
                        // $scope.uniTestFailRateSetting.series[0].data[0].value = matchItem.uniTestFailRate;
                        $scope.uniTestCoverageSetting.series[0].data[0].value = matchItem.uniTestCoverage;
                        $scope.newUniTestCoverageSetting.series[0].data[0].value = matchItem.newUniTestCoverage;
                        $scope.uniTestSuccessRateSetting.execute && $scope.uniTestSuccessRateSetting.execute();
                        // $scope.uniTestFailRateSetting.execute && $scope.uniTestFailRateSetting.execute();
                        $scope.newUniTestCoverageSetting.execute && $scope.newUniTestCoverageSetting.execute();
                        $scope.uniTestCoverageSetting.execute && $scope.uniTestCoverageSetting.execute();
                    }
                );
            }
            // 质量评分
            $scope.qualityScoreImages = [];
            $scope.setScoreImages($scope.quality.score, 'quality');

            // gauge-ring data通用配置
            const gaugeRingData = {
                value: 60,
                name: 'Commonly',
                title: {
                    offsetCenter: ['0%', '30%'],
                    ontSize: 14,
                },
                detail: {
                    valueAnimation: true,
                    offsetCenter: ['0%', '0%']
                }
            }
                ;
            // gauge-ring series通用配置
            const gaugeRingSerie = {
                type: 'gauge',
                radius: '100%',
                startAngle: 210,
                endAngle: -30,
                pointer: {
                    show: false
                },
                progress: {
                    show: true,
                    overlap: false,
                    roundCap: true,
                    clip: false
                },
                axisLine: {
                    lineStyle: {
                        width: 12
                    }
                },
                splitLine: {
                    show: false,
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    show: false,
                    distance: 50
                },
                data: [],
                title: {
                    fontSize: 14
                },
                itemStyle: {
                    color: '#2697FF' // #2697FF 蓝色 #5FD157 绿色 #FF6473 红色
                },
                detail: {
                    width: 40,
                    height: 20,
                    fontSize: 18,
                    fontWeight: 700,
                    color: '#333',
                    formatter: '{value}%'
                }
            };

            // gauge-ring图通用配置
            const gaugeRingSetting = {
                series: []
            };

            $scope.uniTestSuccessRateSetting = Object.assign(angular.copy(gaugeRingSetting), {
                series: [Object.assign(angular.copy(gaugeRingSerie), {
                    data: [Object.assign(angular.copy(gaugeRingData), { value: 0, name: '成功率' })]
                    , itemStyle: { color: '#5FD157' }
                })]
            });
            // console.log('uniTestSuccessRateSetting ++++', JSON.stringify($scope.uniTestSuccessRateSetting));
            // $scope.uniTestFailRateSetting = Object.assign(angular.copy(gaugeRingSetting), {
            //     series: [Object.assign(angular.copy(gaugeRingSerie), {
            //         data: [Object.assign(angular.copy(gaugeRingData), { value: 0, name: '失败率' })]
            //         , itemStyle: { color: '#FF6473' }
            //     })]
            // });

            $scope.newUniTestCoverageSetting = Object.assign(angular.copy(gaugeRingSetting), {
                series: [Object.assign(angular.copy(gaugeRingSerie), {
                    data: [Object.assign(angular.copy(gaugeRingData), { value: 0, name: '最新覆盖率' })]
                    , itemStyle: { color: '#5FD157' }
                })]
            });

            $scope.uniTestCoverageSetting = Object.assign(angular.copy(gaugeRingSetting), {
                series: [Object.assign(angular.copy(gaugeRingSerie), {
                    data: [Object.assign(angular.copy(gaugeRingData), { value: 0, name: '总覆盖率' })]
                })]
            });


            /**************************************** 部署 ****************************************/
            Object.assign($scope.columnsWarnValue, {
                deploy_deploySuccessRate: 60
            });
            const defaultDeploy = {
                score: 0, // float, 应用评分,3.5,
                curDeployVersion: '',// string, 当前部署的版本,
                deployCount: '0', // string, 部署总次数
                deploySuccessRate: 0, // int, 成功率
                version: [
                    // {
                    //     id: 'fff', // string version id,
                    //     name: 'ddd', // string, 版本名称
                    //     deployCount: 12,// long, 该版本部署次数,
                    //     deploySuccessCount: 10,//long, 该版本部署成功次数,
                    //     deployRollbackCount: 2,//long, 该版本部署回滚次数,
                    //     deployAvgTime: 50,//long, 部署平均时长,
                    // }
                ]
            };
            $scope.jumpToVersion = function (versionName) {
                const url = '/devops/?banner=false#!/application/version';
                sessionStorage.setItem('module', url.split("#!")[0]);
                sessionStorage.setItem('url', url.split("#!")[1]);
                sessionStorage.setItem('versionName', versionName);
                window.open(window.parent.location.href);
            }
            $scope.deploy = angular.copy(defaultDeploy);
            $scope.getDeploy = function () {
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/deploy/1/500', function (response) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const data = Object.assign({}, $scope.deploy, response.data);
                            $scope.null20(data, ['score', 'deploySuccessRate']);
                            $scope.deploy = data;
                            $scope.setScoreImages($scope.deploy.score, 'deploy');
                            $scope.initRequestCnt++;
                            $scope.initRequestCnt === $scope.initTotalRequest && $scope.setOverallScore();
                        }
                    );
                });
            }
            // 部署评分
            $scope.deployScoreImages = [];
            $scope.setScoreImages($scope.deploy.score, 'deploy');

            /**************************************** 测试 ****************************************/
            Object.assign($scope.columnsWarnValue, {
                apiTest_deployAvgSuccessRate: 60
            });
            const defaultApiTest = {
                score: 0, // float, 应用评分,3.5,
                testCount: '',// string, 测试总次数
                deployAvgSuccessRate: null, // int, 平均成功率
                version: [
                    // {
                    //     id: 'fff', // string version id
                    //     name: 'ddd', // string, 版本名称
                    //     testCount: 62,//long, 该版本测试次数
                    //     testSuccessRate: 50,//long, 该版本测试成功成功率"
                    //     testAvgTime: 7,//long, 该版本测试平均时长,
                    // }
                ]
            };
            $scope.apiTest = angular.copy(defaultApiTest);
            $scope.getApiTest = function () {
                HttpUtils.get($scope.testIp + 'measure/app/' + encodeURIComponent($scope.currentAppId) + '/apiTest/1/500', function (response) {
                    $scope.$evalAsync(
                        function ($scope) {
                            const data = Object.assign({}, $scope.apiTest, response.data);
                            $scope.null20(data, ['score', 'deployAvgSuccessRate']);
                            data.version || (data.version = []);
                            data.version.map((item) => {
                                $scope.null20(item, ['testCount', 'testSuccessRate', 'testAvgTime']);
                                return item;
                            });
                            // console.log('getApiTest +++', data);
                            $scope.apiTest = data;
                            $scope.setScoreImages($scope.apiTest.score, 'apiTest');
                            $scope.apiTestSetting.series[0].data[0].value = data.deployAvgSuccessRate;
                            $scope.apiTestSetting.execute && $scope.apiTestSetting.execute();
                            $scope.initRequestCnt++;
                            $scope.initRequestCnt === $scope.initTotalRequest && $scope.setOverallScore();
                        }
                    );
                });
            }
            $scope.apiTestSetting = Object.assign(angular.copy(gaugeRingSetting), {
                series: [Object.assign(angular.copy(gaugeRingSerie), {
                    data: [Object.assign(angular.copy(gaugeRingData), { value: 0, name: '最新成功率' })]
                    , itemStyle: { color: '#5FD157' }
                })]
            });
            // 测试评分
            $scope.apiTestScoreImages = [];
            $scope.setScoreImages($scope.apiTest.score, 'apiTest');

            $scope.detailInit = function () {
                $scope.$evalAsync(
                    function ($scope) {
                        $scope.initRequestCnt = 0;
                        $scope.overall = angular.copy(defaultOverall);
                        $scope.code = angular.copy(defaultCode);
                        $scope.build = angular.copy(defaultBuild);
                        $scope.quality = angular.copy(defaultQuality);
                        $scope.deploy = angular.copy(defaultDeploy);
                        $scope.apiTest = angular.copy(defaultApiTest);
                    }
                );
                $scope.getOvaerAll();
                $scope.getCode();
                $scope.getBuild();
                $scope.getQuality();
                $scope.getDeploy();
                $scope.getApiTest();
            }
            $scope.$watch('showDetail', function (n, o) {
                if (n && $scope.currentAppId) {
                    $scope.detailInit()
                    $scope.dailyCommitSetting.execute && $scope.dailyCommitSetting.execute();
                    $scope.dailyBuildSetting.execute && $scope.dailyBuildSetting.execute();
                    $scope.buildTimeSetting.execute && $scope.buildTimeSetting.execute();
                    $scope.uniTestSuccessRateSetting.execute && $scope.uniTestSuccessRateSetting.execute();
                    // $scope.uniTestFailRateSetting.execute && $scope.uniTestFailRateSetting.execute();
                    $scope.uniTestCoverageSetting.execute && $scope.uniTestCoverageSetting.execute();
                    $scope.newUniTestCoverageSetting.execute && $scope.newUniTestCoverageSetting.execute();
                    $scope.apiTestSetting.execute && $scope.apiTestSetting.execute();
                }
            });
        }
    }
});
