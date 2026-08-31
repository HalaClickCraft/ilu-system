<template>
  <div class="space-y-6">
    <!-- Global error banner (replaces alert()) -->
    <div v-if="errorMessage" class="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start justify-between gap-3">
      <p class="text-sm text-red-800">{{ errorMessage }}</p>
      <button @click="errorMessage = ''" class="text-red-400 hover:text-red-600 font-bold">×</button>
    </div>

    <!-- Session loading spinner -->
    <div v-if="loadingSession" class="text-center py-12 text-gray-400">Chargement de l'évaluation...</div>

    <template v-else>
    <button @click="$router.back()" class="flex items-center gap-2 text-gray-500 hover:text-gray-700">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
      Retour
    </button>

    <!-- ====== VIEW 1: Existing Session Detail ====== -->
    <div v-if="session">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Évaluation: {{ session.operatorName }}</h1>
          <p class="text-sm text-gray-500 mt-1">
            Template: {{ session.templateName }}
            <span v-if="session.formationId"> | Formation #{{ session.formationId }}</span>
            | Evaluateur: {{ session.evaluatorName || 'N/A' }}
          </p>
        </div>
        <span :class="sessionStatusClass(session.status)" class="text-sm font-semibold px-3 py-1 rounded-full">
          {{ sessionStatusLabel(session.status) }}
        </span>
      </div>

      <div class="mt-3 flex items-center justify-between flex-wrap gap-2">
        <span class="inline-flex items-center rounded-full bg-violet-100 px-3 py-1 text-xs font-semibold text-violet-800">
          {{ evaluationLabel(session.mode) }}
        </span>
      </div>

      <!-- Wizard Stepper Indicator -->
      <div v-if="session.status === 'IN_PROGRESS' && wizardSteps.length > 1" class="mt-6 bg-white border border-gray-250 rounded-xl p-4 shadow-sm">
        <div class="flex items-center justify-between max-w-xl mx-auto">
          <div v-for="(step, idx) in wizardSteps" :key="step.key" class="flex items-center flex-1 last:flex-none">
            <button
              @click="goToStep(step.key)"
              :disabled="saving"
              class="flex items-center gap-2 text-sm font-semibold transition hover:opacity-80 disabled:opacity-50"
              :class="
                activeStep === step.key
                  ? 'text-sky-600'
                  : isStepCompleted(step.key)
                  ? 'text-green-600'
                  : 'text-gray-400'
              "
            >
              <span
                class="w-8 h-8 rounded-full flex items-center justify-center border-2 text-xs font-bold"
                :class="
                  activeStep === step.key
                    ? 'border-sky-600 bg-sky-50'
                    : isStepCompleted(step.key)
                    ? 'border-green-600 bg-green-50'
                    : 'border-gray-200'
                "
              >
                <svg v-if="isStepCompleted(step.key)" class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
                </svg>
                <span v-else>{{ idx + 1 }}</span>
              </span>
              <span class="hidden sm:inline">{{ step.label }}</span>
            </button>
            <div v-if="idx < wizardSteps.length - 1" class="h-0.5 bg-gray-200 flex-1 mx-4"></div>
          </div>
        </div>
      </div>

      <!-- Seniority info -->
      <div class="bg-amber-50 border border-amber-200 rounded-lg p-4 mt-4">
        <p class="text-sm font-medium text-amber-800">Anciennete: {{ session.seniorityMonths }} mois</p>
        <p class="text-xs text-amber-600 mt-1">
          Niveau I: &lt;6 mois, score 70%+ |
          Niveau L: 6+ mois, score 81%+ |
          Niveau U: 12+ mois, score 91%+
        </p>
      </div>

      <!-- Generic part 100% rule, shown up front for production evaluations -->
      <div v-if="session.templateType === 'POSTE_PRODUCTION'" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mt-3">
        <p class="text-sm font-medium text-blue-800">Règle de la partie commune</p>
        <p class="text-xs text-blue-600 mt-1">
          La partie générique (Sécurité, Qualité, gestion des non conformes) doit être réussie à
          <strong>100%</strong>. En dessous, l'évaluation est un échec sans passage à la partie production.
        </p>
      </div>

      <!-- Role completion status bar -->
      <div v-if="session.status === 'IN_PROGRESS' && roleCompletionStatus.length > 0" class="bg-white rounded-xl border p-4 mt-4">
        <p class="text-sm font-semibold text-gray-700 mb-3">Avancement par role:</p>
        <div class="flex flex-wrap gap-3">
          <div v-for="rs in roleCompletionStatus" :key="rs.role"
            class="flex items-center gap-2 px-3 py-2 rounded-lg border text-sm"
            :class="rs.completed ? 'bg-green-50 border-green-300 text-green-800' : 'bg-yellow-50 border-yellow-300 text-yellow-800'">
            <svg v-if="rs.completed" class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <svg v-else class="w-5 h-5 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <span class="font-medium">{{ roleLabel(rs.role) }}</span>
            <span class="text-xs opacity-75">({{ rs.answered }}/{{ rs.total }})</span>
          </div>
        </div>
      </div>

      <!-- OPmobility Style Validation Sheet -->
      <div class="bg-white border-4 border-blue-900 rounded-xl p-6 shadow-md max-w-5xl mx-auto my-4 font-sans text-gray-900 relative">
        
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
            <h2 class="text-xl font-extrabold text-blue-950 tracking-wider">VALIDATION AU POSTE</h2>
            <div class="text-xs text-gray-500 font-medium mt-0.5">Template : {{ session.templateName }}</div>
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

        <!-- Meta Info Rows -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 py-4 text-sm border-b border-gray-300">
          <div class="flex items-center gap-2">
            <span class="font-bold text-gray-700 whitespace-nowrap">Nom :</span>
            <span class="border-b border-gray-400 flex-1 px-2 py-0.5 font-semibold bg-gray-50/50 rounded">{{ session.operatorName }}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="font-bold text-gray-700 whitespace-nowrap">Date d'évaluation :</span>
            <span class="border-b border-gray-400 flex-1 px-2 py-0.5 font-semibold bg-gray-50/50 rounded">{{ formatDate(session.createdAt) }}</span>
          </div>
        </div>

        <!-- Step Title Banner -->
        <div class="w-full bg-[#003F15] text-white text-center py-2 mt-4 font-bold text-sm uppercase tracking-widest rounded shadow-sm">
          {{ activeStep === 'GENERIC' ? 'PARTIE COMMUNE / COMMENCER' : activeStep === 'ANIMATION' ? 'PARTIE ANIMATION' : 'PARTIE PRODUCTION' }}
        </div>

        <div class="text-right text-xs font-semibold text-gray-500 my-2">
          <span>1 : Bonne réponse</span> &nbsp;|&nbsp; <span>0 : Mauvaise réponse</span>
        </div>

        <!-- Section / Questions Table -->
        <div class="overflow-x-auto mt-2">
          <table class="w-full border-collapse border border-gray-400 text-xs">
            <thead>
              <tr class="bg-gray-100 text-gray-800 border-b border-gray-450 font-bold">
                <th class="border border-gray-450 px-3 py-2 text-left w-[36%]">Question</th>
                <th class="border border-gray-450 px-3 py-2 text-left w-[22%]">Réponse espérée</th>
                <th class="border border-gray-450 px-2 py-2 text-center w-[12%]">Validateur</th>
                <th class="border border-gray-450 px-2 py-2 text-center w-[5%]">0</th>
                <th class="border border-gray-450 px-2 py-2 text-center w-[5%]">1</th>
                <th class="border border-gray-450 px-3 py-2 text-left w-[20%]">Question Complémentaire</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="section in filteredSections" :key="section.id || section.title">
                <!-- Section Subheader row -->
                <tr class="bg-gray-100 font-bold border-b border-gray-400">
                  <td colspan="6" class="border border-gray-400 px-3 py-1.5 text-gray-700 uppercase tracking-wider text-[11px] bg-slate-200/50">
                    {{ section.title || 'Questions sans section' }}
                  </td>
                </tr>

                <tr v-for="q in section.questions" :key="q.id" class="hover:bg-gray-50/50">
                  <!-- Question Text -->
                  <td class="border border-gray-400 px-3 py-2.5">
                    <div class="flex items-start gap-2">
                      <span class="font-bold text-gray-500">{{ q.questionNumber }}.</span>
                      <div class="space-y-1 w-full">
                        <p class="text-gray-900 font-medium leading-relaxed">{{ q.questionText }}</p>
                        
                        <!-- Image Container Design -->
                        <div v-if="q.imageUrl" class="relative group mt-2 max-w-[180px] bg-slate-50 border border-slate-200 rounded p-1 shadow-sm hover:shadow-md transition">
                          <img :src="q.imageUrl" class="w-full max-h-24 rounded object-contain bg-white cursor-zoom-in" @click="activeZoomImageUrl = q.imageUrl" />
                          <div class="absolute bottom-1 right-1 bg-black/60 text-[9px] text-white px-1.5 py-0.2 rounded font-medium opacity-0 group-hover:opacity-100 transition">
                            Zoom
                          </div>
                        </div>
                      </div>
                    </div>
                  </td>

                  <!-- Expected Answer -->
                  <td class="border border-gray-400 px-3 py-2.5 text-gray-600 bg-gray-50/30 italic">
                    {{ q.expectedAnswer || '—' }}
                  </td>

                  <!-- Validator Role -->
                  <td class="border border-gray-400 px-2 py-2.5 text-center text-[10px] font-semibold text-blue-800 bg-blue-50/10">
                    à valider par<br/>{{ roleLabel(q.validatorRole) }}
                  </td>

                  <!-- Action / Score 0 Box -->
                  <td class="border border-gray-400 p-1 text-center bg-red-50/10">
                    <!-- If session is IN_PROGRESS and user can answer -->
                    <template v-if="session.status === 'IN_PROGRESS' && canAnswerQuestion(q.validatorRole)">
                      <button @click="setAnswer(q.id, 0)" 
                        class="w-8 h-8 rounded border-2 transition font-extrabold text-sm"
                        :class="answers[q.id] === 0 ? 'bg-red-600 border-red-700 text-white shadow-sm' : 'border-gray-300 hover:border-red-400 text-gray-400 bg-white'">
                        0
                      </button>
                    </template>
                    <!-- If other validator role is pending -->
                    <template v-else-if="session.status === 'IN_PROGRESS' && !canAnswerQuestion(q.validatorRole)">
                      <span class="text-gray-300">—</span>
                    </template>
                    <!-- Read-only results -->
                    <template v-else>
                      <span v-if="getAnswerForQuestion(q.id) === 0" class="text-red-600 font-extrabold text-base">X</span>
                    </template>
                  </td>

                  <!-- Action / Score 1 Box -->
                  <td class="border border-gray-400 p-1 text-center bg-green-50/10">
                    <template v-if="session.status === 'IN_PROGRESS' && canAnswerQuestion(q.validatorRole)">
                      <button @click="setAnswer(q.id, 1)" 
                        class="w-8 h-8 rounded border-2 transition font-extrabold text-sm"
                        :class="answers[q.id] === 1 ? 'bg-green-600 border-green-700 text-white shadow-sm' : 'border-gray-300 hover:border-green-400 text-gray-400 bg-white'">
                        1
                      </button>
                    </template>
                    <template v-else-if="session.status === 'IN_PROGRESS' && !canAnswerQuestion(q.validatorRole)">
                      <span class="text-gray-300">—</span>
                    </template>
                    <template v-else>
                      <span v-if="getAnswerForQuestion(q.id) === 1" class="text-green-600 font-extrabold text-base">✓</span>
                    </template>
                  </td>

                  <!-- Question Complémentaire -->
                  <td class="border border-gray-400 px-3 py-2.5 text-gray-700 bg-purple-50/10 italic text-[11px]">
                    {{ q.complementaryQuestions || '—' }}
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Stepper Controls -->
      <div v-if="session.status === 'IN_PROGRESS'" class="flex items-center justify-between mt-6 p-4 bg-white border border-gray-200 rounded-xl relative flex-wrap gap-3">
        <div class="flex items-center gap-3">
          <button
            v-if="hasPrevStep"
            @click="prevStep"
            :disabled="saving"
            class="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-sm font-semibold disabled:opacity-50 transition"
          >
            ← Précédent
          </button>
          <button
            @click="saveAnswers"
            :disabled="saving"
            class="px-5 py-2.5 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 rounded-lg text-sm font-medium disabled:opacity-50 transition"
          >
            {{ saving ? 'Sauvegarde...' : 'Sauvegarder' }}
          </button>
        </div>

        <div class="flex items-center gap-3 flex-wrap">
          <span class="text-xs text-gray-400 mr-2 hidden md:inline">
            {{ answeredCount }}/{{ activeStepQuestions.length }} questions répondues pour cette étape
          </span>
          <button
            v-if="hasNextStep"
            @click="nextStep"
            :disabled="saving"
            class="px-5 py-2.5 bg-sky-600 hover:bg-sky-700 text-white rounded-lg text-sm font-semibold disabled:opacity-50 transition shadow-sm"
          >
            Suivant →
          </button>
          <button
            v-else
            @click="showConfirmComplete = true"
            :disabled="saving || !allQuestionsAnswered"
            class="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold disabled:opacity-50 disabled:bg-gray-300 disabled:text-gray-500 transition shadow-sm"
          >
            Terminer l'évaluation
          </button>
        </div>

        <!-- Inline save confirmation -->
        <Transition name="save-pop">
          <span v-if="saveSuccess" class="absolute -top-10 left-1/2 -translate-x-1/2 inline-flex items-center gap-1.5 px-3 py-1.5 bg-green-100 text-green-700 text-sm font-medium rounded-lg border border-green-200">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            Sauvegardé !
          </span>
        </Transition>
      </div>

      <!-- Results -->
      <div v-if="session.status !== 'IN_PROGRESS' && session.totalQuestions" class="bg-white rounded-xl border p-6 mt-4">
        <h2 class="text-lg font-bold mb-4">Resultats</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Partie Générique (HSE+Q)</p>
            <p class="text-xl font-bold" :class="session.genericPercentage >= 100 ? 'text-green-600' : 'text-red-600'">{{ session.genericPercentage }}%</p>
            <p class="text-xs text-gray-400">{{ session.genericCorrect }}/{{ session.genericTotal }}</p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Production</p>
            <p class="text-xl font-bold text-blue-600">{{ session.productionPercentage }}%</p>
            <p class="text-xs text-gray-400">{{ session.productionCorrect }}/{{ session.productionTotal }}</p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Score Total</p>
            <p class="text-xl font-bold text-gray-900">{{ session.scorePercentage }}%</p>
            <p class="text-xs text-gray-400">{{ session.correctAnswers }}/{{ session.totalQuestions }}</p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Niveau</p>
            <p class="text-2xl font-bold" :class="niveauClass(session.niveau)">{{ session.niveau || '-' }}</p>
          </div>
        </div>
        <div v-if="session.decision === 'BLOCKED_GENERIC'" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
          <p class="font-bold">
            Echec{{ session.failureCount != null ? ' (' + session.failureCount + '/2)' : '' }} - Partie générique insuffisante
          </p>
          <p class="text-sm mt-1">La partie générique (HSE + Qualité) doit etre a 100% pour poursuivre l'évaluation.</p>
          <p v-if="session.secondChanceCreated" class="text-sm mt-2 font-medium">
            Une formation complémentaire de 12 jours a été créée automatiquement sur ce poste.
            L'opérateur doit la suivre puis repasser l'évaluation.
          </p>
          <p v-else-if="session.secondChanceCreated === false" class="text-sm mt-2 font-medium">
            Second échec constaté : le dossier est transmis aux Ressources Humaines (fin de contrat
            selon la procédure de formation). Aucune nouvelle évaluation n'est possible sur ce poste.
          </p>
        </div>
        <div v-else-if="session.decision === 'FAILED'" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
          <p class="font-bold">Echec{{ session.failureCount != null ? ' (' + session.failureCount + '/2)' : '' }}</p>
          <p class="text-sm mt-1">Le score de production ne permet pas d'attribuer le niveau correspondant a l'anciennete.</p>
          <p v-if="session.secondChanceCreated" class="text-sm mt-2 font-medium">
            Une formation complémentaire de 12 jours a été créée automatiquement sur ce poste.
            L'opérateur doit la suivre puis repasser l'évaluation.
          </p>
          <p v-else-if="session.secondChanceCreated === false" class="text-sm mt-2 font-medium">
            Second échec constaté : le dossier est transmis aux Ressources Humaines (fin de contrat
            selon la procédure de formation). Aucune nouvelle évaluation n'est possible sur ce poste.
          </p>
        </div>
        <div v-else-if="session.decision === 'PENDING_ANIMATION'" class="mt-4 bg-violet-50 border border-violet-200 rounded-lg p-4 text-violet-900">
          <p class="font-bold">Questionnaire Animation requis pour passer de L a U</p>
          <p class="text-sm mt-1">La production est validee au niveau L. Completez le formulaire Animation pour finaliser le passage au niveau U.</p>
          <button @click="startAnimation" :disabled="startingAnimation" class="mt-3 bg-violet-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-violet-700 disabled:opacity-50">
            {{ startingAnimation ? 'Demarrage...' : 'Commencer le questionnaire Animation' }}
          </button>
        </div>
        <div v-else-if="session.decision === 'PASSED_GENERIC' && session.nextTemplateId" class="mt-4 bg-blue-50 border border-blue-200 rounded-lg p-4 text-blue-900">
          <p class="font-bold">Partie generique reussie</p>
          <p class="text-sm mt-1">Continuez avec les questions de production pour terminer l'evaluation initiale.</p>
          <button @click="startProduction" :disabled="startingProduction" class="mt-3 bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50">
            {{ startingProduction ? 'Demarrage...' : 'Commencer les questions de production' }}
          </button>
        </div>
        <div v-else-if="session.decision?.startsWith('PASSED_')" class="mt-4 bg-green-50 border border-green-200 rounded-lg p-4 text-green-800">
          <p class="font-bold">Réussi - Niveau {{ session.niveau }}</p>
          <p v-if="session.niveau === 'L'" class="text-sm mt-1">Pour passer au niveau U: évaluation Animation requise apres 1 an d'anciennete.</p>
        </div>
      </div>
    </div>

    <!-- ====== VIEW 2: No session - show pending evaluations from completed 12j suivi ====== -->
    <div v-else>
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Évaluations a effectuer</h1>
          <p class="text-sm text-gray-500 mt-1">Opérateurs ayant réussi le suivi 12j et necessitant une évaluation</p>
        </div>
        <button @click="loadPendingEvaluations" class="bg-gray-100 text-gray-700 px-4 py-2 rounded-lg text-sm hover:bg-gray-200">
          Rafraichir
        </button>
      </div>

      <div v-if="loading" class="text-center py-12 text-gray-400">Chargement...</div>

      <div v-else-if="pendingEvaluations.length === 0" class="bg-white rounded-xl border p-12 text-center mt-4">
        <svg class="w-16 h-16 mx-auto text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <p class="mt-4 text-gray-400 text-lg">Aucun operateur en attente d'évaluation</p>
        <p class="text-sm text-gray-300 mt-1">Les operateurs apparaitront ici apres réussite du suivi 12 jours</p>
      </div>

      <div v-else class="space-y-3 mt-4">
        <div v-for="pe in pendingEvaluations" :key="pe.formationId" class="bg-white rounded-xl border p-5 hover:shadow-md transition">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-semibold text-gray-900 text-lg">{{ pe.operatorName }}</h3>
              <p class="text-sm text-gray-500">{{ pe.operatorEmployeeId }} | Poste: {{ pe.workstationName }} | Anciennete: {{ pe.seniorityMonths }} mois</p>
            </div>
            <button @click="goToStartEvaluation(pe)"
              class="bg-emerald-600 text-white px-5 py-2.5 rounded-lg hover:bg-emerald-700 font-medium">
              Commencer l'évaluation
            </button>
          </div>
        </div>
      </div>

      <!-- Manual start form -->
      <div class="bg-white rounded-xl border p-6 mt-8">
        <h2 class="text-lg font-bold mb-4">Demarrer une évaluation manuellement</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="text-sm font-medium text-gray-700">Opérateur</label>
            <select v-model="startForm.operatorId" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
              <option :value="null">-- Selectionner --</option>
              <option v-for="op in operators" :key="op.id" :value="op.id">{{ op.lastName }} {{ op.firstName }} ({{ op.employeeId }})</option>
            </select>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Template</label>
            <select v-model="startForm.templateId" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
              <option :value="null">-- Selectionner --</option>
              <option v-for="tpl in validatedTemplates" :key="tpl.id" :value="tpl.id">{{ tpl.name }} ({{ tpl.type }})</option>
            </select>
          </div>
          <div class="flex items-end">
            <button @click="manualStart" :disabled="!startForm.operatorId || !startForm.templateId" class="bg-emerald-600 text-white px-5 py-2 rounded-lg text-sm hover:bg-emerald-700 disabled:opacity-50 w-full">
              Demarrer
            </button>
          </div>
        </div>
      </div>
    </div>
    </template>
  </div>

  <!-- Confirm-final-completion modal: prevents accidental irreversible completion -->
  <div v-if="showConfirmComplete" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
    <div class="bg-white rounded-xl p-6 max-w-md w-full">
      <h3 class="text-lg font-bold text-gray-900">Terminer l'évaluation ?</h3>
      <p class="text-sm text-gray-600 mt-2">
        Toutes les questions ont une réponse. Une fois l'évaluation terminée, le résultat est
        définitif (réussite, échec ou blocage) et ne peut plus être modifié.
      </p>
      <div class="flex justify-end gap-3 mt-5">
        <button @click="showConfirmComplete = false" class="px-4 py-2 rounded-lg text-sm bg-gray-100 text-gray-700 hover:bg-gray-200">
          Continuer plus tard
        </button>
        <button @click="finishEvaluation" :disabled="saving" class="px-4 py-2 rounded-lg text-sm bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50">
          {{ saving ? 'Validation...' : 'Oui, terminer l\'évaluation' }}
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

  <style scoped>
