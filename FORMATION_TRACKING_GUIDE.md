# Formation Tracking System Documentation

## Overview
The ILU System now includes a comprehensive Formation Tracking System that allows managers to track daily training progress for operators across different workstations. The system includes template management, daily tracking with cadence and defect logging, and visual analytics.

## Key Features

### 1. Formation Templates
- Each workstation (Poste) has a formation template defining:
  - **Cadence Objectif**: Target cadence (e.g., 120 pieces/day)
  - **Objectif Qualité**: Quality objective (e.g., "< 7 defects in 12 days")
- Templates can be created/updated by Superviseur, RH, or Admin

### 2. Formation Creation
- Chef d'Équipe (Team Lead) can create formations by:
  - Selecting an Operator
  - Selecting a Poste (with Primaire/Secondaire flag)
  - Formation automatically inherits template objectives from the selected Poste
- Formations are associated with a specific Project

### 3. Daily Tracking
- Track for each of 12 days:
  - **Cadence Réalisée** (Actual Cadence): Pieces produced per day
  - **Number of Defects**: Quality tracking
  - **Remarks**: Additional notes
- Display columns: J1 through J12, with Moyennes (averages) and Totals

### 4. Chart Visualization
- Line chart displays:
  - **Cadence Réalisée** (blue line): Actual daily cadence
  - **Cadence Objectif du Poste** (green line): Target cadence (constant)
  - X-axis: Days J1-J12
  - Y-axis: Cadence values

### 5. Statistics Dashboard
- Cadence Average: Overall average cadence across tracked days
- Total Defects: Sum of all defects across 12 days
- Days with Data: How many days have been tracked
- Quality Objective Status: Whether quality target has been met

## Database Schema

### New Tables

#### `FORMATION_TEMPLATE`
```sql
CREATE TABLE FORMATION_TEMPLATE (
    id_template BIGINT PRIMARY KEY AUTO_INCREMENT,
    poste_id BIGINT NOT NULL UNIQUE,
    cadence_objectif INT NOT NULL,
    qualite_objectif_texte VARCHAR(255) NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    cree_par VARCHAR(100) NOT NULL,
    date_modification DATETIME,
    modifie_par VARCHAR(100),
    FOREIGN KEY (poste_id) REFERENCES POSTE(id_poste)
);
```

### Modified Tables

#### `AFFECTATION_FORMATION`
- Added column: `qualite_objectif VARCHAR(255)` - Stores quality objective inherited from template

## Backend API Endpoints

### Formation Templates

#### Create or Update Formation Template
```
POST /api/formations/templates
Authorization: SUPERVISEUR, RH, ADMIN
Body: {
  "posteId": 1,
  "cadenceObjectif": 120,
  "qualiteObjectifTexte": "< 7 défauts en 12 jours"
}
Response: FormationTemplate
```

#### Get Template by Poste
```
GET /api/formations/templates/{posteId}
Response: FormationTemplate
```

#### Get All Templates
```
GET /api/formations/templates
Response: List<FormationTemplate>
```

### Formation Tracking

#### Get Formation Details with Tracking Data
```
GET /api/formations/{affectationId}/details
Response: FormationDetailsDto {
  idAffectation: Long,
  operateurMatricule: String,
  operateurNom: String,
  operateurPrenom: String,
  posteId: Long,
  posteNom: String,
  projetId: Long,
  projetNom: String,
  cadenceObjectif: Integer,
  qualiteObjectif: String,
  estAffectationPrimaire: Boolean,
  statut: String,
  dateDebut: LocalDate,
  dateEvaluationPrevue: LocalDate,
  dailyTrackings: List<DailyTrackingDto>,
  statistics: FormationStatisticsDto
}
```

#### Get Chart Data
```
GET /api/formations/{affectationId}/chart-data
Response: ChartDataDto {
  labels: [1, 2, 3, ..., 12],
  cadenceObjectifDataset: {
    label: "Cadence objectif du poste",
    data: [120, 120, 120, ...],
    borderColor: "#00a86b",
    ...
  },
  cadenceRealiseeDataset: {
    label: "Cadence réalisée",
    data: [95, 110, 120, ...],
    borderColor: "#0066cc",
    ...
  }
}
```

