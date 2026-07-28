<script setup>
import { formatDate } from '@/shared/utils/date'

defineProps({
  operators: { type: Array, required: true },
  columns: { type: Array, required: true }, // [{ key: 'dateSortie', label: 'Date de sortie', format: 'date' }]
})

function cellValue(op, col) {
  const raw = col.key.split('.').reduce((obj, k) => obj?.[k], op)
  if (col.format === 'date') return formatDate(raw)
  if (col.format === 'boolean') return raw ? '✅ Oui' : '❌ Non'
  return raw ?? '—'
}
</script>

<template>
  <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr>
          <th v-for="col in columns" :key="col.key">{{ col.label }}</th>
          <th v-if="$slots.actions">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="op in operators" :key="op.matricule">
          <td v-for="col in columns" :key="col.key">{{ cellValue(op, col) }}</td>
          <td v-if="$slots.actions"><slot name="actions" :operator="op" /></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>