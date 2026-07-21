# Formation Tracking System - Quick Start Guide

## 🚀 Getting Started

### For Administrators: Setting Up Formation Templates

1. **Login** with ADMIN, SUPERVISEUR, or RH role
2. **Navigate** to `/formations/templates` in the menu
3. **Click** "Créer un Modèle" (Create Model)
4. **Select** a Poste (Work Station)
5. **Enter** Cadence Objectif (e.g., 120 pieces/day)
6. **Enter** Quality Objective (e.g., "< 7 defects in 12 days")
7. **Click** "Créer" (Create)

**Example Templates:**
| Poste | Cadence | Quality |
|-------|---------|---------|
| Assemblage | 100 | < 7 défauts en 12 jours |
| Vissage | 120 | < 5 défauts en 12 jours |
| Finition & Polissage | 80 | < 10 défauts en 12 jours |

### For Team Leads (Chef d'Équipe): Creating Formations

1. **Login** with CHEF_EQUIPE role
2. **Navigate** to `/formations` → "Créer une Nouvelle Formation"
3. **Select** an Operator from your team
4. **Select** the Poste (training station)
5. **Choose** Type:
   - **Affectation Primaire**: Main job assignment
   - **Affectation Secondaire**: Additional skill training
6. **Select** the Project
7. **Click** "Créer la Formation"
8. **You'll be redirected** to the tracking view

### For Team Leads: Daily Tracking

1. **Navigate** to `/formations` → Select a formation
2. **You'll see:**
   - 📊 Chart showing Cadence (actual vs target)
   - 📋 Daily tracking table (Days 1-12)
   - 📈 Statistics cards
3. **For each day:**
   - **Cadence Réalisée**: Enter actual pieces produced
   - **Défauts**: Enter number of defects
   - **Remarques**: Add optional notes
4. **Click** "Enregistrer" (Save)
5. **Chart updates automatically**

### For HR/Supervisors: Viewing Progress

1. **Navigate** to `/formations`
2. **Apply Filters:**
   - Filter by Status (En Formation, Validée, etc.)
   - Filter by Project
   - Filter by Type (Primary/Secondary)
3. **View Progress Bar** showing % of 12 days completed
4. **Click** "Voir" to see detailed tracking
5. **Export** data if needed for reports

## 📊 Understanding the Dashboard

### Formation List View
```
┌─────────────────────────────────────────────────────────┐
│ Opérateur | Poste | Cadence | Défauts | Statut | Progress │
├─────────────────────────────────────────────────────────┤
│ Dupont J. │ Assem │ 95/100  │ 4      │ ✓ OK   │ ████ 75% │
│ Martin S. │ Vissage│ 112/120 │ 3      │ ⏳ Train│ █████100%│
└─────────────────────────────────────────────────────────┘
```

**Legend:**
- **Cadence Moyenne**: Average pieces/day vs target
- **Défauts**: Total defects (color indicates if target met)
- **Statut**: Training status badge
- **Progress**: % of 12 days with data entered

### Formation Tracking View
```
┌───────────────────────────────────────────────────────┐
│ Opérateur: Jean Dupont (OP001)                        │
│ Poste: Assemblage | Project: Renault Clio            │
│ Statut: ✓ EN_FORMATION                                │
├───────────────────────────────────────────────────────┤
│                                                        │
│ 📊 CHART: Cadence réalisée (blue) vs Objectif (green)│
│    100 ┤     ╱╲    ╱──                                │
│     80 ┤ ╱──╱  ╲──╱                                   │
│        ├─────────────────────────────────            │
│        │ J1 J2 J3 J4 J5 J6 J7 J8 J9...              │
│                                                        │
├───────────────────────────────────────────────────────┤
│ 📈 STATISTICS:                                         │
│ ├─ Cadence Moyenne: 95.2                             │
│ ├─ Défauts Total: 12                                 │
│ ├─ Jours Saisis: 7/12                                │
│ └─ Objectif Qualité: ✗ Non atteint (< 7 défauts)   │
├───────────────────────────────────────────────────────┤
│ 📋 DAILY TRACKING TABLE:                              │
│ Day │ Target │ Actual │ Defects │ Notes              │
│ J1  │  100   │  [_]   │  [_]    │ [________]         │
│ J2  │  100   │  [95]  │  [2]    │ [Good start] Save  │
│ ...                                                    │
└───────────────────────────────────────────────────────┘
```

