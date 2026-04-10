package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.*;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.service.*;
import com.easylive.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;
import java.util.List;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@RestController
@RequestMapping("/ucenter/interaction")
@Validated
@Slf4j
public class UcenterInterActionController extends ABaseController {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    @RequestMapping("/loadAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadAllVideo() {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userInfoDto.getUserId());
        videoInfoQuery.setOrderBy("v.create_time desc");
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVo(videoInfoList);
    }

    @RequestMapping("/loadComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadComment(Integer pageNo, String videoId) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setVideoUserId(userInfoDto.getUserId());
        videoCommentQuery.setOrderBy("v.comment_id desc");
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setQueryVideoInfo(true);
        PaginationResultVo<VideoComment> paginationResultVo = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/delComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadComment(@NotNull Integer commentId) {
        videoCommentService.deleteComment(commentId, getTokenUserInfo().getUserId());
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadDanmu(Integer pageNo, String videoId) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo();
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setVideoUserId(userInfoDto.getUserId());
        videoDanmuQuery.setOrderBy("v.danmu_id desc");
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setQueryVideoInfo(true);
        PaginationResultVo<VideoDanmu> paginationResultVo = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/delDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo delDanmu(@NotNull Integer danmuId) {
        videoDanmuService.deleteDanmu(getTokenUserInfo().getUserId(), danmuId);
        return getSuccessResponseVo(null);
    }
}
