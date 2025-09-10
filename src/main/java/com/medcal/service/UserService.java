package com.medcal.service;

import com.medcal.model.entity.User;
import com.medcal.model.enums.Role;
import com.medcal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public User createUser(String email, String firstName, String lastName, String phone, Role role) {
        // Generar una contraseña temporal
        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
        
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .role(role)
                .build();
        
        return userRepository.save(user);
    }
}
