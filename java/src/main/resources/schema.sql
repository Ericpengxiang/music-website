-- Schema initialization for music-admin (executed by Spring on startup)
-- IMPORTANT: Database should already exist (jdbc url includes createDatabaseIfNotExist=true)

SET FOREIGN_KEY_CHECKS=0;

-- Roles
CREATE TABLE IF NOT EXISTS role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Users (Admin backend)
CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(100),
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Role mapping
CREATE TABLE IF NOT EXISTS user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Genre
CREATE TABLE IF NOT EXISTS genre (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Artist
CREATE TABLE IF NOT EXISTS artist (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  bio TEXT NULL,
  avatar_url VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_artist_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Album
CREATE TABLE IF NOT EXISTS album (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  artist_id BIGINT NOT NULL,
  cover_url VARCHAR(500) NULL,
  release_date DATE NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE RESTRICT,
  INDEX idx_album_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Song
CREATE TABLE IF NOT EXISTS song (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  artist_id BIGINT NOT NULL,
  album_id BIGINT NULL,
  genre_id BIGINT NULL,
  duration_sec INT NULL,
  audio_url VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_song_artist FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE RESTRICT,
  CONSTRAINT fk_song_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE SET NULL,
  CONSTRAINT fk_song_genre FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE SET NULL,
  INDEX idx_song_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Audit Log
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  operation VARCHAR(50) NOT NULL,
  resource_type VARCHAR(100) NOT NULL,
  resource_id BIGINT NULL,
  details TEXT NULL,
  ip_address VARCHAR(50) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_log_username (username),
  INDEX idx_audit_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Frontend User (public user)
CREATE TABLE IF NOT EXISTS frontend_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(100) NULL,
  email VARCHAR(150) NULL,
  avatar_url VARCHAR(500) NULL,
  bio TEXT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_frontend_user_username (username),
  INDEX idx_frontend_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Favorite Song
CREATE TABLE IF NOT EXISTS user_favorite_song (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  song_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_fav_song_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_fav_song_song FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_song (user_id, song_id),
  INDEX idx_user_fav_song_user (user_id),
  INDEX idx_user_fav_song_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Favorite Album
CREATE TABLE IF NOT EXISTS user_favorite_album (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  album_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_fav_album_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_fav_album_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_album (user_id, album_id),
  INDEX idx_user_fav_album_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Favorite Artist
CREATE TABLE IF NOT EXISTS user_favorite_artist (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  artist_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_fav_artist_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_fav_artist_artist FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_artist (user_id, artist_id),
  INDEX idx_user_fav_artist_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Play History
CREATE TABLE IF NOT EXISTS play_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  song_id BIGINT NOT NULL,
  play_duration INT NULL COMMENT 'play seconds',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_play_history_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_play_history_song FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE,
  INDEX idx_play_history_user (user_id),
  INDEX idx_play_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Playlist
CREATE TABLE IF NOT EXISTS playlist (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  description TEXT NULL,
  cover_url VARCHAR(500) NULL,
  is_public TINYINT(1) NOT NULL DEFAULT 1,
  play_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_playlist_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  INDEX idx_playlist_user (user_id),
  INDEX idx_playlist_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Playlist Song
CREATE TABLE IF NOT EXISTS playlist_song (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  playlist_id BIGINT NOT NULL,
  song_id BIGINT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_playlist_song_playlist FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE,
  CONSTRAINT fk_playlist_song_song FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE,
  UNIQUE KEY uk_playlist_song (playlist_id, song_id),
  INDEX idx_playlist_song_playlist (playlist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comment
CREATE TABLE IF NOT EXISTS comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  resource_type VARCHAR(20) NOT NULL,
  resource_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  parent_id BIGINT NULL,
  like_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE CASCADE,
  INDEX idx_comment_resource (resource_type, resource_id),
  INDEX idx_comment_user (user_id),
  INDEX idx_comment_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comment Like
CREATE TABLE IF NOT EXISTS comment_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  comment_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_comment (user_id, comment_id),
  INDEX idx_comment_like_comment (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lyric
CREATE TABLE IF NOT EXISTS lyric (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  song_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  translated TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_lyric_song FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE,
  UNIQUE KEY uk_song_lyric (song_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Music Story
CREATE TABLE IF NOT EXISTS music_story (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  song_id BIGINT NOT NULL,
  story TEXT NOT NULL,
  emotion VARCHAR(20) NULL,
  like_count INT NOT NULL DEFAULT 0,
  is_featured TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_story_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_story_song FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE,
  INDEX idx_story_song (song_id),
  INDEX idx_story_featured (is_featured),
  INDEX idx_story_emotion (emotion),
  INDEX idx_story_like (like_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Story Like
CREATE TABLE IF NOT EXISTS story_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  story_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_story_like_user FOREIGN KEY (user_id) REFERENCES frontend_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_story_like_story FOREIGN KEY (story_id) REFERENCES music_story(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_story (user_id, story_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Banner (轮播图表)
CREATE TABLE IF NOT EXISTS banner (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NULL COMMENT '标题',
  image_url VARCHAR(500) NOT NULL COMMENT '图片地址',
  link_url VARCHAR(500) NULL COMMENT '跳转链接',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_banner_sort (sort_order),
  INDEX idx_banner_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS=1;

-- ====================================================================
-- 示例数据初始化
-- ====================================================================

-- 插入流派数据
INSERT IGNORE INTO genre (id, name) VALUES 
(1, '流行'),
(2, '摇滚'),
(3, '民谣'),
(4, '电子'),
(5, '爵士'),
(6, '古典'),
(7, '嘻哈'),
(8, 'R&B');

-- 插入歌手数据
INSERT IGNORE INTO artist (id, name, bio, avatar_url) VALUES 
(1, '周杰伦', '华语流行音乐天王，擅长融合中国风与流行音乐', 'uploads/2ec5bfc046e54bba8f42bc570626df6d.jpg'),
(2, '五月天', '台湾知名摇滚乐团，充满正能量的音乐风格', 'uploads/2ee0e1d7471843f6b09e06db3803d3b7.jpg'),
(3, '邓紫棋', '香港创作型女歌手，拥有独特的嗓音', 'uploads/706455e0793f4fc0bb85300163bf7fae.jpg'),
(4, '李荣浩', '内地创作型歌手，简约风格深受喜爱', 'uploads/a4c2f15a35364bccbade56502cb14c5a.jpg'),
(5, '陈奕迅', '香港歌手，演唱功力深厚，情感细腻', 'uploads/42e5e8d380504a9885b3f17f619b30ca.jpg'),
(6, '林俊杰', '新加坡歌手，擅长抒情歌曲创作', 'uploads/a1e115d6fd2e486194d0cfa2815005dd.png'),
(7, '薛之谦', '内地歌手，以深情歌曲著称', 'uploads/3325662b3f5b46e987db76171c1e4bb0.png'),
(8, '毛不易', '内地民谣歌手，歌词治愈温暖', 'uploads/ed4d5848c17e4b1299f74a001ad1d68c.png');

-- 插入专辑数据
INSERT IGNORE INTO album (id, title, artist_id, cover_url, release_date) VALUES 
(1, '范特西', 1, 'uploads/2ec5bfc046e54bba8f42bc570626df6d.jpg', '2001-09-14'),
(2, '叶惠美', 1, 'uploads/2ec5bfc046e54bba8f42bc570626df6d.jpg', '2003-07-31'),
(3, '第二人生', 2, 'uploads/2ee0e1d7471843f6b09e06db3803d3b7.jpg', '2011-12-16'),
(4, '自传', 2, 'uploads/2ee0e1d7471843f6b09e06db3803d3b7.jpg', '2016-07-21'),
(5, '启示录', 3, 'uploads/706455e0793f4fc0bb85300163bf7fae.jpg', '2020-11-06'),
(6, '模特', 4, 'uploads/a4c2f15a35364bccbade56502cb14c5a.jpg', '2016-12-20'),
(7, 'U-87', 5, 'uploads/42e5e8d380504a9885b3f17f619b30ca.jpg', '2005-06-07'),
(8, '因你而在', 6, 'uploads/a1e115d6fd2e486194d0cfa2815005dd.png', '2015-03-27'),
(9, '渡', 7, 'uploads/3325662b3f5b46e987db76171c1e4bb0.png', '2020-12-31'),
(10, '平凡的一天', 8, 'uploads/ed4d5848c17e4b1299f74a001ad1d68c.png', '2017-09-01');

-- 插入歌曲数据
INSERT IGNORE INTO song (id, title, artist_id, album_id, genre_id, duration_sec, audio_url) VALUES 
-- 周杰伦的歌
(1, '稻香', 1, 1, 1, 225, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(2, '晴天', 1, 2, 1, 270, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(3, '七里香', 1, 2, 1, 305, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(4, '青花瓷', 1, 1, 1, 228, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),

-- 五月天的歌
(5, '倔强', 2, 3, 2, 264, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(6, '突然好想你', 2, 3, 2, 248, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(7, '温柔', 2, 4, 2, 279, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(8, '如烟', 2, 4, 2, 295, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),

-- 邓紫棋的歌
(9, '泡沫', 3, 5, 1, 245, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(10, '光年之外', 3, 5, 1, 228, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(11, '倒数', 3, 5, 1, 264, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),

-- 李荣浩的歌
(12, '模特', 4, 6, 1, 225, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(13, '李白', 4, 6, 1, 257, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(14, '年少有为', 4, 6, 1, 219, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),

-- 陈奕迅的歌
(15, '十年', 5, 7, 1, 195, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(16, '富士山下', 5, 7, 1, 269, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(17, '孤勇者', 5, 7, 1, 254, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),

-- 林俊杰的歌
(18, '江南', 6, 8, 1, 243, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(19, '修炼爱情', 6, 8, 1, 267, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(20, '小酒窝', 6, 8, 1, 254, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),

-- 薛之谦的歌
(21, '演员', 7, 9, 1, 265, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(22, '丑八怪', 7, 9, 1, 247, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(23, '认真的雪', 7, 9, 1, 233, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),

-- 毛不易的歌
(24, '消愁', 8, 10, 3, 294, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(25, '像我这样的人', 8, 10, 3, 251, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3'),
(26, '平凡的一天', 8, 10, 3, 278, 'uploads/38d7992f2d6f48c99ee8580d1b1167f8.mp3');

-- 插入前台用户数据（用于测试）
INSERT IGNORE INTO frontend_user (id, username, password_hash, nickname, email, avatar_url, bio, is_active) VALUES 
(1, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKdE8L9e', '音乐爱好者', 'user1@example.com', 'uploads/42f1882bf9a542498c6beed8dd7e0e8d.ico', '热爱音乐，享受生活', 1),
(2, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKdE8L9e', '夜听者', 'user2@example.com', 'uploads/c464cfe557b7416da053a39aa2b64ac4.ico', '深夜音乐陪伴者', 1),
(3, 'user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKdE8L9e', '故事分享家', 'user3@example.com', 'uploads/42f1882bf9a542498c6beed8dd7e0e8d.ico', '用音乐记录故事', 1);

-- 插入音乐故事数据（精选故事）
INSERT IGNORE INTO music_story (id, user_id, song_id, story, emotion, like_count, is_featured) VALUES 
(1, 1, 1, '第一次听《稻香》是在高三的晚自习，那时候每天都在为梦想努力。现在每次听到这首歌，都会想起那段拼搏的日子。感谢音乐陪伴我走过最艰难的时光。', '励志', 128, 1),
(2, 2, 5, '《倔强》是我和她的定情曲。我们在咖啡馆第一次相遇时，店里正播放着这首歌。虽然我们最后没能走到一起，但每次听到这首歌，还是会想起她的笑容。', '怀旧', 256, 1),
(3, 1, 9, '深夜失眠的时候总是单曲循环《泡沫》，歌词里的每一句都像是在说我的心事。音乐真的能治愈人心，感谢有这样一首歌陪伴我度过黑暗时光。', '治愈', 89, 1),
(4, 3, 15, '《十年》是妈妈最喜欢的歌。每次回家，她都会放这首歌。现在在外地工作，每次听到这首歌就会想家。音乐承载着家的温暖和妈妈的爱。', '温暖', 342, 1),
(5, 2, 21, '失恋后的第一个夜晚，一个人在房间里听着《演员》哭了一整夜。现在已经走出来了，但这首歌见证了我最脆弱的时刻。感谢音乐让我学会了坚强。', '伤感', 175, 1),
(6, 1, 24, '《消愁》是我们班的毕业歌。那年夏天，我们唱着这首歌告别了青春。五年后再听，泪流满面。有些人，有些事，只能存在于回忆里了。', '怀旧', 421, 1),
(7, 3, 2, '每天早上跑步必听《晴天》！这首歌的节奏完美，每次听都能激发我的斗志。跑了半年，瘦了20斤，感觉整个人都重生了。音乐的力量真的很神奇！', '励志', 98, 0),
(8, 2, 14, '考研那年，每天凌晨3点还在图书馆复习，累的时候就听《年少有为》。终于考上了理想的学校，这首歌见证了我的蜕变。永远感恩那段奋斗时光。', '励志', 156, 1),
(9, 1, 18, '雨天的下午，一个人坐在窗边听《江南》，看着窗外的雨，心情平静下来。这首歌有种魔力，能让浮躁的心安静下来。推荐给每个需要静心的人。', '治愈', 67, 0),
(10, 3, 7, '《温柔》是我和闺蜜的专属歌曲。每次聚会我们都会一起唱这首歌。友情万岁！有音乐相伴的青春真美好。', '温暖', 134, 0);

-- 插入歌词数据（示例）
INSERT IGNORE INTO lyric (id, song_id, content, translated) VALUES 
(1, 1, '[00:00.00]稻香 - 周杰伦
[00:10.00]对这个世界如果你有太多的抱怨
[00:15.00]跌倒了就不敢继续往前走
[00:20.00]为什么人要这么的脆弱 堕落
[00:25.00]请你打开电视看看
[00:28.00]多少人为生命在努力勇敢的走下去', NULL),

(2, 5, '[00:00.00]倔强 - 五月天
[00:15.00]当我和世界不一样
[00:18.00]那就让我不一样
[00:21.00]坚持对我来说就是以刚克刚
[00:27.00]我如果对自己不行
[00:30.00]如果对自己说谎', NULL),

(3, 15, '[00:00.00]十年 - 陈奕迅
[00:20.00]如果那两个字没有颤抖
[00:25.00]我不会发现我难受
[00:30.00]怎么说出口 也不过是分手
[00:35.00]如果对于明天没有要求
[00:40.00]牵牵手就像旅游', NULL),

(4, 24, '[00:00.00]消愁 - 毛不易
[00:15.00]当你走进这欢乐场
[00:19.00]背上所有的梦与想
[00:23.00]各色的脸上各色的妆
[00:27.00]没人记得你的模样', NULL);

-- 插入播放列表数据（示例）
INSERT IGNORE INTO playlist (id, user_id, name, description, cover_url, is_public, play_count) VALUES 
(1, 1, '我的最爱', '收藏了最喜欢的歌曲', 'uploads/2ec5bfc046e54bba8f42bc570626df6d.jpg', 1, 156),
(2, 1, '夜晚专属', '适合深夜聆听的歌曲', 'uploads/706455e0793f4fc0bb85300163bf7fae.jpg', 1, 89),
(3, 2, '励志歌单', '充满正能量的音乐', 'uploads/2ee0e1d7471843f6b09e06db3803d3b7.jpg', 1, 234),
(4, 3, '回忆杀', '那些年我们一起听过的歌', 'uploads/a4c2f15a35364bccbade56502cb14c5a.jpg', 1, 178);

-- 插入播放列表歌曲关联
INSERT IGNORE INTO playlist_song (playlist_id, song_id, sort_order) VALUES 
(1, 1, 1), (1, 5, 2), (1, 15, 3), (1, 18, 4),
(2, 15, 1), (2, 21, 2), (2, 24, 3),
(3, 1, 1), (3, 5, 2), (3, 14, 3), (3, 17, 4),
(4, 2, 1), (4, 6, 2), (4, 15, 3), (4, 18, 4);

-- 插入用户收藏歌曲数据
INSERT IGNORE INTO user_favorite_song (user_id, song_id) VALUES 
(1, 1), (1, 5), (1, 15), (1, 18), (1, 24),
(2, 2), (2, 5), (2, 9), (2, 21),
(3, 1), (3, 7), (3, 14), (3, 15);

-- 插入播放历史数据
INSERT IGNORE INTO play_history (user_id, song_id, play_duration) VALUES 
(1, 1, 225), (1, 2, 270), (1, 5, 264), (1, 15, 195),
(2, 5, 264), (2, 9, 245), (2, 21, 265),
(3, 1, 225), (3, 7, 279), (3, 14, 219), (3, 15, 195);

-- 插入评论数据
INSERT IGNORE INTO comment (user_id, resource_type, resource_id, content, parent_id, like_count) VALUES 
(1, 'song', 1, '《稻香》真的太好听了！满满的回忆！', NULL, 45),
(2, 'song', 1, '每次听都想起高中时光，青春啊~', NULL, 32),
(3, 'song', 5, '倔强是青春的代名词！五月天永远的神！', NULL, 67),
(1, 'song', 15, '陈奕迅的《十年》，唱出了多少人的心声', NULL, 89),
(2, 'song', 24, '毛不易的歌总是那么治愈，消愁是我的最爱', NULL, 54),
(3, 'album', 1, '范特西这张专辑每首歌都是经典！', NULL, 78),
(1, 'album', 3, '五月天的第二人生，陪伴了我整个大学时光', NULL, 56);

-- 插入轮播图数据
INSERT IGNORE INTO banner (id, title, image_url, link_url, sort_order, is_active) VALUES 
(1, '探索无限音乐世界', 'uploads/2ec5bfc046e54bba8f42bc570626df6d.jpg', '/songs', 1, 1),
(2, '精选推荐专辑', 'uploads/2ee0e1d7471843f6b09e06db3803d3b7.jpg', '/albums', 2, 1),
(3, '热门歌手', 'uploads/706455e0793f4fc0bb85300163bf7fae.jpg', '/artists', 3, 1);


