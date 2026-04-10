package com.easylive.admin.controller;

import com.easylive.entity.po.StatisticsInfo;
import com.easylive.entity.query.StatisticsInfoQuery;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.DateTimePatternEnum;
import com.easylive.enums.StatisticsTypeEnum;
import com.easylive.service.StatisticsInfoService;
import com.easylive.service.UserInfoService;
import com.easylive.utils.DateUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@RequestMapping("/index")
public class IndexController extends ABaseController {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/getActualTimeStatisticsInfo")
    public ResponseVo getActualTimeStatisticsInfo() {
        String yesterday = DateUtils.getBeforeDayDate(1);

        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setStatisticsData(yesterday);
        List<StatisticsInfo> preDayData = statisticsInfoService.getStatisticsInfoByQuery(statisticsInfoQuery);

        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setJoinTimeStart(yesterday);
        userInfoQuery.setJoinTimeEnd(yesterday);
        Integer userCount = userInfoService.findCountByParam(userInfoQuery);
        preDayData.forEach(item -> {
            if (item.getDataType().equals(StatisticsTypeEnum.FANS.getType())) {
                item.setStatisticsCount(userCount);
            }
        });

        Map<Integer, Integer> preDayDataMap = preDayData.stream().collect(Collectors.toMap
                (StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount, (item1, item2) -> item2));

        Map<String, Integer> totalCountInfo = statisticsInfoService.getActualTimeStatisticsInfo(null);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("preDayData", preDayDataMap);
        resultMap.put("totalCountInfo", totalCountInfo);

        return getSuccessResponseVo(resultMap);
    }

    @RequestMapping("/getWeekStatisticsInfo")
    public ResponseVo getWeekStatisticsInfo(@NotNull Integer dataType) {

        List<String> weekDates = DateUtils.getBeforeDates(7);

        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setDataType(dataType);
        statisticsInfoQuery.setStatisticsDataStart(weekDates.get(0));
        statisticsInfoQuery.setStatisticsDataEnd(weekDates.get(weekDates.size() - 1));
        statisticsInfoQuery.setOrderBy("statistics_data desc");

        List<StatisticsInfo> statisticsInfoList = null;
        if (!dataType.equals(StatisticsTypeEnum.FANS.getType())) {
            statisticsInfoList = statisticsInfoService.getStatisticsInfoByQuery(statisticsInfoQuery);
        } else {
            statisticsInfoList = statisticsInfoService.getUserTotalInfo(statisticsInfoQuery);
        }

        Map<String, StatisticsInfo> weekData = statisticsInfoList.stream().collect(Collectors.toMap
                (item -> item.getStatisticsData(), Function.identity(), (item1, item2) -> item2));

        List<StatisticsInfo> resultDataList = new ArrayList<>();
        for (String date : weekDates) {
            StatisticsInfo statisticsInfo = weekData.get(date);
            if (statisticsInfo == null) {
                statisticsInfo = new StatisticsInfo();
                statisticsInfo.setStatisticsData(date);
                statisticsInfo.setStatisticsCount(0);
            }
            resultDataList.add(statisticsInfo);
        }
        return getSuccessResponseVo(resultDataList);
    }
}
