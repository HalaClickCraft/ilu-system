package com.ilu.system.auth;

import com.ilu.system.auth.dto.CreateUtilisateurRequest;
import com.ilu.system.auth.dto.UtilisateurDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Utilisateur creerUtilisateur(CreateUtilisateurRequest request) {
        Role role = roleRepository.findByLibelle(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Role inconnu: " + request.getRole()));

        Utilisateur user = new Utilisateur();
        user.setMatricule(request.getMatricule());
        user.setNom(request.getNom());
        user.setCin(request.getCin());
        user.setPassword(passwordEncoder.encode(request.getCin())); // mot de passe initial = CIN
        user.setDoitChangerMdp(true);
        user.setActif(true);
        user.setRole(role);

        return utilisateurRepository.save(user);
    }

    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAll();
    }

    public List<UtilisateurDto> listerParRole(RoleType roleType) {
        return utilisateurRepository.findByRoleLibelleAndActifTrue(roleType).stream()
                .map(user -> new UtilisateurDto(
                        user.getId(),
                        user.getMatricule(),
                        user.getNom(),
                        user.getCin(),
                        user.getRole().getLibelle().name(),
                        user.isActif(),
                        user.isDoitChangerMdp()
                ))
                .collect(Collectors.toList());
    }

    public Utilisateur suspendre(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.setActif(false);
        return utilisateurRepository.save(user);
    }

    public Utilisateur reactiver(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.setActif(true);
        return utilisateurRepository.save(user);
    }
}