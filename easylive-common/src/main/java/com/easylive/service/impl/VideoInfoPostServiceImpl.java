package com.easylive.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.easylive.component.EsSearchComponent;
import com.easylive.component.RedisComponent;
import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.SysSettingDto;
import com.easylive.entity.dto.UploadingFileDto;
import com.easylive.entity.po.*;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.enums.*;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.*;
import com.easylive.service.VideoInfoPostService;
import com.easylive.utils.CopyTools;
import com.easylive.utils.FFmpegUtils;
import com.easylive.utils.StringTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @description: video_info_post ServiceImpl
 * @date: 2025-03-06
 */
@Service("videoInfoPostService")
public class VideoInfoPostServiceImpl implements VideoInfoPostService {

    private static final Logger log = LoggerFactory.getLogger(VideoInfoPostServiceImpl.class);
    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

    @Resource
    private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private FFmpegUtils ffmpegUtils;

    @Resource
    private AppConfig appConfig;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 根据条件查询列表
     */
    public List<VideoInfoPost> findListByParam(VideoInfoPostQuery query) {
        return videoInfoPostMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(VideoInfoPostQuery query) {
        return videoInfoPostMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<VideoInfoPost> findListByPage(VideoInfoPostQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<VideoInfoPost> list = this.findListByParam(query);
        PaginationResultVo<VideoInfoPost> result = new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
        return result;
    }

    /**
     * 新增
     */
    public Integer add(VideoInfoPost bean) {
        return videoInfoPostMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<VideoInfoPost> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoInfoPostMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/更新
     */
    public Integer addOrUpdateBatch(List<VideoInfoPost> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoInfoPostMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据VideoId查询
     */
    public VideoInfoPost getVideoInfoPostByVideoId(String videoId) {
        return videoInfoPostMapper.selectByVideoId(videoId);
    }

    /**
     * 根据VideoId更新
     */
    public Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId) {
        return videoInfoPostMapper.updateByVideoId(bean, videoId);
    }

    /**
     * 根据VideoId删除
     */
    public Integer deleteVideoInfoPostByVideoId(String videoId) {
        return videoInfoPostMapper.deleteByVideoId(videoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)//新增就是新增，更新包括（删除旧视频，上传新视频，更改视频信息）
    public void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> uploadFileList) {
        // 1.上传文件数量限制
        if (uploadFileList.size() > redisComponent.getSysSetting().getVideoPCount()) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        // 视频Id
        String videoId = videoInfoPost.getVideoId();

        // 2.是否存在videoId，不存在或者转码中、待审核状态都不可更新
        if (!StringTools.isEmpty(videoId)) {
            VideoInfoPost videoInfoPostDb = videoInfoPostMapper.selectByVideoId(videoId);
            if (videoInfoPostDb == null) {
                throw new BusinessException(ResponseEnum.CODE_600);
            }
            // 转码中、待审核 这两种状态不允许更新
            if (ArrayUtils.contains(new Integer[]{VideoStatusEnum.STATUS0.getStatus(), VideoStatusEnum.STATUS2.getStatus()},
                    videoInfoPostDb.getStatus())) {
                throw new BusinessException(ResponseEnum.CODE_600);
            }
        }

        Date curDate = new Date();

        // 此次更新操作中删除的视频文件信息
        List<VideoInfoFilePost> deleteList = new ArrayList<>();
        // 此次更新操作中新增的视频文件信息
        List<VideoInfoFilePost> addList = uploadFileList;

        // 3.videoId为空则是新增操作
        if (StringTools.isEmpty(videoId)) {
            videoId = StringTools.getRandomLetters(Constants.LENGTH_10);
            videoInfoPost.setVideoId(videoId);
            videoInfoPost.setCreateTime(curDate);
            videoInfoPost.setLastUpdateTime(curDate);
            videoInfoPost.setStatus(VideoStatusEnum.STATUS0.getStatus());
            videoInfoPostMapper.insert(videoInfoPost);
        } else {
            // 4.更新操作，那就说明数据库中存在视频，与传来视频进行比较，可能出现删除、新增了一些视频，或者视频文件名以及相关文件信息等发生了更改。
            VideoInfoFilePostQuery videoInfoPostQuery = new VideoInfoFilePostQuery();
            videoInfoPostQuery.setVideoId(videoId);
            videoInfoPostQuery.setUserId(videoInfoPost.getUserId());
            // 查询数据库中已存在的视频文件信息
            List<VideoInfoFilePost> dbVideoInfoFilePostList = videoInfoFilePostMapper.selectList(videoInfoPostQuery);
            // 将此次请求中的传来的视频文件信息转换为以uploadId为key的Map
            Map<String, VideoInfoFilePost> uploadFileMap = uploadFileList.stream().
                    collect(Collectors.toMap(item -> item.getUploadId(), Function.identity(),
                            (date1, date2) -> date2));

            Boolean updateFileName = false;

            // 对比数据库中已存在的视频文件信息和传来的视频文件信息
            for (VideoInfoFilePost videoInfoFilePost : dbVideoInfoFilePostList) {
                VideoInfoFilePost updateFile = uploadFileMap.get(videoInfoFilePost.getUploadId());
                if (updateFile == null) { // 传来的视频文件信息中不存在此uploadId，说明此文件已删除
                    deleteList.add(videoInfoFilePost);// 数据库中已存在的视频文件信息加入待删除列表
                } else if (!updateFile.getFileName().equals(videoInfoFilePost.getFileName())) { // 传来的视频文件信息中存在此uploadId，但文件名有变化
                    updateFileName = true;
                }
            }

            // 此次更新操作中新增视频文件信息
            addList = uploadFileList.stream().filter(item -> item.getFileId() == null).collect(Collectors.toList());
            videoInfoPost.setLastUpdateTime(curDate);

            // 检查此次操作基本信息是否有更新  方便审核
            Boolean changeVideoInfo = changeVideoInfo(videoInfoPost);

            if (addList != null && !addList.isEmpty()) {//新增视频文件信息
                videoInfoPost.setStatus(VideoStatusEnum.STATUS0.getStatus());
            } else if (changeVideoInfo || updateFileName) {//更新视频信息
                videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
            }
            videoInfoPostMapper.updateByVideoId(videoInfoPost, videoId);
        }

        // 5.删除视频文件信息，数据库中直接删除即可，路径则存储在redis的List集合中
        if (!deleteList.isEmpty()) {
            // 删除数据库中的视频文件信息
            List<String> deleteFileIdList = deleteList.stream().map(item -> item.getFileId()).collect(Collectors.toList());
            videoInfoFilePostMapper.deleteBatchByFileId(deleteFileIdList, videoInfoPost.getUserId());

            // 要删除文件的路径存储在redis消息队列中，异步删除
            List<String> deleteFilePathList = deleteList.stream().map(item -> item.getFilePath()).collect(Collectors.toList());
            redisComponent.addFile2DelQueue(videoId, deleteFilePathList);
        }

        // 6.对视频文件信息进行处理，例如文件下标顺序、用户ID等，如果是新增视频还需要设置文件审核状态和转码状态等。
        int index = 1;
        for (VideoInfoFilePost videoInfoFilePost : uploadFileList) {
            videoInfoFilePost.setFileIndex(index++);
            videoInfoFilePost.setVideoId(videoId);
            videoInfoFilePost.setUserId(videoInfoPost.getUserId());
            if (videoInfoFilePost.getFileId() == null) {// 新增
                videoInfoFilePost.setFileId(StringTools.getRandomLetters(Constants.LENGTH_20));
                videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.UPDATE.getStatus());
                videoInfoFilePost.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
            }
        }
        // 6.1 批量插入或修改数据库
        videoInfoFilePostMapper.insertOrUpdateBatch(uploadFileList);

        // 6.2 此次文件中新增视频信息设置用户ID和视频ID，添加到转码队列（）也是redis的List结构
        if (addList != null && !addList.isEmpty()) {
            for (VideoInfoFilePost videoInfoFilePost : addList) {
                videoInfoFilePost.setUserId(videoInfoPost.getUserId());
                videoInfoFilePost.setVideoId(videoId);
            }
            redisComponent.addFile2TransferQueue(addList);
        }
    }

    private Boolean changeVideoInfo(VideoInfoPost videoInfoPost) {
        VideoInfoPost dbInfo = this.videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
        // 标题, 封面, 标签, 简介是否有变化
        if (!videoInfoPost.getVideoName().equals(dbInfo.getVideoName())
                || !videoInfoPost.getVideoCover().equals(dbInfo.getVideoCover())
                || !videoInfoPost.getTags().equals(dbInfo.getTags())
                || !videoInfoPost.getIntroduction().equals(dbInfo.getIntroduction() == null ? "" : dbInfo.getIntroduction())) {
            return true;
        } else {
            return false;
        }
    }

    //转码视频文件
    @Override
    public void transferVideoFile(VideoInfoFilePost videoInfoFilePost) {
        VideoInfoFilePost updateFilePost = new VideoInfoFilePost();
        try {
            UploadingFileDto videoFileInfo = redisComponent.getVideoFileInfo(videoInfoFilePost.getUserId(), videoInfoFilePost.getUploadId());

            // 临时目录 存储第一次分片文件
            String tempFilePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + videoFileInfo.getFilePath();
            File tempFile = new File(tempFilePath);

            String targetFilePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_VIDEO + videoFileInfo.getFilePath();
            File targetFile = new File(targetFilePath);

            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }

            FileUtils.copyDirectory(tempFile, targetFile);

            //删除临时文件夹
            FileUtils.deleteDirectory(tempFile);
            redisComponent.deleteVideoFileInfo(videoInfoFilePost.getUserId(), videoInfoFilePost.getUploadId());

            //初次合并成temp.mp4文件 并 删除分片文件
            String completeVideo = targetFilePath + Constants.TEMP_VIDEO_NAME;
            union(targetFilePath, completeVideo, true);

            //根据temp.mp4获取播放时长
            Integer duration = ffmpegUtils.getVideoInfoDuration(completeVideo);
            //填充视频文件信息
            updateFilePost.setDuration(duration);
            updateFilePost.setFilePath(Constants.FILE_VIDEO + videoFileInfo.getFilePath());
            updateFilePost.setFileSize(new File(completeVideo).length());
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.SUCCESS.getStatus());

            convertVideo2Ts(completeVideo);
        } catch (Exception e) {
            log.error("文件转码失败", e);
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
        } finally {
            // 更新视频文件状态
            videoInfoFilePostMapper.updateByUploadIdAndUserId(updateFilePost, videoInfoFilePost.getUploadId(), videoInfoFilePost.getUserId());

            VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
            videoInfoFilePostQuery.setVideoId(videoInfoFilePost.getVideoId());
            videoInfoFilePostQuery.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
            Integer failCount = videoInfoFilePostMapper.selectCount(videoInfoFilePostQuery);
            if (failCount > 0) { //如果有 文件 转码失败，则更新视频状态为转码失败
                VideoInfoPost videoInfoPost = new VideoInfoPost();
                videoInfoPost.setStatus(VideoStatusEnum.STATUS1.getStatus());
                videoInfoPostMapper.updateByVideoId(videoInfoPost, videoInfoFilePost.getVideoId());
                return;
            }
            //如果所有文件都转码成功，则更新视频状态为待审核
            videoInfoFilePostQuery.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
            Integer transferCount = videoInfoFilePostMapper.selectCount(videoInfoFilePostQuery);
            if (transferCount == 0) {
                Integer duration = videoInfoFilePostMapper.sumDuration(videoInfoFilePost.getVideoId());
                VideoInfoPost videoInfoPost = new VideoInfoPost();
                videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
                videoInfoPost.setDuration(duration);
                videoInfoPostMapper.updateByVideoId(videoInfoPost, videoInfoFilePost.getVideoId());
            }
        }
    }

