import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Check token expiry every minute and warn before it expires
setInterval(() => {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const expiresAt = payload.exp * 1000
    const now = Date.now()
    const fiveMinutes = 5 * 60 * 1000
    if (expiresAt - now < fiveMinutes && expiresAt - now > 0 && !window._tokenWarned) {
      window._tokenWarned = true
      const minutesLeft = Math.ceil((expiresAt - now) / 60000)
      // Create a subtle warning banner
      const banner = document.createElement('div')
      banner.id = 'jwt-expiry-warning'
      banner.className = 'fixed top-0 left-0 right-0 z-[100] bg-amber-500 text-white text-center py-2 text-sm font-medium'
      banner.textContent = `Votre session expire dans ${minutesLeft} minute(s). Enregistrez votre travail.`
      document.body.appendChild(banner)
      setTimeout(() => { banner.remove(); window._tokenWarned = false }, 30000)
    }
  } catch { /* ignore */ }
}, 60000)

export default api
