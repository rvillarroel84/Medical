package com.medcal.controller;

import com.medcal.model.dto.AppointmentDTO;
import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.dto.PatientDTO;
import com.medcal.model.entity.Appointment;
import com.medcal.model.enums.AppointmentType;
import com.medcal.model.request.AppointmentRequest;
import com.medcal.service.AppointmentService;
import com.medcal.service.DoctorService;
import com.medcal.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentWebController {
    
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    
    @GetMapping
    public String listAppointments(Model model) {
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        model.addAttribute("appointments", appointments);
        return "appointments/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<DoctorDTO> doctors = doctorService.getAllDoctors();
        List<PatientDTO> patients = patientService.getAllPatients();
        
        model.addAttribute("appointment", new AppointmentRequest());
        model.addAttribute("doctors", doctors);
        model.addAttribute("patients", patients);
        model.addAttribute("appointmentTypes", AppointmentType.values());
        return "appointments/form";
    }
    
    @PostMapping
    public String createAppointment(@Valid @ModelAttribute("appointment") AppointmentRequest appointmentRequest, 
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
            Appointment appointment = appointmentRequest.toEntity();
            appointmentService.createAppointment(appointment);
            redirectAttributes.addFlashAttribute("success", "Cita creada exitosamente");
            return "redirect:/appointments";
        } catch (IllegalArgumentException e) {
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("appointmentTypes", AppointmentType.values());
            model.addAttribute("error", e.getMessage());
            return "appointments/form";
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
