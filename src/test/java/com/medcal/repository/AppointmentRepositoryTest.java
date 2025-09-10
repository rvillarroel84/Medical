package com.medcal.repository;

import com.medcal.model.entity.Appointment;
import com.medcal.model.enums.AppointmentStatus;
import com.medcal.model.enums.AppointmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private UUID doctorId;
    private UUID patientId;
    private UUID createdBy;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
        baseTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void findByDoctorId_ShouldReturnDoctorAppointments() {
        // Given
        Appointment appointment1 = createAppointment(doctorId, patientId, baseTime, baseTime.plusHours(1));
        Appointment appointment2 = createAppointment(UUID.randomUUID(), patientId, baseTime.plusHours(2), baseTime.plusHours(3));
        
        entityManager.persistAndFlush(appointment1);
        entityManager.persistAndFlush(appointment2);

        // When
        List<Appointment> result = appointmentRepository.findByDoctorId(doctorId);

        // Then
        assertEquals(1, result.size());
        assertEquals(doctorId, result.get(0).getDoctorId());
    }

    @Test
    void findByPatientId_ShouldReturnPatientAppointments() {
        // Given
        Appointment appointment1 = createAppointment(doctorId, patientId, baseTime, baseTime.plusHours(1));
        Appointment appointment2 = createAppointment(doctorId, UUID.randomUUID(), baseTime.plusHours(2), baseTime.plusHours(3));
        
        entityManager.persistAndFlush(appointment1);
        entityManager.persistAndFlush(appointment2);

        // When
        List<Appointment> result = appointmentRepository.findByPatientId(patientId);

        // Then
        assertEquals(1, result.size());
        assertEquals(patientId, result.get(0).getPatientId());
    }

    @Test
    void findByStatus_ShouldReturnAppointmentsByStatus() {
        // Given
        Appointment scheduledAppointment = createAppointment(doctorId, patientId, baseTime, baseTime.plusHours(1));
        scheduledAppointment.setStatus(AppointmentStatus.SCHEDULED);
        
        Appointment completedAppointment = createAppointment(doctorId, UUID.randomUUID(), baseTime.plusHours(2), baseTime.plusHours(3));
        completedAppointment.setStatus(AppointmentStatus.COMPLETED);
        
        entityManager.persistAndFlush(scheduledAppointment);
        entityManager.persistAndFlush(completedAppointment);

        // When
        List<Appointment> scheduledResults = appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED);
        List<Appointment> completedResults = appointmentRepository.findByStatus(AppointmentStatus.COMPLETED);

        // Then
        assertEquals(1, scheduledResults.size());
        assertEquals(AppointmentStatus.SCHEDULED, scheduledResults.get(0).getStatus());
        
        assertEquals(1, completedResults.size());
        assertEquals(AppointmentStatus.COMPLETED, completedResults.get(0).getStatus());
    }

    @Test
    void findByDoctorIdAndDateRange_ShouldReturnAppointmentsInRange() {
        // Given
        LocalDateTime rangeStart = baseTime.minusHours(1);
        LocalDateTime rangeEnd = baseTime.plusHours(2);
        
        Appointment appointmentInRange = createAppointment(doctorId, patientId, baseTime, baseTime.plusHours(1));
        Appointment appointmentOutOfRange = createAppointment(doctorId, UUID.randomUUID(), baseTime.plusHours(3), baseTime.plusHours(4));
        
        entityManager.persistAndFlush(appointmentInRange);
        entityManager.persistAndFlush(appointmentOutOfRange);

        // When
        List<Appointment> result = appointmentRepository.findByDoctorIdAndDateRange(doctorId, rangeStart, rangeEnd);

        // Then
        assertEquals(1, result.size());
        assertEquals(appointmentInRange.getId(), result.get(0).getId());
    }

    @Test
    void findConflictingAppointments_ShouldReturnConflictingAppointments() {
        // Given
        LocalDateTime existingStart = baseTime;
        LocalDateTime existingEnd = baseTime.plusHours(1);
        
        Appointment existingAppointment = createAppointment(doctorId, patientId, existingStart, existingEnd);
        existingAppointment.setStatus(AppointmentStatus.SCHEDULED);
        entityManager.persistAndFlush(existingAppointment);

        // Test overlapping scenarios
        LocalDateTime newStart1 = baseTime.plusMinutes(30); // Starts during existing
        LocalDateTime newEnd1 = baseTime.plusHours(2);      // Ends after existing
        
        LocalDateTime newStart2 = baseTime.minusMinutes(30); // Starts before existing
        LocalDateTime newEnd2 = baseTime.plusMinutes(30);    // Ends during existing
        
        LocalDateTime newStart3 = baseTime.minusMinutes(30); // Completely encompasses existing
        LocalDateTime newEnd3 = baseTime.plusHours(2);

        // When
        List<Appointment> conflicts1 = appointmentRepository.findConflictingAppointments(doctorId, newStart1, newEnd1);
        List<Appointment> conflicts2 = appointmentRepository.findConflictingAppointments(doctorId, newStart2, newEnd2);
        List<Appointment> conflicts3 = appointmentRepository.findConflictingAppointments(doctorId, newStart3, newEnd3);

        // Then
        assertEquals(1, conflicts1.size());
        assertEquals(1, conflicts2.size());
        assertEquals(1, conflicts3.size());
        
        assertEquals(existingAppointment.getId(), conflicts1.get(0).getId());
        assertEquals(existingAppointment.getId(), conflicts2.get(0).getId());
        assertEquals(existingAppointment.getId(), conflicts3.get(0).getId());
    }

    @Test
    void findConflictingAppointments_WithNoOverlap_ShouldReturnEmpty() {
        // Given
        Appointment existingAppointment = createAppointment(doctorId, patientId, baseTime, baseTime.plusHours(1));
        existingAppointment.setStatus(AppointmentStatus.SCHEDULED);
        entityManager.persistAndFlush(existingAppointment);

        // Non-overlapping time slots
        LocalDateTime newStart = baseTime.plusHours(2);
        LocalDateTime newEnd = baseTime.plusHours(3);

        // When
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(doctorId, newStart, newEnd);

        // Then
        assertTrue(conflicts.isEmpty());
    }

    @Test
    void findConflictingAppointments_WithCancelledAppointment_ShouldNotReturnCancelled() {
        // Given
        Appointment cancelledAppointment = createAppointment(doctorId, patientId, baseTime, baseTime.plusHours(1));
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
        entityManager.persistAndFlush(cancelledAppointment);

        // Overlapping time slot
        LocalDateTime newStart = baseTime.plusMinutes(30);
        LocalDateTime newEnd = baseTime.plusHours(2);

        // When
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(doctorId, newStart, newEnd);

        // Then
        assertTrue(conflicts.isEmpty()); // Cancelled appointments should not conflict
    }

    @Test
    void findByPatientIdAndDateRange_ShouldReturnPatientAppointmentsInRange() {
        // Given
        LocalDateTime rangeStart = baseTime.minusHours(1);
        LocalDateTime rangeEnd = baseTime.plusHours(2);
        
        Appointment appointmentInRange = createAppointment(doctorId, patientId, baseTime, baseTime.plusHours(1));
        Appointment appointmentOutOfRange = createAppointment(UUID.randomUUID(), patientId, baseTime.plusHours(3), baseTime.plusHours(4));
        
        entityManager.persistAndFlush(appointmentInRange);
        entityManager.persistAndFlush(appointmentOutOfRange);

        // When
        List<Appointment> result = appointmentRepository.findByPatientIdAndDateRange(patientId, rangeStart, rangeEnd);

        // Then
        assertEquals(1, result.size());
        assertEquals(appointmentInRange.getId(), result.get(0).getId());
    }

    private Appointment createAppointment(UUID doctorId, UUID patientId, LocalDateTime startTime, LocalDateTime endTime) {
        return Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(startTime)
                .endTime(endTime)
                .type(AppointmentType.CONSULTATION)
                .status(AppointmentStatus.SCHEDULED)
                .notes("Test appointment")
                .createdBy(createdBy)
                .build();
    }
}
