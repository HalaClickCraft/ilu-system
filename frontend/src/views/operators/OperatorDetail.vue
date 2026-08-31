<template>
  <div class="space-y-6">
    <div v-if="loading" class="flex items-center justify-center py-20"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
    <div v-else-if="error" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{{ error }}</div>
    <template v-else-if="operator">
      <BreadcrumbNav :crumbs="[{ label: 'Opérateurs', to: '/operators' }, { label: operator.lastName + ' ' + operator.firstName }]" />
    <div class="flex items-center gap-4 flex-wrap">
        <button @click="$router.push('/operators')" class="text-gray-400 hover:text-gray-600 transition flex-shrink-0"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg></button>
        <div class="flex items-center gap-3"><div class="w-12 h-12 rounded-full bg-emerald-500 flex items-center justify-center text-white font-bold text-lg flex-shrink-0">{{ (operator.lastName || '')[0] }}{{ (operator.firstName || '')[0] }}</div><h1 class="text-2xl font-bold text-gray-900">{{ operator.lastName }} {{ operator.firstName }}</h1></div>
        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium flex-shrink-0" :class="operator.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">{{ operator.active !== false ? 'Actif' : 'Inactif' }}</span>
        
        <router-link
          v-if="operator.active !== false && authStore.hasAnyRole(['CHEF_EQUIPE', 'SUPERVISEUR', 'ADMIN'])"
          :to="'/training?operatorId=' + operator.id"
          class="ml-auto inline-flex items-center gap-1.5 px-3 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition"
        >
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
          Affecter à un poste
        </router-link>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 class="font-semibold text-gray-900 mb-4">Informations</h2>
          <dl class="space-y-3 text-sm">
            <div class="flex justify-between"><dt class="text-gray-500">Matricule</dt><dd class="font-medium">{{ operator.employeeId || '-' }}</dd></div>
            <div class="flex justify-between"><dt class="text-gray-500">Équipe</dt><dd class="font-medium">{{ operator.team?.name || '-' }}</dd></div>
            <div class="flex justify-between"><dt class="text-gray-500">Role</dt><dd class="font-medium">{{ operator.role || '-' }}</dd></div>
            <div class="flex justify-between"><dt class="text-gray-500">Type d'opérateur</dt><dd class="font-medium">{{ operator.operatorType === 'DEJA_EN_POSTE' ? 'Déjà en poste' : 'Nouvelle recrue' }}</dd></div>
            <div class="flex justify-between"><dt class="text-gray-500">Date d'embauche</dt><dd class="font-medium">{{ formatDate(operator.hireDate) }}</dd></div>
            <div class="flex justify-between"><dt class="text-gray-500">Date de sortie</dt><dd class="font-medium">{{ formatDate(operator.exitDate) }}</dd></div>
          </dl>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <div class="p-4 border-b border-gray-100"><h2 class="font-semibold text-gray-900">Formations</h2></div>
        <div v-if="formationsLoading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="formations.length" class="overflow-x-auto">
          <table class="w-full text-sm"><thead class="bg-gray-50"><tr><th class="text-left py-3 px-4 font-medium text-gray-500">Poste</th><th class="text-left py-3 px-4 font-medium text-gray-500">Atteint</th><th class="text-left py-3 px-4 font-medium text-gray-500">Cible</th><th class="text-left py-3 px-4 font-medium text-gray-500">Statut</th><th class="text-left py-3 px-4 font-medium text-gray-500">Debut</th><th class="text-right py-3 px-4 font-medium text-gray-500">Détail</th></tr></thead>
          <tbody><tr v-for="f in formations" :key="f.id" class="border-b border-gray-50 hover:bg-gray-50"><td class="py-3 px-4">{{ f.workstationName }}</td><td class="py-3 px-4 font-medium">{{ formatNiveau(f.achievedLevel ?? 0) }}</td><td class="py-3 px-4">{{ formatNiveau(f.targetLevel) }}</td><td class="py-3 px-4"><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(f.status)">{{ statusLabel(f.status) }}</span></td><td class="py-3 px-4 text-gray-500">{{ formatDate(f.startDate) }}</td><td class="py-3 px-4 text-right"><router-link :to="'/training/' + f.id" class="text-emerald-600 hover:underline text-sm">Voir</router-link></td></tr></tbody></table>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucune formation</div>
      </div>
    </template>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { operatorsApi } from '@/api/endpoints'
import { formatDate } from '@/shared/utils/date'
import BreadcrumbNav from '@/components/BreadcrumbNav.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const route = useRoute()
const operator = ref(null)
const formations = ref([])
const loading = ref(true)
const formationsLoading = ref(true)
const error = ref('')

const statusLabel = (s) => ({ IN_PROGRESS: 'En Cours', COMPLETED: 'Terminee', PLANNED: 'Planifiee' })[s] || s
const statusClass = (s) => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', PLANNED: 'bg-gray-100 text-gray-600' })[s] || 'bg-gray-100 text-gray-600'

function formatNiveau(level) {
  if (!level) return '-'
  const upper = String(level).toUpperCase().trim()
  if (upper === 'I' || upper === 'NIVEAU_1' || upper === '1') return 'I'
  if (upper === 'L' || upper === 'NIVEAU_2' || upper === '2') return 'L'
  if (upper === 'U' || upper === 'NIVEAU_3' || upper === '3') return 'U'
  return level
}

import { useUserScope } from '@/composables/useUserScope'
const { loadUserProjects, filterOperators } = useUserScope()

onMounted(async () => {
  loading.value = true
  try {
    await loadUserProjects()
    const r = await operatorsApi.getById(route.params.id)
    const op = r.data
    const scopedList = filterOperators([op])
    if (scopedList.length === 0) {
      error.value = "Vous n'avez pas l'autorisation d'accéder à cet opérateur."
    } else {
      operator.value = op
    }
  } catch (e) {
    console.error(e)
    error.value = "Erreur lors du chargement de l'opérateur."
  } finally {
    loading.value = false
  }

  if (operator.value) {
    try {
      const r = await operatorsApi.getFormations(route.params.id)
      formations.value = r.data
    } catch (e) {
      console.error(e)
    } finally {
      formationsLoading.value = false
    }
  } else {
    formationsLoading.value = false
  }
})
</script>