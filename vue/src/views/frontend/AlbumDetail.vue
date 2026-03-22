<template>
  <div class="album-detail" v-loading="loading">
    <div v-if="album" class="detail-container">
      <!-- 专辑头部信息 -->
      <div class="album-header">
        <div class="album-cover">
          <el-image 
            :src="getImageUrl(album.coverUrl)" 
            fit="cover"
          >
            <template #error>
              <div class="cover-placeholder">💿</div>
            </template>
          </el-image>
        </div>
        <div class="album-info">
          <div class="album-type">专辑</div>
          <h1 class="album-title">{{ album.title }}</h1>
          <div class="album-artist" @click="$router.push(`/artist/${album.artist?.id}`)">
            {{ album.artist?.name || '未知歌手' }}
          </div>
          <div class="album-meta">
            <span v-if="album.releaseDate">发行日期：{{ album.releaseDate }}</span>
            <span v-if="songs.length > 0">歌曲数：{{ songs.length }} 首</span>
          </div>
          <div class="album-actions">
            <button class="btn-play-all" @click="playAll">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M8 5v14l11-7z"/>
              </svg>
              播放全部
            </button>
          </div>
        </div>
      </div>

      <!-- 歌曲列表 -->
      <div class="section">
        <h2 class="section-title">歌曲列表</h2>
        <div class="songs-list">
          <div class="song-item" v-for="(song, index) in songs" :key="song.id" @click="playSong(song)">
            <div class="song-index">{{ index + 1 }}</div>
            <div class="song-main">
              <div class="song-name">{{ song.title }}</div>
              <div class="song-meta">
                <span>{{ song.artist?.name }}</span>
                <span v-if="song.genre"> · {{ song.genre.name }}</span>
              </div>
            </div>
            <div class="song-duration">{{ formatDuration(song.durationSec) }}</div>
            <button class="play-btn-sm" v-if="song.audioUrl">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </button>
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
const album = ref<any>(null)
const songs = ref<any[]>([])

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadData() {
  const id = route.params.id
  loading.value = true
  try {
    const [albumRes, songsRes] = await Promise.all([
      http.get(`/albums/${id}`),
      http.get(`/songs/by-album/${id}`) // 使用专用接口
    ])
    
    album.value = albumRes.data
    songs.value = songsRes.data || []
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

function playAll() {
  if (songs.value.length === 0) {
    ElMessage.warning('暂无歌曲')
    return
  }
  const firstSong = songs.value.find(s => s.audioUrl)
  if (firstSong) {
    playSong(firstSong)
  } else {
    ElMessage.warning('暂无可播放歌曲')
  }
}

onMounted(loadData)
</script>

<style scoped>
.album-detail {
  max-width: 1200px;
  margin: 0 auto;
}

/* 专辑头部 */
.album-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 24px;
  padding: 48px;
  margin-bottom: 32px;
  display: flex;
  gap: 40px;
}

.album-cover {
  width: 280px;
  height: 280px;
  border-radius: 16px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 16px 48px rgba(0,0,0,0.2);
}

.album-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 96px;
  color: white;
}

.album-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.album-type {
  font-size: 13px;
  font-weight: 600;
  color: #8b5cf6;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.album-title {
  font-size: 56px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #1f2937;
  line-height: 1.1;
}

.album-artist {
  font-size: 20px;
  color: #6b7280;
  margin-bottom: 16px;
  cursor: pointer;
  transition: color 0.2s;
  width: fit-content;
}

.album-artist:hover {
  color: #8b5cf6;
}

.album-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 32px;
}

.album-actions {
  display: flex;
  gap: 12px;
}

.btn-play-all {
  padding: 14px 32px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  border: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn-play-all:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.4);
}

/* 歌曲列表 */
.section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 20px 0;
  color: #1f2937;
}

.songs-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.song-item {
  display: grid;
  grid-template-columns: 40px 1fr 80px 60px;
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

.play-btn-sm {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.play-btn-sm:hover {
  transform: scale(1.1);
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



