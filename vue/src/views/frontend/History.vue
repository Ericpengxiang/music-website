<template>
  <div class="history-page">
    <div class="page-header">
      <h1 class="page-title"><span class="title-icon">🕒</span> 播放历史</h1>
    </div>

    <div v-if="!isLoggedIn" class="need-login">
      <p>登录后可查看播放历史。</p>
      <el-button type="primary" @click="$router.push('/user/login')">去登录</el-button>
    </div>

    <div v-else class="songs-grid" v-loading="loading">
      <div class="song-item" v-for="(item, index) in list" :key="index">
        <div class="song-time">{{ formatDate(item.createdAt) }}</div>
        <div class="song-cover-sm" @click="playSong(item.song)">
          <img v-if="item.song?.album?.coverUrl" :src="getImageUrl(item.song.album.coverUrl)" alt="" />
          <div v-else class="cover-placeholder">♪</div>
          <div class="play-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="white"><path d="M8 5v14l11-7z"/></svg>
          </div>
        </div>
        <div class="song-main" @click="playSong(item.song)">
          <div class="song-name">{{ item.song?.title }}</div>
          <div class="song-meta">{{ item.song?.artist?.name }} · {{ item.song?.album?.title }}</div>
        </div>
        <div class="song-duration">{{ formatDuration(item.playDuration) }}</div>
      </div>
      <div v-if="list.length === 0" class="empty">暂无播放历史</div>
    </div>

    <div class="pagination-wrapper" v-if="list.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="loadList"
        @current-change="loadList"
      />
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
const list = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadList() {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const { data } = await http.get('/history', {
      params: { page: currentPage.value - 1, size: pageSize.value }
    })
    list.value = data.content || []
    total.value = data.totalElements || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
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

function playSong(song: any) {
  if (!song || !song.audioUrl) {
    ElMessage.warning('该歌曲暂无音频文件')
    return
  }
  playSongGlobal?.(song)
}

onMounted(loadList)
</script>

<style scoped>
.history-page { max-width: 1000px; margin: 0 auto; }
.page-header { background: white; padding: 24px 32px; border-radius: 16px; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.page-title { margin: 0; font-size: 28px; font-weight: 700; display: flex; align-items: center; gap: 10px; }
.title-icon { font-size: 28px; }
.need-login { background: white; padding: 48px; text-align: center; border-radius: 16px; }
.songs-grid { background: white; border-radius: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); overflow: hidden; }
.song-item { display: grid; grid-template-columns: 100px 56px 1fr 80px; gap: 16px; align-items: center; padding: 12px 24px; border-bottom: 1px solid #f5f7fa; }
.song-time { font-size: 13px; color: #6b7280; }
.song-cover-sm { position: relative; width: 56px; height: 56px; border-radius: 8px; overflow: hidden; cursor: pointer; }
.song-cover-sm img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; font-size: 24px; }
.play-icon { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.45); opacity: 0; transition: opacity .2s; }
.song-cover-sm:hover .play-icon { opacity: 1; }
.song-name { font-size: 15px; font-weight: 700; color: #1f2937; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.song-meta { font-size: 13px; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.song-duration { text-align: right; font-size: 14px; color: #9ca3af; }
.empty { padding: 40px; text-align: center; color: #9ca3af; }
.pagination-wrapper { padding: 24px; text-align: center; }
</style>






