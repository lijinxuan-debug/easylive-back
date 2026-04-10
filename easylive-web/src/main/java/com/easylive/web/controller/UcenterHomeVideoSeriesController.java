package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.*;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.entity.vo.UserVideoSeriesDetailVo;
import com.easylive.enums.ResponseEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.*;
import com.easylive.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@RestController
@RequestMapping("/ucenter/series")
@Validated
@Slf4j
public class UcenterHomeVideoSeriesController extends ABaseController {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserVideoSeriesService userVideoSeriesService;

    @Resource
    private UserVideoSeriesVideoService userVideoSeriesVideoService;

    @RequestMapping("/loadVideoSeries")
    public ResponseVo loadVideoSeries(@NotEmpty String userId) {
        List<UserVideoSeries> userVideoSeriesList = userVideoSeriesService.getUserAllVideoSeries(userId);
        return getSuccessResponseVo(userVideoSeriesList);
    }

    @RequestMapping("/saveVideoSeries")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo saveVideoSeries(Integer seriesId, @NotEmpty @Size(max = 100) String seriesName,
                                      @Size(max = 200) String seriesDescription, String videoIds) {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        UserVideoSeries userVideoSeries = new UserVideoSeries();
        userVideoSeries.setSeriesId(seriesId);
        userVideoSeries.setSeriesName(seriesName);
        userVideoSeries.setSeriesDescription(seriesDescription);
        userVideoSeries.setUserId(tokenUserInfoDto.getUserId());
        userVideoSeriesService.saveUserVideoSeries(userVideoSeries, videoIds);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadAllVideo(Integer seriesId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        VideoInfoQuery infoQuery = new VideoInfoQuery();
        if (seriesId != null) {
            UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
            userVideoSeriesVideoQuery.setSeriesId(seriesId);
            userVideoSeriesVideoQuery.setUserId(tokenUserInfoDto.getUserId());
            List<UserVideoSeriesVideo> list = userVideoSeriesVideoService.findListByParam(userVideoSeriesVideoQuery);
            List<String> videoIdList = list.stream().map(item -> item.getVideoId()).collect(Collectors.toList());
            infoQuery.setExcludeVideoIdArray(videoIdList.toArray(new String[videoIdList.size()]));
        }
        infoQuery.setUserId(tokenUserInfoDto.getUserId());
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(infoQuery);
        return getSuccessResponseVo(videoInfoList);
    }

    @RequestMapping("/getVideoSeriesDetail")
    public ResponseVo getVideoSeriesDetail(@NotNull Integer seriesId) {
        UserVideoSeries userVideoSeriesBySeriesId = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        if (userVideoSeriesBySeriesId == null) {
            throw new BusinessException(ResponseEnum.CODE_404);
        }
        UserVideoSeriesVideoQuery userVideoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
        userVideoSeriesVideoQuery.setSeriesId(seriesId);
        userVideoSeriesVideoQuery.setQueryVideoInfo(true);
        userVideoSeriesVideoQuery.setOrderBy("u.sort asc");
        List<UserVideoSeriesVideo> userVideoSeriesVideoList = userVideoSeriesVideoService.findListByParam(userVideoSeriesVideoQuery);

        UserVideoSeriesDetailVo userVideoSeriesDetailVo = new UserVideoSeriesDetailVo();
        userVideoSeriesDetailVo.setUserVideoSeries(userVideoSeriesBySeriesId);
        userVideoSeriesDetailVo.setUserVideoSeriesVideos(userVideoSeriesVideoList);
        return getSuccessResponseVo(userVideoSeriesDetailVo);
    }

    @RequestMapping("/saveSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo saveSeriesVideo(@NotNull Integer seriesId,
                                      @NotEmpty String videoIds) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        userVideoSeriesService.saveVideo2UserVideoSeries(userInfoDto.getUserId(), videoIds, seriesId);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/delSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo delSeriesVideo(@NotNull Integer seriesId,
                                     @NotEmpty String videoId) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        userVideoSeriesService.delSeriesVideo(userInfoDto.getUserId(), videoId, seriesId);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/delSeries")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo delSeries(@NotNull Integer seriesId) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        userVideoSeriesService.delSeries(userInfoDto.getUserId(), seriesId);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/changeVideoSeriesSort")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo changeVideoSeriesSort(@NotEmpty String seriesIds) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        userVideoSeriesService.changeVideoSeriesSort(userInfoDto.getUserId(), seriesIds);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadVideoSeriesWithVideo")
    public ResponseVo loadVideoSeriesWithVideo(@NotEmpty String userId) {
        UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
        userVideoSeriesQuery.setUserId(userId);
        userVideoSeriesQuery.setOrderBy("u.sort asc");
        List<UserVideoSeries> userVideoSeriesList = userVideoSeriesService.findListWithVideo(userVideoSeriesQuery);
        return getSuccessResponseVo(userVideoSeriesList);
    }
}
