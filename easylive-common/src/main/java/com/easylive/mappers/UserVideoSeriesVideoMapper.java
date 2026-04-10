package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @description: Mapper
 * @date: 2025-03-08
 */
public interface UserVideoSeriesVideoMapper<T, P> extends BaseMapper<T, P> {
    /**
     * 根据SeriesIdAndVideoId查询
     */
    T selectBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

    /**
     * 根据SeriesIdAndVideoId更新
     */
    Integer updateBySeriesIdAndVideoId(@Param("bean") T t, @Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

    /**
     * 根据SeriesIdAndVideoId删除
     */
    Integer deleteBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

    Integer selectMaxSort(@Param("seriesId") Integer seriesId);
}