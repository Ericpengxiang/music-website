<template>
  <div class="story-section">
    <div class="story-header">
      <h3>📖 听歌故事 ({{ total }})</h3>
      <div class="emotion-tabs">
        <button 
          v-for="emo in emotions" 
          :key="emo.value" 
          :class="['emotion-tab', { active: selectedEmotion === emo.value }]"
          @click="selectedEmotion = emo.value; loadStories()"
        >
          {{ emo.icon }} {{ emo.label }}
        </button>
      </div>
    </div>

    <!-- 发布故事 -->
    <div class="story-input" v-if="auth.token">
      <el-input
        v-model="newStory"
        type="textarea"
        :rows="4"
        placeholder="分享你与这首歌的故事..."
        maxlength="1000"
        show-word-limit
      />
      <div class="input-footer">
        <div class="emotion-selector">
          <span class="label">情感标签：</span>
          <el-radio-group v-model="storyEmotion" size="small">
            <el-radio-button v-for="emo in emotions" :key="emo.value" :value="emo.value">
              {{ emo.icon }} {{ emo.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" @click="submitStory" :disabled="!newStory.trim()">发布故事</el-button>
      </div>
    </div>
    <div class="need-login" v-else>
      <el-button text type="primary" @click="$router.push('/user/login')">登录后分享你的故事</el-button>
    </div>

    <!-- 故事列表 -->
    <div class="story-list" v-loading="loading">
      <div class="story-item" v-for="story in stories" :key="story.id">
        <div class="story-avatar">
          <img v-if="story.userAvatar" :src="getImageUrl(story.userAvatar)" alt="" />
          <div v-else class="avatar-placeholder">{{ story.username?.charAt(0) }}</div>
        </div>
        <div class="story-content">
          <div class="story-user">
            <span class="username">{{ story.username }}</span>
            <span v-if="story.emotion" class="emotion-tag" :class="`emotion-${story.emotion}`">
              {{ getEmotionIcon(story.emotion) }} {{ story.emotion }}
            </span>
            <span v-if="story.featured" class="featured-badge">精选</span>
          </div>
          <div class="story-text">{{ story.story }}</div>
          <div class="story-meta">
            <span class="story-time">{{ formatTime(story.createdAt) }}</span>
            <button class="meta-btn" :class="{liked: isLiked(story.id)}" @click="toggleLike(story)">
              <svg width="14" height="14" viewBox="0 0 24 24" :fill="isLiked(story.id) ? '#ef4444' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78l1.06 1.06L12 21l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              {{ story.likeCount || 0 }}
            </button>
            <button class="meta-btn danger" v-if="isMyStory(story)" @click="deleteStory(story.id)">删除</button>
          </div>
        </div>
      </div>

      <div v-if="stories.length === 0 && !loading" class="empty">
        暂无故事，快来分享你的音乐故事吧~
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadStories"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../store/auth'
import http from '../api/http'
import { ElMessage } from 'element-plus'

interface Props {
  songId: number
}

const props = defineProps<Props>()
const auth = useAuthStore()

const emotions = [
  { value: '', label: '全部', icon: '📖' },
  { value: '治愈', label: '治愈', icon: '💊' },
  { value: '励志', label: '励志', icon: '💪' },
  { value: '伤感', label: '伤感', icon: '😢' },
  { value: '怀旧', label: '怀旧', icon: '📷' },
  { value: '温暖', label: '温暖', icon: '☀️' }
]

const loading = ref(false)
const stories = ref<any[]>([])
const newStory = ref('')
const storyEmotion = ref('治愈')
const selectedEmotion = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const likedStoryIds = ref<Set<number>>(new Set())

async function loadStories() {
  loading.value = true
  try {
    const { data } = await http.get(`/stories/song/${props.songId}`, {
      params: { page: currentPage.value - 1, size: pageSize.value }
    })
    stories.value = data.content || []
    total.value = data.totalElements || 0
  } catch (e) {
    ElMessage.error('加载故事失败')
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

async function submitStory() {
  if (!newStory.value.trim()) return
  try {
    await http.post('/stories', {
      songId: props.songId,
      story: newStory.value,
      emotion: storyEmotion.value
    })
    newStory.value = ''
    storyEmotion.value = '治愈'
    ElMessage.success('故事发布成功')
    currentPage.value = 1
    await loadStories()
  } catch (e) {
    ElMessage.error('发布失败')
  }
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
    '治愈': '💊',
    '励志': '💪',
    '伤感': '😢',
    '怀旧': '📷',
    '温暖': '☀️'
  }
  return icons[emotion] || '📖'
}

function formatTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

onMounted(() => {
  loadStories()
  loadLikedIds()
})
</script>

<style scoped>
.story-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.story-header {
  margin-bottom: 20px;
}

.story-header h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.emotion-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.emotion-tab {
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #6b7280;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.emotion-tab:hover {
  border-color: #8b5cf6;
  color: #8b5cf6;
}

.emotion-tab.active {
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  border-color: transparent;
}

.story-input {
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.03) 0%, rgba(118, 75, 162, 0.03) 100%);
  border-radius: 12px;
}

.input-footer {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.emotion-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.emotion-selector .label {
  color: #6b7280;
  font-weight: 500;
}

.need-login {
  padding: 20px;
  text-align: center;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 24px;
}

.story-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.story-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.02) 0%, rgba(118, 75, 162, 0.02) 100%);
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.story-item:hover {
  border-color: #8b5cf6;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.1);
}

.story-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.story-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.story-content {
  flex: 1;
}

.story-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.username {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.emotion-tag {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.emotion-治愈 { background: #dbeafe; color: #1e40af; }
.emotion-励志 { background: #fef3c7; color: #92400e; }
.emotion-伤感 { background: #fce7f3; color: #9f1239; }
.emotion-怀旧 { background: #e0e7ff; color: #3730a3; }
.emotion-温暖 { background: #fed7aa; color: #9a3412; }

.featured-badge {
  padding: 2px 10px;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  color: white;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.story-text {
  font-size: 15px;
  color: #374151;
  line-height: 1.8;
  margin-bottom: 12px;
  word-break: break-word;
  white-space: pre-wrap;
}

.story-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #9ca3af;
}

.story-time {
  color: #9ca3af;
}

.meta-btn {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.meta-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.meta-btn.liked {
  color: #ef4444;
}

.meta-btn.danger {
  color: #ef4444;
}

.empty {
  padding: 60px 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

.pagination {
  margin-top: 24px;
  text-align: center;
}
</style>





