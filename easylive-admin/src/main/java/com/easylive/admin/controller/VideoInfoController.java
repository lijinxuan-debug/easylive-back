package com.easylive.admin.controller;

import com.easylive.annotation.MessageInterceptor;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.MessageTypeEnum;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.service.VideoInfoPostService;
import com.easylive.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@RestController
@RequestMapping("/videoInfo")
@Validated
@Slf4j
public class VideoInfoController extends ABaseController {

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;

    @RequestMapping("/loadVideoList")
    public ResponseVo loadVideoList(VideoInfoPostQuery videoInfoPostQuery) {
        videoInfoPostQuery.setOrderBy("v.last_update_time desc");
        videoInfoPostQuery.setCountInfo(true);
        videoInfoPostQuery.setUserInfo(true);
        PaginationResultVo paginationResultVo = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/auditVideo")
    @MessageInterceptor(messageType = MessageTypeEnum.SYS)
    public ResponseVo auditVideo(@NotEmpty String videoId, @NotNull Integer status, String reason) {
        videoInfoPostService.auditVideo(videoId, status, reason);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/recommendVideo")
    public ResponseVo recommendVideo(@NotEmpty String videoId) {
        videoInfoService.recommendVideo(videoId);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/deleteVideo")
    public ResponseVo deleteVideo(@NotEmpty String videoId) {
        videoInfoService.delVideo(videoId, null);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadVideoPList")
    public ResponseVo loadVideoPList(@NotEmpty String videoId) {
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> listByParam = videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);
        return getSuccessResponseVo(listByParam);
    }
}
