package com.easylive.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.easylive.component.RedisComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.StatisticsInfo;
import com.easylive.entity.po.UserFocus;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.StatisticsTypeEnum;
import com.easylive.mappers.UserFocusMapper;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.StatisticsInfoService;
import com.easylive.mappers.StatisticsInfoMapper;
import com.easylive.utils.DateUtils;
import com.easylive.utils.StringTools;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: statistics_info ServiceImpl
 * @date: 2025-03-15
 */
@Service("statisticsInfoService")
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    @Resource
    private StatisticsInfoMapper<StatisticsInfo, StatisticsInfoQuery> statisticsInfoMapper;

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    public List<StatisticsInfo> findListByParam(StatisticsInfoQuery query) {
        return statisticsInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(StatisticsInfoQuery query) {
        return statisticsInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<StatisticsInfo> findListByPage(StatisticsInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<StatisticsInfo> list = this.findListByParam(query);
        PaginationResultVo<StatisticsInfo> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(StatisticsInfo bean) {
        return statisticsInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return statisticsInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return statisticsInfoMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据StatisticsDataAndUserIdAndDataType查询
     */
    public StatisticsInfo getStatisticsInfoByStatisticsDataAndUserIdAndDataType(String statisticsData, String userId, Integer dataType) {
        return statisticsInfoMapper.selectByStatisticsDataAndUserIdAndDataType(statisticsData, userId, dataType);
    }

    /**
     * 根据StatisticsDataAndUserIdAndDataType更新
     */
    public Integer updateStatisticsInfoByStatisticsDataAndUserIdAndDataType(StatisticsInfo bean, String statisticsData, String userId, Integer dataType) {
        return statisticsInfoMapper.updateByStatisticsDataAndUserIdAndDataType(bean, statisticsData, userId, dataType);
    }

    /**
     * 根据StatisticsDataAndUserIdAndDataType删除
     */
    public Integer deleteStatisticsInfoByStatisticsDataAndUserIdAndDataType(String statisticsData, String userId, Integer dataType) {
        return statisticsInfoMapper.deleteByStatisticsDataAndUserIdAndDataType(statisticsData, userId, dataType);
    }

    @Override
    public void updateStatisticsInfo() {
        List<StatisticsInfo> list = new ArrayList<>();

        final String yesterday = DateUtils.getBeforeDayDate(1);

        //统计播放量
        //例：
        // {
        //    "easylive:video:playcount:2025-03-15:HwOVQQonUn" : 1
        //    "easylive:live:playcount:2025-03-15:TaNcAOJADP" : 1,
        // }
        Map<String, Integer> playCountMap = redisComponent.getPlayCount(yesterday);
        List<String> keys = new ArrayList<>(playCountMap.keySet());
        //例：
        //    keys[0] = "HwOVQQonUn"
        //    keys[1] = "TaNcAOJADP"
        keys = keys.stream().map(item -> item.substring(item.lastIndexOf(":") + 1)).collect(Collectors.toList());

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoIdArray(keys.toArray(new String[keys.size()]));
        List<VideoInfo> videoInfoList = videoInfoMapper.selectList(videoInfoQuery);

        Map<String, Integer> videoPlayCountMap = videoInfoList.stream().collect(Collectors.groupingBy(VideoInfo::getUserId,
                Collectors.summingInt(item -> playCountMap.get(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + yesterday + ":" + item.getVideoId()))));

        videoPlayCountMap.forEach((userId, playCount) -> {
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setStatisticsData(yesterday);
            statisticsInfo.setUserId(userId);
            statisticsInfo.setDataType(StatisticsTypeEnum.PLAY.getType());
            statisticsInfo.setStatisticsCount(playCount);
            list.add(statisticsInfo);
        });

        //统计粉丝数
        List<StatisticsInfo> fansCountList = statisticsInfoMapper.selectFansCountByDate(yesterday);
        for (StatisticsInfo statisticsInfo : fansCountList) {
            statisticsInfo.setStatisticsData(yesterday);
            statisticsInfo.setDataType(StatisticsTypeEnum.FANS.getType());
        }
        list.addAll(fansCountList);

        //统计评论数
        List<StatisticsInfo> commentCountList = statisticsInfoMapper.selectCommentsCountByDate(yesterday);
        for (StatisticsInfo statisticsInfo : commentCountList) {
            statisticsInfo.setStatisticsData(yesterday);
            statisticsInfo.setDataType(StatisticsTypeEnum.COMMENT.getType());
        }
        list.addAll(commentCountList);

        //统计弹幕数
        List<StatisticsInfo> danmuCountList = statisticsInfoMapper.selectDanmuCountByDate(yesterday);
        for (StatisticsInfo statisticsInfo : danmuCountList) {
            statisticsInfo.setStatisticsData(yesterday);
            statisticsInfo.setDataType(StatisticsTypeEnum.DANMU.getType());
        }
        list.addAll(danmuCountList);

        //统计点赞，收藏，投币数
        List<StatisticsInfo> otherCountList = statisticsInfoMapper.selectStatisticsInfoByDate(yesterday,
                new Integer[]{StatisticsTypeEnum.LIKE.getType(), StatisticsTypeEnum.COLLECTION.getType(),
                        StatisticsTypeEnum.COIN.getType()});
        for (StatisticsInfo statisticsInfo : otherCountList) {
            if (statisticsInfo.getDataType() == StatisticsTypeEnum.COIN.getType()) {
                statisticsInfo.setStatisticsCount(statisticsInfo.getStatisticsCount() * statisticsInfo.getActionCount());
            }
            statisticsInfo.setStatisticsData(yesterday);
        }
        list.addAll(otherCountList);
        statisticsInfoMapper.insertOrUpdateBatch(list);
    }

    @Override
    public Map<String, Integer> getActualTimeStatisticsInfo(String userId) {
        Map<String, Integer> result = statisticsInfoMapper.selectTotalCount(userId);

        if (!StringTools.isEmpty(userId)) {
            result.put("fansCount", userFocusMapper.selectFansCount(userId));
        } else {
            result.put("fansCount", userInfoMapper.selectCount(new UserInfoQuery()));
        }
        return result;
    }

    @Override
    public List<StatisticsInfo> getStatisticsInfoByQuery(StatisticsInfoQuery query) {
        return statisticsInfoMapper.selectStatisticsTotalInfoByQuery(query);
    }

    @Override
    public List<StatisticsInfo> getUserTotalInfo(StatisticsInfoQuery query) {
        return statisticsInfoMapper.selectUserTotalInfo(query);
    }
}