# EasyLive Web 后端接口文档

## 基础配置

- **Base URL**: `http://localhost:7071`
- **Content-Type**: `application/x-www-form-urlencoded` (默认) 或 `application/json`
- **跨域**: 已配置，支持 CORS
- **Token 认证**: Header 方式，Header 名称：`webToken`

---

# Token 验证说明

## Token 获取方式

**请求 Header**：
```
webToken: 1ce7f0e7-23c2-48dc-8079-1ab4e9912808
```

**注意**：Token 不是通过 `Authorization` Header 传递，而是通过 `webToken` Header（全小写）

## Token 有效期

- Token 默认有效期为 **7 天**
- 当 Token 即将过期（少于 1 天）时，调用「自动登录」接口会自动续期

---

# 通用响应格式

所有接口统一使用以下响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 响应码（200：成功，901：登录已过期，其他：业务错误） |
| message | String | 响应消息 |
| data | Object/Array/null | 响应数据 |

---

# 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |
| 600 | 业务错误（具体错误信息在 message 中） |
| 901 | 登录已过期，请重新登录 |

---

# 1. 基础接口

## 1.1 获取验证码

### 接口描述
生成算术验证码，返回 Base64 图片和验证码 Key

### 请求信息
- **路径**: `/account/checkCode`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "checkCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg...",
    "checkCodeKey": "check_code_key_xxx"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| checkCode | String | Base64 编码的验证码图片 |
| checkCodeKey | String | 验证码唯一标识，后续验证需要 |

---

## 1.2 用户注册

### 接口描述
使用邮箱注册新用户

### 请求信息
- **路径**: `/account/register`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | ✅ | 邮箱地址（需为有效邮箱格式） |
| nickName | String | ✅ | 昵称（最大30字符） |
| registerPassword | String | ✅ | 密码（需符合密码规则） |
| checkCode | String | ✅ | 验证码答案（不区分大小写） |
| checkCodeKey | String | ✅ | 验证码 Key（从 checkCode 接口获取） |

### 密码规则
- 必须包含大小写字母、数字
- 长度：8-20 位

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 1.3 用户登录

### 接口描述
用户登录，返回 Token 信息

### 请求信息
- **路径**: `/account/login`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | ✅ | 邮箱地址 |
| password | String | ✅ | 密码 |
| checkCode | String | ✅ | 验证码答案（不区分大小写） |
| checkCodeKey | String | ✅ | 验证码 Key（从 checkCode 接口获取） |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "web_token_xxx",
    "userId": "user_id_xxx",
    "nickName": "用户昵称",
    "email": "user@example.com",
    "qq": "123456",
    "status": 0,
    "expireTime": 1712736000000
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | 登录凭证，用于后续请求的 Header |
| userId | String | 用户唯一标识 |
| nickName | String | 用户昵称 |
| email | String | 邮箱地址 |
| qq | String | QQ 号码 |
| status | Integer | 用户状态（0：正常，1：禁用） |
| expireTime | Long | Token 过期时间戳（毫秒） |

---

## 1.4 自动登录

### 接口描述
根据 Header 中的 Token 自动登录，并延长 Token 有效期

### 请求信息
- **路径**: `/account/autoLogin`
- **方法**: POST
- **是否需要登录**: ❌ 否（需要 Header 中的 Token）

### 请求参数
无（从 Header 中读取 Token）

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "web_token_xxx",
    "userId": "user_id_xxx",
    "nickName": "用户昵称",
    "email": "user@example.com",
    "qq": "123456",
    "status": 0,
    "expireTime": 1712736000000
  }
}
```

### 说明
- 如果 Token 无效，返回 `data: null`
- 如果 Token 即将过期（少于一天），自动续期
- **推荐在页面加载时调用此接口进行自动登录**

---

## 1.5 用户登出

### 接口描述
清除登录状态

### 请求信息
- **路径**: `/account/loginOut`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 说明
- 清除 Header 中的 Token（需要前端配合）
- 清除 Redis 中的 Token 信息

---

## 1.6 获取用户统计

### 接口描述
获取当前登录用户的统计数据

### 请求信息
- **路径**: `/account/getCountInfo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无（从 Header 中读取 Token）

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "videoCount": 10,
    "userCount": 100,
    "commentCount": 50,
    "likeCount": 200,
    "collectCount": 30
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| videoCount | Integer | 用户发布的视频数量 |
| userCount | Integer | 粉丝数量 |
| commentCount | Integer | 评论数量 |
| likeCount | Integer | 获赞数量 |
| collectCount | Integer | 收藏数量 |

