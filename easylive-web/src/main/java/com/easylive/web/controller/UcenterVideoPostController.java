package com.easylive.web.controller;

import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.VideoInfoFilePostQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.entity.vo.VideoPostEditInfoVo;
import com.easylive.entity.vo.VideoStatusCountInfoVo;
import com.easylive.enums.ResponseEnum;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoInfoFilePostService;
import com.easylive.service.VideoInfoPostService;
import com.easylive.service.VideoInfoService;
import com.easylive.utils.JsonUtils;
import com.easylive.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@RestController
@RequestMapping("/ucenter/post")
@Validated
@Slf4j
public class UcenterVideoPostController extends ABaseController {

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    // 新增、更新
    @RequestMapping("/postVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo getImage(String videoId,
                               @NotEmpty String videoCover,
                               @NotEmpty @Size(max = 100) String videoName,
                               @NotNull Integer pCategoryId,
                               Integer categoryId,
                               @NotNull Integer postType,
                               @NotEmpty @Size(max = 300) String tags,
                               @Size(max = 2000) String introduction,
                               @Size(max = 3) String interaction,
                               @NotEmpty String uploadFileList) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        //视频文件列表
        List<VideoInfoFilePost> videoInfoFilePostList = JsonUtils.convertJsonArray2List(uploadFileList, VideoInfoFilePost.class);

        //视频基本信息
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setVideoId(videoId);
        videoInfoPost.setVideoCover(videoCover);
        videoInfoPost.setVideoName(videoName);
        videoInfoPost.setPCategoryId(pCategoryId);
        videoInfoPost.setCategoryId(categoryId);
        videoInfoPost.setPostType(postType);
        videoInfoPost.setTags(tags);
        videoInfoPost.setIntroduction(introduction);
        videoInfoPost.setInteraction(interaction);
        videoInfoPost.setUserId(tokenUserInfoDto.getUserId());

        videoInfoPostService.saveVideoInfo(videoInfoPost, videoInfoFilePostList);

        return getSuccessResponseVo(null);
    }

    // 查询不同状态下的视频列表，例如未审核、已审核等等
    @RequestMapping("/loadVideoList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo loadVideoList(Integer status, Integer pageNo, String videoNameFuzzy) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();

        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setOrderBy("v.create_time desc");
        videoInfoPostQuery.setPageNo(pageNo);
        if (status != null) {
            if (status == -1) {// 进行中
                videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(), VideoStatusEnum.STATUS4.getStatus()});
            } else {
                videoInfoPostQuery.setStatus(status);
            }
        }
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoPostQuery.setCountInfo(true);
        PaginationResultVo paginationResultVo = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return getSuccessResponseVo(paginationResultVo);
    }

    // 获取当前用户下不同状态的视频数量
    @RequestMapping("/getVideoCountInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo getVideoCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS3.getStatus());
        Integer auditSuccessCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS4.getStatus());
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        videoInfoPostQuery.setStatus(null);
        videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(), VideoStatusEnum.STATUS4.getStatus()});
        Integer inProcessCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        VideoStatusCountInfoVo videoStatusCountInfoVo = new VideoStatusCountInfoVo();
        videoStatusCountInfoVo.setAuditSuccessCount(auditSuccessCount);
        videoStatusCountInfoVo.setAuditFailCount(auditFailCount);
        videoStatusCountInfoVo.setInProcessCount(inProcessCount);
        return getSuccessResponseVo(videoStatusCountInfoVo);
    }

    /**
     *
     * @param videoId
     * @return
     */
    @RequestMapping("/getVideoInfoByVideoId")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo getVideoInfoByVideoId(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        VideoInfoPost videoInfoPost = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null || !videoInfoPost.getUserId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseEnum.CODE_404);
        }

        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("v.file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);

        VideoPostEditInfoVo videoPostEditInfoVo = new VideoPostEditInfoVo();
        videoPostEditInfoVo.setVideoInfo(videoInfoPost);
        videoPostEditInfoVo.setVideoInfoFileList(videoInfoFilePostList);
        return getSuccessResponseVo(videoPostEditInfoVo);
    }

    @RequestMapping("/saveVideoInteraction")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo saveVideoInteraction(@NotEmpty String videoId, String interaction) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        videoInfoService.updateVideoInteraction(videoId, interaction, tokenUserInfoDto.getUserId());
        return getSuccessResponseVo(null);
    }

    @RequestMapping("/delVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo delVideo(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        videoInfoService.delVideo(videoId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVo(null);
    }
}
