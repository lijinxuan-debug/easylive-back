package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: video_info_file_post Service
 * @date: 2025-03-06
 */
public interface VideoInfoFilePostService {
	/**
	 * 根据条件查询列表
	 */
	List<VideoInfoFilePost> findListByParam(VideoInfoFilePostQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(VideoInfoFilePostQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<VideoInfoFilePost> findListByPage(VideoInfoFilePostQuery query);

	/**
	 * 新增
	 */
	Integer add(VideoInfoFilePost bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoInfoFilePost> listBean);

	/**
	 * 批量新增/更新
	 */
	Integer addOrUpdateBatch(List<VideoInfoFilePost> listBean);

	/**
	 * 根据FileId查询
	 */
	VideoInfoFilePost getVideoInfoFilePostByFileId(String fileId); 

	/**
	 * 根据FileId更新
	 */
	Integer updateVideoInfoFilePostByFileId(VideoInfoFilePost bean, String fileId); 

	/**
	 * 根据FileId删除
	 */
	Integer deleteVideoInfoFilePostByFileId(String fileId); 

	/**
	 * 根据UploadIdAndUserId查询
	 */
	VideoInfoFilePost getVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId); 

	/**
	 * 根据UploadIdAndUserId更新
	 */
	Integer updateVideoInfoFilePostByUploadIdAndUserId(VideoInfoFilePost bean, String uploadId, String userId); 

	/**
	 * 根据UploadIdAndUserId删除
	 */
	Integer deleteVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId); 

}