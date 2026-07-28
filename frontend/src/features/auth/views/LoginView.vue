<script setup>
import LoginForm from '../components/LoginForm.vue'
</script>

<template>
  <main class="login-page">
    <LoginForm />
  </main>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  width: 100vw;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background: linear-gradient(135deg, #f5f9f8 0%, #edf3f2 100%);
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
  box-sizing: border-box;
}
</style>