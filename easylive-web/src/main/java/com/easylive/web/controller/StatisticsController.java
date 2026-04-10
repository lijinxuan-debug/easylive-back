package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.StatisticsInfo;
import com.easylive.entity.query.StatisticsInfoQuery;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.service.StatisticsInfoService;
import com.easylive.utils.DateUtils;
import com.easylive.web.annotation.GlobalInterceptor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@RequestMapping("/statistics")
public class StatisticsController extends ABaseController {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @RequestMapping("/getActualTimeStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo getActualTimeStatisticsInfo() {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        String yesterday = DateUtils.getBeforeDayDate(1);

        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setStatisticsData(yesterday);
        statisticsInfoQuery.setUserId(tokenUserInfo.getUserId());

        List<StatisticsInfo> preDayData = statisticsInfoService.findListByParam(statisticsInfoQuery);

        Map<Integer, Integer> preDayDataMap = preDayData.stream().collect(Collectors.toMap
                (StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount, (item1, item2) -> item2));

        Map<String, Integer> totalCountInfo = statisticsInfoService.getActualTimeStatisticsInfo(tokenUserInfo.getUserId());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("preDayData", preDayDataMap);
        resultMap.put("totalCountInfo", totalCountInfo);

        return getSuccessResponseVo(resultMap);
    }

    @RequestMapping("/getWeekStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo getWeekStatisticsInfo(@NotNull Integer dataType) {
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo();

        List<String> weekDates = DateUtils.getBeforeDates(7);

        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setUserId(tokenUserInfo.getUserId());
        statisticsInfoQuery.setDataType(dataType);
        statisticsInfoQuery.setStatisticsDataStart(weekDates.get(0));
        statisticsInfoQuery.setStatisticsDataEnd(weekDates.get(weekDates.size() - 1));
        statisticsInfoQuery.setOrderBy("statistics_data desc");
        List<StatisticsInfo> statisticsInfoList = statisticsInfoService.findListByParam(statisticsInfoQuery);

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
