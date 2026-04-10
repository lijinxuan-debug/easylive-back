package com.easylive.web.controller;

import com.easylive.annotation.MessageInterceptor;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.entity.vo.VideoCommentResultVO;
import com.easylive.enums.*;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserActionService;
import com.easylive.service.VideoCommentService;
import com.easylive.service.VideoInfoService;
import com.easylive.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@RestController
@RequestMapping("/comment")
@Validated
@Slf4j
public class VideoCommentController extends ABaseController {
    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private UserActionService userActionService;

    @Resource
    private VideoInfoService videoInfoService;

    @RequestMapping("postComment")
    @GlobalInterceptor(checkLogin = true)
    @MessageInterceptor(messageType = MessageTypeEnum.COMMENT)
    public ResponseVo postComment(@NotEmpty String videoId,
                                  @NotEmpty @Size(max = 500) String content,
                                  Integer replyCommentId,
                                  @Size(max = 500) String imgPath) {

        TokenUserInfoDto tokenFromCookie = getTokenUserInfo();
        VideoComment videoComment = new VideoComment();
        videoComment.setUserId(tokenFromCookie.getUserId());
        videoComment.setAvatar(tokenFromCookie.getAvatar());
        videoComment.setNickName(tokenFromCookie.getNickName());
        videoComment.setVideoId(videoId);
        videoComment.setImgPath(imgPath);
        videoComment.setContent(content);
        videoCommentService.postComment(videoComment, replyCommentId);
        return getSuccessResponseVo(videoComment);
    }

    @RequestMapping("loadComment")
    public ResponseVo loadComment(@NotEmpty String videoId,
                                  Integer pageNo,
                                  Integer orderType) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
            return getSuccessResponseVo(new ArrayList<>());
        }
        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setVideoId(videoId);
        commentQuery.setPageNo(pageNo);
        commentQuery.setPageSize(PageSizeEnum.SIZE15.getSize());
        commentQuery.setpCommentId(Constants.ZERO);
        commentQuery.setLoadChildren(true);
        String orderBy = orderType == null || orderType == 0 ? "v.like_count desc,v.comment_id desc" : "v.comment_id desc";
        commentQuery.setOrderBy(orderBy);

        PaginationResultVo<VideoComment> commentData = videoCommentService.findListByPage(commentQuery);

        if (pageNo == null) {
            List<VideoComment> topCommentList = topComment(videoId);
            if (!topCommentList.isEmpty()) {
                List<VideoComment> commentList = commentData.getList().stream().filter(item -> !item.getCommentId().equals(topCommentList.get(0).getCommentId())).collect(Collectors.toList());
                commentList.addAll(0, topCommentList);
                commentData.setList(commentList);
            }
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        List<UserAction> userActionList = new ArrayList<>();
        if (tokenUserInfoDto != null) {
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setVideoId(videoId);
            userActionQuery.setUserId(tokenUserInfoDto.getUserId());
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.COMMENT_LIKE.getType(), UserActionTypeEnum.COMMENT_HATE.getType()});
            userActionList = userActionService.findListByParam(userActionQuery);
        }
        VideoCommentResultVO videoCommentResultVO = new VideoCommentResultVO();
        videoCommentResultVO.setCommentData(commentData);
        videoCommentResultVO.setUserActionList(userActionList);

        return getSuccessResponseVo(videoCommentResultVO);
    }

    private List<VideoComment> topComment(String videoId) {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
        videoCommentQuery.setLoadChildren(true);
        return videoCommentService.findListByParam(videoCommentQuery);
    }

    @RequestMapping("top")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo top(@NotNull Integer commentId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        videoCommentService.topComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVo(null);
    }

    @RequestMapping("cancelTop")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo cancelTop(@NotNull Integer commentId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        videoCommentService.cancelTopComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVo(null);
    }

    @RequestMapping("deleteComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo deleteComment(@NotNull Integer commentId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        videoCommentService.deleteComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVo(null);
    }
}
