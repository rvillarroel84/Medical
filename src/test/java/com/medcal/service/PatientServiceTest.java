package com.medcal.service;

import com.medcal.model.dto.PatientDTO;
import com.medcal.model.entity.Patient;
import com.medcal.model.enums.Gender;
import com.medcal.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private UUID testId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testPatient = Patient.builder()
                .id(testId)
                .userId(testUserId)
                .firstName("Juan")
                .lastName("Pérez")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.MALE)
                .address("Calle 123, Ciudad")
                .phone("+1234567890")
                .email("juan.perez@email.com")
                .emergencyContact("555-5678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllPatients_ShouldReturnAllPatients() {
        // Given
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findAll()).thenReturn(patients);

        // When
        List<PatientDTO> result = patientService.getAllPatients();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getFirstName());
        assertEquals("Pérez", result.get(0).getLastName());
        verify(patientRepository).findAll();
    }

    @Test
    void getPatientById_WhenPatientExists_ShouldReturnPatient() {
        // Given
        when(patientRepository.findById(testId)).thenReturn(Optional.of(testPatient));

        // When
        Optional<PatientDTO> result = patientService.getPatientById(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getFirstName());
        assertEquals("Pérez", result.get().getLastName());
        verify(patientRepository).findById(testId);
    }

    @Test
    void getPatientById_WhenPatientDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(patientRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        Optional<PatientDTO> result = patientService.getPatientById(testId);

        // Then
        assertFalse(result.isPresent());
        verify(patientRepository).findById(testId);
    }

    @Test
    void getPatientByUserId_ShouldReturnPatient() {
        // Given
        when(patientRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPatient));

        // When
        Optional<PatientDTO> result = patientService.getPatientByUserId(testUserId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getUserId());
        verify(patientRepository).findByUserId(testUserId);
    }

    @Test
    void searchPatientsByName_ShouldReturnMatchingPatients() {
        // Given
        String searchTerm = "Juan";
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findByNameContaining(searchTerm)).thenReturn(patients);

        // When
        List<PatientDTO> result = patientService.searchPatientsByName(searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getFirstName());
        verify(patientRepository).findByNameContaining(searchTerm);
    }

    @Test
    void createPatient_WithValidData_ShouldCreatePatient() {
        // Given
        Patient newPatient = Patient.builder()
                .userId(UUID.randomUUID())
                .firstName("María")
                .lastName("García")
                .email("maria@email.com")
                .phone("+1987654321")
                .build();

        Patient savedPatient = Patient.builder()
                .id(UUID.randomUUID())
                .userId(newPatient.getUserId())
                .firstName("María")
                .lastName("García")
                .email("maria@email.com")
                .phone("+1987654321")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserId(newPatient.getUserId())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // When
        PatientDTO result = patientService.createPatient(newPatient);

        // Then
        assertNotNull(result);
        assertEquals("María", result.getFirstName());
        assertEquals("García", result.getLastName());
        verify(patientRepository).findByUserId(newPatient.getUserId());
        verify(patientRepository).save(newPatient);
    }

    @Test
    void createPatient_WithDuplicateUserId_ShouldThrowException() {
        // Given
        Patient newPatient = Patient.builder()
                .userId(testUserId)
                .firstName("María")
                .lastName("García")
                .build();

        when(patientRepository.findByUserId(testUserId)).thenReturn(Optional.of(testPatient));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(newPatient)
        );

        assertEquals("Ya existe un paciente asociado a este usuario", exception.getMessage());
        verify(patientRepository).findByUserId(testUserId);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void createPatient_WithMissingFirstName_ShouldThrowException() {
        // Given
        Patient invalidPatient = Patient.builder()
                .userId(UUID.randomUUID())
                .lastName("García")
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(invalidPatient)
        );

        assertEquals("El nombre es obligatorio", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void createPatient_WithMissingLastName_ShouldThrowException() {
        // Given
        Patient invalidPatient = Patient.builder()
                .userId(UUID.randomUUID())
                .firstName("María")
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(invalidPatient)
        );

        assertEquals("El apellido es obligatorio", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void createPatient_WithInvalidEmail_ShouldThrowException() {
        // Given
        Patient invalidPatient = Patient.builder()
                .userId(UUID.randomUUID())
                .firstName("María")
                .lastName("García")
                .email("invalid-email")
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(invalidPatient)
        );

        assertEquals("El formato del email no es válido", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void updatePatient_WhenPatientExists_ShouldUpdatePatient() {
        // Given
        Patient updatedData = Patient.builder()
                .firstName("Juan Carlos")
                .lastName("Pérez García")
                .phone("+1999888777")
                .email("juan.carlos@email.com")
                .build();

        Patient updatedPatient = Patient.builder()
                .id(testId)
                .userId(testUserId)
                .firstName("Juan Carlos")
                .lastName("Pérez García")
                .phone("+1999888777")
                .email("juan.carlos@email.com")
                .createdAt(testPatient.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(patientRepository.findById(testId)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

        // When
        Optional<PatientDTO> result = patientService.updatePatient(testId, updatedData);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Juan Carlos", result.get().getFirstName());
        assertEquals("Pérez García", result.get().getLastName());
        verify(patientRepository).findById(testId);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void updatePatient_WhenPatientDoesNotExist_ShouldReturnEmpty() {
        // Given
        Patient updatedData = Patient.builder()
                .firstName("Juan Carlos")
                .lastName("Pérez García")
                .build();

        when(patientRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        Optional<PatientDTO> result = patientService.updatePatient(testId, updatedData);

        // Then
        assertFalse(result.isPresent());
        verify(patientRepository).findById(testId);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void deletePatient_WhenPatientExists_ShouldReturnTrue() {
        // Given
        when(patientRepository.existsById(testId)).thenReturn(true);

        // When
        boolean result = patientService.deletePatient(testId);

        // Then
        assertTrue(result);
        verify(patientRepository).existsById(testId);
        verify(patientRepository).deleteById(testId);
    }

    @Test
    void deletePatient_WhenPatientDoesNotExist_ShouldReturnFalse() {
        // Given
        when(patientRepository.existsById(testId)).thenReturn(false);

        // When
        boolean result = patientService.deletePatient(testId);

        // Then
        assertFalse(result);
        verify(patientRepository).existsById(testId);
        verify(patientRepository, never()).deleteById(any());
    }
}
