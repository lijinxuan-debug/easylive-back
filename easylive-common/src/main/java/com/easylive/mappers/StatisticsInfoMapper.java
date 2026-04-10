package com.easylive.mappers;

import com.easylive.entity.po.StatisticsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @description: 数据统计表Mapper
 * @date: 2025-03-15
 */
public interface StatisticsInfoMapper<T, P> extends BaseMapper<T, P> {
    /**
     * 根据StatisticsDataAndUserIdAndDataType查询
     */
    T selectByStatisticsDataAndUserIdAndDataType(@Param("statisticsData") String statisticsData, @Param("userId") String userId, @Param("dataType") Integer dataType);

    /**
     * 根据StatisticsDataAndUserIdAndDataType更新
     */
    Integer updateByStatisticsDataAndUserIdAndDataType(@Param("bean") T t, @Param("statisticsData") String statisticsData, @Param("userId") String userId, @Param("dataType") Integer dataType);

    /**
     * 根据StatisticsDataAndUserIdAndDataType删除
     */
    Integer deleteByStatisticsDataAndUserIdAndDataType(@Param("statisticsData") String statisticsData, @Param("userId") String userId, @Param("dataType") Integer dataType);


    List<T> selectFansCountByDate(@Param("date") String date);

    List<T> selectCommentsCountByDate(@Param("date") String date);

    List<T> selectStatisticsInfoByDate(@Param("date") String date, @Param("actionArray") Integer[] actionArray);

    List<T> selectDanmuCountByDate(@Param("date") String date);

    Map<String, Integer> selectTotalCount(@Param("userId") String userId);

    List<T> selectStatisticsTotalInfoByQuery(@Param("query") P p);

    List<T> selectUserTotalInfo(@Param("query") P p);

}