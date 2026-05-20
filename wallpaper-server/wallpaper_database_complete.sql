-- ============================================
-- 壁纸系统完整数据库脚本
-- 数据库：wallpaper
-- 版本：2.0.0
-- 创建时间：2026-04-10
-- 说明：包含建表、初始数据和性能优化索引
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
DROP DATABASE IF EXISTS wallpaper;
CREATE DATABASE wallpaper DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wallpaper;

-- ============================================
-- 1. 用户表 (users)
-- ============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '角色（user用户、admin管理员）',
  `is_creator` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否支持创作（0否、1是）',
  `wechat_open_id` varchar(100) DEFAULT NULL COMMENT '微信openId',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像URL',
  `follower_count` int NOT NULL DEFAULT 0 COMMENT '粉丝数量',
  `work_count` int NOT NULL DEFAULT 0 COMMENT '作品数量',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_wechat_open_id` (`wechat_open_id`),
  KEY `idx_role` (`role`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 分类表 (categories)
-- ============================================
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `alias` varchar(50) DEFAULT NULL COMMENT '分类别名',
  `icon_path` varchar(500) DEFAULT NULL COMMENT '图标路径',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面URL',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '喜欢人数',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_like_count` (`like_count`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================
-- 3. 作品集表 (works)
-- ============================================
DROP TABLE IF EXISTS `works`;
CREATE TABLE `works` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '作品集ID',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `title` varchar(200) DEFAULT NULL COMMENT '作品集标题',
  `cover_url` varchar(500) NOT NULL COMMENT '封面地址（第一张壁纸）',
  `image_width` int DEFAULT NULL COMMENT '封面图片宽度（像素）',
  `image_height` int DEFAULT NULL COMMENT '封面图片高度（像素）',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '封面缩略图URL',
  `file_format` varchar(10) DEFAULT NULL COMMENT '文件格式（JPG、PNG等）',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `type` int NOT NULL COMMENT '作品类型（1手机壁纸、2平板壁纸、3头像）',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `description` varchar(1000) DEFAULT NULL COMMENT '作品集描述',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `wallpaper_count` int NOT NULL DEFAULT 0 COMMENT '作品数量',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `collect_count` int NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `status` int NOT NULL DEFAULT 0 COMMENT '审核状态（0待审核、1已通过、2拒绝）',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间（审核通过时间）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_user_type` (`user_id`, `type`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_category_type` (`category_id`, `type`),
  KEY `idx_like_count` (`like_count`),
  KEY `idx_collect_count` (`collect_count`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_publish_time` (`publish_time` DESC),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品集表';

-- ============================================
-- 4. 壁纸表 (wallpapers)
-- ============================================
DROP TABLE IF EXISTS `wallpapers`;
CREATE TABLE `wallpapers` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '壁纸ID',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `title` varchar(200) NOT NULL COMMENT '壁纸标题',
  `url` varchar(500) NOT NULL COMMENT '原始图片URL（高清原图）',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '缩略图URL（列表展示用）',
  `image_width` int DEFAULT NULL COMMENT '图片宽度（像素）',
  `image_height` int DEFAULT NULL COMMENT '图片高度（像素）',
  `file_format` varchar(10) DEFAULT NULL COMMENT '图片格式（JPG、PNG等）',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `type` int DEFAULT NULL COMMENT '壁纸类型（1手机壁纸、2平板壁纸、3头像）',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `work_id` bigint DEFAULT NULL COMMENT '所属作品集ID',
  `description` varchar(1000) DEFAULT NULL COMMENT '壁纸描述',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `collect_count` int NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签（JSON数组字符串）',
  `status` int NOT NULL DEFAULT 0 COMMENT '审核状态（0待审核、1已通过、2拒绝）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_work_id` (`work_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_user_type` (`user_id`, `type`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_category_type` (`category_id`, `type`),
  KEY `idx_work_status` (`work_id`, `status`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_download_count` (`download_count`),
  KEY `idx_like_count` (`like_count`),
  KEY `idx_collect_count` (`collect_count`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='壁纸表';

-- ============================================
-- 5. 用户行为表 (user_actions)
-- ============================================
DROP TABLE IF EXISTS `user_actions`;
CREATE TABLE `user_actions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `work_id` bigint DEFAULT NULL COMMENT '作品集ID',
  `wallpaper_id` bigint DEFAULT NULL COMMENT '壁纸ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `action_type` varchar(20) NOT NULL COMMENT '行为类型（like点赞、collect收藏、download下载）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_work_action` (`user_id`, `work_id`, `action_type`, `is_del`),
  UNIQUE KEY `uk_user_wallpaper_action` (`user_id`, `wallpaper_id`, `action_type`, `is_del`),
  UNIQUE KEY `uk_user_category_action` (`user_id`, `category_id`, `action_type`, `is_del`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_work_id` (`work_id`),
  KEY `idx_wallpaper_id` (`wallpaper_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为表';

-- ============================================
-- 6. 举报表 (reports)
-- ============================================
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `user_id` bigint NOT NULL COMMENT '举报用户ID',
  `wallpaper_id` bigint DEFAULT NULL COMMENT '壁纸ID',
  `report_type` varchar(20) NOT NULL COMMENT '举报类型（inappropriate不合适、copyright版权、spam垃圾、quality质量、other其他）',
  `reason` varchar(500) NOT NULL COMMENT '举报原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_wallpaper_id` (`wallpaper_id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ============================================
-- 初始数据
-- ============================================

-- 用户数据
INSERT INTO `users` VALUES 
(1, 'user', 1, NULL, 'lin', '$2a$10$JXP.yXR6ZWTXztBo8g8vrOPKXOM8WK2CQbFIwyIr5REE2xclPNqh6', '双木同学', 'https://avatars.githubusercontent.com/u/25338843', 0, 0, NULL, '12228488663', 'i8lv9r.k18@126.com', '2026-03-28 12:27:04', '2026-03-28 12:27:04', 0),
(2, 'admin', 1, NULL, 'admin', '$2a$10$Tw3jvpmgYeLDFn26Z6Y6ludpSV/vgg1XYaZYAiD7bKIJ7luYBapOa', '管理员', 'https://avatars.githubusercontent.com/u/25338843', 0, 1, NULL, '12228488663', 'i8lv9r.k18@126.com', '2026-03-28 12:27:30', '2026-03-28 12:27:30', 0);

-- 分类数据
INSERT INTO `categories` VALUES 
(1, '可爱卡通', 'cute', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/7f419ce9-8305-42f2-a887-ff08bd71393b.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/7f419ce9-8305-42f2-a887-ff08bd71393b.jpg?x-oss-disposition=inline', 0, 30, '2026-03-28 12:36:28', '2026-03-28 12:36:28', 0),
(2, '美食甜品', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/ebc4aebc-f9f2-4e37-840b-075cfe727295.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/ebc4aebc-f9f2-4e37-840b-075cfe727295.jpg?x-oss-disposition=inline', 0, 11, '2026-03-28 12:36:54', '2026-03-28 12:36:54', 0),
(3, '赛博朋克', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/98196470-3abd-4858-b996-7e6c61697f85.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/98196470-3abd-4858-b996-7e6c61697f85.jpg?x-oss-disposition=inline', 0, 18, '2026-03-28 12:38:31', '2026-03-28 12:38:31', 0),
(4, '风景自然', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/1136dc92-98d2-4f9b-8790-3b26ec2fc8ce.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/1136dc92-98d2-4f9b-8790-3b26ec2fc8ce.jpg?x-oss-disposition=inline', 0, 9, '2026-03-28 12:40:05', '2026-03-28 12:40:05', 0),
(5, '影视综艺', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/e2e55e0b-7500-4868-9c83-bafaf195e834.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/e2e55e0b-7500-4868-9c83-bafaf195e834.jpg?x-oss-disposition=inline', 0, 3, '2026-03-28 12:41:55', '2026-03-28 12:41:55', 0);

-- -- 作品集数据
-- INSERT INTO `works` VALUES 
-- (1, 1, '111', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/252eebd9-217e-4eae-a56b-fd0e232ee97c.jpg?x-oss-disposition=inline', 1080, 2340, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/252eebd9-217e-4eae-a56b-fd0e232ee97c.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'JPEG', 440926, 1, 1, '', 2, 2, 0, 0, 0, 0, 1, NULL, '2026-03-28 12:49:02', '2026-03-28 12:49:02', '2026-03-28 12:49:02', 0);

-- -- 壁纸数据
-- INSERT INTO `wallpapers` VALUES 
-- (1, 0, '111', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/252eebd9-217e-4eae-a56b-fd0e232ee97c.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/252eebd9-217e-4eae-a56b-fd0e232ee97c.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1080, 2340, 'JPEG', 440926, 1, 1, 1, NULL, 2, 0, 0, 0, 0, NULL, 0, '2026-03-28 12:49:02', '2026-03-28 12:49:02', 0),
-- (2, 0, '111', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/0be8e93a-16b0-45f5-b05f-c9981fe2b68a.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/0be8e93a-16b0-45f5-b05f-c9981fe2b68a.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1000, 2167, 'JPEG', 93300, 1, 1, 1, NULL, 2, 0, 0, 0, 0, NULL, 0, '2026-03-28 12:49:02', '2026-03-28 12:49:02', 0);

-- ============================================
-- 性能优化索引
-- ============================================

-- 壁纸表索引优化
CREATE INDEX idx_wallpaper_category_type_del_time ON wallpapers(category_id, type, is_del, create_time DESC);
CREATE INDEX idx_wallpaper_user_del_time ON wallpapers(user_id, is_del, create_time DESC);
CREATE INDEX idx_wallpaper_status_del_time ON wallpapers(status, is_del, create_time DESC);
CREATE INDEX idx_wallpaper_hot ON wallpapers(is_del, status, collect_count DESC);
CREATE INDEX idx_wallpaper_download ON wallpapers(is_del, status, download_count DESC);

-- 作品集表索引优化
CREATE INDEX idx_work_user_status_del_time ON works(user_id, status, is_del, create_time DESC);
CREATE INDEX idx_work_category_type_status ON works(category_id, type, status, is_del);

-- 用户行为表索引优化
CREATE INDEX idx_action_user_type_time ON user_actions(user_id, action_type, create_time DESC);

-- 分类表索引优化
CREATE INDEX idx_category_hot ON categories(is_del, like_count DESC);

-- 更新表统计信息
ANALYZE TABLE wallpapers;
ANALYZE TABLE works;
ANALYZE TABLE user_actions;
ANALYZE TABLE categories;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 使用说明
-- ============================================
-- 1. 此脚本会删除现有数据库并重新创建，请谨慎使用
-- 2. 默认用户：
--    - 普通用户：用户名 lin，密码 123456
--    - 管理员：用户名 admin，密码 admin123
-- 3. 包含5个分类：可爱卡通、美食甜品、赛博朋克、风景自然、影视综艺
-- 4. 包含1个作品集和2张壁纸作为示例数据
-- ============================================