.save-pop-enter-active {
  transition: all 0.3s ease-out;
}
.save-pop-leave-active {
  transition: all 0.4s ease-in;
}
.save-pop-enter-from {
  opacity: 0;
  transform: translateY(6px) scale(0.9);
}
.save-pop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { evaluationApi, operatorsApi } from '@/api/endpoints'
import { recyclageApi } from '@/services/recyclageApi'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const session = ref(null)
const activeZoomImageUrl = ref(null)
const templateSections = ref([])
const answers = reactive({})
const loading = ref(true)
const loadingSession = ref(false)
const saving = ref(false)
const saveSuccess = ref(false)
const errorMessage = ref('')
const showConfirmComplete = ref(false)
const pendingEvaluations = ref([])
const operators = ref([])
const validatedTemplates = ref([])
const startingAnimation = ref(false)
const startingProduction = ref(false)

const startForm = reactive({ operatorId: null, templateId: null, formationId: null })

const sessionStatusClass = (s) => ({
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  PASSED: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  BLOCKED: 'bg-red-100 text-red-700',
}[s] || 'bg-gray-100 text-gray-700')

const sessionStatusLabel = (s) => ({
  IN_PROGRESS: 'En cours',
  PASSED: 'Réussi',
  FAILED: 'Échoué',
  BLOCKED: 'Bloqué',
}[s] || s)

