package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.UserFocus;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: user_focus Service
 * @date: 2025-03-08
 */
public interface UserFocusService {
    /**
     * 根据条件查询列表
     */
    List<UserFocus> findListByParam(UserFocusQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserFocusQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<UserFocus> findListByPage(UserFocusQuery query);

    /**
     * 新增
     */
    Integer add(UserFocus bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserFocus> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<UserFocus> listBean);

    /**
     * 根据UserIdAndFocusUserId查询
     */
    UserFocus getUserFocusByUserIdAndFocusUserId(String userId, String focusUserId);

    /**
     * 根据UserIdAndFocusUserId更新
     */
    Integer updateUserFocusByUserIdAndFocusUserId(UserFocus bean, String userId, String focusUserId);

    /**
     * 根据UserIdAndFocusUserId删除
     */
    Integer deleteUserFocusByUserIdAndFocusUserId(String userId, String focusUserId);

    void focus(String userId, String focusUserId);

    void cancelFocus(String userId, String focusUserId);
}