package com.ilu.system.operateur;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/operateurs")
public class OperateurController {

    private final OperateurService operateurService;

    public OperateurController(OperateurService operateurService) {
        this.operateurService = operateurService;
    }

    @GetMapping
    public List<Operateur> getAllOperators() {
        return operateurService.getAllOperators();
    }

    @GetMapping("/mon-equipe")
    public List<Operateur> getTeamOperators(@AuthenticationPrincipal UserDetails userDetails) {
        return operateurService.getTeamOperators(userDetails.getUsername());
    }

    @GetMapping("/mes-equipes")
    public List<Equipe> getMyTeams(@AuthenticationPrincipal UserDetails userDetails) {
        return operateurService.getTeamsByChef(userDetails.getUsername());
    }

    @GetMapping("/equipes")
    public List<Equipe> getAllTeams() {
        return operateurService.getAllTeams();
    }

    @PostMapping
    public Operateur createOperator(@RequestBody CreateOperateurRequest request) {
        return operateurService.createOperator(request);
    }

    @PutMapping("/{matricule}/affecter-poste")
    public Operateur assignToWorkstation(@PathVariable String matricule, @RequestBody Map<String, Object> payload) {
        Long posteId = null;
        if (payload.containsKey("posteId") && payload.get("posteId") != null) {
            posteId = Long.valueOf(payload.get("posteId").toString());
        }
        return operateurService.assignToWorkstation(matricule, posteId);
    }

    @GetMapping("/{matricule}/encadrement")
    public List<EncadrementDto> getEncadrement(@PathVariable String matricule) {
        return operateurService.getEncadrement(matricule);
    }

    @GetMapping("/{matricule}/formations")
    public List<FormationPoste> getFormations(@PathVariable String matricule) {
        return operateurService.getFormations(matricule);
    }

 @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @PutMapping("/{matricule}/statut")
    public Operateur updateStatus(@PathVariable String matricule, @RequestBody Map<String, Object> payload) {
        String statut = payload.get("statut").toString();
        return operateurService.updateStatus(matricule, statut);
    }

    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @PutMapping("/{matricule}/absence")
    public Operateur markAbsence(@PathVariable String matricule, @RequestBody Map<String, Object> payload) {
        String motif = payload.get("motif").toString();
        return operateurService.marquerAbsence(matricule, motif);
    }

@PreAuthorize("hasAnyRole('CHEF_EQUIPE', 'RH', 'ADMIN')")
    @PutMapping("/{matricule}/reprise")
    public Operateur markReprise(@PathVariable String matricule) {
        return operateurService.marquerReprise(matricule);
    }

    @PreAuthorize("hasAnyRole('CHEF_EQUIPE', 'RH', 'ADMIN')")
    @PutMapping("/{matricule}/depart")
    public Operateur markDepart(@PathVariable String matricule) {
        return operateurService.marquerDepart(matricule);
    }
}
