package com.easylive.utils;

import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */
@Slf4j
@Component
public class FFmpegUtils {

    /** HLS 切片目标时长（秒），越小 seek 越准，但 ts 文件更多 */
    private static final int HLS_SEGMENT_TIME_SEC = 4;

    /**
     * 不超过该时长的视频只生成一个 ts 切片，避免 11 秒视频被切成「10 秒 + 1 秒」导致只能 seek 到 0/10 秒
     */
    /** 1 分钟内单切片，减少 HLS 切分导致的缓冲抖动 */
    private static final int HLS_SINGLE_SEGMENT_MAX_DURATION_SEC = 120;

    @Resource
    private AppConfig appConfig;

    // 生成缩略图
    public void createThumbnail(String filePath) {
        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        CMD = String.format(CMD, filePath, filePath + Constants.IMAGE_THUMBNAIL_SUFFIX);
        ProcessUtils.executeCommand(CMD, appConfig.getShowFFmpegLog());
    }

    // 获取视频时长（秒，四舍五入）
    public Integer getVideoInfoDuration(String completeVideo) {
        double seconds = getVideoInfoDurationPrecise(completeVideo);
        if (seconds <= 0) {
            return 0;
        }
        return (int) Math.round(seconds);
    }

    /** 获取视频时长（秒，带小数，用于 m3u8 EXTINF） */
    public double getVideoInfoDurationPrecise(String completeVideo) {
        final String CMD_GET_CODE =
                "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        String cmd = String.format(CMD_GET_CODE, completeVideo);
        String result = ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());

