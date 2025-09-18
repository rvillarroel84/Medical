package com.medcal.service;

import com.medcal.model.entity.User;
import com.medcal.model.enums.Role;
import com.medcal.repository.UserRepository;
import com.medcal.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Map role_id to Role enum
        Role role = mapRoleIdToEnum(user.getRoleId());
        user.setRole(role);
        
        logger.debug("User found: {} with role: {}", user.getEmail(), role);
        
        return new CustomUserDetails(user);
    }
    
    private Role mapRoleIdToEnum(UUID roleId) {
        if (roleId == null) return Role.PATIENT;
        
        String roleIdStr = roleId.toString();
        switch (roleIdStr) {
            case "11111111-1111-1111-1111-111111111111":
                return Role.ADMIN;
            case "22222222-2222-2222-2222-222222222222":
                return Role.DOCTOR;
            case "33333333-3333-3333-3333-333333333333":
                return Role.PATIENT;
            case "44444444-4444-4444-4444-444444444444":
                return Role.RECEPTIONIST;
            default:
                return Role.PATIENT;
        }
    }
    
}
