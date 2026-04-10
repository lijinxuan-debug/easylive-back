package com.easylive.service.impl;

import java.util.Date;
import java.util.List;

import com.easylive.entity.po.VideoPlayHistory;
import com.easylive.entity.query.VideoPlayHistoryQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.service.VideoPlayHistoryService;
import com.easylive.mappers.VideoPlayHistoryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: video_play_history ServiceImpl
 * @date: 2025-03-15
 */
@Service("videoPlayHistoryService")
public class VideoPlayHistoryServiceImpl implements VideoPlayHistoryService {

    @Resource
    private VideoPlayHistoryMapper<VideoPlayHistory, VideoPlayHistoryQuery> videoPlayHistoryMapper;

    /**
     * 根据条件查询列表
     */
    public List<VideoPlayHistory> findListByParam(VideoPlayHistoryQuery query) {
        return videoPlayHistoryMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(VideoPlayHistoryQuery query) {
        return videoPlayHistoryMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<VideoPlayHistory> list = this.findListByParam(query);
        PaginationResultVo<VideoPlayHistory> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(VideoPlayHistory bean) {
        return videoPlayHistoryMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<VideoPlayHistory> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoPlayHistoryMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<VideoPlayHistory> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoPlayHistoryMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据UserIdAndVideoId查询
     */
    public VideoPlayHistory getVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId) {
        return videoPlayHistoryMapper.selectByUserIdAndVideoId(userId, videoId);
    }

    /**
     * 根据UserIdAndVideoId更新
     */
    public Integer updateVideoPlayHistoryByUserIdAndVideoId(VideoPlayHistory bean, String userId, String videoId) {
        return videoPlayHistoryMapper.updateByUserIdAndVideoId(bean, userId, videoId);
    }

    /**
     * 根据UserIdAndVideoId删除
     */
    public Integer deleteVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId) {
        return videoPlayHistoryMapper.deleteByUserIdAndVideoId(userId, videoId);
    }

    @Override
    public void saveVideoPlayHistory(String userId, String videoId, Integer fileIndex) {
        VideoPlayHistory videoPlayHistory = new VideoPlayHistory();
        videoPlayHistory.setUserId(userId);
        videoPlayHistory.setVideoId(videoId);
        videoPlayHistory.setFileIndex(fileIndex);
        videoPlayHistory.setLastUpdateTime(new Date());
        videoPlayHistoryMapper.insertOrUpdate(videoPlayHistory);
    }

    @Override
    public void deleteAll(String userId) {
        videoPlayHistoryMapper.deleteAll(userId);
    }
}