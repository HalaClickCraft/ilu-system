-- ILU System Database Schema & Mock Data
-- Tables created via JPA (Hibernate create-drop mode)
-- Use these INSERT commands manually when needed
-- Note: Hibernate will auto-create tables based on entities

USE ilu_db;

-- =============================================================
-- SCHEMA FIXES (Run these first if schema needs updates)
-- =============================================================

-- Add DEFAULT CURRENT_TIMESTAMP to affectation_formation.date_creation if not already set
ALTER TABLE affectation_formation MODIFY date_creation datetime(6) DEFAULT CURRENT_TIMESTAMP(6);

-- Add DEFAULT CURRENT_TIMESTAMP to suivi_formation_journalier.date_saisie if not already set
ALTER TABLE suivi_formation_journalier MODIFY date_saisie datetime(6) DEFAULT CURRENT_TIMESTAMP(6);

-- =============================================================
-- MOCK DATA INSERTS
-- =============================================================

-- Roles (RoleType enum)
INSERT INTO roles (libelle) VALUES
('ADMIN'),
('CHEF_EQUIPE'),
('RH'),
('QUALITE'),
('AGENT_QUALITE'),
('SUPERVISEUR'),
('HSE'),
('RESPONSABLE_QUALITE')
ON DUPLICATE KEY UPDATE libelle=VALUES(libelle);

-- Utilisateurs (Users)
INSERT INTO utilisateurs (matricule, nom, cin, password, doit_changer_mdp, actif, role_id) 
SELECT 'admin1', 'Alice Administratrice', 'AB123456', 'admin123', 1, 1, r.id FROM roles r WHERE r.libelle = 'ADMIN'
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO utilisateurs (matricule, nom, cin, password, doit_changer_mdp, actif, role_id) 
SELECT 'chef1', 'Bob Chef d''Équipe', 'BC234567', 'chef123', 1, 1, r.id FROM roles r WHERE r.libelle = 'CHEF_EQUIPE'
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO utilisateurs (matricule, nom, cin, password, doit_changer_mdp, actif, role_id) 
SELECT 'rh1', 'Carole RH', 'CD345678', 'rh123', 1, 1, r.id FROM roles r WHERE r.libelle = 'RH'
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO utilisateurs (matricule, nom, cin, password, doit_changer_mdp, actif, role_id) 
SELECT 'aq1', 'David Agent Qualité', 'DE456789', 'aq123', 1, 1, r.id FROM roles r WHERE r.libelle = 'AGENT_QUALITE'
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO utilisateurs (matricule, nom, cin, password, doit_changer_mdp, actif, role_id) 
SELECT 'super1', 'Paul Superviseur', 'PQ567890', 'super123', 1, 1, r.id FROM roles r WHERE r.libelle = 'SUPERVISEUR'
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Projets
INSERT INTO projet (nom, cree_par) VALUES
('Projet Renault Clio', 'Système'),
('Projet Peugeot 208', 'Système')
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Zones
INSERT INTO zone_ligne (nom, cree_par, projet_id) 
SELECT 'Zone A - Assemblage', 'Système', p.id_projet FROM projet p WHERE p.nom = 'Projet Renault Clio' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO zone_ligne (nom, cree_par, projet_id) 
SELECT 'Zone B - Finition', 'Système', p.id_projet FROM projet p WHERE p.nom = 'Projet Renault Clio' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Postes (Work Stations)
INSERT INTO poste (nom, type_poste, cadence_objectif, cible_polyvalence, cree_par, zone_id) 
SELECT 'Assemblage', 'PRODUCTION', 100, 3, 'Système', z.id_zone FROM zone_ligne z WHERE z.nom = 'Zone A - Assemblage' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO poste (nom, type_poste, cadence_objectif, cible_polyvalence, cree_par, zone_id) 
SELECT 'Vissage', 'PRODUCTION', 120, 3, 'Système', z.id_zone FROM zone_ligne z WHERE z.nom = 'Zone A - Assemblage' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO poste (nom, type_poste, cadence_objectif, cible_polyvalence, cree_par, zone_id) 
SELECT 'Finition & Polissage', 'PRODUCTION', 80, 2, 'Système', z.id_zone FROM zone_ligne z WHERE z.nom = 'Zone B - Finition' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Formation Templates (Modèles de Formation)
-- These define the training objectives and quality standards for each workstation
INSERT INTO formation_template (poste_id, cadence_objectif, qualite_objectif_texte, cree_par)
SELECT p.id_poste, p.cadence_objectif, '< 7 défauts en 12 jours', 'Système' 
FROM poste p WHERE p.nom = 'Assemblage' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_objectif=VALUES(cadence_objectif);

