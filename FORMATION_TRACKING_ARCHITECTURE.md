# Formation Tracking System - Architecture & Design

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Vue.js)                       │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  FormationsListView         - List & filter formations │  │
│  │  FormationTrackingView      - Daily tracking & charts  │  │
│  │  CreateFormationView        - Create new formations    │  │
│  │  FormationTemplatesView     - Manage templates         │  │
│  └────────────────────────────────────────────────────────┘  │
│           ↓ (HTTP Requests with JWT)                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    API Layer (REST)                           │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  TrainingController                                     │  │
│  │  ├─ POST /api/formations/templates                      │  │
│  │  ├─ GET /api/formations/templates/{id}                  │  │
│  │  ├─ GET /api/formations/{id}/details                    │  │
│  │  ├─ GET /api/formations/{id}/chart-data                │  │
│  │  └─ PUT /api/formations/{id}/daily/{day}               │  │
│  └────────────────────────────────────────────────────────┘  │
│           ↓                                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Business Logic Layer (Services)                  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  FormationService                                       │  │
│  │  ├─ createOrUpdateTemplate()                            │  │
│  │  ├─ getFormationDetails()                               │  │
│  │  ├─ getChartData()                                      │  │
│  │  ├─ calculateStatistics()                               │  │
│  │  └─ recordDailyTracking()                               │  │
│  └────────────────────────────────────────────────────────┘  │
│           ↓                                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│          Data Access Layer (Repositories)                     │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  FormationTemplateRepository                            │  │
│  │  SuiviFormationJournalierRepository (Enhanced)           │  │
│  │  AffectationFormationRepository                         │  │
│  │  PosteTravailRepository                                 │  │
│  └────────────────────────────────────────────────────────┘  │
│           ↓                                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Database Layer (MySQL)                           │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  FORMATION_TEMPLATE                                     │  │
│  │  ├─ id_template (PK)                                    │  │
│  │  ├─ poste_id (FK) → POSTE                               │  │
│  │  ├─ cadence_objectif                                    │  │
│  │  └─ qualite_objectif_texte                              │  │
│  ├─ AFFECTATION_FORMATION (Modified)                         │  │
│  │  ├─ id_affectation (PK)                                 │  │
│  │  ├─ qualite_objectif (New field)                        │  │
│  │  └─ ... existing fields ...                             │  │
│  ├─ SUIVI_FORMATION_JOURNALIER                              │  │
│  │  ├─ id_suivi (PK)                                       │  │
│  │  ├─ id_affectation (FK) → AFFECTATION_FORMATION         │  │
│  │  ├─ jour (1-12)                                         │  │
│  │  ├─ cadence_realisee                                    │  │
│  │  ├─ nb_defauts                                          │  │
│  │  └─ remarques                                           │  │
│  └─ POSTE, OPERATEUR, PROJECT (Existing)                    │  │
│  └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow Diagrams

### 1. Formation Creation Flow
```
User Selects Operator & Poste
        ↓
FormationTemplateRepository.findByPoste()
        ↓
Template Loaded (cadence_objectif, qualite_objectif_texte)
        ↓
POST /api/formations/initialize
        ↓
OperateurService.initializeTraining()
        ↓
AffectationFormation Created (qualite_objectif set from template)
        ↓
12 Empty SuiviFormationJournalier Records Initialized
        ↓
Formation Ready for Daily Tracking
```

### 2. Daily Tracking Flow
```
User Opens Formation Tracking View
        ↓
GET /api/formations/{id}/details
        ↓
FormationService.getFormationDetails()
        ↓
Load Affectation + User Info
Load All SuiviFormationJournalier Records
Calculate Statistics
Create FormationDetailsDto
        ↓
Return Formation Data + Chart Data
        ↓
Chart Rendered (Line Chart: Cadence réalisée vs Cadence objectif)
Table Displayed (J1-J12 with editable cells)
        ↓
User Edits Day 1-12:
  - cadence_realisee
  - nb_defauts
  - remarques
        ↓
PUT /api/formations/{id}/daily/{day}
        ↓
FormationService.recordDailyTracking()
        ↓
SuiviFormationJournalier Saved/Updated
        ↓
FormationDetailsDto Recalculated
GET /api/formations/{id}/details Again
        ↓
Chart & Statistics Updated
```

### 3. Statistics Calculation Flow
```
SuiviFormationJournalier Records (J1-J12)
        ↓
Filter records with cadence_realisee != null
        ↓
Calculate Cadence Average = Sum(cadence_realisee) / count
        ↓
Calculate Total Defauts = Sum(nb_defauts)
        ↓
Calculate Days With Data = count(records with data)
        ↓
Calculate Percentage = (daysWithData / 12) * 100
        ↓
Parse Quality Objective (e.g., "< 7 défauts")
        ↓
Check: totalDefauts <= quality_limit
        ↓
FormationStatisticsDto Created
```

