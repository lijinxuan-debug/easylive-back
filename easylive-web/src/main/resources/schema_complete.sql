-- ========================================
-- EasyLive 完整数据库表结构
-- ========================================
-- 包含 Admin 和 Web 两个模块的所有表
-- ========================================

-- ----------------------------------------
-- 用户信息表 (Admin: 用户管理 | Web: 登录注册/用户中心)
-- ----------------------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户id',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `nick_name` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `password` VARCHAR(64) DEFAULT NULL COMMENT '密码',
    `sex` TINYINT DEFAULT 2 COMMENT '0：女 1：男 2：未知',
    `birthday` VARCHAR(20) DEFAULT NULL COMMENT '出生日期',
    `school` VARCHAR(100) DEFAULT NULL COMMENT '学校',
    `personal_introduction` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `join_time` DATETIME DEFAULT NULL COMMENT '加入时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `status` TINYINT DEFAULT 1 COMMENT '0：禁用 1：正常',
    `notice_info` VARCHAR(500) DEFAULT NULL COMMENT '空间公告',
    `total_coin_count` INT DEFAULT 0 COMMENT '硬币总数量',
    `current_coin_count` INT DEFAULT 0 COMMENT '当前硬币数量',
    `theme` INT DEFAULT 0 COMMENT '主题',
    `fans_count` INT DEFAULT 0 COMMENT '粉丝数量',
    `focus_count` INT DEFAULT 0 COMMENT '关注数量',
    `like_count` INT DEFAULT 0 COMMENT '获赞数量',
    `play_count` INT DEFAULT 0 COMMENT '视频播放数量',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ----------------------------------------
