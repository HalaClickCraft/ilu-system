const jsonHeaders = (token) => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${token}`,
})

async function request(url, token, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: (options.method && options.method !== 'GET') ? jsonHeaders(token) : { Authorization: `Bearer ${token}` },
  })

  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(data.message || 'Une erreur est survenue.')
  }
  return data
}

/**
 * Ajoute une formation sur un nouveau poste pour un opérateur qui existe déjà.
 * Le backend détecte tout seul s'il s'agit d'une affectation primaire ou secondaire
 * selon qu'il a déjà (ou non) une affectation primaire en cours.
 */
export function initializeFormation(token, { operateurMatricule, posteId, projetId }) {
  return request('/api/formations/initialize', token, {
    method: 'POST',
    body: JSON.stringify({ operateurMatricule, posteId, projetId }),
  })
}