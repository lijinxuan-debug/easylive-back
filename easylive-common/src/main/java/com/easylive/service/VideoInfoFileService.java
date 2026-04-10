package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.query.VideoInfoFileQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: video_info_file Service
 * @date: 2025-03-06
 */
public interface VideoInfoFileService {
	/**
	 * 根据条件查询列表
	 */
	List<VideoInfoFile> findListByParam(VideoInfoFileQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(VideoInfoFileQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<VideoInfoFile> findListByPage(VideoInfoFileQuery query);

	/**
	 * 新增
	 */
	Integer add(VideoInfoFile bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoInfoFile> listBean);

	/**
	 * 批量新增/更新
	 */
	Integer addOrUpdateBatch(List<VideoInfoFile> listBean);

	/**
	 * 根据FileId查询
	 */
	VideoInfoFile getVideoInfoFileByFileId(String fileId); 

	/**
	 * 根据FileId更新
	 */
	Integer updateVideoInfoFileByFileId(VideoInfoFile bean, String fileId); 

	/**
	 * 根据FileId删除
	 */
	Integer deleteVideoInfoFileByFileId(String fileId); 

}