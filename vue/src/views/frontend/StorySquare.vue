<template>
  <div class="story-square">
    <div class="page-header">
      <div>
        <h1 class="page-title">📖 音乐故事广场</h1>
        <p class="page-subtitle">每首歌背后，都有一个动人的故事</p>
      </div>
    </div>

    <!-- 情感标签过滤 -->
    <div class="filter-bar">
      <button 
        v-for="emo in emotions" 
        :key="emo.value" 
        :class="['emotion-btn', { active: selectedEmotion === emo.value }]"
        @click="selectEmotion(emo.value)"
      >
        <span class="emotion-icon">{{ emo.icon }}</span>
        <span class="emotion-label">{{ emo.label }}</span>
      </button>
    </div>

    <!-- 故事列表 -->
    <div class="story-grid" v-loading="loading">
      <div class="story-card" v-for="story in stories" :key="story.id">
        <div class="card-header">
          <div class="user-info">
            <div class="user-avatar">
              <img v-if="story.userAvatar" :src="getImageUrl(story.userAvatar)" alt="" />
              <div v-else class="avatar-placeholder">{{ story.username?.charAt(0) }}</div>
            </div>
            <div class="user-text">
              <div class="username">{{ story.username }}</div>
              <div class="time">{{ formatTime(story.createdAt) }}</div>
            </div>
          </div>
          <div class="tags">
            <span v-if="story.emotion" class="emotion-tag" :class="`emotion-${story.emotion}`">
              {{ getEmotionIcon(story.emotion) }} {{ story.emotion }}
            </span>
            <span v-if="story.featured" class="featured-badge">精选</span>
          </div>
        </div>

        <div class="story-content">
          <p class="story-text">{{ story.story }}</p>
        </div>

        <div class="card-footer">
          <button class="like-btn" :class="{liked: isLiked(story.id)}" @click="toggleLike(story)">
            <svg width="18" height="18" viewBox="0 0 24 24" :fill="isLiked(story.id) ? '#ef4444' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78l1.06 1.06L12 21l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <span>{{ story.likeCount || 0 }}</span>
          </button>
          <button class="delete-btn" v-if="isMyStory(story)" @click="deleteStory(story.id)">删除</button>
        </div>
      </div>

      <div v-if="stories.length === 0 && !loading" class="empty">
        暂无{{ selectedEmotion ? selectedEmotion : '' }}故事，快来分享吧~
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="loadStories"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../../store/auth'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()

const emotions = [
  { value: '', label: '精选故事', icon: '⭐' },
  { value: '治愈', label: '治愈', icon: '💊' },
  { value: '励志', label: '励志', icon: '💪' },
  { value: '伤感', label: '伤感', icon: '😢' },
  { value: '怀旧', label: '怀旧', icon: '📷' },
  { value: '温暖', label: '温暖', icon: '☀️' }
]

const loading = ref(false)
const stories = ref<any[]>([])
const selectedEmotion = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const likedStoryIds = ref<Set<number>>(new Set())

async function loadStories() {
  loading.value = true
  try {
    let endpoint = '/stories/featured'
    const params = { page: currentPage.value - 1, size: pageSize.value }
    
    if (selectedEmotion.value) {
      endpoint = `/stories/emotion/${selectedEmotion.value}`
    }
    
    const { data } = await http.get(endpoint, { params })
    stories.value = data.content || []
    total.value = data.totalElements || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function loadLikedIds() {
  if (!auth.token) return
  try {
    const { data } = await http.get('/stories/likes/check')
    const set = new Set<number>()
    for (const id of data?.likedStoryIds || []) set.add(Number(id))
    likedStoryIds.value = set
  } catch {}
}

function selectEmotion(emotion: string) {
  selectedEmotion.value = emotion
  currentPage.value = 1
  loadStories()
}

async function toggleLike(story: any) {
  if (!auth.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isLiked(story.id)) {
      await http.delete(`/stories/${story.id}/like`)
      likedStoryIds.value.delete(story.id)
      story.likeCount--
    } else {
      await http.post(`/stories/${story.id}/like`)
      likedStoryIds.value.add(story.id)
      story.likeCount++
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function deleteStory(storyId: number) {
  try {
    await http.delete(`/stories/${storyId}`)
    ElMessage.success('已删除')
    await loadStories()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function isLiked(storyId: number): boolean {
  return likedStoryIds.value.has(storyId)
}

function isMyStory(story: any): boolean {
  return auth.username === story.username
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function getEmotionIcon(emotion: string): string {
  const icons: Record<string, string> = {
    '治愈': '💊', '励志': '💪', '伤感': '😢', '怀旧': '📷', '温暖': '☀️'
  }
  return icons[emotion] || '📖'
}

function formatTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

onMounted(() => {
  loadStories()
  loadLikedIds()
})
</script>

<style scoped>
.story-square { max-width: 1200px; margin: 0 auto; }

.page-header { background: white; padding: 32px; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); text-align: center; }
.page-title { margin: 0 0 8px 0; font-size: 32px; font-weight: 700; }
.page-subtitle { margin: 0; color: #6b7280; font-size: 15px; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; justify-content: center; }
.emotion-btn { padding: 12px 24px; border-radius: 24px; border: 2px solid #e5e7eb; background: white; cursor: pointer; font-size: 14px; font-weight: 500; transition: all 0.3s; display: flex; align-items: center; gap: 8px; color: #6b7280; }
.emotion-btn:hover { border-color: #8b5cf6; color: #8b5cf6; transform: translateY(-2px); }
.emotion-btn.active { background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); color: white; border-color: transparent; box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3); }
.emotion-icon { font-size: 18px; }

.story-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
.story-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); border: 1px solid #f0f0f0; transition: all 0.3s; }
.story-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.12); border-color: #8b5cf6; transform: translateY(-4px); }

.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.user-info { display: flex; gap: 12px; align-items: center; }
.user-avatar { width: 40px; height: 40px; border-radius: 50%; overflow: hidden; }
.user-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder { width: 100%; height: 100%; background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); color: white; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; }
.user-text { flex: 1; }
.username { font-size: 15px; font-weight: 600; color: #1f2937; margin-bottom: 2px; }
.time { font-size: 12px; color: #9ca3af; }

.tags { display: flex; gap: 6px; flex-wrap: wrap; }
.emotion-tag { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.emotion-治愈 { background: #dbeafe; color: #1e40af; }
.emotion-励志 { background: #fef3c7; color: #92400e; }
.emotion-伤感 { background: #fce7f3; color: #9f1239; }
.emotion-怀旧 { background: #e0e7ff; color: #3730a3; }
.emotion-温暖 { background: #fed7aa; color: #9a3412; }
.featured-badge { padding: 4px 10px; background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); color: white; border-radius: 12px; font-size: 11px; font-weight: 600; }

.story-content { margin-bottom: 16px; }
.story-text { font-size: 15px; line-height: 1.8; color: #374151; margin: 0; white-space: pre-wrap; word-break: break-word; }

.card-footer { display: flex; gap: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.like-btn { background: none; border: none; color: #6b7280; cursor: pointer; padding: 8px 16px; border-radius: 20px; transition: all 0.2s; display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 500; }
.like-btn:hover { background: #f3f4f6; color: #1f2937; }
.like-btn.liked { color: #ef4444; }
.delete-btn { background: none; border: none; color: #ef4444; cursor: pointer; padding: 8px 16px; font-size: 13px; }

.empty { padding: 80px 20px; text-align: center; color: #9ca3af; grid-column: 1 / -1; }
.pagination-wrapper { margin-top: 32px; text-align: center; }
</style>