---

# 2. 视频相关

## 2.1 获取推荐视频

### 接口描述
获取推荐的视频列表

### 请求信息
- **路径**: `/video/loadCommendVideo`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "videoId": "video_id_xxx",
      "videoName": "视频标题",
      "videoCover": "cover_image_path",
      "playCount": 1000,
      "userId": "user_id_xxx",
      "nickName": "用户昵称",
      "videoDuration": 120,
      "createTime": 1712736000000
    }
  ]
}
```

---

## 2.2 获取视频列表

### 接口描述
根据分类获取视频列表

### 请求信息
- **路径**: `/video/loadVideo`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pCategoryId | Integer | ❌ | 一级分类 ID |
| categoryId | Integer | ❌ | 二级分类 ID |
| pageNo | Integer | ❌ | 页码（默认 1） |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 100,
    "pageSize": 20,
    "pageNo": 1,
    "list": [...]
  }
}
```

---

## 2.3 获取视频详情

### 接口描述
获取视频的详细信息

### 请求信息
- **路径**: `/video/getVideoInfo`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "videoInfo": {
      "videoId": "video_id_xxx",
      "videoName": "视频标题",
      "videoCover": "cover_image_path",
      "userId": "user_id_xxx",
      "nickName": "用户昵称",
      ...
    },
    "userActionList": [
      {
        "videoId": "video_id_xxx",
        "userId": "user_id_xxx",
        "actionType": 1,
        ...
      }
    ]
  }
}
```

---

## 2.4 获取视频播放列表

### 接口描述
获取视频的多个分片文件列表（用于 HLS 播放）

### 请求信息
- **路径**: `/video/loadVideoPList`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "fileId": "file_id_xxx",
      "videoId": "video_id_xxx",
      "filePath": "video/2026-04-10/xxx",
      "fileIndex": 0,
      "fileSize": 1024000
    }
  ]
}
```

---

## 2.5 搜索视频

### 接口描述
根据关键词搜索视频

### 请求信息
- **路径**: `/video/search`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | ✅ | 搜索关键词 |
| orderType | Integer | ❌ | 排序方式 |
| pageNo | Integer | ❌ | 页码（默认 1） |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 50,
    "pageSize": 30,
    "pageNo": 1,
    "list": [...]
  }
}
```

---

## 2.6 获取搜索热词

### 接口描述
获取搜索热词排行榜

### 请求信息
- **路径**: `/video/getHotWordTop`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": [
    "关键词1",
    "关键词2",
    "关键词3",
    ...
  ]
}
```

---

## 2.7 获取推荐视频

### 接口描述
获取相关推荐视频

### 请求信息
- **路径**: `/video/getVideoRecommend`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyWord | String | ✅ | 推荐关键词 |
| videoId | String | ✅ | 当前视频 ID（排除此视频） |

---

## 2.8 获取热门视频

### 接口描述
获取最近 24 小时内的热门视频

### 请求信息
- **路径**: `/video/loadHotVideoList`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | ❌ | 页码（默认 1） |

---

## 2.9 上报视频播放在线人数

### 接口描述
上报视频播放在线人数

### 请求信息
- **路径**: `/video/reportVideoPlayOnline`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | String | ✅ | 文件 ID |
| deviceId | String | ✅ | 设备 ID |

---

# 3. 评论相关

## 3.1 发布评论

### 接口描述
发布视频评论

### 请求信息
- **路径**: `/comment/postComment`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| content | String | ✅ | 评论内容（最大500字符） |
| replyCommentId | Integer | ❌ | 回复的评论 ID |
| imgPath | String | ❌ | 评论图片路径（最大500字符） |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "commentId": 123,
    "videoId": "video_id_xxx",
    "userId": "user_id_xxx",
    "content": "评论内容",
    "postTime": 1712736000000
  }
}
```

---

## 3.2 加载评论

### 接口描述
获取视频的评论列表

### 请求信息
- **路径**: `/comment/loadComment`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| pageNo | Integer | ❌ | 页码（默认 1） |
| orderType | Integer | ❌ | 排序方式（0：按赞排序，1：按时间排序） |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "commentData": {
      "totalCount": 100,
      "pageSize": 15,
      "pageNo": 1,
      "list": [...]
    },
    "userActionList": [...]
  }
}
```

