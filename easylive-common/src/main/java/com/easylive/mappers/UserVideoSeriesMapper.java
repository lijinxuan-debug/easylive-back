package com.easylive.mappers;

import com.easylive.entity.po.UserVideoSeries;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 用户视频序列归档Mapper
 * @date: 2025-03-08
 */
public interface UserVideoSeriesMapper<T, P> extends BaseMapper<T, P> {
    /**
     * 根据SeriesId查询
     */
    T selectBySeriesId(@Param("seriesId") Integer seriesId);

    /**
     * 根据SeriesId更新
     */
    Integer updateBySeriesId(@Param("bean") T t, @Param("seriesId") Integer seriesId);

    /**
     * 根据SeriesId删除
     */
    Integer deleteBySeriesId(@Param("seriesId") Integer seriesId);

    List<T> selectUserAllVideoSeries(@Param("userId") String userId);

    Integer selectMaxSort(@Param("userId") String userId);

    void updateSortBatch(@Param("useVideoSeriesList") List<UserVideoSeries> useVideoSeriesList);

    List<T> selectListWithVideo(@Param("query") P p);
}