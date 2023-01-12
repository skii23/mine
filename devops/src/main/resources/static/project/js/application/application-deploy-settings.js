ProjectApp.controller('ApplicationDeploySettings', function ($scope, HttpUtils, Loading, $filter,Notification) {

    $scope.weekdayDict = {
        'weekday.MONDAY': '星期一',
        'weekday.TUESDAY': '星期二',
        'weekday.WEDNESDAY': '星期三',
        'weekday.THURSDAY': '星期四',
        'weekday.FRIDAY': '星期五',
        'weekday.SATURDAY': '星期六',
        'weekday.SUNDAY': '星期日',
    };

    $scope.hours = [
        '00', '01', '02', '03', '04', '05', '06', '07',
        '08', '09', '10', '11', '12', '13', '14', '15',
        '16', '17', '18', '19', '20', '21', '22', '23'
    ];

    $scope.minAndSecArr = [
        '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
        '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
        '10', '21', '22', '23', '24', '25', '26', '27', '28', '29',
        '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
        '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
        '50', '51', '52', '53', '54', '55', '56', '57', '58', '59',
    ];

    $scope.init = function () {
        $scope.loadingLayer = HttpUtils.get('deploy/settings/weekday', function (data) {
            $scope.weekdays = data.data;
            $scope.weekdays.forEach(function (item) {
                item.startHour = item.start.split(':')[0];
                item.startMinute = item.start.split(':')[1];
                item.startSecond = item.start.split(':')[2];
                item.endHour = item.end.split(':')[0];
                item.endMinute = item.end.split(':')[1];
                item.endSecond = item.end.split(':')[2];
            });
        });
        $scope.expectedDates = [];
        $scope.loadingLayer = HttpUtils.get('deploy/settings/expectedDays', function (data) {
            $scope.expectedDates = $scope.expectedDates.concat(data.data);
        });
    };

    $scope.addExpectedDate = function () {
        if ($scope.selectedDate) {
            let flag = true;
            $scope.expectedDates.forEach(function (item) {
                if (item.date === $scope.selectedDate.getTime()) {
                    flag = false;
                }
            });
            if (flag) {
                $scope.expectedDates.push({name: 'expected_days',date: $scope.selectedDate.getTime()});
            }
        }
    };

    $scope.saveStrategy = function () {
        $scope.weekdays.forEach(function (item) {
            item.start = [item.startHour, item.startMinute, item.startSecond].join(':');
            item.end = [item.endHour, item.endMinute, item.endSecond].join(':');
        });
        $scope.loadingLayer = HttpUtils.post('deploy/settings/weekday/save', $scope.weekdays,function (data) {});
        $scope.loadingLayer = HttpUtils.post('deploy/settings/expectedDays/save', $scope.expectedDates, function (data) {
            Notification.success("保存成功！");
        });
    };

    $scope.init();
});