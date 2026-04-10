package com.easylive.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: Mapper
 * @date: 2025-03-08
 */
public interface UserFocusMapper<T, P> extends BaseMapper<T, P> {
    /**
     * 根据UserIdAndFocusUserId查询
     */
    T selectByUserIdAndFocusUserId(@Param("userId") String userId, @Param("focusUserId") String focusUserId);

    /**
     * 根据UserIdAndFocusUserId更新
     */
    Integer updateByUserIdAndFocusUserId(@Param("bean") T t, @Param("userId") String userId, @Param("focusUserId") String focusUserId);

    /**
     * 根据UserIdAndFocusUserId删除
     */
    Integer deleteByUserIdAndFocusUserId(@Param("userId") String userId, @Param("focusUserId") String focusUserId);

    Integer selectFocusCount(@Param("userId") String userId);

    Integer selectFansCount(@Param("userId") String userId);
}