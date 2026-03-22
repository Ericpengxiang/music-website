import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../store/auth'

const routes: RouteRecordRaw[] = [
  // 前台页面（使用FrontLayout布局）
  {
    path: '/',
    component: () => import('../views/frontend/FrontLayout.vue'),
    children: [
      // 公开页面
      { path: '', name: 'home', component: () => import('../views/frontend/Home.vue') },
      { path: 'songs', name: 'front-songs', component: () => import('../views/frontend/Songs.vue') },
      { path: 'albums', name: 'front-albums', component: () => import('../views/frontend/Albums.vue') },
      { path: 'artists', name: 'front-artists', component: () => import('../views/frontend/Artists.vue') },
      { path: 'genres', name: 'front-genres', component: () => import('../views/frontend/Genres.vue') },
      { path: 'search', name: 'search', component: () => import('../views/frontend/Search.vue') },
      { path: 'ranking', name: 'ranking', component: () => import('../views/frontend/Ranking.vue') },
      { path: 'stories', name: 'story-square', component: () => import('../views/frontend/StorySquare.vue') },
      { path: 'song/:id', name: 'song-detail', component: () => import('../views/frontend/SongDetail.vue') },
      { path: 'artist/:id', name: 'artist-detail', component: () => import('../views/frontend/ArtistDetail.vue') },
      { path: 'album/:id', name: 'album-detail', component: () => import('../views/frontend/AlbumDetail.vue') },
      { path: 'genre/:id', name: 'genre-detail', component: () => import('../views/frontend/GenreDetail.vue') },
      
      // 需要前台登录的页面（也使用FrontLayout）
      { path: 'me/recommend', name: 'my-recommend', meta: { frontAuth: true }, component: () => import('../views/frontend/Recommend.vue') },
      { path: 'me/favorites', name: 'my-favorites', meta: { frontAuth: true }, component: () => import('../views/frontend/Favorites.vue') },
      { path: 'me/playlists', name: 'my-playlists', meta: { frontAuth: true }, component: () => import('../views/frontend/Playlists.vue') },
      { path: 'me/profile', name: 'my-profile', meta: { frontAuth: true }, component: () => import('../views/frontend/Profile.vue') },
      { path: 'me/history', name: 'my-history', meta: { frontAuth: true }, component: () => import('../views/frontend/History.vue') },
      { path: 'playlist/:id', name: 'playlist-detail', meta: { frontAuth: true }, component: () => import('../views/frontend/PlaylistDetail.vue') }
    ]
  },
  
  // 用户注册/登录（独立页面）
  { path: '/user/register', name: 'user-register', component: () => import('../views/frontend/Register.vue') },
  { path: '/user/login', name: 'user-login', component: () => import('../views/frontend/UserLogin.vue') },
  
  // 管理后台（需要登录）
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/Login.vue') },
  {
    path: '/admin',
    component: () => import('../views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'genres', name: 'admin-genres', component: () => import('../views/genres/GenrePage.vue') },
      { path: 'artists', name: 'admin-artists', component: () => import('../views/artists/ArtistPage.vue') },
      { path: 'albums', name: 'admin-albums', component: () => import('../views/albums/AlbumPage.vue') },
      { path: 'songs', name: 'admin-songs', component: () => import('../views/songs/SongPage.vue') },
      { path: 'lyrics', name: 'admin-lyrics', component: () => import('../views/lyrics/LyricPage.vue') },
      { path: 'banners', name: 'admin-banners', component: () => import('../views/banners/BannerPage.vue') },
      { path: 'stories', name: 'admin-stories', component: () => import('../views/stories/StoryPage.vue') },
      { path: 'users', name: 'admin-users', component: () => import('../views/users/UserPage.vue') },
      { path: 'logs', name: 'admin-logs', component: () => import('../views/logs/AuditLogPage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  
  // 管理后台需要登录 + 管理员身份
  if (to.path.startsWith('/admin')) {
    if (to.meta.requiresAuth && !auth.token) {
      return '/admin/login'
    }
    // 非管理员令牌，强制跳转登录获取管理员token
    if (to.meta.requiresAuth && auth.token && !auth.isAdmin) {
      return '/admin/login'
    }
  }
  
  // 已登录用户访问登录页，重定向到管理后台
  if (to.path === '/admin/login' && auth.token && auth.isAdmin) {
    return '/admin'
  }

  // 前台需要登录的页面
  if (to.meta && (to.meta as any).frontAuth && !auth.token) {
    return '/user/login'
  }
})

export default router


