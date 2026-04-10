package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.VideoPlayHistory;
import com.easylive.entity.query.VideoPlayHistoryQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: video_play_history Service
 * @date: 2025-03-15
 */
public interface VideoPlayHistoryService {
    /**
     * 根据条件查询列表
     */
    List<VideoPlayHistory> findListByParam(VideoPlayHistoryQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(VideoPlayHistoryQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery query);

    /**
     * 新增
     */
    Integer add(VideoPlayHistory bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<VideoPlayHistory> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<VideoPlayHistory> listBean);

    /**
     * 根据UserIdAndVideoId查询
     */
    VideoPlayHistory getVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId);

    /**
     * 根据UserIdAndVideoId更新
     */
    Integer updateVideoPlayHistoryByUserIdAndVideoId(VideoPlayHistory bean, String userId, String videoId);

    /**
     * 根据UserIdAndVideoId删除
     */
    Integer deleteVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId);

    void saveVideoPlayHistory(String userId, String videoId, Integer fileIndex);

    void deleteAll(String userId);
}