package com.easylive.web.task;

import com.easylive.component.EsSearchComponent;
import com.easylive.component.RedisComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.VideoPlayInfoDto;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.enums.SearchOrderTypeEnum;
import com.easylive.service.VideoInfoPostService;
import com.easylive.service.VideoInfoService;
import com.easylive.service.VideoPlayHistoryService;
import com.easylive.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@Component
@Slf4j
public class ExecuteQueueTask {

    private ExecutorService executorService = Executors.newFixedThreadPool(Constants.TWO);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @PostConstruct
    public void consumTransferFileQueue() {
        // 提交第1个消费任务
        executorService.execute(this::consumeTransferQueue);
        // 提交第2个消费任务（利用线程池的第2个线程）
        executorService.execute(this::consumeTransferQueue);
    }

    // 抽取消费逻辑为独立方法，方便复用
    private void consumeTransferQueue() {
        while (true) {
            try {
                VideoInfoFilePost file = redisComponent.getFileFromTransferQueue();
                if (file == null) {
                    TimeUnit.SECONDS.sleep(2);
                    continue;
                }
                videoInfoPostService.transferVideoFile(file);
            } catch (Exception e) {
                log.error("转码队列消费失败", e);
            }
        }
    }

    @PostConstruct
    public void consumVideoPlayInfoQueue() {
        executorService.execute(() -> {
            while (true) {
                try {
                    VideoPlayInfoDto videoPlayInfoDto = redisComponent.getVideoPlayInfoQueue();
                    if (videoPlayInfoDto == null) {
                        TimeUnit.SECONDS.sleep(2);
                        continue;
                    }
                    videoInfoService.addReadCount(videoPlayInfoDto.getVideoId());
                    if (!StringTools.isEmpty(videoPlayInfoDto.getUserId())) {
                        videoPlayHistoryService.saveVideoPlayHistory(videoPlayInfoDto.getUserId(), videoPlayInfoDto.getVideoId(), videoPlayInfoDto.getFileIndex());
                    }
                    //按天记录播放数
                    redisComponent.recordPlayCount(videoPlayInfoDto.getVideoId());

                    //更新es播放数量
                    esSearchComponent.updateDocCount(videoPlayInfoDto.getVideoId(), SearchOrderTypeEnum.VIDEO_PLAY.getField(), 1);
                } catch (Exception e) {
                    log.error("获取转码文件传输队列失败", e);
                }
            }
        });
    }
}
