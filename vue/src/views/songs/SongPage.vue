<template>
  <div>
    <el-space style="margin-bottom: 16px;">
      <el-input v-model="keyword" placeholder="搜索歌曲名" clearable style="width: 200px;" />
      <el-button type="primary" @click="openCreate">新增歌曲</el-button>
      <el-button @click="loadList">刷新</el-button>
    </el-space>

    <el-table :data="filteredList" style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="歌手" width="120">
        <template #default="{ row }">{{ row.artist?.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="专辑" width="150">
        <template #default="{ row }">{{ row.album?.title || '-' }}</template>
      </el-table-column>
      <el-table-column label="流派" width="100">
        <template #default="{ row }">{{ row.genre?.name || '-' }}</template>
      </el-table-column>
      <el-table-column prop="durationSec" label="时长(秒)" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑歌曲' : '新增歌曲'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="歌手" required>
          <el-select v-model="artistId" placeholder="请选择歌手" filterable style="width: 100%;">
            <el-option v-for="a in artists" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专辑">
          <el-select v-model="albumId" placeholder="可选" filterable clearable style="width: 100%;">
            <el-option v-for="a in albums" :key="a.id" :label="a.title" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="流派">
          <el-select v-model="genreId" placeholder="可选" filterable clearable style="width: 100%;">
            <el-option v-for="g in genres" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(秒)">
          <el-input-number v-model="form.durationSec" :min="0" style="width: 100%;" />
          <el-text v-if="form.durationSec" type="success" size="small" style="margin-top: 4px;">
            {{ formatDuration(form.durationSec) }}
          </el-text>
        </el-form-item>
        <el-form-item label="音频">
          <el-input v-model="form.audioUrl" placeholder="可手动输入URL" style="margin-bottom: 8px;" />
          <AudioUpload v-model="form.audioUrl" :max-size="80" @duration="onDuration" @filename="onFilename" />
          <el-text type="info" size="small" style="margin-top: 4px; display: block;">
            ✨ 上传音频文件后，系统会自动识别时长并填充标题
          </el-text>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import AudioUpload from '../../components/AudioUpload.vue'

interface Song {
  id: number
  title: string
  durationSec?: number
  audioUrl?: string
  artist: { id: number; name: string }
  album?: { id: number; title: string }
  genre?: { id: number; name: string }
}

const list = ref<Song[]>([])
const artists = ref<any[]>([])
const albums = ref<any[]>([])
const genres = ref<any[]>([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref<Partial<Song>>({})
const artistId = ref<number | null>(null)
const albumId = ref<number | null>(null)
const genreId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filteredList = computed(() => {
  const kw = (keyword.value || '').trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter(it => (it.title || '').toLowerCase().includes(kw))
})

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/songs/page', {
      params: { keyword: keyword.value || undefined, page: currentPage.value - 1, size: pageSize.value }
    })
    list.value = data.content
    total.value = data.totalElements
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadRelated() {
  try {
    const [a, al, g] = await Promise.all([
      http.get('/artists'),
      http.get('/albums'),
      http.get('/genres')
    ])
    artists.value = a.data
    albums.value = al.data
    genres.value = g.data
  } catch (err: any) {
    ElMessage.error('加载关联数据失败')
  }
}

function openCreate() {
  form.value = { title: '', durationSec: 0, audioUrl: '' }
  artistId.value = albumId.value = genreId.value = null
  dialogVisible.value = true
}

function openEdit(row: Song) {
  form.value = { ...row }
  artistId.value = row.artist?.id || null
  albumId.value = row.album?.id || null
  genreId.value = row.genre?.id || null
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请填写歌曲标题')
    return
  }
  if (!artistId.value) {
    ElMessage.warning('请选择歌手')
    return
  }
  submitting.value = true
  try {
    const body = {
      title: form.value.title,
      durationSec: form.value.durationSec,
      audioUrl: form.value.audioUrl,
      artistId: artistId.value,
      albumId: albumId.value,
      genreId: genreId.value
    }
    if (form.value.id) {
      await http.put(`/songs/${form.value.id}`, body)
      ElMessage.success('更新成功')
    } else {
      await http.post('/songs', body)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function onDuration(sec: number) {
  form.value.durationSec = sec
}

function onFilename(name: string) {
  if (!form.value.title) {
    const base = name.replace(/\.[^.]+$/, '')
    form.value.title = base
  }
}

function formatDuration(sec: number): string {
  if (!sec) return '0:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

async function remove(row: Song) {
  try {
    await http.delete(`/songs/${row.id}`)
    ElMessage.success('删除成功')
    await loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadList()
  loadRelated()
})
</script>

<style scoped>
</style>
