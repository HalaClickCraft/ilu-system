import { defineStore } from 'pinia'
import { login as loginApi, changePasswordApi } from '../features/auth/services/authService'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('authToken') || null,
    user: JSON.parse(localStorage.getItem('authUser')) || null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userRole: (state) => state.user?.role || null,
    mustChangePassword: (state) => state.user?.doitChangerMdp || false,
  },

  actions: {
    async login(matricule, password) {
      try {
        const data = await loginApi({ matricule, motDePasse: password })
        this.token = data.token
        this.user = {
          matricule: data.matricule,
          nom: data.nom,
          role: data.role,
          doitChangerMdp: data.doitChangerMdp,
        }
        localStorage.setItem('authToken', data.token)
        localStorage.setItem('authUser', JSON.stringify(this.user))
        return this.user
      } catch (error) {
        this.logout()
        throw error
      }
    },

    async changePassword(ancienMotDePasse, nouveauMotDePasse) {
      if (!this.token) throw new Error('Non authentifié')
      
      await changePasswordApi(this.token, { ancienMotDePasse, nouveauMotDePasse })
      
      // Update state
      if (this.user) {
        this.user.doitChangerMdp = false
        localStorage.setItem('authUser', JSON.stringify(this.user))
      }
    },

    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('authToken')
      localStorage.removeItem('authUser')
    },
  },
})