---

## 3.3 置顶评论

### 接口描述
置顶评论（仅评论作者可操作）

### 请求信息
- **路径**: `/comment/top`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

---

## 3.4 取消置顶

### 接口描述
取消评论置顶

### 请求信息
- **路径**: `/comment/cancelTop`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

---

## 3.5 删除评论

### 接口描述
删除评论（仅评论作者可操作）

### 请求信息
- **路径**: `/comment/deleteComment`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

---

# 4. 弹幕相关

## 4.1 发布弹幕

### 接口描述
发布视频弹幕

### 请求信息
- **路径**: `/danmu/postDanmu`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| fileId | String | ✅ | 文件 ID |
| text | String | ✅ | 弹幕内容（最大200字符） |
| color | String | ✅ | 弹幕颜色（如 #FFFFFF） |
| mode | Integer | ✅ | 弹幕模式 |
| time | Integer | ✅ | 弹幕时间（秒） |

---

## 4.2 加载弹幕

### 接口描述
获取视频的弹幕列表

### 请求信息
- **路径**: `/danmu/loadDanmu`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| fileId | String | ✅ | 文件 ID |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "danmuId": 123,
      "videoId": "video_id_xxx",
      "text": "弹幕内容",
      "color": "#FFFFFF",
      "mode": 1,
      "time": 10,
      "userId": "user_id_xxx",
      "postTime": 1712736000000
    }
  ]
}
```

---

# 5. 用户操作（点赞、收藏、投币）

## 5.1 执行用户操作

### 接口描述
执行用户操作（点赞、收藏、投币）

### 请求信息
- **路径**: `/userAction/doAction`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| actionType | Integer | ✅ | 操作类型（1：点赞，2：收藏，3：投币） |
| actionCount | Integer | ❌ | 操作数量（1-2，默认1） |
| commentId | Integer | ❌ | 评论 ID（评论点赞时需要） |

### 操作类型说明

| actionType | 说明 |
|------------|------|
| 1 | 点赞 |
| 2 | 收藏 |
| 3 | 投币 |

---

# 6. 消息相关

## 6.1 获取未读消息数量

### 接口描述
获取当前登录用户的未读消息数量

### 请求信息
- **路径**: `/message/getNoReadCount`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": 10
}
```

---

## 6.2 获取未读消息分组

### 接口描述
获取当前登录用户的未读消息分组统计

### 请求信息
- **路径**: `/message/getNoReadCountGroup`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "messageType": 1,
      "count": 5,
      "typeName": "点赞"
    },
    {
      "messageType": 2,
      "count": 3,
      "typeName": "评论"
    }
  ]
}
```

---

## 6.3 全部标记为已读

### 接口描述
将指定类型的消息全部标记为已读

### 请求信息
- **路径**: `/message/readAll`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageType | Integer | ✅ | 消息类型 |

### 消息类型说明

| messageType | 说明 |
|------------|------|
| 1 | 点赞 |
| 2 | 评论 |
| 3 | 收藏 |

---

## 6.4 加载消息列表

### 接口描述
获取指定类型的消息列表

### 请求信息
- **路径**: `/message/loadMessage`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageType | Integer | ✅ | 消息类型 |
| pageNo | Integer | ❌ | 页码（默认 1） |

---

## 6.5 删除消息

### 接口描述
删除指定消息

### 请求信息
- **路径**: `/message/delMessage`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageId | Integer | ✅ | 消息 ID |

---

# 7. 文件相关

## 7.1 获取图片

### 接口描述
获取图片资源

### 请求信息
- **路径**: `/file/getImage`
- **方法**: GET
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sourceName | String | ✅ | 图片路径（如 cover/2026-04-10/xxx.jpg） |

---

## 7.2 上传图片

### 接口描述
上传图片

### 请求信息
- **路径**: `/file/uploadImage`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | ✅ | 图片文件 |
| createThumbnail | Boolean | ✅ | 是否创建缩略图 |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": "cover/2026-04-10/xxx.jpg"
}
```

