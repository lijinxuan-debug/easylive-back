package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.query.CategoryInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: category_info Service
 * @date: 2025-03-03
 */
public interface CategoryInfoService {
    /**
     * 根据条件查询列表
     */
    List<CategoryInfo> findListByParam(CategoryInfoQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(CategoryInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<CategoryInfo> findListByPage(CategoryInfoQuery query);

    /**
     * 新增
     */
    Integer add(CategoryInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<CategoryInfo> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<CategoryInfo> listBean);

    /**
     * 根据CategoryId查询
     */
    CategoryInfo getCategoryInfoByCategoryId(Integer categoryId);

    /**
     * 根据CategoryId更新
     */
    Integer updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId);

    /**
     * 根据CategoryId删除
     */
    Integer deleteCategoryInfoByCategoryId(Integer categoryId);

    /**
     * 根据CategoryCode查询
     */
    CategoryInfo getCategoryInfoByCategoryCode(String categoryCode);

    /**
     * 根据CategoryCode更新
     */
    Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode);

    /**
     * 根据CategoryCode删除
     */
    Integer deleteCategoryInfoByCategoryCode(String categoryCode);

    void saveCategoryInfo(CategoryInfo categoryInfo);

    void delCategoryInfo(Integer categoryId);

    void changeSort(Integer pCategoryId, String categoryIds);

    List<CategoryInfo> getAllCategoryInfo();
}