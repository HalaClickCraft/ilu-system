package com.ilu.system.security;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }
    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        User user = userRepository.findByEmployeeIdIgnoreCase(employeeId.trim())
                .orElseGet(() -> userRepository.findByEmployeeId(employeeId.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + employeeId)));
        return new org.springframework.security.core.userdetails.User(user.getEmployeeId(), user.getPassword(), user.getActive(), true, true, true,
                user.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getLabel())).collect(Collectors.toList()));
    }
}