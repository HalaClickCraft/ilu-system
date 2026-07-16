-- SQL Script to create tables and insert mock data matching the class diagram and MLD.
-- To be executed inside the MySQL Docker container.

USE ilu_db;

-- 0. ROLES & UTILISATEURS (Matches JPA structures)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    matricule VARCHAR(255) UNIQUE NOT NULL,
    nom VARCHAR(255) NOT NULL,
    cin VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    doit_changer_mdp BOOLEAN NOT NULL DEFAULT FALSE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 1. PROJET
CREATE TABLE IF NOT EXISTS PROJET (
    id_projet BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    logo VARCHAR(255),
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cree_par VARCHAR(255) NOT NULL DEFAULT 'Système'
);

-- 2. ZONE_LIGNE
CREATE TABLE IF NOT EXISTS ZONE_LIGNE (
    id_zone BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    projet_id BIGINT,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cree_par VARCHAR(255) NOT NULL DEFAULT 'Système',
    FOREIGN KEY (projet_id) REFERENCES PROJET(id_projet) ON DELETE SET NULL
);

-- 3a. PROJET_MEMBRE (projet_membre) with role_projet
CREATE TABLE IF NOT EXISTS projet_membre (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    projet_id BIGINT NOT NULL,
    utilisateur_id BIGINT NOT NULL,
    role_projet VARCHAR(50) NOT NULL DEFAULT 'SUPERVISEUR',
    FOREIGN KEY (projet_id) REFERENCES PROJET(id_projet) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    UNIQUE KEY uk_projet_utilisateur (projet_id, utilisateur_id)
);

CREATE TABLE IF NOT EXISTS POSTE (
    id_poste BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    type_poste VARCHAR(50) NOT NULL,
    cadence_objectif INT NOT NULL,
    cible_polyvalence INT NOT NULL,
    zone_id BIGINT,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cree_par VARCHAR(255) NOT NULL DEFAULT 'Système',
    FOREIGN KEY (zone_id) REFERENCES ZONE_LIGNE(id_zone) ON DELETE SET NULL
);

