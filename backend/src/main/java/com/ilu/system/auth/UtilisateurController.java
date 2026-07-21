package com.ilu.system.auth;

import com.ilu.system.auth.dto.CreateUtilisateurRequest;
import com.ilu.system.auth.dto.UtilisateurDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public List<Utilisateur> lister() {
        return utilisateurService.listerTous();
    }

    @GetMapping("/par-role")
    public List<UtilisateurDto> listerParRole(@RequestParam String role) {
        return utilisateurService.listerParRole(role);
    }

    @PostMapping
    public Utilisateur creer(@RequestBody CreateUtilisateurRequest request) {
        return utilisateurService.creerUtilisateur(request);
    }

    @PutMapping("/{id}/suspendre")
    public Utilisateur suspendre(@PathVariable Long id) {
        return utilisateurService.suspendre(id);
    }

    @PutMapping("/{id}/reactiver")
    public Utilisateur reactiver(@PathVariable Long id) {
        return utilisateurService.reactiver(id);
    }
}