---

## 7.3 预上传视频

### 接口描述
开始上传视频前的预操作（生成分片上传 ID）

### 请求信息
- **路径**: `/file/preUploadVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileName | String | ✅ | 文件名 |
| chunks | Integer | ✅ | 分片数量 |

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": "upload_id_xxx"
}
```

---

## 7.4 上传视频分片

### 接口描述
上传视频分片

### 请求信息
- **路径**: `/file/uploadVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| chunkFile | File | ✅ | 分片文件 |
| chunkIndex | Integer | ✅ | 分片索引 |
| uploadId | String | ✅ | 上传 ID（从预上传接口获取） |

---

## 7.5 删除上传中的视频

### 接口描述
删除正在上传中的视频

### 请求信息
- **路径**: `/file/delUploadVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| uploadId | String | ✅ | 上传 ID |

---

## 7.6 获取视频资源

### 接口描述
获取视频播放资源（M3U8 文件）

### 请求信息
- **路径**: `/file/videoResource/{fileId}`
- **方法**: GET
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | String | ✅ | 文件 ID（路径参数） |

### 返回结果
返回视频 M3U8 文件流

---

# 8. 用户中心主页

## 8.1 获取用户信息

### 接口描述
获取用户详细信息

### 请求信息
- **路径**: `/ucenter/home/getUserInfo`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | ✅ | 用户 ID |

---

## 8.2 更新用户信息

### 接口描述
更新当前登录用户的信息

### 请求信息
- **路径**: `/ucenter/home/updateUserInfo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| avatar | String | ✅ | 头像路径（最大100字符） |
| nickName | String | ✅ | 昵称（最大20字符） |
| sex | Integer | ✅ | 性别（1：男，2：女） |
| birthday | String | ❌ | 生日（最大100字符） |
| school | String | ❌ | 学校（最大100字符） |
| noticeInfo | String | ❌ | 通知信息（最大300字符） |
| personalIntroduction | String | ❌ | 个人介绍（最大80字符） |

---

## 8.3 保存主题

### 接口描述
设置用户主题

### 请求信息
- **路径**: `/ucenter/home/saveTheme`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| theme | Integer | ✅ | 主题 ID（1-10） |

---

## 8.4 关注用户

### 接口描述
关注指定用户

### 请求信息
- **路径**: `/ucenter/home/focus`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| focusUserId | String | ✅ | 要关注的用户 ID |

---

## 8.5 取消关注

### 接口描述
取消关注指定用户

### 请求信息
- **路径**: `/ucenter/home/cancelFocus`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| focusUserId | String | ✅ | 要取消关注的用户 ID |

---

## 8.6 获取关注列表

### 接口描述
获取当前登录用户的关注列表

### 请求信息
- **路径**: `/ucenter/home/loadFocusList`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | ❌ | 页码（默认 1） |

---

## 8.7 获取粉丝列表

### 接口描述
获取当前登录用户的粉丝列表

### 请求信息
- **路径**: `/ucenter/home/loadFansList`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | ❌ | 页码（默认 1） |

---

## 8.8 获取用户视频列表

### 接口描述
获取指定用户的视频列表

### 请求信息
- **路径**: `/ucenter/home/loadVideoList`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | ✅ | 用户 ID |
| type | Integer | ❌ | 视频类型 |
| pageNo | Integer | ❌ | 页码（默认 1） |
| videoName | String | ❌ | 视频名称（模糊搜索） |
| orderType | Integer | ❌ | 排序方式 |

---

## 8.9 获取用户收藏

### 接口描述
获取指定用户的收藏列表

### 请求信息
- **路径**: `/ucenter/home/loadUserCollection`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | ✅ | 用户 ID |
| pageNo | Integer | ❌ | 页码（默认 1） |

---

# 9. 视频发布

## 9.1 发布视频

### 接口描述
发布新视频或更新现有视频

### 请求信息
- **路径**: `/ucenter/post/postVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ❌ | 视频 ID（更新时需要） |
| videoCover | String | ✅ | 视频封面路径 |
| videoName | String | ✅ | 视频名称（最大100字符） |
| pCategoryId | Integer | ✅ | 一级分类 ID |
| categoryId | Integer | ❌ | 二级分类 ID |
| postType | Integer | ✅ | 发布类型（0：原创，1：转载） |
| tags | String | ✅ | 标签（最大300字符） |
| introduction | String | ❌ | 视频简介（最大2000字符） |
| interaction | String | ❌ | 互动设置（1：关闭评论，2：关闭弹幕，3：全部关闭，组合传） |
| uploadFileList | String | ✅ | 上传文件列表 JSON 数组 |

