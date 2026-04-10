package com.easylive.service.impl;

import java.util.Date;
import java.util.List;

import com.easylive.component.EsSearchComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.enums.SearchOrderTypeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.VideoCommentMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserActionService;
import com.easylive.mappers.UserActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: user_action ServiceImpl
 * @date: 2025-03-07
 */
@Service("userActionService")
public class UserActionServiceImpl implements UserActionService {

    @Resource
    private UserActionMapper<UserAction, UserActionQuery> userActionMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 根据条件查询列表
     */
    public List<UserAction> findListByParam(UserActionQuery query) {
        return userActionMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(UserActionQuery query) {
        return userActionMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserAction> findListByPage(UserActionQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserAction> list = this.findListByParam(query);
        PaginationResultVo<UserAction> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserAction bean) {
        return userActionMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userActionMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<UserAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userActionMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据ActionId查询
     */
    public UserAction getUserActionByActionId(Integer actionId) {
        return userActionMapper.selectByActionId(actionId);
    }

    /**
     * 根据ActionId更新
     */
    public Integer updateUserActionByActionId(UserAction bean, Integer actionId) {
        return userActionMapper.updateByActionId(bean, actionId);
    }

    /**
     * 根据ActionId删除
     */
    public Integer deleteUserActionByActionId(Integer actionId) {
        return userActionMapper.deleteByActionId(actionId);
    }

    /**
     * 根据VideoIdAndCommentIdAndActionTypeAndUserId查询
     */
    public UserAction getUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
        return userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
    }

    /**
     * 根据VideoIdAndCommentIdAndActionTypeAndUserId更新
     */
    public Integer updateUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(UserAction bean, String videoId, Integer commentId, Integer actionType, String userId) {
        return userActionMapper.updateByVideoIdAndCommentIdAndActionTypeAndUserId(bean, videoId, commentId, actionType, userId);
    }

    /**
     * 根据VideoIdAndCommentIdAndActionTypeAndUserId删除
     */
    public Integer deleteUserActionByVideoIdAndCommentIdAndActionTypeAndUserId(String videoId, Integer commentId, Integer actionType, String userId) {
        return userActionMapper.deleteByVideoIdAndCommentIdAndActionTypeAndUserId(videoId, commentId, actionType, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAction(UserAction userAction) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(userAction.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        userAction.setVideoUserId(videoInfo.getUserId());

        UserActionTypeEnum actionType = UserActionTypeEnum.getByType(userAction.getActionType());
        if (actionType == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        userAction.setActionTime(new Date());
        UserAction dbUserAction = userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(userAction.getVideoId(), userAction.getCommentId(), userAction.getActionType(), userAction.getUserId());

        switch (actionType) {
            case VIDEO_LIKE:
            case VIDEO_COLLECT:
                if (dbUserAction != null) {
                    userActionMapper.deleteByActionId(dbUserAction.getActionId());
                } else {
                    userActionMapper.insert(userAction);
                }
                Integer changeCount = dbUserAction == null ? Constants.ONE : -Constants.ONE;
                videoInfoMapper.updateCountInfo(userAction.getVideoId(), actionType.getField(), changeCount);
                if (actionType == UserActionTypeEnum.VIDEO_COLLECT) {
                    //更新es收藏数量
                    esSearchComponent.updateDocCount(userAction.getVideoId(), SearchOrderTypeEnum.VIDEO_COLLECT.getField(), changeCount);
                }
                break;
            case VIDEO_COIN:
                if (videoInfo.getUserId().equals(userAction.getUserId())) {
                    throw new BusinessException("UP主不能给自己投币");
                }
                if (dbUserAction != null) {
                    throw new BusinessException("对本稿件的投币枚数已用尽");
                }
                //减少自己的硬币
                Integer updateCoinCount = userInfoMapper.updateCoinCount(userAction.getUserId(), -userAction.getActionCount());
                if (updateCoinCount == 0) {
                    throw new BusinessException("您的硬币不足");
                }
                //增加对方的硬币
                updateCoinCount = userInfoMapper.updateCoinCount(videoInfo.getUserId(), userAction.getActionCount());
                if (updateCoinCount == 0) {
                    throw new BusinessException("投币失败");
                }
                userActionMapper.insert(userAction);
                videoInfoMapper.updateCountInfo(userAction.getVideoId(), actionType.getField(), userAction.getActionCount());
                break;
            case COMMENT_LIKE:
            case COMMENT_HATE:
                UserActionTypeEnum opposeType = actionType.equals(UserActionTypeEnum.COMMENT_LIKE) ? UserActionTypeEnum.COMMENT_HATE : UserActionTypeEnum.COMMENT_LIKE;
                UserAction opposeAction = userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(userAction.getVideoId(), userAction.getCommentId(), opposeType.getType(), userAction.getUserId());
                if (opposeAction != null) {
                    userActionMapper.deleteByActionId(opposeAction.getActionId());
                }

                if (dbUserAction != null) {
                    userActionMapper.deleteByActionId(dbUserAction.getActionId());
                } else {
                    userActionMapper.insert(userAction);
                }

                changeCount = dbUserAction == null ? Constants.ONE : -Constants.ONE;
                Integer opposeChangeCount = -changeCount;
                videoCommentMapper.updateCountInfo(userAction.getCommentId(), actionType.getField(), changeCount,
                        opposeAction == null ? null : opposeType.getField(), opposeChangeCount);
                break;
        }
    }
}