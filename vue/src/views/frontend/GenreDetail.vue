<template>
  <div class="genre-detail" v-loading="loading">
    <div v-if="genre" class="detail-container">
      <div class="genre-header">
        <div class="genre-icon-large">{{ getGenreIcon(genre.name) }}</div>
        <div class="genre-info">
          <h1 class="genre-name">{{ genre.name }}</h1>
          <div class="genre-stat">
            <span class="stat-value">{{ songs.length }}</span>
            <span class="stat-label">首歌曲</span>
          </div>
        </div>
      </div>

      <!-- 该流派的所有歌曲 -->
      <div class="section">
        <h2 class="section-title">{{ genre.name }} 音乐</h2>
        <div class="songs-list">
          <div class="song-item" v-for="(song, index) in songs" :key="song.id" @click="playSong(song)">
            <div class="song-index">{{ index + 1 }}</div>
            <div class="song-cover-sm">
              <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
              <div v-else class="cover-placeholder">♪</div>
            </div>
            <div class="song-main">
              <div class="song-name">{{ song.title }}</div>
              <div class="song-meta">{{ song.artist?.name }} · {{ song.album?.title || '单曲' }}</div>
            </div>
            <div class="song-duration">{{ formatDuration(song.durationSec) }}</div>
            <button class="play-btn" v-if="song.audioUrl">播放</button>
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
const genre = ref<any>(null)
const songs = ref<any[]>([])

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadData() {
  const id = route.params.id
  loading.value = true
  try {
    const [genreRes, songsRes] = await Promise.all([
      http.get(`/genres/${id}`),
      http.get(`/songs/by-genre/${id}`) // 使用专用接口
    ])
    
    genre.value = genreRes.data
    songs.value = songsRes.data || []
  } catch (err: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function getGenreIcon(name: string): string {
  const icons: Record<string, string> = {
    '流行': '🎵', '摇滚': '🎸', '爵士': '🎷', '古典': '🎻',
    '电子': '🎹', '民谣': '🎤', '嘻哈': '🎧', '蓝调': '🎺'
  }
  return icons[name] || '🎼'
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
.genre-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.genre-header {
  background: white;
  border-radius: 24px;
  padding: 48px;
  margin-bottom: 32px;
  display: flex;
  gap: 32px;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.genre-icon-large {
  width: 160px;
  height: 160px;
  border-radius: 24px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 96px;
  box-shadow: 0 12px 40px rgba(139, 92, 246, 0.3);
}

.genre-info {
  flex: 1;
}

.genre-name {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.genre-stat {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 16px;
  color: #6b7280;
}

.section {
  background: white;
  border-radius: 16px;
  padding: 32px;
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
</style>



