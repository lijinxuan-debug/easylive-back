# EasyLive Web 后端接口文档

## 基础配置

- **Base URL**: `http://localhost:7071`
- **Content-Type**: `application/x-www-form-urlencoded` (默认) 或 `application/json`
- **跨域**: 已配置，支持 CORS
- **Token 认证**: Cookie 方式，Cookie 名称：`webToken`

---

# 基础接口

## 1. 获取验证码

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

## 2. 用户注册

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

### 错误码
| 错误码 | 说明 |
|--------|------|
| 600 | 验证码错误 |
| 600 | 邮箱已存在 |

---

## 3. 用户登录

### 接口描述
用户登录，返回 Token 信息，Token 存储在 Cookie 中

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
| token | String | 登录凭证，会自动存储在 Cookie 中 |
| userId | String | 用户唯一标识 |
| nickName | String | 用户昵称 |
| email | String | 邮箱地址 |
| qq | String | QQ 号码 |
| status | Integer | 用户状态（0：正常，1：禁用） |
| expireTime | Long | Token 过期时间戳（毫秒） |

### Cookie 信息
- **Cookie 名称**: `webToken`
- **存储方式**: 自动设置到响应 Cookie 中

### 错误码
| 错误码 | 说明 |
|--------|------|
| 600 | 验证码错误 |
| 600 | 邮箱或密码错误 |
| 600 | 用户已被禁用 |

---

## 4. 自动登录

### 接口描述
根据 Cookie 中的 Token 自动登录，并延长 Token 有效期

### 请求信息
- **路径**: `/account/autoLogin`
- **方法**: POST
- **是否需要登录**: ❌ 否（需要 Cookie 中的 Token）

### 请求参数
无（从 Cookie 中读取 Token）

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
- 如果 Token 即将过期（少于一天），自动续期并更新 Cookie
- **推荐在页面加载时调用此接口进行自动登录**

---

## 5. 用户登出

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
- 清除 Cookie 中的 `webToken`
- 清除 Redis 中的 Token 信息

---

## 6. 获取用户统计信息

### 接口描述
获取当前登录用户的统计数据

### 请求信息
- **路径**: `/account/getCountInfo`
- **方法**: POST
- **是否需要登录**: ✅ 是

### 请求参数
无（从 Cookie 中读取 Token）

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

### 错误码
| 错误码 | 说明 |
|--------|------|
| 901 | 登录已过期，请重新登录 |

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
| code | Integer | 响应码（200：成功，其他：失败） |
| message | String | 响应消息 |
| data | Object/Array/null | 响应数据 |

---

# 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |
| 600 | 业务错误（具体错误信息在 message 中） |
| 901 | 登录已过期 |

---

# 请求示例（使用 curl）

## 获取验证码
```bash
curl -X POST http://localhost:7071/account/checkCode
```

## 用户注册
```bash
curl -X POST http://localhost:7071/account/register \
  -d "email=test@example.com" \
  -d "nickName=测试用户" \
  -d "registerPassword=Test1234" \
  -d "checkCode=5" \
  -d "checkCodeKey=check_code_key_xxx"
```

## 用户登录
```bash
curl -X POST http://localhost:7071/account/login \
  -d "email=test@example.com" \
  -d "password=Test1234" \
  -d "checkCode=5" \
  -d "checkCodeKey=check_code_key_xxx" \
  -c cookies.txt  # 保存 Cookie
```

## 自动登录
```bash
curl -X POST http://localhost:7071/account/autoLogin \
  -b cookies.txt  # 使用之前保存的 Cookie
```

## 获取用户统计
```bash
curl -X POST http://localhost:7071/account/getCountInfo \
  -b cookies.txt
```

## 用户登出
```bash
curl -X POST http://localhost:7071/account/loginOut
```
