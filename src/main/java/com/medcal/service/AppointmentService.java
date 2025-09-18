package com.medcal.service;

import com.medcal.model.dto.AppointmentDTO;
import com.medcal.model.dto.AppointmentRequest;
import com.medcal.model.entity.Appointment;
import com.medcal.model.entity.Doctor;
import com.medcal.model.enums.AppointmentStatus;
import com.medcal.model.enums.AppointmentType;
import com.medcal.exception.ConflictException;
import com.medcal.exception.ResourceNotFoundException;
import com.medcal.repository.AppointmentRepository;
import com.medcal.repository.DoctorRepository;
import com.medcal.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<AppointmentDTO> getAppointmentById(UUID id) {
        return appointmentRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public List<AppointmentDTO> getAppointmentsByDoctorId(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getAppointmentsByPatientId(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getAppointmentsByDoctorAndDateRange(UUID doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByDoctorIdAndDateRange(doctorId, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getAppointmentsByPatientAndDateRange(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByPatientIdAndDateRange(patientId, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AppointmentDTO createAppointment(Appointment appointment) {
        // Validaciones básicas
        validateAppointment(appointment);
        
        // Verificar que el doctor existe
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("El doctor especificado no existe"));
        
        // Verificar que el paciente existe
        if (!patientRepository.existsById(appointment.getPatientId())) {
            throw new IllegalArgumentException("El paciente especificado no existe");
        }
        
        // Verificar conflictos de horarios
        validateNoConflicts(appointment);
        
        // Verificar horarios de trabajo del doctor
        validateDoctorWorkingHours(doctor, appointment);
        
        // Establecer estado por defecto si no se especifica
        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.SCHEDULED);
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }
    
    @Transactional
    public Optional<AppointmentDTO> updateAppointment(UUID id, Appointment appointmentDetails) {
        return appointmentRepository.findById(id)
                .map(appointment -> {
                    // Actualizar campos
                    appointment.setDoctorId(appointmentDetails.getDoctorId());
                    appointment.setPatientId(appointmentDetails.getPatientId());
                    appointment.setStartTime(appointmentDetails.getStartTime());
                    appointment.setEndTime(appointmentDetails.getEndTime());
                    appointment.setType(appointmentDetails.getType());
                    appointment.setStatus(appointmentDetails.getStatus());
                    appointment.setNotes(appointmentDetails.getNotes());
                    
                    // Validar antes de guardar
                    validateAppointment(appointment);
                    
                    // Verificar conflictos solo si cambió el horario o doctor
                    validateNoConflictsForUpdate(appointment);
                    
                    return convertToDTO(appointmentRepository.save(appointment));
                });
    }
    
    @Transactional
    public Optional<AppointmentDTO> updateAppointmentStatus(UUID id, AppointmentStatus status) {
        return appointmentRepository.findById(id)
                .map(appointment -> {
                    appointment.setStatus(status);
                    return convertToDTO(appointmentRepository.save(appointment));
                });
    }
    
    @Transactional
    public boolean deleteAppointment(UUID id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Transactional
    public Appointment scheduleAppointment(AppointmentRequest request) {
        UUID patientId = request.getPatientId();
        UUID doctorId = request.getDoctorId();
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        String notes = request.getNotes();
        AppointmentType type = request.getType();
        AppointmentStatus status = request.getStatus() != null ? request.getStatus() : AppointmentStatus.PENDING;
        
        // Validate appointment time
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule appointment in the past");
        }
        
        // Check if patient exists
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
                
        // Check if doctor exists and is active
        Doctor doctor = doctorRepository.findByIdAndActiveTrue(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Active doctor not found with id: " + doctorId));
                
        // Check for scheduling conflicts
        if (hasSchedulingConflict(doctorId, startTime, endTime, null)) {
            throw new ConflictException("Doctor already has an appointment during this time");
        }
        
        // Create and save the appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctor.getId());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setNotes(notes);
        appointment.setType(type);
        appointment.setStatus(status);
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Checks if there's a scheduling conflict for the given doctor and time range.
     * @param doctorId The ID of the doctor
     * @param startTime Start time of the appointment
     * @param endTime End time of the appointment
     * @param excludeAppointmentId Optional appointment ID to exclude from conflict check (for updates)
     * @return true if there's a conflict, false otherwise
     */
    public boolean hasSchedulingConflict(UUID doctorId, LocalDateTime startTime, 
                                       LocalDateTime endTime, UUID excludeAppointmentId) {
        // Find all appointments for the doctor that overlap with the given time range
        List<Appointment> conflictingAppointments = appointmentRepository
                .findByDoctorIdAndTimeRange(doctorId, startTime, endTime);
                
        // If we're updating an appointment, exclude it from the conflict check
        if (excludeAppointmentId != null) {
            conflictingAppointments = conflictingAppointments.stream()
                    .filter(appt -> !appt.getId().equals(excludeAppointmentId))
                    .collect(Collectors.toList());
        }
        
        // Also check for any appointments that would overlap with the new one
        return !conflictingAppointments.isEmpty();
    }
    
    public boolean isDoctorAvailable(UUID doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(doctorId, startTime, endTime);
        return conflicts.isEmpty();
    }
    
    @Transactional
    public Appointment cancelAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
                
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }
    
    private void validateAppointment(Appointment appointment) {
        if (appointment.getDoctorId() == null) {
            throw new IllegalArgumentException("El ID del doctor es obligatorio");
        }
        
        if (appointment.getPatientId() == null) {
            throw new IllegalArgumentException("El ID del paciente es obligatorio");
        }
        
        if (appointment.getStartTime() == null) {
            throw new IllegalArgumentException("La hora de inicio es obligatoria");
        }
        
        if (appointment.getEndTime() == null) {
            throw new IllegalArgumentException("La hora de fin es obligatoria");
        }
        
        if (appointment.getType() == null) {
            throw new IllegalArgumentException("El tipo de cita es obligatorio");
        }
        
        if (appointment.getCreatedBy() == null) {
            throw new IllegalArgumentException("El ID del usuario que crea la cita es obligatorio");
        }
        
        // Validar que la hora de fin sea posterior a la de inicio
        if (appointment.getEndTime().isBefore(appointment.getStartTime()) || 
            appointment.getEndTime().isEqual(appointment.getStartTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
        
        // Validar que la cita no sea en el pasado
        if (appointment.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden crear citas en el pasado");
        }
        
        // Validar duración mínima y máxima
        long durationMinutes = java.time.Duration.between(appointment.getStartTime(), appointment.getEndTime()).toMinutes();
        if (durationMinutes < 15) {
            throw new IllegalArgumentException("La duración mínima de una cita es de 15 minutos");
        }
        if (durationMinutes > 480) { // 8 horas
            throw new IllegalArgumentException("La duración máxima de una cita es de 8 horas");
        }
    }
    
    private void validateNoConflicts(Appointment appointment) {
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                appointment.getDoctorId(), 
                appointment.getStartTime(), 
                appointment.getEndTime()
        );
        
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("El doctor ya tiene una cita programada en ese horario");
        }
    }
    
    private void validateNoConflictsForUpdate(Appointment appointment) {
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                appointment.getDoctorId(), 
                appointment.getStartTime(), 
                appointment.getEndTime()
        );
        
        // Filtrar el conflicto con la misma cita que se está actualizando
        conflicts = conflicts.stream()
                .filter(conflict -> !conflict.getId().equals(appointment.getId()))
                .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("El doctor ya tiene una cita programada en ese horario");
        }
    }
    
    private void validateDoctorWorkingHours(Doctor doctor, Appointment appointment) {
        // Esta validación se puede expandir según las reglas de negocio específicas
        // Por ahora, validamos horarios básicos de trabajo (8 AM - 6 PM)
        LocalTime startTime = appointment.getStartTime().toLocalTime();
        LocalTime endTime = appointment.getEndTime().toLocalTime();
        
        LocalTime workStart = LocalTime.of(8, 0); // 8:00 AM
        LocalTime workEnd = LocalTime.of(18, 0);  // 6:00 PM
        
        if (startTime.isBefore(workStart) || endTime.isAfter(workEnd)) {
            throw new IllegalArgumentException("La cita debe estar dentro del horario de trabajo (8:00 AM - 6:00 PM)");
        }
        
        // Validar que no sea fin de semana (opcional)
        int dayOfWeek = appointment.getStartTime().getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) { // Sábado o Domingo
            throw new IllegalArgumentException("No se pueden programar citas los fines de semana");
        }
    }
    
    private AppointmentDTO convertToDTO(Appointment appointment) {
        // Fetch doctor name and specialization
        String doctorName = "Unknown";
        String doctorSpecialization = "";
        try {
            var doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
            if (doctor != null) {
                doctorName = doctor.getFirstName() + " " + doctor.getLastName();
                doctorSpecialization = doctor.getSpecialization() != null ? doctor.getSpecialization() : "";
            }
        } catch (Exception e) {
            log.warn("Error fetching doctor info for appointment {}", appointment.getId(), e);
        }

        // Fetch patient name
        String patientName = "Unknown";
        try {
            var patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            if (patient != null) {
                patientName = patient.getFirstName() + " " + patient.getLastName();
            }
        } catch (Exception e) {
            log.warn("Error fetching patient info for appointment {}", appointment.getId(), e);
        }

        return AppointmentDTO.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctorId())
                .patientId(appointment.getPatientId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .type(appointment.getType())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .createdBy(appointment.getCreatedBy())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .doctorName(doctorName)
                .patientName(patientName)
                .doctorSpecialization(doctorSpecialization)
                .build();
    }
    
    /**
     * Get all appointments for a specific doctor
     * @param doctorId The ID of the doctor
     * @return List of appointment DTOs
     */
    public List<AppointmentDTO> getDoctorAppointments(UUID doctorId) {
        return appointmentRepository.findByDoctorIdOrderByStartTimeDesc(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all appointments for a specific patient
     * @param patientId The ID of the patient
     * @return List of appointment DTOs
     */
    public List<AppointmentDTO> getPatientAppointments(UUID patientId) {
        return appointmentRepository.findByPatientIdOrderByStartTimeDesc(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
