export async function login({ matricule, motDePasse }) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ matricule, motDePasse }),
  })

  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    throw new Error(data.message || 'Connexion impossible')
  }

  return data
}

export async function changePasswordApi(token, { ancienMotDePasse, nouveauMotDePasse }) {
  const response = await fetch('/api/auth/change-password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ ancienMotDePasse, nouveauMotDePasse }),
  })

  if (!response.ok) {
    let errMsg = 'Changement de mot de passe impossible'
    try {
      const data = await response.json()
      errMsg = data.message || errMsg
    } catch (e) {}
    throw new Error(errMsg)
  }
}
