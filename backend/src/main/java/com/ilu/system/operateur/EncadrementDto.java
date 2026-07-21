package com.ilu.system.operateur;

import com.ilu.system.structure.ProjectMember;

public record EncadrementDto(Long utilisateurId, String matricule, String nom, String roleProjet) {
    static EncadrementDto from(ProjectMember membre) {
        return new EncadrementDto(membre.getUtilisateur().getId(), membre.getUtilisateur().getMatricule(),
                membre.getUtilisateur().getNom(), membre.getRoleProjet().name());
    }
}
