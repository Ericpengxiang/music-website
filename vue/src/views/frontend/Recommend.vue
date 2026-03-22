<template>
  <div class="recommend-page">
    <div class="page-header">
      <h1 class="page-title"><span class="title-icon">✨</span> 猜你喜欢</h1>
      <p class="subtitle">根据你的收藏和播放记录推荐</p>
    </div>

    <div v-if="!isLoggedIn" class="need-login">
      <p>登录后查看个性化推荐</p>
      <el-button type="primary" @click="$router.push('/user/login')">去登录</el-button>
    </div>

    <div v-else class="song-grid" v-loading="loading">
      <div class="song-card" v-for="song in songs" :key="song.id" @click="playSong(song)">
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
      <div v-if="songs.length === 0" class="empty">暂无推荐，多听歌曲后再来看看吧~</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { useAuthStore } from '../../store/auth'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const isLoggedIn = computed(() => !!auth.token)
const loading = ref(false)
const songs = ref<any[]>([])

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadRecommend() {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const { data } = await http.get('/recommend/for-you', { params: { limit: 24 } })
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

function playSong(song: any) {
  if (!song.audioUrl) {
    ElMessage.warning('该歌曲暂无音频')
    return
  }
  playSongGlobal?.(song)
}

onMounted(loadRecommend)
</script>

<style scoped>
.recommend-page { max-width: 1200px; margin: 0 auto; }
.page-header { background: white; padding: 32px; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); text-align: center; }
.page-title { margin: 0 0 8px 0; font-size: 32px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 10px; }
.title-icon { font-size: 36px; }
.subtitle { margin: 0; color: #6b7280; font-size: 14px; }
.need-login { background: white; padding: 48px; text-align: center; border-radius: 16px; }

.song-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 20px; }
.song-card { cursor: pointer; transition: all 0.3s; background: white; border-radius: 12px; overflow: hidden; border: 1px solid #f0f0f0; }
.song-card:hover { transform: translateY(-8px); box-shadow: 0 12px 32px rgba(0,0,0,0.12); border-color: #8b5cf6; }
.song-cover { position: relative; width: 100%; padding-top: 100%; background: linear-gradient(135deg, #f5f7fa 0%, #e5e7eb 100%); overflow: hidden; }
.song-cover img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.song-card:hover .song-cover img { transform: scale(1.1); }
.song-cover-placeholder { position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: rgba(0,0,0,0.2); background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.song-play-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s; }
.song-card:hover .song-play-overlay { opacity: 1; }
.song-info { padding: 12px; }
.song-title { font-size: 14px; font-weight: 600; color: #1f2937; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.song-artist { font-size: 12px; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty { padding: 60px; text-align: center; color: #9ca3af; grid-column: 1 / -1; }
</style>





