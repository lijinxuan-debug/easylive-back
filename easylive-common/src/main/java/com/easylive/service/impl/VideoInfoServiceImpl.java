package com.easylive.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.easylive.component.EsSearchComponent;
import com.easylive.component.RedisComponent;
import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.SysSettingDto;
import com.easylive.entity.po.*;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.VideoRecommendTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.*;
import com.easylive.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: video_info ServiceImpl
 * @date: 2025-03-06
 */
@Service("videoInfoService")
@Slf4j
public class VideoInfoServiceImpl implements VideoInfoService {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Resource
    private AppConfig appConfig;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

    @Resource
    private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

    @Resource
    private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private RedisComponent redisComponent;


    /**
     * 根据条件查询列表
     */
    public List<VideoInfo> findListByParam(VideoInfoQuery query) {
        return videoInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(VideoInfoQuery query) {
        return videoInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<VideoInfo> findListByPage(VideoInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<VideoInfo> list = this.findListByParam(query);
        PaginationResultVo<VideoInfo> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(VideoInfo bean) {
        return videoInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<VideoInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<VideoInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoInfoMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据VideoId查询
     */
    public VideoInfo getVideoInfoByVideoId(String videoId) {
        return videoInfoMapper.selectByVideoId(videoId);
    }

    /**
     * 根据VideoId更新
     */
    public Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId) {
        return videoInfoMapper.updateByVideoId(bean, videoId);
    }

    /**
     * 根据VideoId删除
     */
    public Integer deleteVideoInfoByVideoId(String videoId) {
        return videoInfoMapper.deleteByVideoId(videoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVideoInteraction(String videoId, String interaction, String userId) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setInteraction(interaction);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setVideoId(videoId);
        videoInfoMapper.updateByQuery(videoInfo, videoInfoQuery);

        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setInteraction(interaction);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(userId);
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostMapper.updateByQuery(videoInfoPost, videoInfoPostQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delVideo(String videoId, String userId) {
        VideoInfoPost dbInfo = videoInfoPostMapper.selectByVideoId(videoId);
        if (dbInfo == null || userId != null && !dbInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseEnum.CODE_404);
        }
        //删除视频信息
        videoInfoMapper.deleteByVideoId(videoId);

        //删除视频审核信息
        videoInfoPostMapper.deleteByVideoId(videoId);

        SysSettingDto sysSetting = redisComponent.getSysSetting();
        userInfoMapper.updateCoinCount(userId, -sysSetting.getPostVideoCoinCount());

        //删除es中的数据
        esSearchComponent.deleteDoc(videoId);
        //删除文件信息
        executorService.execute(() -> {

            VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
            videoInfoFileQuery.setVideoId(videoId);
            //删除文件夹
            List<VideoInfoFile> videoInfoFiles = videoInfoFileMapper.selectList(videoInfoFileQuery);
            for (VideoInfoFile videoInfoFile : videoInfoFiles) {
                try {
                    FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + videoInfoFile.getFilePath()));
                } catch (IOException e) {
                    log.error("删除文件夹失败", e);
                }
            }

            //删除文件信息
            videoInfoFileMapper.deleteByQuery(videoInfoFileQuery);
            //删除审核文件信息
            VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
            videoInfoFilePostQuery.setVideoId(videoId);
            videoInfoFilePostMapper.deleteByQuery(videoInfoFilePostQuery);
            //删除弹幕信息
            VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
            videoDanmuQuery.setVideoId(videoId);
            videoDanmuMapper.deleteByQuery(videoDanmuQuery);
            //删除评论信息
            VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
            videoCommentQuery.setVideoId(videoId);
            videoCommentMapper.deleteByQuery(videoCommentQuery);
        });
    }

    @Override
    public void addReadCount(String videoId) {
        videoInfoMapper.updateCountInfo(videoId, UserActionTypeEnum.VIDEO_PLAY.getField(), Constants.ONE);
    }

    @Override
    public void recommendVideo(String videoId) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        Integer recommendType = null;
        if (VideoRecommendTypeEnum.RECOMMEND.getType().equals(videoInfo.getRecommendType())) {
            recommendType = VideoRecommendTypeEnum.NO_RECOMMEND.getType();
        } else {
            recommendType = VideoRecommendTypeEnum.RECOMMEND.getType();
        }
        VideoInfo updateVideo = new VideoInfo();
        updateVideo.setRecommendType(recommendType);
        videoInfoMapper.updateByVideoId(updateVideo, videoId);
    }
}