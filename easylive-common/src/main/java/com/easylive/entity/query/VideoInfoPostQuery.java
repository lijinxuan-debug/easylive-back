package com.easylive.entity.query;

import java.util.Date;


/**
 * @description: 审核视频信息查询对象
 * @date: 2025-03-06
 */
public class VideoInfoPostQuery extends BaseQuery {
    /**
     * 视频ID
     */
    private String videoId;

    private String videoIdFuzzy;

    /**
     * 视频封面
     */
    private String videoCover;

    private String videoCoverFuzzy;

    /**
     * 视频名称
     */
    private String videoName;

    private String videoNameFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 创建时间
     */
    private Date createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    private String lastUpdateTimeStart;

    private String lastUpdateTimeEnd;

    /**
     * 一级分类ID
     */
    private Integer pCategoryId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 0：转码中 1：转码失败 2：待审核 3：审核成功 4：审核失败
     */
    private Integer status;

    /**
     * 0：自制 1：转载
     */
    private Integer postType;

    /**
     * 转载源资源说明
     */
    private String originInfo;

    private String originInfoFuzzy;

    /**
     * 标签
     */
    private String tags;

    private String tagsFuzzy;

    /**
     * 简介
     */
    private String introduction;

    private String introductionFuzzy;

    /**
     * 互动设置
     */
    private String interaction;

    private String interactionFuzzy;

    /**
     * 持续时间（秒）
     */
    private Integer duration;

    private Integer[] excludeStatusArray;

    private Boolean countInfo;

    private Boolean userInfo;

    public Boolean getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Boolean userInfo) {
        this.userInfo = userInfo;
    }

    public Integer[] getExcludeStatusArray() {
        return excludeStatusArray;
    }

    public void setExcludeStatusArray(Integer[] excludeStatusArray) {
        this.excludeStatusArray = excludeStatusArray;
    }

    public Boolean getCountInfo() {
        return countInfo;
    }

    public void setCountInfo(Boolean countInfo) {
        this.countInfo = countInfo;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setPCategoryId(Integer pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public Integer getPCategoryId() {
        return pCategoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setPostType(Integer postType) {
        this.postType = postType;
    }

    public Integer getPostType() {
        return postType;
    }

    public void setOriginInfo(String originInfo) {
        this.originInfo = originInfo;
    }

    public String getOriginInfo() {
        return originInfo;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setInteraction(String interaction) {
        this.interaction = interaction;
    }

    public String getInteraction() {
        return interaction;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setVideoIdFuzzy(String videoIdFuzzy) {
        this.videoIdFuzzy = videoIdFuzzy;
    }

    public String getVideoIdFuzzy() {
        return videoIdFuzzy;
    }

    public void setVideoCoverFuzzy(String videoCoverFuzzy) {
        this.videoCoverFuzzy = videoCoverFuzzy;
    }

    public String getVideoCoverFuzzy() {
        return videoCoverFuzzy;
    }

    public void setVideoNameFuzzy(String videoNameFuzzy) {
        this.videoNameFuzzy = videoNameFuzzy;
    }

    public String getVideoNameFuzzy() {
        return videoNameFuzzy;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getUserIdFuzzy() {
        return userIdFuzzy;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setLastUpdateTimeStart(String lastUpdateTimeStart) {
        this.lastUpdateTimeStart = lastUpdateTimeStart;
    }

    public String getLastUpdateTimeStart() {
        return lastUpdateTimeStart;
    }

    public void setLastUpdateTimeEnd(String lastUpdateTimeEnd) {
        this.lastUpdateTimeEnd = lastUpdateTimeEnd;
    }

    public String getLastUpdateTimeEnd() {
        return lastUpdateTimeEnd;
    }

    public void setOriginInfoFuzzy(String originInfoFuzzy) {
        this.originInfoFuzzy = originInfoFuzzy;
    }

    public String getOriginInfoFuzzy() {
        return originInfoFuzzy;
    }

    public void setTagsFuzzy(String tagsFuzzy) {
        this.tagsFuzzy = tagsFuzzy;
    }

    public String getTagsFuzzy() {
        return tagsFuzzy;
    }

    public void setIntroductionFuzzy(String introductionFuzzy) {
        this.introductionFuzzy = introductionFuzzy;
    }

    public String getIntroductionFuzzy() {
        return introductionFuzzy;
    }

    public void setInteractionFuzzy(String interactionFuzzy) {
        this.interactionFuzzy = interactionFuzzy;
    }

    public String getInteractionFuzzy() {
        return interactionFuzzy;
    }

}