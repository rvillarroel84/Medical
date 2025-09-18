package com.medcal.controller.api;

import com.medcal.model.dto.AppointmentDTO;
import com.medcal.model.dto.AppointmentRequest;
import com.medcal.model.dto.AvailabilitySlot;
import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.entity.Appointment;
import com.medcal.model.enums.AppointmentStatus;
import com.medcal.service.AppointmentService;
import com.medcal.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentApiController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        // Get the current authenticated user (patient)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID patientId = UUID.fromString(authentication.getName());
        
        // Set the patient ID and default status to PENDING
        request.setPatientId(patientId);
        request.setStatus(AppointmentStatus.PENDING);
        
        Appointment appointment = appointmentService.scheduleAppointment(request);
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }

    @GetMapping("/doctors/available")
    public ResponseEntity<List<DoctorDTO>> getAvailableDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        // Convert LocalDate to LocalDateTime for the service call
        LocalDateTime startOfDay = date != null ? date.atStartOfDay() : null;
        LocalDateTime endOfDay = date != null ? date.plusDays(1).atStartOfDay() : null;
        
        List<DoctorDTO> availableDoctors = doctorService.findAvailableDoctors(specialty, startOfDay, endOfDay);
        return ResponseEntity.ok(availableDoctors);
    }

    @GetMapping("/doctors/{doctorId}/availability")
    public ResponseEntity<List<AvailabilitySlot>> getDoctorAvailability(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Optional<DoctorDTO> doctorOpt = doctorService.getDoctorById(doctorId);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Convert LocalDate to LocalDateTime for the service call
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<AvailabilitySlot> slots = doctorService.getAvailableSlots(doctorId, startOfDay, endOfDay);
        return ResponseEntity.ok(slots);
    }

    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable UUID appointmentId,
            @RequestParam UUID doctorId,
            @RequestParam AppointmentStatus status) {
        
        // Verify the doctor is authorized to update this appointment
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getName().equals(doctorId.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<AppointmentDTO> updatedAppointment = appointmentService.updateAppointmentStatus(appointmentId, status);
        return updatedAppointment
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getUserAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DOCTOR"));
        
        List<AppointmentDTO> appointments;
        if (isDoctor) {
            appointments = appointmentService.getDoctorAppointments(userId);
        } else {
            appointments = appointmentService.getPatientAppointments(userId);
        }
        
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        
        Optional<AppointmentDTO> appointmentOpt = appointmentService.getAppointmentById(id);
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        AppointmentDTO appointment = appointmentOpt.get();
        
        // Verify the user is authorized to view this appointment
        boolean isAuthorized = appointment.getPatientId().equals(userId) || 
                             (appointment.getDoctorId() != null && appointment.getDoctorId().equals(userId));
        
        if (!isAuthorized) {
            throw new AccessDeniedException("Not authorized to view this appointment");
        }
        
        return ResponseEntity.ok(appointment);
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable UUID doctorId) {
        // Verify the doctor is authorized to view these appointments
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        
        if (!doctorId.equals(userId) && !isAdmin(authentication)) {
            throw new AccessDeniedException("Not authorized to view these appointments");
        }
        
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable UUID patientId) {
        // Verify the patient is authorized to view their own appointments
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        
        if (!patientId.equals(userId) && !isAdmin(authentication)) {
            throw new AccessDeniedException("Not authorized to view these appointments");
        }
        
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        // Only admins can filter by status
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(authentication)) {
            throw new AccessDeniedException("Not authorized to filter by status");
        }
        
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/doctor/{doctorId}/range")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorAndDateRange(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
                
        // Verify the doctor is authorized to view these appointments
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        
        if (!doctorId.equals(userId) && !isAdmin(authentication)) {
            throw new AccessDeniedException("Not authorized to view these appointments");
        }
        
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorAndDateRange(doctorId, startDate, endDate);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/patient/{patientId}/range")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientAndDateRange(
            @PathVariable UUID patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
                
        // Verify the patient is authorized to view their own appointments
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        
        if (!patientId.equals(userId) && !isAdmin(authentication)) {
            throw new AccessDeniedException("Not authorized to view these appointments");
        }
        
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatientAndDateRange(patientId, startDate, endDate);
        return ResponseEntity.ok(appointments);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable UUID id) {
        try {
            // Verify the user is authorized to delete this appointment
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            Optional<AppointmentDTO> appointmentOpt = appointmentService.getAppointmentById(id);
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            AppointmentDTO appointment = appointmentOpt.get();
            boolean isAuthorized = appointment.getPatientId().equals(userId) || 
                                 (appointment.getDoctorId() != null && appointment.getDoctorId().equals(userId)) ||
                                 isAdmin(authentication);
            
            if (!isAuthorized) {
                throw new AccessDeniedException("Not authorized to delete this appointment");
            }
            
            boolean deleted = appointmentService.deleteAppointment(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting appointment: " + e.getMessage());
        }
    }
    
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}
