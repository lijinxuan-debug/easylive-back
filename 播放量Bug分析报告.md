# EasyLive 播放量 Bug 分析报告

## 问题描述

**症状**: 用户点击视频播放后，播放量（play_count）仍为 0

---

## 问题定位

### 1. 播放流程分析

```
用户点击播放视频
    ↓
FileController.videoResource() 被调用
    ↓
saveVideoPlayInfo() - 将播放信息存入 Redis 队列
    ↓
ExecuteQueueTask.consumVideoPlayInfoQueue() - 从队列取播放信息
    ↓
videoInfoService.addReadCount(videoId) - 增加"阅读数"
    ↓
redisComponent.recordPlayCount(videoId) - 按天记录"播放数"
```

### 2. Bug 根因

**问题**: 调用的是 `addReadCount` 方法，而不是增加播放计数的方法！

**正确逻辑应该是**：
```
1. 用户点击播放 → videoResource()
2. 保存播放信息到 Redis 队列
3. 定时任务消费队列
4. 调用 addPlayCount() 或类似方法直接增加 play_count
5. 按天记录播放数到 Redis
```

**实际错误逻辑**：
```
1. 用户点击播放 → videoResource()
2. 保存播放信息到 Redis 队列
3. 定时任务消费队列
4. 调用 addReadCount() - 增加"阅读数"（错误的！）
5. 按天记录播放数到 Redis（这个可能正常）
```

---

## 关键代码位置

### ExecuteQueueTask.java:83
```java
// BUG: 这里调用的是 addReadCount（阅读数）
videoInfoService.addReadCount(videoPlayInfoDto.getVideoId());
```

**正确应该是**：
```java
videoInfoService.addPlayCount(videoPlayInfoDto.getVideoId());
```

---

## 验证方法

### 检查点 1：数据库字段确认

查看 `video_info` 表的 play_count 字段

```sql
SELECT video_id, play_count
FROM video_info
WHERE video_id = 'xxx'
```

### 检查点 2：Service 方法检查

查看 VideoInfoService 是否有 `addPlayCount` 方法

```bash
find . -name "*VideoInfoService.java" -type f | xargs grep -n "addPlayCount\|addReadCount"
```

### 检查点 3：定时任务状态

确认定时任务是否正常运行

```bash
# 检查任务日志
tail -f logs/easylive-web.log | grep -i "video.*play\|queue"
```

---

## 修复方案

### 方案一：修改方法调用（推荐）

**文件**: `easylive-web/src/main/java/com/easylive/web/task/ExecuteQueueTask.java`

**第 83 行修改为**：
```java
// 修改前：
videoInfoService.addReadCount(videoPlayInfoDto.getVideoId());

// 修改后：
videoInfoService.addPlayCount(videoPlayInfoDto.getVideoId());
```

### 方案二：确保定时任务运行

检查 SysTask.java 中定时任务配置

```java
@Scheduled(cron = "0 */1 * * * ?")  // 每分钟执行
public void executeQueueTask() {
    ...
}
```

### 方案三：添加播放计数直接调用

在 FileController 中添加播放计数：

```java
@RequestMapping("/videoResource/{fileId}")
public void videoResource(..., ...) {
    ...
    // 读取播放信息
    readFile(...);

    // 直接增加播放计数（可选）
    TokenUserInfoDto userInfoDto = getTokenUserInfoFromCookie();
    if (userInfoDto != null) {
        redisComponent.recordPlayCount(videoId);
    }
}
```

---

## 测试步骤

1. 重启后端服务
2. 清空 Redis 中的播放计数数据（可选）
3. 前端打开视频播放
4. 检查 Redis 数据：
   ```bash
   redis-cli KEYS "*VIDEO_PLAY_COUNT*"
   redis-cli GET "VIDEO_PLAY_COUNT:2026-04-10:videoId"
   ```
5. 检查数据库：
   ```bash
   select play_count from video_info where video_id = 'xxx'
   ```

---

## 数据一致性检查

Redis 中的播放计数与数据库的播放计数应该同步：

```
Redis: VIDEO_PLAY_COUNT:2026-04-10:videoId = 10
Database: video_info.play_count = 10
```

---

## 总结

**问题确认**: 存在播放量不更新的 Bug

**主要原因**: `addReadCount` 方法可能是增加"阅读数"而非"播放数"

**建议修复**:
1. 检查 `addReadCount` 方法实现
2. 如确认是错误的，改为调用正确的播放计数方法
3. 或在播放接口中直接增加播放计数

---

**优先级**: 高（影响核心功能）
