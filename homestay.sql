/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80200
 Source Host           : localhost:3306
 Source Schema         : homestay

 Target Server Type    : MySQL
 Target Server Version : 80200
 File Encoding         : 65001

 Date: 31/07/2025 14:48:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_admin
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin`;
CREATE TABLE `tb_admin`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_admin
-- ----------------------------
INSERT INTO `tb_admin` VALUES (1, 'admin', '123456');
INSERT INTO `tb_admin` VALUES (2, 'root', '265120');
INSERT INTO `tb_admin` VALUES (3, 'Bruce', '123456');
INSERT INTO `tb_admin` VALUES (4, 'Bruce', '123456');

-- ----------------------------
-- Table structure for tb_guest
-- ----------------------------
DROP TABLE IF EXISTS `tb_guest`;
CREATE TABLE `tb_guest`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(21) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `sex` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `card` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `enterTime` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `exitTime` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `h_Type` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `num` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_num`(`num` ASC) USING BTREE,
  INDEX `idx_sex`(`sex` ASC) USING BTREE,
  INDEX `idx_card`(`card` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_h_type`(`h_Type` ASC) USING BTREE,
  INDEX `idx_vip_identity`(`name` ASC, `sex` ASC, `card` ASC, `phone` ASC) USING BTREE,
  INDEX `idx_home_type_num`(`h_Type` ASC, `num` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_guest
-- ----------------------------
INSERT INTO `tb_guest` VALUES (1, 'Crazy_Diamond', '男', '341125198809021234', '18881689888', '2025-07-29-18:36:30', '2025-08-21T18:35', '双床终点房（四小时）', 307);
INSERT INTO `tb_guest` VALUES (2, 'Crazy_Diamond', '男', '341125198809021234', '18881689888', '2025-07-29-18:55:28', '2025-08-28T18:55', '豪华水床房', 406);

-- ----------------------------
-- Table structure for tb_home
-- ----------------------------
DROP TABLE IF EXISTS `tb_home`;
CREATE TABLE `tb_home`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `num` int NOT NULL,
  `h_Type` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `price` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `state` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `file` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `text` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_home_type_num`(`h_Type` ASC, `num` ASC) USING BTREE,
  INDEX `num`(`num` ASC) USING BTREE,
  INDEX `h_type`(`h_Type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_home
-- ----------------------------
INSERT INTO `tb_home` VALUES (1, 302, '大床钟点房（三小时）', '50', '空房', '/upload/f5eb1d96-2f48-4fb9-9361-ec99866389da.jpeg', '急促\r\n                        \r\n                    \r\n                    \r\n                    \r\n                    \r\n                    \r\n                    \r\n                    \r\n                    \r\n                    \r\n                    \r\n     ');
INSERT INTO `tb_home` VALUES (2, 303, '双床终点房（三小时）', '60', '空房', '/upload/9f13ba9b-004c-4b2d-8b1e-ff66a5c4a7a6.png', '急促\r\n                        \r\n                    \r\n                    \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (3, 304, '双床终点房（四小时）', '70', '未打扫', '/0a8c13f3-30f4-4796-bcf6-0c8b447b7a84_屏幕截图 2025-07-21 090045.png', '急促\r\n                        \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (4, 305, '大床钟点房（四小时）', '60', '空房', '/upload/1bd7b7e6-c002-4e89-a795-7dc84499ee89.png', '急促\r\n                        \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (5, 306, '大床钟点房（四小时）', '70', '空房', '/upload/d5006f31-4a3c-46ef-a911-1f3bd62841a9.png', '急促\r\n                        \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (6, 307, '双床终点房（四小时）', '60', '已入住', '/upload/378ad38b-0b50-41b6-8b61-c5e4b0859096.png', '稳健\r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (7, 308, '双床终点房（四小时）', '70', '空房', '/upload/3b2802f9-e800-407a-a3d6-a7a45add3ebb.png', '稳健\r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (8, 309, '大床钟点房（三小时）', '60', '空房', '/upload/1a465054-8e8e-4345-bdd8-a5f48343dce9.png', '稳健\r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (9, 310, '双床终点房（三小时）', '70', '空房', '/upload/7b1820b0-a623-4e34-b9cd-1b8310a51cbc.png', '时尚\r\n                        \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (10, 401, '普通三人间', '130', '空房', '/upload/67a40315-5bc4-4541-ad00-75df7c227496.png', '路途\r\n                        \r\n                    ');
INSERT INTO `tb_home` VALUES (11, 402, '高级三人间', '160', '未打扫', '/upload/cdb93d8c-a4ec-4195-b1e2-33e8978f0630.png', '路途\r\n                        \r\n                    \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (12, 403, '高级双床房', '180', '空房', '/upload/e7ec60a7-70bf-4fc3-8328-acb635641190.png', '路途\r\n                        \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (13, 404, '豪华双床房', '190', '空房', '/upload/ef424d17-18cf-4e6b-b8e4-c7cd1e3036f1.jpeg', '路途\r\n                        \r\n                    \r\n                    \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (14, 405, '豪华双床房', '180', '空房', '/upload/c9d9b084-3dc4-4404-b89f-f432d18052c6.png', '路途\r\n                        \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (15, 406, '豪华水床房', '260', '已入住', '/upload/ba7da68c-e462-45e8-be15-1da2faeb1b2e.png', '水润\r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (16, 407, '高级电脑房', '220', '空房', '/upload/91dc24cb-ec5f-4c0b-babc-5687b08a1bed.png', 'windows\r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (17, 410, '标准套房', '230', '空房', '/upload/8012943c-cd01-43ee-bf5f-4993c491f305.png', '套房\r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (18, 502, '普通大床房', '220', '未打扫', '/upload/fac9595f-1608-44d5-900e-11d3ae3e4f0e.jpeg', '大床\r\n                    \r\n                    \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (19, 505, '豪华大床房', '300', '未打扫', '/upload/550aea92-94b3-465b-a176-1599a8232b17.jpeg', '请填写房间相关信息......二\r\n                        \r\n                    \r\n                    \r\n                    \r\n                    ');
INSERT INTO `tb_home` VALUES (20, 112, '豪华大床房', '123', '未打扫', '/upload/be9ee50c-d766-4b9d-b12e-1a7bb5db43bb.png', '带派');

-- ----------------------------
-- Table structure for tb_vip
-- ----------------------------
DROP TABLE IF EXISTS `tb_vip`;
CREATE TABLE `tb_vip`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `sex` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `card` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `v_type` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `startTime` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `endTime` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `createtime` datetime NULL DEFAULT NULL,
  `updatetime` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_vip
-- ----------------------------
INSERT INTO `tb_vip` VALUES (1, 'Crazy_Diamond', '男', '341125198809021234', '18881689888', '初级会员', '2025-07-26', '2025-08-01', '2025-07-26 11:11:32', '2025-07-29 01:08:43');
INSERT INTO `tb_vip` VALUES (2, 'ACT-3', '男', '511521200404194056', '13458748731', '高级会员', '2025-07-28', '2025-07-30', '2025-07-28 13:55:57', '2025-07-28 16:53:54');
INSERT INTO `tb_vip` VALUES (3, 'Kill_Queen', '女', '511521200404194056', '13458748731', '高级会员', '2025-07-28', '2025-08-01', '2025-07-28 14:00:35', '2025-07-29 01:08:53');
INSERT INTO `tb_vip` VALUES (4, 'The_World', '男', '511521200404194056', '13458748731', '高级会员', '2025-07-28', '2025-08-08', '2025-07-28 14:05:55', '2025-07-29 01:09:01');
INSERT INTO `tb_vip` VALUES (5, 'Gold_Exeprence', '男', '511521200404194056', '13458748731', '高级会员', '2025-07-28', '2025-07-30', '2025-07-28 14:08:41', '2025-07-29 01:09:10');
INSERT INTO `tb_vip` VALUES (6, 'D4C', '男', '511521200404194056', '13458748731', '高级会员', '2025-07-28', '2025-07-31', '2025-07-28 14:15:52', '2025-07-28 14:15:52');
INSERT INTO `tb_vip` VALUES (7, 'Ston_Free', '女', '511521200404194056', '13458748731', '高级会员', '2025-07-28', '2025-07-30', '2025-07-28 14:18:50', '2025-07-29 01:09:19');
INSERT INTO `tb_vip` VALUES (8, 'Star_Platinum', '男', '511521200404194506', '13458748731', '高级会员', '2025-07-28', '2025-08-01', '2025-07-28 14:20:15', '2025-07-29 01:09:27');
INSERT INTO `tb_vip` VALUES (9, 'Heaven\'s_Door', '男', '511521200404194056', '13458748731', '高级会员', '2025-07-28', '2025-08-10', '2025-07-28 14:23:49', '2025-07-29 01:09:39');
INSERT INTO `tb_vip` VALUES (10, 'Blue_Eyes_White_Dragon', '男', '341125198709080765', '18798765467', '初级会员', '2025-07-28', '2025-08-09', '2025-07-28 16:57:43', '2025-07-29 01:09:53');
INSERT INTO `tb_vip` VALUES (11, 'Neo_Blue_Eyes_Ultimate_Dragon', '男', '341125198708165451', '17898767684', '初级会员', '2025-07-28', '2025-07-31', '2025-07-28 16:58:32', '2025-07-29 01:10:09');
INSERT INTO `tb_vip` VALUES (12, 'King_Crimson', '男', '511521188807234056', '13458748731', '高级会员', '2025-07-28', '2025-08-10', '2025-07-28 18:04:42', '2025-07-29 01:10:17');

SET FOREIGN_KEY_CHECKS = 1;
