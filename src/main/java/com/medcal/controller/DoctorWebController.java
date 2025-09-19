package com.medcal.controller;

import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.entity.Doctor;
import com.medcal.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorWebController {
    
    private final DoctorService doctorService;
    
    @GetMapping
    public String listDoctors(Model model, @RequestParam(required = false) String search) {
        List<DoctorDTO> doctors;
        if (search != null && !search.isEmpty()) {
            doctors = doctorService.searchDoctorsByName(search);
        } else {
            doctors = doctorService.getAllDoctors();
        }
        model.addAttribute("doctors", doctors);
        model.addAttribute("searchTerm", search);
        return "doctors/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("doctor", Doctor.builder()
            .workingHours(new HashMap<>())
            .build());
        return "doctors/form";
    }
    
    @PostMapping
    public String createDoctor(@ModelAttribute Doctor doctor, @RequestParam Map<String, String> params, Model model) {
        try {
            // Procesar el horario de trabajo
            Map<String, Map<String, String>> workingHours = new HashMap<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                String dayKey = day.toString().toLowerCase();
                String enabledParam = params.get("workingHours[" + day + "].enabled");
                if ("on".equals(enabledParam)) {
                    Map<String, String> daySchedule = new HashMap<>();
                    String startTime = params.get("workingHours[" + day + "].start");
                    String endTime = params.get("workingHours[" + day + "].end");
                    if (startTime != null && endTime != null && !startTime.isEmpty() && !endTime.isEmpty()) {
                        daySchedule.put("start", startTime);
                        daySchedule.put("end", endTime);
                        workingHours.put(dayKey, daySchedule);
                    }
                }
            }
            doctor.setWorkingHours(workingHours);
            
            // Generar un UUID temporal para el userId si no existe
            if (doctor.getUserId() == null) {
                doctor.setUserId(UUID.randomUUID());
            }
            
            // Crear el doctor
            DoctorDTO createdDoctor = doctorService.createDoctor(doctor);
            return "redirect:/doctors";
            
        } catch (IllegalArgumentException e) {
            // Errores de validación
            model.addAttribute("error", e.getMessage());
            return "doctors/form";
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            
            // Add error message and return to form
            model.addAttribute("error", "Error al guardar el doctor. Por favor, verifica los datos e intenta nuevamente.");
            return "doctors/form";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        doctorService.getDoctorById(id).ifPresent(doctor -> {
            if (doctor.getWorkingHours() == null) {
                doctor.setWorkingHours(new HashMap<>());
            }
            model.addAttribute("doctor", doctor);
        });
        return "doctors/form";
    }
    
    @PostMapping("/{id}")
    public String updateDoctor(@PathVariable UUID id, @ModelAttribute Doctor doctor) {
        doctorService.updateDoctor(id, doctor);
        return "redirect:/doctors";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
        return "redirect:/doctors";
    }
}
