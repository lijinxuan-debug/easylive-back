package com.easylive.mappers;

import com.easylive.entity.dto.UserMessageCountDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 用户消息表Mapper
 * @date: 2025-03-15
 */
public interface UserMessageMapper<T, P> extends BaseMapper<T, P> {
    /**
     * 根据MessageId查询
     */
    T selectByMessageId(@Param("messageId") Integer messageId);

    /**
     * 根据MessageId更新
     */
    Integer updateByMessageId(@Param("bean") T t, @Param("messageId") Integer messageId);

    /**
     * 根据MessageId删除
     */
    Integer deleteByMessageId(@Param("messageId") Integer messageId);

    List<UserMessageCountDto> findNoReadCountGroup(@Param("userId") String userId);
}