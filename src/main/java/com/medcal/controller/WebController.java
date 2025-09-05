package com.medcal.controller;

import com.medcal.model.dto.DoctorDTO;
import com.medcal.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebController {
    
    private final DoctorService doctorService;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Estadísticas básicas para el dashboard
        List<DoctorDTO> recentDoctors = doctorService.getAllDoctors();
        model.addAttribute("totalDoctors", recentDoctors.size());
        model.addAttribute("recentDoctors", recentDoctors.stream().limit(5).toList());
        return "dashboard";
    }
    
    @GetMapping("/doctors")
    public String doctors(Model model, @RequestParam(required = false) String search) {
        List<DoctorDTO> doctors;
        
        if (search != null && !search.trim().isEmpty()) {
            doctors = doctorService.searchDoctorsByName(search.trim());
            model.addAttribute("searchTerm", search);
        } else {
            doctors = doctorService.getAllDoctors();
        }
        
        model.addAttribute("doctors", doctors);
        return "doctors/list";
    }
    
    @GetMapping("/doctors/{id}")
    public String doctorDetail(@PathVariable UUID id, Model model) {
        return doctorService.getDoctorById(id)
                .map(doctor -> {
                    model.addAttribute("doctor", doctor);
                    return "doctors/detail";
                })
                .orElse("redirect:/doctors?error=not-found");
    }
    
    @GetMapping("/doctors/new")
    public String newDoctor(Model model) {
        model.addAttribute("doctor", new DoctorDTO());
        return "doctors/form";
    }
    
    @GetMapping("/doctors/{id}/edit")
    public String editDoctor(@PathVariable UUID id, Model model) {
        return doctorService.getDoctorById(id)
                .map(doctor -> {
                    model.addAttribute("doctor", doctor);
                    return "doctors/form";
                })
                .orElse("redirect:/doctors?error=not-found");
    }
    
    @GetMapping("/patients")
    public String patients(Model model) {
        // TODO: Implementar servicio de pacientes
        return "patients/list";
    }
    
    @GetMapping("/appointments")
    public String appointments(Model model) {
        // TODO: Implementar servicio de citas
        return "appointments/calendar";
    }
}