        if (StringTools.isEmpty(result)) {
            return 0;
        }
        result = result.replace("\n", "").trim();
        if (result.isEmpty()) {
            return 0;
        }
        try {
            return new BigDecimal(result).doubleValue();
        } catch (NumberFormatException e) {
            log.error("解析视频时长失败，ffprobe 输出内容: [{}]", result, e);
            return 0;
        }
    }

    // 获取视频编码格式
    public String getVideoCodec(String videoFilePath) {
        final String CMD_GET_CODE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        String cmd = String.format(CMD_GET_CODE, videoFilePath);
        String result = ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        result = result.replace("\n", "");
        result = result.substring(result.indexOf("=") + 1);
        String codec = result.substring(0, result.indexOf("["));
        return codec;
    }

    // 转换视频编码格式
    public void convertHevc2Mp4(String newFileName, String videoFilePath) {
        String CMD_HEVC_264 = "ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y";
        String cmd = String.format(CMD_HEVC_264, newFileName, videoFilePath);
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
    }

    // 创建雪碧图
    public void createVideoVtt(String videoFilePath, String tsFolderPath) {
        Integer duration = getVideoInfoDuration(videoFilePath);
        if (duration <= 0) return;

        // 总张数定为 400，安全又清晰
        double totalImages = 400.0;
        double fps = totalImages / duration;

        // 10列，40行
        String CMD = "ffmpeg -i \"%s\" -vf \"fps=%.6f,scale=160:90,tile=10x40\" -an -vsync vfr \"%s\" -y";

        String cmd = String.format(CMD, videoFilePath, fps, tsFolderPath + "/" + Constants.VIDEO_PREVIEW_NAME);
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
    }

    /**
     * 转 HLS：短视频强制单 ts + 手写 m3u8（避免 -c copy 时在 10s 关键帧处被强行切段）；
     * 长视频按 {@link #HLS_SEGMENT_TIME_SEC} 秒切片。
     */

    /**
     * 是否必须重编码：HEVC、VFR，或带旋转元数据的竖拍片（否则 HLS copy 后横竖屏错乱）。
     */
    public boolean needsNormalizeForStreaming(String videoFilePath) {
        int rotation = getVideoRotationDegrees(videoFilePath);
        if (rotation != 0) {
            log.info("检测到旋转元数据 {}°，需要 normalize 校正画面: {}", rotation, videoFilePath);
            return true;
        }
        String codec = getVideoCodec(videoFilePath);
        if (Constants.VIDEO_CODE_HEVC.equals(codec)) {
            return true;
        }
        return isVariableFrameRate(videoFilePath);
    }

    /**
     * 手机竖拍常见：存储为横屏像素 + rotate=90 元数据；相册能正着播，copy 切片后播放器常忽略该元数据。
     */
    public int getVideoRotationDegrees(String videoFilePath) {
        int fromSideData = probeRotation(
                "ffprobe -v error -select_streams v:0 -show_entries stream_side_data=rotation "
                        + "-of default=nw=1:nk=1 \"%s\"",
                videoFilePath);
        if (fromSideData != 0) {
            return normalizeRotationDegrees(fromSideData);
        }
        return probeRotation(
                "ffprobe -v error -select_streams v:0 -show_entries stream_tags=rotate "
                        + "-of default=nw=1:nk=1 \"%s\"",
                videoFilePath);
    }

    private int probeRotation(String cmdTemplate, String videoFilePath) {
        String result = ProcessUtils.executeCommand(String.format(cmdTemplate, videoFilePath),
                appConfig.getShowFFmpegLog());
        if (StringTools.isEmpty(result)) {
            return 0;
        }
        String line = result.trim().split("\n")[0].trim();
        if (line.isEmpty()) {
            return 0;
        }
        try {
            return normalizeRotationDegrees((int) Math.round(Double.parseDouble(line)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int normalizeRotationDegrees(int degrees) {
        int d = degrees % 360;
        if (d < 0) {
            d += 360;
        }
        if (d == 90 || d == 180 || d == 270) {
            return d;
        }
        return 0;
    }

    /** 检测 VFR：avg_frame_rate 与 r_frame_rate 差异大时视为可变帧率 */
    private boolean isVariableFrameRate(String videoFilePath) {
        final String cmd = "ffprobe -v error -select_streams v:0 "
                + "-show_entries stream=avg_frame_rate,r_frame_rate -of csv=p=0 \"%s\"";
        String result = ProcessUtils.executeCommand(String.format(cmd, videoFilePath), appConfig.getShowFFmpegLog());
        if (StringTools.isEmpty(result)) {
            return false;
        }
        String[] parts = result.trim().split(",");
        if (parts.length < 2) {
            return false;
        }
        double avg = parseFrameRate(parts[0]);
        double r = parseFrameRate(parts[1]);
        if (avg <= 0 || r <= 0) {
            return false;
        }
        double ratio = avg / r;
        if (ratio < 0.85 || ratio > 1.15) {
            log.info("检测到 VFR: avg={}, r={}, file={}", avg, r, videoFilePath);
            return true;
        }
        return false;
    }

    private static double parseFrameRate(String rate) {
        if (rate == null || rate.isEmpty() || "0/0".equals(rate)) {
            return 0;
        }
        if (rate.contains("/")) {
            String[] ab = rate.split("/");
            double a = Double.parseDouble(ab[0]);
            double b = Double.parseDouble(ab[1]);
            return b == 0 ? 0 : a / b;
        }
        return Double.parseDouble(rate);
    }

    /**
     * 重编码时 ffmpeg 会按元数据自动转正画面，并清除 rotate 标签；保留原时长（passthrough + -t）。
     */
    public void normalizeForStreaming(String videoFilePath) {
        double inputDuration = getVideoInfoDurationPrecise(videoFilePath);
        String normalized = videoFilePath + ".norm.mp4";
        String durationLimit = inputDuration > 0 ? String.format("-t %.3f ", inputDuration) : "";
        String cmd = "ffmpeg -y -i \"%s\" %s-map 0:v:0 -map 0:a? "
                + "-vf \"scale='min(1280,iw)':-2\" "
                + "-c:v libx264 -preset veryfast -crf 23 -g 48 -keyint_min 48 -sc_threshold 0 "
                + "-fps_mode passthrough "
                + "-c:a aac -b:a 128k -metadata:s:v:0 rotate=0 -movflags +faststart \"%s\"";
        cmd = String.format(cmd, videoFilePath, durationLimit, normalized);
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        File src = new File(videoFilePath);
        File out = new File(normalized);
        if (!out.exists() || out.length() == 0) {
            log.warn("normalizeForStreaming 未生成输出，保留原片: {}", videoFilePath);
            return;
        }
        double outDuration = getVideoInfoDurationPrecise(normalized);
        log.info("normalize: in={}s out={}s file={}", inputDuration, outDuration, videoFilePath);
        if (src.exists() && !src.delete()) {
            log.warn("无法删除原片，保留 norm 文件: {}", videoFilePath);
            return;
        }
        if (!out.renameTo(src)) {
            log.error("normalize 重命名失败: {}", normalized);
        }
    }

    /**
     * 规范 + HLS 多切片（ffmpeg 生成 m3u8，EXTINF 与真实片源一致，拖动 seek 更快）。
     * @return 转码后实际时长（秒，四舍五入）
     */
    public Integer convertVideo2TsAndGetDuration(File tsFolder, String videoFilePath) {
        double mergedDuration = getVideoInfoDurationPrecise(videoFilePath);
        if (needsNormalizeForStreaming(videoFilePath)) {
            normalizeForStreaming(videoFilePath);
        } else {
            log.info("跳过 normalize，直接 HLS: {}", videoFilePath);
        }
        String m3u8Path = tsFolder.getAbsolutePath() + "/" + Constants.M3U8_NAME;
        String segPattern = tsFolder.getAbsolutePath() + "/%04d.ts";
        String cmd = String.format(
                "ffmpeg -y -i \"%s\" -c copy -bsf:v h264_mp4toannexb -c:a copy "
                        + "-f hls -hls_time %d -hls_list_size 0 -hls_playlist_type vod "
                        + "-hls_flags independent_segments "
                        + "-hls_segment_filename \"%s\" \"%s\"",
                videoFilePath, HLS_SEGMENT_TIME_SEC, segPattern, m3u8Path);
        ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        double durationSec = getVideoInfoDurationPrecise(m3u8Path);
        if (durationSec <= 0) {
            durationSec = sumTsDurationInFolder(tsFolder);
        }
        if (mergedDuration > 0 && durationSec > 0 && mergedDuration - durationSec > 3) {
            log.warn("HLS 时长明显短于合并原片: merged={}s hls={}s，以原片时长入库", mergedDuration, durationSec);
            durationSec = mergedDuration;
        }
        log.info("HLS VOD: duration={}s, folder={}", durationSec, tsFolder.getPath());
        if (durationSec <= 0) {
            return 0;
        }
        return (int) Math.round(durationSec);
    }

    public void convertVideo2Ts(File tsFolder, String videoFilePath) {
        convertVideo2TsAndGetDuration(tsFolder, videoFilePath);
    }

    /** 从目录内 ts 累加时长，作为 m3u8 探测失败时的兜底 */
    private double sumTsDurationInFolder(File tsFolder) {
        File[] files = tsFolder.listFiles((dir, name) -> name.endsWith(".ts"));
        if (files == null || files.length == 0) {
            return 0;
        }
        double total = 0;
        for (File f : files) {
            total += getVideoInfoDurationPrecise(f.getAbsolutePath());
        }
        return total;
    }

}
