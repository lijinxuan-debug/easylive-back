package com.easylive.entity.query;

import java.util.Date;


/**
 * @description: 查询对象
 * @date: 2025-03-08
 */
public class UserFocusQuery extends BaseQuery {
    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 用户ID
     */
    private String focusUserId;

    private String focusUserIdFuzzy;

    /**
     * 时间
     */
    private Date focusTime;

    private String focusTimeStart;

    private String focusTimeEnd;

    private Integer queryType;

    public Integer getQueryType() {
        return queryType;
    }

    public void setQueryType(Integer queryType) {
        this.queryType = queryType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setFocusUserId(String focusUserId) {
        this.focusUserId = focusUserId;
    }

    public String getFocusUserId() {
        return focusUserId;
    }

    public void setFocusTime(Date focusTime) {
        this.focusTime = focusTime;
    }

    public Date getFocusTime() {
        return focusTime;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getUserIdFuzzy() {
        return userIdFuzzy;
    }

    public void setFocusUserIdFuzzy(String focusUserIdFuzzy) {
        this.focusUserIdFuzzy = focusUserIdFuzzy;
    }

    public String getFocusUserIdFuzzy() {
        return focusUserIdFuzzy;
    }

    public void setFocusTimeStart(String focusTimeStart) {
        this.focusTimeStart = focusTimeStart;
    }

    public String getFocusTimeStart() {
        return focusTimeStart;
    }

    public void setFocusTimeEnd(String focusTimeEnd) {
        this.focusTimeEnd = focusTimeEnd;
    }

    public String getFocusTimeEnd() {
        return focusTimeEnd;
    }

}