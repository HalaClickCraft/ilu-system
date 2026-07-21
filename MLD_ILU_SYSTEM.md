# MLD - Modèle Logique de Données (ILU System)

**Version**: 1.0  
**Date**: 2026-07-20  
**Projet**: ILU System - Training Management Platform

---

## 📊 Vue d'ensemble du système

Le système ILU gère la formation des opérateurs dans une chaîne de production. Il suit:

- Les projets de fabrication
- Les zones de production et les postes de travail
- Les équipes et les opérateurs
- Les formations et l'évolution de leurs compétences
- Le suivi quotidien avec tableau de cadence et défauts

---

## 📋 Tables de base de données

### 1. **ROLES** - Rôles utilisateur

```
┌─────────────────────────────────┐
│          ROLES                  │
├─────────────────────────────────┤
│ PK  id INT                      │
│     libelle VARCHAR(50)         │
└─────────────────────────────────┘
```

**Données**: ADMIN, CHEF_EQUIPE, RH, QUALITE, AGENT_QUALITE, SUPERVISEUR, HSE, RESPONSABLE_QUALITE

---

### 2. **UTILISATEURS** - Utilisateurs du système

```
┌──────────────────────────────────┐
│       UTILISATEURS               │
├──────────────────────────────────┤
│ PK  id BIGINT (AUTO)             │
│ UK  matricule VARCHAR(50)        │
│     nom VARCHAR(255)             │
│     cin VARCHAR(20)              │
│     password VARCHAR(255)        │
│     doit_changer_mdp BOOLEAN     │
│     actif BOOLEAN                │
│ FK  role_id INT                  │
│         → ROLES.id               │
└──────────────────────────────────┘
```

**Rôles**: Administrateurs, Chefs d'équipe, RH, Qualité, Superviseurs

---

### 3. **PROJET** - Projets de fabrication

```
┌─────────────────────────────────┐
│         PROJET                  │
├─────────────────────────────────┤
│ PK  id_projet INT (AUTO)        │
│     nom VARCHAR(255)            │
│     logo VARCHAR(500)           │
│     cree_par VARCHAR(100)       │
│     date_creation DATETIME      │
└─────────────────────────────────┘
```

**Exemples**: Renault Clio, Peugeot 208

---

### 4. **ZONE_LIGNE** - Zones de production

```
┌──────────────────────────────────┐
│        ZONE_LIGNE                │
├──────────────────────────────────┤
│ PK  id_zone INT (AUTO)           │
│     nom VARCHAR(100)             │
│     cree_par VARCHAR(100)        │
│ FK  projet_id INT                │
│         → PROJET.id_projet       │
│     date_creation DATETIME       │
└──────────────────────────────────┘
```

**Structure**: Projet → Zones (Zone A Assemblage, Zone B Finition)

---

### 5. **POSTE** - Postes de travail

```
┌──────────────────────────────────┐
│         POSTE                    │
├──────────────────────────────────┤
│ PK  id_poste INT (AUTO)          │
│     nom VARCHAR(100)             │
│     type_poste VARCHAR(50)       │
│     cadence_objectif INT         │
│     cible_polyvalence INT        │
│     cree_par VARCHAR(100)        │
│ FK  zone_id INT                  │
│         → ZONE_LIGNE.id_zone     │
│     date_creation DATETIME       │
└──────────────────────────────────┘
```

**Attributs clés**:

- `cadence_objectif`: Cadence cible (ex: 100, 120, 80)
- `type_poste`: PRODUCTION, CONTROLE, etc.
- `cible_polyvalence`: Nombre de postes à maîtriser

---

### 6. **FORMATION_TEMPLATE** - Modèles de formation par poste

```
┌──────────────────────────────────┐
│     FORMATION_TEMPLATE           │
├──────────────────────────────────┤
│ PK  id INT (AUTO)                │
│ FK  poste_id INT                 │
│         → POSTE.id_poste         │
│     cadence_objectif INT         │
│     qualite_objectif_texte TEXT  │
│     cree_par VARCHAR(100)        │
│     date_creation DATETIME       │
└──────────────────────────────────┘
```

**Objectifs de qualité**:

