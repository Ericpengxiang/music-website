<template>
  <div>
    <el-space style="margin-bottom: 16px;">
      <el-input v-model="keyword" placeholder="搜索故事内容" clearable style="width: 250px;" />
      <el-select v-model="emotionFilter" placeholder="情感筛选" clearable style="width: 150px;">
        <el-option label="全部" value="" />
        <el-option label="励志" value="励志" />
        <el-option label="治愈" value="治愈" />
        <el-option label="怀旧" value="怀旧" />
        <el-option label="温暖" value="温暖" />
        <el-option label="伤感" value="伤感" />
      </el-select>
      <el-select v-model="featuredFilter" placeholder="精选筛选" clearable style="width: 150px;">
        <el-option label="全部" value="" />
        <el-option label="精选" value="true" />
        <el-option label="非精选" value="false" />
      </el-select>
      <el-button @click="loadList">刷新</el-button>
    </el-space>

    <el-table :data="filteredList" style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="用户" width="120">
        <template #default="{ row }">
          <div class="user-info">
            <img v-if="row.userAvatar" :src="getImageUrl(row.userAvatar)" class="user-avatar" />
            <span>{{ row.username || '未知用户' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="故事内容" min-width="300">
        <template #default="{ row }">
          <div class="story-content">{{ row.story }}</div>
        </template>
      </el-table-column>
      <el-table-column label="关联歌曲" width="180">
        <template #default="{ row }">
          <span v-if="row.song">{{ row.song.title }}</span>
          <span v-else class="text-muted">ID: {{ row.songId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="情感" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.emotion" :type="getEmotionType(row.emotion)" size="small">
            {{ row.emotion }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="点赞" width="80">
        <template #default="{ row }">
          <el-tag type="danger" size="small">
            ❤️ {{ row.likeCount }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="精选" width="80">
        <template #default="{ row }">
          <el-tag :type="row.featured ? 'success' : 'info'" size="small">
            {{ row.featured ? '✨ 精选' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button 
            size="small" 
            :type="row.featured ? 'warning' : 'success'"
            @click="toggleFeatured(row)"
          >
            {{ row.featured ? '取消精选' : '设为精选' }}
          </el-button>
          <el-popconfirm title="确定删除吗？" @confirm="remove(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 16px;"
      @size-change="loadList"
      @current-change="loadList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

interface Story {
  id: number
  userId: number
  username?: string
  userAvatar?: string
  songId: number
  song?: any
  story: string
  emotion?: string
  likeCount: number
  featured: boolean
  createdAt: string
}

const loading = ref(false)
const list = ref<Story[]>([])
const keyword = ref('')
const emotionFilter = ref('')
const featuredFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filteredList = computed(() => {
  let result = list.value

  // 关键词搜索
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(item => 
      item.story?.toLowerCase().includes(kw) ||
      item.username?.toLowerCase().includes(kw)
    )
  }

  // 情感筛选
  if (emotionFilter.value) {
    result = result.filter(item => item.emotion === emotionFilter.value)
  }

  // 精选筛选
  if (featuredFilter.value) {
    const isFeatured = featuredFilter.value === 'true'
    result = result.filter(item => item.featured === isFeatured)
  }

  return result
})

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/stories/admin/all', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    list.value = data.content
    total.value = data.totalElements
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function toggleFeatured(row: Story) {
  try {
    const { data } = await http.put(`/stories/admin/${row.id}/featured`)
    row.featured = data.featured
    ElMessage.success(data.featured ? '已设为精选' : '已取消精选')
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  }
}

async function remove(row: Story) {
  try {
    await http.delete(`/stories/admin/${row.id}`)
    ElMessage.success('删除成功')
    loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getEmotionType(emotion: string): string {
  const typeMap: Record<string, string> = {
    '励志': 'warning',
    '治愈': 'success',
    '怀旧': 'info',
    '温暖': 'danger',
    '伤感': ''
  }
  return typeMap[emotion] || ''
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.story-content {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  color: #333;
}

.text-muted {
  color: #999;
  font-size: 12px;
}
</style>


