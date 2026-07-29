import { defineStore } from 'pinia'
import { authApi } from '../api/endpoints.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    loading: false,
    error: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    fullName: (state) => state.user?.name || 'Utilisateur',
    primaryRole: (state) => {
      if (!state.user?.roles || state.user.roles.size === 0) return ''
      const priority = ['ADMIN', 'RH', 'RESP_QUALITE', 'RESP_HSE', 'AGENT_QUALITE', 'SUPERVISEUR']
      for (const r of priority) {
        if (state.user.roles.has(r)) return r
      }
      return Array.from(state.user.roles)[0]
    },
    isAdmin: (state) => state.user?.roles?.has('ADMIN') || false,
    isRh: (state) => state.user?.roles?.has('RH') || false,
    isRespQualite: (state) => state.user?.roles?.has('RESP_QUALITE') || false,
    isRespHse: (state) => state.user?.roles?.has('RESP_HSE') || false,
    isAgentQualite: (state) => state.user?.roles?.has('AGENT_QUALITE') || false,
    isSuperviseur: (state) => state.user?.roles?.has('SUPERVISEUR') || false,
    hasRole: (state) => {
      return (role) => state.user?.roles?.has(role) || false
    },
    hasAnyRole: (state) => {
      return (roles) => roles.some(r => state.user?.roles?.has(r))
    },
  },
  actions: {
    async login(employeeId, password) {
      this.loading = true
      this.error = null
      try {
        const response = await authApi.login({ employeeId, password })
        const data = response.data
        this.token = data.token
        localStorage.setItem('token', data.token)
        this.user = {
          employeeId: data.employeeId,
          name: data.name,
          mustChangePassword: data.mustChangePassword,
          roles: new Set(data.roles || []),
        }
        return { success: true, mustChangePassword: data.mustChangePassword }
      } catch (err) {
        this.error = err.response?.data?.message || 'Identifiants invalides'
        return { success: false }
      } finally {
        this.loading = false
      }
    },
    async changePassword(currentPassword, newPassword, confirmPassword) {
      this.loading = true
      this.error = null
      try {
        await authApi.changePassword({ currentPassword, newPassword, confirmPassword })
        return true
      } catch (err) {
        this.error = err.response?.data?.message || 'Erreur lors du changement de mot de passe'
        return false
      } finally {
        this.loading = false
      }
    },
    restoreFromToken() {
      const token = this.token
      if (!token) return
      try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        const roles = payload.roles || []
        this.user = {
          employeeId: payload.sub,
          name: payload.name || payload.sub,
          mustChangePassword: false,
          roles: new Set(roles),
        }
      } catch {
        this.logout()
      }
    },
    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
    },
  },
})
