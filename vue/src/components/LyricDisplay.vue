<template>
  <div class="lyric-display">
    <div v-if="!lyrics || lyrics.length === 0" class="no-lyric">
      <p>暂无歌词</p>
    </div>
    <div v-else class="lyric-container" ref="containerRef">
      <div
        v-for="(line, index) in lyrics"
        :key="index"
        class="lyric-line"
        :class="{ active: index === currentLineIndex }"
        @click="seekTo(line.time)"
      >
        {{ line.text }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick } from 'vue'

interface LyricLine {
  time: number
  text: string
}

interface Props {
  lrc: string
  currentTime: number
}

const props = defineProps<Props>()
const emit = defineEmits(['seek'])

const lyrics = ref<LyricLine[]>([])
const currentLineIndex = ref(0)
const containerRef = ref<HTMLElement>()

// 解析LRC格式歌词
function parseLRC(lrc: string): LyricLine[] {
  if (!lrc) return []
  
  const lines: LyricLine[] = []
  const lrcLines = lrc.split('\n')
  
  for (const line of lrcLines) {
    // 匹配 [00:12.34]歌词内容
    const match = line.match(/\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/)
    if (match) {
      const minutes = parseInt(match[1])
      const seconds = parseInt(match[2])
      const milliseconds = parseInt(match[3].padEnd(3, '0'))
      const time = minutes * 60 + seconds + milliseconds / 1000
      const text = match[4].trim()
      
      if (text) {
        lines.push({ time, text })
      }
    }
  }
  
  return lines.sort((a, b) => a.time - b.time)
}

// 监听歌词内容变化
watch(() => props.lrc, (newLrc) => {
  lyrics.value = parseLRC(newLrc)
  currentLineIndex.value = 0
}, { immediate: true })

// 监听播放时间变化
watch(() => props.currentTime, (time) => {
  if (lyrics.value.length === 0) return
  
  // 找到当前时间对应的歌词行
  let index = 0
  for (let i = 0; i < lyrics.value.length; i++) {
    if (time >= lyrics.value[i].time) {
      index = i
    } else {
      break
    }
  }
  
  if (index !== currentLineIndex.value) {
    currentLineIndex.value = index
    scrollToActiveLine()
  }
})

// 滚动到当前歌词
function scrollToActiveLine() {
  nextTick(() => {
    const container = containerRef.value
    if (!container) return
    
    const activeLine = container.querySelector('.lyric-line.active')
    if (activeLine) {
      activeLine.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    }
  })
}

// 点击歌词跳转播放
function seekTo(time: number) {
  emit('seek', time)
}
</script>

<style scoped>
.lyric-display {
  width: 100%;
  height: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.no-lyric {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 14px;
}

.lyric-container {
  flex: 1;
  overflow-y: auto;
  padding: 40px 20px;
  text-align: center;
}

.lyric-line {
  padding: 12px 0;
  font-size: 16px;
  line-height: 1.8;
  color: #9ca3af;
  transition: all 0.3s;
  cursor: pointer;
}

.lyric-line:hover {
  color: #6b7280;
}

.lyric-line.active {
  color: #1f2937;
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  transform: scale(1.05);
}

/* 滚动条样式 */
.lyric-container::-webkit-scrollbar {
  width: 6px;
}

.lyric-container::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.lyric-container::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>