- Assemblage: "< 7 défauts en 12 jours"
- Vissage: "< 5 défauts en 12 jours"
- Finition: "< 10 défauts en 12 jours"

---

### 7. **EQUIPE** - Équipes de production

```
┌──────────────────────────────────┐
│         EQUIPE                   │
├──────────────────────────────────┤
│ PK  id_equipe INT (AUTO)         │
│     nom VARCHAR(100)             │
│ FK  chef_id BIGINT               │
│         → UTILISATEURS.id        │
│ FK  projet_id INT                │
│         → PROJET.id_projet       │
│     date_creation DATETIME       │
└──────────────────────────────────┘
```

**Exemple**: Équipe Assemblage (Chef: Bob)

---

### 8. **OPERATEUR** - Opérateurs de production

```
┌──────────────────────────────────┐
│       OPERATEUR                  │
├──────────────────────────────────┤
│ PK  matricule VARCHAR(50)        │
│     nom VARCHAR(100)             │
│     prenom VARCHAR(100)          │
│     fonctionnalite VARCHAR(100)  │
│     date_embauche DATE           │
│     date_sortie DATE             │
│     statut VARCHAR(50)           │
│     formation_rework BOOLEAN     │
│ FK  equipe_id INT                │
│         → EQUIPE.id_equipe       │
│ FK  poste_affecte_id INT         │
│         → POSTE.id_poste         │
│     date_creation DATETIME       │
└──────────────────────────────────┘
```

**Données**: OP001 (Dupont Jean), OP002 (Martin Sophie), OP003 (Bernard Pierre)

---

### 9. **AFFECTATION_FORMATION** - Formations des opérateurs

```
┌──────────────────────────────────┐
│    AFFECTATION_FORMATION         │
├──────────────────────────────────┤
│ PK  id_affectation BIGINT        │
│ FK  operateur_matricule VARCHAR  │
│         → OPERATEUR.matricule    │
│ FK  id_poste INT                 │
│         → POSTE.id_poste         │
│ FK  id_projet INT                │
│         → PROJET.id_projet       │
│     est_affectation_primaire BOO │
│     statut VARCHAR(50)           │
│     date_debut DATE              │
│     date_evaluation_prevue DATE  │
│ FK  cree_par BIGINT              │
│         → UTILISATEURS.id        │
│     qualite_objectif VARCHAR(255)│
│     date_creation DATETIME       │
│                                  │
│ UNIQUE(operateur_matricule,     │
│        id_poste, id_projet)      │
└──────────────────────────────────┘
```

**Statuts**: EN_FORMATION, EVALUEE, VALIDEE, ECHOUEE

---

### 10. **SUIVI_FORMATION_JOURNALIER** - Suivi quotidien

```
┌────────────────────────────────────┐
│  SUIVI_FORMATION_JOURNALIER        │
├────────────────────────────────────┤
│ PK  id_suivi BIGINT (AUTO)         │
│ FK  id_affectation BIGINT          │
│         → AFFECTATION_FORMATION    │
│     jour INT (1-12)                │
│     cadence_realisee INT           │
│     nb_defauts INT                 │
│     remarques TEXT                 │
│ FK  saisie_par BIGINT              │
│         → UTILISATEURS.id          │
│     date_saisie DATETIME           │
│                                    │
│ UNIQUE(id_affectation, jour)       │
└────────────────────────────────────┘
```

**Exemple de données**:

- Jour 1: cadence=60, défauts=5
- Jour 2: cadence=75, défauts=3
- ...
- Jour 12: cadence=94, défauts=0

---

### 11. **PROJET_MEMBRE** - Membres du projet

```
┌──────────────────────────────────┐
│      PROJET_MEMBRE               │
├──────────────────────────────────┤
│ PK  id INT (AUTO)                │
│ FK  projet_id INT                │
│         → PROJET.id_projet       │
│ FK  utilisateur_id BIGINT        │
│         → UTILISATEURS.id        │
│     role_projet VARCHAR(50)      │
│     date_ajout DATETIME          │
└──────────────────────────────────┘
```

---

### 12. **FORMATION_POSTE** - Formations par poste

