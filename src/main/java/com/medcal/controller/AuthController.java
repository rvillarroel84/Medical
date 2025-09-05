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
    
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                              @RequestParam String password,
                              @RequestParam(required = false) String rememberMe,
                              Model model) {
        
        // Validación básica de credenciales demo
        if (isValidDemoCredentials(email, password)) {
            // En una implementación real, aquí se generaría el JWT y se establecería la sesión
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Credenciales inválidas. Usa una de las cuentas de demostración.");
            return "auth/login";
        }
    }
    
    @GetMapping("/logout")
    public String logout() {
        // En una implementación real, aquí se invalidaría la sesión y el JWT
        return "redirect:/auth/login?logout";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }
    
    private boolean isValidDemoCredentials(String email, String password) {
        // Credenciales de demostración
        return ("doctor@medcal.com".equals(email) && "doctor123".equals(password)) ||
               ("admin@medcal.com".equals(email) && "admin123".equals(password)) ||
               ("recepcion@medcal.com".equals(email) && "recepcion123".equals(password));
    }
}
