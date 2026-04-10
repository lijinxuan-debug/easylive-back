package com.easylive.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.easylive.entity.dto.UserMessageCountDto;
import com.easylive.entity.dto.UserMessageExtendDto;
import com.easylive.entity.po.UserMessage;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.enums.MessageReadTypeEnum;
import com.easylive.enums.MessageTypeEnum;
import com.easylive.enums.PageSizeEnum;
import com.easylive.mappers.VideoCommentMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.mappers.VideoInfoPostMapper;
import com.easylive.service.UserMessageService;
import com.easylive.mappers.UserMessageMapper;
import com.easylive.utils.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: user_message ServiceImpl
 * @date: 2025-03-15
 */
@Service("userMessageService")
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

    /**
     * 根据条件查询列表
     */
    public List<UserMessage> findListByParam(UserMessageQuery query) {
        return userMessageMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(UserMessageQuery query) {
        return userMessageMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserMessage> findListByPage(UserMessageQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserMessage> list = this.findListByParam(query);
        PaginationResultVo<UserMessage> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserMessage bean) {
        return userMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userMessageMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据MessageId查询
     */
    public UserMessage getUserMessageByMessageId(Integer messageId) {
        return userMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId更新
     */
    public Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
        return userMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    public Integer deleteUserMessageByMessageId(Integer messageId) {
        return userMessageMapper.deleteByMessageId(messageId);
    }

    @Override
    public void updateByQuery(UserMessage bean, UserMessageQuery query) {
        userMessageMapper.updateByQuery(bean, query);
    }

    @Override
    public void deleteByQuery(UserMessageQuery query) {
        userMessageMapper.deleteByQuery(query);
    }

    @Override
    @Async
    public void saveMessage(String videoId, String sendUserId, MessageTypeEnum messageTypeEnum, String content, Integer replyCommentId) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
        if (videoInfo == null) {
            return;
        }

        UserMessageExtendDto userMessageDto = new UserMessageExtendDto();
        userMessageDto.setMessageContent(content);

        String receiveUserId = videoInfo.getUserId();

        //点赞，收藏，已经记录过，不再重复记录
        if (ArrayUtils.contains(new Integer[]{MessageTypeEnum.LIKE.getType(), MessageTypeEnum.COLLECTION.getType()},
                messageTypeEnum.getType())) {
            UserMessageQuery query = new UserMessageQuery();
            query.setUserId(receiveUserId);
            query.setVideoId(videoId);
            query.setMessageType(messageTypeEnum.getType());
            Integer count = userMessageMapper.selectCount(query);
            if (count > 0) {
                return;
            }
        }

        UserMessage userMessage = new UserMessage();
        userMessage.setVideoId(videoId);
        userMessage.setMessageType(messageTypeEnum.getType());
        userMessage.setSendUserId(sendUserId);
        userMessage.setCreateTime(new Date());
        userMessage.setReadType(MessageReadTypeEnum.NO_READ.getType());
        //评论特殊处理
        if (replyCommentId != null) {
            VideoComment videoComment = videoCommentMapper.selectByCommentId(replyCommentId);
            if (videoComment != null) {
                receiveUserId = videoComment.getUserId();
                userMessageDto.setMessageContentReply(videoComment.getContent());
            }
        }
        //本人操作
        if (receiveUserId.equals(sendUserId)) {
            return;
        }
        //系统消息
        if (MessageTypeEnum.SYS.getType().equals(messageTypeEnum.getType())) {
            VideoInfoPost videoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);
            userMessageDto.setAuditStatus(videoInfoPost.getStatus());
        }
        userMessage.setUserId(receiveUserId);
        userMessage.setExtendJson(JsonUtils.convertObj2Json(userMessageDto));
        userMessageMapper.insert(userMessage);
    }

    @Override
    public List<UserMessageCountDto> findNoReadCountGroup(String userId) {
        return userMessageMapper.findNoReadCountGroup(userId);
    }
}