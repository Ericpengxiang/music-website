<template>
  <div class="fav-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">❤️ 我的收藏</h1>
        <p class="page-subtitle">共 {{ songs.length }} 首歌曲</p>
      </div>
      <el-radio-group v-model="viewMode" size="small">
        <el-radio-button value="grid">网格</el-radio-button>
        <el-radio-button value="list">列表</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="!isLoggedIn" class="need-login">
      <p>登录后可查看收藏</p>
      <el-button type="primary" @click="$router.push('/user/login')">去登录</el-button>
    </div>

    <!-- 网格视图 -->
    <div v-else-if="viewMode === 'grid'" class="song-grid" v-loading="loading">
      <div class="song-card" v-for="song in songs" :key="song.id">
        <div class="song-cover" @click="playSong(song)">
          <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
          <div v-else class="cover-placeholder">♪</div>
          <div class="play-overlay">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="white"><path d="M8 5v14l11-7z"/></svg>
          </div>
        </div>
        <div class="song-info">
          <div class="song-title">{{ song.title }}</div>
          <div class="song-artist">{{ song.artist?.name }}</div>
        </div>
        <button class="unfavorite-btn" @click="unfavorite(song.id)" title="取消收藏">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#ef4444">
            <path d="M12 21s-6.716-4.42-9.428-7.132C.86 12.156.5 10.5 1.07 9.07 1.72 7.4 3.227 6.25 5 6.25c1.3 0 2.536.54 3.4 1.5L12 11.5l3.6-3.75c.864-.96 2.1-1.5 3.4-1.5 1.773 0 3.28 1.15 3.93 2.82.57 1.43.21 3.086-1.502 4.798C18.716 16.58 12 21 12 21z"/>
          </svg>
        </button>
      </div>
      <div v-if="songs.length === 0" class="empty">暂无收藏，快去收藏喜欢的歌曲吧~</div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="song-list" v-loading="loading">
      <div class="list-item" v-for="song in songs" :key="song.id">
        <div class="list-cover" @click="playSong(song)">
          <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
          <div v-else class="cover-placeholder">♪</div>
        </div>
        <div class="list-main" @click="playSong(song)">
          <div class="list-title">{{ song.title }}</div>
          <div class="list-meta">{{ song.artist?.name }} · {{ song.album?.title || '单曲' }}</div>
        </div>
        <button class="list-unfavorite" @click="unfavorite(song.id)">取消收藏</button>
      </div>
      <div v-if="songs.length === 0" class="empty">暂无收藏</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject, computed } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'

const auth = useAuthStore()
const isLoggedIn = computed(() => !!auth.token)
const loading = ref(false)
const songs = ref<any[]>([])
const viewMode = ref<'grid' | 'list'>('grid')

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadFavorites() {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const { data } = await http.get('/favorites/songs')
    // 后端返回的是 UserFavoriteSong，需要关联的歌曲信息；简化起见，直接取返回中的 songId 再拉取详情
    const ids: number[] = (data || []).map((it: any) => it.songId)
    if (ids.length === 0) { songs.value = []; return }
    // 批量拉取：前端没有批量接口，先并发取详情
    const list = await Promise.all(ids.map(id => http.get(`/songs/${id}`).then(r => r.data).catch(() => null)))
    songs.value = list.filter(Boolean)
  } catch (e) {
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

async function unfavorite(songId: number) {
  try {
    await http.delete(`/favorites/songs/${songId}`)
    songs.value = songs.value.filter(s => s.id !== songId)
    ElMessage.success('已取消收藏')
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

onMounted(loadFavorites)
</script>

<style scoped>
.fav-page { max-width: 1200px; margin: 0 auto; }
.page-header { background: white; padding: 24px 32px; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); display: flex; justify-content: space-between; align-items: center; }
.page-title { margin: 0 0 4px 0; font-size: 28px; font-weight: 700; }
.page-subtitle { margin: 0; font-size: 14px; color: #6b7280; }
.need-login { background: white; padding: 60px; text-align: center; border-radius: 16px; }

/* 网格视图 */
.song-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; }
.song-card { background: white; border-radius: 12px; overflow: hidden; position: relative; border: 1px solid #f0f0f0; transition: all 0.3s; }
.song-card:hover { transform: translateY(-8px); box-shadow: 0 12px 32px rgba(0,0,0,0.12); border-color: #8b5cf6; }

.song-cover { position: relative; width: 100%; padding-top: 100%; background: #f5f7fa; overflow: hidden; cursor: pointer; }
.song-cover img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.song-card:hover .song-cover img { transform: scale(1.1); }

.cover-placeholder { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-size: 36px; }
.play-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s; }
.song-card:hover .play-overlay { opacity: 1; }

.song-info { padding: 12px; }
.song-title { font-size: 14px; font-weight: 600; color: #1f2937; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.song-artist { font-size: 12px; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.unfavorite-btn { position: absolute; top: 8px; right: 8px; width: 32px; height: 32px; border-radius: 50%; border: none; background: rgba(255,255,255,0.9); backdrop-filter: blur(10px); cursor: pointer; display: flex; align-items: center; justify-content: center; opacity: 0; transition: all 0.3s; }
.song-card:hover .unfavorite-btn { opacity: 1; }
.unfavorite-btn:hover { transform: scale(1.1); background: white; }

/* 列表视图 */
.song-list { background: white; border-radius: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); overflow: hidden; }
.list-item { display: grid; grid-template-columns: 60px 1fr 120px; gap: 16px; align-items: center; padding: 12px 24px; border-bottom: 1px solid #f5f7fa; transition: background 0.2s; cursor: pointer; }
.list-item:hover { background: linear-gradient(90deg, rgba(139, 92, 246, 0.05) 0%, rgba(236, 72, 153, 0.05) 100%); }

.list-cover { width: 60px; height: 60px; border-radius: 8px; overflow: hidden; }
.list-cover img { width: 100%; height: 100%; object-fit: cover; }
.list-main { min-width: 0; }
.list-title { font-size: 15px; font-weight: 600; color: #1f2937; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.list-meta { font-size: 13px; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.list-unfavorite { padding: 8px 20px; background: transparent; border: 1px solid #e5e7eb; border-radius: 20px; color: #ef4444; cursor: pointer; font-size: 13px; transition: all 0.3s; }
.list-unfavorite:hover { background: #fef2f2; border-color: #ef4444; }

.empty { padding: 60px; text-align: center; color: #9ca3af; grid-column: 1 / -1; }
</style>



