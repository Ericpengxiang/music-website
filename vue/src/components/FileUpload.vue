<template>
  <div>
    <el-upload
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :accept="accept"
    >
      <el-button size="small" type="primary" :loading="uploading">
        {{ uploading ? '上传中...' : '上传文件' }}
      </el-button>
    </el-upload>
    <div v-if="modelValue" style="margin-top: 8px; font-size: 12px; color: #666;">
      当前文件：{{ modelValue }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

interface Props {
  modelValue?: string
  accept?: string
  maxSize?: number // MB
}

const props = withDefaults(defineProps<Props>(), {
  accept: '*',
  maxSize: 50
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const auth = useAuthStore()
const uploading = ref(false)

const uploadUrl = computed(() => '/api/upload')

const headers = computed(() => ({
  Authorization: `Bearer ${auth.token}`
}))

function beforeUpload(file: File) {
  const maxSizeMB = props.maxSize
  if (file.size / 1024 / 1024 > maxSizeMB) {
    ElMessage.error(`文件大小不能超过 ${maxSizeMB}MB`)
    return false
  }
  uploading.value = true
  return true
}

function handleSuccess(response: any) {
  uploading.value = false
  if (response.url) {
    emit('update:modelValue', response.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

function handleError() {
  uploading.value = false
  ElMessage.error('上传失败')
}
</script>

<style scoped>
</style>


