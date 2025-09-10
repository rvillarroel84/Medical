package com.medcal.controller;

import com.medcal.model.dto.AppointmentDTO;
import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.dto.PatientDTO;
import com.medcal.service.AppointmentService;
import com.medcal.service.DoctorService;
import com.medcal.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {
    
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Estadísticas para el dashboard
        List<DoctorDTO> recentDoctors = doctorService.getAllDoctors();
        List<PatientDTO> patients = patientService.getAllPatients();
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        
        // Total de doctores y doctores recientes
        model.addAttribute("totalDoctors", recentDoctors.size());
        model.addAttribute("recentDoctors", recentDoctors.stream().limit(5).toList());
        
        // Total de pacientes
        model.addAttribute("totalPatients", patients.size());
        
        // Citas de hoy y pendientes
        long todayAppointments = appointments.stream()
            .filter(apt -> apt.getStartTime().toLocalDate().equals(java.time.LocalDate.now()))
            .count();
        long pendingAppointments = appointments.stream()
            .filter(apt -> apt.getStartTime().isAfter(java.time.LocalDateTime.now()))
            .count();
            
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("pendingAppointments", pendingAppointments);
        
        return "dashboard";
    }
}
