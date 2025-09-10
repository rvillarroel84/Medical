package com.medcal.service;

import com.medcal.model.dto.AppointmentDTO;
import com.medcal.model.entity.Appointment;
import com.medcal.model.entity.Doctor;
import com.medcal.model.entity.Patient;
import com.medcal.model.enums.AppointmentStatus;
import com.medcal.model.enums.AppointmentType;
import com.medcal.repository.AppointmentRepository;
import com.medcal.repository.DoctorRepository;
import com.medcal.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment testAppointment;
    private Doctor testDoctor;
    private Patient testPatient;
    private UUID appointmentId;
    private UUID doctorId;
    private UUID patientId;
    private UUID createdBy;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        createdBy = UUID.randomUUID();

        testDoctor = Doctor.builder()
                .id(doctorId)
                .firstName("Dr. Juan")
                .lastName("Médico")
                .specialization("Cardiología")
                .build();

        testPatient = Patient.builder()
                .id(patientId)
                .firstName("María")
                .lastName("Paciente")
                .build();

        testAppointment = Appointment.builder()
                .id(appointmentId)
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0))
                .type(AppointmentType.CONSULTATION)
                .status(AppointmentStatus.SCHEDULED)
                .notes("Consulta de rutina")
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllAppointments_ShouldReturnAllAppointments() {
        // Given
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentRepository.findAll()).thenReturn(appointments);

        // When
        List<AppointmentDTO> result = appointmentService.getAllAppointments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(appointmentId, result.get(0).getId());
        verify(appointmentRepository).findAll();
    }

    @Test
    void getAppointmentById_WhenExists_ShouldReturnAppointment() {
        // Given
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

        // When
        Optional<AppointmentDTO> result = appointmentService.getAppointmentById(appointmentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(appointmentId, result.get().getId());
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void getAppointmentsByDoctorId_ShouldReturnDoctorAppointments() {
        // Given
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentRepository.findByDoctorId(doctorId)).thenReturn(appointments);

        // When
        List<AppointmentDTO> result = appointmentService.getAppointmentsByDoctorId(doctorId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(doctorId, result.get(0).getDoctorId());
        verify(appointmentRepository).findByDoctorId(doctorId);
    }

    @Test
    void createAppointment_WithValidData_ShouldCreateAppointment() {
        // Given
        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(15).withMinute(0))
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findConflictingAppointments(any(), any(), any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        AppointmentDTO result = appointmentService.createAppointment(newAppointment);

        // Then
        assertNotNull(result);
        assertEquals(doctorId, result.getDoctorId());
        assertEquals(patientId, result.getPatientId());
        verify(doctorRepository).findById(doctorId);
        verify(patientRepository).findById(patientId);
        verify(appointmentRepository).findConflictingAppointments(any(), any(), any());
        verify(appointmentRepository).save(newAppointment);
    }

    @Test
    void createAppointment_WithNonExistentDoctor_ShouldThrowException() {
        // Given
        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(15).withMinute(0))
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(newAppointment)
        );

        assertEquals("El doctor especificado no existe", exception.getMessage());
        verify(doctorRepository).findById(doctorId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_WithNonExistentPatient_ShouldThrowException() {
        // Given
        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(15).withMinute(0))
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(newAppointment)
        );

        assertEquals("El paciente especificado no existe", exception.getMessage());
        verify(patientRepository).findById(patientId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_WithConflictingTime_ShouldThrowException() {
        // Given
        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(15).withMinute(0))
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findConflictingAppointments(any(), any(), any()))
                .thenReturn(Arrays.asList(testAppointment));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(newAppointment)
        );

        assertEquals("El doctor ya tiene una cita programada en ese horario", exception.getMessage());
        verify(appointmentRepository).findConflictingAppointments(any(), any(), any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_WithPastTime_ShouldThrowException() {
        // Given
        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(newAppointment)
        );

        assertEquals("No se pueden crear citas en el pasado", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_WithInvalidDuration_ShouldThrowException() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(14).withMinute(0);
        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(10)) // Solo 10 minutos
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(newAppointment)
        );

        assertEquals("La duración mínima de una cita es de 15 minutos", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_WithWeekendDate_ShouldThrowException() {
        // Given
        LocalDateTime weekendStart = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0);
        // Asegurar que sea sábado o domingo
        while (weekendStart.getDayOfWeek().getValue() != 6 && weekendStart.getDayOfWeek().getValue() != 7) {
            weekendStart = weekendStart.plusDays(1);
        }

        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(weekendStart)
                .endTime(weekendStart.plusHours(1))
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findConflictingAppointments(any(), any(), any())).thenReturn(Collections.emptyList());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(newAppointment)
        );

        assertEquals("No se pueden programar citas los fines de semana", exception.getMessage());
    }

    @Test
    void createAppointment_OutsideWorkingHours_ShouldThrowException() {
        // Given
        LocalDateTime earlyTime = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0); // 7 AM
        Appointment newAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(earlyTime)
                .endTime(earlyTime.plusHours(1))
                .type(AppointmentType.CONSULTATION)
                .createdBy(createdBy)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(testDoctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findConflictingAppointments(any(), any(), any())).thenReturn(Collections.emptyList());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(newAppointment)
        );

        assertEquals("La cita debe estar dentro del horario de trabajo (8:00 AM - 6:00 PM)", exception.getMessage());
    }

    @Test
    void updateAppointmentStatus_WhenExists_ShouldUpdateStatus() {
        // Given
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        Optional<AppointmentDTO> result = appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);

        // Then
        assertTrue(result.isPresent());
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_WhenExists_ShouldCancelAppointment() {
        // Given
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        boolean result = appointmentService.cancelAppointment(appointmentId);

        // Then
        assertTrue(result);
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_WhenNotExists_ShouldReturnFalse() {
        // Given
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // When
        boolean result = appointmentService.cancelAppointment(appointmentId);

        // Then
        assertFalse(result);
        verify(appointmentRepository).findById(appointmentId);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void isDoctorAvailable_WithNoConflicts_ShouldReturnTrue() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        when(appointmentRepository.findConflictingAppointments(doctorId, startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        boolean result = appointmentService.isDoctorAvailable(doctorId, startTime, endTime);

        // Then
        assertTrue(result);
        verify(appointmentRepository).findConflictingAppointments(doctorId, startTime, endTime);
    }

    @Test
    void isDoctorAvailable_WithConflicts_ShouldReturnFalse() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        when(appointmentRepository.findConflictingAppointments(doctorId, startTime, endTime))
                .thenReturn(Arrays.asList(testAppointment));

        // When
        boolean result = appointmentService.isDoctorAvailable(doctorId, startTime, endTime);

        // Then
        assertFalse(result);
        verify(appointmentRepository).findConflictingAppointments(doctorId, startTime, endTime);
    }

    @Test
    void deleteAppointment_WhenExists_ShouldReturnTrue() {
        // Given
        when(appointmentRepository.existsById(appointmentId)).thenReturn(true);

        // When
        boolean result = appointmentService.deleteAppointment(appointmentId);

        // Then
        assertTrue(result);
        verify(appointmentRepository).existsById(appointmentId);
        verify(appointmentRepository).deleteById(appointmentId);
    }

    @Test
    void deleteAppointment_WhenNotExists_ShouldReturnFalse() {
        // Given
        when(appointmentRepository.existsById(appointmentId)).thenReturn(false);

        // When
        boolean result = appointmentService.deleteAppointment(appointmentId);

        // Then
        assertFalse(result);
        verify(appointmentRepository).existsById(appointmentId);
        verify(appointmentRepository, never()).deleteById(any());
    }
}
