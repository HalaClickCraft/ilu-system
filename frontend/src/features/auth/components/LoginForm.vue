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
    <div class="form-header">
      <h2>Connexion</h2>
      <p class="form-sub">Accédez à votre espace ILU avec votre matricule.</p>
    </div>

    <div class="input-group">
      <label for="matricule">Matricule</label>
      <input
        id="matricule"
        v-model="matricule"
        placeholder="Ex: admin, chef1, rh1"
        autocomplete="username"
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
        autocomplete="current-password"
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
  width: min(100%, 400px);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 2.5rem;
  background: white;
  border: 1px solid #e3eeee;
  border-radius: 10px;
  box-shadow: 0 12px 32px rgba(18, 63, 67, 0.1);
}

.form-header h2 {
  margin: 0;
  color: #254b4e;
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.form-sub {
  margin: 0.35rem 0 0;
  color: #547174;
  font-size: 0.88rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.input-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #416568;
}

.input-group input {
  padding: 0.75rem 1rem;
  border: 1px solid #d8e5e4;
  border-radius: 6px;
  background: white;
  color: #254b4e;
  font-size: 0.95rem;
  font-family: inherit;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.input-group input:focus {
  outline: none;
  border-color: #58a88c;
  box-shadow: 0 0 0 3px rgba(88, 168, 140, 0.14);
}

.login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  padding: 0.85rem;
  border: 0;
  border-radius: 6px;
  background: #2c766f;
  color: white;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s ease;
}

.login-btn:hover:not(:disabled) {
  background: #205d58;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

.message-banner {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.88rem;
}

.message-banner.error {
  background: #fef2f2;
  color: #b91c1c;
  border-left: 4px solid #ef4444;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>