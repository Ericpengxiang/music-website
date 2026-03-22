<template>
  <div class="audit-log-page">
    <div class="page-header">
      <h2>📋 操作日志</h2>
      <el-space>
        <el-input 
          v-model="keyword" 
          placeholder="搜索用户名" 
          clearable 
          style="width: 250px;"
          @input="searchLogs"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="loadList" :icon="Refresh">刷新</el-button>
        <el-popconfirm title="确定删除选中记录吗？" @confirm="deleteSelected">
          <template #reference>
            <el-button type="danger" :disabled="selectedIds.length === 0" :icon="Delete">
              删除选中 ({{ selectedIds.length }})
            </el-button>
          </template>
        </el-popconfirm>
      </el-space>
    </div>

    <el-table 
      :data="list" 
      style="width: 100%;" 
      v-loading="loading" 
      @selection-change="onSelectionChange"
      stripe
    >
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="操作用户" width="120">
        <template #default="{ row }">
          <el-tag type="primary" size="small">{{ row.username }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getOperationType(row.operation)" size="small">
            {{ row.operation }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="resourceType" label="资源类型" width="120">
        <template #default="{ row }">
          <el-tag type="info" size="small">{{ row.resourceType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="resourceId" label="资源ID" width="100" />
      <el-table-column prop="details" label="操作详情" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="150" />
      <el-table-column label="操作时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确定删除该记录吗？" @confirm="() => deleteRow(row.id)">
            <template #reference>
              <el-button size="small" type="danger" :icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 16px;"
      background
      @size-change="loadList"
      @current-change="loadList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import http from '../../api/http'
import { ElMessage } from 'element-plus'

interface AuditLog {
  id: number
  username: string
  operation: string
  resourceType: string
  resourceId?: number
  details?: string
  ipAddress?: string
  createdAt: string
}

const list = ref<AuditLog[]>([])
const keyword = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedIds = ref<number[]>([])

async function loadList() {
  loading.value = true
  try {
    const { data } = await http.get('/audit-logs/page', {
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

function searchLogs() {
  currentPage.value = 1
  loadList()
}

function onSelectionChange(rows: AuditLog[]) {
  selectedIds.value = rows.map(r => r.id)
}

async function deleteRow(id: number) {
  try {
    await http.delete(`/audit-logs/${id}`)
    ElMessage.success('删除成功')
    await loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

async function deleteSelected() {
  if (selectedIds.value.length === 0) return
  try {
    await http.delete('/audit-logs/batch', { data: selectedIds.value })
    ElMessage.success(`已删除 ${selectedIds.value.length} 条记录`)
    selectedIds.value = []
    await loadList()
  } catch (err: any) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

function getOperationType(operation: string): string {
  const typeMap: Record<string, string> = {
    '创建': 'success',
    '更新': 'warning',
    '删除': 'danger',
    '查看': 'info'
  }
  return typeMap[operation] || ''
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(loadList)
</script>

<style scoped>
.audit-log-page {
  padding: 0;
}

.page-header {
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
}
</style>





