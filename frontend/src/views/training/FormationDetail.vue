<template>
  <div class="space-y-6">
    <div v-if="loading" class="flex items-center justify-center py-20"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
    <template v-else-if="formation">
      <div class="flex items-center gap-4">
        <button @click="$router.push('/training')" class="text-gray-400 hover:text-gray-600 transition"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg></button>
        <h1 class="text-2xl font-bold text-gray-900">Detail Formation</h1>
        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="statusClass(formation.status)">{{ statusLabel(formation.status) }}</span>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5"><p class="text-sm text-gray-500">Operateur</p><p class="font-semibold mt-1">{{ formation.operatorName }}</p></div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5"><p class="text-sm text-gray-500">Poste</p><p class="font-semibold mt-1">{{ formation.workstationName }}</p></div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5"><p class="text-sm text-gray-500">Niveau Cible</p><p class="font-semibold mt-1">{{ formation.targetLevel }}</p></div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5"><p class="text-sm text-gray-500">Niveau Atteint</p><p class="font-semibold mt-1 text-emerald-600">{{ formation.achievedLevel ?? 0 }}</p></div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <div class="p-4 border-b border-gray-100 flex items-center justify-between">
          <h2 class="font-semibold text-gray-900">Suivi Quotidien</h2>
          <div class="flex gap-2">
            <button v-if="formation.status === 'IN_PROGRESS'" @click="showAddTracking = true" class="inline-flex items-center gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 rounded-lg text-sm font-medium transition-colors">Ajouter</button>
            <button v-if="formation.status === 'IN_PROGRESS'" @click="completeFormation" class="inline-flex items-center gap-1.5 bg-amber-500 hover:bg-amber-600 text-white px-3 py-1.5 rounded-lg text-sm font-medium transition-colors">Marquer Terminee</button>
          </div>
        </div>
        <div v-if="trackingLoading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="tracking.length" class="overflow-x-auto">
          <table class="w-full text-sm"><thead class="bg-gray-50"><tr><th class="text-left py-3 px-4 font-medium text-gray-500">Date</th><th class="text-left py-3 px-4 font-medium text-gray-500">Niveau Atteint</th><th class="text-left py-3 px-4 font-medium text-gray-500">Commentaire</th><th class="text-left py-3 px-4 font-medium text-gray-500">Superviseur</th></tr></thead>
          <tbody><tr v-for="t in tracking" :key="t.id" class="border-b border-gray-50 hover:bg-gray-50"><td class="py-3 px-4 text-gray-500">{{ formatDate(t.trackingDate) }}</td><td class="py-3 px-4 font-medium">{{ t.dailyLevel }}</td><td class="py-3 px-4 text-gray-500">{{ t.comment || '-' }}</td><td class="py-3 px-4 text-gray-500">{{ t.supervisor || '-' }}</td></tr></tbody></table>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucun suivi enregistre</div>
      </div>
    </template>
    <div v-if="showAddTracking" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showAddTracking = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Ajouter un Suivi</h2>
        <form @submit.prevent="addTracking" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Date</label><input v-model="trackForm.trackingDate" type="date" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Niveau atteint</label><input v-model.number="trackForm.dailyLevel" type="number" min="1" max="5" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Commentaire</label><textarea v-model="trackForm.comment" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none resize-none"></textarea></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Superviseur</label><input v-model="trackForm.supervisor" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showAddTracking = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button type="submit" :disabled="saving" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg">Enregistrer</button></div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { trainingApi } from '@/api/endpoints'

const route = useRoute()
const formation = ref(null)
const tracking = ref([])
const loading = ref(true)
const trackingLoading = ref(true)
const showAddTracking = ref(false)
const saving = ref(false)
const trackForm = ref({ trackingDate: new Date().toISOString().split('T')[0], dailyLevel: 1, comment: '', supervisor: '' })

const statusLabel = (s) => ({ IN_PROGRESS: 'En Cours', COMPLETED: 'Terminee', PLANNED: 'Planifiee' })[s] || s
const statusClass = (s) => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', PLANNED: 'bg-gray-100 text-gray-600' })[s] || 'bg-gray-100 text-gray-600'
const formatDate = (d) => d ? new Date(d).toLocaleDateString('fr-FR') : '-'

const fetchTracking = async () => { trackingLoading.value = true; try { const r = await trainingApi.getTracking(route.params.id); tracking.value = r.data } catch (e) { console.error(e) } finally { trackingLoading.value = false } }

onMounted(async () => {
  try { const r = await trainingApi.getFormations(); formation.value = r.data.find(f => f.id == route.params.id) } catch (e) { console.error(e) } finally { loading.value = false }
  fetchTracking()
})

const addTracking = async () => {
  saving.value = true
  try { await trainingApi.addTracking(route.params.id, trackForm.value); showAddTracking.value = false; trackForm.value = { trackingDate: new Date().toISOString().split('T')[0], dailyLevel: 1, comment: '', supervisor: '' }; fetchTracking() }
  catch (e) { console.error(e) } finally { saving.value = false }
}

const completeFormation = async () => { try { await trainingApi.completeFormation(route.params.id); formation.value = { ...formation.value, status: 'COMPLETED' } } catch (e) { console.error(e) } }
</script>