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

      <!-- Growth Diagram -->
      <div v-if="tracking.length >= 2" class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <h2 class="font-semibold text-gray-900 mb-4">Courbe de Progression</h2>
        <div class="relative" style="height: 250px;">
          <svg :viewBox="'0 0 ' + svgWidth + ' ' + svgHeight" class="w-full h-full" preserveAspectRatio="none">
            <!-- Grid lines -->
            <line v-for="i in 5" :key="'grid-'+i" :x1="padding" :y1="padding + (chartH / 4) * (i-1)" :x2="svgWidth - paddingRight" :y2="padding + (chartH / 4) * (i-1)" stroke="#e5e7eb" stroke-dasharray="4,4" />
            <!-- Y axis labels -->
            <text v-for="i in 5" :key="'label-'+i" :x="padding - 5" :y="padding + (chartH / 4) * (i-1) + 4" text-anchor="end" class="text-xs" fill="#9ca3af">{{ maxLevel - (i-1) * (maxLevel / 4) }}</text>
            <!-- Objectif line -->
            <line v-if="maxObjectif > 0" :x1="padding" :y1="yPos(maxObjectif)" :x2="svgWidth - paddingRight" :y2="yPos(maxObjectif)" stroke="#f59e0b" stroke-width="2" stroke-dasharray="6,3" />
            <text v-if="maxObjectif > 0" :x="svgWidth - paddingRight + 5" :y="yPos(maxObjectif) + 4" class="text-xs" fill="#f59e0b">Objectif</text>
            <!-- Area fill -->
            <polygon v-if="trackPoints.length" :points="areaPoints" fill="rgba(16,185,129,0.1)" />
            <!-- Line -->
            <polyline v-if="trackPoints.length" :points="trackPoints" fill="none" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
            <!-- Cadence line -->
            <polyline v-if="cadencePoints.length" :points="cadencePoints" fill="none" stroke="#3b82f6" stroke-width="2" stroke-dasharray="5,3" />
            <!-- Data points -->
            <circle v-for="(pt, idx) in tracking" :key="pt.id" :cx="xPos(idx)" :cy="yPos(pt.dailyLevel || 0)" r="4" fill="#10b981" stroke="white" stroke-width="2" />
          </svg>
        </div>
        <div class="flex gap-4 mt-2 text-xs text-gray-500">
          <span class="flex items-center gap-1"><span class="w-3 h-0.5 bg-emerald-500 inline-block"></span> Niveau</span>
          <span v-if="cadencePoints.length" class="flex items-center gap-1"><span class="w-3 h-0.5 bg-blue-500 inline-block" style="border-top: 2px dashed #3b82f6;"></span> Cadence</span>
          <span v-if="maxObjectif > 0" class="flex items-center gap-1"><span class="w-3 h-0.5 bg-amber-500 inline-block" style="border-top: 2px dashed #f59e0b;"></span> Objectif</span>
        </div>
      </div>

      <!-- Suivi Quotidien Table -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <div class="p-4 border-b border-gray-100 flex items-center justify-between">
          <h2 class="font-semibold text-gray-900">Suivi Quotidien (12 jours)</h2>
          <div class="flex gap-2">
            <button v-if="formation.status === 'IN_PROGRESS'" @click="showAddTracking = true" class="inline-flex items-center gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 rounded-lg text-sm font-medium transition-colors">Ajouter</button>
            <button v-if="isChefEquipe && formation.status === 'IN_PROGRESS'" @click="showCadenceModal = true" class="inline-flex items-center gap-1.5 bg-blue-600 hover:bg-blue-700 text-white px-3 py-1.5 rounded-lg text-sm font-medium transition-colors">Cadence</button>
            <button v-if="isAgentQualite && formation.status === 'IN_PROGRESS'" @click="showDefautsModal = true" class="inline-flex items-center gap-1.5 bg-red-600 hover:bg-red-700 text-white px-3 py-1.5 rounded-lg text-sm font-medium transition-colors">Defauts</button>
            <button v-if="formation.status === 'IN_PROGRESS'" @click="completeFormation" class="inline-flex items-center gap-1.5 bg-amber-500 hover:bg-amber-600 text-white px-3 py-1.5 rounded-lg text-sm font-medium transition-colors">Marquer Terminee</button>
          </div>
        </div>
        <div v-if="trackingLoading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="tracking.length" class="overflow-x-auto">
          <table class="w-full text-sm"><thead class="bg-gray-50"><tr>
            <th class="text-left py-3 px-3 font-medium text-gray-500">Date</th>
            <th class="text-left py-3 px-3 font-medium text-gray-500">Niveau</th>
            <th class="text-left py-3 px-3 font-medium text-gray-500">Objectif</th>
            <th class="text-left py-3 px-3 font-medium text-gray-500">Cadence</th>
            <th class="text-left py-3 px-3 font-medium text-gray-500">Defauts</th>
            <th class="text-left py-3 px-3 font-medium text-gray-500">Commentaire</th>
            <th class="text-left py-3 px-3 font-medium text-gray-500">Superviseur</th>
          </tr></thead>
          <tbody><tr v-for="t in tracking" :key="t.id" class="border-b border-gray-50 hover:bg-gray-50">
            <td class="py-3 px-3 text-gray-500">{{ formatDate(t.trackingDate) }}</td>
            <td class="py-3 px-3 font-medium">{{ t.dailyLevel ?? '-' }}</td>
            <td class="py-3 px-3 text-gray-500">{{ t.objectif ?? '-' }}</td>
            <td class="py-3 px-3 text-blue-600 font-medium">{{ t.cadence ?? '-' }}</td>
            <td class="py-3 px-3 text-red-600 font-medium">{{ t.defauts ?? '-' }}</td>
            <td class="py-3 px-3 text-gray-500">{{ t.comment || '-' }}</td>
            <td class="py-3 px-3 text-gray-500">{{ t.supervisor || '-' }}</td>
          </tr></tbody></table>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucun suivi enregistre</div>
      </div>
    </template>

    <!-- Add Tracking Modal -->
    <div v-if="showAddTracking" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showAddTracking = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Ajouter un Suivi</h2>
        <form @submit.prevent="addTracking" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Date</label><input v-model="trackForm.trackingDate" type="date" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Niveau atteint</label><input v-model.number="trackForm.dailyLevel" type="number" min="0" max="100" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Objectif</label><input v-model.number="trackForm.objectif" type="number" min="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" placeholder="Nombre cible" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Commentaire</label><textarea v-model="trackForm.comment" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none resize-none"></textarea></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Superviseur</label><input v-model="trackForm.supervisor" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showAddTracking = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button type="submit" :disabled="saving" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg">Enregistrer</button></div>
        </form>
      </div>
    </div>

    <!-- Cadence Modal (Chef d'Equipe) -->
    <div v-if="showCadenceModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCadenceModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Saisir Cadence</h2>
        <form @submit.prevent="submitCadence" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Date</label><input v-model="cadenceForm.trackingDate" type="date" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Cadence (pieces/heure)</label><input v-model.number="cadenceForm.cadence" type="number" min="0" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Objectif</label><input v-model.number="cadenceForm.objectif" type="number" min="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCadenceModal = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button type="submit" :disabled="saving" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg">Enregistrer</button></div>
        </form>
      </div>
    </div>

    <!-- Defauts Modal (Agent Qualite) -->
    <div v-if="showDefautsModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showDefautsModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Saisir Defauts</h2>
        <form @submit.prevent="submitDefauts" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Date</label><input v-model="defautsForm.trackingDate" type="date" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-red-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nombre de defauts</label><input v-model.number="defautsForm.defauts" type="number" min="0" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-red-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showDefautsModal = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button type="submit" :disabled="saving" class="px-4 py-2 bg-red-600 text-white text-sm rounded-lg">Enregistrer</button></div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { trainingApi } from '@/api/endpoints'

const route = useRoute()
const authStore = useAuthStore()
const formation = ref(null)
const tracking = ref([])
const loading = ref(true)
const trackingLoading = ref(true)
const showAddTracking = ref(false)
const showCadenceModal = ref(false)
const showDefautsModal = ref(false)
const saving = ref(false)
const trackForm = ref({ trackingDate: new Date().toISOString().split('T')[0], dailyLevel: 0, objectif: null, comment: '', supervisor: '' })
const cadenceForm = ref({ trackingDate: new Date().toISOString().split('T')[0], cadence: null, objectif: null })
const defautsForm = ref({ trackingDate: new Date().toISOString().split('T')[0], defauts: null })

const isChefEquipe = computed(() => authStore.hasRole('CHEF_EQUIPE'))
const isAgentQualite = computed(() => authStore.hasRole('AGENT_QUALITE'))

const statusLabel = (s) => ({ IN_PROGRESS: 'En Cours', COMPLETED: 'Terminee', PLANNED: 'Planifiee' })[s] || s
const statusClass = (s) => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', PLANNED: 'bg-gray-100 text-gray-600' })[s] || 'bg-gray-100 text-gray-600'
const formatDate = (d) => d ? new Date(d).toLocaleDateString('fr-FR') : '-'

// Chart constants
const padding = 35
const paddingRight = 15
const svgWidth = 600
const svgHeight = 250
const chartH = svgHeight - padding - 20

const maxLevel = computed(() => {
  const levels = tracking.value.map(t => t.dailyLevel || 0)
  const objs = tracking.value.map(t => t.objectif || 0)
  const max = Math.max(...levels, ...objs, 1)
  return Math.ceil(max * 1.2)
})

const maxObjectif = computed(() => {
  return Math.max(...tracking.value.map(t => t.objectif || 0))
})

const xPos = (idx) => {
  if (tracking.value.length <= 1) return padding
  return padding + (idx / (tracking.value.length - 1)) * (svgWidth - padding - paddingRight)
}

const yPos = (val) => {
  return padding + chartH - (val / maxLevel.value) * chartH
}

const trackPoints = computed(() => {
  return tracking.value.map((t, i) => `${xPos(i)},${yPos(t.dailyLevel || 0)}`).join(' ')
})

const cadencePoints = computed(() => {
  const withCadence = tracking.value.filter(t => t.cadence != null)
  if (withCadence.length < 2) return ''
  const indices = tracking.value.map((t, i) => t.cadence != null ? i : -1).filter(i => i >= 0)
  return indices.map(i => `${xPos(i)},${yPos(tracking.value[i].cadence)}`).join(' ')
})

const areaPoints = computed(() => {
  if (tracking.value.length < 2) return ''
  const first = `${xPos(0)},${yPos(0)}`
  const line = tracking.value.map((t, i) => `${xPos(i)},${yPos(t.dailyLevel || 0)}`).join(' ')
  const last = `${xPos(tracking.value.length - 1)},${yPos(0)}`
  return `${first} ${line} ${last}`
})

const fetchTracking = async () => { trackingLoading.value = true; try { const r = await trainingApi.getTracking(route.params.id); tracking.value = r.data } catch (e) { console.error(e) } finally { trackingLoading.value = false } }

onMounted(async () => {
  try { const r = await trainingApi.getFormations(); formation.value = r.data.find(f => f.id == route.params.id) } catch (e) { console.error(e) } finally { loading.value = false }
  fetchTracking()
})

const addTracking = async () => {
  saving.value = true
  try { await trainingApi.addTracking(route.params.id, trackForm.value); showAddTracking.value = false; trackForm.value = { trackingDate: new Date().toISOString().split('T')[0], dailyLevel: 0, objectif: null, comment: '', supervisor: '' }; fetchTracking() }
  catch (e) { console.error(e) } finally { saving.value = false }
}

const submitCadence = async () => {
  saving.value = true
  try { await trainingApi.addCadence(route.params.id, cadenceForm.value); showCadenceModal.value = false; cadenceForm.value = { trackingDate: new Date().toISOString().split('T')[0], cadence: null, objectif: null }; fetchTracking() }
  catch (e) { console.error(e) } finally { saving.value = false }
}

const submitDefauts = async () => {
  saving.value = true
  try { await trainingApi.addDefauts(route.params.id, defautsForm.value); showDefautsModal.value = false; defautsForm.value = { trackingDate: new Date().toISOString().split('T')[0], defauts: null }; fetchTracking() }
  catch (e) { console.error(e) } finally { saving.value = false }
}

const completeFormation = async () => { try { await trainingApi.completeFormation(route.params.id); formation.value = { ...formation.value, status: 'COMPLETED' } } catch (e) { console.error(e) } }
</script>