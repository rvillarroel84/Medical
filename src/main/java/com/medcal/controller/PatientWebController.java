package com.medcal.controller;

import com.medcal.model.dto.PatientDTO;
import com.medcal.model.entity.Patient;
import com.medcal.service.PatientService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientWebController {
    
    private final PatientService patientService;
    private final EntityManager entityManager;
    
    @GetMapping
    public String listPatients(@RequestParam(required = false) String search, Model model) {
        List<PatientDTO> patients;
        if (search != null && !search.trim().isEmpty()) {
            patients = patientService.searchPatientsByName(search);
            model.addAttribute("searchTerm", search);
        } else {
            patients = patientService.getAllPatients();
        }
        System.out.println("Pacientes cargados: " + patients); // Debug
        model.addAttribute("patients", patients);
        return "patients/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patients/form";
    }
    
    @PostMapping
    @Transactional
    public String createPatient(@ModelAttribute("patient") Patient patient, BindingResult result, Model model) {
        try {
            if (result.hasErrors()) {
                return "patients/form";
            }
            patientService.createPatient(patient);
            // Forzar la limpieza de la caché de Hibernate
            entityManager.flush();
            entityManager.clear();
            return "redirect:/patients";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el paciente: " + e.getMessage());
            return "patients/form";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        patientService.getPatientById(id).ifPresent(patient -> 
            model.addAttribute("patient", patient)
        );
        return "patients/form";
    }
    
    @PostMapping("/{id}")
    public String updatePatient(@PathVariable UUID id, @ModelAttribute Patient patient) {
        patientService.updatePatient(id, patient);
        return "redirect:/patients";
    }
    
    @PostMapping("/{id}/delete")
    public String deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return "redirect:/patients";
    }
}
