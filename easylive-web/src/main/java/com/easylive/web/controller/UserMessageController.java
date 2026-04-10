package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.UserMessageCountDto;
import com.easylive.entity.po.UserMessage;
import com.easylive.entity.query.UserMessageQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.MessageReadTypeEnum;
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
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/loadMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadMessage(@NotNull Integer messageType, Integer pageNo) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfo.getUserId());
        userMessageQuery.setMessageType(messageType);
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setOrderBy("message_id desc");
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
