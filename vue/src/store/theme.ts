import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDark: localStorage.getItem('theme') === 'dark'
  }),
  actions: {
    toggle() {
      this.isDark = !this.isDark
      this.apply()
    },
    setDark(dark: boolean) {
      this.isDark = dark
      this.apply()
    },
    apply() {
      localStorage.setItem('theme', this.isDark ? 'dark' : 'light')
      document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : 'light')
    },
    init() {
      // 初始化主题
      const saved = localStorage.getItem('theme')
      if (saved) {
        this.isDark = saved === 'dark'
      } else {
        // 跟随系统
        this.isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      }
      this.apply()
    }
  }
})





