package com.medcal.controller;

import com.medcal.model.dto.AppointmentDTO;
import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.dto.PatientDTO;
import com.medcal.model.entity.Appointment;
import com.medcal.model.enums.AppointmentType;
import com.medcal.model.request.AppointmentRequest;
import com.medcal.security.CustomUserDetails;
import com.medcal.service.AppointmentService;
import com.medcal.service.DoctorService;
import com.medcal.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentWebController {
    
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    
    @GetMapping
    public String listAppointments(Model model) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
            model.addAttribute("appointments", appointments);
            return "appointments/list";
        } catch (Exception e) {
            log.error("Error listing appointments", e);
            model.addAttribute("error", "Error al cargar la lista de citas: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            List<DoctorDTO> doctors = doctorService.getAllDoctors();
            List<PatientDTO> patients = patientService.getAllPatients();
            
            if (doctors.isEmpty() || patients.isEmpty()) {
                model.addAttribute("error", "No hay doctores o pacientes registrados. Por favor, registre al menos un doctor y un paciente antes de crear una cita.");
                return "error";
            }
            
            // Crear un nuevo AppointmentRequest con valores por defecto
            AppointmentRequest appointmentRequest = AppointmentRequest.builder()
                    .doctorId(doctors.get(0).getId())  // Establecer un doctor por defecto
                    .patientId(patients.get(0).getId()) // Establecer un paciente por defecto
                    .startTime(LocalDateTime.now().plusHours(1)) // Establecer la hora de inicio en 1 hora a partir de ahora
                    .endTime(LocalDateTime.now().plusHours(2))   // Establecer la hora de fin en 2 horas a partir de ahora
                    .type(AppointmentType.CONSULTATION)              // Establecer un tipo de cita por defecto
                    .build();
                    
            model.addAttribute("appointment", appointmentRequest);
            model.addAttribute("doctors", doctors);
            model.addAttribute("patients", patients);
            model.addAttribute("appointmentTypes", AppointmentType.values());
            return "appointments/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping
    public String createAppointment(@Valid @ModelAttribute("appointment") AppointmentRequest appointmentRequest,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("doctors", doctorService.getAllDoctors());
                model.addAttribute("patients", patientService.getAllPatients());
                model.addAttribute("appointmentTypes", AppointmentType.values());
                model.addAttribute("error", "Por favor corrija los errores del formulario");
                return "appointments/form";
            }
            
            // Validate that doctor and patient IDs exist
            if (appointmentRequest.getDoctorId() == null || appointmentRequest.getPatientId() == null) {
                model.addAttribute("doctors", doctorService.getAllDoctors());
                model.addAttribute("patients", patientService.getAllPatients());
                model.addAttribute("appointmentTypes", AppointmentType.values());
                model.addAttribute("error", "Se requiere seleccionar un doctor y un paciente");
                return "appointments/form";
            }

            // Validar fechas
            if (appointmentRequest.getStartTime() == null || appointmentRequest.getEndTime() == null) {
                model.addAttribute("doctors", doctorService.getAllDoctors());
                model.addAttribute("patients", patientService.getAllPatients());
                model.addAttribute("appointmentTypes", AppointmentType.values());
                model.addAttribute("error", "Las fechas de inicio y fin son requeridas");
                return "appointments/form";
            }

            // Validar que la fecha de inicio sea anterior a la de fin
            if (appointmentRequest.getStartTime().isAfter(appointmentRequest.getEndTime())) {
                model.addAttribute("doctors", doctorService.getAllDoctors());
                model.addAttribute("patients", patientService.getAllPatients());
                model.addAttribute("appointmentTypes", AppointmentType.values());
                model.addAttribute("error", "La fecha de inicio debe ser anterior a la fecha de fin");
                return "appointments/form";
            }

            try {
                // Get current authenticated user and set createdBy
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                    appointmentRequest.setCreatedBy(userDetails.getUser().getId());
                }

                Appointment appointment = appointmentRequest.toEntity();
                appointmentService.createAppointment(appointment);
                redirectAttributes.addFlashAttribute("success", "Cita creada exitosamente");
                return "redirect:/appointments";
            } catch (Exception e) {
                model.addAttribute("doctors", doctorService.getAllDoctors());
                model.addAttribute("patients", patientService.getAllPatients());
                model.addAttribute("appointmentTypes", AppointmentType.values());
                model.addAttribute("error", "Error al guardar la cita: " + e.getMessage());
                return "appointments/form";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Error inesperado: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        List<DoctorDTO> doctors = doctorService.getAllDoctors();
        List<PatientDTO> patients = patientService.getAllPatients();
        
        return appointmentService.getAppointmentById(id)
                .map(appointment -> {
                    AppointmentRequest appointmentRequest = AppointmentRequest.builder()
                            .doctorId(appointment.getDoctorId())
                            .patientId(appointment.getPatientId())
                            .startTime(appointment.getStartTime())
                            .endTime(appointment.getEndTime())
                            .type(appointment.getType())
                            .notes(appointment.getNotes())
                            .build();
                    
                    model.addAttribute("appointment", appointmentRequest);
                    model.addAttribute("doctors", doctors);
                    model.addAttribute("patients", patients);
                    model.addAttribute("appointmentTypes", AppointmentType.values());
                    return "appointments/form";
                })
                .orElse("redirect:/appointments");
    }
    
    @PostMapping("/{id}")
    public String updateAppointment(@PathVariable UUID id, 
                                  @Valid @ModelAttribute("appointment") AppointmentRequest appointmentRequest,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("appointmentTypes", AppointmentType.values());
            return "appointments/form";
        }

        try {
            // Get current authenticated user and set createdBy for updates
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                appointmentRequest.setCreatedBy(userDetails.getUser().getId());
            }

            Appointment appointment = appointmentRequest.toEntity();
            appointmentService.updateAppointment(id, appointment);
            redirectAttributes.addFlashAttribute("success", "Cita actualizada exitosamente");
            return "redirect:/appointments";
        } catch (IllegalArgumentException e) {
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("appointmentTypes", AppointmentType.values());
            model.addAttribute("error", e.getMessage());
            return "appointments/form";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteAppointment(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.deleteAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Cita eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la cita: " + e.getMessage());
        }
        return "redirect:/appointments";
    }
    
    @GetMapping("/calendar")
    public String showCalendar(Model model) {
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        model.addAttribute("appointments", appointments);
        return "appointments/calendar";
    }
}
