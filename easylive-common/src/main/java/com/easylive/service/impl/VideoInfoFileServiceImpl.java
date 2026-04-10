package com.easylive.service.impl;

import java.util.List;

import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.query.VideoInfoFileQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.service.VideoInfoFileService;
import com.easylive.mappers.VideoInfoFileMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: video_info_file ServiceImpl
 * @date: 2025-03-06
 */
@Service("videoInfoFileService")
public class VideoInfoFileServiceImpl implements VideoInfoFileService {

	@Resource
	private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;

	/**
	 * 根据条件查询列表
	 */
	public List<VideoInfoFile> findListByParam(VideoInfoFileQuery query) {
		return videoInfoFileMapper.selectList(query);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(VideoInfoFileQuery query) {
		return videoInfoFileMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<VideoInfoFile> findListByPage(VideoInfoFileQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<VideoInfoFile> list = this.findListByParam(query);
		PaginationResultVo<VideoInfoFile> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(VideoInfoFile bean) {
		return videoInfoFileMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<VideoInfoFile> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return videoInfoFileMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/更新
	 */
	public Integer addOrUpdateBatch(List<VideoInfoFile> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return videoInfoFileMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据FileId查询
	 */
	public VideoInfoFile getVideoInfoFileByFileId(String fileId) {
		return videoInfoFileMapper.selectByFileId(fileId);
	}

	/**
	 * 根据FileId更新
	 */
	public Integer updateVideoInfoFileByFileId(VideoInfoFile bean, String fileId) {
		return videoInfoFileMapper.updateByFileId(bean, fileId);
	}

	/**
	 * 根据FileId删除
	 */
	public Integer deleteVideoInfoFileByFileId(String fileId) {
		return videoInfoFileMapper.deleteByFileId(fileId);
	}
}