const niveauClass = (n) => ({ I: 'text-amber-600', L: 'text-blue-600', U: 'text-green-600' }[n] || 'text-gray-400')

const evaluationLabel = (mode) => ({
  RECYCLAGE: 'Recyclage',
  ANNUELLE: 'Évaluation annuelle',
  NOUVELLE_RECRUE: 'Évaluation initiale',
  ANIMATION: 'Évaluation animation',
}[mode] || 'Évaluation')

const roleLabel = (role) => ({
  CHEF_EQUIPE: "Chef d'Équipe",
  AGENT_QUALITE: 'Agent Qualite',
  RESP_HSE: 'Resp. HSE',
  RESP_QUALITE: 'Resp. Qualité',
}[role] || role)

const canAnswerQuestion = (validatorRole) => {
  if (authStore.hasAnyRole(['CHEF_EQUIPE', 'SUPERVISEUR', 'ADMIN', 'AGENT_QUALITE', 'RESP_HSE', 'RESP_QUALITE'])) {
    return true
  }
  return authStore.hasAnyRole([validatorRole])
}

// Wizard active step state
const activeStep = ref('GENERIC')

const wizardSteps = computed(() => {
  const steps = []
  const hasGeneric = templateSections.value.some(s => s.stepType === 'GENERIC')
  if (hasGeneric) {
    steps.push({ key: 'GENERIC', label: 'Générique' })
  }
  const hasProd = templateSections.value.some(s => s.stepType === 'PRODUCTION')
  if (hasProd) {
    steps.push({ key: 'PRODUCTION', label: 'Production' })
  }
  const hasAnim = templateSections.value.some(s => s.stepType === 'ANIMATION')
  if (hasAnim) {
    steps.push({ key: 'ANIMATION', label: 'Animation' })
  }
  return steps
})

