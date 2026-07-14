package com.ilu.system.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DatabaseSeeder(RoleRepository roleRepository, UtilisateurRepository utilisateurRepository) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Seed Roles
        for (RoleType type : RoleType.values()) {
            if (roleRepository.findByLibelle(type).isEmpty()) {
                Role role = new Role();
                role.setLibelle(type);
                roleRepository.save(role);
            }
        }

        // Seed Users
        seedUser("admin", "Administrateur", "12345678", RoleType.ADMIN, "admin", false);
        seedUser("chef1", "Jean Chef", "chef123", RoleType.CHEF_EQUIPE, "chef1", true); // will force password change!
        seedUser("rh1", "Sophie RH", "rh123", RoleType.RH, "rh1", false);
        seedUser("qualite1", "Marc Qualité", "q123", RoleType.QUALITE, "qualite1", false);
        seedUser("hse1", "Hélène HSE", "hse123", RoleType.HSE, "hse1", false);
        seedUser("super1", "Paul Superviseur", "super123", RoleType.SUPERVISEUR, "super1", false);
    }

    private void seedUser(String matricule, String nom, String cin, RoleType roleType, String password, boolean doitChangerMdp) {
        if (utilisateurRepository.findByMatricule(matricule).isEmpty()) {
            Role role = roleRepository.findByLibelle(roleType)
                    .orElseThrow(() -> new IllegalStateException("Role " + roleType + " not found"));

            Utilisateur user = new Utilisateur();
            user.setMatricule(matricule);
            user.setNom(nom);
            user.setCin(cin);
            user.setPassword(password); // plain text
            user.setDoitChangerMdp(doitChangerMdp);
            user.setActif(true);
            user.setRole(role);

            utilisateurRepository.save(user);
        }
    }
}
