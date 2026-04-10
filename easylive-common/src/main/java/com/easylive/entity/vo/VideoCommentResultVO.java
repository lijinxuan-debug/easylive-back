package com.easylive.entity.vo;

import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.VideoComment;

import java.util.List;

public class VideoCommentResultVO {
    private PaginationResultVo<VideoComment> commentData;
    private List<UserAction> userActionList;

    public PaginationResultVo<VideoComment> getCommentData() {
        return commentData;
    }

    public void setCommentData(PaginationResultVo<VideoComment> commentData) {
        this.commentData = commentData;
    }

    public List<UserAction> getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List<UserAction> userActionList) {
        this.userActionList = userActionList;
    }
}