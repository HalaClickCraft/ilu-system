# ILU System Setup & Data Management Guide

## Current Status ✅

All containers are running and ready:

- **Backend**: http://localhost:8080 (Spring Boot API)
- **Frontend**: http://localhost:3000 (Nginx + Vue.js)
- **Database**: MySQL on port 3306 (ilu_db)

## System Architecture

### Database Tables (Auto-created by Hibernate)

Tables are created automatically by Hibernate (`create-drop` mode in application-docker.properties).

**Core Tables:**

- `roles` - Role definitions (ADMIN, CHEF_EQUIPE, RH, AGENT_QUALITE, SUPERVISEUR, etc.)
- `utilisateurs` - User accounts (employees, managers)
- `projet` - Projects (Projet Renault Clio, Projet Peugeot 208)
- `zone_ligne` - Work zones within projects
- `poste` - Work stations (Assemblage, Vissage, Finition & Polissage)
- `equipe` - Teams with team leads
- `operateur` - Operators/workers
- `affectation_formation` - Training assignments (1 operator → 1 poste at a time)
- `suivi_formation_journalier` - Daily training journal (12 days per assignment)

## Manual Data Management

### How to Insert Data

1. **Connect to MySQL Container:**

   ```bash
   docker exec -it ilu-mysql mysql -u ilu_user -p ilu_db
   # Password: ilu123
   ```

2. **Copy SQL commands from `schema_and_mock_data.sql`:**
   - The file contains ready-to-use INSERT commands
   - Copy individual INSERT statements and paste into MySQL
   - Commands use `ON DUPLICATE KEY UPDATE` to prevent errors if data already exists

3. **Example: Insert a Role**
   ```sql
   INSERT INTO roles (libelle) VALUES ('ADMIN')
   ON DUPLICATE KEY UPDATE libelle=VALUES(libelle);
   ```

### To Add New Mock Data

Edit `schema_and_mock_data.sql` and add your INSERT commands at the end, then execute them manually.

**Template for new operateur:**

```sql
INSERT INTO operateur (matricule, nom, prenom, fonctionnalite, date_embauche, statut, formation_rework, equipe_id, poste_affecte_id)
SELECT 'OP004', 'LastName', 'FirstName', 'Opérateur Production', '2024-04-01', 'ACTIF', 0, e.id_equipe, p.id_poste
FROM equipe e, poste p WHERE e.nom = 'Équipe Assemblage' AND p.nom = 'Assemblage' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);
```

**Template for new training assignment (affectation_formation):**

```sql
INSERT INTO affectation_formation (operateur_matricule, id_poste, id_projet, est_affectation_primaire, statut, date_debut, date_evaluation_prevue, cree_par)
SELECT 'OP004', p.id_poste, pr.id_projet, 1, 'EN_FORMATION', '2026-08-01', '2026-08-13', u.id
FROM poste p, projet pr, utilisateurs u
WHERE p.nom = 'Poste Name' AND pr.nom = 'Projet Name' AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE statut=VALUES(statut);
```

**Template for daily journal entry (suivi_formation_journalier):**

```sql
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par)
SELECT af.id_affectation, 1, 60, 5, 'Your comments here', u.id
FROM affectation_formation af, utilisateurs u, operateur op
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);
```

## Frontend Features

### Training Journal Interface (`/formations`)

- **Search by Matricule**: Enter operator matricule (OP001, OP002, etc.)
- **Auto-load 12-day Table**: Shows all 12 days of training with:
  - Cadence Réalisée (realized cadence)
  - Nombre de Défauts (number of defects)
  - Remarques (comments)
- **Role-based Editing**:
  - **Chef d'Équipe**: Can edit Cadence & Remarques only
  - **Agent Qualité**: Can edit Défauts only
  - **Admin/RH**: Can edit all fields
