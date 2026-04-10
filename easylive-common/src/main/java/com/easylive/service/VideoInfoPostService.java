package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: video_info_post Service
 * @date: 2025-03-06
 */
public interface VideoInfoPostService {
    /**
     * 根据条件查询列表
     */
    List<VideoInfoPost> findListByParam(VideoInfoPostQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(VideoInfoPostQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<VideoInfoPost> findListByPage(VideoInfoPostQuery query);

    /**
     * 新增
     */
    Integer add(VideoInfoPost bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<VideoInfoPost> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<VideoInfoPost> listBean);

    /**
     * 根据VideoId查询
     */
    VideoInfoPost getVideoInfoPostByVideoId(String videoId);

    /**
     * 根据VideoId更新
     */
    Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId);

    /**
     * 根据VideoId删除
     */
    Integer deleteVideoInfoPostByVideoId(String videoId);

    void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> videoInfoFilePostList);

    void transferVideoFile(VideoInfoFilePost videoInfoFilePost);

    void auditVideo(String videoId, Integer status, String reason);
}