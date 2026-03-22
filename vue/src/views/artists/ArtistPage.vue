<template>
  <div>
    <el-space style="margin-bottom: 16px;">
      <el-input v-model="keyword" placeholder="搜索歌手名" clearable style="width: 200px;" />
      <el-button type="primary" @click="openCreate">新增歌手</el-button>
      <el-button @click="loadList">刷新</el-button>
    </el-space>

    <el-table :data="list" style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="bio" label="简介" show-overflow-tooltip />
      <el-table-column prop="avatarUrl" label="头像" width="100">
        <template #default="{ row }">
          <el-image 
            v-if="row.avatarUrl" 
            :src="getImageUrl(row.avatarUrl)" 
            style="width: 50px; height: 50px;" 
            fit="cover"
            :preview-src-list="[getImageUrl(row.avatarUrl)]"
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑歌手' : '新增歌手'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.bio" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="头像">
          <el-input v-model="form.avatarUrl" placeholder="可手动输入" style="margin-bottom: 8px;" />
          <FileUpload v-model="form.avatarUrl" accept="image/*" :max-size="5" />
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

interface Artist {
  id: number
  name: string
  bio?: string
  avatarUrl?: string
}

const list = ref<Artist[]>([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref<Partial<Artist>>({})
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 处理图片URL，确保完整路径
function getImageUrl(url: string | undefined): string {
  if (!url) return ''
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径，拼接baseURL
  return url.startsWith('/') ? url : `/${url}`
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/artists/page', {
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

function openCreate() {
  form.value = { name: '', bio: '', avatarUrl: '' }
  dialogVisible.value = true
}

function openEdit(row: Artist) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请填写歌手姓名')
    return
  }
  submitting.value = true
  try {
    const body = { name: form.value.name, bio: form.value.bio, avatarUrl: form.value.avatarUrl }
    if (form.value.id) {
      await http.put(`/artists/${form.value.id}`, body)
      ElMessage.success('更新成功')
    } else {
      await http.post('/artists', body)
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

async function remove(row: Artist) {
  try {
    await http.delete(`/artists/${row.id}`)
    ElMessage.success('删除成功')
    await loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
</style>