-- 分类信息表 (Admin: 分类管理 | Web: 视频分类筛选)
-- ----------------------------------------
DROP TABLE IF EXISTS `category_info`;
CREATE TABLE `category_info` (
    `category_id` INT NOT NULL AUTO_INCREMENT COMMENT '分类id',
    `category_code` VARCHAR(20) DEFAULT NULL COMMENT '分类编码',
    `category_name` VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    `p_category_id` INT DEFAULT 0 COMMENT '父级分类id',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标',
    `background` VARCHAR(255) DEFAULT NULL COMMENT '背景图',
    `sort` INT DEFAULT 0 COMMENT '排序字段',
    PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类信息表';

-- ----------------------------------------
-- 视频信息表 (Admin: 视频管理 | Web: 视频展示/搜索/推荐)
-- ----------------------------------------
DROP TABLE IF EXISTS `video_info`;
CREATE TABLE `video_info` (
    `video_id` VARCHAR(32) NOT NULL COMMENT '视频ID',
    `video_cover` VARCHAR(255) DEFAULT NULL COMMENT '视频封面',
    `video_name` VARCHAR(100) DEFAULT NULL COMMENT '视频名称',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `last_update_time` DATETIME DEFAULT NULL COMMENT '最后更新时间',
    `p_category_id` INT DEFAULT NULL COMMENT '一级分类ID',
    `category_id` INT DEFAULT NULL COMMENT '分类ID',
    `post_type` TINYINT DEFAULT 0 COMMENT '0：自制 1：转载',
    `origin_info` VARCHAR(500) DEFAULT NULL COMMENT '转载源资源说明',
    `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签',
    `introduction` VARCHAR(1000) DEFAULT NULL COMMENT '简介',
    `interaction` VARCHAR(50) DEFAULT NULL COMMENT '互动设置',
    `duration` INT DEFAULT 0 COMMENT '持续时间（秒）',
    `play_count` INT DEFAULT 0 COMMENT '播放数量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数量',
    `danmu_count` INT DEFAULT 0 COMMENT '弹幕数量',
    `comment_count` INT DEFAULT 0 COMMENT '评论数量',
    `coin_count` INT DEFAULT 0 COMMENT '投币数量',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数量',
    `recommend_type` TINYINT DEFAULT 0 COMMENT '是否推荐 0：未推荐 1：已推荐',
    `last_play_time` DATETIME DEFAULT NULL COMMENT '最后播放时间',
    PRIMARY KEY (`video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_recommend_type` (`recommend_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_play_count` (`play_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频信息表';

-- ----------------------------------------
-- 视频文件信息表 (Web: 视频播放/切片)
-- ----------------------------------------
DROP TABLE IF EXISTS `video_info_file`;
CREATE TABLE `video_info_file` (
    `file_id` VARCHAR(32) NOT NULL COMMENT '唯一ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `video_id` VARCHAR(32) DEFAULT NULL COMMENT '视频ID',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    `file_index` INT DEFAULT 0 COMMENT '文件索引',
    `file_size` INT DEFAULT 0 COMMENT '文件大小',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `duration` INT DEFAULT 0 COMMENT '持续时间（秒）',
    PRIMARY KEY (`file_id`),
    KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频文件信息表';

-- ----------------------------------------
-- 审核视频信息表 (Admin: 视频审核 | Web: 视频投稿状态查询)
-- ----------------------------------------
DROP TABLE IF EXISTS `video_info_post`;
CREATE TABLE `video_info_post` (
    `video_id` VARCHAR(32) NOT NULL COMMENT '视频ID',
    `video_cover` VARCHAR(255) DEFAULT NULL COMMENT '视频封面',
    `video_name` VARCHAR(100) DEFAULT NULL COMMENT '视频名称',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `last_update_time` DATETIME DEFAULT NULL COMMENT '最后更新时间',
    `p_category_id` INT DEFAULT NULL COMMENT '一级分类ID',
    `category_id` INT DEFAULT NULL COMMENT '分类ID',
    `status` TINYINT DEFAULT 0 COMMENT '0：转码中 1：转码失败 2：待审核 3：审核成功 4：审核失败',
    `post_type` TINYINT DEFAULT 0 COMMENT '0：自制 1：转载',
    `origin_info` VARCHAR(500) DEFAULT NULL COMMENT '转载源资源说明',
    `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签',
    `introduction` VARCHAR(1000) DEFAULT NULL COMMENT '简介',
    `interaction` VARCHAR(50) DEFAULT NULL COMMENT '互动设置',
    `duration` INT DEFAULT 0 COMMENT '持续时间（秒）',
    PRIMARY KEY (`video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核视频信息表';

-- ----------------------------------------
-- 审核视频文件信息表 (Admin: 审核详情查看)
-- ----------------------------------------
DROP TABLE IF EXISTS `video_info_file_post`;
CREATE TABLE `video_info_file_post` (
    `file_id` VARCHAR(32) NOT NULL COMMENT '唯一ID',
    `upload_id` VARCHAR(32) DEFAULT NULL COMMENT '上传ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `video_id` VARCHAR(32) DEFAULT NULL COMMENT '视频ID',
    `file_index` INT DEFAULT 0 COMMENT '文件索引',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `update_type` TINYINT DEFAULT 0 COMMENT '0：无更新 1：有更新',
    `transfer_result` TINYINT DEFAULT 0 COMMENT '0：转码中 1：转码成功 2：转码失败',
    `duration` INT DEFAULT 0 COMMENT '持续时间（秒）',
    PRIMARY KEY (`file_id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_upload_id` (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核视频文件信息表';

-- ----------------------------------------
-- 视频评论表 (Web: 评论功能)
-- ----------------------------------------
DROP TABLE IF EXISTS `video_comment`;
CREATE TABLE `video_comment` (
    `comment_id` INT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `p_comment_id` INT DEFAULT 0 COMMENT '父级评论ID',
    `video_id` VARCHAR(32) DEFAULT NULL COMMENT '视频ID',
    `video_user_id` VARCHAR(32) DEFAULT NULL COMMENT '视频作者ID',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '回复内容',
    `img_path` VARCHAR(255) DEFAULT NULL COMMENT '图片',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `reply_user_id` VARCHAR(32) DEFAULT NULL COMMENT '回复人ID',
    `top_type` TINYINT DEFAULT 0 COMMENT '0：未置顶 1：已置顶',
    `post_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `like_count` INT DEFAULT 0 COMMENT '喜欢数量',
    `hate_count` INT DEFAULT 0 COMMENT '点赞数量',
    PRIMARY KEY (`comment_id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_p_comment_id` (`p_comment_id`),
    KEY `idx_post_time` (`post_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频评论表';

-- ----------------------------------------
-- 视频弹幕表 (Web: 弹幕功能)
-- ----------------------------------------
DROP TABLE IF EXISTS `video_danmu`;
CREATE TABLE `video_danmu` (
    `danmu_id` INT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `video_id` VARCHAR(32) DEFAULT NULL COMMENT '视频ID',
    `file_id` VARCHAR(32) DEFAULT NULL COMMENT '文件ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `post_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `text` VARCHAR(100) DEFAULT NULL COMMENT '内容',
    `mode` TINYINT DEFAULT 0 COMMENT '展示位置',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '文字颜色',
    `time` INT DEFAULT 0 COMMENT '展示时间',
    PRIMARY KEY (`danmu_id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频弹幕表';

-- ----------------------------------------
-- 用户关注表 (Web: 关注/粉丝功能)
-- ----------------------------------------
DROP TABLE IF EXISTS `user_focus`;
CREATE TABLE `user_focus` (
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `focus_user_id` VARCHAR(32) NOT NULL COMMENT '关注用户ID',
    `focus_time` DATETIME DEFAULT NULL COMMENT '关注时间',
    `focus_type` TINYINT DEFAULT 0 COMMENT '关注类型',
    PRIMARY KEY (`user_id`, `focus_user_id`),
    KEY `idx_focus_user_id` (`focus_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- ----------------------------------------
-- 用户行为表 (Admin: 数据查看 | Web: 点赞/收藏/投币)
-- ----------------------------------------
DROP TABLE IF EXISTS `user_action`;
CREATE TABLE `user_action` (
    `action_id` INT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `video_id` VARCHAR(32) DEFAULT NULL COMMENT '视频ID',
    `video_user_id` VARCHAR(32) DEFAULT NULL COMMENT '视频作者ID',
    `comment_id` INT DEFAULT NULL COMMENT '评论ID',
    `action_type` TINYINT DEFAULT NULL COMMENT '0：评论喜欢 1：评论讨厌 2：视频点赞 3：视频收藏 4：视频投币',
    `action_count` INT DEFAULT 1 COMMENT '数量-（投币可以2）',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `action_time` DATETIME DEFAULT NULL COMMENT '操作时间',
    PRIMARY KEY (`action_id`),
    UNIQUE KEY `uk_user_action` (`user_id`, `video_id`, `comment_id`, `action_type`),
    KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- ----------------------------------------
-- 视频播放历史表 (Web: 播放历史/断点续播)
-- ----------------------------------------
DROP TABLE IF EXISTS `video_play_history`;
CREATE TABLE `video_play_history` (
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `video_id` VARCHAR(32) NOT NULL COMMENT '视频ID',
    `file_index` INT DEFAULT 0 COMMENT '文件索引',
    `last_update_time` DATETIME DEFAULT NULL COMMENT '最后更新时间',
    PRIMARY KEY (`user_id`, `video_id`),
    KEY `idx_last_update_time` (`last_update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频播放历史表';

-- ----------------------------------------
-- 用户消息表 (Web: 消息通知)
-- ----------------------------------------
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message` (
    `message_id` INT NOT NULL AUTO_INCREMENT COMMENT '消息ID自增',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '接收人ID',
    `video_id` VARCHAR(32) DEFAULT NULL COMMENT '视频ID',
    `message_type` TINYINT DEFAULT NULL COMMENT '消息类型',
    `send_user_id` VARCHAR(32) DEFAULT NULL COMMENT '发送人ID',
    `read_type` TINYINT DEFAULT 0 COMMENT '0：未读 1：已读',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `extend_json` TEXT DEFAULT NULL COMMENT '扩展信息',
    PRIMARY KEY (`message_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_read_type` (`read_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息表';

-- ----------------------------------------
-- 数据统计表 (Admin: 数据统计 | Web: 个人数据)
-- ----------------------------------------
DROP TABLE IF EXISTS `statistics_info`;
CREATE TABLE `statistics_info` (
    `statistics_data` VARCHAR(10) NOT NULL COMMENT '统计日期',
    `user_id` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '用户ID',
    `data_type` TINYINT NOT NULL DEFAULT 0 COMMENT '数据统计类型',
    `statistics_count` INT DEFAULT 0 COMMENT '统计数量',
    `action_count` INT DEFAULT 0 COMMENT '操作数量',
    PRIMARY KEY (`statistics_data`, `user_id`, `data_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据统计表';

-- ----------------------------------------
-- 用户视频系列表 (Web: 视频收藏夹/合集)
-- ----------------------------------------
DROP TABLE IF EXISTS `user_video_series`;
CREATE TABLE `user_video_series` (
    `series_id` INT NOT NULL AUTO_INCREMENT COMMENT '列表ID',
    `series_name` VARCHAR(100) DEFAULT NULL COMMENT '列表名称',
    `series_description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `cover` VARCHAR(255) DEFAULT NULL COMMENT '封面',
    PRIMARY KEY (`series_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户视频系列表';

-- ----------------------------------------
-- 用户视频系列视频关联表 (Web: 视频收藏)
-- ----------------------------------------
DROP TABLE IF EXISTS `user_video_series_video`;
CREATE TABLE `user_video_series_video` (
    `series_id` INT NOT NULL COMMENT '列表ID',
    `video_id` VARCHAR(32) NOT NULL COMMENT '视频ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `sort` INT DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`series_id`, `video_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户视频系列视频关联表';

-- ========================================
-- 初始化分类数据
-- ========================================
INSERT INTO `category_info` (`category_id`, `category_code`, `category_name`, `p_category_id`, `sort`) VALUES
(1, 'ANIMATION', '动画', 0, 1),
(2, 'GAME', '游戏', 0, 2),
(3, 'MUSIC', '音乐', 0, 3),
(4, 'TECHNOLOGY', '科技', 0, 4),
(5, 'LIFE', '生活', 0, 5),
(6, 'FAN_ART', '鬼畜', 0, 6),
(7, 'FASHION', '时尚', 0, 7),
(8, 'ADVERTISING', '广告', 0, 8),
(9, 'ENTERTAINMENT', '娱乐', 0, 9),
(10, 'DOCUMENTARY', '影视', 0, 10);
