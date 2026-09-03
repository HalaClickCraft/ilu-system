import { defineStore } from 'pinia'
import { authApi } from '../api/endpoints.js'

function decodeJwt(token) {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    while (base64.length % 4) {
      base64 += '='
    }
    const jsonStr = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonStr)
  } catch (e) {
    console.warn('Failed to parse JWT token', e)
    return null
  }
}

function getStoredUser() {
  try {
    const raw = localStorage.getItem('user')
    if (!raw) return null
    const parsed = JSON.parse(raw)
    if (parsed && parsed.roles) {
      parsed.roles = new Set(Array.isArray(parsed.roles) ? parsed.roles : Array.from(parsed.roles))
    }
    return parsed
  } catch {
    return null
  }
}

function getUserRoles(user) {
  if (!user || !user.roles) return []
  if (user.roles instanceof Set) return Array.from(user.roles)
  if (Array.isArray(user.roles)) {
    return user.roles.map(r => (typeof r === 'object' && r ? r.label || r.name || '' : String(r)))
  }
  return []
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: getStoredUser(),
    token: localStorage.getItem('token') || null,
    loading: false,
    error: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    fullName: (state) => state.user?.name || 'Utilisateur',
    primaryRole: (state) => {
      const roles = getUserRoles(state.user)
      if (roles.length === 0) return ''
      const priority = ['ADMIN', 'RH', 'SUPERVISEUR', 'RESP_QUALITE', 'RESP_HSE', 'CHEF_EQUIPE', 'AGENT_QUALITE']
      for (const r of priority) {
        if (roles.includes(r)) return r
      }
      return roles[0]
    },
    isAdmin: (state) => getUserRoles(state.user).includes('ADMIN'),
    isRh: (state) => getUserRoles(state.user).includes('RH'),
    isRespQualite: (state) => getUserRoles(state.user).includes('RESP_QUALITE'),
    isRespHse: (state) => getUserRoles(state.user).includes('RESP_HSE'),
    isAgentQualite: (state) => getUserRoles(state.user).includes('AGENT_QUALITE'),
    isSuperviseur: (state) => getUserRoles(state.user).includes('SUPERVISEUR'),
    isChefEquipe: (state) => getUserRoles(state.user).includes('CHEF_EQUIPE'),
    hasRole: (state) => {
      return (role) => getUserRoles(state.user).includes(role)
    },
    hasAnyRole: (state) => {
      return (roles) => {
        const userRoles = getUserRoles(state.user)
        return roles.some(r => userRoles.includes(r))
      }
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
        const rolesArray = Array.from(data.roles || [])
        this.user = {
          id: data.id,
          employeeId: data.employeeId,
          name: data.name,
          mustChangePassword: data.mustChangePassword,
          roles: new Set(rolesArray),
        }
        localStorage.setItem('user', JSON.stringify({
          id: data.id,
          employeeId: data.employeeId,
          name: data.name,
          mustChangePassword: data.mustChangePassword,
          roles: rolesArray,
        }))
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
      const stored = getStoredUser()
      if (stored) {
        this.user = stored
        return
      }
      const token = this.token
      if (!token) return
      const payload = decodeJwt(token)
      if (payload) {
        const roles = payload.roles || []
        this.user = {
          id: payload.userId || null,
          employeeId: payload.sub,
          name: payload.name || payload.sub,
          mustChangePassword: false,
          roles: new Set(roles),
        }
        localStorage.setItem('user', JSON.stringify({
          id: payload.userId || null,
          employeeId: payload.sub,
          name: payload.name || payload.sub,
          mustChangePassword: false,
          roles: roles,
        }))
      }
    },
    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
  },
})
