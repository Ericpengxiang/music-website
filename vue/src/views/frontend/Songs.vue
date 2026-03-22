<template>
  <div class="songs-page">
    <div class="page-header">
      <h1 class="page-title">
        <span class="title-icon">🎵</span>
        歌曲库
      </h1>
      <div class="search-bar">
        <input 
          v-model="keyword" 
          placeholder="搜索歌曲名称、歌手..." 
          class="search-input"
          @input="searchSongs"
        />
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.35-4.35"/>
        </svg>
      </div>
    </div>

    <div class="songs-grid" v-loading="loading">
      <div class="song-item" v-for="(song, index) in songs" :key="song.id">
        <div class="song-index">{{ (currentPage - 1) * pageSize + index + 1 }}</div>
        <div class="song-cover-sm" @click="playSong(song)">
          <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
          <div v-else class="cover-placeholder">♪</div>
          <div class="play-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="white">
              <path d="M8 5v14l11-7z"/>
            </svg>
          </div>
        </div>
        <div class="song-main">
          <div class="song-name" @click="$router.push(`/song/${song.id}`)">{{ song.title }}</div>
          <div class="song-meta">
            <span class="meta-artist">{{ song.artist?.name }}</span>
            <span v-if="song.album" class="meta-sep">·</span>
            <span v-if="song.album" class="meta-album">{{ song.album.title }}</span>
          </div>
        </div>
        <div class="song-genre">
          <span v-if="song.genre" class="genre-tag">{{ song.genre.name }}</span>
        </div>
        <div class="song-duration">{{ formatDuration(song.durationSec) }}</div>
        <div class="song-actions">
          <button class="heart" :class="{ on: isFavorited(song.id) }" @click.stop="toggleFavorite(song)">
            <svg v-if="isFavorited(song.id)" width="22" height="22" viewBox="0 0 24 24" fill="#ef4444"><path d="M12 21s-6.716-4.42-9.428-7.132C.86 12.156.5 10.5 1.07 9.07 1.72 7.4 3.227 6.25 5 6.25c1.3 0 2.536.54 3.4 1.5L12 11.5l3.6-3.75c.864-.96 2.1-1.5 3.4-1.5 1.773 0 3.28 1.15 3.93 2.82.57 1.43.21 3.086-1.502 4.798C18.716 16.58 12 21 12 21z"/></svg>
            <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78l1.06 1.06L12 21l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          </button>
          <el-dropdown @command="(plId: number) => addToPlaylist(song.id, plId)">
            <button class="add-btn" @click.stop>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 5v14M5 12h14"/>
              </svg>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="pl in myPlaylists" :key="pl.id" :command="pl.id">{{ pl.name }}</el-dropdown-item>
                <el-dropdown-item v-if="myPlaylists.length === 0" disabled>暂无歌单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const songs = ref<any[]>([])
const favoritedIds = ref<Set<number>>(new Set())
const myPlaylists = ref<any[]>([])
const keyword = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/songs/page', {
      params: { keyword: keyword.value || undefined, page: currentPage.value - 1, size: pageSize.value }
    })
    songs.value = data.content
    total.value = data.totalElements
  } catch (err: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function searchSongs() {
  currentPage.value = 1
  loadList()
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

function playSong(row: any) {
  if (!row.audioUrl) {
    ElMessage.warning('该歌曲暂无音频文件')
    return
  }
  playSongGlobal?.(row)
}

async function loadFavoritedIds() {
  try {
    const { data } = await http.get('/favorites/songs/check')
    const set = new Set<number>()
    for (const id of data?.favoritedIds || []) set.add(Number(id))
    favoritedIds.value = set
  } catch {}
}

function isFavorited(id: number): boolean {
  return favoritedIds.value.has(id)
}

async function toggleFavorite(song: any) {
  try {
    if (isFavorited(song.id)) {
      await http.delete(`/favorites/songs/${song.id}`)
      favoritedIds.value.delete(song.id)
    } else {
      await http.post(`/favorites/songs/${song.id}`)
      favoritedIds.value.add(song.id)
    }
  } catch (e) {
    ElMessage.error('请先登录后再收藏')
  }
}

async function loadPlaylists() {
  try {
    const { data } = await http.get('/playlists')
    myPlaylists.value = data || []
  } catch {}
}

async function addToPlaylist(songId: number, playlistId: number) {
  try {
    await http.post(`/playlists/${playlistId}/songs/${songId}`)
    ElMessage.success('已添加到歌单')
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

onMounted(loadList)
onMounted(loadFavoritedIds)
onMounted(loadPlaylists)
</script>

<style scoped>
.songs-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  background: white;
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1f2937;
}

.title-icon {
  font-size: 40px;
}

.search-bar {
  position: relative;
  width: 400px;
}

.search-input {
  width: 100%;
  padding: 12px 40px 12px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 28px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.search-input:focus {
  border-color: #8b5cf6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.search-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

/* 歌曲列表 */
.songs-grid {
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  overflow: hidden;
}

.song-item {
  display: grid;
  grid-template-columns: 48px 56px 1fr 120px 80px 100px;
  gap: 16px;
  align-items: center;
  padding: 12px 24px;
  border-bottom: 1px solid #f5f7fa;
  transition: all 0.2s;
}

.song-item:hover {
  background: linear-gradient(90deg, rgba(139, 92, 246, 0.05) 0%, rgba(236, 72, 153, 0.05) 100%);
}

.song-item:hover .song-index {
  opacity: 0;
}

.song-item:hover .play-icon {
  opacity: 1;
}

.song-index {
  text-align: center;
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
  transition: opacity 0.2s;
}

.song-cover-sm {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
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

.play-icon {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.song-main {
  min-width: 0;
}

.song-name {
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

.song-name:hover {
  color: #8b5cf6;
}

.song-meta {
  font-size: 13px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-sep {
  margin: 0 6px;
}

.genre-tag {
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

.song-actions { display: flex; justify-content: flex-end; gap: 8px; }
.heart { background: transparent; border: none; cursor: pointer; padding: 6px; }
.heart.on { transform: scale(1.05); }
.add-btn { background: transparent; border: 1px solid #e5e7eb; border-radius: 50%; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; color: #8b5cf6; }
.add-btn:hover { background: #f3f4f6; border-color: #8b5cf6; }

.pagination-wrapper {
  padding: 24px;
  text-align: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
}
</style>
