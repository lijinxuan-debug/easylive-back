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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: user_action ServiceImpl
 * @date: 2025-03-07
 */
@Service("userActionService")
@Slf4j
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
        log.info("开始处理用户操作 - userId: {}, videoId: {}, actionType: {}, commentId: {}",
                userAction.getUserId(), userAction.getVideoId(), userAction.getActionType(), userAction.getCommentId());

        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(userAction.getVideoId());
        if (videoInfo == null) {
            log.error("视频不存在 - videoId: {}", userAction.getVideoId());
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        userAction.setVideoUserId(videoInfo.getUserId());

        UserActionTypeEnum actionType = UserActionTypeEnum.getByType(userAction.getActionType());
        if (actionType == null) {
            log.error("操作类型无效 - actionType: {}", userAction.getActionType());
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        userAction.setActionTime(new Date());
        UserAction dbUserAction = userActionMapper.selectByVideoIdAndCommentIdAndActionTypeAndUserId(
                userAction.getVideoId(), userAction.getCommentId(), userAction.getActionType(), userAction.getUserId());
        log.info("查询现有操作记录 - dbUserAction: {}", dbUserAction);

        switch (actionType) {
            case VIDEO_LIKE:
            case VIDEO_COLLECT:
                log.info("处理点赞/收藏 - actionType: {}, dbUserAction: {}", actionType, dbUserAction);
                if (dbUserAction != null) {
                    log.info("取消操作 - 删除记录, actionId: {}", dbUserAction.getActionId());
                    userActionMapper.deleteByActionId(dbUserAction.getActionId());
                } else {
                    log.info("新增操作 - 插入记录");
                    userActionMapper.insert(userAction);
                }
                Integer changeCount = dbUserAction == null ? Constants.ONE : -Constants.ONE;
                log.info("更新视频计数 - videoId: {}, field: {}, changeCount: {}",
                        userAction.getVideoId(), actionType.getField(), changeCount);
                videoInfoMapper.updateCountInfo(userAction.getVideoId(), actionType.getField(), changeCount);
                //更新ES点赞/收藏数量
                if (actionType == UserActionTypeEnum.VIDEO_LIKE) {
                    log.info("更新ES点赞数量");
//                    esSearchComponent.updateDocCount(userAction.getVideoId(), SearchOrderTypeEnum.VIDEO_LIKE.getField(), changeCount);
                } else if (actionType == UserActionTypeEnum.VIDEO_COLLECT) {
                    log.info("更新ES收藏数量");
                    esSearchComponent.updateDocCount(userAction.getVideoId(), SearchOrderTypeEnum.VIDEO_COLLECT.getField(), changeCount);
                }
                log.info("点赞/收藏操作完成");
                break;
            case VIDEO_COIN:
                log.info("处理投币操作");
                if (videoInfo.getUserId().equals(userAction.getUserId())) {
                    log.error("UP主不能给自己投币 - userId: {}", userAction.getUserId());
                    throw new BusinessException("UP主不能给自己投币");
                }
                if (dbUserAction != null) {
                    log.error("投币次数已用尽 - userId: {}, videoId: {}", userAction.getUserId(), userAction.getVideoId());
                    throw new BusinessException("对本稿件的投币枚数已用尽");
                }
                //减少自己的硬币
                Integer updateCoinCount = userInfoMapper.updateCoinCount(userAction.getUserId(), -userAction.getActionCount());
                if (updateCoinCount == 0) {
                    log.error("硬币不足 - userId: {}", userAction.getUserId());
                    throw new BusinessException("您的硬币不足");
                }
                //增加对方的硬币
                updateCoinCount = userInfoMapper.updateCoinCount(videoInfo.getUserId(), userAction.getActionCount());
                if (updateCoinCount == 0) {
                    log.error("投币失败");
                    throw new BusinessException("投币失败");
                }
                userActionMapper.insert(userAction);
                videoInfoMapper.updateCountInfo(userAction.getVideoId(), actionType.getField(), userAction.getActionCount());
                log.info("投币操作完成");
                break;
            case COMMENT_LIKE:
            case COMMENT_HATE:
                log.info("处理评论点赞/踩");
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
                log.info("评论点赞/踩操作完成");
                break;
        }
        log.info("用户操作处理成功完成 - userId: {}, videoId: {}, actionType: {}",
                userAction.getUserId(), userAction.getVideoId(), actionType);
    }
}