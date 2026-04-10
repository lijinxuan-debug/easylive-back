package com.easylive.service.impl;

import java.util.List;

import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.mappers.VideoInfoFilePostMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: video_info_file_post ServiceImpl
 * @date: 2025-03-06
 */
@Service("videoInfoFilePostService")
public class VideoInfoFilePostServiceImpl implements VideoInfoFilePostService {

	@Resource
	private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

	/**
	 * 根据条件查询列表
	 */
	public List<VideoInfoFilePost> findListByParam(VideoInfoFilePostQuery query) {
		return videoInfoFilePostMapper.selectList(query);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(VideoInfoFilePostQuery query) {
		return videoInfoFilePostMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<VideoInfoFilePost> findListByPage(VideoInfoFilePostQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<VideoInfoFilePost> list = this.findListByParam(query);
		PaginationResultVo<VideoInfoFilePost> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(VideoInfoFilePost bean) {
		return videoInfoFilePostMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<VideoInfoFilePost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return videoInfoFilePostMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/更新
	 */
	public Integer addOrUpdateBatch(List<VideoInfoFilePost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return videoInfoFilePostMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据FileId查询
	 */
	public VideoInfoFilePost getVideoInfoFilePostByFileId(String fileId) {
		return videoInfoFilePostMapper.selectByFileId(fileId);
	}

	/**
	 * 根据FileId更新
	 */
	public Integer updateVideoInfoFilePostByFileId(VideoInfoFilePost bean, String fileId) {
		return videoInfoFilePostMapper.updateByFileId(bean, fileId);
	}

	/**
	 * 根据FileId删除
	 */
	public Integer deleteVideoInfoFilePostByFileId(String fileId) {
		return videoInfoFilePostMapper.deleteByFileId(fileId);
	}

	/**
	 * 根据UploadIdAndUserId查询
	 */
	public VideoInfoFilePost getVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId) {
		return videoInfoFilePostMapper.selectByUploadIdAndUserId(uploadId, userId);
	}

	/**
	 * 根据UploadIdAndUserId更新
	 */
	public Integer updateVideoInfoFilePostByUploadIdAndUserId(VideoInfoFilePost bean, String uploadId, String userId) {
		return videoInfoFilePostMapper.updateByUploadIdAndUserId(bean, uploadId, userId);
	}

	/**
	 * 根据UploadIdAndUserId删除
	 */
	public Integer deleteVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId) {
		return videoInfoFilePostMapper.deleteByUploadIdAndUserId(uploadId, userId);
	}
}