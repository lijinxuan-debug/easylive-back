package com.easylive.service;

import java.util.List;
import java.util.Map;

import com.easylive.entity.po.StatisticsInfo;
import com.easylive.entity.query.StatisticsInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;

/**
 * @description: statistics_info Service
 * @date: 2025-03-15
 */
public interface StatisticsInfoService {
    /**
     * 根据条件查询列表
     */
    List<StatisticsInfo> findListByParam(StatisticsInfoQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(StatisticsInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVo<StatisticsInfo> findListByPage(StatisticsInfoQuery query);

    /**
     * 新增
     */
    Integer add(StatisticsInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<StatisticsInfo> listBean);

    /**
     * 批量新增/更新
     */
    Integer addOrUpdateBatch(List<StatisticsInfo> listBean);

    /**
     * 根据StatisticsDataAndUserIdAndDataType查询
     */
    StatisticsInfo getStatisticsInfoByStatisticsDataAndUserIdAndDataType(String statisticsData, String userId, Integer dataType);

    /**
     * 根据StatisticsDataAndUserIdAndDataType更新
     */
    Integer updateStatisticsInfoByStatisticsDataAndUserIdAndDataType(StatisticsInfo bean, String statisticsData, String userId, Integer dataType);

    /**
     * 根据StatisticsDataAndUserIdAndDataType删除
     */
    Integer deleteStatisticsInfoByStatisticsDataAndUserIdAndDataType(String statisticsData, String userId, Integer dataType);

    void updateStatisticsInfo();

    Map<String, Integer> getActualTimeStatisticsInfo(String userId);

    List<StatisticsInfo> getStatisticsInfoByQuery(StatisticsInfoQuery query);

    List<StatisticsInfo> getUserTotalInfo(StatisticsInfoQuery query);
}