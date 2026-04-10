package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.VideoPlayHistory;
import com.easylive.entity.query.VideoPlayHistoryQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.service.VideoPlayHistoryService;
import com.easylive.web.annotation.GlobalInterceptor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@RequestMapping("/history")
public class VideoPlayHistoryController extends ABaseController {
    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    @RequestMapping("/loadHistory")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadHistory(Integer pageNo) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setPageNo(pageNo);
        videoPlayHistoryQuery.setUserId(tokenUserInfo.getUserId());
        videoPlayHistoryQuery.setOrderBy("last_update_time desc");
        PaginationResultVo<VideoPlayHistory> paginationResultVo = videoPlayHistoryService.findListByPage(videoPlayHistoryQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/cleanAllHistory")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo cleanAllHistory() {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();
        videoPlayHistoryService.deleteAll(tokenUserInfo.getUserId());
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/delHistory")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo delHistory(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();
        videoPlayHistoryService.deleteVideoPlayHistoryByUserIdAndVideoId(tokenUserInfo.getUserId(), videoId);
        return getSuccessResponseVo(null);
    }
}
