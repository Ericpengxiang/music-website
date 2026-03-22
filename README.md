# 基于 Java 的音乐网站

一个基于 Spring Boot + Vue 3 的全栈音乐网站，支持前台音乐浏览和后台管理。

## 技术栈

- **后端**：Spring Boot 3 + Java 17 + Spring Security + JPA + MySQL
- **前端**：Vue 3 + TypeScript + Vite + Element Plus + Pinia

## 功能模块

### 前台
- 音乐浏览（歌曲、专辑、歌手、流派）
- 音乐播放器
- 排行榜
- 音乐故事
- 用户注册/登录
- 收藏、歌单、播放历史

### 管理后台
- 数据概览
- 轮播图管理
- 流派/歌手/专辑/歌曲/歌词管理
- 音乐故事管理
- 用户管理
- 操作日志

## 部署

### 环境变量（后端）

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DATABASE_URL` | MySQL 连接 URL | `jdbc:mysql://localhost:3306/music?...` |
| `DATABASE_USERNAME` | 数据库用户名 | `root` |
| `DATABASE_PASSWORD` | 数据库密码 | 空 |
| `PORT` | 服务端口 | `8080` |
| `JWT_SECRET` | JWT 密钥 | `change-me-...` |

### 本地运行

```bash
# 后端
cd java
mvn spring-boot:run

# 前端
cd vue
pnpm install
pnpm dev
```
