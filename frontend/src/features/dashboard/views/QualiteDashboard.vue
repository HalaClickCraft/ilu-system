<script setup>
import { ref } from 'vue'

const templates = ref([
  { id: 1, nom: 'Évaluation Standard Shift A', ordre: 1, date: '2026-07-01' },
  { id: 2, nom: 'Évaluation Avancée Rework', ordre: 2, date: '2026-07-05' },
])
const questions = ref([
  {
    id: 1,
    enonce: "Vérifier la conformité de l'étiquetage du bloc arrière",
    reponse: 'Conforme et lisible',
    bloc: 'Bloc A',
  },
  {
    id: 2,
    enonce: 'Calculer le temps de cycle standard sur poste de vissage',
    reponse: '42 secondes',
    bloc: 'Bloc B',
  },
])

const newQuestionEnonce = ref('')
const newQuestionReponse = ref('')
const newQuestionBloc = ref('Bloc A')

function addQuestion() {
  if (!newQuestionEnonce.value || !newQuestionReponse.value) return
  questions.value.push({
    id: Date.now(),
    enonce: newQuestionEnonce.value,
    reponse: newQuestionReponse.value,
    bloc: newQuestionBloc.value,
  })
  newQuestionEnonce.value = ''
  newQuestionReponse.value = ''
}
</script>

<template>
  <section class="role-section">
    <div class="admin-grid" style="margin-top: 1.5rem">
      <!-- Add Question Form -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Ajouter une Question d'Évaluation</h3>
        </div>
        <form @submit.prevent="addQuestion" class="panel-form">
          <div class="input-group">
            <label>Énoncé de la question</label>
            <input
              v-model="newQuestionEnonce"
              required
              placeholder="Ex: Vérifier le couple de serrage"
            />
          </div>
          <div class="input-group">
            <label>Réponse attendue</label>
            <input v-model="newQuestionReponse" required placeholder="Ex: Entre 4.5 et 5.2 Nm" />
          </div>
          <div class="input-group">
            <label>Bloc d'évaluation concerné</label>
            <select v-model="newQuestionBloc">
              <option value="Bloc A">Bloc A (Sécurité)</option>
              <option value="Bloc B">Bloc B (Technique)</option>
              <option value="Bloc C">Bloc C (Qualité)</option>
            </select>
          </div>
          <button type="submit" class="submit-btn">Ajouter à la banque</button>
        </form>
      </div>

      <!-- Templates and Questions List -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Gabarits & Banque de Questions</h3>
        </div>
        <div class="tabs-sim">
          <p><strong>Gabarits actifs (TemplateQuestionnaire) :</strong></p>
          <ul>
            <li v-for="t in templates" :key="t.id">
              📁 <strong>{{ t.nom }}</strong> (Ordre affichage: {{ t.ordre }}) - Créé le
              {{ t.date }}
            </li>
          </ul>

          <p style="margin-top: 1.5rem"><strong>Banque de questions associées :</strong></p>
          <div class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Bloc</th>
                  <th>Énoncé</th>
                  <th>Réponse Attendue</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="q in questions" :key="q.id">
                  <td>
                    <span class="role-badge">{{ q.bloc }}</span>
                  </td>
                  <td>{{ q.enonce }}</td>
                  <td>
                    <code>{{ q.reponse }}</code>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>