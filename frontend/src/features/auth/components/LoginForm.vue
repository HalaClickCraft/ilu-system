<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const matricule = ref('')
const motDePasse = ref('')
const message = ref('')
const loading = ref(false)

async function submitLogin() {
  loading.value = true
  message.value = ''

  try {
    await authStore.login(matricule.value, motDePasse.value)
    router.push({ name: 'dashboard' })
  } catch (error) {
    message.value = error.message || 'Matricule ou mot de passe incorrect'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form class="login-card" @submit.prevent="submitLogin">
    <div class="logo-area">
      <span class="logo-icon">🔒</span>
      <h2>Système ILU</h2>
      <p class="subtitle">Portail d'évaluation & polyvalence</p>
    </div>

    <div class="input-group">
      <label for="matricule">Matricule</label>
      <input 
        id="matricule" 
        v-model="matricule" 
        placeholder="Ex: admin, chef1, rh1" 
        required 
      />
    </div>

    <div class="input-group">
      <label for="password">Mot de passe</label>
      <input 
        id="password" 
        v-model="motDePasse" 
        type="password" 
        placeholder="••••••••" 
        required 
      />
    </div>

    <button type="submit" :disabled="loading" class="login-btn">
      <span v-if="loading" class="spinner"></span>
      <span>{{ loading ? 'Connexion en cours...' : 'Se connecter' }}</span>
    </button>

    <div v-if="message" class="message-banner error">
      <span class="msg-icon">⚠️</span>
      <span class="msg-text">{{ message }}</span>
    </div>


  </form>
</template>

<style scoped>
.login-card {
  width: min(100%, 460px);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 2.5rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.logo-area {
  text-align: center;
  margin-bottom: 0.5rem;
}

.logo-icon {
  font-size: 2.5rem;
  display: block;
  margin-bottom: 0.5rem;
}

.logo-area h2 {
  font-size: 1.8rem;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0.25rem 0 0;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #334155;
  text-align: left;
}

input {
  padding: 0.8rem 1rem;
  border: 1.5px solid #cbd5e1;
  border-radius: 10px;
  background: #ffffff;
  color: #1e293b;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.9rem;
  border: 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
  transition: all 0.2s ease;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.35);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: wait;
  transform: none;
  box-shadow: none;
}

.message-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.8rem 1rem;
  border-radius: 8px;
  font-size: 0.9rem;
  text-align: left;
}

.message-banner.error {
  background: #fef2f2;
  border-left: 4px solid #ef4444;
  color: #991b1b;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.credentials-info {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px dashed #cbd5e1;
  font-size: 0.8rem;
  color: #475569;
  text-align: left;
}

.credentials-info p {
  margin: 0 0 0.5rem;
}

.credentials-info ul {
  margin: 0;
  padding-left: 1.2rem;
}

.credentials-info li {
  margin-bottom: 0.25rem;
}

code {
  font-family: monospace;
  background: rgba(0, 0, 0, 0.05);
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
  color: #0f172a;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
