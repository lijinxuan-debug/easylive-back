package com.easylive.web.controller;

import com.easylive.component.RedisComponent;
import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.SysSettingDto;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.dto.UploadingFileDto;
import com.easylive.entity.dto.VideoPlayInfoDto;
import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.DateTimePatternEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.VideoInfoFileService;
import com.easylive.utils.DateUtils;
import com.easylive.utils.FFmpegUtils;
import com.easylive.utils.StringTools;
import com.easylive.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@RestController
@RequestMapping("/file")
@Validated
@Slf4j
public class FileController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private FFmpegUtils ffmpegUtils;

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @RequestMapping("/getImage")
    public void getImage(HttpServletResponse response, @NotNull String sourceName) {
        if (!StringTools.pathIsOk(sourceName)) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        String suffix = StringTools.getFileSuffix(sourceName);
        response.setContentType("image/" + suffix.replace(".", ""));
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, sourceName);
    }

    protected void readFile(HttpServletResponse response, String filePath) {
        HttpServletRequest request = null;
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) attrs).getRequest();
        }
        readFile(request, response, filePath);
    }

    protected void readFile(HttpServletRequest request, HttpServletResponse response, String filePath) {
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + filePath);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long fileLength = file.length();
        long start = 0;
        long end = fileLength - 1;
        String range = request != null ? request.getHeader("Range") : null;
        if (range != null && range.startsWith("bytes=")) {
            String[] parts = range.substring(6).split("-", 2);
            try {
                start = Long.parseLong(parts[0].trim());
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    end = Long.parseLong(parts[1].trim());
                }
            } catch (NumberFormatException ignored) {
                start = 0;
                end = fileLength - 1;
            }
            if (end >= fileLength) {
                end = fileLength - 1;
            }
            if (start > end || start < 0) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + fileLength);
                return;
            }
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        }
        long contentLength = end - start + 1;
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentLengthLong(contentLength);
        try (OutputStream outputStream = response.getOutputStream();
             FileInputStream inputStream = new FileInputStream(file)) {
            long skipped = inputStream.skip(start);
            if (skipped < start) {
                return;
            }
            byte[] buffer = new byte[8192];
            long remaining = contentLength;
            while (remaining > 0) {
                int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) {
                    break;
                }
                outputStream.write(buffer, 0, read);
                remaining -= read;
            }
            outputStream.flush();
        } catch (IOException e) {
            if (isClientAbort(e)) {
                log.debug("客户端中断下载(正常): {}", filePath);
            } else {
                log.error("读取文件失败: {}", filePath, e);
            }
        }
    }

    private boolean isClientAbort(Throwable e) {
        while (e != null) {
            if (e instanceof ClientAbortException) {
                return true;
            }
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Broken pipe") || msg.contains("Connection reset"))) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    //文件预上传
    @RequestMapping("/preUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo preUploadVideo(@NotEmpty String fileName, @NotNull Integer chunks) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        String uploadId = redisComponent.saveVideoFileInfo(tokenUserInfoDto.getUserId(), fileName, chunks);
        return getSuccessResponseVo(uploadId);
    }

    //文件上传
    @RequestMapping("/uploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo uploadVideo(@NotNull MultipartFile chunkFile, @NotNull Integer chunkIndex, @NotEmpty String uploadId) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        UploadingFileDto videoFileInfo = redisComponent.getVideoFileInfo(tokenUserInfoDto.getUserId(), uploadId);
        if (videoFileInfo == null) {
            throw new BusinessException("文件不存在,请重新上传");
        }
        //限制大小
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        if (videoFileInfo.getFileSize() > sysSettingDto.getVideoSize() * Constants.MB_SIZE) {
            throw new BusinessException("文件大小超出限制");
        }
        //判断分片
        if ((chunkIndex - 1) > videoFileInfo.getChunkIndex() || chunkIndex > videoFileInfo.getChunks() - 1) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + videoFileInfo.getFilePath();
        File targetFile = new File(folder + "/" + chunkIndex);
        chunkFile.transferTo(targetFile);
        videoFileInfo.setChunkIndex(chunkIndex);
        videoFileInfo.setFileSize(videoFileInfo.getFileSize() + chunkFile.getSize());
        redisComponent.updateVideoFileInfo(tokenUserInfoDto.getUserId(), videoFileInfo);
        return getSuccessResponseVo(null);
    }

    //未提交前删除
    @RequestMapping("/delUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVo delUploadVideo(@NotEmpty String uploadId) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        UploadingFileDto videoFileInfo = redisComponent.getVideoFileInfo(tokenUserInfoDto.getUserId(), uploadId);
        if (videoFileInfo == null) {
            throw new BusinessException("文件不存在");
        }
        redisComponent.deleteVideoFileInfo(tokenUserInfoDto.getUserId(), uploadId);
        FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + videoFileInfo.getFilePath()));
        return getSuccessResponseVo(uploadId);
    }

    //上传图片
    @RequestMapping("/uploadImage")
    public ResponseVo uploadImage(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws IOException {
        String day = DateUtils.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_COVER + day;
        File fileFolder = new File(folder);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        String suffix = StringTools.getFileSuffix(fileName);
        String realFileName = StringTools.getRandomLetters(Constants.LENGTH_30) + suffix;
        String realFilePath = folder + "/" + realFileName;
        File realFile = new File(realFilePath);
        file.transferTo(realFile);
        if (createThumbnail) {
            ffmpegUtils.createThumbnail(realFilePath);
        }
        return getSuccessResponseVo(Constants.FILE_COVER + day + "/" + realFileName);
    }

    @RequestMapping("/videoResource/{fileId}")
    public void videoResource(HttpServletResponse response, @PathVariable("fileId") @NotEmpty String fileId) {
        log.debug("开始处理视频播放请求，fileId: {}", fileId);
        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        if (videoInfoFile == null) {
            log.error("视频文件不存在，fileId: {}", fileId);
            return;
        }
        log.debug("找到视频文件，videoId: {}, filePath: {}", videoInfoFile.getVideoId(), videoInfoFile.getFilePath());
        String filePath = videoInfoFile.getFilePath();
        readFile(response, filePath + "/" + Constants.M3U8_NAME);
        enqueueVideoPlayInfo(videoInfoFile);
    }

    @RequestMapping("/videoResource/{fileId}/{ts}")
    public void videoResourceTs(HttpServletResponse response, @PathVariable("fileId") @NotEmpty String fileId, @PathVariable("ts") @NotEmpty String ts) {
        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        if (videoInfoFile == null) {
            return;
        }
        String filePath = videoInfoFile.getFilePath();
        // Android/Exo 等客户端拉取 index.m3u8 走此接口，需同样计入播放量
        if (Constants.M3U8_NAME.equals(ts)) {
            enqueueVideoPlayInfo(videoInfoFile);
        }
        if (ts.endsWith(".mp4")) {
            response.setContentType("video/mp4");
            response.setHeader("Cache-Control", "max-age=3600");
        }
        readFile(response, filePath + "/" + ts);
    }

    /** 播放量：写入 Redis 队列，由 ExecuteQueueTask 异步 addReadCount */
    private void enqueueVideoPlayInfo(VideoInfoFile videoInfoFile) {
        log.debug("准备保存播放信息，videoId: {}", videoInfoFile.getVideoId());
        VideoPlayInfoDto videoPlayInfoDto = new VideoPlayInfoDto();
        videoPlayInfoDto.setVideoId(videoInfoFile.getVideoId());
        videoPlayInfoDto.setFileIndex(videoInfoFile.getFileIndex());
        TokenUserInfoDto userInfoDto = getTokenUserInfoFromCookie();
        if (userInfoDto != null) {
            videoPlayInfoDto.setUserId(userInfoDto.getUserId());
        }
        redisComponent.saveVideoPlayInfo(videoPlayInfoDto);
        log.debug("播放信息已保存到Redis队列，videoId: {}", videoInfoFile.getVideoId());
    }

    @RequestMapping("/videoPreview/{fileId}")
    public void videoPreview(HttpServletResponse response, @PathVariable("fileId") @NotEmpty String fileId) {
        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        if (videoInfoFile == null) {
            return;
        }
        String filePath = videoInfoFile.getFilePath();
        response.setContentType("image/jpeg");
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, filePath + "/" + Constants.VIDEO_PREVIEW_NAME);
    }
}
