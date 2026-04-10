package com.easylive.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.po.UserVideoSeriesVideo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.query.UserVideoSeriesVideoQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserVideoSeriesVideoMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserVideoSeriesService;
import com.easylive.mappers.UserVideoSeriesMapper;
import com.easylive.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: user_video_series ServiceImpl
 * @date: 2025-03-08
 */
@Service("userVideoSeriesService")
public class UserVideoSeriesServiceImpl implements UserVideoSeriesService {

    @Resource
    private UserVideoSeriesMapper<UserVideoSeries, UserVideoSeriesQuery> userVideoSeriesMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserVideoSeriesVideoMapper<UserVideoSeriesVideo, UserVideoSeriesVideoQuery> userVideoSeriesVideoMapper;

    /**
     * 根据条件查询列表
     */
    public List<UserVideoSeries> findListByParam(UserVideoSeriesQuery query) {
        return userVideoSeriesMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(UserVideoSeriesQuery query) {
        return userVideoSeriesMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserVideoSeries> findListByPage(UserVideoSeriesQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserVideoSeries> list = this.findListByParam(query);
        PaginationResultVo<UserVideoSeries> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserVideoSeries bean) {
        return userVideoSeriesMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserVideoSeries> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userVideoSeriesMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<UserVideoSeries> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userVideoSeriesMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据SeriesId查询
     */
    public UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId) {
        return userVideoSeriesMapper.selectBySeriesId(seriesId);
    }

    /**
     * 根据SeriesId更新
     */
    public Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId) {
        return userVideoSeriesMapper.updateBySeriesId(bean, seriesId);
    }

    /**
     * 根据SeriesId删除
     */
    public Integer deleteUserVideoSeriesBySeriesId(Integer seriesId) {
        return userVideoSeriesMapper.deleteBySeriesId(seriesId);
    }

    @Override
    public List<UserVideoSeries> getUserAllVideoSeries(String userId) {
        return userVideoSeriesMapper.selectUserAllVideoSeries(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserVideoSeries(UserVideoSeries userVideoSeries, String videoIds) {
        if (userVideoSeries.getSeriesId() == null && StringTools.isEmpty(videoIds)) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        if (userVideoSeries.getSeriesId() == null) {
            checkVideoIds(userVideoSeries.getUserId(), videoIds);
            userVideoSeries.setUpdateTime(new Date());
            userVideoSeries.setSort(userVideoSeriesMapper.selectMaxSort(userVideoSeries.getUserId()) + 1);
            userVideoSeriesMapper.insert(userVideoSeries);
            saveVideo2UserVideoSeries(userVideoSeries.getUserId(), videoIds, userVideoSeries.getSeriesId());
        } else {
            userVideoSeriesMapper.updateBySeriesId(userVideoSeries, userVideoSeries.getSeriesId());
        }
    }

    private void checkVideoIds(String userId, String videoIds) {
        String[] videoIdArray = videoIds.split(",");
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setVideoIdArray(videoIdArray);
        Integer count = videoInfoMapper.selectCount(videoInfoQuery);
        if (count != videoIdArray.length) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
    }

    @Override
    public void saveVideo2UserVideoSeries(String userId, String videoIds, Integer seriesId) {
        UserVideoSeries userVideoSeries = userVideoSeriesMapper.selectBySeriesId(seriesId);
        if (userVideoSeries == null || !userVideoSeries.getUserId().equals(userId)) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        checkVideoIds(userId, videoIds);
        String[] videoIdArray = videoIds.split(",");
        Integer sort = userVideoSeriesVideoMapper.selectMaxSort(seriesId);

        List<UserVideoSeriesVideo> list = new ArrayList<>();
        for (String videoId : videoIdArray) {
            UserVideoSeriesVideo userVideoSeriesVideo = new UserVideoSeriesVideo();
            userVideoSeriesVideo.setSeriesId(seriesId);
            userVideoSeriesVideo.setVideoId(videoId);
            userVideoSeriesVideo.setUserId(userId);
            userVideoSeriesVideo.setSort(++sort);
            list.add(userVideoSeriesVideo);
        }
        userVideoSeriesVideoMapper.insertOrUpdateBatch(list);
    }

    @Override
    public void delSeriesVideo(String userId, String videoId, Integer seriesId) {
        UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        userVideoSeriesVideoQuery.setUserId(userId);
        userVideoSeriesVideoQuery.setVideoId(videoId);
        userVideoSeriesVideoQuery.setSeriesId(seriesId);
        Integer count = userVideoSeriesVideoMapper.deleteByQuery(userVideoSeriesVideoQuery);
        if (count == 0) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delSeries(String userId, Integer seriesId) {
        UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
        userVideoSeriesQuery.setUserId(userId);
        userVideoSeriesQuery.setSeriesId(seriesId);
        Integer count = userVideoSeriesMapper.deleteByQuery(userVideoSeriesQuery);
        if (count == 0) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        userVideoSeriesVideoQuery.setUserId(userId);
        userVideoSeriesVideoQuery.setSeriesId(seriesId);
        userVideoSeriesVideoMapper.deleteByQuery(userVideoSeriesVideoQuery);
    }

    @Override
    public void changeVideoSeriesSort(String userId, String seriesIds) {
        String[] ids = seriesIds.split(",");
        List<UserVideoSeries> list = new ArrayList<>();
        Integer sort = 0;
        for (String seriesId : ids) {
            UserVideoSeries userVideoSeries = new UserVideoSeries();
            userVideoSeries.setUserId(userId);
            userVideoSeries.setSeriesId(Integer.parseInt(seriesId));
            userVideoSeries.setSort(++sort);
            list.add(userVideoSeries);
        }
        userVideoSeriesMapper.updateSortBatch(list);
    }

    @Override
    public List<UserVideoSeries> findListWithVideo(UserVideoSeriesQuery userVideoSeriesQuery) {
        return userVideoSeriesMapper.selectListWithVideo(userVideoSeriesQuery);
    }
}