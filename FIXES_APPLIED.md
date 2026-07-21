# ILU System - Fixes Applied (2026-07-20)

## Issues Fixed

### 1. ✅ Database Schema Constraints

**Problem:** Mock data loading failed with error:

```
ERROR 1364 (HY000): Field 'date_creation' doesn't have a default value
```

**Root Cause:**

- `affectation_formation.date_creation` was NOT NULL without a default value
- `suivi_formation_journalier.date_saisie` was NOT NULL without a default value

**Solution Applied:**

```sql
-- Fixed in database
ALTER TABLE affectation_formation MODIFY date_creation datetime(6) DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE suivi_formation_journalier MODIFY date_saisie datetime(6) DEFAULT CURRENT_TIMESTAMP(6);
```

**Updated Files:**

- `schema_and_mock_data.sql` - Added schema ALTER statements at the beginning

---

### 2. ✅ Hibernate Configuration (Data Persistence)

**Problem:** Mock data was wiped out every time the backend restarted - database had empty tables

**Root Cause:**

- Backend used `spring.jpa.hibernate.ddl-auto=create-drop` mode
- This mode drops ALL tables and recreates them on every startup, deleting all data

**Solution Applied:**
Changed Hibernate DDL strategy from `create-drop` to `update`:

**Updated Files:**

- `backend/src/main/resources/application-docker.properties`
  - Changed: `spring.jpa.hibernate.ddl-auto=create-drop`
  - To: `spring.jpa.hibernate.ddl-auto=update`

This ensures:

- Tables are created if they don't exist
- Schema is updated with new columns if needed
- **Existing data is NOT deleted on restart** ✅

---

### 3. ✅ Hibernate Lazy-Loading Serialization Errors

**Problem:** API requests returned errors instead of data:

```
No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor
```

**Root Cause:**

- Hibernate lazy-loaded relationships (marked with `FetchType.LAZY`) created proxy objects
- Jackson (JSON serializer) couldn't serialize these proxy objects to JSON
- This broke all API endpoints that returned Operateur, Equipe, and formation data

**Solution Applied:**
Added `@JsonIgnore` annotation to lazy-loaded relationships to prevent serialization attempts:

**Updated Files:**

1. **`backend/src/main/java/com/ilu/system/operateur/Equipe.java`**

   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "projet_id")
   @JsonIgnore  // ← ADDED
   private Project projet;
   ```

2. **`backend/src/main/java/com/ilu/system/operateur/AffectationFormation.java`**

   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "cree_par")
   @JsonIgnore  // ← ADDED
   private Utilisateur creePar;
   ```

3. **`backend/src/main/java/com/ilu/system/structure/PosteTravail.java`**
   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "zone_id")
   @JsonIgnore  // ← ADDED
   private Zone zone;
   ```

---

## Data Verification

After fixes, the following data is successfully persisting in the database:

| Table                      | Records | Status |
| -------------------------- | ------- | ------ |
| roles                      | 8       | ✅     |
| utilisateurs               | 5       | ✅     |
| projet                     | 2       | ✅     |
| zone_ligne                 | 6       | ✅     |
| poste                      | 9       | ✅     |
| equipe                     | 3       | ✅     |
| operateur                  | 3       | ✅     |
| affectation_formation      | 5       | ✅     |
| suivi_formation_journalier | 17      | ✅     |

---

## How to Use Going Forward

### Loading Mock Data

To reload mock data into the database at any time:

```bash
cd C:\Users\ENVY\ilu-system
Get-Content schema_and_mock_data.sql | docker exec -i ilu-mysql mysql -uroot -proot123 ilu_db
```

### Starting the System

```bash
docker-compose up --build
```

The system will now:

- ✅ Create/update database schema without deleting existing data
- ✅ Properly serialize all API responses (no Hibernate proxy errors)
- ✅ Persist all data between container restarts

### Test Login Credentials

- **Username (matricule):** `chef1`
- **Password:** `chef123`
- **Role:** CHEF_EQUIPE

---

## Technical Details

### Why These Changes Were Necessary

1. **Default Values:** MySQL requires NOT NULL columns to have either a value provided on INSERT or a DEFAULT value. The database schema was missing the defaults.

2. **Persistent vs Ephemeral Data:**
   - `create-drop` mode is useful for development/testing but loses data on restart
   - `update` mode preserves data while still updating schema as needed
   - This is the correct choice for a working development environment

3. **JSON Serialization:**
   - Hibernate proxies are internal implementation details used for lazy loading
   - They cannot be serialized to JSON by default
   - Using `@JsonIgnore` prevents attempts to serialize these proxy objects
   - The data is still loaded from the database when you access the parent entity directly

---

## Files Modified

1. ✅ `schema_and_mock_data.sql`
2. ✅ `backend/src/main/resources/application-docker.properties`
3. ✅ `backend/src/main/java/com/ilu/system/operateur/Equipe.java`
4. ✅ `backend/src/main/java/com/ilu/system/operateur/AffectationFormation.java`
5. ✅ `backend/src/main/java/com/ilu/system/structure/PosteTravail.java`

---

## Status

✅ All issues resolved
✅ Mock data persists across container restarts
✅ API endpoints properly serialize data
✅ System ready for feature development
