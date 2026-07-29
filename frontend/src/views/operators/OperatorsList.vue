<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div><h1 class="text-2xl font-bold text-gray-900">Operateurs</h1><p class="text-gray-500 mt-1">Gestion des operateurs de l'usine</p></div>
      <button @click="openCreateModal" class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>Nouvel Operateur
      </button>
    </div>
    <div class="bg-white rounded-xl shadow-sm border border-gray-200">
      <div class="p-4 border-b border-gray-100"><input v-model="search" type="text" placeholder="Rechercher un operateur..." class="w-full sm:w-80 px-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none" /></div>
      <div v-if="loading" class="flex items-center justify-center py-16"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="filteredOperators.length" class="overflow-x-auto">
        <table class="w-full text-sm"><thead class="bg-gray-50"><tr><th class="text-left py-3 px-4 font-medium text-gray-500">Nom</th><th class="text-left py-3 px-4 font-medium text-gray-500">Matricule</th><th class="text-left py-3 px-4 font-medium text-gray-500">Equipe</th><th class="text-left py-3 px-4 font-medium text-gray-500">Statut</th><th class="text-left py-3 px-4 font-medium text-gray-500">Date Embauche</th><th class="text-right py-3 px-4 font-medium text-gray-500">Actions</th></tr></thead>
          <tbody><tr v-for="op in filteredOperators" :key="op.id" class="border-b border-gray-50 hover:bg-gray-50"><td class="py-3 px-4"><router-link :to="'/operators/' + op.id" class="font-medium text-emerald-600 hover:underline">{{ op.lastName }} {{ op.firstName }}</router-link></td><td class="py-3 px-4 text-gray-500">{{ op.employeeId || '-' }}</td><td class="py-3 px-4 text-gray-500">{{ op.team?.name || '-' }}</td><td class="py-3 px-4"><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="op.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">{{ op.active !== false ? 'Actif' : 'Inactif' }}</span></td><td class="py-3 px-4 text-gray-500">{{ formatDate(op.hireDate) }}</td><td class="py-3 px-4 text-right space-x-2"><button @click="$router.push('/operators/' + op.id)" class="text-gray-400 hover:text-emerald-600 transition" title="Details"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg></button><button v-if="op.active !== false" @click="deactivateOperator(op.id)" class="text-gray-400 hover:text-red-600 transition" title="Desactiver"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg></button><button v-else @click="activateOperator(op.id)" class="text-gray-400 hover:text-emerald-600 transition" title="Activer"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg></button></td></tr></tbody>
        </table>
      </div>
      <div v-else class="text-center py-16 text-gray-400">Aucun operateur trouve</div>
    </div>
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6"><h2 class="text-lg font-semibold text-gray-900 mb-4">Nouvel Operateur</h2><form @submit.prevent="createOperator" class="space-y-4"><div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="form.lastName" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div><div><label class="block text-sm font-medium text-gray-700 mb-1">Prenom</label><input v-model="form.firstName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div><div><label class="block text-sm font-medium text-gray-700 mb-1">Matricule</label><input v-model="form.employeeId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div><div><label class="block text-sm font-medium text-gray-700 mb-1">Role</label><input v-model="form.role" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" placeholder="Ex: Operateur" /></div><div><label class="block text-sm font-medium text-gray-700 mb-1">Date d'embauche</label><input v-model="form.hireDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div><div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div><div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCreateModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm rounded-lg transition">Creer</button></div></form></div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { operatorsApi } from '@/api/endpoints'

const operators = ref([])
const loading = ref(true)
const search = ref('')
const showCreateModal = ref(false)
const creating = ref(false)
const error = ref('')
const form = ref({ lastName: '', firstName: '', employeeId: '', role: '', hireDate: '' })

const filteredOperators = computed(() => {
  const q = search.value.toLowerCase()
  return operators.value.filter(o => `${o.lastName} ${o.firstName} ${o.employeeId}`.toLowerCase().includes(q))
})

const formatDate = (d) => d ? new Date(d).toLocaleDateString('fr-FR') : '-'

const fetchOperators = async () => { loading.value = true; try { const r = await operatorsApi.getAll(); operators.value = r.data } catch (e) { console.error(e) } finally { loading.value = false } }
const openCreateModal = () => { form.value = { lastName: '', firstName: '', employeeId: '', role: '', hireDate: '' }; error.value = ''; showCreateModal.value = true }
const createOperator = async () => { creating.value = true; error.value = ''; try { await operatorsApi.create(form.value); showCreateModal.value = false; fetchOperators() } catch (e) { error.value = e.response?.data?.message || 'Erreur' } finally { creating.value = false } }
const deactivateOperator = async (id) => { try { await operatorsApi.deactivate(id); fetchOperators() } catch (e) { console.error(e) } }
const activateOperator = async (id) => { try { await operatorsApi.activate(id); fetchOperators() } catch (e) { console.error(e) } }

onMounted(fetchOperators)
</script>