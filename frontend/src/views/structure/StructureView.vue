<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div><h1 class="text-2xl font-bold text-gray-900">Structure</h1><p class="text-gray-500 mt-1">Organisation des projets, zones et postes de travail</p></div>
      <button @click="showCreateProject = true" class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>Nouveau Projet
      </button>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>

    <div v-else>
      <!-- Projects Accordion -->
      <div v-for="project in projects" :key="project.id" class="bg-white rounded-xl shadow-sm border border-gray-200 mb-4">
        <div class="p-4 flex items-center justify-between cursor-pointer hover:bg-gray-50 transition" @click="toggleProject(project.id)">
          <div class="flex items-center gap-3">
            <svg class="w-5 h-5 text-gray-400 transition-transform" :class="{ 'rotate-90': expandedProjects.has(project.id) }" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
            <div><h2 class="font-semibold text-gray-900">{{ project.name }}</h2><p class="text-xs text-gray-500">{{ project.zones?.length || 0 }} zones, {{ project.members?.length || 0 }} membres · Cree par {{ project.createdByName || 'Systeme' }}</p></div>
          </div>
          <div class="flex items-center gap-2">
            <button @click.stop="showAddZone(project.id)" class="text-sm text-emerald-600 hover:underline">+ Zone</button>
            <button @click.stop="deleteProject(project.id)" class="text-gray-400 hover:text-red-600 transition"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg></button>
          </div>
        </div>
        <div v-if="expandedProjects.has(project.id)" class="border-t border-gray-100">
          <!-- Members -->
          <div v-if="project.members?.length" class="p-4 border-b border-gray-100">
            <h3 class="text-sm font-medium text-gray-700 mb-2">Membres du projet</h3>
            <div class="flex flex-wrap gap-2">
              <span v-for="m in project.members" :key="m.id" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 text-sm text-slate-700">
                {{ m.employeeName || m.employeeId }}
                <span class="text-xs text-slate-400">({{ m.projectRole }})</span>
              </span>
            </div>
          </div>
          <!-- Zones -->
          <div v-for="zone in project.zones" :key="zone.id" class="border-b border-gray-100 last:border-b-0">
            <div class="p-4 pl-8 flex items-center justify-between cursor-pointer hover:bg-gray-50 transition" @click="toggleZone(zone.id)">
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{ 'rotate-90': expandedZones.has(zone.id) }" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
                <span class="font-medium text-gray-800">{{ zone.name }}</span>
                <span class="text-xs text-gray-400">({{ zone.workstations?.length || 0 }} postes) · Cree par {{ zone.createdByName || 'Systeme' }}</span>
              </div>
              <button @click.stop="deleteZone(zone.id)" class="text-gray-400 hover:text-red-600 transition"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
            </div>
            <div v-if="expandedZones.has(zone.id)" class="pl-12 pr-4 pb-4">
              <div v-if="zone.workstations?.length" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                <div v-for="ws in zone.workstations" :key="ws.id" class="p-3 rounded-lg border border-gray-200 hover:border-emerald-300 transition">
                  <div class="flex items-center justify-between">
                    <div><p class="text-sm font-medium text-gray-900">{{ ws.name }}</p><p class="text-xs text-gray-500">{{ ws.type || 'Non defini' }} · Cree par {{ ws.createdByName || 'Systeme' }}</p></div>
                    <button @click="deleteWorkstation(ws.id)" class="text-gray-400 hover:text-red-600 transition"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
                  </div>
                  <div class="mt-2 grid grid-cols-3 gap-2 text-xs">
                    <div><span class="text-gray-400">Cadence:</span> <span class="font-medium">{{ ws.targetCadence || '-' }}</span></div>
                    <div><span class="text-gray-400">Versatilite:</span> <span class="font-medium">{{ ws.versatilityTarget || '-' }}</span></div>
                    <div><span class="text-gray-400">ILU:</span> <span class="font-medium">{{ ws.targetIluLevel || '-' }}</span></div>
                  </div>
                </div>
              </div>
              <div v-else class="text-sm text-gray-400 py-2">Aucun poste de travail</div>
              <button @click="showAddWorkstation(zone.id, project.id)" class="mt-3 text-sm text-emerald-600 hover:underline">+ Ajouter un poste</button>
            </div>
          </div>
        </div>
      </div>
      <div v-if="projects.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">Aucun projet configure</div>
    </div>

    <!-- Create Project Modal -->
    <div v-if="showCreateProject" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateProject = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouveau Projet</h2>
        <form @submit.prevent="createProject" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom du projet</label><input v-model="projectForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCreateProject = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Creer</button></div>
        </form>
      </div>
    </div>

    <!-- Add Zone Modal -->
    <div v-if="showZoneModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showZoneModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouvelle Zone</h2>
        <form @submit.prevent="createZone" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom de la zone</label><input v-model="zoneForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showZoneModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg">Ajouter</button></div>
        </form>
      </div>
    </div>

    <!-- Add Workstation Modal -->
    <div v-if="showWorkstationModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showWorkstationModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouveau Poste de Travail</h2>
        <div v-if="wsError" class="text-sm text-red-600 mb-3">{{ wsError }}</div>
        <form @submit.prevent="createWorkstation" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="wsForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Type</label><input v-model="wsForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Cadence cible</label><input v-model.number="wsForm.targetCadence" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Versatilite</label><input v-model.number="wsForm.versatilityTarget" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Niveau ILU cible</label>
              <select v-model="wsForm.targetIluLevel" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
                <option value="I">I</option>
                <option value="L">L</option>
                <option value="U">U</option>
              </select>
            </div>
          </div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showWorkstationModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg">Ajouter</button></div>
        </form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { structureApi } from '@/api/endpoints'