```
┌────────────────────────────────────┐
│      FORMATION_POSTE               │
├────────────────────────────────────┤
│ PK  id INT (AUTO)                  │
│ FK  affectation_formation_id BIGINT│
│         → AFFECTATION_FORMATION    │
│ FK  poste_id INT                   │
│         → POSTE.id_poste           │
│     ordre_poste INT                │
│     date_debut DATE                │
│     date_fin DATE                  │
│     statut VARCHAR(50)             │
│     date_creation DATETIME         │
└────────────────────────────────────┘
```

---

## 🔗 Diagramme des relations

```
                    ┌─────────────┐
                    │   ROLES     │
                    └──────┬──────┘
                           │ 1
                           │
                           │ *
                    ┌──────▼──────────┐
                    │  UTILISATEURS   │
                    │ (id, matricule) │
                    └──────┬──────────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
            │ 1            │ 1            │ 1
            │              │              │
    ┌───────▼──────┐  ┌───▼────────────┐ ┌──┴──────────────┐
    │ AFFECTATION_ │  │   EQUIPE       │ │ PROJET_MEMBRE  │
    │ FORMATION    │  │  (chef_id)     │ │(utilisateur_id)│
    └───┬──────────┘  └───┬────────────┘ └───────┬────────┘
        │                 │                      │
        │ *               │ *                     │
        │                 │                       │
    ┌───▼──────────────────▼─────────────────────▼─┐
    │              PROJET                          │
    │        (id_projet, nom)                      │
    └─────────────┬──────────────┬─────────────────┘
                  │              │ 1
                  │ *            │
              ┌───▼──────────┐   │
              │ ZONE_LIGNE   │   │
              └───┬──────────┘   │
                  │ 1            │
                  │              │
                  │ *            │
              ┌───▼──────────────▼──┐
              │    POSTE            │
              │ (id_poste, nom)     │
              └─────────────────────┘
                      │ 1
                      │
                      │ *
              ┌───────▼──────────────┐
              │ FORMATION_TEMPLATE   │
              │ (cadence, qualite)   │
              └──────────────────────┘


┌──────────────────────────────────────────┐
│       AFFECTATION_FORMATION              │
│    (Linking Formation Tracking)          │
└────┬──────────────────────────────────┬──┘
     │ 1                                │ 1
     │                                  │
     │ *                                │ *
┌────▼──────────┐          ┌───────────▼────────┐
│   OPERATEUR   │          │ FORMATION_POSTE    │
│ (matricule)   │          │                    │
└───────────────┘          └───────────────────┘
                                   │ 1
                                   │
                                   │ *
                        ┌──────────▼──────────────┐
                        │SUIVI_FORMATION_         │
                        │JOURNALIER              │
                        │(J1-J12 tracking)      │
                        └───────────────────────┘
```

---

## 📊 Flux de données principal

### Cycle de formation:

```
1. ADMIN/RH crée PROJET
                 ↓
2. ADMIN crée ZONE dans le PROJET
                 ↓
3. ADMIN crée POSTE dans la ZONE
                 ↓
4. SYSTEM crée FORMATION_TEMPLATE pour le POSTE
                 ↓
5. ADMIN crée EQUIPE avec CHEF_EQUIPE
                 ↓
6. CHEF_EQUIPE ajoute OPERATEURS à l'EQUIPE
                 ↓
7. CHEF_EQUIPE crée AFFECTATION_FORMATION
   (Opérateur + Poste + Primaire/Secondaire)
                 ↓
8. CHEF_EQUIPE remplit SUIVI_FORMATION_JOURNALIER
   (Cadence Réalisée + Défauts) pour J1-J12
                 ↓
9. Système calcule:
   - Moyenne cadence
   - Total défauts
   - Comparaison vs Objectif Qualité
                 ↓
10. AGENT_QUALITE évalue et change statut
    (EVALUEE → VALIDEE ou ECHOUEE)
```

---

## 🎯 Cas d'usage principaux

### 1. **Créer une Formation**

- Sélectionner Opérateur
- Sélectionner Poste (Primaire/Secondaire)
- Formation hérite des objectifs du Poste
- Crée une AFFECTATION_FORMATION

### 2. **Suivre la Progression Quotidienne**

