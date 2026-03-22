<template>
  <div class="home">
    <!-- 轮播图 -->
    <div class="banner-section">
      <el-carousel 
        v-if="banners.length > 0"
        :interval="5000" 
        arrow="hover" 
        height="480px"
        indicator-position="outside"
      >
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div 
            class="banner-item"
            :style="{ backgroundImage: `url(${getImageUrl(banner.imageUrl)})` }"
            @click="handleBannerClick(banner)"
          >
            <div class="banner-overlay"></div>
            <div class="banner-content" v-if="banner.title">
              <h1 class="banner-title">{{ banner.title }}</h1>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <!-- 默认Banner（如果没有轮播图数据） -->
      <div v-else class="hero">
        <div class="hero-overlay"></div>
        <div class="hero-content">
          <h1 class="hero-title">探索无限音乐世界</h1>
          <p class="hero-subtitle">百万曲库 · 精选推荐 · 随心畅听</p>
          <div class="hero-buttons">
            <button class="btn-primary" @click="$router.push('/songs')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M8 5v14l11-7z"/>
              </svg>
              开始探索
            </button>
            <button class="btn-secondary" @click="$router.push('/albums')">
              浏览专辑
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速统计 -->
    <div class="stats-section">
      <div class="stat-item" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon-wrapper">
          <span class="stat-icon">{{ stat.icon }}</span>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value.toLocaleString() }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 最近播放 (登录用户) -->
    <section v-if="auth.token && recentPlays.length > 0" class="section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="section-icon">🕒</span>
          最近播放
        </h2>
        <a @click="$router.push('/me/history')" class="section-more">查看全部 →</a>
      </div>
      
      <div class="song-grid">
        <div class="song-card" v-for="item in recentPlays.slice(0, 6)" :key="item.id" @click="playSong(item.song)">
          <div class="song-cover">
            <img 
              v-if="item.song?.album?.coverUrl" 
              :src="getImageUrl(item.song.album.coverUrl)" 
              alt=""
            />
            <div v-else class="song-cover-placeholder">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
              </svg>
            </div>
            <div class="song-play-overlay">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="white">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </div>
          </div>
          <div class="song-info">
            <div class="song-title">{{ item.song?.title }}</div>
            <div class="song-artist">{{ item.song?.artist?.name || '未知歌手' }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新歌曲 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="section-icon">🔥</span>
          最新上架
        </h2>
        <a @click="$router.push('/songs')" class="section-more">查看全部 →</a>
      </div>
      
      <div class="song-grid" v-loading="loading">
        <div class="song-card" v-for="song in recentSongs" :key="song.id" @click="playSong(song)">
          <div class="song-cover">
            <img 
              v-if="song.album?.coverUrl" 
              :src="getImageUrl(song.album.coverUrl)" 
              alt=""
            />
            <div v-else class="song-cover-placeholder">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
              </svg>
            </div>
            <div class="song-play-overlay">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="white">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </div>
          </div>
          <div class="song-info">
            <div class="song-title">{{ song.title }}</div>
            <div class="song-artist">{{ song.artist?.name || '未知歌手' }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const banners = ref<any[]>([])
const recentSongs = ref<any[]>([])
const recentPlays = ref<any[]>([])
const stats = ref([
  { icon: '🎵', label: '首歌曲', value: 0 },
  { icon: '💿', label: '张专辑', value: 0 },
  { icon: '🎤', label: '位歌手', value: 0 },
  { icon: '🎼', label: '个流派', value: 0 }
])

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadData() {
  loading.value = true
  try {
    const promises = [
      http.get('/banners/active'),
      http.get('/songs'),
      http.get('/dashboard/stats')
    ]
    
    // 如果是前台登录用户，加载最近播放（管理员token不请求前台接口）
    if (auth.token && !(auth as any).isAdmin) {
      promises.push(http.get('/history', { params: { page: 0, size: 6 } }))
    }
    
    const results = await Promise.all(promises)
    const [bannersRes, songsRes, statsRes, historyRes] = results
    
    banners.value = bannersRes.data || []
    recentSongs.value = songsRes.data.slice(0, 12)
    
    if (statsRes.data) {
      stats.value[0].value = statsRes.data.songCount || 0
      stats.value[1].value = statsRes.data.albumCount || 0
      stats.value[2].value = statsRes.data.artistCount || 0
      stats.value[3].value = statsRes.data.genreCount || 0
    }
    
    if (historyRes?.data?.content) {
      recentPlays.value = historyRes.data.content
    }
  } catch (err: any) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleBannerClick(banner: any) {
  if (banner.linkUrl) {
    router.push(banner.linkUrl)
  }
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function playSong(song: any) {
  if (!song.audioUrl) {
    ElMessage.warning('该歌曲暂无音频')
    return
  }
  playSongGlobal?.(song)
}

onMounted(loadData)
</script>

<style scoped>
.home {
  margin: -32px;
}

/* 轮播图区域 */
.banner-section {
  margin-bottom: -32px;
}

:deep(.el-carousel) {
  border-radius: 0;
}

:deep(.el-carousel__container) {
  height: 480px;
}

:deep(.el-carousel__indicator) {
  padding: 0;
}

:deep(.el-carousel__button) {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.5);
  transition: all 0.3s;
}

:deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: linear-gradient(90deg, #ec4899 0%, #8b5cf6 100%);
  width: 50px;
}

:deep(.el-carousel__arrow) {
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  transition: all 0.3s;
}

:deep(.el-carousel__arrow:hover) {
  background: rgba(0, 0, 0, 0.5);
  transform: scale(1.1);
}

.banner-item {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0.4) 100%);
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 40px;
}

.banner-title {
  font-size: 64px;
  font-weight: 800;
  margin: 0;
  letter-spacing: -1px;
  text-shadow: 0 4px 20px rgba(0,0,0,0.3);
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 默认Hero Banner */
.hero {
  position: relative;
  height: 480px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 600"><circle cx="100" cy="100" r="80" fill="rgba(255,255,255,0.05)"/><circle cx="800" cy="300" r="120" fill="rgba(255,255,255,0.03)"/><circle cx="1000" cy="100" r="60" fill="rgba(255,255,255,0.04)"/></svg>');
  opacity: 0.5;
}

.hero-content {
  position: relative;
  text-align: center;
  z-index: 1;
  color: white;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  margin: 0 0 16px 0;
  letter-spacing: -1px;
  text-shadow: 0 4px 20px rgba(0,0,0,0.2);
}

.hero-subtitle {
  font-size: 20px;
  margin: 0 0 40px 0;
  opacity: 0.95;
  font-weight: 300;
  letter-spacing: 2px;
}

.hero-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-primary, .btn-secondary {
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 28px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
}

.btn-primary {
  background: white;
  color: #764ba2;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 30px rgba(0,0,0,0.25);
}

.btn-secondary {
  background: rgba(255,255,255,0.15);
  color: white;
  border: 2px solid rgba(255,255,255,0.3);
  backdrop-filter: blur(10px);
}

.btn-secondary:hover {
  background: rgba(255,255,255,0.25);
  border-color: rgba(255,255,255,0.5);
}

/* 统计区域 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding: 40px 32px;
  background: white;
  margin: -32px 0 40px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border-color: #8b5cf6;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.stat-icon {
  font-size: 32px;
  filter: grayscale(1) brightness(10);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

/* 内容区域 */
.section {
  padding: 0 32px 48px 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
}

.section-icon {
  font-size: 32px;
}

.section-more {
  color: #8b5cf6;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.section-more:hover {
  color: #ec4899;
  transform: translateX(4px);
}

/* 歌曲网格 */
.song-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
}

.song-card {
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.song-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
  border-color: #8b5cf6;
}

.song-cover {
  position: relative;
  width: 100%;
  padding-top: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e5e7eb 100%);
  overflow: hidden;
}

.song-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.song-card:hover .song-cover img {
  transform: scale(1.1);
}

.song-cover-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(0,0,0,0.2);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.song-play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.song-card:hover .song-play-overlay {
  opacity: 1;
}

.song-info {
  padding: 12px;
}

.song-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-artist {
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

