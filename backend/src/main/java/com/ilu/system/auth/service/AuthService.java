package com.ilu.system.auth.service;
import com.ilu.system.auth.dto.*;
import com.ilu.system.auth.entity.Role;
import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }
    public LoginResponse login(LoginRequest request) {
        String empId = request.getEmployeeId() != null ? request.getEmployeeId().trim() : "";
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(empId, request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmployeeIdIgnoreCase(empId)
                .orElseGet(() -> userRepository.findByEmployeeId(empId)
                .orElseThrow(() -> new RuntimeException("User not found")));
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setToken(token);
        response.setEmployeeId(user.getEmployeeId());
        response.setName(user.getName());
        response.setMustChangePassword(user.getMustChangePassword());
        response.setRoles(user.getRoles().stream().map(Role::getLabel).collect(Collectors.toSet()));
        return response;
    }
    @Transactional
    public void changePassword(String employeeId, ChangePasswordRequest request) {
        User user = userRepository.findByEmployeeId(employeeId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) throw new RuntimeException("Current password is incorrect");
        if (!request.getNewPassword().equals(request.getConfirmPassword())) throw new RuntimeException("Passwords do not match");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }
}
