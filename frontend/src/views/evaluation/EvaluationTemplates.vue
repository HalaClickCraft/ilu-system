<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Gestion des Templates d'Évaluation</h1>
        <p class="text-sm text-gray-500 mt-1">
          Créer et gérer les templates de questions par poste
        </p>
      </div>
      <button
        v-if="canManage"
        @click="openCreateModal"
        class="bg-sky-600 text-white px-4 py-2 rounded-lg hover:bg-sky-700 flex items-center gap-2"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          ></path>
        </svg>
        Nouveau Template
      </button>
    </div>

    <!-- Filter & Search Bar -->
    <div class="bg-white p-4 rounded-xl border border-gray-200 space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <!-- Type Tabs -->
        <div class="flex gap-2 border-b border-gray-200">
          <button
            v-for="t in typeTabs"
            :key="t.key"
            @click="activeType = t.key"
            class="px-4 py-2 text-sm font-medium border-b-2 transition"
            :class="
              activeType === t.key
                ? 'border-sky-600 text-sky-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            "
          >
            {{ t.label }} ({{ countByType(t.key) }})
          </button>
        </div>

        <!-- Filter Chips & Reset -->
        <div v-if="selectedProjectId || selectedZoneId || searchTemplateQuery" class="flex items-center gap-2 text-xs">
          <span class="text-gray-400 font-medium">Filtres actifs:</span>
          <span v-if="selectedProjectId" class="bg-sky-50 text-sky-700 px-2 py-1 rounded-md border border-sky-200 flex items-center gap-1">
            Projet: {{ projects.find(p => p.id === Number(selectedProjectId))?.name }}
            <button @click="selectedProjectId = ''" class="hover:text-sky-900">✕</button>
          </span>
          <span v-if="selectedZoneId" class="bg-blue-50 text-blue-700 px-2 py-1 rounded-md border border-blue-200 flex items-center gap-1">
            Zone: {{ availableZones.find(z => z.id === Number(selectedZoneId))?.name }}
            <button @click="selectedZoneId = ''" class="hover:text-blue-900">✕</button>
          </span>
          <button @click="resetFilters" class="text-xs text-gray-500 hover:text-gray-700 underline ml-2">Réinitialiser tout</button>
        </div>
      </div>

      <!-- Project & Zone Selectors -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2 border-t border-gray-100">
        <div>
          <label class="block text-xs font-semibold text-gray-500 mb-1">Filtrer par Projet</label>
          <select v-model="selectedProjectId" @change="selectedZoneId = ''" class="w-full border border-gray-300 rounded-lg px-3 py-1.5 text-xs focus:ring-2 focus:ring-sky-500">
            <option value="">Tous les projets</option>
            <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-gray-500 mb-1">Filtrer par Zone</label>
          <select v-model="selectedZoneId" :disabled="!selectedProjectId" class="w-full border border-gray-300 rounded-lg px-3 py-1.5 text-xs focus:ring-2 focus:ring-sky-500 disabled:bg-gray-100">
            <option value="">Toutes les zones</option>
            <option v-for="z in availableZones" :key="z.id" :value="z.id">{{ z.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-gray-500 mb-1">Recherche par Nom</label>
          <input v-model="searchTemplateQuery" type="text" placeholder="Chercher un template..." class="w-full border border-gray-300 rounded-lg px-3 py-1.5 text-xs focus:ring-2 focus:ring-sky-500" />
        </div>
      </div>
    </div>

    <!-- Templates list -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="tpl in filteredTemplates"
        :key="tpl.id"
        @click="selectTemplate(tpl)"
        class="bg-white rounded-xl border border-gray-200 p-4 cursor-pointer hover:shadow-md hover:border-sky-300 transition"
      >
        <div class="flex items-start justify-between">
          <div>
            <h3 class="font-semibold text-gray-900">{{ tpl.name }}</h3>
            <p v-if="tpl.description" class="text-sm text-gray-500 mt-1">{{ tpl.description }}</p>
          </div>
          <span
            :class="statusClass(tpl.status)"
            class="text-xs font-medium px-2 py-1 rounded-full"
            >{{ statusLabel(tpl.status) }}</span
          >
        </div>
        <div class="mt-3 flex items-center gap-2 text-xs text-gray-500 flex-wrap">
          <span class="bg-gray-100 px-2 py-0.5 rounded">{{ typeLabel(tpl.type) }}</span>
          <span v-if="tpl.workstations && tpl.workstations.length > 1" class="bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded font-semibold border border-indigo-200">
            🔗 {{ tpl.workstations.length }} postes liés
          </span>
          <span v-else-if="tpl.workstationName" class="bg-blue-50 text-blue-700 px-2 py-0.5 rounded">
            {{ tpl.workstationName }}
          </span>
          <span v-if="tpl.targetNiveau" class="bg-amber-50 text-amber-700 px-2 py-0.5 rounded">
            Niveau {{ tpl.targetNiveau }}
          </span>
          <span>{{ tpl.validatedQuestionCount || 0 }} questions validées</span>
        </div>
      </div>
      <div
        v-if="filteredTemplates.length === 0"
        class="col-span-full text-center py-12 text-gray-400"
      >
        Aucun template trouve
      </div>
    </div>

    <!-- Template Detail Panel -->
    <div v-if="selectedTemplate" class="fixed inset-0 bg-black/40 z-40" @click="closePanel"></div>
    <div
      v-if="selectedTemplate"
      class="fixed right-0 top-0 h-full w-full max-w-3xl bg-white shadow-2xl z-50 overflow-y-auto"
    >
      <div class="p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold">{{ selectedTemplate.name }}</h2>
          <div class="flex items-center gap-2 flex-wrap">
            <button @click="exportQuestionsToExcel" class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-medium transition shadow-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
              Exporter (Excel)
            </button>
            <button v-if="canManage" @click="openAssignModal(selectedTemplate)" class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-medium transition shadow-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>
              Affecter Postes
            </button>
            <button v-if="canManage" @click="openImportQuestionsModal" class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-xs font-medium transition shadow-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
              Importer (Excel)
            </button>
            <button v-if="canManage" @click="clearQuestions" class="inline-flex items-center gap-1.5 px-2.5 py-1.5 bg-amber-500 hover:bg-amber-600 text-white rounded-lg text-xs font-medium transition shadow-sm" title="Vider toutes les questions pour réimporter proprement">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
              Vider questions
            </button>
            <button v-if="canManage" @click="deleteThisTemplate" class="inline-flex items-center gap-1.5 px-2.5 py-1.5 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-medium transition shadow-sm" title="Supprimer définitivement ce template">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              Supprimer template
            </button>
            <button @click="closePanel" class="p-2 hover:bg-gray-100 rounded-lg ml-1">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                ></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- Template info -->
        <!-- Template info -->
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 mb-6 p-4 bg-gray-50 rounded-xl border border-gray-200">
          <div>
            <span class="text-xs text-gray-500 block mb-0.5">Type</span>
            <p class="font-medium text-gray-900">{{ typeLabel(selectedTemplate.type) }}</p>
          </div>
          <div>
            <span class="text-xs text-gray-500 block mb-0.5">Statut</span>
            <p>
              <span
                :class="statusClass(selectedTemplate.status)"
                class="text-xs font-semibold px-2 py-0.5 rounded-full"
                >{{ statusLabel(selectedTemplate.status) }}</span
              >
            </p>
          </div>
          <div>
            <span class="text-xs text-gray-500 block mb-0.5">Questions validées</span>
            <p class="font-medium text-gray-900">
              {{
                templateDetail?.sections?.reduce((sum, s) => sum + (s.questions?.length || 0), 0) ||
                selectedTemplate.validatedQuestionCount ||
                0
              }}
            </p>
          </div>

          <!-- Workstations display -->
          <div class="sm:col-span-2 md:col-span-1" v-if="selectedTemplate.type === 'POSTE_PRODUCTION'">
            <span class="text-xs text-gray-500 block mb-0.5">Poste(s) affecté(s)</span>
            <div v-if="(templateDetail?.workstations && templateDetail.workstations.length > 0) || (selectedTemplate.workstations && selectedTemplate.workstations.length > 0)" class="flex flex-wrap gap-1">
              <span 
                v-for="ws in (templateDetail?.workstations?.length ? templateDetail.workstations : selectedTemplate.workstations || [])" 
                :key="ws.id" 
                class="inline-flex items-center gap-1 text-[11px] font-semibold bg-indigo-50 text-indigo-800 border border-indigo-200 px-2 py-0.5 rounded"
              >
                <span v-if="ws.projectName" class="text-indigo-500 font-normal">{{ ws.projectName }} &rsaquo;</span>
                <span>{{ ws.name }}</span>
              </span>
            </div>
            <div v-else-if="selectedTemplate.workstationName" class="font-medium text-gray-800">
              {{ selectedTemplate.workstationName }}
            </div>
            <div v-else class="text-xs text-amber-600 font-medium italic">
              Aucun poste affecté
            </div>
          </div>
          <div class="sm:col-span-2 md:col-span-1" v-else>
            <span class="text-xs text-gray-500 block mb-0.5">Portée</span>
            <span class="inline-flex items-center gap-1 text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 px-2 py-0.5 rounded">
              🌐 Global Usine (Tous postes)
            </span>
          </div>
        </div>

        <!-- Add Section -->
        <div
          v-if="canManage && selectedTemplate.status === 'DRAFT'"
          class="mb-4 p-4 bg-sky-50 rounded-lg"
        >
          <h4 class="text-sm font-semibold text-sky-800 mb-2">Ajouter une section</h4>
          <div class="flex gap-2">
            <input
              v-model="newSectionTitle"
              placeholder="Nom de la section..."
              class="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm"
              @keyup.enter="addSection"
            />
            <button
              @click="addSection"
              class="bg-sky-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-sky-700"
            >
              + Section
            </button>
          </div>
        </div>

        <!-- OPmobility Style Validation Sheet Preview -->
        <div class="bg-white border-4 border-blue-900 rounded-xl p-6 shadow-sm max-w-5xl mx-auto my-4 font-sans text-gray-900 relative">
          
          <!-- Document Header -->
          <div class="flex items-start justify-between pb-4 border-b border-gray-300">
            <!-- Logo -->
            <div class="flex flex-col">
              <div class="flex items-center gap-1">
                <span class="text-2xl font-black text-blue-950 italic tracking-tighter">OP</span>
                <span class="text-lg font-bold text-gray-700">mobility</span>
              </div>
              <span class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">ILU System</span>
            </div>

            <!-- Document Title -->
            <div class="text-center flex-1">
              <h2 class="text-xl font-extrabold text-blue-950 tracking-wider">VALIDATION AU POSTE (PREVIEW)</h2>
              <div class="text-xs text-gray-500 font-medium mt-0.5">{{ selectedTemplate.name }}</div>
            </div>

            <!-- Safety Symbol Placeholder -->
            <div class="flex items-center gap-2 pr-2">
              <div class="relative w-16 h-16 flex items-center justify-center border-2 border-red-600 rounded-full bg-red-50/20">
                <span class="absolute top-1 left-2 text-[10px] font-bold text-red-600">S</span>
                <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                  <polygon points="12,3 3,20 21,20" />
                </svg>
                <span class="absolute bottom-1 right-2 text-[10px] font-bold text-red-600">R</span>
              </div>
            </div>
          </div>

          <!-- Meta Info Lines -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 py-4 text-xs border-b border-gray-300">
            <div class="flex items-center gap-1">
              <span class="font-bold text-gray-700">Nom :</span>
              <span class="border-b border-dashed border-gray-400 flex-1 h-5 bg-gray-50/30"></span>
            </div>
            <div class="flex items-center gap-1">
              <span class="font-bold text-gray-700">Date d'embauche / évaluation :</span>
              <span class="border-b border-dashed border-gray-400 flex-1 h-5 bg-gray-50/30"></span>
            </div>
          </div>

          <!-- Banner header -->
          <div class="w-full bg-[#003F15] text-white text-center py-2 mt-4 font-bold text-xs uppercase tracking-widest rounded">
            {{ selectedTemplate.type === 'GENERIC_COMMON' ? 'PARTIE COMMUNE / COMMENCER' : selectedTemplate.type === 'ANIMATION' ? 'PARTIE ANIMATION' : 'PARTIE PRODUCTION' }}
          </div>

          <div class="text-right text-[10px] font-semibold text-gray-500 my-1.5">
            <span>1 : Bonne réponse</span> &nbsp;|&nbsp; <span>0 : Mauvaise réponse</span>
          </div>

          <!-- Table wrapper -->
          <div class="overflow-x-auto mt-2">
            <table class="w-full border-collapse border border-gray-400 text-xs">
              <thead>
                <tr class="bg-gray-100 text-gray-800 border-b border-gray-400 font-bold">
                  <th class="border border-gray-400 px-3 py-2 text-left w-[40%]">Question</th>
                  <th class="border border-gray-400 px-3 py-2 text-left w-[22%]">Réponse espérée</th>
                  <th class="border border-gray-400 px-2 py-2 text-center w-[12%]">Validateur</th>
                  <th class="border border-gray-400 px-2 py-2 text-center w-[5%]">0</th>
                  <th class="border border-gray-400 px-2 py-2 text-center w-[5%]">1</th>
                  <th class="border border-gray-400 px-3 py-2 text-left w-[16%]">Question Complémentaire</th>
                  <th v-if="canManage" class="border border-gray-400 px-2 py-2 text-center w-[8%]">Actions</th>
                </tr>
              </thead>
              <tbody v-for="section in templateDetail?.sections" :key="section.id">
                  <!-- Section Header Row -->
                  <tr class="bg-slate-200/60 font-bold border-b border-gray-450">
                    <td :colspan="canManage ? 7 : 6" class="border border-gray-400 px-3 py-1.5 text-gray-800 uppercase tracking-wider text-[11px] bg-slate-200/50">
                      {{ section.title }}
                    </td>
                  </tr>

                  <!-- Questions Rows -->
                  <tr v-for="q in section.questions" :key="q.id" class="hover:bg-gray-50/40">
                    <!-- 1. Question details -->
                    <td class="border border-gray-400 px-3 py-2">
                      <!-- Editor View -->
                      <div v-if="editingQuestionId === q.id" class="space-y-2 py-2">
                        <input v-model="editForm.questionText" class="w-full border border-gray-300 rounded px-2 py-1 text-xs" placeholder="Question..." />
                        <input v-model="editForm.expectedAnswer" class="w-full border border-gray-300 rounded px-2 py-1 text-xs" placeholder="Réponse attendue..." />
                        <textarea v-model="editForm.complementaryQuestions" rows="2" class="w-full border border-gray-300 rounded px-2 py-1 text-xs" placeholder="Question complémentaire..."></textarea>
                        <div class="space-y-1">
                          <label class="block text-[10px] font-semibold text-gray-500">Image (optionnelle)</label>
                          <div class="flex items-center gap-2">
                            <input type="file" accept="image/*" @change="handleEditQuestionImageUpload($event)" class="text-[10px] text-gray-500" />
                            <button v-if="editForm.imageUrl" type="button" @click="editForm.imageUrl = ''" class="text-[10px] text-red-600 hover:underline">Supprimer l'image</button>
                          </div>
                          <img v-if="editForm.imageUrl" :src="editForm.imageUrl" class="mt-1 h-12 w-auto object-contain rounded border" />
                        </div>
                        <div class="flex gap-2 pt-1">
                          <button @click="saveEditQuestion" class="bg-blue-600 text-white px-2.5 py-1 rounded text-[11px] hover:bg-blue-700">Enregistrer</button>
                          <button @click="cancelEdit" class="bg-gray-200 text-gray-700 px-2.5 py-1 rounded text-[11px] hover:bg-gray-300">Annuler</button>
                        </div>
                      </div>

                      <!-- Preview View -->
                      <div v-else class="space-y-1">
                        <div class="flex items-start gap-1.5">
                          <span class="font-bold text-gray-500 text-[11px]">{{ q.questionNumber }}.</span>
                          <div class="space-y-1">
                            <p class="text-gray-900 font-medium leading-relaxed">{{ q.questionText }}</p>
                            <span v-if="q.status === 'PENDING'" class="text-[9px] bg-yellow-100 text-yellow-800 px-1 py-0.2 rounded font-semibold uppercase tracking-wider">En attente</span>
                            <span v-if="q.status === 'REJECTED'" class="text-[9px] bg-red-100 text-red-800 px-1 py-0.2 rounded font-semibold uppercase tracking-wider">Rejetée</span>
                            <span v-if="q.status === 'VALIDATED'" class="text-[9px] bg-green-100 text-green-800 px-1 py-0.2 rounded font-semibold uppercase tracking-wider">Validée</span>
                          </div>
                        </div>
                        <!-- Image Container Design -->
                        <div v-if="q.imageUrl" class="relative group mt-2 max-w-[180px] bg-slate-50 border border-slate-200 rounded p-1 shadow-sm hover:shadow-md transition">
                          <img :src="q.imageUrl" class="w-full max-h-24 rounded object-contain bg-white cursor-zoom-in" @click="activeZoomImageUrl = q.imageUrl" />
                          <div class="absolute bottom-1 right-1 bg-black/60 text-[9px] text-white px-1.5 py-0.2 rounded font-medium opacity-0 group-hover:opacity-100 transition">
                            Zoom
                          </div>
                        </div>
                      </div>
                    </td>

                    <!-- 2. Expected Answer -->
                    <td class="border border-gray-400 px-3 py-2 text-gray-600 bg-gray-50/20 italic text-[11px]">
                      <div v-if="editingQuestionId !== q.id">
                        {{ q.expectedAnswer || '—' }}
                      </div>
                    </td>

                    <!-- 3. Validator Role -->
                    <td class="border border-gray-400 px-2 py-2 text-center text-[10px] font-semibold text-blue-800 bg-blue-50/10">
                      à valider par<br/>{{ formatRoleLabel(q.validatorRole) }}
                    </td>

                    <!-- 4. Score 0 box -->
                    <td class="border border-gray-400 p-1 text-center bg-gray-50/10">
                      <div class="w-6 h-6 border border-gray-300 rounded mx-auto bg-white flex items-center justify-center text-gray-300">0</div>
                    </td>

                    <!-- 5. Score 1 box -->
                    <td class="border border-gray-400 p-1 text-center bg-gray-50/10">
                      <div class="w-6 h-6 border border-gray-300 rounded mx-auto bg-white flex items-center justify-center text-gray-300">1</div>
                    </td>

                    <!-- 6. Question Complémentaire -->
                    <td class="border border-gray-400 px-3 py-2 text-gray-700 bg-purple-50/10 italic text-[11px]">
                      <div v-if="editingQuestionId !== q.id">
                        {{ q.complementaryQuestions || '—' }}
                      </div>
                    </td>

                    <!-- 7. Actions (Management) -->
                    <td v-if="canManage" class="border border-gray-400 p-1.5 text-center">
                      <div v-if="editingQuestionId !== q.id" class="flex items-center justify-center gap-1">
                        <button @click.stop="startEditQuestion(q)" class="p-1 text-blue-600 hover:bg-blue-50 rounded" title="Modifier la question (texte, image, etc.)">
                          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                        </button>
                        <button v-if="selectedTemplate.status === 'DRAFT' || authStore.isAdmin" @click.stop="deleteQuestion(q.id)" class="p-1 text-red-600 hover:bg-red-50 rounded" title="Supprimer la question">
                          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                  <!-- Inline row: Add question to this section -->
                  <tr v-if="canManage && selectedTemplate.status === 'DRAFT'">
                    <td :colspan="7" class="border border-gray-400 p-3 bg-gray-50/80">
                      <div class="max-w-2xl mx-auto space-y-2">
                        <h4 class="text-xs font-semibold text-gray-700">+ Ajouter une question à la section "{{ section.title }}"</h4>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
                          <input v-model="newQuestion[section.id].text" placeholder="Texte de la question..." class="border border-gray-300 rounded px-2 py-1 text-xs" />
                          <input v-model="newQuestion[section.id].expected" placeholder="Réponse espérée..." class="border border-gray-300 rounded px-2 py-1 text-xs" />
                        </div>
                        <textarea v-model="newQuestion[section.id].complementary" rows="2" placeholder="Question complémentaire (optionnel)..." class="w-full border border-gray-300 rounded px-2 py-1 text-xs"></textarea>
                        <div class="flex items-center justify-between flex-wrap gap-2 pt-1">
                          <div class="flex items-center gap-2">
                            <label class="text-[10px] font-semibold text-gray-500">Image :</label>
                            <input type="file" accept="image/*" @change="handleQuestionImageUpload($event, section.id)" class="text-[10px]" />
                            <button v-if="newQuestion[section.id]?.imageUrl" type="button" @click="newQuestion[section.id].imageUrl = ''" class="text-[10px] text-red-600 hover:underline">Supprimer l'image</button>
                          </div>
                          <button @click="addQuestionToSection(section.id)" class="bg-sky-600 hover:bg-sky-700 text-white px-3 py-1 rounded text-xs font-semibold">Ajouter la Question</button>
                        </div>
                        <img v-if="newQuestion[section.id]?.imageUrl" :src="newQuestion[section.id].imageUrl" class="mt-1 h-12 w-auto object-contain rounded border" />
                      </div>
                    </td>
                  </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Template summary -->
        <div
          v-if="selectedTemplate && selectedTemplate.status === 'DRAFT'"
          class="mt-6 p-4 bg-gray-50 rounded-lg border"
        >
          <h4 class="text-sm font-semibold text-gray-700 mb-3">Validation du template</h4>
          <div class="space-y-2">
            <div
              v-for="role in roleSummaryForTemplate"
              :key="role.role"
              class="flex items-center justify-between text-sm rounded-lg px-3 py-2 border bg-white"
            >
              <span class="font-medium text-gray-700">{{ role.label }}</span>
              <span class="flex items-center gap-2">
                <span class="text-green-600 font-semibold">{{ role.validated }} validees</span>
                <span v-if="role.pending" class="text-amber-600 font-semibold"
                  >{{ role.pending }} en attente</span
                >
              </span>
            </div>
          </div>
          <p v-if="templateValidationMessage" class="mt-3 text-sm text-amber-700 font-medium">
            {{ templateValidationMessage }}
          </p>
        </div>

        <!-- Template actions -->
        <div v-if="canManage || isResponsable" class="flex gap-2 mt-6 pt-4 border-t">
          <button
            v-if="isResponsable && selectedTemplate.status === 'DRAFT'"
            @click="validateThisTemplate"
            :disabled="!canValidateSelectedTemplate"
            class="px-4 py-2 rounded-lg text-sm transition"
            :class="
              canValidateSelectedTemplate
                ? 'bg-green-600 text-white hover:bg-green-700'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            "
          >
            Valider le Template
          </button>
        </div>
      </div>
    </div>

    <!-- Create Template Modal -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/40 z-40"
      @click="showCreateModal = false"
    ></div>
    <div v-if="showCreateModal" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6" @click.stop>
        <h2 class="text-lg font-bold mb-4">Nouveau Template</h2>
        <div class="space-y-3">
          <div>
            <label class="text-sm font-medium text-gray-700">Nom *</label>
            <input
              v-model="newTemplate.name"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1"
              placeholder="Ex: RAC SC"
            />
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Description</label>
            <textarea
              v-model="newTemplate.description"
              rows="2"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1"
            ></textarea>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Type *</label>
            <select
              v-model="newTemplate.type"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1"
            >
              <option value="GENERIC_COMMON">Partie Générique</option>
              <option value="POSTE_PRODUCTION">Poste - Production</option>
              <option value="ANIMATION">Animation (L vers U)</option>
            </select>
          </div>
          <!-- Info badge for GENERIC_COMMON -->
          <div v-if="newTemplate.type === 'GENERIC_COMMON'" class="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-800 flex items-start gap-2">
            <svg class="w-4 h-4 text-blue-600 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <div>
              <p class="font-bold">Template Global Transversal</p>
              <p class="text-blue-600 mt-0.5">Ce template est unique pour l'usine et s'applique automatiquement à tous les projets et tous les postes de travail.</p>
            </div>
          </div>

          <!-- Info badge for ANIMATION -->
          <div v-if="newTemplate.type === 'ANIMATION'" class="p-3 bg-purple-50 border border-purple-200 rounded-lg text-xs text-purple-800 flex items-start gap-2">
            <svg class="w-4 h-4 text-purple-600 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <div>
              <p class="font-bold">Template Global Animation (L ➡️ U)</p>
              <p class="text-purple-600 mt-0.5">Ce questionnaire d'animation s'applique globalement à tous les opérateurs passant du niveau L vers le niveau U.</p>
            </div>
          </div>

          <!-- Multi-workstations picker for POSTE_PRODUCTION -->
          <div v-if="newTemplate.type === 'POSTE_PRODUCTION'" class="space-y-3">
            <div class="border border-gray-200 rounded-xl p-3 bg-slate-50 space-y-3">
              <div class="flex items-center justify-between">
                <label class="text-xs font-bold text-gray-800 flex items-center gap-1.5">
                  <svg class="w-4 h-4 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/></svg>
                  Affectation aux postes de travail
                </label>
                <span class="text-[11px] font-bold text-indigo-700 bg-indigo-100 px-2 py-0.5 rounded-full border border-indigo-200">
                  {{ newTemplate.workstationIds.length }} poste(s) sélectionné(s)
                </span>
              </div>

              <!-- Filter controls: Project dropdown & Zone dropdown -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <div>
                  <label class="block text-[11px] font-semibold text-gray-600 mb-0.5">1. Filtrer par Projet</label>
                  <select v-model="createPickerProjectId" @change="createPickerZoneId = ''" class="w-full border border-gray-300 rounded-lg px-2.5 py-1.5 text-xs bg-white focus:ring-2 focus:ring-indigo-500">
                    <option value="">-- Choisir un Projet --</option>
                    <option v-for="p in createTemplateProjects" :key="p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-[11px] font-semibold text-gray-600 mb-0.5">2. Filtrer par Zone</label>
                  <select v-model="createPickerZoneId" :disabled="!createPickerProjectId" class="w-full border border-gray-300 rounded-lg px-2.5 py-1.5 text-xs bg-white focus:ring-2 focus:ring-indigo-500 disabled:bg-gray-100 disabled:text-gray-400">
                    <option value="">Toutes les zones du projet</option>
                    <option v-for="z in createPickerAvailableZones" :key="z.id" :value="z.id">{{ z.name }}</option>
                  </select>
                </div>
              </div>

              <!-- Search and bulk select/deselect in filtered view -->
              <div v-if="createPickerProjectId" class="flex items-center justify-between gap-2 pt-1 border-t border-gray-200">
                <input v-model="createPickerSearch" type="text" placeholder="Rechercher un poste dans ce projet..." class="flex-1 border border-gray-300 rounded-lg px-2.5 py-1 text-xs bg-white focus:ring-1 focus:ring-indigo-500" />
                <div class="flex items-center gap-1.5 shrink-0">
                  <button type="button" @click="selectAllInCreatePicker" class="text-[11px] font-medium text-indigo-600 hover:text-indigo-800 underline">Tout cocher</button>
                  <span class="text-gray-300">|</span>
                  <button type="button" @click="deselectAllInCreatePicker" class="text-[11px] font-medium text-gray-500 hover:text-gray-700 underline">Décocher</button>
                </div>
              </div>

              <!-- Workstations list for selected project/zone -->
              <div v-if="createPickerProjectId" class="max-h-44 overflow-y-auto space-y-1.5 border border-gray-200 rounded-lg p-2 bg-white">
                <div v-if="createPickerDisplayedWorkstations.length === 0" class="text-center py-4 text-xs text-gray-400">
                  Aucun poste trouvé pour cette sélection
                </div>
                <label
                  v-for="ws in createPickerDisplayedWorkstations"
                  :key="ws.id"
                  :class="newTemplate.workstationIds.includes(ws.id) ? 'bg-emerald-50 border-emerald-300 text-emerald-900 font-semibold' : 'bg-gray-50 border-gray-200 text-gray-700'"
                  class="flex items-center justify-between p-2 rounded-lg border hover:bg-slate-100 cursor-pointer transition text-xs"
                >
                  <div class="flex items-center gap-2">
                    <input type="checkbox" :value="ws.id" v-model="newTemplate.workstationIds" class="rounded text-emerald-600 focus:ring-emerald-500" />
                    <span>{{ ws.name }}</span>
                  </div>
                  <span class="text-[10px] text-gray-400 bg-white px-1.5 py-0.5 rounded border border-gray-200">{{ ws.zoneName || 'Zone' }}</span>
                </label>
              </div>

              <div v-else class="text-center py-4 text-xs text-gray-500 bg-white rounded-lg border border-dashed border-gray-200">
                👉 Sélectionnez d'abord un projet ci-dessus pour afficher et cocher ses postes de travail.
              </div>

              <!-- Selected Workstations Chips Tray (Cross-Project summary) -->
              <div v-if="newTemplate.workstationIds.length > 0" class="pt-2 border-t border-gray-200">
                <div class="flex items-center justify-between mb-1.5">
                  <span class="text-[11px] font-bold text-gray-700">Postes sélectionnés (Tous projets confondus) :</span>
                  <button type="button" @click="newTemplate.workstationIds = []" class="text-[10px] text-red-600 hover:text-red-800 underline">Vider tout 🗑️</button>
                </div>
                <div class="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto">
                  <span
                    v-for="wsId in newTemplate.workstationIds"
                    :key="wsId"
                    class="inline-flex items-center gap-1 text-[11px] bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full border border-emerald-300 shadow-xs"
                  >
                    <span class="font-medium">{{ getWorkstationInfo(wsId).projectName }} &rsaquo; {{ getWorkstationInfo(wsId).name }}</span>
                    <button type="button" @click="removeWsFromCreate(wsId)" class="hover:text-red-600 font-bold ml-0.5">✕</button>
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="flex gap-2 pt-2">
            <button
              @click="createTemplate"
              class="flex-1 bg-sky-600 text-white py-2 rounded-lg text-sm hover:bg-sky-700"
            >
              Créer
            </button>
            <button
              @click="showCreateModal = false"
              class="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg text-sm hover:bg-gray-300"
            >
              Annuler
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- Import Questions Modal -->
    <div v-if="showImportModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-[60]" @click.self="closeImportModal">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-xl mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-2">Importer des Questions (Excel)</h2>
        <p class="text-xs text-gray-500 mb-4">
          Téléversez un fichier Excel pour le template <strong class="text-gray-700">{{ selectedTemplate?.name }}</strong>. 
          Colonne requise : <strong class="text-gray-700">Texte de la Question</strong>. 
          Optionnel : <strong class="text-gray-700">Réponse Attendue, Rôle du Validateur, Questions Complémentaires</strong>.
        </p>

        <div class="space-y-4">
          <div class="border-2 border-dashed border-gray-200 rounded-lg p-6 text-center">
            <input type="file" accept=".xlsx, .xls" @change="handleFileChange" class="hidden" id="questions-excel-upload" />
            <label for="questions-excel-upload" class="cursor-pointer inline-flex flex-col items-center gap-2">
              <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-9 1V4a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
              <span class="text-sm font-medium text-teal-600 hover:text-teal-700">Choisir un fichier</span>
              <span class="text-xs text-gray-400 block mt-1" v-if="importFile">{{ importFile.name }}</span>
            </label>
          </div>

          <div v-if="parsedQuestions.length > 0" class="max-h-48 overflow-y-auto border border-gray-100 rounded-lg p-2 bg-gray-50">
            <p class="text-xs font-semibold text-gray-600 mb-2">Questions détectées ({{ parsedQuestions.length }}) :</p>
            <ul class="text-xs space-y-1 divide-y divide-gray-100">
              <li v-for="(q, i) in parsedQuestions" :key="i" class="py-1 flex justify-between gap-2">
                <span class="truncate">{{ q.questionText }}</span>
                <span class="text-gray-400 shrink-0">{{ q.validatorRole || 'CHEF_EQUIPE' }}</span>
              </li>
            </ul>
          </div>

          <div v-if="importError" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg max-h-32 overflow-y-auto">{{ importError }}</div>
          <div v-if="importSuccess" class="bg-emerald-50 text-emerald-700 text-sm p-3 rounded-lg">{{ importSuccess }}</div>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-gray-100 mt-6">
          <button type="button" @click="closeImportModal" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Fermer</button>
          <button type="button" @click="submitImport" :disabled="parsedQuestions.length === 0 || importing" class="px-4 py-2 bg-teal-600 text-white text-sm rounded-lg hover:bg-teal-700 disabled:opacity-50">
            {{ importing ? 'Importation...' : 'Importer' }}
          </button>
        </div>
      </div>
    </div>
    <!-- Assign Workstations Modal (Multi-Postes / Shared Template) -->
    <div v-if="showAssignModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-[60]" @click.self="showAssignModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl mx-4 p-6 flex flex-col max-h-[85vh]">
        <div class="flex items-center justify-between border-b pb-3 mb-4">
          <div>
            <h2 class="text-lg font-bold text-gray-900">Affecter le Template à plusieurs Postes</h2>
            <p class="text-xs text-gray-500">Sélectionnez les postes de travail du même projet ou d'autres projets</p>
          </div>
          <button @click="showAssignModal = false" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>

        <div class="space-y-3 overflow-y-auto flex-1 pr-1">
          <div class="flex items-center justify-between bg-sky-50 border border-sky-200 rounded-lg p-2.5">
            <div>
              <span class="text-[11px] text-sky-600 font-semibold uppercase tracking-wider block">Template sélectionné</span>
              <span class="text-xs font-bold text-sky-900">{{ assignTargetTemplate?.name }}</span>
            </div>
            <span class="text-xs font-bold text-indigo-700 bg-indigo-100 px-2.5 py-1 rounded-full border border-indigo-200">
              {{ selectedAssignWsIds.length }} poste(s) lié(s)
            </span>
          </div>

          <!-- Filter controls: Project dropdown & Zone dropdown -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 bg-slate-50 p-3 rounded-xl border border-gray-200">
            <div>
              <label class="block text-[11px] font-semibold text-gray-600 mb-0.5">1. Filtrer par Projet</label>
              <select v-model="assignPickerProjectId" @change="assignPickerZoneId = ''" class="w-full border border-gray-300 rounded-lg px-2.5 py-1.5 text-xs bg-white focus:ring-2 focus:ring-indigo-500">
                <option value="">-- Choisir un Projet --</option>
                <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-[11px] font-semibold text-gray-600 mb-0.5">2. Filtrer par Zone</label>
              <select v-model="assignPickerZoneId" :disabled="!assignPickerProjectId" class="w-full border border-gray-300 rounded-lg px-2.5 py-1.5 text-xs bg-white focus:ring-2 focus:ring-indigo-500 disabled:bg-gray-100 disabled:text-gray-400">
                <option value="">Toutes les zones du projet</option>
                <option v-for="z in assignPickerAvailableZones" :key="z.id" :value="z.id">{{ z.name }}</option>
              </select>
            </div>

            <!-- Search and bulk actions -->
            <div v-if="assignPickerProjectId" class="sm:col-span-2 flex items-center justify-between gap-2 pt-1 border-t border-gray-200 mt-1">
              <input v-model="assignPickerSearch" type="text" placeholder="Rechercher un poste dans ce projet..." class="flex-1 border border-gray-300 rounded-lg px-2.5 py-1 text-xs bg-white focus:ring-1 focus:ring-indigo-500" />
              <div class="flex items-center gap-1.5 shrink-0">
                <button type="button" @click="selectAllInAssignPicker" class="text-[11px] font-medium text-indigo-600 hover:text-indigo-800 underline">Tout cocher</button>
                <span class="text-gray-300">|</span>
                <button type="button" @click="deselectAllInAssignPicker" class="text-[11px] font-medium text-gray-500 hover:text-gray-700 underline">Décocher</button>
              </div>
            </div>
          </div>

          <!-- Workstations list for selected project/zone -->
          <div v-if="assignPickerProjectId" class="max-h-48 overflow-y-auto space-y-1.5 border border-gray-200 rounded-lg p-2 bg-white">
            <div v-if="assignPickerDisplayedWorkstations.length === 0" class="text-center py-4 text-xs text-gray-400">
              Aucun poste trouvé pour cette sélection
            </div>
            <label
              v-for="ws in assignPickerDisplayedWorkstations"
              :key="ws.id"
              :class="selectedAssignWsIds.includes(ws.id) ? 'bg-emerald-50 border-emerald-300 text-emerald-900 font-semibold' : 'bg-gray-50 border-gray-200 text-gray-700'"
              class="flex items-center justify-between p-2 rounded-lg border hover:bg-slate-100 cursor-pointer transition text-xs"
            >
              <div class="flex items-center gap-2">
                <input type="checkbox" :value="ws.id" v-model="selectedAssignWsIds" class="rounded text-emerald-600 focus:ring-emerald-500" />
                <span>{{ ws.name }}</span>
              </div>
              <span class="text-[10px] text-gray-400 bg-white px-1.5 py-0.5 rounded border border-gray-200">{{ ws.zoneName || 'Zone' }}</span>
            </label>
          </div>

          <div v-else class="text-center py-5 text-xs text-gray-500 bg-slate-50 rounded-lg border border-dashed border-gray-200">
            👉 Sélectionnez un projet ci-dessus pour afficher et cocher/décocher ses postes de travail.
          </div>

          <!-- Selected Workstations Chips Tray (Cross-Project summary) -->
          <div v-if="selectedAssignWsIds.length > 0" class="pt-2 border-t border-gray-200">
            <div class="flex items-center justify-between mb-1.5">
              <span class="text-[11px] font-bold text-gray-700">Tous les postes actuellement affectés à ce template :</span>
              <button type="button" @click="selectedAssignWsIds = []" class="text-[10px] text-red-600 hover:text-red-800 underline">Vider tout 🗑️</button>
            </div>
            <div class="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto p-1 bg-slate-50 rounded-lg border border-gray-200">
              <span
                v-for="wsId in selectedAssignWsIds"
                :key="wsId"
                class="inline-flex items-center gap-1 text-[11px] bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full border border-emerald-300 shadow-xs"
              >
                <span class="font-medium">{{ getWorkstationInfo(wsId).projectName }} &rsaquo; {{ getWorkstationInfo(wsId).name }}</span>
                <button type="button" @click="removeWsFromAssign(wsId)" class="hover:text-red-600 font-bold ml-0.5">✕</button>
              </span>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 pt-3 border-t border-gray-100 mt-3">
          <button @click="showAssignModal = false" class="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
          <button @click="saveWorkstationAssignments" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-lg shadow-sm">
            Enregistrer l'Affectation ({{ selectedAssignWsIds.length }} poste(s))
          </button>
        </div>
      </div>
    </div>

    <!-- Zoom Image Modal -->
    <div v-if="activeZoomImageUrl" class="fixed inset-0 bg-black/80 z-50 flex items-center justify-center p-4 cursor-zoom-out" @click="activeZoomImageUrl = null">
      <div class="relative bg-white rounded-lg p-2 max-w-4xl max-h-[85vh] overflow-auto shadow-2xl" @click.stop>
        <button @click="activeZoomImageUrl = null" class="absolute top-2 right-2 bg-black/60 hover:bg-black/80 text-white rounded-full w-8 h-8 flex items-center justify-center font-bold text-lg">×</button>
        <img :src="activeZoomImageUrl" class="max-w-full max-h-[80vh] object-contain rounded" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { evaluationApi, structureApi } from '@/api/endpoints'
import * as XLSX from 'xlsx'
import ExcelJS from 'exceljs'
import { useUserScope } from '@/composables/useUserScope'

const { isRestrictedRole, myProjectIds, loadUserProjects } = useUserScope()

const showImportModal = ref(false)
const importFile = ref(null)
const parsedQuestions = ref([])
const importError = ref('')
const importSuccess = ref('')
const importing = ref(false)

const authStore = useAuthStore()
const canManage = computed(() =>
  authStore.hasAnyRole(['ADMIN', 'AGENT_QUALITE', 'RESP_HSE', 'RESP_QUALITE', 'CHEF_EQUIPE']),
)
const isResponsable = computed(() => authStore.hasAnyRole(['CHEF_EQUIPE', 'RESP_QUALITE', 'ADMIN']))
const currentEmployeeId = computed(() => authStore.user?.employeeId)

const templates = ref([])
const workstations = ref([])
const projects = ref([])
const selectedProjectId = ref('')
const selectedZoneId = ref('')
const searchTemplateQuery = ref('')

const availableZones = computed(() => {
  if (!selectedProjectId.value) return []
  const proj = projects.value.find(p => p.id === Number(selectedProjectId.value))
  return proj?.zones || []
})

function resetFilters() {
  selectedProjectId.value = ''
  selectedZoneId.value = ''
  searchTemplateQuery.value = ''
}

const activeType = ref('ALL')
const selectedTemplate = ref(null)
const templateDetail = ref(null)
const activeZoomImageUrl = ref(null)
const showCreateModal = ref(false)
const showAssignModal = ref(false)
const assignTargetTemplate = ref(null)
const selectedAssignWsIds = ref([])

const assignPickerProjectId = ref('')
const assignPickerZoneId = ref('')
const assignPickerSearch = ref('')

const openAssignModal = (tpl) => {
  assignTargetTemplate.value = tpl
  assignPickerProjectId.value = ''
  assignPickerZoneId.value = ''
  assignPickerSearch.value = ''
  const existingIds = []
  if (tpl.workstationId) existingIds.push(tpl.workstationId)
  if (tpl.workstations && Array.isArray(tpl.workstations)) {
    for (const w of tpl.workstations) {
      if (w.id && !existingIds.includes(w.id)) {
        existingIds.push(w.id)
      }
    }
  }
  selectedAssignWsIds.value = existingIds
  showAssignModal.value = true
}

const assignPickerAvailableZones = computed(() => {
  if (!assignPickerProjectId.value) return []
  const p = projects.value.find(proj => proj.id === Number(assignPickerProjectId.value))
  return p?.zones || []
})

const assignPickerDisplayedWorkstations = computed(() => {
  if (!assignPickerProjectId.value) return []
  const p = projects.value.find(proj => proj.id === Number(assignPickerProjectId.value))
  if (!p) return []
  let list = []
  if (assignPickerZoneId.value) {
    const z = p.zones?.find(zone => zone.id === Number(assignPickerZoneId.value))
    list = (z?.workstations || []).map(ws => ({ ...ws, zoneName: z.name, projectName: p.name }))
  } else {
    list = (p.zones || []).flatMap(z => (z.workstations || []).map(ws => ({ ...ws, zoneName: z.name, projectName: p.name })))
  }
  if (assignPickerSearch.value) {
    const q = assignPickerSearch.value.trim().toLowerCase()
    list = list.filter(ws => ws.name.toLowerCase().includes(q))
  }
  return list
})

const selectAllInAssignPicker = () => {
  for (const ws of assignPickerDisplayedWorkstations.value) {
    if (!selectedAssignWsIds.value.includes(ws.id)) {
      selectedAssignWsIds.value.push(ws.id)
    }
  }
}

const deselectAllInAssignPicker = () => {
  const displayedIds = new Set(assignPickerDisplayedWorkstations.value.map(ws => ws.id))
  selectedAssignWsIds.value = selectedAssignWsIds.value.filter(id => !displayedIds.has(id))
}

const removeWsFromAssign = (id) => {
  selectedAssignWsIds.value = selectedAssignWsIds.value.filter(wsId => wsId !== id)
}

const saveWorkstationAssignments = async () => {
  if (!assignTargetTemplate.value) return
  try {
    await evaluationApi.assignWorkstationsToTemplate(assignTargetTemplate.value.id, selectedAssignWsIds.value)
    showAssignModal.value = false
    await load()
  } catch (e) {
    alert('Erreur lors de l\'affectation: ' + (e.response?.data?.message || e.message))
  }
}

// Helpers & State for Create Modal Picker
const createPickerProjectId = ref('')
const createPickerZoneId = ref('')
const createPickerSearch = ref('')

const createPickerAvailableZones = computed(() => {
  if (!createPickerProjectId.value) return []
  const p = projects.value.find(proj => proj.id === Number(createPickerProjectId.value))
  return p?.zones || []
})

const createPickerDisplayedWorkstations = computed(() => {
  if (!createPickerProjectId.value) return []
  const p = projects.value.find(proj => proj.id === Number(createPickerProjectId.value))
  if (!p) return []
  let list = []
  if (createPickerZoneId.value) {
    const z = p.zones?.find(zone => zone.id === Number(createPickerZoneId.value))
    list = (z?.workstations || []).map(ws => ({ ...ws, zoneName: z.name, projectName: p.name }))
  } else {
    list = (p.zones || []).flatMap(z => (z.workstations || []).map(ws => ({ ...ws, zoneName: z.name, projectName: p.name })))
  }
  if (createPickerSearch.value) {
    const q = createPickerSearch.value.trim().toLowerCase()
    list = list.filter(ws => ws.name.toLowerCase().includes(q))
  }
  return list
})

const selectAllInCreatePicker = () => {
  for (const ws of createPickerDisplayedWorkstations.value) {
    if (!newTemplate.workstationIds.includes(ws.id)) {
      newTemplate.workstationIds.push(ws.id)
    }
  }
}

const deselectAllInCreatePicker = () => {
  const displayedIds = new Set(createPickerDisplayedWorkstations.value.map(ws => ws.id))
  newTemplate.workstationIds = newTemplate.workstationIds.filter(id => !displayedIds.has(id))
}

const removeWsFromCreate = (id) => {
  newTemplate.workstationIds = newTemplate.workstationIds.filter(wsId => wsId !== id)
}

const getWorkstationInfo = (id) => {
  for (const p of projects.value) {
    for (const z of (p.zones || [])) {
      for (const w of (z.workstations || [])) {
        if (w.id === id) {
          return { id: w.id, name: w.name, zoneName: z.name, projectName: p.name }
        }
      }
    }
  }
  const w = workstations.value.find(ws => ws.id === id)
  return w ? { id: w.id, name: w.name, zoneName: '', projectName: '' } : { id, name: `Poste #${id}`, zoneName: '', projectName: '' }
}

const newSectionTitle = ref('')
const newQuestion = reactive({})
const editingQuestionId = ref(null)
const editForm = reactive({
  questionText: '',
  expectedAnswer: '',
  complementaryQuestions: '',
})

const newTemplate = reactive({
  name: '',
  description: '',
  type: 'POSTE_PRODUCTION',
  workstationIds: [],
  workstationId: null,
  projectId: null,
  zoneId: null,
})

const createTemplateProjects = computed(() => {
  if (isRestrictedRole.value) {
    return projects.value.filter(p => myProjectIds.value.has(p.id))
  }
  return projects.value
})

const createTemplateZones = computed(() => {
  if (!newTemplate.projectId) return []
  const proj = projects.value.find(p => p.id === Number(newTemplate.projectId))
  return proj?.zones || []
})

const createTemplateWorkstations = computed(() => {
  if (!newTemplate.zoneId) return []
  const zone = createTemplateZones.value.find(z => z.id === Number(newTemplate.zoneId))
  return zone?.workstations || []
})

const typeTabs = [
  { key: 'ALL', label: 'Tous' },
  { key: 'GENERIC_COMMON', label: 'Partie Generique' },
  { key: 'POSTE_PRODUCTION', label: 'Poste Production' },
  { key: 'ANIMATION', label: 'Animation' },
]

const filteredTemplates = computed(() => {
  let list = templates.value

  if (activeType.value !== 'ALL') {
    list = list.filter((t) => t.type === activeType.value)
  }

  if (selectedProjectId.value) {
    const pId = Number(selectedProjectId.value)
    const proj = projects.value.find(p => p.id === pId)
    if (proj) {
      const wsIds = new Set(
        (proj.zones || []).flatMap(z => z.workstations || []).map(w => w.id)
      )
      list = list.filter(t => {
        if (t.type === 'GENERIC_COMMON' || t.type === 'ANIMATION') return true
        if (!t.workstationId && (!t.workstations || t.workstations.length === 0)) return true
        if (t.workstationId && wsIds.has(t.workstationId)) return true
        if (Array.isArray(t.workstations) && t.workstations.some(w => wsIds.has(w.id))) return true
        return false
      })
    }
  }

  if (selectedZoneId.value) {
    const zId = Number(selectedZoneId.value)
    const zone = availableZones.value.find(z => z.id === zId)
    if (zone) {
      const wsIds = new Set((zone.workstations || []).map(w => w.id))
      list = list.filter(t => {
        if (t.type === 'GENERIC_COMMON' || t.type === 'ANIMATION') return true
        if (!t.workstationId && (!t.workstations || t.workstations.length === 0)) return true
        if (t.workstationId && wsIds.has(t.workstationId)) return true
        if (Array.isArray(t.workstations) && t.workstations.some(w => wsIds.has(w.id))) return true
        return false
      })
    }
  }

  if (searchTemplateQuery.value) {
    const q = searchTemplateQuery.value.toLowerCase().trim()
    list = list.filter(t => t.name?.toLowerCase().includes(q) || t.description?.toLowerCase().includes(q))
  }

  return list
})

const countByType = (type) => {
  if (type === 'ALL') return templates.value.length
  return templates.value.filter((t) => t.type === type).length
}

const typeLabel = (type) => {
  const map = {
    GENERIC_COMMON: 'Partie Générique',
    POSTE_PRODUCTION: 'Production',
    ANIMATION: 'Animation',
  }
  return map[type] || type
}

const requiredContributorRolesByType = {
  GENERIC_COMMON: ['CHEF_EQUIPE', 'RESP_HSE', 'AGENT_QUALITE'],
  POSTE_PRODUCTION: ['CHEF_EQUIPE', 'AGENT_QUALITE'],
  ANIMATION: ['CHEF_EQUIPE', 'AGENT_QUALITE'],
}

const roleLabelMap = {
  CHEF_EQUIPE: 'CHEF_EQUIPE',
  RESP_HSE: 'RESP_HSE',
  AGENT_QUALITE: 'AGENT_QUALITE',
  RESP_QUALITE: 'RESP_QUALITE',
}

const roleSummaryForTemplate = computed(() => {
  if (!selectedTemplate.value || !templateDetail.value) return []

  const allQuestions = (templateDetail.value.sections || []).flatMap(
    (section) => section.questions || [],
  )
  const requiredRoles = requiredContributorRolesByType[selectedTemplate.value.type] || [
    'CHEF_EQUIPE',
  ]

  return requiredRoles.map((role) => {
    const validated = allQuestions.filter(
      (q) => q.validatorRole === role && q.status === 'VALIDATED',
    ).length
    const pending = allQuestions.filter(
      (q) => q.validatorRole === role && q.status === 'PENDING',
    ).length
    const rejected = allQuestions.filter(
      (q) => q.validatorRole === role && q.status === 'REJECTED',
    ).length
    return {
      role,
      label: roleLabelMap[role] || role,
      validated,
      pending,
      rejected,
    }
  })
})

const canValidateSelectedTemplate = computed(() => {
  if (!selectedTemplate.value || selectedTemplate.value.status !== 'DRAFT') return false
  const requiredRoles = requiredContributorRolesByType[selectedTemplate.value.type] || [
    'CHEF_EQUIPE',
  ]
  return requiredRoles.every((role) => {
    const item = roleSummaryForTemplate.value.find((entry) => entry.role === role)
    return !!item && item.validated > 0 && item.pending === 0 && item.rejected === 0
  })
})

const templateValidationMessage = computed(() => {
  if (!selectedTemplate.value || !templateDetail.value) return ''
  const pending = roleSummaryForTemplate.value.filter(
    (item) => item.pending > 0 || item.validated === 0,
  )
  if (!pending.length) return ''
  const text = pending
    .map((item) => {
      if (item.pending > 0) return `${item.label}: ${item.pending} en attente validation`
      return `${item.label}: 0 questions validees`
    })
    .join(' | ')
  return `En attente de validation: ${text}`
})

const statusLabel = (s) =>
  ({ DRAFT: 'Brouillon', VALIDATED: 'Valide', ARCHIVED: 'Archive' })[s] || s
const statusClass = (s) =>
  ({
    DRAFT: 'bg-gray-100 text-gray-700',
    VALIDATED: 'bg-green-100 text-green-700',
    ARCHIVED: 'bg-red-100 text-red-700',
  })[s] || ''

const questionBorderClass = (q) => {
  if (q.status === 'PENDING') return 'bg-yellow-50 border-l-yellow-500'
  if (q.status === 'REJECTED') return 'bg-red-50 border-l-red-500'
  return 'bg-gray-50 border-l-emerald-500'
}

const isOwnQuestion = (q) => {
  if (authStore.isAdmin || authStore.isRespQualite) return true
  return q.createdByEmployeeId === currentEmployeeId.value || q.validatorRole === authStore.primaryRole
}

async function load() {
  const [tplRes, wsRes, projRes] = await Promise.allSettled([
    evaluationApi.getTemplates(),
    structureApi.getWorkstations(),
    structureApi.getAll(),
  ])
  if (tplRes.status === 'fulfilled') templates.value = tplRes.value.data || []
  if (wsRes.status === 'fulfilled') workstations.value = wsRes.value.data || []
  if (projRes.status === 'fulfilled') projects.value = projRes.value.data || []
  await loadUserProjects()
}

async function selectTemplate(tpl) {
  selectedTemplate.value = tpl
  editingQuestionId.value = null
  try {
    const res = await evaluationApi.getTemplateDetail(tpl.id)
    templateDetail.value = res.data
    for (const s of res.data?.sections || []) {
      if (!newQuestion[s.id]) {
        newQuestion[s.id] = { text: '', expected: '', complementary: '', imageUrl: '' }
      }
    }
  } catch (e) {
    console.error('Error loading template détail', e)
  }
}

function closePanel() {
  selectedTemplate.value = null
  templateDetail.value = null
  editingQuestionId.value = null
}

async function openCreateModal() {
  newTemplate.name = ''
  newTemplate.description = ''
  newTemplate.type = 'POSTE_PRODUCTION'
  newTemplate.workstationIds = []
  newTemplate.workstationId = null
  newTemplate.projectId = null
  newTemplate.zoneId = null
  createPickerProjectId.value = ''
  createPickerZoneId.value = ''
  createPickerSearch.value = ''
  showCreateModal.value = true
}

async function createTemplate() {
  if (!newTemplate.name || !newTemplate.type) return
  try {
    const payload = {
      name: newTemplate.name,
      description: newTemplate.description,
      type: newTemplate.type,
      workstationIds: newTemplate.workstationIds || [],
      workstationId: newTemplate.workstationIds?.length > 0 ? newTemplate.workstationIds[0] : null
    }
    await evaluationApi.createTemplate(payload)
    showCreateModal.value = false
    await load()
  } catch (e) {
    alert('Erreur lors de la creation: ' + (e.response?.data?.message || e.message))
  }
}

async function addSection() {
  if (!newSectionTitle.value || !selectedTemplate.value) return
  try {
    await evaluationApi.addSection(selectedTemplate.value.id, {
      title: newSectionTitle.value,
      displayOrder: templateDetail.value?.sections?.length || 0,
      complementaryQuestions: null,
    })
    newSectionTitle.value = ''
    await selectTemplate(selectedTemplate.value)
    await load()
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
}

async function addQuestionToSection(sectionId) {
  const q = newQuestion[sectionId]
  if (!q?.text || !selectedTemplate.value) return
  try {
    const section = templateDetail.value.sections.find((s) => s.id === sectionId)
    await evaluationApi.addQuestion(selectedTemplate.value.id, {
      questionText: q.text,
      expectedAnswer: q.expected || '',
      complementaryQuestions: q.complementary || '',
      questionNumber: (section?.questions?.length || 0) + 1,
      sectionId,
      imageUrl: q.imageUrl || '',
    })
    newQuestion[sectionId] = { text: '', expected: '', complementary: '', imageUrl: '' }
    await selectTemplate(selectedTemplate.value)
    await load()
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
}

function startEditQuestion(q) {
  editingQuestionId.value = q.id
  editForm.questionText = q.questionText
  editForm.expectedAnswer = q.expectedAnswer || ''
  editForm.complementaryQuestions = q.complementaryQuestions || ''
  editForm.imageUrl = q.imageUrl || ''
}

function cancelEdit() {
  editingQuestionId.value = null
}

async function saveEditQuestion() {
  if (!editingQuestionId.value) return
  try {
    await evaluationApi.updateQuestion(editingQuestionId.value, {
      questionText: editForm.questionText,
      expectedAnswer: editForm.expectedAnswer,
      complementaryQuestions: editForm.complementaryQuestions,
      imageUrl: editForm.imageUrl || '',
      templateId: selectedTemplate.value.id,
    })
    editingQuestionId.value = null
    await selectTemplate(selectedTemplate.value)
    await load()
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
}

async function handleQuestionImageUpload(event, sectionId) {
  const file = event.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await evaluationApi.uploadQuestionImage(formData)
    if (res.data?.imageUrl) {
      newQuestion[sectionId].imageUrl = res.data.imageUrl
    }
  } catch (e) {
    alert("Erreur lors du téléversement de l'image: " + (e.response?.data?.message || e.message))
  }
}

async function handleEditQuestionImageUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await evaluationApi.uploadQuestionImage(formData)
    if (res.data?.imageUrl) {
      editForm.imageUrl = res.data.imageUrl
    }
  } catch (e) {
    alert("Erreur lors du téléversement de l'image: " + (e.response?.data?.message || e.message))
  }
}

function formatRoleLabel(role) {
  const labels = {
    CHEF_EQUIPE: "Chef d'Équipe",
    AGENT_QUALITE: "Agent Qualité",
    RESP_HSE: "Responsable HSE",
    RESP_QUALITE: "Responsable Qualité",
    ADMIN: "Administrateur",
  }
  return labels[role] || role
}

function detectValidatorRole(questionText, explicitRoleStr, sectionTitle = '') {
  const combined = ((explicitRoleStr || '') + ' ' + (sectionTitle || '') + ' ' + (questionText || '')).toUpperCase().trim()
  if (combined.includes('HSE') || combined.includes('EHS') || combined.includes('SÉCURITÉ') || combined.includes('SECURITE') || combined.includes('ENVIRONNEMENT')) {
    return 'RESP_HSE'
  }
  if (combined.includes('QUALITÉ') || combined.includes('QUALITE') || combined.includes('KPC') || combined.includes('S/R') || combined.includes('NON CONFORME') || combined.includes('DÉFAUT') || combined.includes('DEFAUT')) {
    return combined.includes('RESP') ? 'RESP_QUALITE' : 'AGENT_QUALITE'
  }
  if (combined.includes('CHEF') || combined.includes('EQUIPE') || combined.includes('ÉQUIPE')) {
    return 'CHEF_EQUIPE'
  }
  return 'CHEF_EQUIPE'
}

async function deleteQuestion(questionId) {
  if (!confirm('Supprimer cette question ?')) return
  try {
    await evaluationApi.deleteQuestion(questionId, selectedTemplate.value.id)
    await selectTemplate(selectedTemplate.value)
    await load()
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
}

async function clearQuestions() {
  if (!selectedTemplate.value) return
  if (!confirm(`Voulez-vous vraiment supprimer toutes les questions du template "${selectedTemplate.value.name}" ? Vous pourrez ensuite réimporter votre fichier Excel proprement.`)) return
  try {
    await evaluationApi.clearTemplateQuestions(selectedTemplate.value.id)
    await selectTemplate(selectedTemplate.value)
    await load()
    alert('Toutes les questions ont été supprimées avec succès. Vous pouvez maintenant réimporter le fichier Excel.')
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
}

async function deleteThisTemplate() {
  if (!selectedTemplate.value) return
  if (!confirm(`Voulez-vous vraiment supprimer définitivement le template "${selectedTemplate.value.name}" ?`)) return
  try {
    await evaluationApi.deleteTemplate(selectedTemplate.value.id)
    closePanel()
    await load()
    alert('Template supprimé avec succès.')
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
}

async function validateThisTemplate() {
  if (!selectedTemplate.value) return
  try {
    const templateId = selectedTemplate.value.id
    const res = await evaluationApi.validateTemplate(templateId)

    await load()

    const refreshed = templates.value.find((t) => t.id === templateId) || {
      ...selectedTemplate.value,
      status: res.data?.status || 'VALIDATED',
    }

    selectedTemplate.value = refreshed
    await selectTemplate(refreshed)
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
}

function openImportQuestionsModal() {
  importFile.value = null
  parsedQuestions.value = []
  importError.value = ''
  importSuccess.value = ''
  showImportModal.value = true
}

function closeImportModal() {
  showImportModal.value = false
  importFile.value = null
  parsedQuestions.value = []
  importError.value = ''
  importSuccess.value = ''
}

function handleFileChange(event) {
  const file = event.target.files[0]
  if (!file) return
  importFile.value = file
  importError.value = ''
  importSuccess.value = ''
  
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const firstSheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[firstSheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
      
      if (jsonData.length < 2) {
        importError.value = "Le fichier Excel est vide ou ne contient pas assez de données."
        return
      }
      
      // Multi-sheet / Header detection for OPmobility form
      let qTextIdx = -1, expectedIdx = -1, roleIdx = -1, compIdx = -1
      
      // Check if header row exists in first sheet
      const headers = jsonData[0].map(h => String(h).trim().toLowerCase())
      qTextIdx = headers.findIndex(h => h.includes('question') || h.includes('texte'))
      expectedIdx = headers.findIndex(h => h.includes('réponse') || h.includes('reponse') || h.includes('attendu'))
      roleIdx = headers.findIndex(h => h.includes('rôle') || h.includes('role') || h.includes('validateur'))
      compIdx = headers.findIndex(h => h.includes('complémentaire') || h.includes('complementaire'))

      const list = []
      
      if (qTextIdx !== -1) {
        // Standard Tabular Format (Header row present)
        let lastAnswer = ''
        for (let i = 1; i < jsonData.length; i++) {
          const row = jsonData[i]
          if (!row || row.length === 0 || !row[qTextIdx]) continue
          const qText = String(row[qTextIdx]).trim()
          if (!qText) continue
          
          let expAnswer = expectedIdx !== -1 && row[expectedIdx] ? String(row[expectedIdx]).trim() : ''
          if (expAnswer) {
            lastAnswer = expAnswer
          } else if (lastAnswer) {
            expAnswer = lastAnswer
          }

          let roleVal = detectValidatorRole(qText, roleIdx !== -1 && row[roleIdx] ? String(row[roleIdx]) : '')
          const secTitle = headers.findIndex(h => h.includes('section')) !== -1 && row[headers.findIndex(h => h.includes('section'))]
            ? String(row[headers.findIndex(h => h.includes('section'))]).trim()
            : 'Questions d\'évaluation'

          list.push({
            sectionTitle: secTitle,
            questionText: qText,
            expectedAnswer: expAnswer,
            validatorRole: roleVal,
            complementaryQuestions: compIdx !== -1 && row[compIdx] ? String(row[compIdx]).trim() : '',
            questionNumber: list.length + 1
          })
        }
      } else {
        // OPmobility Official Form Format
        let currentCategory = 'Général'
        let currentSectionRole = 'CHEF_EQUIPE'
        let lastExpectedAnswer = ''

        for (let i = 0; i < jsonData.length; i++) {
          const row = jsonData[i]
          if (!row || row.length === 0) continue
          
          const cell0 = String(row[0] || '').trim()
          const fullRowStr = row.map(c => String(c || '').trim()).filter(Boolean).join(' ')
          
          if (fullRowStr.includes('VALIDATION AU POSTE') || fullRowStr.includes('Total des questions') || fullRowStr.includes('Date de validation') || fullRowStr.includes('Nom :') || fullRowStr.includes('Signature')) {
            continue
          }

          // Check if row starts with a question number (e.g. 1-, 2-, 10-, 21-)
          const qMatch = cell0.match(/^(\d+)[\s.-]+(.+)/s)
          if (qMatch) {
            const qNum = parseInt(qMatch[1], 10)
            let qText = qMatch[2].trim()
            let expectedAns = String(row[1] || '').trim()
            let roleStr = String(row[2] || '').trim()
            let compQuestion = String(row[9] || '').trim()

            // Lookahead for wrapped continuation rows (e.g. non-conforme?, PPM, etc.)
            while (i + 1 < jsonData.length && jsonData[i+1] && jsonData[i+1].length > 0) {
              const nextCell0 = String(jsonData[i+1][0] || '').trim()
              const nextCell1 = String(jsonData[i+1][1] || '').trim()
              const nextCell9 = String(jsonData[i+1][9] || '').trim()
              
              if (nextCell0.match(/^(\d+)[\s.-]+/) || nextCell0.length > 30 || nextCell0.includes('Total') || nextCell0.includes('Signature')) {
                break
              }
              
              if (nextCell0) {
                qText += ' ' + nextCell0
              }
              if (nextCell1) {
                expectedAns = expectedAns ? expectedAns + ' - ' + nextCell1 : nextCell1
              }
              if (nextCell9) {
                compQuestion = compQuestion ? compQuestion + ' | ' + nextCell9 : nextCell9
              }
              i++
            }

            if (roleStr) {
              currentSectionRole = detectValidatorRole(qText, roleStr, currentCategory)
            } else {
              currentSectionRole = detectValidatorRole(qText, '', currentCategory)
            }

            if (expectedAns) {
              lastExpectedAnswer = expectedAns
            } else if (lastExpectedAnswer) {
              // Forward-fill expected answer for merged/shared question rows!
              expectedAns = lastExpectedAnswer
            }

            list.push({
              questionNumber: qNum,
              sectionTitle: currentCategory,
              questionText: qText,
              expectedAnswer: expectedAns,
              validatorRole: currentSectionRole,
              complementaryQuestions: compQuestion
            })
          } else if (cell0 && cell0.length > 2 && cell0.length < 50 && !cell0.includes('1: Bonne') && !cell0.includes('0: Mauvaise') && !cell0.includes('Réponse espérée')) {
            currentCategory = cell0
            currentSectionRole = detectValidatorRole('', '', currentCategory)
            lastExpectedAnswer = ''
          }
        }
      }
      
      if (list.length === 0) {
        importError.value = "Aucune question détectée dans le fichier. Assurez-vous d'avoir une colonne 'Question' ou d'utiliser le formulaire officiel."
      } else {
        parsedQuestions.value = list
      }
    } catch (err) {
      console.error(err)
      importError.value = "Erreur lors de la lecture du fichier Excel."
    }
  }
  reader.readAsArrayBuffer(file)
}

async function submitImport() {
  if (parsedQuestions.value.length === 0 || !selectedTemplate.value) return
  importing.value = true
  importError.value = ''
  importSuccess.value = ''
  
  try {
    const res = await evaluationApi.addQuestionsBatch(selectedTemplate.value.id, parsedQuestions.value)
    importSuccess.value = `${parsedQuestions.value.length} questions ont été importées avec succès !`
    parsedQuestions.value = []
    await selectTemplate(selectedTemplate.value)
    await load()
  } catch (err) {
    console.error(err)
    importError.value = err.response?.data?.message || err.message || "Erreur lors de l'importation."
  } finally {
    importing.value = false
  }
}

async function exportQuestionsToExcel() {
  if (!selectedTemplate.value) return
  const tpl = selectedTemplate.value
  const sections = templateDetail.value?.sections || tpl.sections || []
  
  const workstationNames = tpl.workstations?.length > 0
    ? tpl.workstations.map(w => (w.projectName ? `${w.projectName} › ${w.name}` : w.name)).join(', ')
    : (tpl.workstationName || "Tous postes")

  const subTitle = tpl.type === 'ANIMATION'
    ? "Capacité d'animation et suivi (applicable pour le passage de L à U)"
    : (tpl.type === 'GENERIC_COMMON' ? "Partie Générique Commune" : "Partie Production")

  const workbook = new ExcelJS.Workbook()
  const sheetName = tpl.type === 'ANIMATION' ? "Capacité d'animation Suivi" : "Validation au poste"
  const worksheet = workbook.addWorksheet(sheetName, {
    views: [{ showGridLines: true }]
  })

  // Define column dimensions
  worksheet.columns = [
    { key: 'q', width: 48 },
    { key: 'ans', width: 38 },
    { key: 'val', width: 24 },
    { key: 's0', width: 8 },
    { key: 's1', width: 8 },
    { key: 'comp', width: 40 },
  ]

  const thinBorder = {
    top: { style: 'thin', color: { argb: 'FF9CA3AF' } },
    left: { style: 'thin', color: { argb: 'FF9CA3AF' } },
    bottom: { style: 'thin', color: { argb: 'FF9CA3AF' } },
    right: { style: 'thin', color: { argb: 'FF9CA3AF' } }
  }

  // 1. Top Header Banner
  worksheet.mergeCells('A1:F1')
  const r1 = worksheet.getCell('A1')
  r1.value = `VALIDATION AU POSTE : ${(tpl.name || 'ÉVALUATION').toUpperCase()}`
  r1.font = { name: 'Calibri', size: 13, bold: true, color: { argb: 'FFFFFFFF' } }
  r1.alignment = { horizontal: 'center', vertical: 'middle' }
  r1.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF003F15' } }
  worksheet.getRow(1).height = 28

  // 2. Subtitle Banner
  worksheet.mergeCells('A2:F2')
  const r2 = worksheet.getCell('A2')
  r2.value = subTitle
  r2.font = { name: 'Calibri', size: 10, italic: true, color: { argb: 'FF374151' } }
  r2.alignment = { horizontal: 'center', vertical: 'middle' }
  r2.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF3F4F6' } }
  worksheet.getRow(2).height = 20

  // 3. Metadata & Scoring Legend
  worksheet.getCell('A4').value = 'Nom : _______________________________'
  worksheet.getCell('A4').font = { name: 'Calibri', size: 10, bold: true }
  worksheet.getCell('C4').value = "Date d'évaluation : ________________"
  worksheet.getCell('C4').font = { name: 'Calibri', size: 10, bold: true }

  worksheet.getCell('A5').value = `Poste(s) de travail : ${workstationNames}`
  worksheet.getCell('A5').font = { name: 'Calibri', size: 10 }
  worksheet.getCell('F5').value = '1 : Bonne réponse   |   0 : Mauvaise réponse'
  worksheet.getCell('F5').font = { name: 'Calibri', size: 9, bold: true, color: { argb: 'FF003F15' } }
  worksheet.getCell('F5').alignment = { horizontal: 'right' }

  // 4. Table Header (Row 7)
  const headerRow = worksheet.getRow(7)
  headerRow.values = ['Question', 'Réponse espérée', 'Validateur', '0', '1', 'Question Complémentaire']
  headerRow.height = 24
  headerRow.eachCell((cell, colNum) => {
    cell.font = { name: 'Calibri', size: 10, bold: true, color: { argb: 'FF111827' } }
    cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFE5E7EB' } }
    cell.border = thinBorder
    cell.alignment = { horizontal: 'center', vertical: 'middle' }
  })
  worksheet.getCell('A7').alignment = { horizontal: 'left', vertical: 'middle' }
  worksheet.getCell('B7').alignment = { horizontal: 'left', vertical: 'middle' }
  worksheet.getCell('F7').alignment = { horizontal: 'left', vertical: 'middle' }

  let currRowNum = 8
  let qNum = 1
  let totalQuestionsCount = 0

  sections.forEach(sec => {
    const qList = sec.questions || []
    if (qList.length > 0 || sec.title) {
      // Section Header Row
      worksheet.mergeCells(`A${currRowNum}:F${currRowNum}`)
      const secCell = worksheet.getCell(`A${currRowNum}`)
      secCell.value = (sec.title || 'GÉNÉRAL').toUpperCase()
      secCell.font = { name: 'Calibri', size: 10, bold: true, color: { argb: 'FF1E293B' } }
      secCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFE2E8F0' } }
      secCell.border = thinBorder
      secCell.alignment = { horizontal: 'left', vertical: 'middle', indent: 1 }
      worksheet.getRow(currRowNum).height = 22
      currRowNum++

      // Question Rows
      qList.forEach(q => {
        totalQuestionsCount++
        const row = worksheet.getRow(currRowNum)
        const roleLabel = formatRoleLabel(q.validatorRole)
        row.values = [
          `${qNum++}- ${q.questionText || ''}`,
          q.expectedAnswer || '',
          roleLabel ? `à valider par ${roleLabel}` : '',
          '',
          '',
          q.complementaryQuestions || ''
        ]
        row.height = 28
        row.eachCell((cell, colNum) => {
          cell.border = thinBorder
          cell.alignment = { vertical: 'middle', wrapText: true }
          if (colNum === 1) cell.font = { name: 'Calibri', size: 10, bold: false }
          if (colNum === 2) {
            cell.font = { name: 'Calibri', size: 9.5, italic: true, color: { argb: 'FF4B5563' } }
            cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF9FAFB' } }
          }
          if (colNum === 3) {
            cell.font = { name: 'Calibri', size: 9.5, bold: true, color: { argb: 'FF1E40AF' } }
            cell.alignment = { horizontal: 'center', vertical: 'middle', wrapText: true }
          }
          if (colNum === 4 || colNum === 5) {
            cell.alignment = { horizontal: 'center', vertical: 'middle' }
            cell.font = { name: 'Calibri', size: 10, bold: true, color: { argb: 'FF6B7280' } }
          }
          if (colNum === 6) {
            cell.font = { name: 'Calibri', size: 9.5, italic: true, color: { argb: 'FF6B21A8' } }
          }
        })
        currRowNum++
      })
    }
  })

  // 5. Evaluation Summary Block
  currRowNum++
  worksheet.getCell(`C${currRowNum}`).value = 'Total des questions :'
  worksheet.getCell(`C${currRowNum}`).font = { name: 'Calibri', size: 10, bold: true }
  worksheet.getCell(`D${currRowNum}`).value = totalQuestionsCount
  worksheet.getCell(`D${currRowNum}`).font = { name: 'Calibri', size: 11, bold: true }
  worksheet.getCell(`D${currRowNum}`).alignment = { horizontal: 'center' }

  currRowNum++
  worksheet.getCell(`C${currRowNum}`).value = 'Total des réponses correctes :'
  worksheet.getCell(`C${currRowNum}`).font = { name: 'Calibri', size: 10, bold: true }
  worksheet.getCell(`D${currRowNum}`).value = '____'
  worksheet.getCell(`D${currRowNum}`).alignment = { horizontal: 'center' }

  currRowNum++
  worksheet.getCell(`C${currRowNum}`).value = 'Score final :'
  worksheet.getCell(`C${currRowNum}`).font = { name: 'Calibri', size: 10, bold: true }
  worksheet.getCell(`D${currRowNum}`).value = '____ %'
  worksheet.getCell(`D${currRowNum}`).alignment = { horizontal: 'center' }

  currRowNum += 2
  worksheet.getCell(`A${currRowNum}`).value = 'Décision :   [   ] Validé (≥ 80%)      [   ] Non Validé (< 80%)'
  worksheet.getCell(`A${currRowNum}`).font = { name: 'Calibri', size: 11, bold: true, color: { argb: 'FF1F2937' } }

  // 6. Signatures Box
  currRowNum += 2
  const sigHeadersRow = worksheet.getRow(currRowNum)
  sigHeadersRow.values = [
    'Signature Opérateur',
    'Signature Responsable HSE',
    'Signature Chef d\'Équipe',
    '',
    '',
    'Signature Agent Qualité'
  ]
  sigHeadersRow.height = 20
  sigHeadersRow.eachCell((cell) => {
    cell.font = { name: 'Calibri', size: 9.5, bold: true, color: { argb: 'FF374151' } }
    cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFF3F4F6' } }
    cell.border = thinBorder
    cell.alignment = { horizontal: 'center', vertical: 'middle' }
  })

  currRowNum++
  const sigBoxRow = worksheet.getRow(currRowNum)
  sigBoxRow.height = 45
  sigBoxRow.values = ['', '', '', '', '', '']
  sigBoxRow.eachCell((cell) => {
    cell.border = thinBorder
  })

  // Write file in browser
  const buffer = await workbook.xlsx.writeBuffer()
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const url = window.URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  const safeName = (tpl.name || 'Evaluation').replace(/[^a-z0-9]/gi, '_')
  anchor.download = `Fiche_Evaluation_${safeName}_${new Date().toISOString().slice(0, 10)}.xlsx`
  anchor.click()
  window.URL.revokeObjectURL(url)
}

onMounted(load)
</script>
