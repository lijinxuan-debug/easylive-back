package com.easylive.web.task;

import com.easylive.service.StatisticsInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@Component
public class SysTask {
    @Resource
    private StatisticsInfoService statisticsInfoService;

    /** 每天 10:00 汇总昨日 statistics_info */
    @Scheduled(cron = "0 0 10 * * ?")
    public void updateStatisticsInfo() {
        statisticsInfoService.updateStatisticsInfo();
    }
}