INSERT INTO formation_template (poste_id, cadence_objectif, qualite_objectif_texte, cree_par)
SELECT p.id_poste, p.cadence_objectif, '< 5 défauts en 12 jours', 'Système' 
FROM poste p WHERE p.nom = 'Vissage' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_objectif=VALUES(cadence_objectif);

INSERT INTO formation_template (poste_id, cadence_objectif, qualite_objectif_texte, cree_par)
SELECT p.id_poste, p.cadence_objectif, '< 10 défauts en 12 jours', 'Système' 
FROM poste p WHERE p.nom = 'Finition & Polissage' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_objectif=VALUES(cadence_objectif);

-- Equipes (Teams)
INSERT INTO equipe (nom, chef_id, projet_id) 
SELECT 'Équipe Assemblage', u.id, p.id_projet FROM utilisateurs u, projet p 
WHERE u.matricule = 'chef1' AND p.nom = 'Projet Renault Clio' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Operateurs (Operators)
INSERT INTO operateur (matricule, nom, prenom, fonctionnalite, date_embauche, statut, formation_rework, equipe_id, poste_affecte_id) 
SELECT 'OP001', 'Dupont', 'Jean', 'Opérateur Production', '2024-01-15', 'ACTIF', 0, e.id_equipe, p.id_poste 
FROM equipe e, poste p WHERE e.nom = 'Équipe Assemblage' AND p.nom = 'Assemblage' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO operateur (matricule, nom, prenom, fonctionnalite, date_embauche, statut, formation_rework, equipe_id, poste_affecte_id) 
SELECT 'OP002', 'Martin', 'Sophie', 'Opérateur Production', '2024-02-20', 'ACTIF', 0, e.id_equipe, p.id_poste 
FROM equipe e, poste p WHERE e.nom = 'Équipe Assemblage' AND p.nom = 'Assemblage' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

INSERT INTO operateur (matricule, nom, prenom, fonctionnalite, date_embauche, statut, formation_rework, equipe_id, poste_affecte_id) 
SELECT 'OP003', 'Bernard', 'Pierre', 'Opérateur Production', '2024-03-10', 'ACTIF', 0, e.id_equipe, p.id_poste 
FROM equipe e, poste p WHERE e.nom = 'Équipe Assemblage' AND p.nom = 'Assemblage' LIMIT 1
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Affectation Formation (Training Assignments)
-- OP001: Primary training on Poste 1 (Assemblage) - VALIDATED
INSERT INTO affectation_formation (operateur_matricule, id_poste, id_projet, est_affectation_primaire, statut, date_debut, date_evaluation_prevue, cree_par) 
SELECT 'OP001', p.id_poste, pr.id_projet, 1, 'VALIDEE', '2026-02-10', '2026-02-22', u.id 
FROM poste p, projet pr, utilisateurs u 
WHERE p.nom = 'Assemblage' AND pr.nom = 'Projet Renault Clio' AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE statut=VALUES(statut);

-- OP001: Secondary training on Poste 2 (Vissage) - IN TRAINING
INSERT INTO affectation_formation (operateur_matricule, id_poste, id_projet, est_affectation_primaire, statut, date_debut, date_evaluation_prevue, cree_par) 
SELECT 'OP001', p.id_poste, pr.id_projet, 0, 'EN_FORMATION', '2026-07-08', '2026-07-20', u.id 
FROM poste p, projet pr, utilisateurs u 
WHERE p.nom = 'Vissage' AND pr.nom = 'Projet Renault Clio' AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE statut=VALUES(statut);

-- OP002: Primary training on Poste 2 (Vissage) - VALIDATED
INSERT INTO affectation_formation (operateur_matricule, id_poste, id_projet, est_affectation_primaire, statut, date_debut, date_evaluation_prevue, cree_par) 
SELECT 'OP002', p.id_poste, pr.id_projet, 1, 'VALIDEE', '2026-04-15', '2026-04-27', u.id 
FROM poste p, projet pr, utilisateurs u 
WHERE p.nom = 'Vissage' AND pr.nom = 'Projet Renault Clio' AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE statut=VALUES(statut);

