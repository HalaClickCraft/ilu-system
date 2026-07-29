<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div><h1 class="text-2xl font-bold text-gray-900">Formation</h1><p class="text-gray-500 mt-1">Suivi et planification des formations ILU</p></div>
      <button @click="showCreate = true" class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>Nouvelle Formation
      </button>
    </div>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5"><p class="text-sm text-gray-500">Total Formations</p><p class="text-2xl font-bold mt-1">{{ formations.length }}</p></div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5"><p class="text-sm text-gray-500">En Cours</p><p class="text-2xl font-bold text-amber-600 mt-1">{{ inProgressCount }}</p></div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5"><p class="text-sm text-gray-500">Terminees</p><p class="text-2xl font-bold text-emerald-600 mt-1">{{ completedCount }}</p></div>
    </div>
    <div class="bg-white rounded-xl shadow-sm border border-gray-200">
      <div class="p-4 border-b border-gray-100 flex flex-col sm:flex-row gap-3">
        <input v-model="search" type="text" placeholder="Rechercher..." class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" />
        <select v-model="statusFilter" class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
          <option value="">Tous les statuts</option>
          <option value="IN_PROGRESS">En Cours</option>
          <option value="COMPLETED">Terminee</option>
          <option value="PLANNED">Planifiee</option>
        </select>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-16"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="filteredFormations.length" class="overflow-x-auto">
        <table class="w-full text-sm"><thead class="bg-gray-50"><tr><th class="text-left py-3 px-4 font-medium text-gray-500">Operateur</th><th class="text-left py-3 px-4 font-medium text-gray-500">Poste</th><th class="text-left py-3 px-4 font-medium text-gray-500">Atteint</th><th class="text-left py-3 px-4 font-medium text-gray-500">Cible</th><th class="text-left py-3 px-4 font-medium text-gray-500">Statut</th><th class="text-right py-3 px-4 font-medium text-gray-500">Actions</th></tr></thead>
          <tbody><tr v-for="f in filteredFormations" :key="f.id" class="border-b border-gray-50 hover:bg-gray-50"><td class="py-3 px-4 font-medium">{{ f.operatorName }}</td><td class="py-3 px-4 text-gray-500">{{ f.workstationName }}</td><td class="py-3 px-4">{{ f.achievedLevel ?? 0 }}</td><td class="py-3 px-4">{{ f.targetLevel }}</td><td class="py-3 px-4"><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(f.status)">{{ statusLabel(f.status) }}</span></td><td class="py-3 px-4 text-right"><router-link :to="'/training/' + f.id" class="text-emerald-600 hover:underline text-sm">Detail</router-link></td></tr></tbody></table>
      </div>
      <div v-else class="text-center py-16 text-gray-400">Aucune formation trouvee</div>
    </div>
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreate = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouvelle Formation</h2>
        <form @submit.prevent="createFormation" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Operateur</label><select v-model="form.operatorId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"><option value="">Selectionner</option><option v-for="op in activeOperators" :key="op.id" :value="op.id">{{ op.lastName }} {{ op.firstName }}</option></select></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Poste de travail</label><select v-model="form.workstationId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"><option value="">Selectionner</option><option v-for="ws in workstations" :key="ws.id" :value="ws.id">{{ ws.name }}</option></select></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Niveau cible</label><input v-model.number="form.targetLevel" type="number" min="1" max="5" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCreate = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Creer</button></div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { trainingApi, operatorsApi, structureApi } from '@/api/endpoints'

const formations = ref([])
const activeOperators = ref([])
const workstations = ref([])
const loading = ref(true)
const search = ref('')
const statusFilter = ref('')
const showCreate = ref(false)
const creating = ref(false)
const form = ref({ operatorId: '', workstationId: '', targetLevel: 3 })

const inProgressCount = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS').length)
const completedCount = computed(() => formations.value.filter(f => f.status === 'COMPLETED').length)

const statusLabel = (s) => ({ IN_PROGRESS: 'En Cours', COMPLETED: 'Terminee', PLANNED: 'Planifiee' })[s] || s
const statusClass = (s) => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', PLANNED: 'bg-gray-100 text-gray-600' })[s] || 'bg-gray-100 text-gray-600'

const filteredFormations = computed(() => {
  let list = formations.value
  if (search.value) { const q = search.value.toLowerCase(); list = list.filter(f => (f.operatorName + ' ' + f.workstationName).toLowerCase().includes(q)) }
  if (statusFilter.value) list = list.filter(f => f.status === statusFilter.value)
  return list
})

onMounted(async () => {
  loading.value = true
  try { const [f, ops, ws] = await Promise.all([trainingApi.getFormations(), operatorsApi.getActive(), structureApi.getWorkstations()]); formations.value = f.data; activeOperators.value = ops.data; workstations.value = ws.data } catch (e) { console.error(e) } finally { loading.value = false }
})

const createFormation = async () => {
  creating.value = true
  try { await trainingApi.createFormation({ operatorId: form.value.operatorId, workstationId: form.value.workstationId, targetLevel: form.value.targetLevel }); showCreate.value = false; formations.value = (await trainingApi.getFormations()).data } catch (e) { console.error(e) } finally { creating.value = false }
}
</script>