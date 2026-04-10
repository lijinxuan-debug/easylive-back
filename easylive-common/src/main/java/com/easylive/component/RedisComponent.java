package com.easylive.component;

import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.SysSettingDto;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.UploadingFileDto;
import com.easylive.entity.dto.VideoPlayInfoDto;
import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.enums.DateTimePatternEnum;
import com.easylive.redis.RedisUtils;
import com.easylive.utils.DateUtils;
import com.easylive.utils.StringTools;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@Component
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private AppConfig appConfig;

    public String saveCheckCode(String checkCode) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, checkCode, Constants.REDIS_KEY_EXPIRE_ONE_MINUTE * 10);
        return checkCodeKey;
    }

    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void saveTokenInfo4Web(TokenUserInfoDto tokenUserInfoDto) {
        String token = UUID.randomUUID().toString();
        tokenUserInfoDto.setExpireTime(System.currentTimeMillis() + Constants.REDIS_KEY_EXPIRE_ONE_DAY * 7);
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRE_ONE_DAY * 7);
    }

    public String saveTokenInfo4Admin(String account) {
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN + token, account, Constants.REDIS_KEY_EXPIRE_ONE_DAY);
        return token;
    }

    public void updateTokenInfo(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRE_ONE_DAY * 7);
    }

    public TokenUserInfoDto getTokenInfo4Web(String token) {
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    public void cleanTokenInfo4Web(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    public String getTokenInfo4Admin(String token) {
        return (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    public void cleanTokenInfo4Admin(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    public void saveCategoryInfo(List<CategoryInfo> categoryInfoList) {
        redisUtils.set(Constants.REDIS_KEY_CATEGORY_LIST, categoryInfoList);
    }

    public List<CategoryInfo> getCategoryInfo() {
        return (List<CategoryInfo>) redisUtils.get(Constants.REDIS_KEY_CATEGORY_LIST);
    }

    //保存上传文件信息
    public String saveVideoFileInfo(String userId, String FileName, Integer chunks) {
        String uploadId = StringTools.getRandomLetters(Constants.LENGTH_10);
        UploadingFileDto uploadingFileDto = new UploadingFileDto();
        uploadingFileDto.setFileName(FileName);
        uploadingFileDto.setChunks(chunks);
        uploadingFileDto.setChunkIndex(0);
        uploadingFileDto.setUploadId(uploadId);
        String day = DateUtils.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        String filePath = day + "/" + userId + uploadId;
        String fileFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + filePath;
        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        uploadingFileDto.setFilePath(filePath);
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId, uploadingFileDto, Constants.REDIS_KEY_EXPIRE_ONE_DAY);
        return uploadId;
    }

    //获取上传文件信息
    public UploadingFileDto getVideoFileInfo(String UserId, String uploadId) {
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE + UserId + uploadId);
    }

    //更新上传文件信息
    public void updateVideoFileInfo(String userId, UploadingFileDto uploadingFileDto) {
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadingFileDto.getUploadId(), uploadingFileDto, Constants.REDIS_KEY_EXPIRE_ONE_DAY);
    }

    //获取系统设置信息
    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDto == null) {
            sysSettingDto = new SysSettingDto();
        }
        return sysSettingDto;
    }

    public void saveSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }


    //删除上传文件信息
    public void deleteVideoFileInfo(String userId, String uploadId) {
        redisUtils.delete(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    //添加文件到删除队列
    public void addFile2DelQueue(String videoId, List<String> deleteFilePathList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_DEL + videoId, deleteFilePathList, Constants.REDIS_KEY_EXPIRE_ONE_DAY * 7);
    }

    //获取删除队列文件路径
    public List<String> getFileFormDelQueue(String videoId) {
        return redisUtils.getQueueList(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    //清空删除队列
    public void cleanDelQueue(String videoId) {
        redisUtils.delete(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    //添加文件到转码队列
    public void addFile2TransferQueue(List<VideoInfoFilePost> addList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_QUEUE_TRANSFER, addList, 0);
    }

    //获取转码队列文件
    public VideoInfoFilePost getFileFromTransferQueue() {
        return (VideoInfoFilePost) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_TRANSFER);
    }

    //视频在线观看人数 前端每隔5秒请求一次
    public Integer VideoPlayOnline(String fileId, String deviceId) {
        // 当前用户
        String userPlayOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER, fileId, deviceId);
        // 当前文件观看总人数
        String playOnlineCountKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId);

        // 当前用户首次打开该文件
        if (!redisUtils.keyExists(userPlayOnlineKey)) {
            redisUtils.setex(userPlayOnlineKey, fileId, Constants.REDIS_KEY_EXPIRE_ONE_SECOND * 8);
            return redisUtils.incrementex(playOnlineCountKey, Constants.REDIS_KEY_EXPIRE_ONE_SECOND * 10).intValue();
        }
        // 续期
        redisUtils.expire(playOnlineCountKey, Constants.REDIS_KEY_EXPIRE_ONE_SECOND * 10);
        redisUtils.expire(userPlayOnlineKey, Constants.REDIS_KEY_EXPIRE_ONE_SECOND * 8);
        Integer count = (Integer) redisUtils.get(playOnlineCountKey);

        return count == null ? 1 : count;
    }

    //减少视频在线观看人数
    public void decrVideoPlayOnline(String key) {
        redisUtils.decrement(key);
    }

    //记录搜索热词
    public void recordSearchHotWord(String word) {
        redisUtils.zAddCount(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, word);
    }

    //获取搜索热词
    public List<String> getSearchHotWord(Integer top) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, top - 1);
    }

    //保存视频播放信息
    public void saveVideoPlayInfo(VideoPlayInfoDto videoPlayInfoDto) {
        redisUtils.lpush(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY, videoPlayInfoDto, null);
    }

    //获取视频播放信息队列
    public VideoPlayInfoDto getVideoPlayInfoQueue() {
        return (VideoPlayInfoDto) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY);
    }

    //按天记录视频播放次数
    public void recordPlayCount(String videoId) {
        String date = DateUtils.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        redisUtils.incrementex(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date + ":" + videoId, Constants.REDIS_KEY_EXPIRE_ONE_DAY * 2);
    }

    //获取视频播放次数
    public Map<String, Integer> getPlayCount(String date) {
        Map<String, Integer> playCountMap = redisUtils.getBatch(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date);
        return playCountMap;
    }
}