-- OP002: Secondary training on Poste 3 (Finition & Polissage) - IN TRAINING
INSERT INTO affectation_formation (operateur_matricule, id_poste, id_projet, est_affectation_primaire, statut, date_debut, date_evaluation_prevue, cree_par) 
SELECT 'OP002', p.id_poste, pr.id_projet, 0, 'EN_FORMATION', '2026-07-10', '2026-07-22', u.id 
FROM poste p, projet pr, utilisateurs u 
WHERE p.nom = 'Finition & Polissage' AND pr.nom = 'Projet Renault Clio' AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE statut=VALUES(statut);

-- OP003: Primary training on Poste 3 (Finition & Polissage) - IN TRAINING
INSERT INTO affectation_formation (operateur_matricule, id_poste, id_projet, est_affectation_primaire, statut, date_debut, date_evaluation_prevue, cree_par) 
SELECT 'OP003', p.id_poste, pr.id_projet, 1, 'EN_FORMATION', '2026-06-01', '2026-06-13', u.id 
FROM poste p, projet pr, utilisateurs u 
WHERE p.nom = 'Finition & Polissage' AND pr.nom = 'Projet Renault Clio' AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE statut=VALUES(statut);

-- =============================================================
-- Daily Journal Entries (Suivi Formation Journalier)
-- Note: Fill these in manually via the UI or insert here as needed
-- =============================================================

-- OP001 Secondary Training (Poste 2 - Vissage) - 12 Days
-- Day 1
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 1, 60, 5, 'Premier jour : apprentissage des gestes de base. Beaucoup de défauts initiaux.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 2
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 2, 75, 3, 'Progression notable. Opérateur comprend bien la mécanique du poste.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 3
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 3, 82, 2, 'Cadence s''améliore. Qualité bonne.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 4
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 4, 88, 1, 'Très bon progrès. Opérateur à l''aise avec le poste.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 5
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 5, 92, 1, 'Cadence cible presque atteinte. Qualité excellente.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 6
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 6, 95, 0, 'Cadence cible atteinte ! Zéro défaut.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 7
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 7, 94, 1, 'Maintien de la cadence cible avec qualité constante.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 8
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 8, 93, 0, 'Excellent rendement. Autonomie confirmée.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 9
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 9, 95, 1, 'Cadence stable. Un petit défaut détecté en fin de journée.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 10
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 10, 96, 0, 'Performance optimale. Opérateur maîtrise complètement le poste.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 11
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 11, 95, 0, 'Maintien excellent de la performance et de la qualité.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 12
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 12, 94, 0, 'Formation complète avec succès. Prêt pour évaluation finale.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP001' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- OP002 Secondary Training (Poste 3 - Finition & Polissage) - 5 Days
-- Day 1
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 1, 55, 6, 'Première journée sur Finition & Polissage. Courbe d''apprentissage initiale.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP002' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 2
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 2, 65, 4, 'Adaptation progressive. Opérateur apprend les techniques de polissage.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP002' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 3
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 3, 70, 3, 'Bonne progression. Maîtrise des outils croissante.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP002' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 4
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 4, 75, 2, 'Cadence augmente régulièrement. Qualité acceptable.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP002' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Day 5
INSERT INTO suivi_formation_journalier (id_affectation, jour, cadence_realisee, nb_defauts, remarques, saisie_par) 
SELECT af.id_affectation, 5, 76, 2, 'Stabilité croissante. Opérateur devient autonome progressivement.', u.id 
FROM affectation_formation af, utilisateurs u, operateur op 
WHERE op.matricule = 'OP002' AND af.operateur_matricule = op.matricule AND af.est_affectation_primaire = 0 AND u.matricule = 'chef1' LIMIT 1
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);
--update 
UPDATE poste p
JOIN zone_ligne z ON p.zone_id = z.id_zone
SET p.niveau_cible_ilu = 'I'
WHERE p.nom = 'Assemblage' AND z.nom = 'Zone A - Assemblage';

UPDATE poste p
JOIN zone_ligne z ON p.zone_id = z.id_zone
SET p.niveau_cible_ilu = 'I'
WHERE p.nom = 'Vissage' AND z.nom = 'Zone A - Assemblage';

UPDATE poste p
JOIN zone_ligne z ON p.zone_id = z.id_zone
SET p.niveau_cible_ilu = 'I'
WHERE p.nom = 'Finition & Polissage' AND z.nom = 'Zone B - Finition';