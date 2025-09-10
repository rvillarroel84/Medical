package com.medcal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcal.model.dto.PatientDTO;
import com.medcal.model.entity.Patient;
import com.medcal.model.enums.Gender;
import com.medcal.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PatientController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientDTO testPatientDTO;
    private Patient testPatient;
    private UUID testId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testPatientDTO = PatientDTO.builder()
                .id(testId)
                .userId(testUserId)
                .firstName("Juan")
                .lastName("Pérez")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.MALE)
                .address("Calle 123, Ciudad")
                .phone("555-1234")
                .email("juan.perez@email.com")
                .emergencyContact("555-5678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPatient = Patient.builder()
                .userId(testUserId)
                .firstName("Juan")
                .lastName("Pérez")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.MALE)
                .address("Calle 123, Ciudad")
                .phone("555-1234")
                .email("juan.perez@email.com")
                .emergencyContact("555-5678")
                .build();
    }

    @Test
    void getAllPatients_ShouldReturnAllPatients() throws Exception {
        // Given
        List<PatientDTO> patients = Arrays.asList(testPatientDTO);
        when(patientService.getAllPatients()).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].firstName").value("Juan"))
                .andExpect(jsonPath("$[0].lastName").value("Pérez"));

        verify(patientService).getAllPatients();
    }

    @Test
    void getPatientById_WhenPatientExists_ShouldReturnPatient() throws Exception {
        // Given
        when(patientService.getPatientById(testId)).thenReturn(Optional.of(testPatientDTO));

        // When & Then
        mockMvc.perform(get("/api/patients/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"));

        verify(patientService).getPatientById(testId);
    }

    @Test
    void getPatientById_WhenPatientDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(patientService.getPatientById(testId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/patients/{id}", testId))
                .andExpect(status().isNotFound());

        verify(patientService).getPatientById(testId);
    }

    @Test
    void getPatientByUserId_ShouldReturnPatient() throws Exception {
        // Given
        when(patientService.getPatientByUserId(testUserId)).thenReturn(Optional.of(testPatientDTO));

        // When & Then
        mockMvc.perform(get("/api/patients/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"));

        verify(patientService).getPatientByUserId(testUserId);
    }

    @Test
    void getPatientByEmail_ShouldReturnPatient() throws Exception {
        // Given
        String email = "juan.perez@email.com";
        when(patientService.getPatientByEmail(email)).thenReturn(Optional.of(testPatientDTO));

        // When & Then
        mockMvc.perform(get("/api/patients/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(email));

        verify(patientService).getPatientByEmail(email);
    }

    @Test
    void searchPatients_ShouldReturnMatchingPatients() throws Exception {
        // Given
        String searchTerm = "Juan";
        List<PatientDTO> patients = Arrays.asList(testPatientDTO);
        when(patientService.searchPatientsByName(searchTerm)).thenReturn(patients);

        // When & Then
        mockMvc.perform(get("/api/patients/search")
                        .param("name", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Juan"));

        verify(patientService).searchPatientsByName(searchTerm);
    }

    @Test
    void createPatient_WithValidData_ShouldCreatePatient() throws Exception {
        // Given
        when(patientService.createPatient(any(Patient.class))).thenReturn(testPatientDTO);

        // When & Then
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"));

        verify(patientService).createPatient(any(Patient.class));
    }

    @Test
    void createPatient_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        when(patientService.createPatient(any(Patient.class)))
                .thenThrow(new IllegalArgumentException("El nombre es obligatorio"));

        // When & Then
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El nombre es obligatorio"));

        verify(patientService).createPatient(any(Patient.class));
    }

    @Test
    void createPatient_WithServerError_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(patientService.createPatient(any(Patient.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno del servidor: Database error"));

        verify(patientService).createPatient(any(Patient.class));
    }

    @Test
    void updatePatient_WhenPatientExists_ShouldUpdatePatient() throws Exception {
        // Given
        PatientDTO updatedPatientDTO = PatientDTO.builder()
                .id(testId)
                .userId(testUserId)
                .firstName("Juan Carlos")
                .lastName("Pérez García")
                .email("juan.carlos@email.com")
                .phone("555-9999")
                .build();

        when(patientService.updatePatient(eq(testId), any(Patient.class)))
                .thenReturn(Optional.of(updatedPatientDTO));

        // When & Then
        mockMvc.perform(put("/api/patients/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                .andExpect(jsonPath("$.lastName").value("Pérez García"));

        verify(patientService).updatePatient(eq(testId), any(Patient.class));
    }

    @Test
    void updatePatient_WhenPatientDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(patientService.updatePatient(eq(testId), any(Patient.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/patients/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isNotFound());

        verify(patientService).updatePatient(eq(testId), any(Patient.class));
    }

    @Test
    void updatePatient_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        when(patientService.updatePatient(eq(testId), any(Patient.class)))
                .thenThrow(new IllegalArgumentException("El formato del email no es válido"));

        // When & Then
        mockMvc.perform(put("/api/patients/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatient)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El formato del email no es válido"));

        verify(patientService).updatePatient(eq(testId), any(Patient.class));
    }

    @Test
    void deletePatient_WhenPatientExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(patientService.deletePatient(testId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/patients/{id}", testId))
                .andExpect(status().isNoContent());

        verify(patientService).deletePatient(testId);
    }

    @Test
    void deletePatient_WhenPatientDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(patientService.deletePatient(testId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/patients/{id}", testId))
                .andExpect(status().isNotFound());

        verify(patientService).deletePatient(testId);
    }

    @Test
    void deletePatient_WithServerError_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(patientService.deletePatient(testId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(delete("/api/patients/{id}", testId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno del servidor: Database error"));

        verify(patientService).deletePatient(testId);
    }
}
