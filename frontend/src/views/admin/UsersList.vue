<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div><h1 class="text-2xl font-bold text-gray-900">Gestion des Utilisateurs</h1><p class="text-gray-500 mt-1">Comptes et droits d'acces au systeme</p></div>
      <div class="flex gap-2">
        <button @click="handleResetDatabase" class="inline-flex items-center gap-2 bg-rose-600 hover:bg-rose-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors shadow-sm" title="Vider tous les opérateurs et formations de test">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
          Remettre la Base à 0
        </button>
        <button @click="showCreateModal = true" class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors shadow-sm">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>Nouvel Utilisateur
        </button>
      </div>
    </div>
    <div class="bg-white rounded-xl shadow-sm border border-gray-200">
      <!-- Filters bar -->
      <div class="p-4 border-b border-gray-100 flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div class="relative flex-1 max-w-sm w-full">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Rechercher un utilisateur (nom, matricule, CIN)..."
            class="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
          />
        </div>
        
        <div class="flex items-center gap-2">
          <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Taille de page:</label>
          <select
            v-model="pageSize"
            class="px-2.5 py-1.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
          >
            <option :value="10">10</option>
            <option :value="15">15</option>
            <option :value="25">25</option>
            <option :value="50">50</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-16"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="filteredUsers.length" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" @click="handleSort('name')" class="text-left py-3 px-4 font-semibold text-gray-500 cursor-pointer hover:bg-gray-100 select-none">
                Nom <span v-if="sortBy === 'name'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
              </th>
              <th scope="col" @click="handleSort('employeeId')" class="text-left py-3 px-4 font-semibold text-gray-500 cursor-pointer hover:bg-gray-100 select-none">
                Matricule <span v-if="sortBy === 'employeeId'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
              </th>
              <th scope="col" @click="handleSort('nationalId')" class="text-left py-3 px-4 font-semibold text-gray-500 cursor-pointer hover:bg-gray-100 select-none">
                CIN <span v-if="sortBy === 'nationalId'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
              </th>
              <th scope="col" class="text-left py-3 px-4 font-semibold text-gray-500">Rôles</th>
              <th scope="col" @click="handleSort('active')" class="text-left py-3 px-4 font-semibold text-gray-500 cursor-pointer hover:bg-gray-100 select-none">
                Statut <span v-if="sortBy === 'active'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
              </th>
              <th scope="col" class="text-left py-3 px-4 font-semibold text-gray-500">Mot de passe</th>
              <th scope="col" class="text-right py-3 px-4 font-semibold text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in paginatedUsers" :key="user.id" class="border-b border-gray-50 hover:bg-gray-50">
              <td class="py-3 px-4 font-medium">{{ user.name }}</td>
              <td class="py-3 px-4 text-gray-500">{{ user.employeeId }}</td>
              <td class="py-3 px-4 text-gray-500">{{ user.nationalId }}</td>
              <td class="py-3 px-4"><div class="flex flex-wrap gap-1"><span v-for="role in user.roles" :key="role" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-700">{{ roleLabel(role) }}</span></div></td>
              <td class="py-3 px-4"><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="user.active ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">{{ user.active ? 'Actif' : 'Inactif' }}</span></td>
              <td class="py-3 px-4"><span v-if="user.mustChangePassword" class="text-xs text-amber-600 font-medium">À changer</span><span v-else class="text-xs text-gray-400">Défini</span></td>
              <td class="py-3 px-4 text-right space-x-2">
                <button @click="toggleStatus(user.id)" class="text-gray-400 hover:text-amber-600 transition" :title="user.active ? 'Désactiver' : 'Activer'">
                  <svg v-if="user.active" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg>
                  <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                </button>
                <button @click="deleteUser(user.id)" class="text-gray-400 hover:text-red-600 transition" title="Supprimer">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination Footer -->
        <div v-if="totalPages > 1" class="px-6 py-3.5 bg-gray-50 border-t flex flex-col sm:flex-row justify-between items-center gap-3 text-xs text-gray-500 font-medium">
          <span>Affichage de {{ (currentPage - 1) * pageSize + 1 }} à {{ Math.min(currentPage * pageSize, filteredUsers.length) }} sur {{ filteredUsers.length }} utilisateur(s)</span>
          <div class="flex gap-1">
            <button :disabled="currentPage === 1" @click="currentPage--" class="px-2.5 py-1.5 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 font-semibold text-gray-700">Précédent</button>
            <span class="px-3 py-1.5 bg-gray-100 rounded-lg flex items-center font-semibold text-gray-800">Page {{ currentPage }} sur {{ totalPages }}</span>
            <button :disabled="currentPage === totalPages" @click="currentPage++" class="px-2.5 py-1.5 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 font-semibold text-gray-700">Suivant</button>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-16 text-gray-400">Aucun utilisateur trouvé</div>
    </div>

    <!-- Create User Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouvel Utilisateur</h2>
        <form @submit.prevent="createUser" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom complet</label><input v-model="form.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Matricule</label><input v-model="form.employeeId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">CIN</label><input v-model="form.nationalId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Mot de passe temporaire</label><input :value="form.password" @input="form.password = $event.target.value; passwordManuallyChanged = true" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none bg-gray-50" /><p class="text-xs text-gray-400 mt-1">Par defaut, le mot de passe est le CIN</p></div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Roles</label>
            <div class="flex flex-wrap gap-2 mt-1">
              <label v-for="role in availableRoles" :key="role.value" class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-gray-200 cursor-pointer hover:bg-gray-50 transition" :class="{ 'border-emerald-300 bg-emerald-50': form.roles.includes(role.value) }">
                <input type="checkbox" :value="role.value" v-model="form.roles" class="rounded text-emerald-600" />
                <span class="text-sm">{{ role.label }}</span>
              </label>
            </div>
          </div>
          <div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCreateModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Créer</button></div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { usersApi } from '@/api/endpoints'

