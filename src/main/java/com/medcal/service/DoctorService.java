package com.medcal.service;

import com.medcal.model.dto.AvailabilitySlot;
import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.entity.Appointment;
import com.medcal.model.entity.Doctor;
import com.medcal.model.entity.User;
import com.medcal.model.enums.Role;
import com.medcal.exception.ResourceNotFoundException;
import com.medcal.repository.AppointmentRepository;
import com.medcal.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {
    
    private static final int APPOINTMENT_DURATION_MINUTES = 30;
    
    private final DoctorRepository doctorRepository;
    private final UserService userService;
    private final AppointmentRepository appointmentRepository;
    
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<DoctorDTO> getDoctorById(UUID id) {
        return doctorRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Find available doctors based on specialty and time range
     * @param specialty Optional specialty filter
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return List of available doctors with their availability slots
     */
    public List<DoctorDTO> findAvailableDoctors(String specialty, LocalDateTime startTime, LocalDateTime endTime) {
        // First get all active doctors (optionally filtered by specialty)
        List<Doctor> doctors;
        if (specialty != null && !specialty.isEmpty()) {
            doctors = doctorRepository.findBySpecialization(specialty);
        } else {
            doctors = doctorRepository.findAll();
        }
        
        // Filter doctors who are available during the requested time
        return doctors.stream()
                .filter(doctor -> isDoctorAvailable(doctor.getId(), startTime, endTime))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DoctorDTO> searchDoctorsByName(String name) {
        return doctorRepository.findByNameContaining(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a doctor is available during a specific time range
     * @param doctorId The ID of the doctor
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return true if the doctor is available, false otherwise
     */
    public boolean isDoctorAvailable(UUID doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        // Check if doctor exists and is active
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return false;
        }
        
        // Check working hours (8 AM to 6 PM)
        LocalTime startWork = LocalTime.of(8, 0);
        LocalTime endWork = LocalTime.of(18, 0);
        
        if (startTime.toLocalTime().isBefore(startWork) || 
            endTime.toLocalTime().isAfter(endWork)) {
            return false;
        }
        
        // Check if it's a weekend
        DayOfWeek dayOfWeek = startTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        
        // Check for conflicting appointments
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                doctorId, 
                startTime, 
                endTime
        );
        
        return conflicts.isEmpty();
    }
    
    public List<DoctorDTO> findAvailableDoctors() {
        // Get all active doctors who are currently available
        return doctorRepository.findAllByActiveTrue()
                .stream()
                .filter(doctor -> {
                    // Check if doctor has availability in the next 7 days
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime endDate = now.plusWeeks(1);
                    
                    // Get doctor's appointments in the next 7 days
                    List<Appointment> upcomingAppointments = appointmentRepository
                            .findByDoctorIdAndStartTimeBetween(
                                    doctor.getId(), 
                                    now, 
                                    endDate
                            );
                    
                    // Consider doctors who have less than 80% of their time booked
                    int totalPossibleSlots = calculateTotalPossibleSlots(doctor, now, endDate);
                    return upcomingAppointments.size() < (totalPossibleSlots * 0.8);
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AvailabilitySlot> getAvailableSlots(UUID doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado"));
        
        // Get existing appointments for the doctor in the date range
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndStartTimeBetween(doctorId, startDate, endDate);
        
        // Generate all possible time slots
        List<AvailabilitySlot> allSlots = generateTimeSlots(doctor, startDate, endDate);
        
        // Mark slots as unavailable if they conflict with existing appointments
        return allSlots.stream()
                .map(slot -> {
                    boolean isAvailable = existingAppointments.stream()
                            .noneMatch(apt -> isTimeSlotOverlap(
                                    slot.getStartTime(), 
                                    slot.getEndTime(), 
                                    apt.getStartTime(), 
                                    apt.getEndTime()
                            ));
                    slot.setAvailable(isAvailable);
                    return slot;
                })
                .collect(Collectors.toList());
    }
    
    private List<AvailabilitySlot> generateTimeSlots(Doctor doctor, LocalDateTime startDate, LocalDateTime endDate) {
        List<AvailabilitySlot> slots = new ArrayList<>();
        LocalDateTime current = startDate;
        
        // Generate slots for each day in the range
        while (current.isBefore(endDate)) {
            if (isWorkingDay(doctor, current.toLocalDate())) {
                LocalTime workStart = getWorkStartTime(doctor, current.getDayOfWeek());
                LocalTime workEnd = getWorkEndTime(doctor, current.getDayOfWeek());
                
                // Skip if doctor doesn't work this day
                if (workStart != null && workEnd != null) {
                    LocalDateTime slotStart = LocalDateTime.of(current.toLocalDate(), workStart);
                    LocalDateTime slotEnd = slotStart.plusMinutes(APPOINTMENT_DURATION_MINUTES);
                    
                    // Generate slots for the day
                    while (slotEnd.toLocalTime().isBefore(workEnd) || 
                           slotEnd.toLocalTime().equals(workEnd)) {
                        slots.add(new AvailabilitySlot(slotStart, slotEnd));
                        
                        // Move to next slot
                        slotStart = slotEnd;
                        slotEnd = slotStart.plusMinutes(APPOINTMENT_DURATION_MINUTES);
                    }
                }
            }
            
            // Move to next day
            current = current.plusDays(1).with(LocalTime.MIN);
        }
        
        return slots;
    }
    
    private boolean isWorkingDay(Doctor doctor, LocalDate date) {
        // TODO: Implement based on doctor's working days
        // For now, assume Monday to Friday are working days
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && 
               date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
    
    private LocalTime getWorkStartTime(Doctor doctor, DayOfWeek dayOfWeek) {
        // TODO: Get from doctor's working hours
        // For now, use default working hours: 9 AM to 5 PM
        return LocalTime.of(9, 0);
    }
    
    private LocalTime getWorkEndTime(Doctor doctor, DayOfWeek dayOfWeek) {
        // TODO: Get from doctor's working hours
        // For now, use default working hours: 9 AM to 5 PM
        return LocalTime.of(17, 0);
    }
    
    private boolean isTimeSlotOverlap(LocalDateTime start1, LocalDateTime end1, 
                                     LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    private int calculateTotalPossibleSlots(Doctor doctor, LocalDateTime start, LocalDateTime end) {
        int totalSlots = 0;
        LocalDateTime current = start;
        
        while (current.isBefore(end)) {
            if (isWorkingDay(doctor, current.toLocalDate())) {
                LocalTime workStart = getWorkStartTime(doctor, current.getDayOfWeek());
                LocalTime workEnd = getWorkEndTime(doctor, current.getDayOfWeek());
                
                if (workStart != null && workEnd != null) {
                    long minutes = workStart.until(workEnd, java.time.temporal.ChronoUnit.MINUTES);
                    totalSlots += (int) (minutes / APPOINTMENT_DURATION_MINUTES);
                }
            }
            current = current.plusDays(1);
        }
        
        return totalSlots;
    }
    
    @Transactional
    public DoctorDTO createDoctor(Doctor doctor) {
        // Validaciones básicas
        if (doctor.getFirstName() == null || doctor.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (doctor.getLastName() == null || doctor.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es requerido");
        }
        if (doctor.getLicenseNumber() == null || doctor.getLicenseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de licencia es requerido");
        }
        if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
            throw new IllegalArgumentException("La especialización es requerida");
        }
        if (doctor.getEmail() == null || doctor.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido");
        }
        
        // Verificar si ya existe un doctor con el mismo número de licencia
        if (doctorRepository.findByLicenseNumber(doctor.getLicenseNumber()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un doctor con ese número de licencia");
        }
        
        // Crear usuario primero
        User user = userService.createUser(
            doctor.getEmail(),
            doctor.getFirstName(),
            doctor.getLastName(),
            doctor.getPhone(),
            Role.DOCTOR
        );
        
        // Asignar el ID del usuario al doctor
        doctor.setUserId(user.getId());
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
    }
    
    @Transactional
    public Optional<DoctorDTO> updateDoctor(UUID id, Doctor doctorDetails) {
        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctor.setFirstName(doctorDetails.getFirstName());
                    doctor.setLastName(doctorDetails.getLastName());
                    doctor.setSpecialization(doctorDetails.getSpecialization());
                    doctor.setPhone(doctorDetails.getPhone());
                    doctor.setEmail(doctorDetails.getEmail());
                    doctor.setWorkingHours(doctorDetails.getWorkingHours());
                    return convertToDTO(doctorRepository.save(doctor));
                });
    }
    
    @Transactional
    public boolean deleteDoctor(UUID id) {
        if (doctorRepository.existsById(id)) {
            doctorRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private DoctorDTO convertToDTO(Doctor doctor) {
        return DoctorDTO.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .licenseNumber(doctor.getLicenseNumber())
                .specialization(doctor.getSpecialization())
                .phone(doctor.getPhone())
                .email(doctor.getEmail())
                .workingHours(doctor.getWorkingHours())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}
