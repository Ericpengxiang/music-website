<template>
  <div class="song-detail" v-loading="loading">
    <div v-if="song" class="detail-container">
      <!-- 歌曲信息 -->
      <div class="song-header">
        <div class="song-cover-large">
          <img v-if="song.album?.coverUrl" :src="getImageUrl(song.album.coverUrl)" alt="" />
          <div v-else class="cover-placeholder">🎵</div>
        </div>
        <div class="song-info">
          <div class="song-type">歌曲</div>
          <h1 class="song-title">{{ song.title }}</h1>
          <div class="song-artist" @click="$router.push(`/artist/${song.artist?.id}`)">
            {{ song.artist?.name || '未知歌手' }}
          </div>
          <div class="song-meta">
            <span v-if="song.album" @click="$router.push(`/album/${song.album.id}`)" class="clickable">
              专辑：{{ song.album.title }}
            </span>
            <span v-if="song.genre">流派：{{ song.genre.name }}</span>
            <span v-if="song.durationSec">时长：{{ formatDuration(song.durationSec) }}</span>
          </div>
          <div class="song-actions">
            <el-button type="primary" size="large" @click="playSong(song)" v-if="song.audioUrl">
              <el-icon><VideoPlay /></el-icon> 播放
            </el-button>
            <el-button size="large" @click="toggleFavorite">
              <svg width="18" height="18" viewBox="0 0 24 24" :fill="isFavorited ? '#ef4444' : 'none'" stroke="#ef4444" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78l1.06 1.06L12 21l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- Tab切换：歌词、故事和评论 -->
      <el-tabs v-model="activeTab" class="content-tabs">
        <el-tab-pane label="歌词" name="lyric">
          <LyricDisplay :lrc="lyricContent" :current-time="0" @seek="seekToTime" />
        </el-tab-pane>
        <el-tab-pane :label="`听歌故事 (${storyCount})`" name="story">
          <MusicStorySection :song-id="song.id" />
        </el-tab-pane>
        <el-tab-pane :label="`评论 (${commentCount})`" name="comment">
          <CommentSection resource-type="song" :resource-id="song.id" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { VideoPlay } from '@element-plus/icons-vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import CommentSection from '../../components/CommentSection.vue'
import LyricDisplay from '../../components/LyricDisplay.vue'
import MusicStorySection from '../../components/MusicStorySection.vue'

const route = useRoute()
const loading = ref(false)
const song = ref<any>(null)
const activeTab = ref('story')
const lyricContent = ref('')
const commentCount = ref(0)
const storyCount = ref(0)
const isFavorited = ref(false)

const playSongGlobal = inject<(song: any) => void>('playSong')

async function loadData() {
  const id = route.params.id
  loading.value = true
  try {
    const [songRes, lyricRes, commentRes, storyRes] = await Promise.all([
      http.get(`/songs/${id}`),
      http.get(`/lyrics/song/${id}`).catch(() => ({ data: null })),
      http.get('/comments/count', { params: { resourceType: 'song', resourceId: id } }).catch(() => ({ data: { count: 0 } })),
      http.get(`/stories/count/song/${id}`).catch(() => ({ data: { count: 0 } }))
    ])
    
    song.value = songRes.data
    lyricContent.value = lyricRes.data?.content || ''
    commentCount.value = commentRes.data?.count || 0
    storyCount.value = storyRes.data?.count || 0
    
    // 检查收藏状态
    await checkFavorite()
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function checkFavorite() {
  try {
    const { data } = await http.get(`/favorites/songs/${song.value.id}/check`)
    isFavorited.value = data?.favorited || false
  } catch {}
}

async function toggleFavorite() {
  try {
    if (isFavorited.value) {
      await http.delete(`/favorites/songs/${song.value.id}`)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await http.post(`/favorites/songs/${song.value.id}`)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    ElMessage.error('请先登录')
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
  playSongGlobal?.(song)
}

function seekToTime(time: number) {
  // TODO: 实现跳转播放
  console.log('Seek to:', time)
}

onMounted(loadData)
</script>

<style scoped>
.song-detail { max-width: 1200px; margin: 0 auto; }

.song-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 24px;
  padding: 48px;
  margin-bottom: 32px;
  display: flex;
  gap: 40px;
}

.song-cover-large {
  width: 280px;
  height: 280px;
  border-radius: 16px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 16px 48px rgba(0,0,0,0.2);
}

.song-cover-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.song-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.song-type {
  font-size: 13px;
  font-weight: 600;
  color: #8b5cf6;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.song-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #1f2937;
  line-height: 1.1;
}

.song-artist {
  font-size: 20px;
  color: #6b7280;
  margin-bottom: 16px;
  cursor: pointer;
  transition: color 0.2s;
  width: fit-content;
}

.song-artist:hover {
  color: #8b5cf6;
}

.song-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 32px;
}

.song-meta .clickable {
  cursor: pointer;
  transition: color 0.2s;
}

.song-meta .clickable:hover {
  color: #8b5cf6;
}

.song-actions {
  display: flex;
  gap: 12px;
}

.content-tabs {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  min-height: 500px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
}

:deep(.el-tabs__item.is-active) {
  color: #8b5cf6;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #ec4899 0%, #8b5cf6 100%);
}
</style>

