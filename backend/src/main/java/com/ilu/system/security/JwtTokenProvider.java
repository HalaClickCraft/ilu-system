package com.ilu.system.security;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}") private String jwtSecret;
    @Value("${app.jwt.expiration-ms}") private long jwtExpirationMs;
    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private SecretKey getSigningKey() { return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)); }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        // Fetch user to include roles and name in token
        List<String> roles = new ArrayList<>();
        String name = "";
        Long userId = null;
        Optional<User> userOpt = userRepository.findByEmployeeId(userDetails.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userId = user.getId();
            name = user.getName();
            roles = user.getRoles().stream().map(r -> r.getLabel()).collect(Collectors.toList());
        }

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("name", name)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try { Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }
}
