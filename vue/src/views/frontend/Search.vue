<template>
  <div class="search-page">
    <!-- 搜索头部 -->
    <div class="search-header">
      <div class="search-title">
        <h1>
          <svg class="search-icon" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
          搜索结果
        </h1>
        <div v-if="keyword" class="search-query">
          关键词：<span class="keyword-tag">{{ keyword }}</span>
        </div>
      </div>
      
      <!-- 快速搜索框 -->
      <div class="quick-search">
        <input 
          v-model="searchInput"
          @keyup.enter="performSearch"
          placeholder="继续搜索..."
          class="search-input"
        />
        <button @click="performSearch" class="search-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 搜索结果统计 -->
    <div class="search-stats">
      <div class="stat-item" :class="{active: activeTab === 'songs'}" @click="activeTab = 'songs'">
        <span class="stat-icon">🎵</span>
        <span class="stat-label">歌曲</span>
        <span class="stat-count">{{ songResults.length }}</span>
      </div>
      <div class="stat-item" :class="{active: activeTab === 'albums'}" @click="activeTab = 'albums'">
        <span class="stat-icon">💿</span>
        <span class="stat-label">专辑</span>
        <span class="stat-count">{{ albumResults.length }}</span>
      </div>
      <div class="stat-item" :class="{active: activeTab === 'artists'}" @click="activeTab = 'artists'">
        <span class="stat-icon">🎤</span>
        <span class="stat-label">歌手</span>
        <span class="stat-count">{{ artistResults.length }}</span>
      </div>
    </div>

    <!-- 结果内容 -->
    <div class="results-container" v-loading="loading">
      <!-- 歌曲结果 -->
      <div v-show="activeTab === 'songs'" class="results-section">
        <div v-if="songResults.length > 0" class="song-list">
          <div class="song-item" v-for="(song, index) in songResults" :key="song.id">
            <div class="song-index">{{ index + 1 }}</div>
            <div class="song-cover" @click="playSong(song)">
              <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
              <div v-else class="cover-placeholder">🎵</div>
              <div class="play-overlay">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="white">
                  <path d="M8 5v14l11-7z"/>
                </svg>
              </div>
            </div>
            <div class="song-info">
              <div class="song-title" @click="$router.push(`/song/${song.id}`)">{{ song.title }}</div>
              <div class="song-meta">
                <span class="artist">{{ song.artist?.name || '未知歌手' }}</span>
                <span v-if="song.album" class="separator">·</span>
                <span v-if="song.album" class="album">{{ song.album.title }}</span>
              </div>
            </div>
            <div class="song-genre">
              <span v-if="song.genre" class="genre-badge">{{ song.genre.name }}</span>
            </div>
            <div class="song-duration">{{ formatDuration(song.durationSec) }}</div>
          </div>
        </div>
        <div v-else class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
          <p>未找到相关歌曲</p>
          <p class="empty-tip">试试其他关键词吧</p>
        </div>
      </div>

      <!-- 专辑结果 -->
      <div v-show="activeTab === 'albums'" class="results-section">
        <div v-if="albumResults.length > 0" class="album-grid">
          <div class="album-card" v-for="album in albumResults" :key="album.id" @click="$router.push(`/album/${album.id}`)">
            <div class="album-cover">
              <img v-if="album.coverUrl" :src="getImageUrl(album.coverUrl)" alt="" />
              <div v-else class="cover-placeholder">💿</div>
              <div class="album-overlay">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="white">
                  <path d="M8 5v14l11-7z"/>
                </svg>
              </div>
            </div>
            <div class="album-info">
              <div class="album-title">{{ album.title }}</div>
              <div class="album-artist">{{ album.artist?.name }}</div>
              <div class="album-date">{{ album.releaseDate || '-' }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
          <p>未找到相关专辑</p>
        </div>
      </div>

      <!-- 歌手结果 -->
      <div v-show="activeTab === 'artists'" class="results-section">
        <div v-if="artistResults.length > 0" class="artist-grid">
          <div class="artist-card" v-for="artist in artistResults" :key="artist.id" @click="$router.push(`/artist/${artist.id}`)">
            <div class="artist-avatar">
              <img v-if="artist.avatarUrl" :src="getImageUrl(artist.avatarUrl)" alt="" />
              <div v-else class="avatar-placeholder">🎤</div>
            </div>
            <div class="artist-name">{{ artist.name }}</div>
            <div class="artist-bio">{{ artist.bio || '暂无简介' }}</div>
          </div>
        </div>
        <div v-else class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <p>未找到相关歌手</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const searchInput = ref('')
const activeTab = ref('songs')
const loading = ref(false)
const songResults = ref<any[]>([])
const albumResults = ref<any[]>([])
const artistResults = ref<any[]>([])

const playSongGlobal = inject<(song: any) => void>('playSong')

async function search() {
  if (!keyword.value.trim()) return
  
  loading.value = true
  try {
    const [songs, albums, artists] = await Promise.all([
      http.get('/songs/page', { params: { keyword: keyword.value, page: 0, size: 50 } }),
      http.get('/albums/page', { params: { keyword: keyword.value, page: 0, size: 24 } }),
      http.get('/artists/page', { params: { keyword: keyword.value, page: 0, size: 30 } })
    ])
    
    songResults.value = songs.data.content
    albumResults.value = albums.data.content
    artistResults.value = artists.data.content
    
    // 自动切换到有结果的标签
    if (songResults.value.length > 0) {
      activeTab.value = 'songs'
    } else if (albumResults.value.length > 0) {
      activeTab.value = 'albums'
    } else if (artistResults.value.length > 0) {
      activeTab.value = 'artists'
    }
  } catch (err: any) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

function performSearch() {
  if (searchInput.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchInput.value)}`)
  }
}

function playSong(song: any) {
  if (!song.audioUrl) {
    ElMessage.warning('该歌曲暂无音频')
    return
  }
  playSongGlobal?.(song)
}

function formatDuration(sec: number | undefined): string {
  if (!sec) return '-'
  const mins = Math.floor(sec / 60)
  const secs = sec % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

watch(() => route.query.q, (newVal) => {
  if (newVal) {
    keyword.value = newVal as string
    searchInput.value = newVal as string
    search()
  }
}, { immediate: true })

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q as string
    searchInput.value = route.query.q as string
    search()
  }
})
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* 搜索头部 */
.search-header {
  background: white;
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
}

.search-title h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1f2937;
}

.search-icon {
  color: #8b5cf6;
}

.search-query {
  font-size: 14px;
  color: #6b7280;
}

.keyword-tag {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  border-radius: 12px;
  font-weight: 600;
  font-size: 13px;
}

/* 快速搜索框 */
.quick-search {
  display: flex;
  align-items: center;
  background: #f9fafb;
  border-radius: 28px;
  padding: 8px 16px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s;
  min-width: 400px;
}

.quick-search:focus-within {
  border-color: #8b5cf6;
  background: white;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.search-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 15px;
  color: #1f2937;
}

.search-input::placeholder {
  color: #9ca3af;
}

.search-btn {
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  border: none;
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

/* 统计标签 */
.search-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 2px solid transparent;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.stat-item.active {
  border-color: #8b5cf6;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%);
}

.stat-icon {
  font-size: 32px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  flex: 1;
}

.stat-count {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 结果容器 */
.results-container {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  min-height: 400px;
}

.results-section {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 歌曲列表 */
.song-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.song-item {
  display: grid;
  grid-template-columns: 40px 64px 1fr 120px 80px;
  gap: 16px;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.2s;
}

.song-item:hover {
  background: linear-gradient(90deg, rgba(139, 92, 246, 0.05) 0%, rgba(236, 72, 153, 0.05) 100%);
}

.song-index {
  text-align: center;
  font-size: 14px;
  color: #9ca3af;
  font-weight: 600;
}

.song-cover {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.song-cover:hover .play-overlay {
  opacity: 1;
}

.song-info {
  min-width: 0;
}

.song-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s;
}

.song-title:hover {
  color: #8b5cf6;
}

.song-meta {
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.separator {
  margin: 0 6px;
}

.genre-badge {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  font-size: 12px;
  border-radius: 12px;
  font-weight: 500;
}

.song-duration {
  text-align: right;
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

/* 专辑网格 */
.album-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.album-card {
  cursor: pointer;
  transition: all 0.3s;
}

.album-card:hover {
  transform: translateY(-8px);
}

.album-cover {
  position: relative;
  width: 100%;
  padding-top: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.album-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.album-card:hover .album-cover img {
  transform: scale(1.1);
}

.album-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.album-card:hover .album-overlay {
  opacity: 1;
}

.album-info {
  margin-top: 12px;
}

.album-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-artist {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-date {
  font-size: 12px;
  color: #9ca3af;
}

/* 歌手网格 */
.artist-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 24px;
}

.artist-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 16px;
  border-radius: 12px;
}

.artist-card:hover {
  background: #f9fafb;
  transform: translateY(-8px);
}

.artist-avatar {
  width: 100%;
  padding-top: 100%;
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin-bottom: 12px;
}

.artist-avatar img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.artist-card:hover .artist-avatar img {
  transform: scale(1.1);
}

.avatar-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
}

.artist-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.artist-bio {
  font-size: 12px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #9ca3af;
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 8px 0;
  font-size: 16px;
}

.empty-tip {
  font-size: 14px;
  color: #d1d5db;
}
</style>