const projects = ref([])
const loading = ref(true)
const creating = ref(false)
const expandedProjects = ref(new Set())
const expandedZones = ref(new Set())
const showCreateProject = ref(false)
const showZoneModal = ref(false)
const showWorkstationModal = ref(false)
const projectForm = ref({ name: '' })
const zoneForm = ref({ name: '', projectId: null })
const wsForm = ref({ name: '', type: '', targetCadence: null, versatilityTarget: null, targetIluLevel: 'I', zoneId: null })
const wsError = ref('')

const toggleProject = (id) => { if (expandedProjects.value.has(id)) expandedProjects.value.delete(id); else expandedProjects.value.add(id); expandedProjects.value = new Set(expandedProjects.value) }
const toggleZone = (id) => { if (expandedZones.value.has(id)) expandedZones.value.delete(id); else expandedZones.value.add(id); expandedZones.value = new Set(expandedZones.value) }

const fetchProjects = async () => { loading.value = true; try { projects.value = (await structureApi.getAll()).data } catch (e) { console.error(e) } finally { loading.value = false } }

const createProject = async () => { creating.value = true; try { await structureApi.createProject({ name: projectForm.value.name }); showCreateProject.value = false; projectForm.value = { name: '' }; fetchProjects() } catch (e) { console.error(e) } finally { creating.value = false } }
const deleteProject = async (id) => { if (!confirm('Supprimer ce projet ?')) return; try { await structureApi.deleteProject(id); fetchProjects() } catch (e) { console.error(e) } }

const showAddZone = (projectId) => { zoneForm.value = { name: '', projectId }; showZoneModal.value = true }
const createZone = async () => { creating.value = true; try { await structureApi.createZone(zoneForm.value.projectId, { name: zoneForm.value.name }); showZoneModal.value = false; fetchProjects() } catch (e) { console.error(e) } finally { creating.value = false } }
const deleteZone = async (id) => { if (!confirm('Supprimer cette zone ?')) return; try { await structureApi.deleteZone(id); fetchProjects() } catch (e) { console.error(e) } }

const showAddWorkstation = (zoneId, projectId) => { wsForm.value = { name: '', type: '', targetCadence: null, versatilityTarget: null, targetIluLevel: 'I', zoneId }; wsError.value = ''; showWorkstationModal.value = true }
const createWorkstation = async () => {
  creating.value = true
  wsError.value = ''
  try {
    await structureApi.createWorkstation(wsForm.value)
    showWorkstationModal.value = false
    fetchProjects()
  } catch (e) {
    console.error(e)
    wsError.value = e.response?.data?.message || e.message || 'Erreur lors de la creation du poste.'
  } finally {
    creating.value = false
  }
}
const deleteWorkstation = async (id) => { if (!confirm('Supprimer ce poste ?')) return; try { await structureApi.deleteWorkstation(id); fetchProjects() } catch (e) { console.error(e) } }

onMounted(fetchProjects)
</script>