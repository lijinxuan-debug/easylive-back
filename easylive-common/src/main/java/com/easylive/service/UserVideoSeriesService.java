package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: user_video_series Service
 * @date: 2025-03-08
 */
public interface UserVideoSeriesService {
    /**
     * 根据条件查询列表
     */
    List<UserVideoSeries> findListByParam(UserVideoSeriesQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserVideoSeriesQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<UserVideoSeries> findListByPage(UserVideoSeriesQuery query);

    /**
     * 新增
     */
    Integer add(UserVideoSeries bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserVideoSeries> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<UserVideoSeries> listBean);

    /**
     * 根据SeriesId查询
     */
    UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId);

    /**
     * 根据SeriesId更新
     */
    Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId);

    /**
     * 根据SeriesId删除
     */
    Integer deleteUserVideoSeriesBySeriesId(Integer seriesId);

    List<UserVideoSeries> getUserAllVideoSeries(String userId);

    void saveUserVideoSeries(UserVideoSeries userVideoSeries, String videoIds);

    void saveVideo2UserVideoSeries(String userId, String videoIds, Integer seriesId);

    void delSeriesVideo(String userId, String videoId, Integer seriesId);

    void delSeries(String userId, Integer seriesId);

    void changeVideoSeriesSort(String userId, String seriesIds);

    List<UserVideoSeries> findListWithVideo(UserVideoSeriesQuery userVideoSeriesQuery);

}