- **Real-time Chart**: Blue curve (realized cadence) vs Green line (objective cadence)
- **Auto-calculations**: Average cadence, total defects with alerts
- **Save Button**: Single "Sauvegarder le Journal" button to persist all changes

### Dynamic Post Selection

- If operator has multiple trainings → dropdown menu appears
- Select which post's training to view/edit
- Filters applied based on user role (Chef d'Équipe sees only their posts)

## Default Test Users

| Matricule | Nom                   | Role          | Password |
| --------- | --------------------- | ------------- | -------- |
| admin1    | Alice Administratrice | ADMIN         | admin123 |
| chef1     | Bob Chef d'Équipe     | CHEF_EQUIPE   | chef123  |
| rh1       | Carole RH             | RH            | rh123    |
| aq1       | David Agent Qualité   | AGENT_QUALITE | aq123    |
| super1    | Paul Superviseur      | SUPERVISEUR   | super123 |

## Default Test Operators

| Matricule | Nom     | Prenom | Poste Affecté | Équipe            |
| --------- | ------- | ------ | ------------- | ----------------- |
| OP001     | Dupont  | Jean   | Assemblage    | Équipe Assemblage |
| OP002     | Martin  | Sophie | Assemblage    | Équipe Assemblage |
| OP003     | Bernard | Pierre | Assemblage    | Équipe Assemblage |

## Training Data Example

**OP001's Trainings:**

1. **Primary (est_affectation_primaire = TRUE)**: Poste 1 (Assemblage) - Status: VALIDEE
2. **Secondary (est_affectation_primaire = FALSE)**: Poste 2 (Vissage) - Status: EN_FORMATION
   - 12-day journal available (cadence 60→94, defects 5→0)

**OP002's Trainings:**

1. **Primary**: Poste 2 (Vissage) - Status: VALIDEE
2. **Secondary**: Poste 3 (Finition & Polissage) - Status: EN_FORMATION
   - 5-day journal available (cadence 55→76, defects 6→2)

## Docker Management

### Start All Services

```bash
cd C:\Users\ENVY\ilu-system
docker compose up -d
```

### Stop All Services

```bash
docker compose down
```

### Stop & Remove Data

```bash
docker compose down -v
```

### View Logs

```bash
docker logs ilu-backend        # Backend
docker logs ilu-frontend       # Frontend
docker logs ilu-mysql          # Database
```

### Access MySQL Shell

```bash
docker exec -it ilu-mysql mysql -u ilu_user -p ilu_db
```

## Database Configuration

**Docker MySQL:**

- Host: mysql (inside Docker network)
- Port: 3306 (exposed to host)
- Database: ilu_db
- User: ilu_user
- Password: ilu123

**Hibernate Settings** (`application-docker.properties`):

- `ddl-auto=create-drop` - Tables recreated on each startup (dev only)
- `show-sql=true` - SQL logging enabled for debugging

## Notes

- **No automatic DataLoader**: All data is manual. Use `schema_and_mock_data.sql` as your reference.
- **Case Sensitivity**: Table names in uppercase (POSTE, OPERATEUR, etc.)
- **Unique Constraints**:
  - Project names must be unique
  - Operator matricules must be unique
  - Operator-Poste-Project combinations must be unique for training
- **Foreign Keys**: Verify all parent records exist before inserting child records
- **ON DUPLICATE KEY UPDATE**: Safely re-run INSERT commands without errors

## Troubleshooting

### Backend won't start?

Check logs: `docker logs ilu-backend`

### Can't connect to database?

```bash
docker exec -it ilu-mysql ping mysql    # Test connectivity
docker ps                                # Check if mysql container is running
```

### Data not showing in UI?

1. Verify data exists: `SELECT * FROM operateur;`
2. Check that Affectation records exist for the operator
3. Verify Journal entries exist

### Clear All Data & Start Fresh?

```bash
docker compose down -v
docker compose up -d
# Then manually insert data from schema_and_mock_data.sql
```
