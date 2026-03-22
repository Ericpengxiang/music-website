import { defineStore } from 'pinia'
import http from '../api/http'

export interface PlayerSong {
  id: number | string
  title: string
  audioUrl: string
  artist?: { id?: number | string; name?: string }
  album?: { id?: number | string; title?: string; coverUrl?: string }
  lyric?: { lrc?: string }
}

export const usePlayerStore = defineStore('player', {
  state: () => ({
    queue: [] as PlayerSong[],
    currentIndex: 0,
    isPlaying: false,
    isExpanded: false,
    currentTime: 0,
    duration: 0,
    volume: 0.8,
    isMuted: false,
    playMode: 0 as 0 | 1 | 2, // 0 顺序, 1 随机, 2 单曲循环
    audio: null as HTMLAudioElement | null,
    favorites: new Set<string | number>()
  }),
  getters: {
    currentSong(state): PlayerSong | null {
      return state.queue[state.currentIndex] || null
    },
    hasPrevious(state): boolean {
      return state.currentIndex > 0
    },
    hasNext(state): boolean {
      return state.currentIndex < state.queue.length - 1
    },
    progress(state): number {
      return state.duration > 0 ? (state.currentTime / state.duration) * 100 : 0
    }
  },
  actions: {
    attachAudio(el: HTMLAudioElement) {
      this.audio = el
      el.volume = this.volume
      el.muted = this.isMuted
    },
    setQueue(list: PlayerSong[], startIndex = 0) {
      this.queue = list || []
      this.currentIndex = Math.min(Math.max(startIndex, 0), Math.max(this.queue.length - 1, 0))
      this.loadAndPlay()
    },
    playSong(song: PlayerSong) {
      const idx = this.queue.findIndex(s => s.id === song.id)
      if (idx === -1) {
        this.queue.push(song)
        this.currentIndex = this.queue.length - 1
      } else {
        this.currentIndex = idx
      }
      this.loadAndPlay()
    },
    addToQueue(song: PlayerSong) {
      if (!this.queue.some(s => s.id === song.id)) this.queue.push(song)
    },
    async loadAndPlay() {
      if (!this.audio || !this.currentSong) return
      await this.ensureCurrentLyric()
      const url = this.normalizeUrl(this.currentSong.audioUrl)
      if (!url) return
      this.audio.src = url
      this.audio.load()
      this.audio.play()
      this.isPlaying = true
    },
    togglePlay() {
      if (!this.audio) return
      if (this.isPlaying) {
        this.audio.pause()
        this.isPlaying = false
      } else {
        this.audio.play()
        this.isPlaying = true
      }
    },
    playNext() {
      if (this.playMode === 1 && this.queue.length > 0) {
        this.currentIndex = Math.floor(Math.random() * this.queue.length)
      } else if (this.hasNext) {
        this.currentIndex += 1
      } else if (this.playMode === 0) {
        this.currentIndex = 0
      }
      this.loadAndPlay()
    },
    playPrevious() {
      if (this.hasPrevious) {
        this.currentIndex -= 1
        this.loadAndPlay()
      }
    },
    seek(percent: number) {
      if (!this.audio || this.duration <= 0) return
      this.audio.currentTime = (percent / 100) * this.duration
    },
    seekTo(time: number) {
      if (!this.audio) return
      this.audio.currentTime = Math.max(0, Math.min(time, this.duration || 0))
    },
    setExpanded(v: boolean) {
      this.isExpanded = v
    },
    setVolume(v: number) {
      this.volume = v
      if (this.audio) this.audio.volume = v
      localStorage.setItem('playerVolume', String(Math.round(v * 100)))
    },
    toggleMute() {
      this.isMuted = !this.isMuted
      if (this.audio) this.audio.muted = this.isMuted
    },
    toggleFavorite() {
      const id = this.currentSong?.id
      if (id == null) return
      if (this.favorites.has(id)) this.favorites.delete(id)
      else this.favorites.add(id)
    },
    isFavorite(id?: string | number): boolean {
      if (id == null) return false
      return this.favorites.has(id)
    },
    loadSavedVolume() {
      const saved = localStorage.getItem('playerVolume')
      if (saved) {
        const value = Math.max(0, Math.min(100, parseInt(saved))) / 100
        this.volume = value
        if (this.audio) this.audio.volume = value
      }
    },
    // helpers
    normalizeUrl(url?: string): string {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      return url.startsWith('/') ? url : `/${url}`
    },
    async ensureCurrentLyric() {
      const s = this.currentSong as any
      if (!s || !s.id) return
      // 若已有歌词，且为后端content字段，转成lrc键
      if (s.lyric && (s.lyric.lrc || s.lyric.content)) {
        if (s.lyric.content && !s.lyric.lrc) s.lyric.lrc = s.lyric.content
        return
      }
      try {
        const res = await http.get(`/lyrics/song/${s.id}`)
        if (res?.data?.content) {
          s.lyric = { lrc: res.data.content, translated: res.data.translated }
        }
      } catch {
        // 无歌词或请求失败，忽略
      }
    }
  }
})