### 4. Chart Data Preparation Flow
```
AffectationFormation (cadence_objectif from poste)
        ↓
SuiviFormationJournalier Records (1-12)
        ↓
FOR day = 1 TO 12:
  - Add cadence_objectif to green line dataset
  - Find SuiviFormationJournalier for day
  - Add cadence_realisee (or null) to blue line dataset
        ↓
Chart Datasets:
  Green Line (constant): [100, 100, 100, ..., 100]
  Blue Line (variable): [95, 110, 102, null, 98, ...]
        ↓
ChartDataDto with Chart.js Format:
{
  labels: ["J1", "J2", ..., "J12"],
  datasets: [
    { label: "Cadence objectif du poste", ... },
    { label: "Cadence réalisée", ... }
  ]
}
        ↓
Frontend Renders Chart
```

## Component Interactions

### FormationTrackingView ↔ FormationService
```
FormationTrackingView
├─ onMounted()
│  └─ loadFormationDetails()
│     └─ GET /api/formations/{id}/details
│        └─ FormationService.getFormationDetails()
│           ├─ Load AffectationFormation
│           ├─ Load SuiviFormationJournalier records
│           ├─ Calculate statistics
│           └─ Return FormationDetailsDto
│
├─ loadFormationDetails()
│  └─ GET /api/formations/{id}/chart-data
│     └─ FormationService.getChartData()
│        ├─ Get affectation + poste
│        ├─ Build cadence datasets
│        └─ Return ChartDataDto
│
└─ saveDay(day)
   └─ PUT /api/formations/{id}/daily/{day}
      └─ FormationService.recordDailyTracking()
         ├─ Find or create SuiviFormationJournalier
         ├─ Update cadence/defects/remarks
         └─ Return DailyTrackingDto
```

## Security & Authorization

```
User Request
     ↓
JWT Validation (JwtAuthFilter)
     ↓
Extract User Details & Roles
     ↓
@PreAuthorize Annotations
     ├─ Template Management: SUPERVISEUR, RH, ADMIN
     ├─ Formation Creation: CHEF_EQUIPE, SUPERVISEUR, RH, ADMIN
     ├─ Daily Tracking: CHEF_EQUIPE, SUPERVISEUR, RH, ADMIN
     └─ View Formations: Any Authenticated User
     ↓
If Authorized:
  └─ Process Request → Return Data
Else:
  └─ Return 403 Forbidden
```

## Error Handling Flow

```
Request to Endpoint
     ↓
Try to Process Request
     ↓
If Error Occurs:
  ├─ FormationTemplate Not Found → HTTP 404
  ├─ Permission Denied → HTTP 403
  ├─ Invalid Data → HTTP 400
  ├─ Database Error → HTTP 500
  ├─ Missing Parameter → HTTP 400
  └─ Other → HTTP 500
     ↓
Return Error Response with Message
     ↓
Frontend Catches Error
     ↓
Display Error Alert to User
```

## Database Schema Relationships

```
                    ┌─────────────┐
                    │   POSTE     │
                    │ (Workstation)│
                    └──────┬──────┘
                           │
                           │ 1:1
                           ↓
        ┌─────────────────────────────────┐
        │  FORMATION_TEMPLATE             │
        │  (Training Objectives)          │
        │ ├─ cadence_objectif             │
        │ └─ qualite_objectif_texte       │
        └─────────────────────────────────┘


        ┌──────────────┐
        │  OPERATEUR   │
        └──────┬───────┘
               │
               │ 1:N
               ↓
    ┌──────────────────────────────┐
    │ AFFECTATION_FORMATION        │
    │ (Training Assignment)        │
    │ ├─ cadence_objectif (copied) │
    │ └─ qualite_objectif (copied) │
    └──────────┬───────────────────┘
               │
               │ 1:N
               ↓
    ┌──────────────────────────────────┐
    │ SUIVI_FORMATION_JOURNALIER       │
    │ (Daily Tracking)                 │
    │ ├─ jour (1-12)                   │
    │ ├─ cadence_realisee              │
    │ ├─ nb_defauts                    │
    │ └─ remarques                     │
    └──────────────────────────────────┘

    ┌──────────────┐
    │  PROJECT     │
    └──────┬───────┘
           │ 1:N
           ↓
    ┌──────────────────────────────┐
    │ AFFECTATION_FORMATION        │
    │ (Associates with Project)    │
    └──────────────────────────────┘
```

## Key Design Decisions

1. **Template Pattern**: Postes have templates that define objectives once, applied to all formations
2. **12-Day Training**: Hard-coded as industry standard, tracks daily progress
3. **Statistics on Read**: Calculations done when retrieving data, not pre-calculated
4. **Soft Error Handling**: Missing defect entries default to 0, missing cadence shows as null
5. **Quality Threshold**: Parsed from text, flexible format ("< 7 défauts")
6. **Audit Trail**: Track who created/modified templates and who entered daily data
7. **Chart.js Integration**: Industry-standard charting library with vue-chartjs wrapper

## Performance Considerations

- **Indexes**: Created on frequently queried columns
- **Lazy Loading**: Relations set to LAZY where appropriate
- **DTO Pattern**: Only necessary data returned to frontend
- **Caching**: Templates can be cached (static objectives)
- **Pagination**: Consider for large formation lists (future enhancement)

## Testing Strategy

1. **Unit Tests**: Test FormationService statistics calculations
2. **Integration Tests**: Test repositories and service layer
3. **E2E Tests**: Test complete flow from UI to database
4. **Chart Rendering**: Manual testing in browsers
5. **Authorization**: Test role-based access control

---

**Document Version**: 1.0  
**Last Updated**: 2026-07-20
