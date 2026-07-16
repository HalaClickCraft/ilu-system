package com.ilu.system.structure;

public record ProjectMemberDto(Long id, Long utilisateurId, String matricule, String nom, String role, String roleProjet) {}