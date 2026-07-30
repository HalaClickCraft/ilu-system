package com.ilu.system.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) { this.jwtAuthFilter = jwtAuthFilter; }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers("/api/auth/login").permitAll()
                // User management - ADMIN only
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                // Operator creation - only RH, CHEF_EQUIPE, SUPERVISEUR
                .requestMatchers(HttpMethod.POST, "/api/operators").hasAnyRole("RH", "CHEF_EQUIPE", "SUPERVISEUR")
                // Project creation - only SUPERVISEUR, CHEF_EQUIPE
                .requestMatchers(HttpMethod.POST, "/api/structure/projects").hasAnyRole("SUPERVISEUR", "CHEF_EQUIPE")
                // Zone/Workstation creation - SUPERVISEUR, CHEF_EQUIPE, ADMIN
                .requestMatchers(HttpMethod.POST, "/api/structure/projects/*/zones").hasAnyRole("SUPERVISEUR", "CHEF_EQUIPE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/structure/workstations").hasAnyRole("SUPERVISEUR", "CHEF_EQUIPE", "ADMIN")
                // Cadence entry - CHEF_EQUIPE only
                .requestMatchers(HttpMethod.POST, "/api/training/formations/*/tracking/cadence").hasRole("CHEF_EQUIPE")
                // Defauts entry - AGENT_QUALITE only
                .requestMatchers(HttpMethod.POST, "/api/training/formations/*/tracking/defauts").hasRole("AGENT_QUALITE")
                // Everything else requires authentication
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
}