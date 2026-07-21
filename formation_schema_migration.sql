-- Formation Template Table Migration
-- Add this to your database when deploying the formation tracking system

-- Create Formation Template table
CREATE TABLE IF NOT EXISTS FORMATION_TEMPLATE (
    id_template BIGINT AUTO_INCREMENT PRIMARY KEY,
    poste_id BIGINT NOT NULL,
    cadence_objectif INT NOT NULL,
    qualite_objectif_texte VARCHAR(255) NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    cree_par VARCHAR(100) NOT NULL,
    date_modification DATETIME,
    modifie_par VARCHAR(100),
    UNIQUE KEY unique_poste (poste_id),
    FOREIGN KEY (poste_id) REFERENCES POSTE(id_poste) ON DELETE RESTRICT
);

-- Add qualite_objectif column to affectation_formation if not exists
ALTER TABLE AFFECTATION_FORMATION ADD COLUMN IF NOT EXISTS qualite_objectif VARCHAR(255);

-- Create indexes for better query performance
CREATE INDEX idx_formation_template_poste ON FORMATION_TEMPLATE(poste_id);
CREATE INDEX idx_affectation_formation_qualite ON AFFECTATION_FORMATION(id_affectation, qualite_objectif);
