<template>
  <div>
    <div style="margin-bottom:12px;">
      <el-button type="primary" @click="openCreate">新增流派</el-button>
    </div>
    <el-table :data="items" style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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
      @size-change="fetchList"
      @current-change="fetchList"
    />

    <el-dialog v-model="visible" :title="editing ? '编辑流派' : '新增流派'">
      <el-form label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
  
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import http from '../../api/http';

interface Genre { id: number; name: string }

const items = ref<Genre[]>([]);
const visible = ref(false);
const editing = ref<Genre | null>(null);
const form = reactive({ name: '' });
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

async function fetchList() {
  const { data } = await http.get('/genres/page', {
    params: { page: currentPage.value - 1, size: pageSize.value }
  });
  items.value = data.content;
  total.value = data.totalElements;
}

function openCreate() {
  editing.value = null;
  form.name = '';
  visible.value = true;
}

function openEdit(row: Genre) {
  editing.value = row;
  form.name = row.name;
  visible.value = true;
}

async function submit() {
  if (!form.name.trim()) return;
  if (editing.value) {
    await http.put(`/genres/${editing.value.id}`, { name: form.name });
  } else {
    await http.post('/genres', { name: form.name });
  }
  visible.value = false;
  await fetchList();
}

async function remove(row: Genre) {
  await http.delete(`/genres/${row.id}`);
  await fetchList();
}

onMounted(fetchList);
</script>

<style scoped>
</style>