const filteredSections = computed(() => {
  return templateSections.value.filter(s => s.stepType === activeStep.value)
})

const activeStepQuestions = computed(() => {
  return allQuestions.value.filter(q => q.stepType === activeStep.value)
})

const hasPrevStep = computed(() => {
  const steps = wizardSteps.value
  const idx = steps.findIndex(s => s.key === activeStep.value)
  return idx > 0
})

const hasNextStep = computed(() => {
  const steps = wizardSteps.value
  const idx = steps.findIndex(s => s.key === activeStep.value)
  return idx !== -1 && idx < steps.length - 1
})

async function prevStep() {
  const steps = wizardSteps.value
  const idx = steps.findIndex(s => s.key === activeStep.value)
  if (idx > 0) {
    await saveAnswers()
    activeStep.value = steps[idx - 1].key
  }
}

async function nextStep() {
  const steps = wizardSteps.value
  const idx = steps.findIndex(s => s.key === activeStep.value)
  if (idx !== -1 && idx < steps.length - 1) {
    await saveAnswers()
    activeStep.value = steps[idx + 1].key
  }
}

function isStepCompleted(stepKey) {
  const stepQuestions = allQuestions.value.filter(q => q.stepType === stepKey)
  if (stepQuestions.length === 0) return false
  return stepQuestions.every(q => answers[q.id] !== undefined)
}

