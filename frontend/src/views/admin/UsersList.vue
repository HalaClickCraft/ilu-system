<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div><h1 class="text-2xl font-bold text-gray-900">Gestion des Utilisateurs</h1><p class="text-gray-500 mt-1">Comptes et droits d'acces au systeme</p></div>
      <button @click="showCreateModal = true" class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>Nouvel Utilisateur
      </button>
    </div>
    <div class="bg-white rounded-xl shadow-sm border border-gray-200">
      <div v-if="loading" class="flex items-center justify-center py-16"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="users.length" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Nom</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Matricule</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">CIN</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Roles</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Statut</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Mot de passe</th>
              <th class="text-right py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id" class="border-b border-gray-50 hover:bg-gray-50">
              <td class="py-3 px-4 font-medium">{{ user.name }}</td>
              <td class="py-3 px-4 text-gray-500">{{ user.employeeId }}</td>
              <td class="py-3 px-4 text-gray-500">{{ user.nationalId }}</td>
              <td class="py-3 px-4"><div class="flex flex-wrap gap-1"><span v-for="role in user.roles" :key="role" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-700">{{ roleLabel(role) }}</span></div></td>
              <td class="py-3 px-4"><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="user.active ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">{{ user.active ? 'Actif' : 'Inactif' }}</span></td>
              <td class="py-3 px-4"><span v-if="user.mustChangePassword" class="text-xs text-amber-600 font-medium">A changer</span><span v-else class="text-xs text-gray-400">Defini</span></td>
              <td class="py-3 px-4 text-right space-x-2">
                <button @click="toggleStatus(user.id)" class="text-gray-400 hover:text-amber-600 transition" :title="user.active ? 'Desactiver' : 'Activer'">
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
      </div>
      <div v-else class="text-center py-16 text-gray-400">Aucun utilisateur</div>
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
import { ref, watch, onMounted } from 'vue'
import { usersApi } from '@/api/endpoints'

const users = ref([])
const loading = ref(true)
const creating = ref(false)
const showCreateModal = ref(false)
const error = ref('')
const passwordManuallyChanged = ref(false)
const form = ref({ name: '', employeeId: '', nationalId: '', password: '', roles: [] })

// Auto-fill password with CIN value when CIN changes
watch(() => form.value.nationalId, (newCin) => {
  if (newCin && !passwordManuallyChanged.value) {
    form.value.password = newCin
  }
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

onMounted(fetchUsers)
</script>