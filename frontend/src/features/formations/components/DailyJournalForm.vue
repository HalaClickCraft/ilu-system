<template>
  <div>
    <div class="row mb-3">
      <div class="col-md-4">
        <label class="form-label">Jour (1-12) *</label>
        <input
          v-model.number="form.jour"
          type="number"
          min="1"
          max="12"
          class="form-control"
          required
        />
      </div>
      <div class="col-md-4">
        <label class="form-label">Cadence Réalisée (units/jour)</label>
        <input v-model.number="form.cadenceRealisee" type="number" class="form-control" />
      </div>
      <div class="col-md-4">
        <label class="form-label">Nombre de Défauts</label>
        <input v-model.number="form.nbDefauts" type="number" min="0" class="form-control" />
      </div>
    </div>

    <div class="mb-3">
      <label class="form-label">Remarques</label>
      <textarea
        v-model="form.remarques"
        class="form-control"
        rows="3"
        placeholder="Ex: Bonne progression, petit problème de..."
      ></textarea>
    </div>

    <!-- Journal Table -->
    <div class="table-responsive mb-3">
      <table class="table table-sm table-bordered">
        <thead class="table-light">
          <tr>
            <th style="width: 50px">Jour</th>
            <th>Cadence</th>
            <th>Défauts</th>
            <th>Remarques</th>
            <th style="width: 80px">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="entry in journal"
            :key="entry.idSuivi"
            :class="entry.cadenceRealisee >= 90 ? 'table-success' : ''"
          >
            <td>{{ entry.jour }}/12</td>
            <td>{{ entry.cadenceRealisee || '-' }} u/j</td>
            <td>{{ entry.nbDefauts || 0 }}</td>
            <td class="small">{{ entry.remarques }}</td>
            <td>
              <button
                @click="editEntry(entry)"
                class="btn btn-xs btn-warning"
                style="font-size: 11px; padding: 2px 6px"
              >
                ✎
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="d-flex gap-2 justify-content-end">
      <button type="button" @click="resetForm" class="btn btn-secondary btn-sm">
        Réinitialiser
      </button>
      <button type="button" @click="submitEntry" class="btn btn-primary btn-sm" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
        {{ editingId ? 'Mettre à jour' : 'Ajouter' }}
      </button>
    </div>

    <div
      v-if="message"
      :class="[
        'alert',
        messageType === 'success' ? 'alert-success' : 'alert-danger',
        'mt-3 mb-0',
        'alert-sm',
      ]"
    >
      {{ message }}
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'DailyJournalForm',
  props: {
    affectationId: {
      type: Number,
      required: true,
    },
    operateurMatricule: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      form: {
        jour: 1,
        cadenceRealisee: null,
        nbDefauts: 0,
        remarques: '',
      },
      journal: [],
      loading: false,
      message: '',
      messageType: '',
      editingId: null,
    }
  },
  methods: {
    async loadJournal() {
      try {
        const response = await axios.get(`/api/formations/${this.affectationId}/journal`)
        this.journal = response.data.sort((a, b) => a.jour - b.jour)
      } catch (error) {
        console.error('Erreur chargement journal:', error)
      }
    },
    async submitEntry() {
      if (!this.form.jour || this.form.jour < 1 || this.form.jour > 12) {
        this.message = 'Jour invalide (1-12)'
        this.messageType = 'error'
        return
      }

      this.loading = true
      this.message = ''

      try {
        if (this.editingId) {
          await axios.put(`/api/formations/journal/${this.editingId}`, {
            cadenceRealisee: this.form.cadenceRealisee,
            nbDefauts: this.form.nbDefauts,
            remarques: this.form.remarques,
          })
          this.message = '✅ Entrée mise à jour'
          this.editingId = null
        } else {
          await axios.post(`/api/formations/${this.affectationId}/journal`, this.form)
          this.message = `✅ Jour ${this.form.jour} enregistré`
        }

        this.messageType = 'success'
        this.resetForm()
        await this.loadJournal()
        this.$emit('entry-saved')
      } catch (error) {
        this.message = `❌ ${error.response?.data?.message || error.message}`
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    editEntry(entry) {
      this.form = {
        jour: entry.jour,
        cadenceRealisee: entry.cadenceRealisee,
        nbDefauts: entry.nbDefauts,
        remarques: entry.remarques,
      }
      this.editingId = entry.idSuivi
    },
    resetForm() {
      this.form = {
        jour: Math.max(...this.journal.map((j) => j.jour), 0) + 1,
        cadenceRealisee: null,
        nbDefauts: 0,
        remarques: '',
      }
      this.editingId = null
      this.message = ''
    },
  },
  mounted() {
    this.loadJournal()
  },
}
</script>

<style scoped>
.alert-sm {
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  margin-bottom: 0 !important;
}
</style>