-- 4. EQUIPE
CREATE TABLE IF NOT EXISTS EQUIPE (
    id_equipe BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    chef_id BIGINT,
    FOREIGN KEY (chef_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
);

-- 5. OPERATEUR
CREATE TABLE IF NOT EXISTS OPERATEUR (
    matricule VARCHAR(50) PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    date_embauche DATE NOT NULL,
    date_sortie DATE,
    statut VARCHAR(50) NOT NULL,
    formation_rework BOOLEAN DEFAULT FALSE,
    equipe_id BIGINT,
    FOREIGN KEY (equipe_id) REFERENCES EQUIPE(id_equipe) ON DELETE SET NULL
);

-- 6. TEMPLATE_QUESTIONNAIRE
CREATE TABLE IF NOT EXISTS TEMPLATE_QUESTIONNAIRE (
    id_gabarit BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    ordre_affichage INT NOT NULL,
    date_creation DATE NOT NULL
);

-- 7. QUESTION
CREATE TABLE IF NOT EXISTS QUESTION (
    id_question BIGINT AUTO_INCREMENT PRIMARY KEY,
    enonce TEXT NOT NULL,
    reponse_attendue VARCHAR(255) NOT NULL,
    bloc VARCHAR(100) NOT NULL,
    image VARCHAR(255),
    statut_validation VARCHAR(50) NOT NULL,
    gabarit_id BIGINT,
    FOREIGN KEY (gabarit_id) REFERENCES TEMPLATE_QUESTIONNAIRE(id_gabarit) ON DELETE SET NULL
);

-- 8. SESSION_EVALUATION
CREATE TABLE IF NOT EXISTS SESSION_EVALUATION (
    id_session BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_session VARCHAR(50) NOT NULL,
    date_debut DATETIME NOT NULL,
    date_cloture DATETIME,
    shift VARCHAR(20) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    score_global DOUBLE DEFAULT 0.0,
    niveau_obtenu VARCHAR(50),
    operateur_matricule VARCHAR(50),
    FOREIGN KEY (operateur_matricule) REFERENCES OPERATEUR(matricule) ON DELETE SET NULL
);

-- 9. SUIVI_INTEGRATION_JOURNALIER
CREATE TABLE IF NOT EXISTS SUIVI_INTEGRATION_JOURNALIER (
    id_suivi BIGINT AUTO_INCREMENT PRIMARY KEY,
    jour INT NOT NULL,
    cadence_realisee INT NOT NULL,
    nb_defauts INT NOT NULL,
    remarques TEXT,
    session_id BIGINT,
    FOREIGN KEY (session_id) REFERENCES SESSION_EVALUATION(id_session) ON DELETE CASCADE
);

-- 10. EVALUATION_REPONSE_INDIVIDUELLE
CREATE TABLE IF NOT EXISTS EVALUATION_REPONSE_INDIVIDUELLE (
    id_reponse BIGINT AUTO_INCREMENT PRIMARY KEY,
    est_correcte BOOLEAN NOT NULL,
    session_id BIGINT,
    question_id BIGINT,
    FOREIGN KEY (session_id) REFERENCES SESSION_EVALUATION(id_session) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES QUESTION(id_question) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS DETAILS_EVALUATION_POSTE (
    id_detail BIGINT AUTO_INCREMENT PRIMARY KEY,
    bloc VARCHAR(100) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    score_bloc DOUBLE DEFAULT 0.0,
    date_validation DATE,
    session_id BIGINT,
    FOREIGN KEY (session_id) REFERENCES SESSION_EVALUATION(id_session) ON DELETE CASCADE
);

-- 12. JOURNAL_MODIF_GABARIT
CREATE TABLE IF NOT EXISTS JOURNAL_MODIF_GABARIT (
    id_log BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_modification DATETIME NOT NULL,
    motif VARCHAR(255) NOT NULL,
    gabarit_id BIGINT,
    FOREIGN KEY (gabarit_id) REFERENCES TEMPLATE_QUESTIONNAIRE(id_gabarit) ON DELETE CASCADE
);

-- 13. NOTIFICATION
CREATE TABLE IF NOT EXISTS NOTIFICATION (
    id_notification BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    type_notif VARCHAR(50) NOT NULL,
    date_envoi DATETIME NOT NULL,
    lue BOOLEAN DEFAULT FALSE,
    destinataire_id BIGINT,
    FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
);

-- 14. DEMANDE_MAJ_EQUIPE
CREATE TABLE IF NOT EXISTS DEMANDE_MAJ_EQUIPE (
    id_demande BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_demande VARCHAR(100) NOT NULL,
    statut VARCHAR(50) NOT NULL,
    date_demande DATE NOT NULL,
    equipe_id BIGINT,
    demandeur_id BIGINT,
    FOREIGN KEY (equipe_id) REFERENCES EQUIPE(id_equipe) ON DELETE SET NULL,
    FOREIGN KEY (demandeur_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
);

-- =======================================================
-- MOCK DATA INSERTS
-- =======================================================

-- Mock Roles done
INSERT INTO roles (id, libelle) VALUES
(1, 'ADMIN'),
(2, 'CHEF_EQUIPE'),
(3, 'RH'),
(4, 'QUALITE'),
(5, 'RESPONSABLE_QUALITE'),
(6, 'HSE'),
(7, 'SUPERVISEUR')
ON DUPLICATE KEY UPDATE libelle=VALUES(libelle);

-- Mock Users (Plain Text Passwords) done
INSERT INTO utilisateurs (id, matricule, nom, cin, password, doit_changer_mdp, actif, role_id) VALUES
(1, 'admin', 'Administrateur', '12345678', '12345678', TRUE, TRUE, 1),
(2, 'chef1', 'Jean Chef', 'chef123', 'chef123', TRUE, TRUE, 2),
(3, 'rh1', 'Sophie RH', 'rh123', 'rh123', TRUE, TRUE, 3),
(4, 'qualite1', 'Marc Qualité', 'q123', 'q123', TRUE, TRUE, 4),
(5, 'rq1', 'Nadia Responsable Qualité', 'rq123', 'rq123', TRUE, TRUE, 5),
(6, 'hse1', 'Hélène HSE', 'hse123', 'hse123', TRUE, TRUE, 6),
(7, 'super1', 'Paul Superviseur', 'super123', 'super123', TRUE, TRUE, 7)
ON DUPLICATE KEY UPDATE matricule=VALUES(matricule);

-- Mock Projet done 
INSERT INTO projet (id_projet, nom, logo, cree_par) VALUES 
(1, 'Projet Renault Clio', 'renault_logo.png', 'Système'),
(2, 'Projet Peugeot 208', 'peugeot_logo.png', 'Système')
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Mock projet_membre (members with roles in projects) done 
INSERT INTO projet_membre (projet_id, utilisateur_id, role_projet) VALUES
(1, 2, 'CHEF_DE_PROJET'),
(1, 4, 'AGENT_QUALITE'),
(1, 6, 'RESPONSABLE_HSE'),
(1, 7, 'SUPERVISEUR'),
(2, 2, 'CHEF_DE_PROJET'),
(2, 6, 'RESPONSABLE_HSE')
ON DUPLICATE KEY UPDATE role_projet=VALUES(role_projet);

INSERT INTO zone_ligne (id_zone, nom, projet_id, cree_par) VALUES
(1, 'Ligne Assemblage Pare-chocs', 1, 'Système'),
(2, 'Ligne Finition Cockpit', 2, 'Système')
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Mock Poste done
INSERT INTO poste (id_poste, nom, type_poste, cadence_objectif, cible_polyvalence, zone_id, cree_par) VALUES
(1, 'Poste Assemblage Visuel', 'Manuel', 120, 3, 1, 'Système'),
(2, 'Poste Vissage Automatique', 'Automatique', 95, 4, 1, 'Système'),
(3, 'Poste Finition & Polissage', 'Manuel', 80, 2, 2, 'Système')
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Mock Equipe
INSERT INTO EQUIPE (id_equipe, nom, chef_id) VALUES
(1, 'Equipe Shift A', 2),
(2, 'Equipe Shift B', 2)
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Mock Operateur
INSERT INTO OPERATEUR (matricule, nom, date_embauche, date_sortie, statut, formation_rework, equipe_id) VALUES
('OP001', 'Amine Ben Ali', '2025-01-10', NULL, 'Actif', FALSE, 1),
('OP002', 'Salma Mansour', '2025-03-15', NULL, 'Actif', TRUE, 1),
('OP003', 'Youssef Trabelsi', '2025-06-01', NULL, 'En Formation', FALSE, 2)
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Mock Template Questionnaire
INSERT INTO TEMPLATE_QUESTIONNAIRE (id_gabarit, nom, ordre_affichage, date_creation) VALUES
(1, 'Gabarit Evaluation Visuelle', 1, '2026-07-01'),
(2, 'Gabarit Vissage Couple', 2, '2026-07-05')
ON DUPLICATE KEY UPDATE nom=VALUES(nom);

-- Mock Question
INSERT INTO QUESTION (id_question, enonce, reponse_attendue, bloc, image, statut_validation, gabarit_id) VALUES
(1, 'Vérifier la conformité de l\'étiquetage du bloc arrière', 'Conforme et lisible', 'Bloc A', NULL, 'Validé', 1),
(2, 'Calculer le temps de cycle standard sur poste de vissage', '42 secondes', 'Bloc B', NULL, 'Validé', 1),
(3, 'Ajuster la pression de serrage à 5 bars', '5 bars +/- 0.2', 'Bloc B', NULL, 'En Attente', 2)
ON DUPLICATE KEY UPDATE enonce=VALUES(enonce);

-- Mock Session Evaluation
INSERT INTO SESSION_EVALUATION (id_session, type_session, date_debut, date_cloture, shift, statut, score_global, niveau_obtenu, operateur_matricule) VALUES
(1, 'Hebdomadaire', '2026-07-14 08:00:00', NULL, 'Matin', 'En cours', 85.5, 'Niveau 3', 'OP001'),
(2, 'Mensuelle', '2026-06-30 08:00:00', '2026-06-30 16:00:00', 'Après-midi', 'Clôturée', 91.2, 'Niveau 4', 'OP002')
ON DUPLICATE KEY UPDATE score_global=VALUES(score_global);

-- Mock Suivi Integration Journalier
INSERT INTO SUIVI_INTEGRATION_JOURNALIER (id_suivi, jour, cadence_realisee, nb_defauts, remarques, session_id) VALUES
(1, 1, 80, 2, 'Bon démarrage, opérateur motivé.', 1),
(2, 2, 95, 1, 'Cadence atteinte avec une bonne qualité.', 1)
ON DUPLICATE KEY UPDATE cadence_realisee=VALUES(cadence_realisee);

-- Mock Details Evaluation Poste
INSERT INTO DETAILS_EVALUATION_POSTE (id_detail, bloc, statut, score_bloc, date_validation, session_id) VALUES
(1, 'Bloc A', 'Validé', 90.0, '2026-07-14', 1),
(2, 'Bloc B', 'En attente', 81.0, NULL, 1)
ON DUPLICATE KEY UPDATE score_bloc=VALUES(score_bloc);

-- Mock Journal Modif Gabarit
INSERT INTO JOURNAL_MODIF_GABARIT (id_log, date_modification, motif, gabarit_id) VALUES
(1, '2026-07-14 09:15:00', 'Ajout de consigne de sécurité Zone A', 1),
(2, '2026-07-14 11:30:00', 'Correction gabarit sécurité incendie', 2)
ON DUPLICATE KEY UPDATE motif=VALUES(motif);

-- Mock Demande MAJ Equipe
INSERT INTO DEMANDE_MAJ_EQUIPE (id_demande, type_demande, statut, date_demande, equipe_id, demandeur_id) VALUES
(1, 'Ajout Opérateur', 'Validé', '2026-07-10', 1, 2),
(2, 'Modification Shift', 'En attente', '2026-07-14', 1, 2)
ON DUPLICATE KEY UPDATE type_demande=VALUES(type_demande);