- Pour chaque jour (J1-J12):
  - Saisir Cadence Réalisée (ex: 60, 75, 82, ...)
  - Saisir Nombre de Défauts (0, 1, 2, ...)
  - Enregistrer dans SUIVI_FORMATION_JOURNALIER

### 3. **Générer Diagramme**

- Récupérer toutes les données SUIVI_FORMATION_JOURNALIER pour une AFFECTATION_FORMATION
- Afficher graphique:
  - Cadence Objectif (ligne verte constante)
  - Cadence Réalisée (ligne bleue variable)
- X-axis: J1, J2, ..., J12
- Y-axis: Valeurs de cadence

### 4. **Évaluer la Formation**

- Vérifier si Objectif Qualité est atteint
  - Ex: "< 7 défauts en 12 jours"
- Moyenne cadence ≥ Cadence Objectif du Poste
- Changer statut: EN_FORMATION → VALIDEE/ECHOUEE

---

## 📈 Statistiques disponibles

Pour chaque formation (AFFECTATION_FORMATION):

```
┌─ Cadence ─────────────────────┐
│ • Cadence Objectif du Poste   │  (constant: 100, 120, 80...)
│ • Cadence Réalisée (J1-J12)   │  (variable: 60, 75, 82...)
│ • Moyenne Cadence              │  (sum / 12 days)
│ • Min/Max Cadence              │
└───────────────────────────────┘

┌─ Qualité ─────────────────────┐
│ • Objectif Qualité Texte      │  "< 7 défauts en 12 jours"
│ • Défauts par Jour (J1-J12)   │  (0, 0, 0, 1, 1, 0...)
│ • Total Défauts (12 days)     │  (sum of défauts)
│ • Objectif Atteint?            │  (YES/NO)
└───────────────────────────────┘

┌─ Statut ──────────────────────┐
│ • EN_FORMATION                │
│ • EVALUEE                     │
│ • VALIDEE (✓ pass)            │
│ • ECHOUEE (✗ fail)            │
└───────────────────────────────┘
```

---

## 🔐 Sécurité & Accès

| Rôle          | Accès                                                   |
| ------------- | ------------------------------------------------------- |
| ADMIN         | Tous les projets, zones, postes, formations             |
| CHEF_EQUIPE   | Crée formations, saisit suivi quotidien pour son équipe |
| AGENT_QUALITE | Évalue et valide les formations                         |
| SUPERVISEUR   | Consulte les formations et statistiques                 |
| RH            | Gère les opérateurs et équipes                          |
| QUALITE       | Définit les objectifs et templates                      |

---

## 📝 Exemple de données (Formation complète)

```
AFFECTATION_FORMATION #1:
├─ Opérateur: OP001 (Dupont Jean)
├─ Poste: Vissage (Cadence Objectif: 120)
├─ Type: SECONDAIRE
├─ Statut: EN_FORMATION
├─ Objectif Qualité: < 5 défauts en 12 jours
│
└─ SUIVI_FORMATION_JOURNALIER (J1-J12):
   ├─ J1:  Cadence=60,  Défauts=5
   ├─ J2:  Cadence=75,  Défauts=3
   ├─ J3:  Cadence=82,  Défauts=2
   ├─ J4:  Cadence=88,  Défauts=1
   ├─ J5:  Cadence=92,  Défauts=1
   ├─ J6:  Cadence=95,  Défauts=0
   ├─ J7:  Cadence=94,  Défauts=1
   ├─ J8:  Cadence=93,  Défauts=0
   ├─ J9:  Cadence=95,  Défauts=1
   ├─ J10: Cadence=96,  Défauts=0
   ├─ J11: Cadence=95,  Défauts=0
   └─ J12: Cadence=94,  Défauts=0

   Statistiques:
   - Moyenne Cadence: 88.75 (✓ ≈ Objectif 120)
   - Total Défauts: 4 (✓ < 5)
   - Résultat: VALIDEE ✓
```

---

## 🚀 Évolutions futures

1. **Multi-formations simultanées** - Un opérateur peut suivre plusieurs formations
2. **Réévaluation** - Possibilité de reprendre une formation échouée
3. **Certifications** - Système de badges après formations validées
4. **Analytics** - Tableaux de bord par poste, équipe, projet
5. **Export** - Générer rapports PDF des formations

---

**Fin du MLD**
