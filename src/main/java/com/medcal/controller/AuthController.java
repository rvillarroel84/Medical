package com.medcal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas. Por favor intenta nuevamente.");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Has cerrado sesión exitosamente.");
        }
        
        return "auth/login";
    }
    
    // Spring Security handles the POST /auth/login automatically
    // This method is no longer needed as form login is configured in SecurityConfig
    
    @GetMapping("/logout")
    public String logout() {
        // En una implementación real, aquí se invalidaría la sesión y el JWT
        return "redirect:/auth/login?logout";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }
    
    // Authentication is now handled by Spring Security with UserDetailsService
    // No custom validation needed
}
