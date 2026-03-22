<template>
  <div class="artist-detail" v-loading="loading">
    <div v-if="artist" class="detail-container">
      <!-- 歌手头部信息 -->
      <div class="artist-header">
        <div class="artist-avatar">
          <el-image 
            :src="getImageUrl(artist.avatarUrl)" 
            fit="cover"
          >
            <template #error>
              <div class="avatar-placeholder">🎤</div>
            </template>
          </el-image>
        </div>
        <div class="artist-info">
          <h1 class="artist-name">{{ artist.name }}</h1>
          <p class="artist-bio" v-if="artist.bio">{{ artist.bio }}</p>
          <div class="artist-stats">
            <span class="stat-item">
              <span class="stat-value">{{ songCount }}</span>
              <span class="stat-label">首歌曲</span>
            </span>
            <span class="stat-item">
              <span class="stat-value">{{ albumCount }}</span>
              <span class="stat-label">张专辑</span>
            </span>
          </div>
        </div>
      </div>

      <!-- 热门歌曲 -->
      <div class="section">
        <h2 class="section-title">热门歌曲</h2>
        <div class="songs-list">
          <div class="song-item" v-for="(song, index) in songs" :key="song.id" @click="playSong(song)">
            <div class="song-index">{{ index + 1 }}</div>
            <div class="song-cover-sm">
              <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
              <div v-else class="cover-placeholder">♪</div>
            </div>
            <div class="song-main">
              <div class="song-name">{{ song.title }}</div>
              <div class="song-meta">
                <span v-if="song.album">{{ song.album.title }}</span>
              </div>
            </div>
            <div class="song-duration">{{ formatDuration(song.durationSec) }}</div>
            <button class="play-btn" v-if="song.audioUrl">播放</button>
          </div>
        </div>
      </div>

      <!-- 专辑列表 -->
      <div class="section" v-if="albums.length > 0">
        <h2 class="section-title">专辑</h2>
        <div class="albums-grid">
          <div class="album-card" v-for="album in albums" :key="album.id" @click="$router.push(`/album/${album.id}`)">
            <el-image 
              :src="getImageUrl(album.coverUrl)" 
              style="width: 100%; aspect-ratio: 1; border-radius: 12px;"
              fit="cover"
            >
              <template #error>
                <div class="cover-placeholder">💿</div>
              </template>
            </el-image>
            <div class="album-title">{{ album.title }}</div>
            <div class="album-date">{{ album.releaseDate || '-' }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const artist = ref<any>(null)
const songs = ref<any[]>([])
const albums = ref<any[]>([])
const songCount = ref(0)
const albumCount = ref(0)

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadData() {
  const id = route.params.id
  loading.value = true
  try {
    const [artistRes, songsRes, albumsRes] = await Promise.all([
      http.get(`/artists/${id}`),
      http.get(`/songs/by-artist/${id}`), // 使用专用接口
      http.get('/albums')
    ])
    
    artist.value = artistRes.data
    songs.value = songsRes.data || []
    
    // 筛选该歌手的专辑
    albums.value = albumsRes.data.filter((a: any) => a.artist?.id == id)
    
    songCount.value = songs.value.length
    albumCount.value = albums.value.length
  } catch (err: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function formatDuration(sec: number | undefined): string {
  if (!sec) return '-'
  const mins = Math.floor(sec / 60)
  const secs = sec % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
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
.artist-detail {
  max-width: 1200px;
  margin: 0 auto;
}

/* 歌手头部 */
.artist-header {
  background: white;
  border-radius: 24px;
  padding: 48px;
  margin-bottom: 32px;
  display: flex;
  gap: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.artist-avatar {
  width: 240px;
  height: 240px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}

.artist-avatar :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 96px;
  color: white;
}

.artist-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.artist-name {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.artist-bio {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.8;
  margin: 0 0 24px 0;
}

.artist-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 14px;
  color: #9ca3af;
}

/* 内容区域 */
.section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 24px 0;
  color: #1f2937;
}

/* 歌曲列表 */
.songs-list {
  display: flex;
  flex-direction: column;
}

.song-item {
  display: grid;
  grid-template-columns: 40px 56px 1fr 80px 100px;
  gap: 16px;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.song-item:hover {
  background: linear-gradient(90deg, rgba(139, 92, 246, 0.05) 0%, rgba(236, 72, 153, 0.05) 100%);
}

.song-index {
  text-align: center;
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

.song-cover-sm {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
}

.song-cover-sm img {
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

.song-main {
  min-width: 0;
}

.song-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.song-meta {
  font-size: 13px;
  color: #6b7280;
}

.song-duration {
  text-align: right;
  font-size: 14px;
  color: #9ca3af;
}

.play-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.play-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

/* 专辑网格 */
.albums-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.album-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.album-card:hover {
  transform: translateY(-8px);
}

.album-title {
  margin-top: 12px;
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

.album-date {
  margin-top: 4px;
  font-size: 12px;
  color: #9ca3af;
}
</style>



