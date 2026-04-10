package com.easylive.entity.po;

import java.io.Serializable;


/**
 * @description: 数据统计表
 * @date: 2025-03-15
 */
public class StatisticsInfo implements Serializable {
    /**
     * 统计日期
     */
    private String statisticsData;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 数据统计类型
     */
    private Integer dataType;
    /**
     * 统计数量
     */
    private Integer statisticsCount;

    private Integer actionCount;

    public Integer getActionCount() {
        return actionCount;
    }

    public void setActionCount(Integer actionCount) {
        this.actionCount = actionCount;
    }

    public void setStatisticsData(String statisticsData) {
        this.statisticsData = statisticsData;
    }

    public String getStatisticsData() {
        return statisticsData;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setStatisticsCount(Integer statisticsCount) {
        this.statisticsCount = statisticsCount;
    }

    public Integer getStatisticsCount() {
        return statisticsCount;
    }

    @Override
    public String toString() {
        return "统计日期:" + (statisticsData == null ? "空" : statisticsData) + " ,用户ID:" + (userId == null ? "空" : userId) + " ,数据统计类型:" + (dataType == null ? "空" : dataType) + " ,统计数量:" + (statisticsCount == null ? "空" : statisticsCount);
    }
}