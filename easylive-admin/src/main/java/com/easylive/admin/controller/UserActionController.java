package com.easylive.admin.controller;

import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.query.VideoDanmuQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.service.UserActionService;
import com.easylive.service.VideoCommentService;
import com.easylive.service.VideoDanmuService;
import com.easylive.service.VideoInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@RequestMapping("/action")
public class UserActionController extends ABaseController {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    @RequestMapping("/loadComment")
    public ResponseVo loadComment(Integer pageNo, String videoNameFuzzy) {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setOrderBy("comment_id desc");
        videoCommentQuery.setQueryVideoInfo(true);
        videoCommentQuery.setVideoNameFuzzy(videoNameFuzzy);
        PaginationResultVo<VideoComment> paginationResultVo = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/delComment")
    public ResponseVo delComment(@NotNull Integer commentId) {
        videoCommentService.deleteComment(commentId, null);
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadDanmu")
    public ResponseVo loadDanmu(Integer pageNo, String videoNameFuzzy) {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setOrderBy("danmu_id desc");
        videoDanmuQuery.setQueryVideoInfo(true);
        videoDanmuQuery.setVideoNameFuzzy(videoNameFuzzy);
        PaginationResultVo<VideoDanmu> paginationResultVo = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/delDanmu")
    public ResponseVo delDanmu(@NotNull Integer danmuId) {
        videoDanmuService.deleteDanmu(null,danmuId);
        return getSuccessResponseVo(null);
    }
}
