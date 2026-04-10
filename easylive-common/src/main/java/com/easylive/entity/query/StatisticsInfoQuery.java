package com.easylive.entity.query;


/**
 * @description: 数据统计表查询对象
 * @date: 2025-03-15
 */
public class StatisticsInfoQuery extends BaseQuery {
    /**
     * 统计日期
     */
    private String statisticsData;

    private String statisticsDataFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 数据统计类型
     */
    private Integer dataType;

    /**
     * 统计数量
     */
    private Integer statisticsCount;

    private String statisticsDataStart;
    
    private String statisticsDataEnd;

    public String getStatisticsDataStart() {
        return statisticsDataStart;
    }

    public void setStatisticsDataStart(String statisticsDataStart) {
        this.statisticsDataStart = statisticsDataStart;
    }

    public String getStatisticsDataEnd() {
        return statisticsDataEnd;
    }

    public void setStatisticsDataEnd(String statisticsDataEnd) {
        this.statisticsDataEnd = statisticsDataEnd;
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

    public void setStatisticsDataFuzzy(String statisticsDataFuzzy) {
        this.statisticsDataFuzzy = statisticsDataFuzzy;
    }

    public String getStatisticsDataFuzzy() {
        return statisticsDataFuzzy;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getUserIdFuzzy() {
        return userIdFuzzy;
    }

}