async function goToStep(stepKey) {
  await saveAnswers()
  activeStep.value = stepKey
}

// Collect all questions flat from templateSections
const allQuestions = computed(() => {
  const qs = []
  for (const section of templateSections.value) {
    if (section.questions) {
      for (const q of section.questions) {
        qs.push(q)
      }
    }
  }
  return qs
})

// Role completion status: which roles have all their questions answered (scoped to active wizard step)
const roleCompletionStatus = computed(() => {
  const roleMap = {}
  for (const q of activeStepQuestions.value) {
    const role = q.validatorRole
    if (!role) continue
    if (!roleMap[role]) {
      roleMap[role] = { role, total: 0, answered: 0 }
    }
    roleMap[role].total++
    if (answers[q.id] !== undefined) {
      roleMap[role].answered++
    }
  }
  return Object.values(roleMap).map(r => ({
    ...r,
    completed: r.answered === r.total && r.total > 0,
  }))
})

// Check if ALL questions (across all wizard steps) are answered (enables Terminer button)
const allQuestionsAnswered = computed(() => {
  if (allQuestions.value.length === 0) return false
  return allQuestions.value.every(q => answers[q.id] !== undefined)
})

const answeredCount = computed(() =>
  allQuestions.value.filter(q => q.stepType === activeStep.value && answers[q.id] !== undefined).length
)

