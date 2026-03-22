<template>
  <div class="ranking-page">
    <div class="page-header">
      <h1 class="page-title"><span class="title-icon">🏆</span> 排行榜</h1>
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadRanking">
      <el-tab-pane label="🔥 热门榜" name="hot">
        <div class="songs-list" v-loading="loading">
          <div class="song-item" v-for="(song, index) in songs" :key="song.id" @click="playSong(song)">
            <div class="rank" :class="`rank-${index + 1}`">{{ index + 1 }}</div>
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
      </el-tab-pane>

      <el-tab-pane label="🆕 新歌榜" name="new">
        <div class="songs-list" v-loading="loading">
          <div class="song-item" v-for="(song, index) in songs" :key="song.id" @click="playSong(song)">
            <div class="rank">{{ index + 1 }}</div>
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
      </el-tab-pane>

      <el-tab-pane label="❤️ 收藏榜" name="favorite">
        <div class="songs-list" v-loading="loading">
          <div class="song-item" v-for="(song, index) in songs" :key="song.id" @click="playSong(song)">
            <div class="rank">{{ index + 1 }}</div>
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const activeTab = ref('hot')
const loading = ref(false)
const songs = ref<any[]>([])

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadRanking() {
  loading.value = true
  try {
    const endpoint = activeTab.value === 'hot' ? '/ranking/hot' : 
                     activeTab.value === 'new' ? '/ranking/new' : '/ranking/favorite'
    const { data } = await http.get(endpoint, { params: { limit: 50 } })
    songs.value = data || []
  } catch (e) {
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

onMounted(loadRanking)
</script>

<style scoped>
.ranking-page { max-width: 1000px; margin: 0 auto; }
.page-header { background: white; padding: 32px; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); text-align: center; }
.page-title { margin: 0 0 8px 0; font-size: 32px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 10px; }
.title-icon { font-size: 36px; }
.subtitle { margin: 0; color: #6b7280; font-size: 14px; }
.need-login { background: white; padding: 48px; text-align: center; border-radius: 16px; }

.songs-list { background: white; border-radius: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); overflow: hidden; padding: 16px 0; }
.song-item { display: grid; grid-template-columns: 60px 56px 1fr 80px 100px; gap: 16px; align-items: center; padding: 12px 24px; cursor: pointer; transition: background 0.2s; border-radius: 8px; margin: 0 16px; }
.song-item:hover { background: linear-gradient(90deg, rgba(139, 92, 246, 0.05) 0%, rgba(236, 72, 153, 0.05) 100%); }

.rank { font-size: 18px; font-weight: 700; text-align: center; color: #6b7280; }
.rank-1 { color: #ef4444; font-size: 24px; }
.rank-2 { color: #f97316; font-size: 22px; }
.rank-3 { color: #eab308; font-size: 20px; }

.song-cover-sm { width: 56px; height: 56px; border-radius: 8px; overflow: hidden; }
.song-cover-sm img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; font-size: 24px; color: white; }

.song-main { min-width: 0; }
.song-name { font-size: 15px; font-weight: 600; color: #1f2937; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.song-meta { font-size: 13px; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.song-duration { text-align: right; font-size: 14px; color: #9ca3af; }

.play-btn { padding: 8px 20px; background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); color: white; border: none; border-radius: 20px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.3s; }
.play-btn:hover { transform: scale(1.05); box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4); }

:deep(.el-tabs__item) { font-size: 16px; font-weight: 600; }
:deep(.el-tabs__item.is-active) { color: #8b5cf6; }
:deep(.el-tabs__active-bar) { background: linear-gradient(90deg, #ec4899 0%, #8b5cf6 100%); }
</style>





