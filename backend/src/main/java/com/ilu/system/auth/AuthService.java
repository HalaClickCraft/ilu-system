package com.ilu.system.auth;

import com.ilu.system.auth.dto.*;
import com.ilu.system.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByMatricule(request.getMatricule())
                .orElseThrow(() -> new BadCredentialsException("Matricule ou mot de passe incorrect"));

        if (!user.isActif()) {
            throw new BadCredentialsException("Compte suspendu");
        }
        if (!passwordMatches(request.getMotDePasse(), user.getPassword())) {
            throw new BadCredentialsException("Matricule ou mot de passe incorrect");
        }

        String token = jwtUtil.generateToken(user.getMatricule(), user.getRole().getLibelle().name());
        return new LoginResponse(token, user.getMatricule(), user.getNom(), user.getRole().getLibelle().name(), user.isDoitChangerMdp());
    }

    public void changePassword(String matricule, ChangePasswordRequest request) {
        Utilisateur user = utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

        if (!passwordMatches(request.getAncienMotDePasse(), user.getPassword())) {
            throw new BadCredentialsException("Ancien mot de passe incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        user.setDoitChangerMdp(false);
        utilisateurRepository.save(user);
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        return rawPassword.equals(storedPassword);
    }
}