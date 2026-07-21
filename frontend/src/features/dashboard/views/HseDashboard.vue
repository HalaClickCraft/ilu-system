<script setup>
import { ref } from 'vue'

const safetyChecks = ref([
  { id: 1, zone: 'Zone Assemblage A', portEPI: true, securiteMachine: true, statut: 'Sécurisé' },
  {
    id: 2,
    zone: 'Zone Finition B',
    portEPI: true,
    securiteMachine: false,
    statut: 'Alerte Mineure',
  },
])
const hseLogs = ref([
  {
    id: 101,
    date: '2026-07-14 09:15',
    auteur: 'Hélène HSE',
    motif: 'Ajout de consigne de sécurité Zone A',
  },
  {
    id: 102,
    date: '2026-07-14 11:30',
    auteur: 'Hélène HSE',
    motif: 'Correction gabarit sécurité incendie',
  },
])
</script>

<template>
  <section class="role-section">
    <div class="admin-grid" style="margin-top: 1.5rem">
      <!-- Safety status check list -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Contrôles de Sécurité par Zone Ligne</h3>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Zone</th>
                <th>Port des EPI</th>
                <th>Sécurités Machines</th>
                <th>Statut Global</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="check in safetyChecks" :key="check.id">
                <td>
                  <strong>{{ check.zone }}</strong>
                </td>
                <td>{{ check.portEPI ? '✅ Conforme' : '❌ Non conforme' }}</td>
                <td>{{ check.securiteMachine ? '✅ Active' : '⚠️ Anomalie' }}</td>
                <td>
                  <span
                    :class="['status-badge', check.statut === 'Sécurisé' ? 'active' : 'suspended']"
                  >
                    {{ check.statut }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Modif gabarit logs -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Journal des Modifications (JournalModifGabarit)</h3>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Auteur</th>
                <th>Motif de modification</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in hseLogs" :key="log.id">
                <td>
                  <code>{{ log.date }}</code>
                </td>
                <td>{{ log.auteur }}</td>
                <td>
                  <em>{{ log.motif }}</em>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </section>
</template>
