package com.easylive.service;

import java.util.List;

import com.easylive.entity.dto.UserMessageCountDto;
import com.easylive.entity.po.UserMessage;
import com.easylive.entity.query.UserMessageQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.enums.MessageTypeEnum;

/**
 * @description: user_message Service
 * @date: 2025-03-15
 */
public interface UserMessageService {
    /**
     * 根据条件查询列表
     */
    List<UserMessage> findListByParam(UserMessageQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserMessageQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<UserMessage> findListByPage(UserMessageQuery query);

    /**
     * 新增
     */
    Integer add(UserMessage bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserMessage> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<UserMessage> listBean);

    /**
     * 根据MessageId查询
     */
    UserMessage getUserMessageByMessageId(Integer messageId);

    /**
     * 根据MessageId更新
     */
    Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId);

    /**
     * 根据MessageId删除
     */
    Integer deleteUserMessageByMessageId(Integer messageId);

    /**
     * 根据条件更新
     */
    void updateByQuery(UserMessage bean, UserMessageQuery query);

    /**
     * 根据条件删除
     */
    void deleteByQuery(UserMessageQuery query);

    void saveMessage(String videoId, String sendUserId, MessageTypeEnum messageTypeEnum, String content, Integer replyCommentId);

    List<UserMessageCountDto> findNoReadCountGroup(String userId);
}