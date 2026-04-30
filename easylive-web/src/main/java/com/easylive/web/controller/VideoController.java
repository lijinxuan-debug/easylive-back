package com.easylive.web.controller;

import com.easylive.component.EsSearchComponent;
import com.easylive.component.RedisComponent;
import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.VideoPreviewDto;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.VideoInfoFileQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.entity.vo.VideoInfoResultVo;
import com.easylive.entity.vo.VideoStatusCountInfoVo;
import com.easylive.enums.*;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoInfoFileService;
import com.easylive.service.VideoInfoService;
import com.easylive.service.impl.UserActionServiceImpl;
//import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@RestController
@RequestMapping("/video")
@Validated
@Slf4j
public class VideoController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private UserActionServiceImpl userActionService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private EsSearchComponent esSearchComponent;

    @RequestMapping("/loadCommendVideo")
    public ResponseVo loadCommendVideo() {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserInfo(true);
        videoInfoQuery.setOrderBy("v.create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.RECOMMEND.getType());
        List<VideoInfo> recommendVideoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVo(recommendVideoInfoList);
    }

    @RequestMapping("/loadVideo")
    public ResponseVo loadVideo(Integer pCategoryId, Integer categoryId, Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPCategoryId(pCategoryId);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setUserInfo(true);
        videoInfoQuery.setOrderBy("v.create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.NO_RECOMMEND.getType());
        PaginationResultVo<VideoInfo> recommendVideoInfoList = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVo(recommendVideoInfoList);
    }

    @RequestMapping("/getVideoInfo")
    public ResponseVo getVideoInfo(@NotEmpty String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_404);
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        List<UserAction> userActionList = new ArrayList<>();
        if (tokenUserInfoDto != null) {
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setVideoId(videoId);
            userActionQuery.setUserId(tokenUserInfoDto.getUserId());
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(),
                    UserActionTypeEnum.VIDEO_COLLECT.getType(), UserActionTypeEnum.VIDEO_COIN.getType()});
            userActionQuery.setCommentId(Constants.ZERO);
            userActionList = userActionService.findListByParam(userActionQuery);
        }

        VideoInfoResultVo videoInfoResultVo = new VideoInfoResultVo();
        videoInfoResultVo.setVideoInfo(videoInfo);
        videoInfoResultVo.setUserActionList(userActionList);
        // 只有当视频已经转码成功（或者你有预览图逻辑时）才创建
        if (videoInfo.getDuration() != null && videoInfo.getDuration() > 0) {
            VideoPreviewDto previewDto = getVideoPreviewDto(videoId, videoInfo);

            videoInfoResultVo.setPreviewConfig(previewDto);
        }

        return getSuccessResponseVo(videoInfoResultVo);
    }

    private VideoPreviewDto getVideoPreviewDto(String videoId, VideoInfo videoInfo) {
        VideoPreviewDto previewDto = new VideoPreviewDto();

        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        videoInfoFileQuery.setOrderBy("v.file_index asc");

        // 注意：videoInfo.getFilePath() 已经包含了 "video/202Xxx/..." 这一层
        String previewUrl = appConfig.getAppDomain() + "/file/videoResource/"
                + videoInfoFileQuery.getFilePath() + "/" + Constants.VIDEO_PREVIEW_NAME;

        previewDto.setUrl(previewUrl);

        // 计算间隔 (必须强转 double)
        double interval = (double) videoInfo.getDuration() / previewDto.getTotal();
        previewDto.setInterval(interval);
        return previewDto;
    }

    @RequestMapping("/loadVideoPList")
    public ResponseVo loadVideoPList(@NotEmpty String videoId) {
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        videoInfoFileQuery.setOrderBy("v.file_index asc");
        List<VideoInfoFile> videoInfoFileList = videoInfoFileService.findListByParam(videoInfoFileQuery);
        return getSuccessResponseVo(videoInfoFileList);
    }

    @RequestMapping("reportVideoPlayOnline")
    public ResponseVo reportVideoPlayOnline(@NotEmpty String fileId, @NotEmpty String deviceId) {
        return getSuccessResponseVo(redisComponent.VideoPlayOnline(fileId, deviceId));
    }

    @RequestMapping("getVideoRecommend")
    public ResponseVo getVideoRecommend(@NotEmpty String keyWord, @NotEmpty String videoId) {
        List<VideoInfo> videoInfoList = esSearchComponent.search(true, keyWord, SearchOrderTypeEnum.VIDEO_PLAY.getType(), 1, PageSizeEnum.SIZE10.getSize()).getList();
        return getSuccessResponseVo(videoInfoList.stream().filter
                (item -> !item.getVideoId().equals(videoId)).collect(Collectors.toList()));
    }

    @RequestMapping("search")
    public ResponseVo search(@NotEmpty String keyword, Integer orderType, Integer pageNo) {
        // 记录搜索热词
        redisComponent.recordSearchHotWord(keyword);
        return getSuccessResponseVo(esSearchComponent.search(true, keyword, orderType, pageNo, PageSizeEnum.SIZE30.getSize()));
    }

    @RequestMapping("getHotWordTop")
    public ResponseVo getHotWordTop() {
        List<String> searchHotWord = redisComponent.getSearchHotWord(Constants.LENGTH_10);
        return getSuccessResponseVo(searchHotWord);
    }

    @RequestMapping("loadHotVideoList")
    public ResponseVo loadHotVideoList(Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setOrderBy("v.play_count desc");
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setUserInfo(true);
        videoInfoQuery.setLastPlayHour(Constants.HOUR_24);
        PaginationResultVo<VideoInfo> hotVideoList = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVo(hotVideoList);
    }
}
