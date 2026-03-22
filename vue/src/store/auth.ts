import { defineStore } from 'pinia';

interface AuthState {
  token: string | null;
  username: string | null;
  isAdmin: boolean;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
    isAdmin: localStorage.getItem('isAdmin') === '1'
  }),
  actions: {
    login(token: string, username: string, isAdmin: boolean) {
      this.token = token;
      this.username = username;
      this.isAdmin = isAdmin;
      localStorage.setItem('token', token);
      localStorage.setItem('username', username);
      localStorage.setItem('isAdmin', isAdmin ? '1' : '0');
    },
    logout() {
      this.token = null;
      this.username = null;
      this.isAdmin = false;
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('isAdmin');
    }
  }
});



