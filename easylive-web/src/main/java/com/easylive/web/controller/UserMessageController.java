package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.UserMessageCountDto;
import com.easylive.entity.po.UserMessage;
import com.easylive.entity.query.UserMessageQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.MessageReadTypeEnum;
import com.easylive.enums.MessageTypeEnum;
import com.easylive.service.UserMessagePushService;
import com.easylive.service.UserMessageService;
import com.easylive.web.annotation.GlobalInterceptor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@Validated
@RequestMapping("/message")
public class UserMessageController extends ABaseController {

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private UserMessagePushService userMessagePushService;

    @RequestMapping("/getNoReadCount")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo getNoReadCount() {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfo.getUserId());
        userMessageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());
        Integer count = userMessageService.findCountByParam(userMessageQuery);
        return getSuccessResponseVo(count);
    }

    @RequestMapping("/getNoReadCountGroup")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo getNoReadCountGroup() {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();
        List<UserMessageCountDto> list = userMessageService.findNoReadCountGroup(tokenUserInfo.getUserId());
        return getSuccessResponseVo(list);
    }

    @RequestMapping("/readAll")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo readAll(@NotNull Integer messageType) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfo.getUserId());
        userMessageQuery.setMessageType(messageType);

        UserMessage userMessage = new UserMessage();
        userMessage.setReadType(MessageReadTypeEnum.READ.getType());
        userMessageService.updateByQuery(userMessage, userMessageQuery);
        if (userMessagePushService != null) {
            userMessagePushService.onUnreadChanged(tokenUserInfo.getUserId());
        }
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/readMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo readMessage(@NotNull Integer messageId) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfo.getUserId());
        userMessageQuery.setMessageId(messageId);

        UserMessage userMessage = new UserMessage();
        userMessage.setReadType(MessageReadTypeEnum.READ.getType());
        userMessageService.updateByQuery(userMessage, userMessageQuery);
        if (userMessagePushService != null) {
            userMessagePushService.onUnreadChanged(tokenUserInfo.getUserId());
        }
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadAllMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadAllMessage(Integer pageNo) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfo.getUserId());
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setOrderBy("message_id desc");
        userMessageQuery.setMessageTypeArray(new Integer[]{
                MessageTypeEnum.LIKE.getType(),
                MessageTypeEnum.COLLECTION.getType(),
                MessageTypeEnum.COMMENT.getType(),
                MessageTypeEnum.FANS.getType(),
        });
        userMessageQuery.setHideInactiveFans(1);
        PaginationResultVo<UserMessage> paginationResultVo = userMessageService.findListByPage(userMessageQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/loadMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadMessage(@NotNull Integer messageType, Integer pageNo, String messageTypes, Integer atMeOnly) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfo.getUserId());
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setOrderBy("message_id desc");
        if (messageTypes != null && !messageTypes.trim().isEmpty()) {
            String[] typeArray = messageTypes.split(",");
            Integer[] messageTypeArray = new Integer[typeArray.length];
            for (int i = 0; i < typeArray.length; i++) {
                messageTypeArray[i] = Integer.parseInt(typeArray[i].trim());
            }
            userMessageQuery.setMessageTypeArray(messageTypeArray);
        } else {
            userMessageQuery.setMessageType(messageType);
        }
        if (atMeOnly != null && atMeOnly == 1) {
            userMessageQuery.setAtMeOnly(1);
            String nickName = tokenUserInfo.getNickName();
            if (nickName != null && !nickName.trim().isEmpty()) {
                userMessageQuery.setAtMeKeyword("@" + nickName.trim());
            }
        }
        if (MessageTypeEnum.FANS.getType().equals(messageType)) {
            userMessageQuery.setHideInactiveFans(1);
        }
        PaginationResultVo<UserMessage> paginationResultVo = userMessageService.findListByPage(userMessageQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    @RequestMapping("/delMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo delMessage(@NotNull Integer messageId) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfo.getUserId());
        userMessageQuery.setMessageId(messageId);
        userMessageService.deleteByQuery(userMessageQuery);
        return getSuccessResponseVo(null);
    }
}