    // 我们只要h264（mp4），不要hevc
    private Integer convertVideo2Ts(String completeVideo) {
        File videoFile = new File(completeVideo);
        File tsFolder = videoFile.getParentFile();
        String codec = ffmpegUtils.getVideoCodec(completeVideo);
        if (Constants.VIDEO_CODE_HEVC.equals(codec)) {
            String tempFileName = completeVideo + Constants.VIDEO_CODE_TEMP_FILE_SUFFIX;
            new File(completeVideo).renameTo(new File(tempFileName));
            ffmpegUtils.convertHevc2Mp4(tempFileName, completeVideo);
            new File(tempFileName).delete();
        }
        Integer duration = ffmpegUtils.convertVideo2TsAndGetDuration(tsFolder, completeVideo);
        ffmpegUtils.createVideoVtt(completeVideo, tsFolder.getPath());
        videoFile.delete();
        return duration;
    }

    // 分片->.mp4文件 合并分片
    private void union(String dirPath, String toFilePath, Boolean delSource) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }

        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        try (RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw")) {
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                // 创建读取文件的对象
                File chunkFile = new File(dirPath + File.separator + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new BusinessException("合并文件失败");
                } finally {
                    if (readFile != null) {
                        readFile.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException("合并文件" + dirPath + " 出错了");
        } finally {
            if (delSource) {
                for (int i = 0; i < fileList.length; i++) {
                    fileList[i].delete();
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditVideo(String videoId, Integer status, String reason) {
        VideoStatusEnum videoStatusEnum = VideoStatusEnum.getByStatus(status);
        if (videoStatusEnum == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setStatus(status);

        //避免同一时间有多个审核人审核同一个视频---乐观锁（update video_info_post set status = #{query.status} where videoId = #{query.videoId} and status = #{bean.status}）
        //修改视频状态
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS2.getStatus());
        Integer count = videoInfoPostMapper.updateByQuery(videoInfoPost, videoInfoPostQuery);
        if (count == 0) {
            throw new BusinessException("审核失败，请刷新后重试");
        }

        //修改视频文件状态
        VideoInfoFilePost videoInfoFilePost = new VideoInfoFilePost();
        videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.NO_UPDATE.getStatus());

        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostMapper.updateByQuery(videoInfoFilePost, videoInfoFilePostQuery);

        //审核不通过
        if (VideoStatusEnum.STATUS4.equals(videoStatusEnum)) {
            return;
        }

        VideoInfoPost infoPost = videoInfoPostMapper.selectByVideoId(videoId);

        VideoInfo dbVideoInfo = videoInfoMapper.selectByVideoId(videoId);
        if (dbVideoInfo == null) {
            SysSettingDto sysSetting = redisComponent.getSysSetting();
            userInfoMapper.updateCoinCount(videoInfoPost.getUserId(), sysSetting.getPostVideoCoinCount());
        }

        //更新视频信息到正式表
        VideoInfo videoInfo = CopyTools.copy(infoPost, VideoInfo.class);
        videoInfoMapper.insertOrUpdate(videoInfo);

        //更新视频文件信息到正式表 先删除再插入
        VideoInfoFileQuery infoFileQuery = new VideoInfoFileQuery();
        infoFileQuery.setVideoId(videoId);
        videoInfoFileMapper.deleteByQuery(infoFileQuery);

        VideoInfoFilePostQuery infoFilePostQuery = new VideoInfoFilePostQuery();
        infoFilePostQuery.setVideoId(videoId);
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostMapper.selectList(infoFilePostQuery);

        List<VideoInfoFile> videoInfoFileList = CopyTools.copyList(videoInfoFilePostList, VideoInfoFile.class);
        videoInfoFileMapper.insertBatch(videoInfoFileList);

        //删除文件
        List<String> delQueue = redisComponent.getFileFormDelQueue(videoId);
        if (delQueue != null) {
            for (String filePath : delQueue) {
                File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + filePath);
                if (file.exists()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        log.error("删除文件失败", e);
                    }
                }
            }
        }
        redisComponent.cleanDelQueue(videoId);

        //保存信息到es中
        esSearchComponent.saveDoc(videoInfo);
    }
}