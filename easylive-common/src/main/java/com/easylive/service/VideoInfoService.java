package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: video_info Service
 * @date: 2025-03-06
 */
public interface VideoInfoService {
	/**
	 * 根据条件查询列表
	 */
	List<VideoInfo> findListByParam(VideoInfoQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(VideoInfoQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<VideoInfo> findListByPage(VideoInfoQuery query);

	/**
	 * 新增
	 */
	Integer add(VideoInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoInfo> listBean);

	/**
	 * 批量新增/更新
	 */
	Integer addOrUpdateBatch(List<VideoInfo> listBean);

	/**
	 * 根据VideoId查询
	 */
	VideoInfo getVideoInfoByVideoId(String videoId); 

	/**
	 * 根据VideoId更新
	 */
	Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId); 

	/**
	 * 根据VideoId删除
	 */
	Integer deleteVideoInfoByVideoId(String videoId);

    void updateVideoInteraction(String videoId, String interaction, String userId);

	void delVideo(String videoId, String userId);

	void addReadCount(String videoId);

    void recommendVideo(String videoId);
}