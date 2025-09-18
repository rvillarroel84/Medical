package com.medcal.controller;

import com.medcal.model.entity.Appointment;
import com.medcal.model.entity.Doctor;
import com.medcal.model.entity.Patient;
import com.medcal.model.entity.User;
import com.medcal.model.enums.AppointmentStatus;
import com.medcal.model.enums.AppointmentType;
import com.medcal.model.enums.Gender;
import com.medcal.model.enums.Role;
import com.medcal.model.request.AppointmentRequest;
import com.medcal.repository.AppointmentRepository;
import com.medcal.repository.DoctorRepository;
import com.medcal.repository.PatientRepository;
import com.medcal.repository.UserRepository;
import com.medcal.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AppointmentCreationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID doctorId;
    private UUID patientId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        // Create test user
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        User savedUser = userRepository.save(user);
        userId = savedUser.getId();

        // Create test doctor with user relationship
        Doctor doctor = Doctor.builder()
                .firstName("Dr. Test")
                .lastName("Doctor")
                .email("doctor@example.com")
                .specialization("General Medicine")
                .licenseNumber("12345")
                .phone("123456789")
                .active(true)
                .userId(userId) // Link to the created user
                .build();
        Doctor savedDoctor = doctorRepository.save(doctor);
        doctorId = savedDoctor.getId();

        // Create test patient
        Patient patient = Patient.builder()
                .firstName("Test")
                .lastName("Patient")
                .email("patient@example.com")
                .phone("987654321")
                .dateOfBirth(java.time.LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .address("Test Address")
                .build();
        Patient savedPatient = patientRepository.save(patient);
        patientId = savedPatient.getId();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    void testAppointmentCreationWithValidData() {
        System.out.println("=== Starting Appointment Creation Test ===");

        // Create appointment request with valid data
        AppointmentRequest request = AppointmentRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .type(AppointmentType.CONSULTATION)
                .notes("Test appointment")
                .createdBy(userId)
                .build();

        System.out.println("Created request:");
        System.out.println("- Doctor ID: " + request.getDoctorId());
        System.out.println("- Patient ID: " + request.getPatientId());
        System.out.println("- Start Time: " + request.getStartTime());
        System.out.println("- End Time: " + request.getEndTime());
        System.out.println("- Type: " + request.getType());
        System.out.println("- Created By: " + request.getCreatedBy());

        try {
            // Test the toEntity conversion
            Appointment appointment = request.toEntity();
            System.out.println("Appointment entity created:");
            System.out.println("- Created By: " + appointment.getCreatedBy());

            // Test the service validation
            var result = appointmentService.createAppointment(appointment);
            System.out.println("Appointment created successfully with ID: " + result.getId());

            assertNotNull(result.getId());
            assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());

        } catch (Exception e) {
            System.err.println("Error during appointment creation: " + e.getMessage());
            e.printStackTrace();
            fail("Appointment creation should not fail with valid data: " + e.getMessage());
        }
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    void testAppointmentCreationWithoutCreatedBy() {
        System.out.println("=== Testing Appointment Creation Without CreatedBy ===");

        // Create appointment request without createdBy
        AppointmentRequest request = AppointmentRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(3))
                .type(AppointmentType.CONSULTATION)
                .notes("Test appointment")
                .build();

        System.out.println("Created request without createdBy:");
        System.out.println("- Doctor ID: " + request.getDoctorId());
        System.out.println("- Patient ID: " + request.getPatientId());
        System.out.println("- Created By: " + request.getCreatedBy());

        try {
            Appointment appointment = request.toEntity();
            System.out.println("Appointment entity created:");
            System.out.println("- Created By: " + appointment.getCreatedBy());

            appointmentService.createAppointment(appointment);
            fail("Should have thrown an exception for missing createdBy");

        } catch (IllegalArgumentException e) {
            System.out.println("Expected error caught: " + e.getMessage());
            assertTrue(e.getMessage().contains("El ID del usuario que crea la cita es obligatorio"));
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            fail("Unexpected error type: " + e.getMessage());
        }
    }
}