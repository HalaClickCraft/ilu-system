package com.ilu.system.structure;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.ilu.system.auth.RoleType;
import com.ilu.system.auth.Utilisateur;
import com.ilu.system.auth.UtilisateurRepository;
import com.ilu.system.structure.ProjectMember;
import com.ilu.system.structure.ProjectMemberDto;
import com.ilu.system.structure.ProjectMemberRepository;
import com.ilu.system.structure.ProjectRepository;
import com.ilu.system.structure.ZoneRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StructureService {

    private final ProjectRepository projectRepository;
    private final ZoneRepository zoneRepository;
    private final PosteTravailRepository posteTravailRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public StructureService(ProjectRepository projectRepository, ZoneRepository zoneRepository,
                            PosteTravailRepository posteTravailRepository, ProjectMemberRepository projectMemberRepository,
                            UtilisateurRepository utilisateurRepository) {
        this.projectRepository = projectRepository;
        this.zoneRepository = zoneRepository;
        this.posteTravailRepository = posteTravailRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    StructureService(ProjectRepository projectRepository, ZoneRepository zoneRepository,
                     PosteTravailRepository posteTravailRepository) {
        this(projectRepository, zoneRepository, posteTravailRepository, null, null);
    }

    @Transactional
    public void seedDefaultStructureIfNeeded() {
        if (!projectRepository.findAll().isEmpty()) {
            return;
        }

        Map<String, List<String>> defaults = new LinkedHashMap<>();
        defaults.put("KJ92", List.of("Zone Assemblage", "Zone Contrôle"));
        defaults.put("CMP", List.of("Zone Montage", "Zone Test"));
        defaults.put("Smart Car", List.of("Zone Électronique", "Zone Logistique"));
        defaults.put("SQ52 L1", List.of("Ligne 1", "Ligne 2"));
        defaults.put("SQ52 L2", List.of("Ligne A", "Ligne B"));

        for (Map.Entry<String, List<String>> entry : defaults.entrySet()) {
            Project project = projectRepository.save(new Project(entry.getKey()));
            for (String zoneName : entry.getValue()) {
                Zone zone = zoneRepository.save(new Zone(zoneName, project));
                posteTravailRepository.save(new PosteTravail(zoneName + " - Poste 1", zone));
                posteTravailRepository.save(new PosteTravail(zoneName + " - Poste 2", zone));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjects() {
        return projectRepository.findAll().stream().map(project -> new ProjectDto(
                project.getIdProjet(),
                project.getNom(),
                project.getLogo(),
                project.getDateCreation(),
                project.getCreePar(),
                zoneRepository.findAll().stream()
                        .filter(zone -> zone.getProjet() != null && zone.getProjet().getIdProjet().equals(project.getIdProjet()))
                        .map(zone -> new ZoneDto(
                                zone.getIdZone(),
                                zone.getNom(),
                                zone.getDateCreation(),
                                zone.getCreePar(),
                                posteTravailRepository.findAll().stream()
                                        .filter(poste -> poste.getZone() != null && poste.getZone().getIdZone().equals(zone.getIdZone()))
                                        .map(poste -> new PosteTravailDto(
                                                poste.getIdPoste(),
                                                poste.getNom(),
                                                poste.getTypePoste(),
                                                poste.getCadenceObjectif(),
                                               poste.getCiblePolyvalence(),
                                                poste.getNiveauCibleIlu(),
                                                poste.getDateCreation(),
                                                poste.getCreePar()
                                        ))
                                        .collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList()),
                projectMemberRepository.findByProjet_IdProjet(project.getIdProjet()).stream().map(member -> new ProjectMemberDto(
                        member.getId(), member.getUtilisateur().getId(), member.getUtilisateur().getMatricule(),
                        member.getUtilisateur().getNom(), member.getUtilisateur().getRole().getLibelle().name(),
                        member.getRoleProjet() != null ? member.getRoleProjet().name() : null)).toList()
        )).collect(Collectors.toList());
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest request, String creePar) {
        Project project = new Project(request.getNom(), creePar);
        if (request.getLogo() != null && !request.getLogo().isBlank()) {
            project.setLogo(request.getLogo());
        }
        project = projectRepository.save(project);

        // Assign members with their roles in the project
        if (request.getMembres() != null) {
            for (CreateProjectRequest.MemberAssignment assignment : request.getMembres()) {
                Utilisateur user = utilisateurRepository.findById(assignment.getUtilisateurId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Utilisateur introuvable avec ID: " + assignment.getUtilisateurId()));

                ProjectMember.RoleInProject roleProjet;
                try {
                    roleProjet = ProjectMember.RoleInProject.valueOf(assignment.getRoleProjet());
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rôle dans le projet invalide: " + assignment.getRoleProjet());
                }

                if (projectMemberRepository.existsByProjet_IdProjetAndUtilisateur_Id(project.getIdProjet(), user.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "L'utilisateur " + user.getMatricule() + " est déjà affecté à ce projet.");
                }

                projectMemberRepository.save(new ProjectMember(project, user, roleProjet));
            }
        }

        return toProjectDto(project, List.of());
    }

    @Transactional
    public ZoneDto createZone(Long projectId, String nom, String creePar) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Projet introuvable"));
        if (zoneRepository.existsByNomAndProjet_IdProjet(nom, projectId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une zone portant ce nom existe déjà dans ce projet.");
        }
        Zone zone = zoneRepository.save(new Zone(nom, project, creePar));
        return toZoneDto(zone, List.of());
    }

      private static final java.util.Set<String> NIVEAUX_ILU_VALIDES = java.util.Set.of("I", "L", "U");

    @Transactional
    public PosteTravailDto createPoste(Long zoneId, String nom, String creePar, String niveauCibleIlu) {
        Zone zone = zoneRepository.findById(zoneId).orElseThrow(() -> new IllegalArgumentException("Zone introuvable"));
        Long projectId = zone.getProjet().getIdProjet();
        if (posteTravailRepository.existsByNomAndZone_Projet_IdProjet(nom, projectId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un poste portant ce nom existe déjà dans ce projet.");
        }
        String niveau = niveauCibleIlu == null || niveauCibleIlu.isBlank() ? "I" : niveauCibleIlu.trim().toUpperCase();
        if (!NIVEAUX_ILU_VALIDES.contains(niveau)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le niveau cible doit être I, L ou U.");
        }
        return toPosteDto(posteTravailRepository.save(new PosteTravail(nom, zone, creePar, niveau)));
    }

    @Transactional(readOnly = true)
    public List<PosteTravailDto> getAllPostes() {
        return posteTravailRepository.findAll().stream()
                .map(this::toPosteDto)
                .collect(Collectors.toList());
    }

    private ProjectDto toProjectDto(Project project, List<ZoneDto> zones) {
        return new ProjectDto(project.getIdProjet(), project.getNom(), project.getLogo(), project.getDateCreation(), project.getCreePar(), zones,
                projectMemberRepository.findByProjet_IdProjet(project.getIdProjet()).stream().map(member -> new ProjectMemberDto(
                        member.getId(), member.getUtilisateur().getId(), member.getUtilisateur().getMatricule(),
                        member.getUtilisateur().getNom(), member.getUtilisateur().getRole().getLibelle().name(),
                        member.getRoleProjet() != null ? member.getRoleProjet().name() : null)).toList());
    }

    @Transactional
    public ProjectMemberDto addProjectMember(Long projectId, Long userId, String roleProjet) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet introuvable."));
        Utilisateur user = utilisateurRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
        RoleType role = user.getRole().getLibelle();
        if (!java.util.Set.of(RoleType.CHEF_EQUIPE, RoleType.HSE, RoleType.QUALITE, RoleType.AGENT_QUALITE, RoleType.SUPERVISEUR, RoleType.RESPONSABLE_QUALITE).contains(role))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seuls les rôles opérationnels peuvent être affectés à un projet.");
        if (projectMemberRepository.existsByProjet_IdProjetAndUtilisateur_Id(projectId, userId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet utilisateur est déjà affecté à ce projet.");
        ProjectMember.RoleInProject roleProjetEnum = parseRoleProjet(roleProjet, true);

        ProjectMember member = projectMemberRepository.save(new ProjectMember(project, user, roleProjetEnum));
        return new ProjectMemberDto(member.getId(), user.getId(), user.getMatricule(), user.getNom(), role.name(),
                member.getRoleProjet().name());
    }

    @Transactional
    public ProjectMemberDto updateProjectMember(Long projectId, Long memberId, Long userId, String roleProjet) {
        ProjectMember member = projectMemberRepository.findByIdAndProjet_IdProjet(memberId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Affectation introuvable."));
        Utilisateur updatedUser = member.getUtilisateur();
        if (userId != null) {
            updatedUser = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
            RoleType updatedUserRole = updatedUser.getRole().getLibelle();
            if (!java.util.Set.of(RoleType.CHEF_EQUIPE, RoleType.HSE, RoleType.QUALITE, RoleType.AGENT_QUALITE, RoleType.SUPERVISEUR, RoleType.RESPONSABLE_QUALITE).contains(updatedUserRole)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seuls les rôles opérationnels peuvent être affectés à un projet.");
            }
            if (projectMemberRepository.existsByProjet_IdProjetAndUtilisateur_IdAndIdNot(projectId, userId, memberId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet utilisateur est déjà affecté à ce projet.");
            }
            member.setUtilisateur(updatedUser);
        }

        if (roleProjet != null && !roleProjet.isBlank()) {
            member.setRoleProjet(parseRoleProjet(roleProjet, false));
        }

        ProjectMember updatedMember = projectMemberRepository.save(member);
        return new ProjectMemberDto(
                updatedMember.getId(),
                updatedMember.getUtilisateur().getId(),
                updatedMember.getUtilisateur().getMatricule(),
                updatedMember.getUtilisateur().getNom(),
                updatedMember.getUtilisateur().getRole().getLibelle().name(),
                updatedMember.getRoleProjet().name()
        );
    }

    @Transactional
    public void removeProjectMember(Long projectId, Long memberId) {
        ProjectMember member = projectMemberRepository.findByIdAndProjet_IdProjet(memberId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Affectation introuvable."));
        projectMemberRepository.delete(member);
    }

    private ProjectMember.RoleInProject parseRoleProjet(String roleProjet, boolean allowDefault) {
        if (roleProjet == null || roleProjet.isBlank()) {
            if (allowDefault) {
                return ProjectMember.RoleInProject.SUPERVISEUR;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle de projet obligatoire.");
        }
        try {
            return ProjectMember.RoleInProject.valueOf(roleProjet);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle de projet invalide: " + roleProjet);
        }
    }

    private ZoneDto toZoneDto(Zone zone, List<PosteTravailDto> postes) {
        return new ZoneDto(zone.getIdZone(), zone.getNom(), zone.getDateCreation(), zone.getCreePar(), postes);
    }

     private PosteTravailDto toPosteDto(PosteTravail poste) {
        return new PosteTravailDto(
                poste.getIdPoste(),
                poste.getNom(),
                poste.getTypePoste(),
                poste.getCadenceObjectif(),
                poste.getCiblePolyvalence(),
                poste.getNiveauCibleIlu(),
                poste.getDateCreation(),
                poste.getCreePar()
        );
    }
}