#### Record Daily Tracking
```
PUT /api/formations/{affectationId}/daily/{day}
Authorization: CHEF_EQUIPE, SUPERVISEUR, RH, ADMIN
Body: {
  "cadenceRealisee": 115,
  "nbDefauts": 1,
  "remarques": "Good progress"
}
Response: DailyTrackingDto {
  idSuivi: Long,
  jour: Integer,
  cadenceRealisee: Integer,
  nbDefauts: Integer,
  remarques: String
}
```

## Frontend Routes

| Route | Component | Purpose |
|-------|-----------|---------|
| `/formations` | FormationsListView | View all formations with filters |
| `/formations/new` | CreateFormationView | Create new formation |
| `/formations/tracking/:id` | FormationTrackingView | Track daily data and view analytics |
| `/formations/templates` | FormationTemplatesView | Manage formation templates |

## Frontend Components

### FormationsListView
- Lists all formations for the current team
- Filters by status, project, and type
- Shows statistics summary (cadence average, defects, progress)
- Links to individual formation tracking

### FormationTrackingView
- Displays formation header with operator and poste info
- Shows cadence chart (realizée vs objectif)
- Displays statistics cards (average cadence, total defects, quality status)
- Daily tracking table (J1-J12) with editable inputs
- Auto-save functionality for daily entries

### CreateFormationView
- Operator selection
- Poste selection with auto-display of objectives
- Formation type selection (Primary/Secondary)
- Project selection
- Auto-loads template objectives for selected poste

### FormationTemplatesView
- List all formation templates
- Create/Edit templates
- Delete templates

## Data Models (DTOs)

### FormationDetailsDto
Complete formation information with tracking data and statistics.

### DailyTrackingDto
Single day's tracking data (cadence, defects, remarks).

### FormationStatisticsDto
Calculated statistics for a formation:
- `cadenceMoyenne`: Average cadence across tracked days
- `totalDefauts`: Sum of all defects
- `daysWithData`: Number of days with recorded data
- `percentageOfDaysWithData`: Percentage of 12 days covered
- `qualityObjectifMet`: Whether quality objective is met

### ChartDataDto
Chart data formatted for Chart.js visualization.

## Usage Flow

### 1. Setup Phase
1. Admin/Superviseur creates formation templates for each poste
2. Define cadence objectives and quality standards for each poste

### 2. Training Phase
1. Chef d'Équipe creates a formation for an operator on a selected poste
2. Formation automatically inherits template objectives
3. Chef d'Équipe records daily progress (cadence and defects)
4. System automatically calculates statistics and displays charts

### 3. Evaluation Phase
1. View complete 12-day tracking with visual analytics
2. Check if cadence and quality objectives are met
3. Export data for evaluation reports

## Security

### Authorization Levels
- **Template Management**: SUPERVISEUR, RH, ADMIN
- **Formation Creation**: CHEF_EQUIPE, SUPERVISEUR, RH, ADMIN
- **Daily Tracking Input**: CHEF_EQUIPE, SUPERVISEUR, RH, ADMIN
- **View Formation Details**: Any authenticated user

## Database Migration

Run the migration script to create new tables:
```bash
mysql -u [user] -p [database] < formation_schema_migration.sql
```

## Future Enhancements

1. **Export to PDF**: Generate formation tracking reports
2. **Bulk Template Upload**: CSV import for templates
3. **Performance Alerts**: Automatic notifications when objectives not met
4. **Historical Comparison**: Compare current formations with past performance
5. **Operator Profiling**: Track operator performance across multiple formations
6. **Custom Objectives**: Allow per-formation objective customization

## Troubleshooting

### Chart Not Displaying
- Ensure Chart.js and vue-chartjs are installed: `npm install chart.js vue-chartjs`
- Check browser console for errors

### Templates Not Loading
- Verify formation_template table exists in database
- Check that PosteTravail records exist
- Ensure user has proper permissions

### Daily Tracking Not Saving
- Verify user has CHEF_EQUIPE or admin role
- Check that affectation_formation record exists
- Ensure suivi_formation_journalier table has proper structure

## Support
For issues or questions about the Formation Tracking System, contact the development team.
