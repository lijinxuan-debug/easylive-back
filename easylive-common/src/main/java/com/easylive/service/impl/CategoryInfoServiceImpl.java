package com.easylive.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.easylive.component.RedisComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.CategoryInfoQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.CategoryInfoService;
import com.easylive.mappers.CategoryInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: category_info ServiceImpl
 * @date: 2025-03-03
 */
@Service("categoryInfoService")
public class CategoryInfoServiceImpl implements CategoryInfoService {

    @Resource
    private CategoryInfoMapper<CategoryInfo, CategoryInfoQuery> categoryInfoMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    public List<CategoryInfo> findListByParam(CategoryInfoQuery query) {
        List<CategoryInfo> categoryInfoList = categoryInfoMapper.selectList(query);
        if (query.getConvert2Tree() != null && query.getConvert2Tree()) {
            categoryInfoList = convertLine2Tree(categoryInfoList, Constants.ZERO);
        }
        return categoryInfoList;
    }

    private List<CategoryInfo> convertLine2Tree(List<CategoryInfo> dataList, Integer pCategoryId) {
        List<CategoryInfo> children = new ArrayList<>();
        for (CategoryInfo data : dataList) {
            if (data.getCategoryId() != null && data.getpCategoryId() != null && data.getpCategoryId().equals(pCategoryId)) {
                data.setChildren(convertLine2Tree(dataList, data.getCategoryId()));
                children.add(data);
            }
        }
        return children;
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(CategoryInfoQuery query) {
        return categoryInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<CategoryInfo> findListByPage(CategoryInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<CategoryInfo> list = this.findListByParam(query);
        PaginationResultVo<CategoryInfo> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(CategoryInfo bean) {
        return categoryInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<CategoryInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return categoryInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<CategoryInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return categoryInfoMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据CategoryId查询
     */
    public CategoryInfo getCategoryInfoByCategoryId(Integer categoryId) {
        return categoryInfoMapper.selectByCategoryId(categoryId);
    }

    /**
     * 根据CategoryId更新
     */
    public Integer updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId) {
        return categoryInfoMapper.updateByCategoryId(bean, categoryId);
    }

    /**
     * 根据CategoryId删除
     */
    public Integer deleteCategoryInfoByCategoryId(Integer categoryId) {
        return categoryInfoMapper.deleteByCategoryId(categoryId);
    }

    /**
     * 根据CategoryCode查询
     */
    public CategoryInfo getCategoryInfoByCategoryCode(String categoryCode) {
        return categoryInfoMapper.selectByCategoryCode(categoryCode);
    }

    /**
     * 根据CategoryCode更新
     */
    public Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
        return categoryInfoMapper.updateByCategoryCode(bean, categoryCode);
    }

    /**
     * 根据CategoryCode删除
     */
    public Integer deleteCategoryInfoByCategoryCode(String categoryCode) {
        return categoryInfoMapper.deleteByCategoryCode(categoryCode);
    }

    @Override
    public void saveCategoryInfo(CategoryInfo categoryInfo) {
        CategoryInfo dbInfo = categoryInfoMapper.selectByCategoryCode(categoryInfo.getCategoryCode());
        //如果是新增操作，但编码已存在，则抛出异常
        //如果是更新操作，但编码已存在，并且不属于当前记录，则抛出异常
        if (categoryInfo.getCategoryId() == null && dbInfo != null ||
                categoryInfo.getCategoryId() != null && dbInfo != null && !dbInfo.getCategoryId().equals(categoryInfo.getCategoryId())) {
            throw new BusinessException("分类编码已存在");
        }
        if (categoryInfo.getCategoryId() == null) {//新增
            Integer maxSort = categoryInfoMapper.selectMaxSort(categoryInfo.getpCategoryId());
            categoryInfo.setSort(maxSort + 1);
            categoryInfoMapper.insert(categoryInfo);
        } else {//更新
            categoryInfoMapper.updateByCategoryId(categoryInfo, categoryInfo.getCategoryId());
        }
        //刷新缓存
        saveCategoryInfo2Redis();
    }

    @Override
    public void delCategoryInfo(Integer categoryId) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setCategoryIdOrpCategoryId(categoryId);
        Integer count = videoInfoMapper.selectCount(videoInfoQuery);
        if (count > 0) {
            throw new BusinessException("分类下存在视频，不能删除");
        }
        CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
        categoryInfoQuery.setpCategoryIdOrCategoryId(categoryId);
        categoryInfoMapper.deleteByQuery(categoryInfoQuery);

        //刷新缓存
        saveCategoryInfo2Redis();
    }

    @Override
    public void changeSort(Integer pCategoryId, String categoryIds) {
        String[] ids = categoryIds.split(",");
        List<CategoryInfo> categoryInfoList = new ArrayList<>();
        Integer sort = 0;
        for (String categoryId : ids) {
            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.setCategoryId(Integer.parseInt(categoryId));
            categoryInfo.setpCategoryId(pCategoryId);
            categoryInfo.setSort(++sort);
            categoryInfoList.add(categoryInfo);
        }
        categoryInfoMapper.updateSortBatch(categoryInfoList);
        //刷新缓存
        saveCategoryInfo2Redis();
    }

    private void saveCategoryInfo2Redis() {
        CategoryInfoQuery query = new CategoryInfoQuery();
        query.setOrderBy("sort asc");
        query.setConvert2Tree(true);
        List<CategoryInfo> categoryInfoList = findListByParam(query);
        redisComponent.saveCategoryInfo(categoryInfoList);
    }

    @Override
    public List<CategoryInfo> getAllCategoryInfo() {
        List<CategoryInfo> categoryInfoList = redisComponent.getCategoryInfo();
        if (categoryInfoList == null || categoryInfoList.isEmpty()) { //缓存中没有数据，则从数据库中查询
            saveCategoryInfo2Redis();
        }
        return categoryInfoList;
    }
}