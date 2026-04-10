package com.easylive.service.impl;

import java.util.Date;
import java.util.List;

import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.CommentTopTypeEnum;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.VideoCommentService;
import com.easylive.mappers.VideoCommentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: video_comment ServiceImpl
 * @date: 2025-03-07
 */
@Service("videoCommentService")
public class VideoCommentServiceImpl implements VideoCommentService {

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    public List<VideoComment> findListByParam(VideoCommentQuery query) {
        if (query.getLoadChildren() != null && query.getLoadChildren()) {
            return videoCommentMapper.selectListWithChildren(query);
        }
        return videoCommentMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(VideoCommentQuery query) {
        return videoCommentMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<VideoComment> findListByPage(VideoCommentQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<VideoComment> list = this.findListByParam(query);
        PaginationResultVo<VideoComment> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(VideoComment bean) {
        return videoCommentMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoCommentMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoCommentMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据CommentId查询
     */
    public VideoComment getVideoCommentByCommentId(Integer commentId) {
        return videoCommentMapper.selectByCommentId(commentId);
    }

    /**
     * 根据CommentId更新
     */
    public Integer updateVideoCommentByCommentId(VideoComment bean, Integer commentId) {
        return videoCommentMapper.updateByCommentId(bean, commentId);
    }

    /**
     * 根据CommentId删除
     */
    public Integer deleteVideoCommentByCommentId(Integer commentId) {
        return videoCommentMapper.deleteByCommentId(commentId);
    }

    @Override
    public void postComment(VideoComment videoComment, Integer replyCommentId) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
            throw new BusinessException("UP主未开启评论功能");
        }
        if (replyCommentId != null) {
            VideoComment replyComment = videoCommentMapper.selectByCommentId(replyCommentId);
            if (replyComment == null || !replyComment.getVideoId().equals(videoComment.getVideoId())) {
                throw new BusinessException(ResponseEnum.CODE_600);
            }
            if (replyComment.getpCommentId() == 0) {//回复的是一级评论
                videoComment.setpCommentId(replyComment.getCommentId());//本条评论的父评论就是该一级评论
            } else {
                videoComment.setpCommentId(replyComment.getpCommentId());
                videoComment.setReplyUserId(replyComment.getUserId());
            }
            UserInfo replyUserInfo = userInfoMapper.selectByUserId(replyComment.getUserId());
            videoComment.setReplyUserId(replyUserInfo.getUserId());
            videoComment.setReplyNickName(replyUserInfo.getNickName());
            videoComment.setReplyAvatar(replyUserInfo.getAvatar());
        } else {
            videoComment.setpCommentId(0);
        }
        videoComment.setPostTime(new Date());
        videoComment.setVideoUserId(videoInfo.getUserId());
        videoCommentMapper.insert(videoComment);
        if (videoComment.getpCommentId() == 0) {//如果是一级评论，则更新视频的评论数
            videoInfoMapper.updateCountInfo(videoComment.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), Constants.ONE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topComment(Integer commentId, String userId) {
        cancelTopComment(commentId, userId);
        VideoComment videoComment = new VideoComment();
        videoComment.setTopType(CommentTopTypeEnum.TOP.getType());
        videoCommentMapper.updateByCommentId(videoComment, commentId);
    }

    @Override
    public void cancelTopComment(Integer commentId, String userId) {
        VideoComment videoComment = videoCommentMapper.selectByCommentId(commentId);
        if (videoComment == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        if (!videoInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }

        VideoComment comment = new VideoComment();
        comment.setTopType(CommentTopTypeEnum.NO_TOP.getType());

        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoComment.getVideoId());
        videoCommentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
        videoCommentMapper.updateByQuery(comment, videoCommentQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Integer commentId, String userId) {
        VideoComment videoComment = videoCommentMapper.selectByCommentId(commentId);
        if (videoComment == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        if (userId != null && !videoInfo.getUserId().equals(userId) && !videoComment.getUserId().equals(userId)) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        videoCommentMapper.deleteByCommentId(commentId);

        if (videoComment.getpCommentId() == 0) {//如果是一级评论，同时删除该评论下的子评论，并更新视频的评论数
            videoInfoMapper.updateCountInfo(videoComment.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), -Constants.ONE);
            VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
            videoCommentQuery.setpCommentId(commentId);
            videoCommentMapper.deleteByQuery(videoCommentQuery);
        }
    }
}