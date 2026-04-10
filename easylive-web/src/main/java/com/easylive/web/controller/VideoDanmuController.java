package com.easylive.web.controller;

import java.util.ArrayList;
import java.util.Date;

import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.VideoDanmuQuery;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.service.VideoDanmuService;
import com.easylive.service.VideoInfoService;
import com.easylive.web.annotation.GlobalInterceptor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @description: video_danmu Controller
 * @date: 2025-03-07
 */
@RestController
@RequestMapping("/danmu")
public class VideoDanmuController extends ABaseController {

    @Resource
    private VideoDanmuService videoDanmuService;

    @Resource
    private VideoInfoService videoInfoService;

    @RequestMapping("/postDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo postDanmu(@NotEmpty String videoId, @NotEmpty String fileId,
                                @NotEmpty @Size(max = 200) String text, @NotEmpty String color,
                                @NotEmpty Integer mode, @NotNull Integer time) {

        VideoDanmu videoDanmu = new VideoDanmu();
        videoDanmu.setVideoId(videoId);
        videoDanmu.setFileId(fileId);
        videoDanmu.setText(text);
        videoDanmu.setColor(color);
        videoDanmu.setMode(mode);
        videoDanmu.setTime(time);
        videoDanmu.setUserId(getTokenUserInfo().getUserId());
        videoDanmu.setPostTime(new Date());
        videoDanmuService.saveVideoDanmu(videoDanmu);

        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadDanmu")
    public ResponseVo loadDanmu(@NotEmpty String videoId, @NotEmpty String fileId) {

        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            return getSuccessResponseVo(new ArrayList<>());
        }
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setFileId(fileId);
        videoDanmuQuery.setOrderBy("v.danmu_id asc");
        return getSuccessResponseVo(videoDanmuService.findListByParam(videoDanmuQuery));
    }
}