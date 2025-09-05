package com.medcal.service;

import com.medcal.model.entity.User;
import com.medcal.model.enums.Role;
import com.medcal.repository.UserRepository;
import com.medcal.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createDemoUser(email));
        
        return new CustomUserDetails(user);
    }
    
    private User createDemoUser(String email) {
        // Crear usuarios demo si no existen en la base de datos
        switch (email) {
            case "doctor@medcal.com":
                return createUser(email, "doctor123", Role.DOCTOR, "Dr. Juan", "García");
            case "admin@medcal.com":
                return createUser(email, "admin123", Role.ADMIN, "Admin", "Sistema");
            case "recepcion@medcal.com":
                return createUser(email, "recepcion123", Role.RECEPTIONIST, "Ana", "Recepción");
            default:
                throw new UsernameNotFoundException("Usuario no encontrado: " + email);
        }
    }
    
    private User createUser(String email, String password, Role role, String firstName, String lastName) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .firstName(firstName)
                .lastName(lastName)
                .phone("555-0123")
                .build();
    }
}
