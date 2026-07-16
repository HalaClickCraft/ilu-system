package com.ilu.system.operateur;

import com.ilu.system.auth.Utilisateur;
import com.ilu.system.auth.UtilisateurRepository;
import com.ilu.system.structure.PosteTravail;
import com.ilu.system.structure.PosteTravailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class OperateurService {

    private final OperateurRepository operateurRepository;
    private final EquipeRepository equipeRepository;
    private final PosteTravailRepository posteTravailRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public OperateurService(OperateurRepository operateurRepository,
                            EquipeRepository equipeRepository,
                            PosteTravailRepository posteTravailRepository,
                            UtilisateurRepository utilisateurRepository) {
        this.operateurRepository = operateurRepository;
        this.equipeRepository = equipeRepository;
        this.posteTravailRepository = posteTravailRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public List<Operateur> getAllOperators() {
        return operateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Operateur> getTeamOperators(String chefMatricule) {
        Utilisateur user = utilisateurRepository.findByMatricule(chefMatricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur chef introuvable avec matricule: " + chefMatricule));
        return operateurRepository.findByEquipe_Chef_Id(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Equipe> getTeamsByChef(String chefMatricule) {
        Utilisateur user = utilisateurRepository.findByMatricule(chefMatricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur chef introuvable avec matricule: " + chefMatricule));
        return equipeRepository.findByChef_Id(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Equipe> getAllTeams() {
        return equipeRepository.findAll();
    }

    @Transactional
    public Operateur createOperator(CreateOperateurRequest request) {
        if (operateurRepository.existsById(request.getMatricule())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un opérateur avec le matricule " + request.getMatricule() + " existe déjà.");
        }

        Operateur operateur = new Operateur();
        operateur.setMatricule(request.getMatricule());
        operateur.setNom(request.getNom());

        if (request.getDateEmbauche() != null && !request.getDateEmbauche().isBlank()) {
            operateur.setDateEmbauche(LocalDate.parse(request.getDateEmbauche()));
        } else {
            operateur.setDateEmbauche(LocalDate.now());
        }

        if (request.getDateSortie() != null && !request.getDateSortie().isBlank()) {
            operateur.setDateSortie(LocalDate.parse(request.getDateSortie()));
        }

        operateur.setStatut(request.getStatut() != null ? request.getStatut() : "Actif");
        operateur.setFormationRework(request.isFormationRework());

        if (request.getEquipeId() != null) {
            Equipe equipe = equipeRepository.findById(request.getEquipeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Équipe introuvable avec ID: " + request.getEquipeId()));
            operateur.setEquipe(equipe);
        }

        return operateurRepository.save(operateur);
    }

    @Transactional
    public Operateur updateStatus(String matricule, String statut) {
        Operateur operateur = operateurRepository.findById(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule));
        operateur.setStatut(statut);
        
        if ("Sorti".equalsIgnoreCase(statut) && operateur.getDateSortie() == null) {
            operateur.setDateSortie(LocalDate.now());
        } else if (!"Sorti".equalsIgnoreCase(statut)) {
            operateur.setDateSortie(null);
        }
        
        return operateurRepository.save(operateur);
    }

    @Transactional
    public Operateur assignToWorkstation(String matricule, Long posteId) {
        Operateur operateur = operateurRepository.findById(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule));

        if (posteId == null) {
            operateur.setPosteAffecte(null);
        } else {
            PosteTravail poste = posteTravailRepository.findById(posteId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste de travail introuvable avec ID: " + posteId));
            operateur.setPosteAffecte(poste);
        }

        return operateurRepository.save(operateur);
    }
}
