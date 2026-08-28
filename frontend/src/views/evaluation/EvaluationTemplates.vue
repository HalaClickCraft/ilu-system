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
          <span v-if="tpl.workstationName" class="bg-blue-50 text-blue-700 px-2 py-0.5 rounded">{{
            tpl.workstationName
          }}</span>
          <span v-if="tpl.targetNiveau" class="bg-amber-50 text-amber-700 px-2 py-0.5 rounded"
            >Niveau {{ tpl.targetNiveau }}</span
          >
          <span>{{ tpl.validatedQuestionCount || 0 }} questions validees</span>
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
          <div class="flex items-center gap-3">
            <button v-if="canManage" @click="openImportQuestionsModal" class="inline-flex items-center gap-2 px-3 py-1.5 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-xs font-medium transition shadow-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
              Importer Questions (Excel)
            </button>
            <button @click="closePanel" class="p-2 hover:bg-gray-100 rounded-lg">
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
        <div class="grid grid-cols-2 gap-4 mb-6 p-4 bg-gray-50 rounded-lg">
          <div>
            <span class="text-xs text-gray-500">Type</span>
            <p class="font-medium">{{ typeLabel(selectedTemplate.type) }}</p>
          </div>
          <div>
            <span class="text-xs text-gray-500">Statut</span>
            <p>
              <span
                :class="statusClass(selectedTemplate.status)"
                class="text-xs font-medium px-2 py-0.5 rounded-full"
                >{{ statusLabel(selectedTemplate.status) }}</span
              >
            </p>
          </div>
          <div v-if="selectedTemplate.workstationName">
            <span class="text-xs text-gray-500">Poste</span>
            <p class="font-medium">{{ selectedTemplate.workstationName }}</p>
          </div>
          <div>
            <span class="text-xs text-gray-500">Questions validees</span>
            <p class="font-medium">
              {{
                templateDetail?.sections?.reduce((sum, s) => sum + (s.questions?.length || 0), 0) ||
                0
              }}
            </p>
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

        <!-- Sections & Questions -->
        <div v-for="section in templateDetail?.sections" :key="section.id" class="mb-6">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-semibold text-gray-800 flex items-center gap-2">
              <span
                class="w-6 h-6 bg-sky-100 text-sky-700 rounded-full flex items-center justify-center text-xs font-bold"
                >{{ section.displayOrder + 1 }}</span
              >
              {{ section.title }}
              <span class="text-xs text-gray-400">({{ section.questions.length }} questions)</span>
            </h3>
          </div>

          <!-- Questions in section -->
          <div
            v-for="q in section.questions"
            :key="q.id"
            class="ml-8 mb-2 p-3 rounded-lg border-l-4 transition"
            :class="questionBorderClass(q)"
          >
            <!-- Normal view -->
            <div v-if="editingQuestionId !== q.id">
              <div class="flex items-start justify-between gap-3">
                <div class="flex-1 space-y-2">
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="text-xs font-semibold text-gray-500"
                      >Q{{ q.questionNumber || '' }}</span
                    >
                    <p class="text-sm font-medium text-gray-800">{{ q.questionText }}</p>
                    <span
                      v-if="q.status === 'PENDING'"
                      class="text-[10px] bg-yellow-100 text-yellow-800 px-1.5 py-0.5 rounded font-medium"
                      >EN ATTENTE</span
                    >
                    <span
                      v-if="q.status === 'REJECTED'"
                      class="text-[10px] bg-red-100 text-red-800 px-1.5 py-0.5 rounded font-medium"
                      >REJETEE</span
                    >
                    <span
                      v-if="q.status === 'VALIDATED'"
                      class="text-[10px] bg-green-100 text-green-800 px-1.5 py-0.5 rounded font-medium"
                      >VALIDEE</span
                    >
                  </div>

                  <div v-if="q.imageUrl" class="my-2">
                    <img :src="q.imageUrl" class="max-h-32 rounded border object-contain bg-white" />
                  </div>

                  <div class="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
                    <div class="rounded-md bg-gray-50 border border-gray-200 px-2 py-1.5">
                      <div class="font-semibold text-gray-600 mb-0.5">Réponse attendue</div>
                      <div class="text-gray-800">{{ q.expectedAnswer || '—' }}</div>
                    </div>
                    <div class="rounded-md bg-blue-50 border border-blue-200 px-2 py-1.5">
                      <div class="font-semibold text-blue-700 mb-0.5">Créée par</div>
                      <div class="text-blue-900">
                        {{ formatRoleLabel(q.validatorRole) }} · {{ q.createdByName || 'Inconnu' }}
                      </div>
                    </div>
                  </div>

                  <div
                    v-if="q.complementaryQuestions"
                    class="rounded-md border border-purple-200 bg-purple-50 px-2 py-1.5 text-xs text-purple-800"
                  >
                    <span class="font-semibold">Question complémentaire :</span>
                    {{ q.complementaryQuestions }}
                  </div>
                </div>

                <!-- Edit / Delete buttons -->
                <div
                  v-if="canManage && selectedTemplate.status === 'DRAFT' && isOwnQuestion(q)"
                  class="flex gap-1 ml-2 shrink-0"
                >
                  <button
                    @click.stop="startEditQuestion(q)"
                    class="p-1.5 text-blue-600 hover:bg-blue-50 rounded"
                    title="Modifier"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                      ></path>
                    </svg>
                  </button>
                  <button
                    @click.stop="deleteQuestion(q.id)"
                    class="p-1.5 text-red-600 hover:bg-red-50 rounded"
                    title="Supprimer"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                      ></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>

            <!-- Edit mode -->
            <div v-else class="space-y-2">
              <input
                v-model="editForm.questionText"
                class="w-full border border-gray-300 rounded px-2 py-1.5 text-sm"
                placeholder="Question..."
              />
              <input
                v-model="editForm.expectedAnswer"
                class="w-full border border-gray-300 rounded px-2 py-1.5 text-sm"
                placeholder="Reponse attendue..."
              />
              <textarea
                v-model="editForm.complementaryQuestions"
                rows="2"
                class="w-full border border-gray-300 rounded px-2 py-1.5 text-sm"
                placeholder="Question complémentaire..."
              ></textarea>
              <div class="space-y-1">
                <label class="block text-xs font-semibold text-gray-500">Image (optionnelle)</label>
                <div class="flex items-center gap-2">
                  <input
                    type="file"
                    accept="image/*"
                    @change="handleEditQuestionImageUpload($event)"
                    class="text-xs text-gray-500"
                  />
                  <button
                    v-if="editForm.imageUrl"
                    type="button"
                    @click="editForm.imageUrl = ''"
                    class="text-xs text-red-600 hover:underline"
                  >
                    Supprimer l'image
                  </button>
                </div>
                <img
                  v-if="editForm.imageUrl"
                  :src="editForm.imageUrl"
                  class="mt-1 h-12 w-auto object-contain rounded border"
                />
              </div>
              <div class="flex gap-2">
                <button
                  @click="saveEditQuestion"
                  class="bg-blue-600 text-white px-3 py-1.5 rounded text-xs hover:bg-blue-700"
                >
                  Enregistrer
                </button>
                <button
                  @click="cancelEdit"
                  class="bg-gray-200 text-gray-700 px-3 py-1.5 rounded text-xs hover:bg-gray-300"
                >
                  Annuler
                </button>
              </div>
            </div>
          </div>

          <!-- Add question to section -->
          <div
            v-if="canManage && selectedTemplate.status === 'DRAFT'"
            class="ml-8 mt-2 p-3 bg-gray-50 rounded-lg"
          >
            <h4 class="text-xs font-semibold text-gray-600 mb-2">+ Ajouter une question</h4>
            <div class="space-y-2">
              <input
                v-model="newQuestion[section.id].text"
                placeholder="Question..."
                class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
              />
              <input
                v-model="newQuestion[section.id].expected"
                placeholder="Reponse attendue..."
                class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
              />
              <textarea
                v-model="newQuestion[section.id].complementary"
                rows="2"
                placeholder="Question complémentaire (optionnel)..."
                class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
              ></textarea>
              <div class="space-y-1">
                <label class="block text-xs font-semibold text-gray-500">Image (optionnelle)</label>
                <div class="flex items-center gap-2">
                  <input
                    type="file"
                    accept="image/*"
                    @change="handleQuestionImageUpload($event, section.id)"
                    class="text-xs text-gray-500 file:mr-2 file:py-1 file:px-2 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-teal-50 file:text-teal-700 hover:file:bg-teal-100"
                  />
                  <button
                    v-if="newQuestion[section.id]?.imageUrl"
                    type="button"
                    @click="newQuestion[section.id].imageUrl = ''"
                    class="text-xs text-red-600 hover:underline"
                  >
                    Supprimer
                  </button>
                </div>
                <img
                  v-if="newQuestion[section.id]?.imageUrl"
                  :src="newQuestion[section.id].imageUrl"
                  class="mt-1 h-12 w-auto object-contain rounded border"
                />
              </div>
              <button
                @click="addQuestionToSection(section.id)"
                class="bg-sky-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-sky-700"
              >
                Ajouter
              </button>
            </div>
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
          <div v-if="newTemplate.type === 'POSTE_PRODUCTION' || newTemplate.type === 'ANIMATION'" class="space-y-3">
            <div>
              <label class="text-sm font-medium text-gray-700">Projet *</label>
              <select
                v-model="newTemplate.projectId"
                @change="newTemplate.zoneId = ''; newTemplate.workstationId = null"
                class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1"
              >
                <option :value="null">-- Sélectionner un Projet --</option>
                <option v-for="p in createTemplateProjects" :key="p.id" :value="p.id">{{ p.name }}</option>
              </select>
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Zone *</label>
              <select
                v-model="newTemplate.zoneId"
                :disabled="!newTemplate.projectId"
                @change="newTemplate.workstationId = null"
                class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1 disabled:bg-gray-100"
              >
                <option :value="null">-- Sélectionner une Zone --</option>
                <option v-for="z in createTemplateZones" :key="z.id" :value="z.id">{{ z.name }}</option>
              </select>
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Poste de travail (Workstation) *</label>
              <select
                v-model="newTemplate.workstationId"
                :disabled="!newTemplate.zoneId"
                class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1 disabled:bg-gray-100"
              >
                <option :value="null">-- Sélectionner un Poste --</option>
                <option v-for="ws in createTemplateWorkstations" :key="ws.id" :value="ws.id">{{ ws.name }}</option>
              </select>
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
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { evaluationApi, structureApi } from '@/api/endpoints'
import * as XLSX from 'xlsx'
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
const showCreateModal = ref(false)
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
      list = list.filter(t => !t.workstationId || wsIds.has(t.workstationId))
    }
  }

  if (selectedZoneId.value) {
    const zId = Number(selectedZoneId.value)
    const zone = availableZones.value.find(z => z.id === zId)
    if (zone) {
      const wsIds = new Set((zone.workstations || []).map(w => w.id))
      list = list.filter(t => !t.workstationId || wsIds.has(t.workstationId))
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
  newTemplate.workstationId = null
  newTemplate.projectId = null
  newTemplate.zoneId = null
  showCreateModal.value = true
}

async function createTemplate() {
  if (!newTemplate.name || !newTemplate.type) return
  try {
    const payload = { ...newTemplate }
    if (payload.workstationId) payload.workstationId = Number(payload.workstationId)
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

async function deleteQuestion(questionId) {
  if (!confirm('Supprimer cette question ?')) return
  try {
    await evaluationApi.deleteQuestion(questionId, selectedTemplate.value.id)
    await selectTemplate(selectedTemplate.value)
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
      
      const headers = jsonData[0].map(h => String(h).trim().toLowerCase())
      
      const qTextIdx = headers.findIndex(h => h.includes('question') || h.includes('texte'))
      const expectedIdx = headers.findIndex(h => h.includes('réponse') || h.includes('reponse') || h.includes('attendu'))
      const roleIdx = headers.findIndex(h => h.includes('rôle') || h.includes('role') || h.includes('validateur'))
      const compIdx = headers.findIndex(h => h.includes('complémentaire') || h.includes('complementaire'))
      
      if (qTextIdx === -1) {
        importError.value = "Colonne requise manquante. Assurez-vous d'avoir une colonne nommée 'Texte de la Question' ou 'Question'."
        return
      }
      
      const list = []
      for (let i = 1; i < jsonData.length; i++) {
        const row = jsonData[i]
        if (!row || row.length === 0 || !row[qTextIdx]) continue
        
        let roleVal = 'CHEF_EQUIPE'
        if (roleIdx !== -1 && row[roleIdx]) {
          const rStr = String(row[roleIdx]).toUpperCase().trim()
          if (rStr.includes('QUALITE') || rStr.includes('QUALITÉ')) {
            roleVal = rStr.includes('RESP') ? 'RESP_QUALITE' : 'AGENT_QUALITE'
          } else if (rStr.includes('HSE')) {
            roleVal = 'RESP_HSE'
          } else if (rStr.includes('CHEF')) {
            roleVal = 'CHEF_EQUIPE'
          }
        }
        
        list.push({
          questionText: String(row[qTextIdx]).trim(),
          expectedAnswer: expectedIdx !== -1 && row[expectedIdx] ? String(row[expectedIdx]).trim() : '',
          validatorRole: roleVal,
          complementaryQuestions: compIdx !== -1 && row[compIdx] ? String(row[compIdx]).trim() : '',
          questionNumber: i
        })
      }
      
      if (list.length === 0) {
        importError.value = "Aucune question valide détectée dans le fichier."
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
  } catch (err) {
    console.error(err)
    importError.value = err.response?.data?.message || err.message || "Erreur lors de l'importation."
  } finally {
    importing.value = false
  }
}

onMounted(load)
</script>