---

## 9.2 加载视频列表（发布管理）

### 接口描述
获取当前登录用户的视频管理列表

### 请求信息
- **路径**: `/ucenter/post/loadVideoList`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | ❌ | 视频状态（-1：进行中，3：审核成功，4：审核失败） |
| pageNo | Integer | ❌ | 页码（默认 1） |
| videoNameFuzzy | String | ❌ | 视频名称（模糊搜索） |

---

## 9.3 获取视频数量统计

### 接口描述
获取当前登录用户的不同状态视频数量统计

### 请求信息
- **路径**: `/ucenter/post/getVideoCountInfo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "auditSuccessCount": 10,
    "auditFailCount": 2,
    "inProcessCount": 5
  }
}
```

---

## 9.4 获取视频编辑信息

### 接口描述
获取视频的编辑信息

### 请求信息
- **路径**: `/ucenter/post/getVideoInfoByVideoId`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |

---

## 9.5 保存视频互动设置

### 接口描述
保存视频的互动设置（评论、弹幕开关）

### 请求信息
- **路径**: `/ucenter/post/saveVideoInteraction`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| interaction | String | ✅ | 互动设置（1：关闭评论，2：关闭弹幕，3：全部关闭，组合传） |

---

## 9.6 删除视频

### 接口描述
删除指定视频

### 请求信息
- **路径**: `/ucenter/post/delVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |

---

# 10. 用户互动管理

## 10.1 加载所有视频

### 接口描述
获取当前登录用户的所有视频（用于互动管理）

### 请求信息
- **路径**: `/ucenter/interaction/loadAllVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无

---

## 10.2 加载评论

### 接口描述
获取当前登录用户的评论列表

### 请求信息
- **路径**: `/ucenter/interaction/loadComment`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | ❌ | 页码（默认 1） |
| videoId | String | ❌ | 视频 ID（筛选指定视频） |

---

## 10.3 删除评论

### 接口描述
删除当前登录用户的评论

### 请求信息
- **路径**: `/ucenter/interaction/delComment`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

---

## 10.4 加载弹幕

### 接口描述
获取当前登录用户的弹幕列表

### 请求信息
- **路径**: `/ucenter/interaction/loadDanmu`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | ❌ | 页码（默认 1） |
| videoId | String | ❌ | 视频 ID（筛选指定视频） |

---

## 10.5 删除弹幕

### 接口描述
删除当前登录用户的弹幕

### 请求信息
- **路径**: `/ucenter/interaction/delDanmu`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| danmuId | Integer | ✅ | 弹幕 ID |

---

# 11. 视频系列

## 11.1 加载视频系列

### 接口描述
获取指定用户的视频系列列表

### 请求信息
- **路径**: `/ucenter/series/loadVideoSeries`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | ✅ | 用户 ID |

---

## 11.2 保存视频系列

### 接口描述
创建或更新视频系列

### 请求信息
- **路径**: `/ucenter/series/saveVideoSeries`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| seriesId | Integer | ❌ | 系列 ID（更新时需要） |
| seriesName | String | ✅ | 系列名称（最大100字符） |
| seriesDescription | String | ❌ | 系列描述（最大200字符） |
| videoIds | String | ❌ | 视频 ID 列表（逗号分隔） |

---

## 11.3 加载所有视频

### 接口描述
获取当前登录用户的可添加到系列的视频列表

### 请求信息
- **路径**: `/ucenter/series/loadAllVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| seriesId | Integer | ❌ | 系列 ID（筛选排除该系列已添加的视频） |

---

## 11.4 获取系列详情

### 接口描述
获取视频系列详情及包含的视频

### 请求信息
- **路径**: `/ucenter/series/getVideoSeriesDetail`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| seriesId | Integer | ✅ | 系列 ID |

---

## 11.5 保存系列视频

### 接口描述
将视频添加到指定系列

### 请求信息
- **路径**: `/ucenter/series/saveSeriesVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| seriesId | Integer | ✅ | 系列 ID |
| videoIds | String | ✅ | 视频 ID 列表（逗号分隔） |

