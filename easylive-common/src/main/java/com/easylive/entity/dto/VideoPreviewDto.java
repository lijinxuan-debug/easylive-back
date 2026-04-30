package com.easylive.entity.dto;

public class VideoPreviewDto {
    private String url;        // 预览大图的完整访问路径
    private int total = 400;   // 总帧数（对应你命令里的 totalImages）
    private int col = 10;      // 矩阵列数（对应 tile 的第一个参数）
    private int row = 40;      // 矩阵行数（对应 tile 的第二个参数）
    private int frameW = 160;  // 单帧宽度
    private int frameH = 90;   // 单帧高度
    private double interval;   // 核心：每隔多少秒换一张图 (duration / total)

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getFrameW() {
        return frameW;
    }

    public void setFrameW(int frameW) {
        this.frameW = frameW;
    }

    public int getFrameH() {
        return frameH;
    }

    public void setFrameH(int frameH) {
        this.frameH = frameH;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }
}