function setAnswer(questionId, value) { answers[questionId] = value }
function getAnswerForQuestion(questionId) { return answers[questionId] }

async function loadPendingEvaluations() {
  loading.value = true
  try {
    const res = await evaluationApi.getAllPendingEvaluations()
    pendingEvaluations.value = res.data || []
  } catch (e) {
    console.error('Error loading pending', e)
    errorMessage.value = 'Impossible de charger les évaluations en attente. Vérifiez la connexion puis réessayez.'
  }
  loading.value = false
}

async function loadDropdowns() {
  const [opsRes, tplRes] = await Promise.allSettled([
    operatorsApi.getActive(),
    evaluationApi.getTemplates()
  ])
  if (opsRes.status === 'fulfilled') operators.value = opsRes.value.data || []
  if (tplRes.status === 'fulfilled') validatedTemplates.value = (tplRes.value.data || []).filter(t => t.status === 'VALIDATED')
}

function goToStartEvaluation(pe) {
  startForm.operatorId = pe.operatorId
  startForm.formationId = pe.formationId
  
  // Find template linked to this operator's workstation
  const tpl = validatedTemplates.value.find(t => t.workstationId === pe.workstationId)
  if (tpl) {
    startForm.templateId = tpl.id
    manualStart()
  } else {
    errorMessage.value = `Aucun template d'évaluation validé n'a été trouvé pour le poste "${pe.workstationName}". Veuillez créer et valider un template de production pour ce poste dans la Gestion des Templates.`
  }
}

async function manualStart() {
  if (!startForm.operatorId || !startForm.templateId) return
  saving.value = true
  try {
    const payload = {
      operatorId: startForm.operatorId,
      templateId: startForm.templateId,
    }
    if (startForm.formationId) payload.formationId = startForm.formationId
    const res = await evaluationApi.startEvaluation(payload)
    const newSessionId = res.data.sessionId
    router.push(`/evaluation/session/${newSessionId}`)
  } catch (e) {
    errorMessage.value = 'Erreur: ' + (e.response?.data?.message || e.message)
  }
  saving.value = false
}