## 🎯 Common Tasks

### Task 1: Check if Operator is Meeting Quality Objectives
1. Go to `/formations`
2. Look for the formation in the list
3. Check the **Défauts** column:
   - 🟢 Green/Blue badge = On track
   - 🟡 Yellow/Orange badge = Close to limit
   - 🔴 Red badge = Exceeded limit
4. Click to see detailed breakdown

### Task 2: Monitor Cadence Improvement
1. Open the formation tracking view
2. Look at the **Chart** section:
   - Blue line should trend toward green line
   - Flat blue line = needs improvement
   - Rising blue line = good progress
3. Check **Cadence Moyenne** card for overall average

### Task 3: Generate Progress Report
1. Go to `/formations` list
2. Apply filters for desired date range/project/status
3. Copy or screenshot the table
4. View individual formations for detailed charts
5. Export data (consider for future enhancement)

### Task 4: Identify Training Issues Early
**Warning Signs:**
- Blue line (actual) consistently below green line (target)
- Too many defects in early days (J1-J3)
- No data entered after J5 (incomplete tracking)
- Quality objective not being met

**Actions:**
1. Review remarks for the low-performing days
2. Provide additional support/coaching
3. Adjust training schedule if needed
4. Escalate to supervisor if issues persist

## 📱 Mobile Access

The system is responsive and works on mobile devices:
- **Phone**: Horizontal scroll for daily table
- **Tablet**: Full interface visible
- **Desktop**: Recommended for detailed analysis

## ⌨️ Keyboard Shortcuts

- **Tab**: Move between input fields in daily tracking table
- **Enter**: Save current cell and move to next
- **Esc**: Cancel edit (if implementing)

## 🔄 Typical Training Timeline

```
Day 1-3: Learning Phase
└─ Low cadence (50-70% of target)
└─ Higher defects
└─ Focus on understanding process

Day 4-8: Adaptation Phase  
└─ Increasing cadence (70-90% of target)
└─ Decreasing defects
└─ Building confidence and speed

Day 9-12: Mastery Phase
└─ Cadence near target (90-100%+)
└─ Defects meet quality objective
└─ Ready for independent work
```

## 🚨 Troubleshooting

### Chart Not Showing
- **Problem**: Chart appears blank
- **Solution**: 
  1. Refresh page
  2. Ensure data is entered for at least some days
  3. Check browser console for errors

### Data Not Saving
- **Problem**: "Enregistrer" button doesn't work
- **Solution**:
  1. Verify you have CHEF_EQUIPE or admin role
  2. Check internet connection
  3. Reload page and try again
  4. Contact admin if persists

### Can't See Formations
- **Problem**: Formation list is empty
- **Solution**:
  1. Ensure you're logged in
  2. Check filters (clear all filters)
  3. Verify you're part of a team
  4. Contact HR/admin

### Template Not Found When Creating
- **Problem**: "Formation template not found"
- **Solution**:
  1. Go to `/formations/templates`
  2. Create template for that Poste
  3. Return to create formation page

## 📞 Need Help?

1. **Check** `/formations/templates` - ensure template exists
2. **Review** the daily tracking table - all data entered correctly?
3. **Verify** your role/permissions with admin
4. **Check** browser console for error messages
5. **Contact** your IT support team

## 💡 Tips for Best Results

✅ **Do:**
- Enter data daily for better tracking
- Write meaningful remarks for low-performing days
- Review chart progress weekly
- Keep quality standards consistent
- Communicate with operators about objectives

❌ **Don't:**
- Skip days of data entry
- Enter inflated/deflated numbers
- Ignore quality objectives
- Make large changes without notes
- Rush through final days of training

## 📊 Expected Metrics

**Good Formation Results:**
- Final cadence ≥ 95% of target
- Total defects ≤ quality objective
- Steady improvement over 12 days
- Notes showing progression

**Formation to Review:**
- Cadence below 80% of target on day 12
- Total defects > 2x quality objective
- Erratic/no improvement pattern
- Minimal or no notes

---

**Need detailed API documentation?** See `FORMATION_TRACKING_GUIDE.md`  
**Want to understand the system design?** See `FORMATION_TRACKING_ARCHITECTURE.md`  
**Looking for deployment info?** See `FORMATION_TRACKING_IMPLEMENTATION.md`
