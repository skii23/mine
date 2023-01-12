package com.fit2cloud.devops.controller;


import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.service.DevopsMeasureAppQualityService;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.fit2cloud.devops.common.util.RetUtils;
import javax.annotation.Resource;

@RestController
@Api(tags = "度量-应用质量指标")
@RequestMapping("measure/app/{id}/")
public class DevopsMeasureAppQualityController {

    @Resource
    private DevopsMeasureAppQualityService qualityService;

    private static final Integer MAX_PAGE_SIZE = 500; //限制破坏式查询

    @ApiOperation("获取全部任务质量指标")
    @GetMapping("quality/{goPages}/{pageSize}")
    public ResultHolder getAllAppList(@PathVariable String id, @PathVariable Integer goPages, @PathVariable Integer pageSize) {
        String msg = ParamCheck(goPages, pageSize);
        if (!msg.equals("success")) {
            return ResultHolder.error(msg);
        }
        return RetUtils.convert(qualityService.getJobsQualityMetrics(id, goPages, pageSize));
    }

    private String ParamCheck(Integer goPages, Integer pageSize) {
        if (goPages <= 0) {
            return "goPages invalid: " + goPages + ", must greater than 0.";
        }

        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return "pageSize invalid: " + goPages + ", pageSize over 0 and low with "+ MAX_PAGE_SIZE;
        }
        return "success";
    }

}
