package com.fit2cloud.devops.controller;


import com.fit2cloud.commons.utils.ResultHolder;
import com.fit2cloud.devops.service.DevopsMeasureAppService;
import com.fit2cloud.devops.dto.request.ApplicationRequest;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.fit2cloud.devops.vo.AppMetricsVO;
import com.fit2cloud.devops.common.util.RetUtils;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(tags = "度量-应用列表")
@RequestMapping("measure/app")
public class DevopsMeasureAppController {

    @Resource
    private DevopsMeasureAppService measureAppService;

    private static final Integer MAX_PAGE_SIZE = 30; //限制破坏式查询

    @ApiOperation("获取全部列表")
    @GetMapping("list/{goPages}/{pageSize}")
    public ResultHolder getAllAppList(@PathVariable Integer goPages, @PathVariable Integer pageSize, @RequestParam(required=false) ApplicationRequest req) {
        String msg = ParamCheck(goPages, pageSize, req);
        if (!msg.equals("success")) {
            return ResultHolder.error(msg);
        }
        return RetUtils.convert(measureAppService.getList(goPages, pageSize, req));
    }

    @GetMapping("list")
    public ResultHolder getAllAppList() {
        return RetUtils.convert(measureAppService.getList(1, 1, null));
    }

    @ApiOperation("获取自选列表")
    @GetMapping("customize/list/{goPages}/{pageSize}")
    public ResultHolder getAllCustomize(@PathVariable Integer goPages, @PathVariable Integer pageSize, @RequestParam(required=false) ApplicationRequest req) {
        String msg = ParamCheck(goPages, pageSize, req);
        if (!msg.equals("success")) {
            return ResultHolder.error(msg);
        }
        return RetUtils.convert(measureAppService.getCustomizeList(goPages, pageSize, req));
    }
    
    @GetMapping("customize/list")
    public ResultHolder getAllCustomize() {
        return RetUtils.convert(measureAppService.getCustomizeList(1, 1, null));
    }
    
    @ApiOperation("添加自选")
    @PostMapping("customize/add/{id}")
    public ResultHolder addCustomize(@PathVariable String id) {
        return RetUtils.convert(measureAppService.AddCustomize(id));
    }

    @ApiOperation("移除自选")
    @PostMapping("customize/remove/{id}")
    public ResultHolder RemoveCustomize(@PathVariable String id) {
        return RetUtils.convert(measureAppService.RemoveCustomize(id));
    }

    @ApiOperation("应用总数")
    @GetMapping("list/total")
    public ResultHolder getTotal(@RequestParam(required=false) ApplicationRequest req) {
        return RetUtils.convert(measureAppService.getTotal());
    }

    @ApiOperation("自选应用总数")
    @GetMapping("customize/list/total")
    public ResultHolder getCustomizeTotal(@RequestParam(required=false) ApplicationRequest req) {
        return RetUtils.convert(measureAppService.getCustomizeTotal());
    }

    private String ParamCheck(Integer goPages, Integer pageSize, ApplicationRequest req) {
        if (goPages <= 0) {
            return "goPages invalid: " + goPages + ", must greater than 0.";
        }

        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return "pageSize invalid: " + goPages + ", pageSize over 0 and low with "+ MAX_PAGE_SIZE;
        }
        return "success";
    }

}