async function loadSessionDetail() {
  if (!session.value) return
  try {
    const [sessionRes, templateRes] = await Promise.all([
      evaluationApi.getSessionDetail(session.value.id || route.params.id),
      evaluationApi.getTemplateDetail(session.value.templateId)
    ])
    session.value = sessionRes.data
    const currentTemplate = templateRes.data
    
    // Tag the active template sections with their templateId and stepType
    const currentStepType = currentTemplate?.type === 'GENERIC_COMMON'
      ? 'GENERIC'
      : currentTemplate?.type === 'ANIMATION'
      ? 'ANIMATION'
      : 'PRODUCTION'

    const currentSections = (currentTemplate?.sections || []).map(sec => ({
      ...sec,
      templateId: currentTemplate.id,
      stepType: currentStepType,
      questions: (sec.questions || []).map(q => ({ ...q, templateId: currentTemplate.id, stepType: currentStepType }))
    }))
    
    templateSections.value = currentSections

    // Set initial active step based on active template
    activeStep.value = currentStepType

    // Fill current answers
    if (sessionRes.data?.answers) {
      for (const a of sessionRes.data.answers) {
        answers[a.questionId] = a.answer
      }
    }

    // Resolve additional templates (Production + Animation) for unified evaluation sheet
    if (session.value.status === 'IN_PROGRESS') {
      const allTemplatesRes = await evaluationApi.getTemplates()
      const allTemplates = allTemplatesRes.data || []
      
      let nextTemplateId = session.value.nextTemplateId
      let activeWstationId = currentTemplate?.workstation?.id

      // If active session is generic, load production template
      if (currentTemplate?.type === 'GENERIC_COMMON' && nextTemplateId) {
        try {
          const prodTplRes = await evaluationApi.getTemplateDetail(nextTemplateId)
          const prodTemplate = prodTplRes.data
          activeWstationId = prodTemplate?.workstation?.id
          
          if (prodTemplate?.sections) {
            const prodSections = prodTemplate.sections.map(sec => ({
              ...sec,
              templateId: prodTemplate.id,
              stepType: 'PRODUCTION',
              questions: (sec.questions || []).map(q => ({ ...q, templateId: prodTemplate.id, stepType: 'PRODUCTION' }))
            }))
            templateSections.value = [...templateSections.value, ...prodSections]
          }
        } catch (e) {
          console.error('Error loading production template', e)
        }
      }

      // If workstation is known, find if there is an animation template for it
      if (activeWstationId) {
        const animTemplateMeta = allTemplates.find(t => 
          t.workstationId === activeWstationId && 
          t.type === 'ANIMATION' && 
          t.status === 'VALIDATED'
        )
        if (animTemplateMeta) {
          try {
            const animTplRes = await evaluationApi.getTemplateDetail(animTemplateMeta.id)
            const animTemplate = animTplRes.data
            if (animTemplate?.sections) {
              const animSections = animTemplate.sections.map(sec => ({
                ...sec,
                templateId: animTemplate.id,
                stepType: 'ANIMATION',
                questions: (sec.questions || []).map(q => ({ ...q, templateId: animTemplate.id, stepType: 'ANIMATION' }))
              }))
              templateSections.value = [...templateSections.value, ...animSections]
            }
            // Store animation template ID for submission
            session.value.animationTemplateId = animTemplate.id
          } catch (e) {
            console.error('Error loading animation template', e)
          }
        }
      }
    }
  } catch (e) {
    console.error('Error loading session', e)
  }
}

async function saveAnswers() {
  if (!session.value) return
  saving.value = true
  saveSuccess.value = false
  try {
    const activeTemplateId = session.value.templateId
    const activeAnswers = allQuestions.value
      .filter(q => q.templateId === activeTemplateId && answers[q.id] !== undefined)
      .map(q => ({
        questionId: q.id,
        answer: answers[q.id]
      }))
    if (activeAnswers.length > 0) {
      await evaluationApi.submitAnswers(session.value.id, activeAnswers)
    }

    // Show inline confirmation near the button
    saveSuccess.value = true
    setTimeout(() => { saveSuccess.value = false }, 2500)
  } catch (e) {
    console.error('Erreur sauvegarde:', e)
    errorMessage.value = 'Échec de la sauvegarde ! Les réponses ne sont PAS enregistrées. Réessayez: '
      + (e.response?.data?.message || e.message)
  }
  saving.value = false
}

