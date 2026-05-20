/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : 127.0.0.1:3306
 Source Schema         : wallpaper

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 17/04/2026 15:09:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for authors
-- ----------------------------
DROP TABLE IF EXISTS `authors`;
CREATE TABLE `authors`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `follower_count` int NULL DEFAULT NULL,
  `is_del` bit(1) NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `wallpaper_count` int NULL DEFAULT NULL,
  `work_count` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of authors
-- ----------------------------

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `alias` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类别名',
  `icon_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标路径',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面URL',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '喜欢人数',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name`) USING BTREE,
  INDEX `idx_sort_order`(`sort_order`) USING BTREE,
  INDEX `idx_like_count`(`like_count`) USING BTREE,
  INDEX `idx_is_del`(`is_del`) USING BTREE,
  INDEX `idx_category_hot`(`is_del`, `like_count`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '可爱卡通', 'cute', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/035abfaf-09d3-4583-883a-df6241eeb969.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/035abfaf-09d3-4583-883a-df6241eeb969.jpg?x-oss-disposition=inline', 0, 9, '2026-04-17 12:55:58', '2026-04-17 13:53:36', 0);
INSERT INTO `categories` VALUES (2, '美食甜品', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/db82a5f9-5ffb-4687-9296-242453b9e224.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/db82a5f9-5ffb-4687-9296-242453b9e224.jpg?x-oss-disposition=inline', 0, 6, '2026-04-17 12:56:46', '2026-04-17 13:53:44', 0);
INSERT INTO `categories` VALUES (3, '赛博朋克', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/179a0dac-463b-49a9-9832-4abc8a9ab421.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/179a0dac-463b-49a9-9832-4abc8a9ab421.jpg?x-oss-disposition=inline', 0, 2, '2026-04-17 12:59:31', '2026-04-17 13:54:13', 0);
INSERT INTO `categories` VALUES (4, '动物萌宠', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/d17fab6c-d41b-4f3c-ad1d-a9740067e918.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/d17fab6c-d41b-4f3c-ad1d-a9740067e918.jpg?x-oss-disposition=inline', 0, 6, '2026-04-17 12:59:47', '2026-04-17 13:53:59', 0);
INSERT INTO `categories` VALUES (5, '文字', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/23d4746d-519c-4436-a72f-8dce31207d1b.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/23d4746d-519c-4436-a72f-8dce31207d1b.jpg?x-oss-disposition=inline', 0, 0, '2026-04-17 13:00:34', '2026-04-17 13:00:34', 0);
INSERT INTO `categories` VALUES (6, '纯色', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/e36f3f15-a415-4a19-95cb-146449b69a43.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/e36f3f15-a415-4a19-95cb-146449b69a43.jpg?x-oss-disposition=inline', 0, 7, '2026-04-17 13:01:00', '2026-04-17 13:33:31', 0);
INSERT INTO `categories` VALUES (7, '真人', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/b6d544f9-e876-4786-bc67-07e28d7bd7fd.png?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/b6d544f9-e876-4786-bc67-07e28d7bd7fd.png?x-oss-disposition=inline', 0, 0, '2026-04-17 13:07:23', '2026-04-17 13:32:41', 0);
INSERT INTO `categories` VALUES (8, '情侣', '', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/4e1b0909-9077-4ad5-8663-8815e87ad502.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/4e1b0909-9077-4ad5-8663-8815e87ad502.jpg?x-oss-disposition=inline', 0, 4, '2026-04-17 13:08:57', '2026-04-17 13:53:49', 0);

-- ----------------------------
-- Table structure for reports
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `user_id` bigint NOT NULL COMMENT '举报用户ID',
  `wallpaper_id` bigint NULL DEFAULT NULL COMMENT '壁纸ID',
  `report_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报类型（inappropriate不合适、copyright版权、spam垃圾、quality质量、other其他）',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_wallpaper_id`(`wallpaper_id`) USING BTREE,
  INDEX `idx_report_type`(`report_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_is_del`(`is_del`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '举报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reports
-- ----------------------------

-- ----------------------------
-- Table structure for user_actions
-- ----------------------------
DROP TABLE IF EXISTS `user_actions`;
CREATE TABLE `user_actions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `work_id` bigint NULL DEFAULT NULL COMMENT '作品集ID',
  `wallpaper_id` bigint NULL DEFAULT NULL COMMENT '壁纸ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `action_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行为类型（like点赞、collect收藏、download下载）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_work_action`(`user_id`, `work_id`, `action_type`, `is_del`) USING BTREE,
  UNIQUE INDEX `uk_user_wallpaper_action`(`user_id`, `wallpaper_id`, `action_type`, `is_del`) USING BTREE,
  UNIQUE INDEX `uk_user_category_action`(`user_id`, `category_id`, `action_type`, `is_del`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_work_id`(`work_id`) USING BTREE,
  INDEX `idx_wallpaper_id`(`wallpaper_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_action_type`(`action_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_is_del`(`is_del`) USING BTREE,
  INDEX `idx_action_user_type_time`(`user_id`, `action_type`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户行为表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_actions
-- ----------------------------
INSERT INTO `user_actions` VALUES (2, 3, 4, NULL, NULL, 'like', '2026-04-17 13:15:32', 0);
INSERT INTO `user_actions` VALUES (5, 1, NULL, 6, NULL, 'like', '2026-04-17 13:22:20', 0);
INSERT INTO `user_actions` VALUES (8, 3, NULL, 6, NULL, 'like', '2026-04-17 13:25:42', 0);
INSERT INTO `user_actions` VALUES (9, 3, NULL, 2, NULL, 'like', '2026-04-17 13:25:45', 0);
INSERT INTO `user_actions` VALUES (10, 3, NULL, 1, NULL, 'like', '2026-04-17 13:25:47', 0);
INSERT INTO `user_actions` VALUES (11, 1, 4, NULL, NULL, 'like', '2026-04-17 13:26:17', 0);
INSERT INTO `user_actions` VALUES (12, 1, NULL, 5, NULL, 'download', '2026-04-17 13:41:31', 0);
INSERT INTO `user_actions` VALUES (13, 1, NULL, 9, NULL, 'download', '2026-04-17 13:50:48', 0);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '角色（user用户、admin管理员）',
  `is_creator` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否支持创作（0否、1是）',
  `wechat_open_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信openId',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `follower_count` int NOT NULL DEFAULT 0 COMMENT '粉丝数量',
  `work_count` int NOT NULL DEFAULT 0 COMMENT '作品数量',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_wechat_open_id`(`wechat_open_id`) USING BTREE,
  INDEX `idx_role`(`role`) USING BTREE,
  INDEX `idx_is_del`(`is_del`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'user', 1, NULL, 'lin', '$2a$10$JXP.yXR6ZWTXztBo8g8vrOPKXOM8WK2CQbFIwyIr5REE2xclPNqh6', '双木同学', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/8f6ee676-3f91-4b11-8d09-3cb298792419.jpg?x-oss-disposition=inline', 0, 1, 'hihihi', '12228488663', 'i8lv9r.k18@126.com', '2026-03-28 12:27:04', '2026-04-17 13:40:48', 0);
INSERT INTO `users` VALUES (2, 'admin', 1, NULL, 'admin', '$2a$10$Tw3jvpmgYeLDFn26Z6Y6ludpSV/vgg1XYaZYAiD7bKIJ7luYBapOa', '管理员', 'https://avatars.githubusercontent.com/u/25338843', 0, 4, NULL, '12228488663', 'i8lv9r.k18@126.com', '2026-03-28 12:27:30', '2026-03-28 12:27:30', 0);
INSERT INTO `users` VALUES (3, 'user', 1, 'o2hB41-VpaWnFSgAvgogdD2iQwHI', 'wx_1776402764795', NULL, '夏天', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/a8670cdb-05b5-4577-b3ee-319b1f6a9b2c.jpg?x-oss-disposition=inline', 0, 1, '你好你好', NULL, NULL, '2026-04-17 13:12:45', '2026-04-17 13:56:50', 0);

-- ----------------------------
-- Table structure for wallpapers
-- ----------------------------
DROP TABLE IF EXISTS `wallpapers`;
CREATE TABLE `wallpapers`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '壁纸ID',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '壁纸标题',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始图片URL（高清原图）',
  `thumbnail_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图URL（列表展示用）',
  `image_width` int NULL DEFAULT NULL COMMENT '图片宽度（像素）',
  `image_height` int NULL DEFAULT NULL COMMENT '图片高度（像素）',
  `file_format` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片格式（JPG、PNG等）',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `type` int NULL DEFAULT NULL COMMENT '壁纸类型（1手机壁纸、2平板壁纸、3头像）',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `work_id` bigint NULL DEFAULT NULL COMMENT '所属作品集ID',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '壁纸描述',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `collect_count` int NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签（JSON数组字符串）',
  `status` int NOT NULL DEFAULT 0 COMMENT '审核状态（0待审核、1已通过、2拒绝）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_work_id`(`work_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_user_status`(`user_id`, `status`) USING BTREE,
  INDEX `idx_user_type`(`user_id`, `type`) USING BTREE,
  INDEX `idx_category_status`(`category_id`, `status`) USING BTREE,
  INDEX `idx_category_type`(`category_id`, `type`) USING BTREE,
  INDEX `idx_work_status`(`work_id`, `status`) USING BTREE,
  INDEX `idx_view_count`(`view_count`) USING BTREE,
  INDEX `idx_download_count`(`download_count`) USING BTREE,
  INDEX `idx_like_count`(`like_count`) USING BTREE,
  INDEX `idx_collect_count`(`collect_count`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_is_del`(`is_del`) USING BTREE,
  INDEX `idx_wallpaper_category_type_del_time`(`category_id`, `type`, `is_del`, `create_time`) USING BTREE,
  INDEX `idx_wallpaper_user_del_time`(`user_id`, `is_del`, `create_time`) USING BTREE,
  INDEX `idx_wallpaper_status_del_time`(`status`, `is_del`, `create_time`) USING BTREE,
  INDEX `idx_wallpaper_hot`(`is_del`, `status`, `collect_count`) USING BTREE,
  INDEX `idx_wallpaper_download`(`is_del`, `status`, `download_count`) USING BTREE,
  CONSTRAINT `FK1dgdy2g8wrchwjs0ktaw772mk` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '壁纸表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wallpapers
-- ----------------------------
INSERT INTO `wallpapers` VALUES (1, 3, '111', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/42a2e28d-2866-40cb-9413-663151978b77.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/42a2e28d-2866-40cb-9413-663151978b77.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1280, 2781, 'JPEG', 139160, 1, 1, 1, NULL, 2, 0, 0, 1, 0, NULL, 0, '2026-04-17 13:04:58', '2026-04-17 13:25:46', 0);
INSERT INTO `wallpapers` VALUES (2, 0, '111', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/cb7956b0-85df-422d-9433-503cc8b353e5.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/cb7956b0-85df-422d-9433-503cc8b353e5.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1280, 2781, 'JPEG', 146471, 1, 1, 1, NULL, 2, 0, 0, 1, 0, NULL, 0, '2026-04-17 13:04:58', '2026-04-17 13:25:45', 0);
INSERT INTO `wallpapers` VALUES (3, 0, '111', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/5b80a119-9ec9-4921-a140-35c7b69b2f86.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/5b80a119-9ec9-4921-a140-35c7b69b2f86.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1280, 2772, 'JPEG', 196846, 1, 1, 1, NULL, 2, 0, 0, 0, 0, NULL, 0, '2026-04-17 13:04:58', '2026-04-17 13:04:58', 0);
INSERT INTO `wallpapers` VALUES (4, 0, '222', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/fa6733bf-d6d0-48d5-8b66-2d9c372ba3cd.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/fa6733bf-d6d0-48d5-8b66-2d9c372ba3cd.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 2048, 2731, 'JPEG', 291239, 2, 2, 2, NULL, 2, 0, 0, 0, 0, NULL, 0, '2026-04-17 13:05:19', '2026-04-17 13:05:19', 0);
INSERT INTO `wallpapers` VALUES (5, 1, '222', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/8470e188-80b0-43bd-9132-98d0320fb49e.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/8470e188-80b0-43bd-9132-98d0320fb49e.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1080, 1439, 'JPEG', 82518, 2, 2, 2, NULL, 2, 0, 1, 0, 0, NULL, 0, '2026-04-17 13:05:19', '2026-04-17 13:41:31', 0);
INSERT INTO `wallpapers` VALUES (6, 2, '333', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/0a1cad68-f07c-4d8f-ac8c-e9eb3ccaf619.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/0a1cad68-f07c-4d8f-ac8c-e9eb3ccaf619.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1080, 1080, 'JPEG', 72048, 3, 1, 3, NULL, 2, 0, 0, 2, 0, NULL, 0, '2026-04-17 13:05:59', '2026-04-17 13:25:42', 0);
INSERT INTO `wallpapers` VALUES (7, 1, NULL, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/2bf7ebcc-baea-4274-bf20-a219e50853e3.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/2bf7ebcc-baea-4274-bf20-a219e50853e3.jpg?x-oss-disposition=inline', 1024, 1024, 'jpg', 0, 3, 7, 4, NULL, 3, 0, 0, 0, 0, NULL, 0, '2026-04-17 13:14:15', '2026-04-17 13:14:15', 0);
INSERT INTO `wallpapers` VALUES (8, 1, NULL, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/059ee8c6-c7fa-4ec4-a7b2-1ca8390c902e.jpg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/059ee8c6-c7fa-4ec4-a7b2-1ca8390c902e.jpg?x-oss-disposition=inline', 1024, 1024, 'jpg', 0, 3, 7, 4, NULL, 3, 0, 0, 0, 0, NULL, 0, '2026-04-17 13:14:15', '2026-04-17 13:14:15', 0);
INSERT INTO `wallpapers` VALUES (9, 2, NULL, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/tablet/d6c52321-adf1-41fe-ad10-92f602a0437d.jpeg?x-oss-disposition=inline', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/tablet/d6c52321-adf1-41fe-ad10-92f602a0437d.jpeg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 1280, 900, 'WEBP', 215855, 2, 1, 5, NULL, 1, 0, 1, 0, 0, NULL, 0, '2026-04-17 13:50:01', '2026-04-17 13:50:47', 0);

-- ----------------------------
-- Table structure for works
-- ----------------------------
DROP TABLE IF EXISTS `works`;
CREATE TABLE `works`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '作品集ID',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '作品集标题',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '封面地址（第一张壁纸）',
  `image_width` int NULL DEFAULT NULL COMMENT '封面图片宽度（像素）',
  `image_height` int NULL DEFAULT NULL COMMENT '封面图片高度（像素）',
  `thumbnail_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面缩略图URL',
  `file_format` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件格式（JPG、PNG等）',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `type` int NOT NULL COMMENT '作品类型（1手机壁纸、2平板壁纸、3头像）',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作品集描述',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `wallpaper_count` int NOT NULL DEFAULT 0 COMMENT '作品数量',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `collect_count` int NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `status` int NOT NULL DEFAULT 0 COMMENT '审核状态（0待审核、1已通过、2拒绝）',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间（审核通过时间）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否、1是）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_user_status`(`user_id`, `status`) USING BTREE,
  INDEX `idx_user_type`(`user_id`, `type`) USING BTREE,
  INDEX `idx_category_status`(`category_id`, `status`) USING BTREE,
  INDEX `idx_category_type`(`category_id`, `type`) USING BTREE,
  INDEX `idx_like_count`(`like_count`) USING BTREE,
  INDEX `idx_collect_count`(`collect_count`) USING BTREE,
  INDEX `idx_view_count`(`view_count`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_publish_time`(`publish_time`) USING BTREE,
  INDEX `idx_is_del`(`is_del`) USING BTREE,
  INDEX `idx_work_user_status_del_time`(`user_id`, `status`, `is_del`, `create_time`) USING BTREE,
  INDEX `idx_work_category_type_status`(`category_id`, `type`, `status`, `is_del`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品集表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of works
-- ----------------------------
INSERT INTO `works` VALUES (1, 1, '111', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/42a2e28d-2866-40cb-9413-663151978b77.jpg?x-oss-disposition=inline', 1280, 2781, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/42a2e28d-2866-40cb-9413-663151978b77.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'JPEG', 139160, 1, 1, '', 2, 3, 0, 0, 0, 0, 1, NULL, '2026-04-17 13:04:58', NULL, '2026-04-17 13:04:58', 0);
INSERT INTO `works` VALUES (2, 1, '222', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/fa6733bf-d6d0-48d5-8b66-2d9c372ba3cd.jpg?x-oss-disposition=inline', 2048, 2731, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/fa6733bf-d6d0-48d5-8b66-2d9c372ba3cd.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'JPEG', 291239, 2, 2, '', 2, 2, 0, 0, 0, 0, 1, NULL, '2026-04-17 13:05:19', NULL, '2026-04-17 13:05:19', 0);
INSERT INTO `works` VALUES (3, 1, '333', 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/0a1cad68-f07c-4d8f-ac8c-e9eb3ccaf619.jpg?x-oss-disposition=inline', 1080, 1080, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/phone/0a1cad68-f07c-4d8f-ac8c-e9eb3ccaf619.jpg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'JPEG', 72048, 3, 1, '', 2, 1, 0, 0, 0, 0, 1, NULL, '2026-04-17 13:05:59', NULL, '2026-04-17 13:05:59', 0);
INSERT INTO `works` VALUES (4, 2, NULL, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/2bf7ebcc-baea-4274-bf20-a219e50853e3.jpg?x-oss-disposition=inline', 1024, 1024, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/avatars/2bf7ebcc-baea-4274-bf20-a219e50853e3.jpg?x-oss-disposition=inline', 'jpg', 0, 3, 7, NULL, 3, 2, 2, 0, 0, 0, 1, NULL, '2026-04-17 13:14:15', '2026-04-17 13:14:28', '2026-04-17 13:26:16', 0);
INSERT INTO `works` VALUES (5, 2, NULL, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/tablet/d6c52321-adf1-41fe-ad10-92f602a0437d.jpeg?x-oss-disposition=inline', 1280, 900, 'https://wallpaper-project.oss-cn-beijing.aliyuncs.com/tablet/d6c52321-adf1-41fe-ad10-92f602a0437d.jpeg?x-oss-process=image/resize,w_300,h_300&x-oss-disposition=inline', 'WEBP', 215855, 2, 1, NULL, 1, 1, 0, 0, 0, 0, 1, NULL, '2026-04-17 13:50:01', '2026-04-17 13:50:12', '2026-04-17 13:50:01', 0);

SET FOREIGN_KEY_CHECKS = 1;
