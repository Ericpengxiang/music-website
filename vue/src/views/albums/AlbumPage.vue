<template>
  <div>
    <el-space style="margin-bottom: 16px;">
      <el-input v-model="keyword" placeholder="搜索专辑名" clearable style="width: 200px;" />
      <el-button type="primary" @click="openCreate">新增专辑</el-button>
      <el-button @click="loadList">刷新</el-button>
    </el-space>

    <el-table :data="filteredList" style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="歌手" width="150">
        <template #default="{ row }">{{ row.artist?.name || '-' }}</template>
      </el-table-column>
      <el-table-column prop="releaseDate" label="发行日期" width="120" />
      <el-table-column prop="coverUrl" label="封面" width="100">
        <template #default="{ row }">
          <el-image 
            v-if="row.coverUrl" 
            :src="getImageUrl(row.coverUrl)" 
            style="width: 50px; height: 50px;" 
            fit="cover"
            :preview-src-list="[getImageUrl(row.coverUrl)]"
          >
            <template #error>
              <div style="display: flex; align-items: center; justify-content: center; width: 50px; height: 50px; background: #f5f5f5; font-size: 12px; color: #999;">无图</div>
            </template>
          </el-image>
        </template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑专辑' : '新增专辑'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="歌手" required>
          <el-select v-model="artistId" placeholder="请选择歌手" filterable style="width: 100%;">
            <el-option v-for="a in artists" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发行日期">
          <el-date-picker v-model="form.releaseDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="封面">
          <el-input v-model="form.coverUrl" placeholder="可手动输入" style="margin-bottom: 8px;" />
          <FileUpload v-model="form.coverUrl" accept="image/*" :max-size="5" />
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
import FileUpload from '../../components/FileUpload.vue'

interface Album {
  id: number
  title: string
  coverUrl?: string
  releaseDate?: string
  artist: { id: number; name: string }
}

interface Artist {
  id: number
  name: string
}

const list = ref<Album[]>([])
const artists = ref<Artist[]>([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref<Partial<Album>>({})
const artistId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filteredList = computed(() => {
  const kw = (keyword.value || '').trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter(it => (it.title || '').toLowerCase().includes(kw))
})

// 处理图片URL，确保完整路径
function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  return url.startsWith('/') ? url : `/${url}`
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/albums/page', {
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

async function loadArtists() {
  try {
    const { data } = await http.get<Artist[]>('/artists')
    artists.value = data
  } catch (err: any) {
    ElMessage.error('加载歌手列表失败')
  }
}

function openCreate() {
  form.value = { title: '', coverUrl: '', releaseDate: '' }
  artistId.value = null
  dialogVisible.value = true
}

function openEdit(row: Album) {
  form.value = { ...row }
  artistId.value = row.artist?.id || null
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请填写专辑标题')
    return
  }
  if (!artistId.value) {
    ElMessage.warning('请选择歌手')
    return
  }
  submitting.value = true
  try {
    const body = { title: form.value.title, coverUrl: form.value.coverUrl, releaseDate: form.value.releaseDate, artistId: artistId.value }
    if (form.value.id) {
      await http.put(`/albums/${form.value.id}`, body)
      ElMessage.success('更新成功')
    } else {
      await http.post('/albums', body)
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

async function remove(row: Album) {
  try {
    await http.delete(`/albums/${row.id}`)
    ElMessage.success('删除成功')
    await loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadList()
  loadArtists()
})
</script>

<style scoped>
</style>
