package com.easylive.service;

import java.util.List;

import com.easylive.entity.po.UserAction;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: user_action Service
 * @date: 2025-03-07
 */
public interface UserActionService {
	/**
	 * 根据条件查询列表
	 */
	List<UserAction> findListByParam(UserActionQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserActionQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVo<UserAction> findListByPage(UserActionQuery query);

	/**
	 * 新增
	 */
	Integer add(UserAction bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserAction> listBean);

	/**
	 * 批量新增/更新
	 */
	Integer addOrUpdateBatch(List<UserAction> listBean);

	/**
	 * 根据ActionId查询
	 */
	UserAction getUserActionByActionId(Integer actionId); 

	/**
	 * 根据ActionId更新
	 */
	Integer updateUserActionByActionId(UserAction bean, Integer actionId); 

	/**
	 * 根据ActionId删除
	 */
	Integer deleteUserActionByActionId(Integer actionId); 

	/**
	 * 根据VideoIdAndCommentIdAndActionTypeAndUserId查询
	 */
	UserAction getUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId); 

	/**
	 * 根据VideoIdAndCommentIdAndActionTypeAndUserId更新
	 */
	Integer updateUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(UserAction bean, String videoId, Integer commentId, Integer actionType, String userId); 

	/**
	 * 根据VideoIdAndCommentIdAndActionTypeAndUserId删除
	 */
	Integer deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId);

    void saveAction(UserAction userAction);

}