<template>
  <div class="audio-upload">
    <el-upload
      drag
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      :on-change="onFileChange"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="onProgress"
      accept="audio/*"
    >
      <div class="upload-area">
        <div v-if="!uploading && !modelValue" class="placeholder">
          <div class="icon">🎵</div>
          <div class="tip">拖拽音频到此处，或点击选择文件（支持 mp3/flac/wav 等）</div>
          <div class="sub">最大 {{ maxSize }}MB</div>
        </div>
        <div v-else class="status">
          <el-progress v-if="uploading" :percentage="progress" :stroke-width="10" />
          <div v-else-if="extracting" class="extracting">
            <span>正在识别音频时长...</span>
          </div>
          <div v-else class="done">
            <span class="file">{{ fileName }}</span>
            <span v-if="durationSec" class="duration">· {{ formatDuration(durationSec) }}</span>
          </div>
        </div>
      </div>
    </el-upload>
    <div v-if="modelValue" class="url">已上传：{{ modelValue }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'

interface Props {
  modelValue?: string
  maxSize?: number // MB
}

const props = withDefaults(defineProps<Props>(), { maxSize: 80 })
const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
  (e: 'duration', s: number): void
  (e: 'filename', name: string): void
}>()

const auth = useAuthStore()
const apiBase = import.meta.env.VITE_API_BASE_URL ? `${import.meta.env.VITE_API_BASE_URL}/api` : '/api'
const uploadUrl = computed(() => `${apiBase}/upload`)
const headers = computed(() => ({ Authorization: `Bearer ${auth.token}` }))

const modelValue = defineModel<string>()
const uploading = ref(false)
const progress = ref(0)
const fileName = ref('')
const durationSec = ref<number | null>(null)
const extracting = ref(false)

function onFileChange(file: any) {
  fileName.value = file?.name || ''
}

function beforeUpload(file: File) {
  if (file.size / 1024 / 1024 > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  uploading.value = true
  progress.value = 0
  // 预读时长
  tryExtractDuration(file)
  return true
}

function onProgress(evt: any) {
  progress.value = Math.round(evt.percent)
}

function handleSuccess(res: any) {
  uploading.value = false
  if (res?.url) {
    modelValue.value = res.url
    ElMessage.success('音频上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

function handleError() {
  uploading.value = false
  ElMessage.error('上传失败')
}

function tryExtractDuration(file: File) {
  extracting.value = true
  const url = URL.createObjectURL(file)
  const audio = new Audio()
  audio.preload = 'metadata'
  audio.src = url
  audio.onloadedmetadata = () => {
    durationSec.value = Math.round(audio.duration || 0)
    if (durationSec.value > 0) {
      emit('duration', durationSec.value)
      ElMessage.success(`已自动识别时长：${formatDuration(durationSec.value)}`)
    }
    emit('filename', file.name)
    extracting.value = false
    URL.revokeObjectURL(url)
  }
  audio.onerror = () => {
    extracting.value = false
    URL.revokeObjectURL(url)
  }
}

function formatDuration(total: number) {
  const m = Math.floor(total / 60)
  const s = Math.floor(total % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.audio-upload { width: 100%; }
.upload-area { padding: 18px; border: 1px dashed var(--border-color, #e5e7eb); border-radius: 10px; background: rgba(0,0,0,0.02); }
.placeholder { text-align: center; color: #6b7280; }
.icon { font-size: 28px; }
.tip { margin-top: 6px; }
.sub { font-size: 12px; opacity: .8; margin-top: 2px; }
.status { padding: 6px 8px; }
.extracting { font-size: 13px; color: #8b5cf6; text-align: center; }
.done { font-size: 13px; color: #374151; }
.file { font-weight: 600; margin-right: 6px; }
.duration { color: #10b981; font-weight: 500; }
.url { margin-top: 6px; font-size: 12px; color: #6b7280; word-break: break-all; }
</style>


