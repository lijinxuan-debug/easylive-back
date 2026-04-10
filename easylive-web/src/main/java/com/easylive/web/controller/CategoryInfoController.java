package com.easylive.web.controller;

import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.query.CategoryInfoQuery;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.service.CategoryInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description: category_info Controller
 * @date: 2025-03-03
 */
@RestController
@RequestMapping("/categoryInfo")
@Validated
public class CategoryInfoController extends ABaseController {

    @Resource
    private CategoryInfoService categoryInfoService;

    @RequestMapping("/loadAllCategoryInfo")
    public ResponseVo loadAllCategoryInfo() {
        return getSuccessResponseVo(categoryInfoService.getAllCategoryInfo());
    }
}