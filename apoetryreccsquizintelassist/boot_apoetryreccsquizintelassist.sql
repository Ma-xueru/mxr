-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: r8479
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `username` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `role` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'2026-01-17 03:35:42','admin','admin','管理员');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat`
--

DROP TABLE IF EXISTS `chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `userid` bigint NOT NULL COMMENT '用户id',
  `adminid` bigint DEFAULT NULL COMMENT '管理员id',
  `ask` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '提问',
  `reply` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '回复',
  `isreply` int DEFAULT NULL COMMENT '是否回复',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '昵称',
  `zhaopian` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '照片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1773646270602 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='在线客服';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat`
--

LOCK TABLES `chat` WRITE;
/*!40000 ALTER TABLE `chat` DISABLE KEYS */;
INSERT INTO `chat` VALUES (1773644308614,'2026-03-16 06:58:30',76,NULL,'1+1=',NULL,0,'古诗词用户6','file/student6.jpg'),(1773644308615,'2026-03-16 06:58:30',76,NULL,'在常规数学运算里，1 + 1 = 2 。不过在某些特殊情境下，答案可能不同，比如二进制中1+1 = 10 。这里按常见数学规则，答案是2。 ',NULL,1,NULL,NULL),(1773644940118,'2026-03-16 07:09:01',76,NULL,'1-1=',NULL,0,'古诗词用户6','file/student6.jpg'),(1773644940119,'2026-03-16 07:09:01',76,NULL,'1 - 1 = 0',NULL,1,NULL,NULL),(1773646270600,'2026-03-16 07:31:13',76,NULL,'讲解下数据结构的学习方法',NULL,0,'古诗词用户6','file/student6.jpg'),(1773646270601,'2026-03-16 07:31:13',76,NULL,'理解基本概念，如线性表、树等。结合代码实现加深理解，可用 Python 等语言。多做习题巩固，分析经典算法案例。借助可视化工具辅助，还可与他人交流分享学习心得。 ',NULL,1,NULL,NULL);
/*!40000 ALTER TABLE `chat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classinfo`
--

DROP TABLE IF EXISTS `classinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classinfo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `grade` varchar(200) DEFAULT NULL COMMENT '年级',
  `classname` varchar(200) DEFAULT NULL COMMENT '班级名称',
  `headteacher` varchar(200) DEFAULT NULL COMMENT '班主任',
  `studentcount` int DEFAULT '0' COMMENT '学生人数',
  `classdesc` longtext COMMENT '班级说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=307 DEFAULT CHARSET=utf8mb3 COMMENT='班级管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classinfo`
--

LOCK TABLES `classinfo` WRITE;
/*!40000 ALTER TABLE `classinfo` DISABLE KEYS */;
INSERT INTO `classinfo` VALUES (301,'2026-01-17 03:35:42','一年级','一年级1班','教师姓名1',3,'一年级古诗文启蒙班级'),(302,'2026-01-17 03:35:42','二年级','二年级1班','教师姓名2',3,'二年级古诗文基础班级'),(303,'2026-01-17 03:35:42','三年级','三年级1班','教师姓名3',3,'三年级古诗文提升班级'),(304,'2026-01-17 03:35:42','四年级','四年级1班','教师姓名4',3,'四年级古诗文积累班级'),(305,'2026-01-17 03:35:42','五年级','五年级1班','教师姓名5',3,'五年级古诗文拓展班级'),(306,'2026-01-17 03:35:42','六年级','六年级1班','教师姓名6',6,'六年级古诗文冲刺班级');
/*!40000 ALTER TABLE `classinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classpk`
--

DROP TABLE IF EXISTS `classpk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classpk` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) DEFAULT NULL COMMENT '发起学生账号',
  `studentname` varchar(200) DEFAULT NULL COMMENT '发起学生姓名',
  `classname` varchar(200) DEFAULT NULL COMMENT '班级',
  `opponentaccount` varchar(200) DEFAULT NULL COMMENT '对手账号',
  `opponentname` varchar(200) DEFAULT NULL COMMENT '对手姓名',
  `myscore` int DEFAULT '0' COMMENT '我的PK得分',
  `opponentscore` int DEFAULT '0' COMMENT '对手PK得分',
  `winneraccount` varchar(200) DEFAULT NULL COMMENT '获胜账号',
  `winnername` varchar(200) DEFAULT NULL COMMENT '获胜姓名',
  `medalreward` int DEFAULT '1' COMMENT '奖励勋章',
  `pkstatus` varchar(200) DEFAULT NULL COMMENT 'PK结果',
  `pktime` datetime DEFAULT NULL COMMENT 'PK时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1775734462862 DEFAULT CHARSET=utf8mb3 COMMENT='班级PK';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classpk`
--

LOCK TABLES `classpk` WRITE;
/*!40000 ALTER TABLE `classpk` DISABLE KEYS */;
INSERT INTO `classpk` VALUES (201,'2026-04-09 06:18:19','用户账号2','用户姓名2','二年级1班','用户账号2A','同学小勇',86,78,'用户账号2','用户姓名2',1,'胜利','2026-01-17 19:30:00'),(1775716106274,'2026-04-09 06:28:26','1','用户姓名6','六年级1班','用户账号6A','六年级同学甲',60,60,'1','用户姓名6',1,'胜利','2026-04-09 14:28:26'),(1775716111419,'2026-04-09 06:28:30','1','用户姓名6','六年级1班','用户账号6B','六年级同学乙',60,60,'1','用户姓名6',1,'胜利','2026-04-09 14:28:30'),(1775716114212,'2026-04-09 06:28:33','1','用户姓名6','六年级1班','用户账号6A','六年级同学甲',60,60,'1','用户姓名6',1,'胜利','2026-04-09 14:28:33'),(1775717134619,'2026-04-09 06:45:34','1','用户姓名6','六年级1班','用户账号6B','六年级同学乙',3,4,'用户账号6B','六年级同学乙',0,'惜败','2026-04-09 14:45:34'),(1775726148625,'2026-04-09 09:15:47','1','用户姓名6','六年级1班','用户账号6B','六年级同学乙',2,3,'用户账号6B','六年级同学乙',0,'惜败','2026-04-09 17:15:47'),(1775734462861,'2026-04-09 11:34:22','1','用户姓名6','六年级1班','用户账号1','用户姓名1',2,3,'用户账号1','用户姓名1',0,'惜败','2026-04-09 19:34:22');
/*!40000 ALTER TABLE `classpk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `config`
--

DROP TABLE IF EXISTS `config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '配置参数名称',
  `value` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '配置参数值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='配置文件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config`
--

LOCK TABLES `config` WRITE;
/*!40000 ALTER TABLE `config` DISABLE KEYS */;
INSERT INTO `config` VALUES (1,'swiper1','file/swiperPicture1.jpg'),(2,'swiper2','file/swiperPicture2.jpg'),(3,'swiper3','file/swiperPicture3.jpg');
/*!40000 ALTER TABLE `config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `courseno` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '古诗词号',
  `coursetitle` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '古诗词标题',
  `coursetype` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '古诗词类型',
  `grade` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '适用年级',
  `picture` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '古诗词封面',
  `intro` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '古诗词简介',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '古诗词详情',
  `video` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '视频',
  `thumbsupnum` int DEFAULT NULL COMMENT '点赞数',
  `crazilynum` int DEFAULT NULL COMMENT '点踩数',
  `clicknum` int DEFAULT NULL COMMENT '点击数',
  `addtime` datetime DEFAULT NULL COMMENT '添加时间',
  `clicktime` datetime DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='古诗词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'CP001','《诗经》选读','经典诗词','一年级','file/course1.jpg','《诗经》是中国古代诗歌开端，最早的一部诗歌总集','《诗经》内容丰富，反映了劳动与爱情、战争与徭役、压迫与反抗、风俗与婚姻、祭祖与宴会，甚至天象、地貌、动物、植物等方方面面，是周代社会生活的一面镜子。本古诗词将选取其中最具代表性的作品进行深入讲解。','file/1.mp4',160,5,1207,'2024-01-15 10:00:00','2026-04-09 19:41:41'),(2,'CP002','唐诗三百首精讲','经典诗词','二年级','file/course2.jpg','唐诗是中华文化的瑰宝，本古诗词精选唐诗三百首进行系统讲解','唐代是我国古典诗歌发展的全盛时期，唐诗是我国优秀的文学遗产之一，也是全世界文学宝库中的一颗灿烂的明珠。本古诗词将带领大家领略李白、杜甫、白居易等伟大诗人的作品魅力。','file/1.mp4',203,2,1800,'2024-01-20 14:30:00','2024-01-15 10:00:00'),(3,'CP003','宋词鉴赏','经典诗词','三年级','file/course3.jpg','宋词是宋代文学的代表，本古诗词将带领大家走进宋词的唯美世界','宋词是一种相对于古体诗的新体诗歌之一，标志宋代文学的最高成就。宋词句子有长有短，便于歌唱。因是合乐的歌词，故又称曲子词、乐府、乐章、长短句、诗余、琴趣等。','file/1.mp4',178,1,1500,'2024-01-25 09:15:00','2024-01-15 10:00:00'),(4,'CP004','元曲欣赏','经典诗词','四年级','file/course4.jpg','元曲是中华民族灿烂文化宝库中的一朵奇葩','元曲是盛行于元代的一种文艺形式，包括杂剧和散曲，有时专指杂剧。 杂剧，宋代以滑稽搞笑为特点的一种表演形式，元代发展成戏曲形式。每本以四折为主，在开头或折间另加楔子，每折用同宫调同韵的北曲套曲和宾白组成。','file/1.mp4',125,0,900,'2026-01-01 16:45:00','2024-01-15 10:00:00'),(5,'CP005','明清小说中的诗词','经典诗词','五年级','file/course5.jpg','探索四大名著等明清小说中的经典诗词','明清小说是中国古代文学的重要组成部分，其中包含了大量精彩的诗词作品。本古诗词将从《红楼梦》、《三国演义》、《水浒传》、《西游记》等经典小说中选取代表性诗词进行赏析。','file/1.mp4',142,1,1113,'2026-01-10 13:20:00','2026-01-04 17:26:33'),(6,'CP006','小学毕业古诗词提升','经典诗词','六年级','file/course6.jpg','围绕六年级常学常考古诗词，进行理解、背诵与赏析训练','本课程聚焦六年级阶段常见的古诗词篇目，如《长歌行》《示儿》《石灰吟》等，帮助学生在毕业阶段进一步提升背诵熟练度、诗意理解能力和情感表达能力。','file/1.mp4',96,0,860,'2026-01-12 09:30:00','2026-01-12 09:30:00');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coursereserve`
--

DROP TABLE IF EXISTS `coursereserve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coursereserve` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户账号',
  `studentname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户姓名',
  `reservetime` datetime DEFAULT NULL COMMENT '预约时间',
  `teacheraccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师账号',
  `teachername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师姓名',
  `reservestatus` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '预约状态',
  `sfsh` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '是否审核',
  `shhf` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '回复内容',
  `reservecount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '人数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1775735001532 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='预约课程';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coursereserve`
--

LOCK TABLES `coursereserve` WRITE;
/*!40000 ALTER TABLE `coursereserve` DISABLE KEYS */;
INSERT INTO `coursereserve` VALUES (111,'2026-01-17 03:35:42','用户账号1','用户姓名1','2026-01-17 11:35:42','1','教师姓名1','已取消','是','','人数1'),(112,'2026-01-17 03:35:42','用户账号2','用户姓名2','2026-01-17 11:35:42','教师账号2','教师姓名2','已取消','是','','人数2'),(113,'2026-01-17 03:35:42','用户账号3','用户姓名3','2026-01-17 11:35:42','教师账号3','教师姓名3','已取消','是','','人数3'),(114,'2026-01-17 03:35:42','用户账号4','用户姓名4','2026-01-17 11:35:42','教师账号4','教师姓名4','已取消','是','','人数4'),(115,'2026-01-17 03:35:42','用户账号5','用户姓名5','2026-01-17 11:35:42','教师账号5','教师姓名5','已取消','是','','人数5'),(116,'2026-01-17 03:35:42','用户账号6','用户姓名6','2026-01-17 11:35:42','教师账号6','教师姓名6','已取消','是','','人数6'),(1746011224093,'2026-01-10 11:07:03','1','用户姓名6','2026-01-10 19:07:02','教师账号2','教师姓名2','已预约','待审核',NULL,'2'),(1775734932552,'2026-04-09 11:42:11','1','用户姓名6','2026-04-09 19:42:08','教师账号6','教师姓名6','已预约','是','11111111','6'),(1775735001531,'2026-04-09 11:43:21','1','用户姓名6','2026-04-09 19:43:12','教师账号6','教师姓名6','已预约','是','1','人数6');
/*!40000 ALTER TABLE `coursereserve` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discusscourse`
--

DROP TABLE IF EXISTS `discusscourse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discusscourse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `refid` bigint DEFAULT NULL COMMENT '关联表id',
  `userid` bigint DEFAULT NULL COMMENT '用户id',
  `avatarurl` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头像',
  `nickname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户名',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '评论内容',
  `reply` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '回复内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1762248302330 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='古诗词评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discusscourse`
--

LOCK TABLES `discusscourse` WRITE;
/*!40000 ALTER TABLE `discusscourse` DISABLE KEYS */;
INSERT INTO `discusscourse` VALUES (1,'2024-01-16 01:30:00',1,1,'file/student1.jpg','文学爱好者','老师讲解很细致，收获很大！',NULL),(2,'2024-01-17 06:20:00',1,2,'file/student2.jpg','诗词初学者','请问《关雎》表达的是什么样的情感呢？','《关雎》表达了古代劳动人民对美好爱情的向往和追求。'),(3,'2024-01-22 03:15:00',2,3,'file/student3.jpg','唐诗迷','李白的诗歌气势磅礴，听完这节课对李白有了更深的了解！',NULL),(4,'2024-01-23 07:40:00',2,4,'file/student4.jpg','古典文学专业','老师对杜甫诗歌的分析非常到位，建议可以再深入讲解一下杜甫的思想。',NULL),(5,'2024-01-26 02:50:00',3,5,'file/student5.jpg','文化研究者','宋词的婉约派和豪放派各有特色，这节课对比讲解很精彩！',NULL),(6,'2024-01-27 08:30:00',3,1,'file/student1.jpg','文学爱好者','苏轼的《念奴娇·赤壁怀古》真是气势恢宏，听完古诗词更能体会其中意境。',NULL),(7,'2026-01-02 05:10:00',4,2,'file/student2.jpg','诗词初学者','第一次系统学习元曲，原来元曲有这么多精彩作品！',NULL),(8,'2026-01-11 01:45:00',5,3,'file/student3.jpg','唐诗迷','《红楼梦》中的诗词真是太美了，每首都很有意境。',NULL),(1762244980575,'2026-01-11 01:45:00',1,76,'file/student6.jpg','1','asdasd',NULL),(1762248302329,'2026-01-04 09:25:02',1,76,'file/student6.jpg','1','123123',NULL);
/*!40000 ALTER TABLE `discusscourse` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discussforum`
--

DROP TABLE IF EXISTS `discussforum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discussforum` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `refid` bigint NOT NULL COMMENT '关联表id',
  `userid` bigint NOT NULL COMMENT '用户id',
  `avatarurl` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '头像',
  `nickname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户名',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '评论内容',
  `reply` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '回复内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1762244971634 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='学习社区评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discussforum`
--

LOCK TABLES `discussforum` WRITE;
/*!40000 ALTER TABLE `discussforum` DISABLE KEYS */;
INSERT INTO `discussforum` VALUES (1746011232852,'2026-01-10 11:07:12',141,76,'file/studentAvatar6.jpg','1','1212',NULL),(1760582650361,'2025-10-16 02:44:10',142,76,'file/student6.jpg','1','666',NULL),(1762223809662,'2026-01-04 02:36:49',141,76,'file/student6.jpg','1','123',NULL),(1762244971633,'2026-01-04 08:29:31',141,76,'file/student6.jpg','1','123123',NULL);
/*!40000 ALTER TABLE `discussforum` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exampaper`
--

DROP TABLE IF EXISTS `exampaper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exampaper` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '古诗词练习名称',
  `time` int NOT NULL COMMENT '时长(分钟)',
  `status` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '练习状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1775734785023 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='古诗词练习管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exampaper`
--

LOCK TABLES `exampaper` WRITE;
/*!40000 ALTER TABLE `exampaper` DISABLE KEYS */;
INSERT INTO `exampaper` VALUES (1,'2026-01-17 01:00:00','唐诗名句默写',30,'1'),(2,'2026-01-18 01:00:00','宋词作者辨析',30,'1'),(3,'2026-01-19 01:00:00','古诗主题理解',40,'1'),(4,'2026-01-20 01:00:00','《诗经》经典赏析',40,'1'),(5,'2026-01-21 01:00:00','李白诗歌专题',35,'1'),(6,'2026-01-22 01:00:00','杜甫诗歌专题',35,'1'),(7,'2026-01-23 01:00:00','唐宋词对比',45,'1'),(8,'2026-01-24 01:00:00','古诗意象解读',45,'1');
/*!40000 ALTER TABLE `exampaper` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `examquestion`
--

DROP TABLE IF EXISTS `examquestion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examquestion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `paperid` bigint NOT NULL COMMENT '练习id（外键）',
  `papername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '练习名称',
  `questionname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '题目名称',
  `options` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '选项，json字符串',
  `score` bigint DEFAULT NULL COMMENT '分值',
  `answer` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '正确答案',
  `analysis` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '答案解析',
  `type` bigint DEFAULT NULL COMMENT '题目类型（0:单选,1:多选,2:判断,3:填空）',
  `sequence` bigint DEFAULT NULL COMMENT '题目排序，值越大排越前面',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1775734812435 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='古诗词题目管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `examquestion`
--

LOCK TABLES `examquestion` WRITE;
/*!40000 ALTER TABLE `examquestion` DISABLE KEYS */;
INSERT INTO `examquestion` VALUES (1,'2026-01-17 01:00:00',1,'唐诗名句默写','\"床前明月光，______。\" 出自李白《静夜思》','[]',10,'疑是地上霜','此句以月光喻霜，写尽思乡之情',3,1),(2,'2026-01-17 01:00:00',1,'唐诗名句默写','下列哪句是杜甫《望岳》中的名句？','[{\"text\":\"会当凌绝顶，一览众山小\",\"code\":\"A\"},{\"text\":\"大漠孤烟直，长河落日圆\",\"code\":\"B\"},{\"text\":\"春风又绿江南岸，明月何时照我还\",\"code\":\"C\"}]',10,'A','《望岳》表达诗人攀登之志，A为正确句；B是王维作品，C是王安石作品',0,2),(3,'2026-01-17 01:00:00',1,'唐诗名句默写','\"独在异乡为异客，每逢佳节倍思亲\"的作者是王维。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','出自王维《九月九日忆山东兄弟》，是思乡诗代表作',2,3),(4,'2026-01-17 01:00:00',1,'唐诗名句默写','\"大漠沙如雪，______。\" 出自李贺《马诗》','[]',10,'燕山月似钩','以比喻写大漠夜景，凸显苍茫感',3,4),(5,'2026-01-17 01:00:00',1,'唐诗名句默写','下列哪些属于白居易的作品？','[{\"text\":\"《长恨歌》\",\"code\":\"A\"},{\"text\":\"《琵琶行》\",\"code\":\"B\"},{\"text\":\"《登高》\",\"code\":\"C\"},{\"text\":\"《钱塘湖春行》\",\"code\":\"D\"}]',10,'A,B,D','《登高》是杜甫作品，其余均为白居易所作',1,5),(6,'2026-01-17 01:00:00',1,'唐诗名句默写','\"飞流直下三千尺，疑是银河落九天\"描写的是庐山瀑布。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','出自李白《望庐山瀑布》，以夸张手法写瀑布壮观',2,6),(7,'2026-01-17 01:00:00',1,'唐诗名句默写','\"羌笛何须怨杨柳，______。\" 出自王之涣《凉州词》','[]',10,'春风不度玉门关','借羌笛抒边塞之苦，暗含思乡之情',3,7),(8,'2026-01-17 01:00:00',1,'唐诗名句默写','下列诗句中，表达友情的是？','[{\"text\":\"海内存知己，天涯若比邻\",\"code\":\"A\"},{\"text\":\"慈母手中线，游子身上衣\",\"code\":\"B\"},{\"text\":\"举头望明月，低头思故乡\",\"code\":\"C\"}]',10,'A','A出自王勃《送杜少府之任蜀州》，写友情；B是母爱，C是思乡',0,8),(9,'2026-01-17 01:00:00',1,'唐诗名句默写','\"接天莲叶无穷碧，映日荷花别样红\"描写的是夏季景色。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','出自杨万里《晓出净慈寺送林子方》，写西湖夏季荷花',2,9),(10,'2026-01-17 01:00:00',1,'唐诗名句默写','下列属于边塞诗的有？','[{\"text\":\"大漠孤烟直，长河落日圆\",\"code\":\"A\"},{\"text\":\"醉卧沙场君莫笑，古来征战几人回\",\"code\":\"B\"},{\"text\":\"明月松间照，清泉石上流\",\"code\":\"C\"}]',10,'A,B','A是王维边塞诗，B是王翰边塞诗；C是山水田园诗',1,10);
/*!40000 ALTER TABLE `examquestion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `examrecord`
--

DROP TABLE IF EXISTS `examrecord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examrecord` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `username` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户名',
  `paperid` bigint NOT NULL COMMENT '练习id（外键）',
  `papername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '练习名称',
  `questionid` bigint NOT NULL COMMENT '题目id（外键）',
  `questionname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '题目名称',
  `options` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '选项，json字符串',
  `score` bigint DEFAULT NULL COMMENT '分值',
  `answer` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '正确答案',
  `analysis` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '答案解析',
  `myscore` bigint DEFAULT NULL COMMENT '题目得分',
  `myanswer` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '考生答案',
  `userid` bigint NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1765400010008 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='古诗词练习记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `examrecord`
--

LOCK TABLES `examrecord` WRITE;
/*!40000 ALTER TABLE `examrecord` DISABLE KEYS */;
INSERT INTO `examrecord` VALUES (1762248313250,'2026-01-04 09:25:12','用户姓名6',1,'1',1,'1. \"床前明月光，______。\" 出自李白《静夜思》','[]',10,'疑是地上霜','此句以月光喻霜，写尽思乡之情',10,'疑是地上霜',76),(1762248317850,'2026-01-04 09:25:16','用户姓名6',1,'1',2,'2. 下列哪句是杜甫《望岳》中的名句？','[{\"text\":\"会当凌绝顶，一览众山小\",\"code\":\"A\"},{\"text\":\"大漠孤烟直，长河落日圆\",\"code\":\"B\"},{\"text\":\"春风又绿江南岸，明月何时照我还\",\"code\":\"C\"}]',10,'A','《望岳》表达诗人攀登之志，A为正确句；B是王维作品，C是王安石作品',10,'A',76),(1762248319885,'2026-01-04 09:25:19','用户姓名6',1,'1',3,'3. \"独在异乡为异客，每逢佳节倍思亲\"的作者是王维。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','出自王维《九月九日忆山东兄弟》，是思乡诗代表作',10,'对',76),(1762248323347,'2026-01-04 09:25:22','用户姓名6',1,'1',4,'4. \"大漠沙如雪，______。\" 出自李贺《马诗》','[]',10,'燕山月似钩','以比喻写大漠夜景，凸显苍茫感',0,'123',76),(1762248324669,'2026-01-04 09:25:24','用户姓名6',1,'1',5,'5. 下列哪些属于白居易的作品？','[{\"text\":\"《长恨歌》\",\"code\":\"A\"},{\"text\":\"《琵琶行》\",\"code\":\"B\"},{\"text\":\"《登高》\",\"code\":\"C\"},{\"text\":\"《钱塘湖春行》\",\"code\":\"D\"}]',10,'A,B,D','《登高》是杜甫作品，其余均为白居易所作',0,'A',76),(1762248326617,'2026-01-04 09:25:26','用户姓名6',1,'1',6,'6. \"飞流直下三千尺，疑是银河落九天\"描写的是庐山瀑布。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','出自李白《望庐山瀑布》，以夸张手法写瀑布壮观',10,'对',76),(1762248328961,'2026-01-04 09:25:28','用户姓名6',1,'1',7,'7. \"羌笛何须怨杨柳，______。\" 出自王之涣《凉州词》','[]',10,'春风不度玉门关','借羌笛抒边塞之苦，暗含思乡之情',0,'123',76),(1762248331171,'2026-01-04 09:25:30','用户姓名6',1,'1',8,'8. 下列诗句中，表达友情的是？','[{\"text\":\"海内存知己，天涯若比邻\",\"code\":\"A\"},{\"text\":\"慈母手中线，游子身上衣\",\"code\":\"B\"},{\"text\":\"举头望明月，低头思故乡\",\"code\":\"C\"}]',10,'A','A出自王勃《送杜少府之任蜀州》，写友情；B是母爱，C是思乡',10,'A',76),(1762248332410,'2026-01-04 09:25:31','用户姓名6',1,'1',9,'9. \"接天莲叶无穷碧，映日荷花别样红\"描写的是夏季景色。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','出自杨万里《晓出净慈寺送林子方》，写西湖夏季荷花',10,'对',76),(1762248333881,'2026-01-04 09:25:33','用户姓名6',1,'1',10,'10. 下列属于边塞诗的有？','[{\"text\":\"大漠孤烟直，长河落日圆\",\"code\":\"A\"},{\"text\":\"醉卧沙场君莫笑，古来征战几人回\",\"code\":\"B\"},{\"text\":\"明月松间照，清泉石上流\",\"code\":\"C\"}]',10,'A,B','A是王维边塞诗，B是王翰边塞诗；C是山水田园诗',0,'A',76),(1762248335878,'2026-01-04 09:25:35','用户姓名6',1,'1',10,'10. 下列属于边塞诗的有？','[{\"text\":\"大漠孤烟直，长河落日圆\",\"code\":\"A\"},{\"text\":\"醉卧沙场君莫笑，古来征战几人回\",\"code\":\"B\"},{\"text\":\"明月松间照，清泉石上流\",\"code\":\"C\"}]',10,'A,B','A是王维边塞诗，B是王翰边塞诗；C是山水田园诗',0,'A',76),(1762248336825,'2026-01-04 09:25:36','用户姓名6',1,'1',10,'10. 下列属于边塞诗的有？','[{\"text\":\"大漠孤烟直，长河落日圆\",\"code\":\"A\"},{\"text\":\"醉卧沙场君莫笑，古来征战几人回\",\"code\":\"B\"},{\"text\":\"明月松间照，清泉石上流\",\"code\":\"C\"}]',10,'A,B','A是王维边塞诗，B是王翰边塞诗；C是山水田园诗',0,'A',76),(1765400010001,'2026-04-03 11:10:00','用户姓名6',201,'六年级古诗词周测',2001,'“少壮不努力，______。”','[]',10,'老大徒伤悲','出自《长歌行》，提醒珍惜少年时光。',10,'老大徒伤悲',76),(1765400010002,'2026-04-04 11:20:00','用户姓名6',202,'六年级边塞诗专练',2002,'“羌笛何须怨杨柳，______。”','[]',10,'春风不度玉门关','出自王之涣《凉州词》，常见边塞诗句。',0,'不知道',76),(1765400010003,'2026-04-05 12:15:00','用户姓名6',203,'六年级思乡诗专练',2003,'“举头望明月，低头思故乡。”的作者是李白。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','出自李白《静夜思》。',10,'对',76),(1765400010004,'2026-04-06 10:40:00','用户姓名6',204,'六年级送别诗专练',2004,'下列哪句表达友情？','[{\"text\":\"海内存知己，天涯若比邻\",\"code\":\"A\"},{\"text\":\"谁言寸草心，报得三春晖\",\"code\":\"B\"},{\"text\":\"独在异乡为异客，每逢佳节倍思亲\",\"code\":\"C\"}]',10,'A','A 写友情，B 写母爱，C 写思乡。',10,'A',76),(1765400010005,'2026-04-07 11:05:00','用户姓名6',205,'六年级综合提升卷',2005,'下列属于边塞诗的有？','[{\"text\":\"大漠孤烟直，长河落日圆\",\"code\":\"A\"},{\"text\":\"醉卧沙场君莫笑，古来征战几人回\",\"code\":\"B\"},{\"text\":\"接天莲叶无穷碧，映日荷花别样红\",\"code\":\"C\"}]',10,'A,B','A、B 属于边塞诗，C 是写景诗。',0,'A',76),(1765400010006,'2026-04-08 12:00:00','用户姓名6',206,'六年级名句默写',2006,'“会当凌绝顶，______。”','[]',10,'一览众山小','出自杜甫《望岳》。',10,'一览众山小',76),(1765400010007,'2026-04-09 10:30:00','用户姓名6',207,'六年级国学判断练习',2007,'“接天莲叶无穷碧，映日荷花别样红”描写的是夏天。','[{\"text\":\"对\",\"code\":\"A\"},{\"text\":\"错\",\"code\":\"B\"}]',10,'A','杨万里笔下的夏日西湖。',10,'对',76);
/*!40000 ALTER TABLE `examrecord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户账号',
  `studentname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户姓名',
  `feedbacktitle` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '反馈标题',
  `feedbackcontent` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '反馈内容',
  `feedbacktime` datetime DEFAULT NULL COMMENT '反馈时间',
  `sfsh` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '是否审核',
  `shhf` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '回复内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='意见反馈';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (91,'2026-01-17 03:35:42','用户账号1','用户姓名1','反馈标题1','反馈内容1','2026-01-17 11:35:42','是',''),(92,'2026-01-17 03:35:42','用户账号2','用户姓名2','反馈标题2','反馈内容2','2026-01-17 11:35:42','是',''),(93,'2026-01-17 03:35:42','用户账号3','用户姓名3','反馈标题3','反馈内容3','2026-01-17 11:35:42','是',''),(94,'2026-01-17 03:35:42','用户账号4','用户姓名4','反馈标题4','反馈内容4','2026-01-17 11:35:42','是',''),(95,'2026-01-17 03:35:42','用户账号5','用户姓名5','反馈标题5','反馈内容5','2026-01-17 11:35:42','是',''),(96,'2026-01-17 03:35:42','用户账号6','用户姓名6','反馈标题6','反馈内容6','2026-01-17 11:35:42','是','');
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum`
--

DROP TABLE IF EXISTS `forum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `picture` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '图片',
  `forumtitle` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '贴子标题',
  `forumcontent` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '帖子内容',
  `releasetime` date DEFAULT NULL COMMENT '发布时间',
  `userid` bigint NOT NULL COMMENT '用户id',
  `thumbsupnum` int DEFAULT NULL COMMENT '赞',
  `crazilynum` int DEFAULT NULL COMMENT '踩',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1762238786723 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='学习社区';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum`
--

LOCK TABLES `forum` WRITE;
/*!40000 ALTER TABLE `forum` DISABLE KEYS */;
INSERT INTO `forum` VALUES (141,'2026-01-17 03:35:42','file/forum1.jpg,file/forum2.jpg,file/forum3.jpg','贴子标题1','帖子内容1','2026-01-17',1,16,3),(142,'2026-01-17 03:35:42','file/forum2.jpg,file/forum3.jpg,file/forum4.jpg','贴子标题2','帖子内容2','2026-01-17',2,7,2),(143,'2026-01-17 03:35:42','file/forum3.jpg,file/forum4.jpg,file/forum5.jpg','贴子标题3','帖子内容3','2026-01-17',3,3,3),(144,'2026-01-17 03:35:42','file/forum4.jpg,file/forum5.jpg,file/forum6.jpg','贴子标题4','帖子内容4','2026-01-17',4,11,4),(145,'2026-01-17 03:35:42','file/forum5.jpg,file/forum6.jpg,file/forum7.jpg','贴子标题5','帖子内容5','2026-01-17',5,5,5),(146,'2026-01-17 03:35:42','file/forum6.jpg,file/forum7.jpg,file/forum8.jpg','贴子标题6','帖子内容6','2026-01-17',6,6,6),(1762238786722,'2026-01-04 06:46:25','','123','<p>123123s</p>','2026-01-04',76,NULL,NULL);
/*!40000 ALTER TABLE `forum` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu`
--

DROP TABLE IF EXISTS `menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `menujson` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '菜单',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu`
--

LOCK TABLES `menu` WRITE;
/*!40000 ALTER TABLE `menu` DISABLE KEYS */;
INSERT INTO `menu` VALUES (1,'2026-01-17 03:35:42','[{\"backMenu\":[{\"child\":[{\"appFrontIcon\":\"cuIcon-discover\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"轮播图\",\"menuJump\":\"列表\",\"tableName\":\"config\"}],\"fontClass\":\"icon-common18\",\"menu\":\"轮播图管理\",\"unicode\":\"&#xedff;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-keyboard\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"管理员\",\"menuJump\":\"列表\",\"tableName\":\"admin\"},{\"appFrontIcon\":\"cuIcon-full\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\",\"首页总数\",\"分配\"],\"menu\":\"用户\",\"menuJump\":\"列表\",\"tableName\":\"student\"},{\"appFrontIcon\":\"cuIcon-qrcode\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\",\"首页总数\"],\"menu\":\"教师\",\"menuJump\":\"列表\",\"tableName\":\"teacher\"}],\"fontClass\":\"icon-common50\",\"menu\":\"管理员管理\",\"unicode\":\"&#xef96;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-pay\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"公告信息\",\"menuJump\":\"列表\",\"tableName\":\"news\"}],\"fontClass\":\"icon-common46\",\"menu\":\"公告信息\",\"unicode\":\"&#xef3d;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-circle\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"古诗词测试管理\",\"tableName\":\"exampaper\"}],\"fontClass\":\"icon-common27\",\"menu\":\"古诗词测试管理\",\"unicode\":\"&#xee2c;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-addressbook\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\",\"打印\",\"导出\"],\"menu\":\"题目管理\",\"menuJump\":\"列表\",\"tableName\":\"examquestion\"}],\"fontClass\":\"icon-common47\",\"menu\":\"题目管理\",\"unicode\":\"&#xef63;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-circle\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"古诗词测试列表\",\"menuJump\":\"12\",\"tableName\":\"exampaper\"},{\"appFrontIcon\":\"cuIcon-pic\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"测试记录\",\"tableName\":\"examrecord\"},{\"appFrontIcon\":\"cuIcon-pic\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"错题本\",\"menuJump\":\"22\",\"tableName\":\"examrecord\"}],\"fontClass\":\"icon-common18\",\"menu\":\"题库管理\",\"unicode\":\"&#xedff;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-send\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\",\"审核\"],\"menu\":\"意见反馈\",\"menuJump\":\"列表\",\"tableName\":\"feedback\"}],\"fontClass\":\"icon-common28\",\"menu\":\"意见反馈管理\",\"unicode\":\"&#xee2d;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-discover\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"用户\",\"menuJump\":\"列表\",\"tableName\":\"mystudent\"}],\"fontClass\":\"icon-common38\",\"menu\":\"用户管理\",\"unicode\":\"&#xeeb2;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-explore\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"预约课程\",\"menuJump\":\"列表\",\"tableName\":\"coursereserve\"},{\"appFrontIcon\":\"cuIcon-vip\",\"buttons\":[\"查看\",\"修改\",\"删除\"],\"menu\":\"预约取消\",\"menuJump\":\"列表\",\"tableName\":\"reservecancel\"}],\"fontClass\":\"icon-common50\",\"menu\":\"预约课程管理\",\"unicode\":\"&#xef96;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-paint\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\",\"查看评论\"],\"menu\":\"学习社区\",\"menuJump\":\"列表\",\"tableName\":\"forum\"}],\"fontClass\":\"icon-common23\",\"menu\":\"学习社区管理\",\"unicode\":\"&#xee05;\"}],\"frontMenu\":[{\"child\":[{\"appFrontIcon\":\"cuIcon-time\",\"buttons\":[\"预约\"],\"menu\":\"教师\",\"menuJump\":\"列表\",\"tableName\":\"teacher\"}],\"menu\":\"教师管理\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-skin\",\"buttons\":[\"新增\",\"查看\",\"查看评论\"],\"menu\":\"学习社区\",\"menuJump\":\"列表\",\"tableName\":\"forum\"}],\"menu\":\"学习社区管理\"}],\"hasBackLogin\":\"是\",\"hasBackRegister\":\"否\",\"hasFrontLogin\":\"否\",\"hasFrontRegister\":\"否\",\"roleName\":\"管理员\",\"tableName\":\"admin\"},{\"backMenu\":[{\"child\":[{\"appFrontIcon\":\"cuIcon-pic\",\"buttons\":[\"查看\"],\"menu\":\"测试记录\",\"tableName\":\"examrecord\"},{\"appFrontIcon\":\"cuIcon-pic\",\"buttons\":[\"查看\"],\"menu\":\"错题本\",\"menuJump\":\"22\",\"tableName\":\"examrecord\"}],\"fontClass\":\"icon-common18\",\"menu\":\"题库管理\",\"unicode\":\"&#xedff;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-send\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\"],\"menu\":\"意见反馈\",\"menuJump\":\"列表\",\"tableName\":\"feedback\"}],\"fontClass\":\"icon-common28\",\"menu\":\"意见反馈管理\",\"unicode\":\"&#xee2d;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-explore\",\"buttons\":[\"查看\",\"取消预约\"],\"menu\":\"预约课程\",\"menuJump\":\"列表\",\"tableName\":\"coursereserve\"},{\"appFrontIcon\":\"cuIcon-vip\",\"buttons\":[\"查看\"],\"menu\":\"预约取消\",\"menuJump\":\"列表\",\"tableName\":\"reservecancel\"}],\"fontClass\":\"icon-common50\",\"menu\":\"预约课程管理\",\"unicode\":\"&#xef96;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-paint\",\"buttons\":[\"查看\",\"修改\",\"删除\",\"查看评论\"],\"menu\":\"学习社区\",\"menuJump\":\"列表\",\"tableName\":\"forum\"}],\"fontClass\":\"icon-common23\",\"menu\":\"学习社区管理\",\"unicode\":\"&#xee05;\"}],\"frontMenu\":[{\"child\":[{\"appFrontIcon\":\"cuIcon-time\",\"buttons\":[\"预约\"],\"menu\":\"教师\",\"menuJump\":\"列表\",\"tableName\":\"teacher\"}],\"menu\":\"教师管理\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-skin\",\"buttons\":[\"新增\",\"查看\",\"查看评论\"],\"menu\":\"学习社区\",\"menuJump\":\"列表\",\"tableName\":\"forum\"}],\"menu\":\"学习社区管理\"}],\"hasBackLogin\":\"否\",\"hasBackRegister\":\"否\",\"hasFrontLogin\":\"是\",\"hasFrontRegister\":\"是\",\"roleName\":\"用户\",\"tableName\":\"student\"},{\"backMenu\":[{\"child\":[{\"appFrontIcon\":\"cuIcon-pic\",\"buttons\":[\"查看\",\"成绩统计\"],\"menu\":\"测试记录\",\"tableName\":\"examrecord\"},{\"appFrontIcon\":\"cuIcon-pic\",\"buttons\":[\"查看\"],\"menu\":\"错题本\",\"menuJump\":\"22\",\"tableName\":\"examrecord\"}],\"fontClass\":\"icon-common18\",\"menu\":\"题库管理\",\"unicode\":\"&#xedff;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-discover\",\"buttons\":[\"查看\"],\"menu\":\"用户\",\"menuJump\":\"列表\",\"tableName\":\"mystudent\"}],\"fontClass\":\"icon-common38\",\"menu\":\"用户管理\",\"unicode\":\"&#xeeb2;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-explore\",\"buttons\":[\"查看\",\"审核\"],\"menu\":\"预约课程\",\"menuJump\":\"列表\",\"tableName\":\"coursereserve\"},{\"appFrontIcon\":\"cuIcon-vip\",\"buttons\":[\"查看\"],\"menu\":\"预约取消\",\"menuJump\":\"列表\",\"tableName\":\"reservecancel\"}],\"fontClass\":\"icon-common50\",\"menu\":\"预约课程管理\",\"unicode\":\"&#xef96;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-paint\",\"buttons\":[\"新增\",\"查看\",\"修改\",\"删除\",\"查看评论\"],\"menu\":\"学习社区\",\"menuJump\":\"列表\",\"tableName\":\"forum\"}],\"fontClass\":\"icon-common23\",\"menu\":\"学习社区管理\",\"unicode\":\"&#xee05;\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-attentionfavor\",\"buttons\":[\"查看\",\"修改\",\"删除\",\"成绩统计\",\"新增\"],\"menu\":\"成绩信息\",\"menuJump\":\"列表\",\"tableName\":\"transcript\"}],\"fontClass\":\"icon-common31\",\"menu\":\"成绩信息管理\",\"unicode\":\"&#xee48;\"}],\"frontMenu\":[{\"child\":[{\"appFrontIcon\":\"cuIcon-time\",\"buttons\":[\"预约\"],\"menu\":\"教师\",\"menuJump\":\"列表\",\"tableName\":\"teacher\"}],\"menu\":\"教师管理\"},{\"child\":[{\"appFrontIcon\":\"cuIcon-skin\",\"buttons\":[\"新增\",\"查看\",\"查看评论\"],\"menu\":\"学习社区\",\"menuJump\":\"列表\",\"tableName\":\"forum\"}],\"menu\":\"学习社区管理\"}],\"hasBackLogin\":\"是\",\"hasBackRegister\":\"是\",\"hasFrontLogin\":\"否\",\"hasFrontRegister\":\"否\",\"roleName\":\"教师\",\"tableName\":\"teacher\"}]');
/*!40000 ALTER TABLE `menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mystudent`
--

DROP TABLE IF EXISTS `mystudent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mystudent` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户账号',
  `studentname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户姓名',
  `teacheraccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师账号',
  `teachername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师姓名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mystudent`
--

LOCK TABLES `mystudent` WRITE;
/*!40000 ALTER TABLE `mystudent` DISABLE KEYS */;
INSERT INTO `mystudent` VALUES (101,'2026-01-17 03:35:42','用户账号1','用户姓名1','1','教师姓名1'),(102,'2026-01-17 03:35:42','用户账号2','用户姓名2','教师账号2','教师姓名2'),(103,'2026-01-17 03:35:42','用户账号3','用户姓名3','教师账号3','教师姓名3'),(104,'2026-01-17 03:35:42','用户账号4','用户姓名4','教师账号4','教师姓名4'),(105,'2026-01-17 03:35:42','用户账号5','用户姓名5','教师账号5','教师姓名5'),(106,'2026-01-17 03:35:42','用户账号6','用户姓名6','教师账号6','教师姓名6');
/*!40000 ALTER TABLE `mystudent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `news`
--

DROP TABLE IF EXISTS `news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `news` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `title` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '标题',
  `introduction` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '简介',
  `picture` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '图片',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='公告信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `news`
--

LOCK TABLES `news` WRITE;
/*!40000 ALTER TABLE `news` DISABLE KEYS */;
INSERT INTO `news` VALUES (31,'2026-01-17 03:35:42','青花瓷','素胚勾勒出青花笔锋浓转淡，瓶身描绘的牡丹一如你初妆，冉冉檀香透过窗心事我了然，宣上走笔至此搁一半，釉色渲染仕女图韵味被私藏，而你嫣然的一笑如含苞待放，你的美缕飘散，去到我去不了的地方，天青色等烟雨 而我在等你，炊烟袅袅升起','file/newsPicture1.jpg','素胚勾勒出青花笔锋浓转淡，瓶身描绘的牡丹一如你初妆，冉冉檀香透过窗心事我了然，宣上走笔至此搁一半，釉色渲染仕女图韵味被私藏，而你嫣然的一笑如含苞待放，你的美缕飘散，去到我去不了的地方，天青色等烟雨 而我在等你，炊烟袅袅升起， 隔江千万里，在瓶底书前朝的飘逸，就当我为遇见你伏笔，天青色等烟雨， 而我在等你，月色被打捞起， 晕开了局，如传世的青花瓷自顾自美丽，你眼带笑意，色白花青的锦鲤跃然于碗底，临摹宋体落款时却惦记着你，你隐藏在窑烧里千年的秘密，极细腻犹如绣花针落地，帘外芭蕉惹骤雨门环惹铜绿，而我路过那江南小镇惹了你，在泼墨山水画里，你从墨色深处被隐去，天青色等烟雨 ，而我在等你，炊烟袅袅升起 ，隔江千万里，在瓶底书汉隶仿前朝的飘逸，就当我为遇见你伏笔，天色等烟雨 ，而我在等你，月色被打捞起， 晕开了结局，如传世的青花瓷自顾自美丽，你眼带笑意，天青色等烟雨 ，而我在等你，炊烟袅袅升起 ，隔江千万里，在瓶底书汉隶仿前朝的飘逸，就当我为遇见你伏笔，天青色等烟雨， 而我在等你，月色被打捞起 ，晕开了结局，如传世的青花瓷自顾自美丽，你眼带笑意。'),(32,'2026-01-17 03:35:42','理想三旬','雨后有车驶来，驶过暮色苍白，旧铁皮往南开，恋人已不在，收听浓烟下的，诗歌电台，不动情的咳嗽，至少看起来，归途也还可爱，琴弦少了姿态，再不见那夜里，听歌的小孩，时光匆匆独白，将颠沛磨成卡带，已枯倦的情怀，踏碎成年代，就老去吧，孤独别醒来，你渴望的离开，只是无处停摆，就歌唱吧，眼睛眯起来，而热泪的崩坏，只是没抵达的存在','file/newsPicture2.jpg','雨后有车驶来，驶过暮色苍白，旧铁皮往南开，恋人已不在，收听浓烟下的，诗歌电台，不动情的咳嗽，至少看起来，归途也还可爱，琴弦少了姿态，再不见那夜里，听歌的小孩，时光匆匆独白，将颠沛磨成卡带，已枯倦的情怀，踏碎成年代，就老去吧，孤独别醒来，你渴望的离开，只是无处停摆，就歌唱吧，眼睛眯起来，而热泪的崩坏，只是没抵达的存在，青春又醉倒在，籍籍无名的怀，靠嬉笑来虚度，聚散得慷慨，辗转却去不到，对的站台，如果漂泊是成长，必经的路牌，你迷醒岁月中，那贫瘠的未来，像遗憾季节里，未结果的爱，弄脏了每一页诗，吻最疼痛的告白，而风声吹到这，已不需要释怀，就老去吧，孤独别醒来，渴望的离开只是无处停摆就歌唱吧，眼睛眯起来而热泪的崩坏，只是没抵达的存在，就甜蜜地忍耐，繁星润湿窗台，光影跳动着像在，困倦里说爱，再无谓的感慨，以为明白，梦倒塌的地方，今已爬满青苔。'),(33,'2026-01-17 03:35:42','七里香','窗外的麻雀在电线杆上多嘴，你说这一句很有夏天的感觉，手中的铅笔在纸上来来回回，我用几行字形容你是我的谁，秋刀鱼的滋味猫跟你都想了解，初恋的香味就这样被我们寻回，那温暖的阳光像刚摘的鲜艳草莓，你说你舍不得吃掉这一种感觉，雨下整夜我的爱溢出就像雨水，院子落叶跟我的思念厚厚一叠','file/newsPicture3.jpg','窗外的麻雀在电线杆上多嘴，你说这一句很有夏天的感觉，手中的铅笔在纸上来来回回，我用几行字形容你是我的谁，秋刀鱼的滋味猫跟你都想了解，初恋的香味就这样被我们寻回，那温暖的阳光像刚摘的鲜艳草莓，你说你舍不得吃掉这一种感觉，雨下整夜我的爱溢出就像雨水，院子落叶跟我的思念厚厚一叠，几句是非也无法将我的热情冷却，你出现在我诗的每一页，雨下整夜我的爱溢出就像雨水，窗台蝴蝶像诗里纷飞的美丽章节，我接着写，把永远爱你写进诗的结尾，你是我唯一想要的了解，雨下整夜我的爱溢出就像雨水，院子落叶跟我的思念厚厚一叠，几句是非也无法将我的热情冷却，你出现在我诗的每一页，那饱满的稻穗幸福了这个季节，而你的脸颊像田里熟透的番茄，你突然对我说七里香的名字很美，我此刻却只想亲吻你倔强的嘴，雨下整夜我的爱溢出就像雨水，院子落叶跟我的思念厚厚一叠，几句是非也无法将我的热情冷却，你出现在我诗的每一页，整夜我的爱溢出就像雨水，窗台蝴蝶像诗里纷飞的美丽章节，我接着写，把永远爱你写进诗的结尾，是我唯一想要的了解。'),(34,'2026-01-17 03:35:42','江南','风到这里就是粘，粘住过客的思念，雨到了这里缠成线，缠着我们流连人世间，你在身边就是缘，缘分写在三生石上面，爱有万分之一甜，宁愿我就葬在这一点，圈圈圆圆圈圈，天天年年天天的我，深深看你的脸，生气的温柔，埋怨的温柔的脸','file/newsPicture4.jpg','风到这里就是粘，粘住过客的思念，雨到了这里缠成线，缠着我们流连人世间，你在身边就是缘，缘分写在三生石上面，爱有万分之一甜，宁愿我就葬在这一点，圈圈圆圆圈圈，天天年年天天的我，深深看你的脸，生气的温柔，埋怨的温柔的脸，不懂爱恨情愁煎熬的我们，都以为相爱就像风云的善变，相信爱一天抵过永远，在这一刹那冻结了时间，不懂怎么表现温柔的我们，还以为殉情只是古老的传言，离愁能有多痛痛有多浓，当梦被埋在江南烟雨中，心碎了才懂，圈圈圆圆圈圈，天天年年天天的我，深深看你的脸，生气的温柔，埋怨的温柔的脸，不懂爱恨情愁煎熬的我们，都以为相爱就像风云的善变，相信爱一天 抵过永远，在这一刹那冻结了时间，不懂怎么表现温柔的我们，还以为殉情只是古老的传言，离愁能有多痛 痛有多浓，当梦被埋在江南烟雨中，心碎了才懂，相信爱一天抵过永远。在这一刹那冻结了时间，不懂怎么表现温柔的我们，还以为殉情只是古老的传言，离愁能有多痛 痛有多浓，当梦被埋在江南烟雨中，心碎了才懂。'),(35,'2026-01-17 03:35:42','那些你很冒险的梦','当两颗心开始震动，当你瞳孔学会闪躲，当爱慢慢被遮住只剩下黑，距离像影子被拉拖，当爱的故事剩听说，我找不到你单纯的面孔，当生命每分每秒都为你转动，心多执着就加倍心痛，那些你很冒险的梦， 我陪你去疯，折纸飞机碰到雨天终究会坠落','file/newsPicture5.jpg','当两颗心开始震动，当你瞳孔学会闪躲，当爱慢慢被遮住只剩下黑，距离像影子被拉拖，当爱的故事剩听说，我找不到你单纯的面孔，当生命每分每秒都为你转动，心多执着就加倍心痛，那些你很冒险的梦， 我陪你去疯，折纸飞机碰到雨天终究会坠落，太残忍的话我直说 因为爱很重，你却不想懂 只往反方向走，当爱的故事剩听说，我找不到你单纯的面孔，当生命每分每秒都为你转动，心有多执着就加倍心痛，那些你很冒险的梦 我陪你去疯，折纸飞机 碰到雨天 终究会坠落，太残忍的话我直说 因为爱很重，你却不想懂 只往反方向走，我不想放手 你松开的左手，你爱的放纵 我白不回天空，我输了 累了，但你再也 不回头，那些你很冒险的梦 我陪你去疯，折纸飞机 碰到雨天 终究会坠落，太残忍的话我直说 因为爱很重，你却不想懂 只往反方向走，你真的不懂 我的爱已降落。'),(36,'2026-01-17 03:35:42','孤勇者','都，是勇敢的，你额头的伤口 你的 不同 你犯的错，都 不必隐藏，你破旧的玩偶 你的 面具 你的自我，他们说 要带着光 驯服每一头怪兽，他们说 要缝好你的伤，没有人爱小丑 为何孤独 不可 光荣，人只有不完美 值得歌颂，谁说污泥满身的不算英雄，爱你孤身走暗巷，爱你不跪的模样，爱你对峙过绝望','file/newsPicture6.jpg','都，是勇敢的，你额头的伤口 你的 不同 你犯的错，都 不必隐藏，你破旧的玩偶 你的 面具 你的自我，他们说 要带着光 驯服每一头怪兽，他们说 要缝好你的伤，没有人爱小丑 为何孤独 不可 光荣，人只有不完美 值得歌颂，谁说污泥满身的不算英雄，爱你孤身走暗巷，爱你不跪的模样，爱你对峙过绝望，不肯哭一场，爱你破烂的衣裳，却敢堵命运的枪，爱你和我那么像，缺口都一样，去吗 配吗 这褴褛的披风，战吗 战啊 以最卑微的梦，致那黑夜中的呜咽与怒吼，谁说站在光里的才算英雄，他们说 要戒了你的狂，就像擦掉了污垢，他们说 要顺台阶而上，而代价是低头，那就让我 不可 乘风，你一样骄傲着 那种孤勇，谁说对弈平凡的不算英雄，爱你孤身走暗巷 爱你不跪的模样，爱你对峙过绝望 不肯哭一场，爱你破烂的衣裳 却敢堵命运的枪，爱你和我那么像 缺口都一样，去吗 配吗 这褴褛的披风，战吗 战啊 以最卑微的梦，致那黑夜中的呜咽与怒吼，谁说站在光里的才算英雄，你的斑驳 与众不同 与众不同，你的沉默 震耳欲聋 震耳欲聋，You Are The Hero，爱你孤身走暗巷 爱你不跪的模样，爱你对峙过绝望 不肯哭一场，爱你来自于蛮荒 一生不借谁的光，你将造你的城邦 在废墟之上，去吗 去啊 以最卑微的梦，战吗 战啊 以最孤高的梦，致那黑夜中的呜咽与怒吼，谁说站在光里的才算英雄。');
/*!40000 ALTER TABLE `news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recitationtask`
--

DROP TABLE IF EXISTS `recitationtask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recitationtask` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户账号',
  `studentname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户姓名',
  `courseids` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '背诵古诗ID集合',
  `coursetitles` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '背诵古诗标题集合',
  `tasktitle` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '任务标题',
  `taskcontent` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '任务要求',
  `deadline` datetime DEFAULT NULL COMMENT '截止日期',
  `completionstatus` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '待完成' COMMENT '完成状态',
  `completionremark` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '完成说明',
  `recitationaudio` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '背诵音频',
  `completiontime` datetime DEFAULT NULL COMMENT '完成时间',
  `kaoshichengji` int DEFAULT NULL COMMENT '背诵得分',
  `teachercomment` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师评语',
  `teacheraccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师账号',
  `teachername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师姓名',
  `releasetime` datetime DEFAULT NULL COMMENT '发布日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1775734737884 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='背诵任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recitationtask`
--

LOCK TABLES `recitationtask` WRITE;
/*!40000 ALTER TABLE `recitationtask` DISABLE KEYS */;
INSERT INTO `recitationtask` VALUES (132,'2026-01-17 03:35:42','用户账号2','用户姓名2','2,3','唐诗三百首精讲,宋词鉴赏','背诵《春晓》与宋词片段','背诵时注意停顿，并说出诗中描写的春天景象。','2026-01-21 18:00:00','已完成','已经会背，还能讲出诗意。','upload/demo-recitation-132.mp3','2026-01-18 19:30:00',92,'吐字清晰，节奏很好。','教师账号2','教师姓名2','2026-01-17 11:35:42'),(1765400030001,'2026-04-03 09:00:00','1','用户姓名6','301','《凉州词》','背诵《凉州词》','要求背诵全文，并说出这首诗表达的边塞情感。','2026-04-05 18:00:00','已完成','已经能完整背诵。','upload/demo-recitation-301.mp3','2026-04-04 18:10:00',89,'吐字清晰，节奏较好。','教师账号6','教师姓名6','2026-04-03 17:00:00'),(1765400030002,'2026-04-06 09:10:00','1','用户姓名6','302,303','《出塞》,《从军行》','边塞诗背诵巩固','完成《出塞》《从军行》两首古诗背诵，并录音提交。','2026-04-10 18:00:00','已完成','请老师批改','1775734395209.mp3','2026-04-09 19:33:15',NULL,'','教师账号6','教师姓名6','2026-04-06 17:10:00'),(1765400030003,'2026-04-08 08:40:00','1','用户姓名6','304','《静夜思》','思乡诗复习背诵','复习《静夜思》，注意停顿和感情表达。','2026-04-11 18:00:00','已完成','11','1775725564546.mp3','2026-04-09 17:06:10',33,'33','教师账号6','教师姓名6','2026-04-08 16:40:00'),(1775726104479,'2026-04-09 09:15:04','1','用户姓名6','','123','3123','请完成以下古诗背诵：123。','2026-04-08 00:00:00','已完成','11111','1775734405343.mp3','2026-04-09 19:33:30',0,'1','教师账号6','教师姓名6','2026-04-09 17:14:30'),(1775734737434,'2026-04-09 11:38:57','用户账号1B','一年级同学乙','','111','111','请完成以下古诗背诵：111。',NULL,'待完成','','',NULL,NULL,'','教师账号6','教师姓名6','2026-04-09 19:38:32'),(1775734737490,'2026-04-09 11:38:57','1','用户姓名6','','111','111','请完成以下古诗背诵：111。',NULL,'待完成','','',NULL,NULL,'','教师账号6','教师姓名6','2026-04-09 19:38:32'),(1775734737505,'2026-04-09 11:38:57','用户账号2','用户姓名2','','111','111','请完成以下古诗背诵：111。',NULL,'待完成','','',NULL,NULL,'','教师账号6','教师姓名6','2026-04-09 19:38:32'),(1775734737521,'2026-04-09 11:38:57','用户账号6A','六年级同学甲','','111','111','请完成以下古诗背诵：111。',NULL,'待完成','','',NULL,NULL,'','教师账号6','教师姓名6','2026-04-09 19:38:32'),(1775734737600,'2026-04-09 11:38:57','用户账号6B','六年级同学乙','','111','111','请完成以下古诗背诵：111。',NULL,'待完成','','',NULL,NULL,'','教师账号6','教师姓名6','2026-04-09 19:38:32'),(1775734737883,'2026-04-09 11:38:57','用户账号1','用户姓名1','','111','111','请完成以下古诗背诵：111。',NULL,'待完成','','',NULL,NULL,'','教师账号6','教师姓名6','2026-04-09 19:38:32');
/*!40000 ALTER TABLE `recitationtask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservecancel`
--

DROP TABLE IF EXISTS `reservecancel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservecancel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户账号',
  `studentname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户姓名',
  `canceltime` datetime DEFAULT NULL COMMENT '取消时间',
  `teacheraccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师账号',
  `teachername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师姓名',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='预约取消';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservecancel`
--

LOCK TABLES `reservecancel` WRITE;
/*!40000 ALTER TABLE `reservecancel` DISABLE KEYS */;
INSERT INTO `reservecancel` VALUES (121,'2026-01-17 03:35:42','用户账号1','用户姓名1','2026-01-17 11:35:42','1','教师姓名1'),(122,'2026-01-17 03:35:42','用户账号2','用户姓名2','2026-01-17 11:35:42','教师账号2','教师姓名2'),(123,'2026-01-17 03:35:42','用户账号3','用户姓名3','2026-01-17 11:35:42','教师账号3','教师姓名3'),(124,'2026-01-17 03:35:42','用户账号4','用户姓名4','2026-01-17 11:35:42','教师账号4','教师姓名4'),(125,'2026-01-17 03:35:42','用户账号5','用户姓名5','2026-01-17 11:35:42','教师账号5','教师姓名5'),(126,'2026-01-17 03:35:42','用户账号6','用户姓名6','2026-01-17 11:35:42','教师账号6','教师姓名6');
/*!40000 ALTER TABLE `reservecancel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storeup`
--

DROP TABLE IF EXISTS `storeup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `storeup` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `refid` bigint DEFAULT NULL COMMENT 'refid',
  `tablename` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '表名',
  `name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '名称',
  `picture` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '图片',
  `type` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '类型(1:收藏,21:赞,22:踩,31:竞拍参与,41:关注)',
  `inteltype` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '推荐类型',
  `remark` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `userid` bigint NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1762248365007 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='我的收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storeup`
--

LOCK TABLES `storeup` WRITE;
/*!40000 ALTER TABLE `storeup` DISABLE KEYS */;
INSERT INTO `storeup` VALUES (1762243575243,'2026-01-04 08:06:14',1,'course','《诗经》选读','file/course1.jpg','1',NULL,NULL,76),(1762248365006,'2026-01-04 09:26:04',141,'forum','贴子标题1','file/forum1.jpg,file/forum2.jpg,file/forum3.jpg','1',NULL,NULL,76);
/*!40000 ALTER TABLE `storeup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户账号',
  `studentpassword` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户密码',
  `studentname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户姓名',
  `avatar` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '头像',
  `gender` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '性别',
  `telephone` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '手机号码',
  `grade` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '年级',
  `classname` varchar(200) DEFAULT NULL COMMENT '班级',
  `medalcount` int DEFAULT '0' COMMENT '勋章数量',
  `permissionstatus` varchar(200) DEFAULT '启用' COMMENT '权限状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `studentaccount` (`studentaccount`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1775734352311 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (71,'2026-01-17 03:35:42','用户账号1','123456','用户姓名1','file/student1.jpg','男','19819881111','六年级','六年级1班',0,'启用'),(72,'2026-01-17 03:35:42','用户账号2','123456','用户姓名2','file/student2.jpg','男','19819881112','六年级','六年级1班',0,'启用'),(73,'2026-01-17 03:35:42','用户账号3','123456','用户姓名3','file/student3.jpg','男','19819881113','三年级','三年级1班',0,'启用'),(74,'2026-01-17 03:35:42','用户账号4','123456','用户姓名4','file/student4.jpg','男','19819881114','四年级','四年级1班',0,'启用'),(75,'2026-01-17 03:35:42','用户账号5','123456','用户姓名5','file/student5.jpg','男','19819881115','五年级','五年级1班',0,'启用'),(76,'2026-01-17 03:35:42','1','1','用户姓名6','file/student6.jpg','男','19819881116','六年级','六年级1班',3,'启用'),(81,'2026-01-17 03:35:42','用户账号1A','123456','一年级同学甲','file/student1.jpg','女','19819881211','一年级','一年级1班',0,'启用'),(82,'2026-01-17 03:35:42','用户账号1B','123456','一年级同学乙','file/student1.jpg','男','19819881212','六年级','六年级1班',1,'启用'),(83,'2026-01-17 03:35:42','用户账号2A','123456','二年级同学甲','file/student2.jpg','女','19819881221','二年级','二年级1班',2,'启用'),(84,'2026-01-17 03:35:42','用户账号2B','123456','二年级同学乙','file/student2.jpg','男','19819881222','二年级','二年级1班',1,'启用'),(85,'2026-01-17 03:35:42','用户账号3A','123456','三年级同学甲','file/student3.jpg','女','19819881231','三年级','三年级1班',0,'启用'),(86,'2026-01-17 03:35:42','用户账号3B','123456','三年级同学乙','file/student3.jpg','男','19819881232','三年级','三年级1班',2,'启用'),(87,'2026-01-17 03:35:42','用户账号4A','123456','四年级同学甲','file/student4.jpg','女','19819881241','四年级','四年级1班',1,'启用'),(88,'2026-01-17 03:35:42','用户账号4B','123456','四年级同学乙','file/student4.jpg','男','19819881242','四年级','四年级1班',3,'启用'),(89,'2026-01-17 03:35:42','用户账号5A','123456','五年级同学甲','file/student5.jpg','女','19819881251','五年级','五年级1班',0,'启用'),(90,'2026-01-17 03:35:42','用户账号5B','123456','五年级同学乙','file/student5.jpg','男','19819881252','五年级','五年级1班',2,'启用'),(91,'2026-01-17 03:35:42','用户账号6A','123456','六年级同学甲','file/student6.jpg','女','19819881261','六年级','六年级1班',1,'启用'),(92,'2026-01-17 03:35:42','用户账号6B','123456','六年级同学乙','file/student6.jpg','男','19819881262','六年级','六年级1班',2,'启用');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `teacheraccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教师账号',
  `teacherpassword` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教师密码',
  `teachername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教师姓名',
  `zhaopian` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '照片',
  `gender` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '性别',
  `lianxidianhua` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系电话',
  `reservecount` int DEFAULT NULL COMMENT '可约人数',
  `permissionstatus` varchar(200) DEFAULT '启用' COMMENT '权限状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `teacheraccount` (`teacheraccount`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='教师';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (81,'2026-01-17 03:35:42','1','1','教师姓名1','file/teacher1.jpg','男','19819881111',1,'启用'),(82,'2026-01-17 03:35:42','教师账号2','123456','教师姓名2','file/teacher2.jpg','男','19819881112',2,'启用'),(83,'2026-01-17 03:35:42','教师账号3','123456','教师姓名3','file/teacher3.jpg','男','19819881113',3,'启用'),(84,'2026-01-17 03:35:42','教师账号4','123456','教师姓名4','file/teacher4.jpg','男','19819881114',4,'启用'),(85,'2026-01-17 03:35:42','教师账号5','123456','教师姓名5','file/teacher5.jpg','男','19819881115',5,'启用'),(86,'2026-01-17 03:35:42','教师账号6','1','教师姓名6','file/teacher6.jpg','男','19819881116',6,'启用');
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `token`
--

DROP TABLE IF EXISTS `token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userid` bigint NOT NULL COMMENT '用户id',
  `username` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户名',
  `tablename` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '表名',
  `role` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '角色',
  `token` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `expiratedtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='token表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `token`
--

LOCK TABLES `token` WRITE;
/*!40000 ALTER TABLE `token` DISABLE KEYS */;
INSERT INTO `token` VALUES (1,76,'1','student','用户','79pjn1ilvv01hmpq77pc5nru4r5ccpwh','2026-01-10 11:04:50','2026-04-09 12:41:26'),(2,86,'教师账号6','teacher','管理员','8etwz0ygfky9luqry4jg35o0m95j681b','2026-01-10 11:05:35','2026-04-09 12:58:00'),(3,81,'1','teacher','管理员','yuc91n5f68hwpygb3xn2ueknnnmoorql','2026-01-10 11:05:56','2026-04-09 12:57:11'),(4,1,'admin','admin','管理员','3jh4hdb4h44ydxnix3g75nxsvblmzevs','2026-01-10 11:08:02','2026-04-09 12:51:41'),(5,1775734352310,'111','student','用户','qtmkfd0gxh7lnfquznh5zvf5cfhttpkg','2026-04-09 11:32:38','2026-04-09 12:32:39');
/*!40000 ALTER TABLE `token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transcript`
--

DROP TABLE IF EXISTS `transcript`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transcript` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `studentaccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户账号',
  `studentname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户姓名',
  `kaoshichengji` int DEFAULT NULL COMMENT '练习成绩',
  `teacheraccount` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师账号',
  `teachername` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教师姓名',
  `releasetime` datetime DEFAULT NULL COMMENT '发布日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1765400020004 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='成绩信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transcript`
--

LOCK TABLES `transcript` WRITE;
/*!40000 ALTER TABLE `transcript` DISABLE KEYS */;
INSERT INTO `transcript` VALUES (131,'2026-01-17 03:35:42','用户账号1','用户姓名1',1,'1','教师姓名1','2026-01-17 11:35:42'),(132,'2026-01-17 03:35:42','用户账号2','用户姓名2',2,'教师账号2','教师姓名2','2026-01-17 11:35:42'),(133,'2026-01-17 03:35:42','用户账号3','用户姓名3',3,'教师账号3','教师姓名3','2026-01-17 11:35:42'),(134,'2026-01-17 03:35:42','用户账号4','用户姓名4',4,'教师账号4','教师姓名4','2026-01-17 11:35:42'),(135,'2026-01-17 03:35:42','用户账号5','用户姓名5',5,'教师账号5','教师姓名5','2026-01-17 11:35:42'),(136,'2026-01-17 03:35:42','用户账号6','用户姓名6',6,'教师账号6','教师姓名6','2026-01-17 11:35:42'),(1765400020001,'2026-04-04 12:30:00','1','用户姓名6',82,'教师账号6','教师姓名6','2026-04-04 20:30:00'),(1765400020002,'2026-04-07 12:20:00','1','用户姓名6',76,'教师账号6','教师姓名6','2026-04-07 20:20:00'),(1765400020003,'2026-04-09 11:10:00','1','用户姓名6',88,'教师账号6','教师姓名6','2026-04-09 19:10:00');
/*!40000 ALTER TABLE `transcript` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-09 19:59:40
