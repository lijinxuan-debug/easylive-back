package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @description: 视频播放历史Mapper
 * @date: 2025-03-15
 */
public interface VideoPlayHistoryMapper<T, P> extends BaseMapper<T, P> {
    /**
     * 根据UserIdAndVideoId查询
     */
    T selectByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

    /**
     * 根据UserIdAndVideoId更新
     */
    Integer updateByUserIdAndVideoId(@Param("bean") T t, @Param("userId") String userId, @Param("videoId") String videoId);

    /**
     * 根据UserIdAndVideoId删除
     */
    Integer deleteByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

    void deleteAll(@Param("userId") String userId);
}