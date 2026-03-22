<template>
  <div>
    <el-space style="margin-bottom: 16px;">
      <el-button type="primary" @click="openCreate">新增轮播图</el-button>
      <el-button @click="loadList">刷新</el-button>
    </el-space>

    <el-table :data="list" style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="预览" width="200">
        <template #default="{ row }">
          <el-image 
            v-if="row.imageUrl" 
            :src="getImageUrl(row.imageUrl)" 
            style="width: 180px; height: 80px; border-radius: 8px;"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="linkUrl" label="跳转链接" min-width="150" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.active ? 'success' : 'danger'">
            {{ row.active ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button 
            size="small" 
            :type="row.active ? 'warning' : 'success'"
            @click="toggleActive(row)"
          >
            {{ row.active ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm title="确定删除吗？" @confirm="remove(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog 
      v-model="dialogVisible" 
      :title="form.id ? '编辑轮播图' : '新增轮播图'" 
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="轮播图标题（可选）" />
        </el-form-item>
        <el-form-item label="图片" required>
          <FileUpload v-model="form.imageUrl" accept="image/*" />
          <el-text type="info" size="small" style="margin-top: 8px;">
            建议尺寸：1920x480px，支持 JPG、PNG 格式
          </el-text>
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="例如：/songs 或 /albums" />
          <el-text type="info" size="small" style="margin-top: 8px;">
            点击轮播图跳转的页面，留空则不跳转
          </el-text>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number 
            v-model="form.sortOrder" 
            :min="0" 
            :max="999" 
            controls-position="right"
          />
          <el-text type="info" size="small" style="margin-left: 12px;">
            数字越小越靠前
          </el-text>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.active" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'
import FileUpload from '../../components/FileUpload.vue'

interface Banner {
  id?: number
  title?: string
  imageUrl: string
  linkUrl?: string
  sortOrder: number
  active: boolean
}

const loading = ref(false)
const list = ref<Banner[]>([])
const dialogVisible = ref(false)
const form = reactive<Banner>({
  imageUrl: '',
  sortOrder: 0,
  active: true
})

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/banners')
    list.value = data
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    title: '',
    imageUrl: '',
    linkUrl: '',
    sortOrder: 0,
    active: true
  })
  dialogVisible.value = true
}

function openEdit(row: Banner) {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submit() {
  if (!form.imageUrl) {
    ElMessage.warning('请上传轮播图图片')
    return
  }

  try {
    if (form.id) {
      await http.put(`/banners/${form.id}`, form)
      ElMessage.success('更新成功')
    } else {
      await http.post('/banners', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  }
}

async function toggleActive(row: Banner) {
  try {
    await http.put(`/banners/${row.id}/toggle`)
    ElMessage.success(row.active ? '已禁用' : '已启用')
    loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  }
}

async function remove(row: Banner) {
  try {
    await http.delete(`/banners/${row.id}`)
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

onMounted(() => {
  loadList()
})
</script>

<style scoped>
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
</style>


