package com.easylive.web.controller;

import com.easylive.annotation.MessageInterceptor;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.MessageTypeEnum;
import com.easylive.service.UserActionService;
import com.easylive.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

/**
 * @description: video_danmu Controller
 * @date: 2025-03-07
 */
@RestController
@RequestMapping("/userAction")
@Validated
@Slf4j
public class UserActionController extends ABaseController {

    @Resource
    private UserActionService userActionService;

    @RequestMapping("/doAction")
    @GlobalInterceptor(checkLogin = true)
    @MessageInterceptor(messageType = MessageTypeEnum.LIKE)
    public ResponseVo doAction(@NotEmpty String videoId, @NotNull Integer actionType,
                               @Max(2) @Min(1) Integer actionCount, Integer commentId) {
        UserAction userAction = new UserAction();
        userAction.setUserId(getTokenUserInfo().getUserId());
        userAction.setVideoId(videoId);
        commentId = commentId == null ? Constants.ZERO : commentId;
        userAction.setCommentId(commentId);
        userAction.setActionType(actionType);
        actionCount = actionCount == null ? Constants.ONE : actionCount;
        userAction.setActionCount(actionCount);
        userActionService.saveAction(userAction);
        return getSuccessResponseVo(null);
    }
}