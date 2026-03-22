<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3>评论 ({{ total }})</h3>
    </div>

    <!-- 发表评论 -->
    <div class="comment-input" v-if="auth.token">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="说点什么..."
        maxlength="500"
        show-word-limit
      />
      <div class="input-actions">
        <el-button type="primary" @click="submit" :disabled="!newComment.trim()">发表评论</el-button>
      </div>
    </div>
    <div class="need-login" v-else>
      <el-button text type="primary" @click="$router.push('/user/login')">登录后发表评论</el-button>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list" v-loading="loading">
      <div class="comment-item" v-for="comment in comments" :key="comment.id">
        <div class="comment-avatar">
          <img v-if="comment.userAvatar" :src="getImageUrl(comment.userAvatar)" alt="" />
          <div v-else class="avatar-placeholder">{{ comment.username?.charAt(0) }}</div>
        </div>
        <div class="comment-content">
          <div class="comment-user">{{ comment.username }}</div>
          <div class="comment-text">{{ comment.content }}</div>
          <div class="comment-meta">
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            <button class="meta-btn" :class="{liked: isLiked(comment.id)}" @click="toggleLike(comment)">
              <svg width="14" height="14" viewBox="0 0 24 24" :fill="isLiked(comment.id) ? '#ef4444' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78l1.06 1.06L12 21l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              {{ comment.likeCount || 0 }}
            </button>
            <button class="meta-btn" @click="openReply(comment)">回复</button>
            <button class="meta-btn danger" v-if="isMyComment(comment)" @click="deleteComment(comment.id)">删除</button>
          </div>

          <!-- 回复列表 -->
          <div class="replies" v-if="comment.replies && comment.replies.length > 0">
            <div class="reply-item" v-for="reply in comment.replies" :key="reply.id">
              <span class="reply-user">{{ reply.username }}:</span>
              <span class="reply-text">{{ reply.content }}</span>
              <button class="reply-delete" v-if="isMyComment(reply)" @click="deleteComment(reply.id)">删除</button>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div class="reply-input" v-if="replyTo === comment.id && auth.token">
            <el-input
              v-model="replyContent"
              placeholder="回复评论..."
              size="small"
              @keyup.enter="submitReply(comment.id)"
            >
              <template #append>
                <el-button @click="submitReply(comment.id)">回复</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>

      <div v-if="comments.length === 0 && !loading" class="empty">暂无评论，快来抢沙发~</div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadComments"
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
  resourceType: 'song' | 'album' | 'playlist'
  resourceId: number
}

const props = defineProps<Props>()
const auth = useAuthStore()

const loading = ref(false)
const comments = ref<any[]>([])
const newComment = ref('')
const replyTo = ref<number | null>(null)
const replyContent = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const likedCommentIds = ref<Set<number>>(new Set())

async function loadComments() {
  loading.value = true
  try {
    const { data } = await http.get('/comments', {
      params: {
        resourceType: props.resourceType,
        resourceId: props.resourceId,
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    comments.value = data.content || []
    total.value = data.totalElements || 0

    // 加载回复
    for (const comment of comments.value) {
      const { data: replies } = await http.get(`/comments/${comment.id}/replies`)
      comment.replies = replies || []
    }
  } catch (e) {
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

async function loadLikedIds() {
  if (!auth.token) return
  try {
    const { data } = await http.get('/comments/likes/check')
    const set = new Set<number>()
    for (const id of data?.likedCommentIds || []) set.add(Number(id))
    likedCommentIds.value = set
  } catch {}
}

async function submit() {
  if (!newComment.value.trim()) return
  try {
    await http.post('/comments', {
      resourceType: props.resourceType,
      resourceId: props.resourceId,
      content: newComment.value
    })
    newComment.value = ''
    ElMessage.success('发表成功')
    currentPage.value = 1
    await loadComments()
  } catch (e) {
    ElMessage.error('发表失败')
  }
}

function openReply(comment: any) {
  replyTo.value = comment.id
  replyContent.value = ''
}

async function submitReply(parentId: number) {
  if (!replyContent.value.trim()) return
  try {
    await http.post('/comments', {
      resourceType: props.resourceType,
      resourceId: props.resourceId,
      content: replyContent.value,
      parentId
    })
    replyTo.value = null
    replyContent.value = ''
    ElMessage.success('回复成功')
    await loadComments()
  } catch (e) {
    ElMessage.error('回复失败')
  }
}

async function toggleLike(comment: any) {
  if (!auth.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isLiked(comment.id)) {
      await http.delete(`/comments/${comment.id}/like`)
      likedCommentIds.value.delete(comment.id)
      comment.likeCount--
    } else {
      await http.post(`/comments/${comment.id}/like`)
      likedCommentIds.value.add(comment.id)
      comment.likeCount++
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function deleteComment(commentId: number) {
  try {
    await http.delete(`/comments/${commentId}`)
    ElMessage.success('已删除')
    await loadComments()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function isLiked(commentId: number): boolean {
  return likedCommentIds.value.has(commentId)
}

function isMyComment(comment: any): boolean {
  return auth.username === comment.username
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
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
  loadComments()
  loadLikedIds()
})
</script>

<style scoped>
.comment-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.comment-header h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.comment-input {
  margin-bottom: 24px;
}

.input-actions {
  margin-top: 12px;
  text-align: right;
}

.need-login {
  padding: 24px;
  text-align: center;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 24px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.comment-avatar img {
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
  font-size: 16px;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.comment-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #9ca3af;
}

.comment-time {
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

.replies {
  margin-top: 12px;
  padding-left: 16px;
  border-left: 2px solid #e5e7eb;
}

.reply-item {
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  position: relative;
}

.reply-user {
  font-weight: 600;
  color: #8b5cf6;
  margin-right: 6px;
}

.reply-text {
  color: #374151;
}

.reply-delete {
  position: absolute;
  right: 8px;
  top: 8px;
  background: none;
  border: none;
  color: #ef4444;
  font-size: 12px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.reply-item:hover .reply-delete {
  opacity: 1;
}

.reply-input {
  margin-top: 12px;
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