---

## 11.6 删除系列视频

### 接口描述
从系列中删除指定视频

### 请求信息
- **路径**: `/ucenter/series/delSeriesVideo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| seriesId | Integer | ✅ | 系列 ID |
| videoId | String | ✅ | 视频 ID |

---

## 11.7 删除系列

### 接口描述
删除指定视频系列

### 请求信息
- **路径**: `/ucenter/series/delSeries`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| seriesId | Integer | ✅ | 系列 ID |

---

## 11.8 修改系列排序

### 接口描述
修改视频系列的排序

### 请求信息
- **路径**: `/ucenter/series/changeVideoSeriesSort`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| seriesIds | String | ✅ | 系列 ID 列表（逗号分隔，按顺序排列） |

---

## 11.9 加载系列及视频

### 接口描述
获取指定用户的所有系列及包含的视频

### 请求信息
- **路径**: `/ucenter/series/loadVideoSeriesWithVideo`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | ✅ | 用户 ID |

---

# 12. 播放历史

## 12.1 加载播放历史

### 接口描述
获取当前登录用户的播放历史

### 请求信息
- **路径**: `/history/loadHistory`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | ❌ | 页码（默认 1） |

---

## 12.2 清空播放历史

### 接口描述
清空当前登录用户的播放历史

### 请求信息
- **路径**: `/history/cleanAllHistory`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无

---

## 12.3 删除播放历史

### 接口描述
删除指定视频的播放历史

### 请求信息
- **路径**: `/history/delHistory`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| videoId | String | ✅ | 视频 ID |

---

# 13. 统计

## 13.1 获取实时统计信息

### 接口描述
获取当前登录用户的实时统计数据（昨日数据 + 总计数据）

### 请求信息
- **路径**: `/statistics/getActualTimeStatisticsInfo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "preDayData": {
      "1": 10,
      "2": 5,
      "3": 3
    },
    "totalCountInfo": {
      "playCount": 1000,
      "likeCount": 500,
      "collectCount": 100,
      "commentCount": 200
    }
  }
}
```

---

## 13.2 获取周统计信息

### 接口描述
获取当前登录用户的周统计数据

### 请求信息
- **路径**: `/statistics/getWeekStatisticsInfo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dataType | Integer | ✅ | 数据类型（1：播放量，2：点赞，3：收藏，4：评论） |

---

# 14. 其他

## 14.1 获取所有分类

### 接口描述
获取所有视频分类

### 请求信息
- **路径**: `/categoryInfo/loadAllCategoryInfo`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryId": 1,
      "categoryName": "一级分类",
      "sort": 1,
      "children": [
        {
          "categoryId": 2,
          "categoryName": "二级分类",
          "pCategoryId": 1,
          "sort": 1
        }
      ]
    }
  ]
}
```

---

## 14.2 获取系统设置

### 接口描述
获取系统设置信息

### 请求信息
- **路径**: `/sysSetting/getSysSetting`
- **方法**: POST
- **是否需要登录**: ❌ 否

### 请求参数
无

### 返回结果
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "admin",
    "password": "admin123",
    "registerType": 0,
    "userStatus": 0,
    "videoSize": 500,
    "videoPostAudit": 0,
    "commentAudit": 0,
    "danmuAudit": 0
  }
}
```

---

# 附录

## 文件路径规则

| 路径类型 | 说明 |
|----------|------|
| cover/ | 封面图片 |
| video/ | 视频文件 |
| file/video/ | 播放中的视频分片 |

## 时间格式

- 所有时间戳均为毫秒级（Long 类型）
- 日期格式统一为 yyyy-MM-dd

## 分页参数

| 参数 | 说明 |
|------|------|
| totalCount | 总记录数 |
| pageSize | 每页记录数（一般为 15、20、30） |
| pageNo | 当前页码（从 1 开始） |
| list | 数据列表 |

## 联系方式

如有问题，请联系技术支持。

---

**文档版本**: v1.0
**最后更新**: 2026-04-10
