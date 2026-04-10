package com.easylive.service.impl;

import java.util.List;

import com.easylive.component.EsSearchComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.VideoDanmuQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.enums.SearchOrderTypeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.VideoDanmuService;
import com.easylive.mappers.VideoDanmuMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: video_danmu ServiceImpl
 * @date: 2025-03-07
 */
@Service("videoDanmuService")
public class VideoDanmuServiceImpl implements VideoDanmuService {

    @Resource
    private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 根据条件查询列表
     */
    public List<VideoDanmu> findListByParam(VideoDanmuQuery query) {
        return videoDanmuMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(VideoDanmuQuery query) {
        return videoDanmuMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<VideoDanmu> findListByPage(VideoDanmuQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<VideoDanmu> list = this.findListByParam(query);
        PaginationResultVo<VideoDanmu> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(VideoDanmu bean) {
        return videoDanmuMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<VideoDanmu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoDanmuMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<VideoDanmu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoDanmuMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据DanmuId查询
     */
    public VideoDanmu getVideoDanmuByDanmuId(Integer danmuId) {
        return videoDanmuMapper.selectByDanmuId(danmuId);
    }

    /**
     * 根据DanmuId更新
     */
    public Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId) {
        return videoDanmuMapper.updateByDanmuId(bean, danmuId);
    }

    /**
     * 根据DanmuId删除
     */
    public Integer deleteVideoDanmuByDanmuId(Integer danmuId) {
        return videoDanmuMapper.deleteByDanmuId(danmuId);
    }

    @Override
    public void saveVideoDanmu(VideoDanmu videoDanmu) {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoDanmu.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            throw new BusinessException("UP主已关闭弹幕功能");
        }
        videoDanmuMapper.insert(videoDanmu);

        videoInfoMapper.updateCountInfo(videoDanmu.getVideoId(), UserActionTypeEnum.VIDEO_DANMU.getField(), Constants.ONE);

        //更新es弹幕
        esSearchComponent.updateDocCount(videoDanmu.getVideoId(), SearchOrderTypeEnum.VIDEO_DANMU.getField(), Constants.ONE);
    }

    @Override
    public void deleteDanmu(String userId, Integer danmuId) {
        VideoDanmu videoDanmu = videoDanmuMapper.selectByDanmuId(danmuId);
        if (videoDanmu == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoDanmu.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        if (userId != null && !userId.equals(videoDanmu.getUserId())) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        videoDanmuMapper.deleteByDanmuId(danmuId);
        //更新es弹幕
        esSearchComponent.updateDocCount(videoDanmu.getVideoId(), SearchOrderTypeEnum.VIDEO_DANMU.getField(), -Constants.ONE);
    }
}