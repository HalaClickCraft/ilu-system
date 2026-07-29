package com.ilu.system.auth.service;
import com.ilu.system.auth.dto.*;
import com.ilu.system.auth.entity.Role;
import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.RoleRepository;
import com.ilu.system.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new RuntimeException("User with employee ID '" + request.getEmployeeId() + "' already exists");
        }
        if (userRepository.existsByNationalId(request.getNationalId())) {
            throw new RuntimeException("User with national ID '" + request.getNationalId() + "' already exists");
        }
        User user = new User();
        user.setEmployeeId(request.getEmployeeId());
        user.setName(request.getName());
        user.setNationalId(request.getNationalId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMustChangePassword(true);
        user.setActive(true);
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null) {
            for (String roleLabel : request.getRoles()) {
                Role role = roleRepository.findByLabel(roleLabel)
                        .orElseThrow(() -> new RuntimeException("Role '" + roleLabel + "' not found"));
                roles.add(role);
            }
        }
        user.setRoles(roles);
        return toDto(userRepository.save(user));
    }
    public List<UserDto> listAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }
    public UserDto findById(Long id) {
        return toDto(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with ID: " + id)));
    }
    public UserDto findByEmployeeId(String employeeId) {
        return toDto(userRepository.findByEmployeeId(employeeId).orElseThrow(() -> new RuntimeException("User not found")));
    }
    @Transactional
    public UserDto toggleActiveStatus(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.getActive());
        return toDto(userRepository.save(user));
    }
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new RuntimeException("User not found");
        userRepository.deleteById(id);
    }
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setName(user.getName());
        dto.setNationalId(user.getNationalId());
        dto.setMustChangePassword(user.getMustChangePassword());
        dto.setActive(user.getActive());
        dto.setRoles(user.getRoles().stream().map(Role::getLabel).collect(Collectors.toSet()));
        return dto;
    }
}
