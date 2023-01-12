ProjectApp.service('CommonService', function ($http) {
    /**
     * 获取集群列表
     */
    this.getClusters = function () {
        return $http.get("cluster/list");
    };

    /**
     * 获取主机组列表
     */
    this.getClusterRoles = function (clusterId) {
        return $http.get("clusterRole/list/" + clusterId);
    };

    /**
     * 获取标签列表
     */
    this.getTags = function () {
        return $http.get("tag/listAll");
    };
    /**
     * 获取资源标签
     */
    this.getResourceTagMappings = function (resourceId) {
        return $http.post("tag/mapping/list", {resourceId: resourceId});
    };
    /**
     * 保存标签
     */
    this.saveTagMappings = function (tagMappings) {
        return $http.post("mapping/save", tagMappings);
    };

    this.searchTagValue = function (tags, tagKey) {
        var result = null;
        tags.forEach(function (tag) {
            if (tag.tagKey === tagKey) {

                result = tag.tagValues;
            }
        });
        return result;
    }

}).service('DeleteService', function () {
    this.deleteItem = function (items, item, key) {
        for (let i = 0; i < items.length; i++) {
            if (key) {
                if (items[i][key] === item[key]) {
                    items.splice(i, 1);
                }
            }else {
                if (items[i].$$hashKey === item.$$hashKey) {
                    items.splice(i, 1);
                }
            }
        }
    };
});
