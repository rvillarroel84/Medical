package com.medcal.controller;

import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.entity.Doctor;
import com.medcal.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/htmx/doctors")
@RequiredArgsConstructor
public class DoctorHtmxController {
    
    private final DoctorService doctorService;
    
    @GetMapping
    public String searchDoctors(@RequestParam(required = false) String search, 
                               @RequestParam(required = false) String specialization,
                               Model model) {
        List<DoctorDTO> doctors;
        
        if (search != null && !search.trim().isEmpty()) {
            doctors = doctorService.searchDoctorsByName(search.trim());
        } else if (specialization != null && !specialization.trim().isEmpty()) {
            doctors = doctorService.getDoctorsBySpecialization(specialization);
        } else {
            doctors = doctorService.getAllDoctors();
        }
        
        model.addAttribute("doctors", doctors);
        model.addAttribute("searchTerm", search);
        return "doctors/fragments/table :: doctors-table";
    }
    
    @PostMapping
    public String createDoctor(@ModelAttribute Doctor doctor, Model model) {
        try {
            DoctorDTO createdDoctor = doctorService.createDoctor(doctor);
            model.addAttribute("success", "Doctor creado exitosamente");
            return "redirect:/doctors";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el doctor: " + e.getMessage());
            model.addAttribute("doctor", doctor);
            return "doctors/form";
        }
    }
    
    @PutMapping("/{id}")
    public String updateDoctor(@PathVariable UUID id, @ModelAttribute Doctor doctor, Model model) {
        try {
            doctorService.updateDoctor(id, doctor)
                    .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
            model.addAttribute("success", "Doctor actualizado exitosamente");
            return "redirect:/doctors/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el doctor: " + e.getMessage());
            model.addAttribute("doctor", doctor);
            return "doctors/form";
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDoctor(@PathVariable UUID id) {
        try {
            boolean deleted = doctorService.deleteDoctor(id);
            if (deleted) {
                return ResponseEntity.ok()
                        .header("HX-Trigger", "doctorDeleted")
                        .body("");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .header("HX-Trigger", "error")
                    .body("Error al eliminar el doctor");
        }
    }
    
    @GetMapping("/{id}/card")
    public String getDoctorCard(@PathVariable UUID id, Model model) {
        return doctorService.getDoctorById(id)
                .map(doctor -> {
                    model.addAttribute("doctor", doctor);
                    return "doctors/fragments/card :: doctor-card";
                })
                .orElse("error/404");
    }
}
