<template>
  <div class="detail-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ playlist?.name || '歌单详情' }}</h1>
        <p class="desc" v-if="playlist?.description">{{ playlist.description }}</p>
      </div>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <div class="songs-grid" v-loading="loading">
      <div class="song-item" v-for="song in songs" :key="song.id">
        <div class="song-cover-sm" @click="playSong(song)">
          <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
          <div v-else class="cover-placeholder">♪</div>
          <div class="play-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="white"><path d="M8 5v14l11-7z"/></svg>
          </div>
        </div>
        <div class="song-main" @click="playSong(song)">
          <div class="song-name">{{ song.title }}</div>
          <div class="song-meta">{{ song.artist?.name }} · {{ song.album?.title }}</div>
        </div>
        <div class="song-actions">
          <el-popconfirm title="确定从歌单移除？" @confirm="removeSong(song.id)">
            <template #reference>
              <el-button size="small" text type="danger">移除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
      <div v-if="songs.length === 0" class="empty">歌单暂无歌曲，去歌曲库添加吧~</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const route = useRoute()
const playlistId = Number(route.params.id)
const loading = ref(false)
const playlist = ref<any>(null)
const songIds = ref<number[]>([])
const songs = ref<any[]>([])
const playSongGlobal = inject<(song: any) => void>('playSong')

async function load() {
  loading.value = true
  try {
    // 获取歌单基础信息
    const listRes = await http.get('/playlists')
    playlist.value = (listRes.data || []).find((p: any) => p.id === playlistId)
    
    // 获取歌单内歌曲ID
    const songsRes = await http.get(`/playlists/${playlistId}/songs`)
    songIds.value = (songsRes.data || []).map((ps: any) => ps.songId)
    
    // 批量拉取歌曲详情
    if (songIds.value.length > 0) {
      const list = await Promise.all(songIds.value.map(id => http.get(`/songs/${id}`).then(r => r.data).catch(() => null)))
      songs.value = list.filter(Boolean)
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function removeSong(songId: number) {
  try {
    await http.delete(`/playlists/${playlistId}/songs/${songId}`)
    songs.value = songs.value.filter(s => s.id !== songId)
    ElMessage.success('已移除')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function playSong(song: any) {
  if (!song.audioUrl) {
    ElMessage.warning('该歌曲暂无音频文件')
    return
  }
  playSongGlobal?.(song)
}

onMounted(load)
</script>

<style scoped>
.detail-page { max-width: 1000px; margin: 0 auto; }
.page-header { background: white; padding: 24px 32px; border-radius: 16px; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); display: flex; justify-content: space-between; align-items: center; }
.page-title { margin: 0 0 8px 0; font-size: 28px; font-weight: 700; }
.desc { margin: 0; color: #6b7280; }
.songs-grid { background: white; border-radius: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); overflow: hidden; }
.song-item { display: grid; grid-template-columns: 56px 1fr 100px; gap: 16px; align-items: center; padding: 12px 24px; border-bottom: 1px solid #f5f7fa; }
.song-cover-sm { position: relative; width: 56px; height: 56px; border-radius: 8px; overflow: hidden; cursor: pointer; }
.song-cover-sm img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; font-size: 24px; }
.play-icon { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.45); opacity: 0; transition: opacity .2s; }
.song-cover-sm:hover .play-icon { opacity: 1; }
.song-name { font-size: 15px; font-weight: 700; color: #1f2937; }
.song-meta { font-size: 13px; color: #6b7280; }
.empty { padding: 40px; text-align: center; color: #9ca3af; }
</style>






