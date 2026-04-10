package com.easylive.admin.controller;

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

    @RequestMapping("/loadCategoryInfo")
    public ResponseVo loadCategoryInfo(CategoryInfoQuery query) {
        query.setOrderBy("sort asc");
        query.setConvert2Tree(true);
        List<CategoryInfo> categoryInfoList = categoryInfoService.findListByParam(query);
        return getSuccessResponseVo(categoryInfoList);
    }

    @RequestMapping("/saveCategoryInfo")
    public ResponseVo saveCategoryInfo(@NotNull Integer pCategoryId,
                                       Integer categoryId,
                                       @NotEmpty String categoryCode,
                                       @NotEmpty String categoryName,
                                       String icon,
                                       String background) {
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setCategoryId(categoryId);
        categoryInfo.setpCategoryId(pCategoryId);
        categoryInfo.setCategoryCode(categoryCode);
        categoryInfo.setCategoryName(categoryName);
        categoryInfo.setIcon(icon);
        categoryInfo.setBackground(background);

        categoryInfoService.saveCategoryInfo(categoryInfo);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/delCategoryInfo")
    public ResponseVo delCategoryInfo(@NotNull Integer categoryId) {
        categoryInfoService.delCategoryInfo(categoryId);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/changeSort")
    public ResponseVo changeSort(@NotNull Integer pCategoryId, @NotEmpty String categoryIds) {
        categoryInfoService.changeSort(pCategoryId, categoryIds);
        return getSuccessResponseVo(null);
    }
}