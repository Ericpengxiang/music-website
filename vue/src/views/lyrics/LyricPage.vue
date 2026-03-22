<template>
  <div>
    <el-space style="margin-bottom: 16px;">
      <el-input v-model="keyword" placeholder="搜索歌曲名称" clearable style="width: 250px;" @input="filterList" />
      <el-button type="primary" @click="openCreate">新增歌词</el-button>
      <el-button @click="loadList">刷新</el-button>
    </el-space>

    <el-table :data="filteredList" style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="歌曲信息" min-width="250">
        <template #default="{ row }">
          <div v-if="row.song">
            <div style="font-weight: 600; margin-bottom: 4px;">{{ row.song.title }}</div>
            <div style="font-size: 13px; color: #6b7280;">
              {{ row.song.artist?.name || '未知歌手' }}
              <span v-if="row.song.album"> · {{ row.song.album.title }}</span>
            </div>
          </div>
          <div v-else style="color: #9ca3af;">歌曲ID: {{ row.songId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="歌词预览" min-width="300">
        <template #default="{ row }">
          <div class="lyric-preview">
            {{ getLyricPreview(row.content) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="翻译" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.translated" type="success" size="small">有翻译</el-tag>
          <el-tag v-else type="info" size="small">无翻译</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="openPreview(row)">预览</el-button>
          <el-popconfirm title="确定删除吗？" @confirm="remove(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑歌词' : '新增歌词'" 
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择歌曲" required>
          <el-select 
            v-model="form.songId" 
            filterable 
            remote 
            :remote-method="searchSongs"
            placeholder="输入歌曲名搜索"
            style="width: 100%;"
            :disabled="isEdit"
          >
            <el-option 
              v-for="song in songOptions" 
              :key="song.id" 
              :label="`${song.title} - ${song.artist?.name || '未知歌手'}`"
              :value="song.id"
            />
          </el-select>
          <el-text type="info" size="small" style="margin-top: 8px;">
            {{ isEdit ? '编辑模式下不能更换歌曲' : '搜索并选择要添加歌词的歌曲' }}
          </el-text>
        </el-form-item>

        <el-form-item label="歌词内容" required>
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="12"
            placeholder="请输入或导入LRC格式歌词：可点击右侧“导入LRC”选择文件"
          />
          <div style="display:flex; align-items:center; gap:8px; margin-top:8px;">
            <el-text type="info" size="small">LRC示例：[00:12.34]歌词文本</el-text>
            <el-button size="small" @click="triggerImport('content')">导入LRC</el-button>
            <input ref="contentFileInput" type="file" accept=".lrc,.txt" style="display:none" @change="handleImport($event, 'content')" />
          </div>
        </el-form-item>

        <el-form-item label="翻译歌词">
          <el-input 
            v-model="form.translated" 
            type="textarea" 
            :rows="8"
            placeholder="翻译歌词（可选，同样使用LRC格式）。可点击右侧“导入翻译LRC”"
          />
          <div style="display:flex; align-items:center; gap:8px; margin-top:8px;">
            <el-text type="info" size="small">翻译也建议使用 LRC 时间标记</el-text>
            <el-button size="small" @click="triggerImport('translated')">导入翻译LRC</el-button>
            <input ref="translatedFileInput" type="file" accept=".lrc,.txt" style="display:none" @change="handleImport($event, 'translated')" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog 
      v-model="previewVisible" 
      title="歌词预览" 
      width="600px"
    >
      <div class="preview-container">
        <h3 v-if="previewData?.song">{{ previewData.song.title }} - {{ previewData.song.artist?.name }}</h3>
        <el-divider />
        <div class="lyric-content">
          <pre>{{ previewData?.content }}</pre>
        </div>
        <el-divider v-if="previewData?.translated" />
        <div v-if="previewData?.translated" class="lyric-content">
          <h4>翻译:</h4>
          <pre>{{ previewData.translated }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

interface Lyric {
  id: number
  songId: number
  song?: any
  content: string
  translated?: string
  createdAt: string
}

const loading = ref(false)
const list = ref<Lyric[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const previewVisible = ref(false)
const isEdit = ref(false)
const songOptions = ref<any[]>([])
const previewData = ref<Lyric | null>(null)
const contentFileInput = ref<HTMLInputElement>()
const translatedFileInput = ref<HTMLInputElement>()

const form = ref({
  songId: null as number | null,
  content: '',
  translated: ''
})

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  const kw = keyword.value.toLowerCase()
  return list.value.filter(item => 
    item.song?.title?.toLowerCase().includes(kw) ||
    item.song?.artist?.name?.toLowerCase().includes(kw)
  )
})

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/lyrics')
    list.value = data
    
    // 加载歌曲信息
    const songIds = [...new Set(data.map((l: Lyric) => l.songId))]
    if (songIds.length > 0) {
      const songPromises = songIds.map(id => 
        http.get(`/songs/${id}`).catch(() => ({ data: null }))
      )
      const songResults = await Promise.all(songPromises)
      const songMap = new Map()
      songResults.forEach((res, idx) => {
        if (res.data) {
          songMap.set(songIds[idx], res.data)
        }
      })
      
      list.value.forEach(lyric => {
        lyric.song = songMap.get(lyric.songId)
      })
    }
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function filterList() {
  // 触发computed重新计算
}

async function searchSongs(query: string) {
  if (!query) {
    songOptions.value = []
    return
  }
  try {
    const { data } = await http.get('/songs/page', {
      params: { keyword: query, page: 0, size: 20 }
    })
    songOptions.value = data.content || []
  } catch (err) {
    console.error('搜索歌曲失败', err)
  }
}

function openCreate() {
  isEdit.value = false
  form.value = {
    songId: null,
    content: '',
    translated: ''
  }
  songOptions.value = []
  dialogVisible.value = true
}

async function openEdit(row: Lyric) {
  isEdit.value = true
  form.value = {
    songId: row.songId,
    content: row.content,
    translated: row.translated || ''
  }
  if (row.song) {
    songOptions.value = [row.song]
  }
  dialogVisible.value = true
}

function openPreview(row: Lyric) {
  previewData.value = row
  previewVisible.value = true
}

async function submit() {
  if (!form.value.songId) {
    ElMessage.warning('请选择歌曲')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('请输入歌词内容')
    return
  }

  try {
    await http.post(`/lyrics/song/${form.value.songId}`, {
      content: form.value.content,
      translated: form.value.translated || null
    })
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  }
}

async function remove(row: Lyric) {
  try {
    await http.delete(`/lyrics/song/${row.songId}`)
    ElMessage.success('删除成功')
    loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

function getLyricPreview(content: string): string {
  if (!content) return '暂无歌词'
  const lines = content.split('\n').filter(line => {
    // 过滤掉空行和纯时间标签
    return line.trim() && line.includes(']')
  })
  
  // 取前3行，去掉时间标签
  return lines.slice(0, 3)
    .map(line => line.replace(/\[\d{2}:\d{2}\.\d{2,3}\]/g, '').trim())
    .filter(line => line)
    .join(' / ') || '暂无歌词'
}

// LRC 文件导入
function triggerImport(target: 'content' | 'translated') {
  if (target === 'content') contentFileInput.value?.click()
  else translatedFileInput.value?.click()
}

function handleImport(e: Event, target: 'content' | 'translated') {
  const input = e.target as HTMLInputElement
  const file = input.files && input.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    const text = (reader.result as string || '').replace(/^\uFEFF/, '')
    if (target === 'content') form.value.content = text
    else form.value.translated = text
    input.value = ''
  }
  reader.readAsText(file, 'utf-8')
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

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.lyric-preview {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.preview-container {
  padding: 16px;
}

.preview-container h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.preview-container h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
}

.lyric-content {
  max-height: 400px;
  overflow-y: auto;
  background: #f9fafb;
  padding: 16px;
  border-radius: 8px;
}

.lyric-content pre {
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>


