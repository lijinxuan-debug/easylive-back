package com.easylive.service.impl;

import java.util.List;

import com.easylive.entity.po.UserVideoSeriesVideo;
import com.easylive.entity.query.UserVideoSeriesVideoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.service.UserVideoSeriesVideoService;
import com.easylive.mappers.UserVideoSeriesVideoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: user_video_series_video ServiceImpl
 * @date: 2025-03-08
 */
@Service("userVideoSeriesVideoService")
public class UserVideoSeriesVideoServiceImpl implements UserVideoSeriesVideoService {

	@Resource
	private UserVideoSeriesVideoMapper<UserVideoSeriesVideo, UserVideoSeriesVideoQuery> userVideoSeriesVideoMapper;

	/**
	 * 根据条件查询列表
	 */
	public List<UserVideoSeriesVideo> findListByParam(UserVideoSeriesVideoQuery query) {
		return userVideoSeriesVideoMapper.selectList(query);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(UserVideoSeriesVideoQuery query) {
		return userVideoSeriesVideoMapper.selectCount(query);
	}

	/**
	 * 分页查询
	 */
	public PaginationResultVo<UserVideoSeriesVideo> findListByPage(UserVideoSeriesVideoQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<UserVideoSeriesVideo> list = this.findListByParam(query);
		PaginationResultVo<UserVideoSeriesVideo> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
		return result;
	}

	/**
	 * 新增
	 */
	public Integer add(UserVideoSeriesVideo bean) {
		return userVideoSeriesVideoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<UserVideoSeriesVideo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return userVideoSeriesVideoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/更新
	 */
	public Integer addOrUpdateBatch(List<UserVideoSeriesVideo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return userVideoSeriesVideoMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据SeriesIdAndVideoId查询
	 */
	public UserVideoSeriesVideo getUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId) {
		return userVideoSeriesVideoMapper.selectBySeriesIdAndVideoId(seriesId, videoId);
	}

	/**
	 * 根据SeriesIdAndVideoId更新
	 */
	public Integer updateUserVideoSeriesVideoBySeriesIdAndVideoId(UserVideoSeriesVideo bean, Integer seriesId, String videoId) {
		return userVideoSeriesVideoMapper.updateBySeriesIdAndVideoId(bean, seriesId, videoId);
	}

	/**
	 * 根据SeriesIdAndVideoId删除
	 */
	public Integer deleteUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId) {
		return userVideoSeriesVideoMapper.deleteBySeriesIdAndVideoId(seriesId, videoId);
	}
}