package com.ilu.system.auth.controller;
import com.ilu.system.auth.dto.ChangePasswordRequest;
import com.ilu.system.auth.dto.LoginRequest;
import com.ilu.system.auth.dto.LoginResponse;
import com.ilu.system.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) { return ResponseEntity.ok(authService.login(request)); }
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}