const users = ref([])
const loading = ref(true)
const creating = ref(false)
const showCreateModal = ref(false)
const error = ref('')
const passwordManuallyChanged = ref(false)
const form = ref({ name: '', employeeId: '', nationalId: '', password: '', roles: [] })

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(15)
const sortBy = ref('name')
const sortOrder = ref('asc')

// Auto-fill password with CIN value when CIN changes
watch(() => form.value.nationalId, (newCin) => {
  if (newCin && !passwordManuallyChanged.value) {
    form.value.password = newCin
  }
})

const filteredUsers = computed(() => {
  let list = users.value
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase().trim()
    list = list.filter(u => {
      const name = (u.name || '').toLowerCase()
      const mat = (u.employeeId || '').toLowerCase()
      const cin = (u.nationalId || '').toLowerCase()
      return name.includes(q) || mat.includes(q) || cin.includes(q)
    })
  }
  return list
})

const sortedUsers = computed(() => {
  const result = [...filteredUsers.value]
  const field = sortBy.value
  const order = sortOrder.value === 'asc' ? 1 : -1
  
  result.sort((a, b) => {
    let valA = '', valB = ''
    if (field === 'name') {
      valA = (a.name || '').toLowerCase()
      valB = (b.name || '').toLowerCase()
    } else if (field === 'employeeId') {
      valA = (a.employeeId || '').toLowerCase()
      valB = (b.employeeId || '').toLowerCase()
    } else if (field === 'nationalId') {
      valA = (a.nationalId || '').toLowerCase()
      valB = (b.nationalId || '').toLowerCase()
    } else if (field === 'active') {
      valA = a.active ? '1' : '0'
      valB = b.active ? '1' : '0'
    }
    
    if (valA < valB) return -1 * order
    if (valA > valB) return 1 * order
    return 0
  })
  
  return result
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedUsers.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredUsers.value.length / pageSize.value) || 1
})

const handleSort = (field) => {
  if (sortBy.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = field
    sortOrder.value = 'asc'
  }
}

watch([searchQuery, pageSize], () => {
  currentPage.value = 1
})

const availableRoles = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'RH', label: 'RH' },
  { value: 'RESP_QUALITE', label: 'Resp. Qualite' },
  { value: 'RESP_HSE', label: 'Resp. HSE' },
  { value: 'AGENT_QUALITE', label: 'Agent Qualite' },
   { value: 'CHEF_EQUIPE', label: 'Chef d\'Equipe' },
  { value: 'SUPERVISEUR', label: 'Superviseur' },
  { value: 'DEPT_PROCESS', label: 'Dept Process' },
{ value: 'DEPT_MAINTENANCE', label: 'Dept Maintenance' },
{ value: 'DEPT_DGT_MANUFACTURING', label: 'Dept DGT Manufacturing' },
]

const roleLabel = (r) => ({ ADMIN: 'Admin', RH: 'RH', RESP_QUALITE: 'Resp. Qualite', RESP_HSE: 'Resp. HSE', AGENT_QUALITE: 'Agent Qualité', CHEF_EQUIPE: 'Chef d\'Équipe', SUPERVISEUR: 'Superviseur' })[r] || r

const fetchUsers = async () => { loading.value = true; try { users.value = (await usersApi.getAll()).data } catch (e) { console.error(e) } finally { loading.value = false } }

const createUser = async () => {
  creating.value = true; error.value = ''
  // Ensure password defaults to CIN if not manually changed
  if (!form.value.password || form.value.password === form.value.nationalId) {
    form.value.password = form.value.nationalId
  }
  try { await usersApi.create(form.value); showCreateModal.value = false; form.value = { name: '', employeeId: '', nationalId: '', password: '', roles: [] }; passwordManuallyChanged.value = false; fetchUsers() }
  catch (e) { error.value = e.response?.data?.message || 'Erreur lors de la creation' } finally { creating.value = false }
}

const toggleStatus = async (id) => { try { await usersApi.toggleStatus(id); fetchUsers() } catch (e) { console.error(e) } }
const deleteUser = async (id) => { if (!confirm('Supprimer cet utilisateur ?')) return; try { await usersApi.deleteUser(id); fetchUsers() } catch (e) { console.error(e) } }

const handleResetDatabase = async () => {
  if (!confirm('ATTENTION: Voulez-vous vraiment vider toutes les données d\'opérateurs, formations et évaluations de test pour remettre la base à 0 ?')) return
  loading.value = true
  try {
    const res = await usersApi.resetDatabase()
    alert(res.data || 'Base de données réinitialisée avec succès !')
    fetchUsers()
  } catch (e) {
    alert('Erreur lors de la réinitialisation: ' + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

onMounted(fetchUsers)
</script>