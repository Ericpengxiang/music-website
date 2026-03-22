<template>
  <div>
    <el-space style="margin-bottom: 16px;">
      <el-input v-model="keyword" placeholder="搜索用户名" clearable style="width: 200px;" />
      <el-button type="primary" @click="openCreate">新增用户</el-button>
      <el-button @click="loadList">刷新</el-button>
    </el-space>

    <el-table :data="list" style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="displayName" label="昵称" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.active ? 'success' : 'danger'">{{ row.active ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="账号" required v-if="!form.id">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item label="密码" :required="!form.id">
          <el-input v-model="password" type="password" placeholder="留空表示不修改" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="active" active-text="启用" inactive-text="禁用" />
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

interface User {
  id: number
  username: string
  displayName?: string
  active: boolean
}

const list = ref<User[]>([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref<Partial<User>>({})
const password = ref('')
const active = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/users/page', {
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
  form.value = { username: '', displayName: '' }
  password.value = ''
  active.value = true
  dialogVisible.value = true
}

function openEdit(row: User) {
  form.value = { ...row }
  password.value = ''
  active.value = row.active
  dialogVisible.value = true
}

async function submit() {
  if (!form.value.id && !form.value.username?.trim()) {
    ElMessage.warning('请填写账号')
    return
  }
  if (!form.value.id && !password.value?.trim()) {
    ElMessage.warning('请填写密码')
    return
  }
  submitting.value = true
  try {
    const body: any = { displayName: form.value.displayName, active: active.value }
    if (password.value?.trim()) {
      body.password = password.value
    }
    if (form.value.id) {
      await http.put(`/users/${form.value.id}`, body)
      ElMessage.success('更新成功')
    } else {
      body.username = form.value.username
      await http.post('/users', body)
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

async function remove(row: User) {
  try {
    await http.delete(`/users/${row.id}`)
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