// Explicit final completion, always behind the confirmation modal
async function finishEvaluation() {
  if (!session.value) return
  saving.value = true
  errorMessage.value = ''
  try {
    // 1. Submit and complete active session (normally Generic or Production)
    const activeTemplateId = session.value.templateId
    const activeAnswers = allQuestions.value
      .filter(q => q.templateId === activeTemplateId && answers[q.id] !== undefined)
      .map(q => ({
        questionId: q.id,
        answer: answers[q.id]
      }))
    if (activeAnswers.length > 0) {
      await evaluationApi.submitAnswers(session.value.id, activeAnswers)
    }
    
    let res = await evaluationApi.completeEvaluation(session.value.id)
    let currentSessionData = res.data

    // 2. Check if we just completed Generic and need to proceed to Production
    if (currentSessionData.decision === 'PASSED_GENERIC' && session.value.nextTemplateId) {
      const prodTemplateId = session.value.nextTemplateId
      // Start production session in background
      const startRes = await evaluationApi.startEvaluation({
        operatorId: session.value.operatorId,
        templateId: prodTemplateId,
        formationId: session.value.workstationFormationId || session.value.formationId || undefined,
        mode: session.value.mode || 'NOUVELLE_RECRUE',
        planningId: session.value.planningId || undefined
      })
      const prodSessionId = startRes.data.sessionId

      // Submit production answers to new session
      const prodAnswers = allQuestions.value
        .filter(q => q.templateId === prodTemplateId && answers[q.id] !== undefined)
        .map(q => ({
          questionId: q.id,
          answer: answers[q.id]
        }))
      if (prodAnswers.length > 0) {
        await evaluationApi.submitAnswers(prodSessionId, prodAnswers)
      }

      // Complete production session
      const completeRes = await evaluationApi.completeEvaluation(prodSessionId)
      currentSessionData = completeRes.data
    }

    // 3. Check if we need to proceed to Animation (either from the step above or if we started on Production directly)
    if (currentSessionData.decision === 'PENDING_ANIMATION' && (currentSessionData.nextTemplateId || session.value.animationTemplateId)) {
      const animTemplateId = currentSessionData.nextTemplateId || session.value.animationTemplateId
      // Start animation session in background
      const startRes = await evaluationApi.startEvaluation({
        operatorId: session.value.operatorId,
        templateId: animTemplateId,
        mode: 'ANIMATION',
        planningId: session.value.planningId || undefined
      })
      const animSessionId = startRes.data.sessionId

      // Submit animation answers
      const animAnswers = allQuestions.value
        .filter(q => q.templateId === animTemplateId && answers[q.id] !== undefined)
        .map(q => ({
          questionId: q.id,
          answer: answers[q.id]
        }))
      if (animAnswers.length > 0) {
        await evaluationApi.submitAnswers(animSessionId, animAnswers)
      }

      // Complete animation session
      const completeRes = await evaluationApi.completeEvaluation(animSessionId)
      currentSessionData = completeRes.data
    }

    // Update active session ref to display the final result state
    session.value = { ...session.value, ...currentSessionData }
    showConfirmComplete.value = false
    router.push('/evaluation')
  } catch (e) {
    console.error('Erreur completion:', e)
    errorMessage.value = 'Erreur lors de la finalisation: ' + (e.response?.data?.message || e.message)
  }
  saving.value = false
}

async function startAnimation() {
  if (!session.value?.nextTemplateId) return
  startingAnimation.value = true
  try {
    const res = await evaluationApi.startEvaluation({
      operatorId: session.value.operatorId,
      templateId: session.value.nextTemplateId,
      mode: 'ANIMATION',
      planningId: session.value.planningId || undefined,
    })
    router.push({
      name: 'evaluation-session',
      params: { id: res.data.sessionId },
      query: route.query,
    })
  } catch (e) {
    errorMessage.value = 'Erreur: ' + (e.response?.data?.message || e.message)
  } finally {
    startingAnimation.value = false
  }
}

async function startProduction() {
  if (!session.value?.nextTemplateId) return
  startingProduction.value = true
  try {
    const res = await evaluationApi.startEvaluation({
      operatorId: session.value.operatorId,
      templateId: session.value.nextTemplateId,
      formationId: session.value.workstationFormationId || session.value.formationId || undefined,
      mode: session.value.mode || 'NOUVELLE_RECRUE',
      planningId: session.value.planningId || undefined,
    })
    router.push({ name: 'evaluation-session', params: { id: res.data.sessionId }, query: route.query })
  } catch (e) {
    errorMessage.value = 'Erreur: ' + (e.response?.data?.message || e.message)
  } finally {
    startingProduction.value = false
  }
}

watch(() => route.params.id, async (newId) => {
  if (newId && newId !== 'new') {
    loadingSession.value = true
    try {
      const res = await evaluationApi.getSessionDetail(newId)
      session.value = res.data
      // Reset local answers reactive state
      for (const k of Object.keys(answers)) {
        delete answers[k]
      }
      await loadSessionDetail()
    } catch (e) {
      errorMessage.value = 'Impossible de charger l\'évaluation (session introuvable ou erreur serveur).'
      console.error('Error loading session', e)
    }
    loadingSession.value = false
    loading.value = false
  }
})

onMounted(async () => {
  if (route.params.id && route.params.id !== 'new') {
    loadingSession.value = true
    try {
      const res = await evaluationApi.getSessionDetail(route.params.id)
      session.value = res.data
      await loadSessionDetail()
    } catch (e) {
      errorMessage.value = 'Impossible de charger l\'évaluation (session introuvable ou erreur serveur).'
      console.error('Error loading session', e)
    }
    loadingSession.value = false
    loading.value = false
    return
  }
  await Promise.all([loadPendingEvaluations(), loadDropdowns()])
  loading.value = false
})


</script>