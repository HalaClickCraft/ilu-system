package com.ilu.system.auth.controller;
import com.ilu.system.auth.dto.CreateUserRequest;
import com.ilu.system.auth.dto.UserDto;
import com.ilu.system.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> listAllUsers() { return ResponseEntity.ok(userService.listAll()); }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) { return ResponseEntity.ok(userService.findById(id)); }
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) { return ResponseEntity.ok(userService.toggleActiveStatus(id)); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) { userService.deleteUser(id); return ResponseEntity.noContent().build(); }
    @PutMapping("/{id}/roles")
public ResponseEntity<UserDto> updateUserRoles(
        @PathVariable Long id,
        @RequestBody Map<String, Object> body) {
    @SuppressWarnings("unchecked")
    Set<String> roles = new HashSet<>((List<String>) body.get("roles"));
    String department = (String) body.get("department");
    return ResponseEntity.ok(userService.updateUserRoles(id, roles, department));
}

@PostMapping("/seed-roles")
public ResponseEntity<String> seedRoles() {
    userService.seedRoles();
    return ResponseEntity.ok("Roles seeded successfully");
}
}
