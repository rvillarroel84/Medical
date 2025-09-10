package com.medcal.service;

import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.entity.Doctor;
import com.medcal.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor testDoctor;
    private UUID testId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testDoctor = Doctor.builder()
                .id(testId)
                .userId(testUserId)
                .firstName("Dr. Juan")
                .lastName("Pérez")
                .licenseNumber("MED-12345")
                .specialization("Cardiología")
                .phone("555-1234")
                .email("dr.perez@hospital.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllDoctors_ShouldReturnAllDoctors() {
        // Given
        List<Doctor> doctors = Arrays.asList(testDoctor);
        when(doctorRepository.findAll()).thenReturn(doctors);

        // When
        List<DoctorDTO> result = doctorService.getAllDoctors();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Juan", result.get(0).getFirstName());
        assertEquals("Cardiología", result.get(0).getSpecialization());
        verify(doctorRepository).findAll();
    }

    @Test
    void getDoctorById_WhenDoctorExists_ShouldReturnDoctor() {
        // Given
        when(doctorRepository.findById(testId)).thenReturn(Optional.of(testDoctor));

        // When
        Optional<DoctorDTO> result = doctorService.getDoctorById(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Dr. Juan", result.get().getFirstName());
        assertEquals("MED-12345", result.get().getLicenseNumber());
        verify(doctorRepository).findById(testId);
    }

    @Test
    void getDoctorById_WhenDoctorDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(doctorRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        Optional<DoctorDTO> result = doctorService.getDoctorById(testId);

        // Then
        assertFalse(result.isPresent());
        verify(doctorRepository).findById(testId);
    }

    @Test
    void getDoctorsBySpecialization_ShouldReturnMatchingDoctors() {
        // Given
        String specialization = "Cardiología";
        List<Doctor> doctors = Arrays.asList(testDoctor);
        when(doctorRepository.findBySpecialization(specialization)).thenReturn(doctors);

        // When
        List<DoctorDTO> result = doctorService.getDoctorsBySpecialization(specialization);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(specialization, result.get(0).getSpecialization());
        verify(doctorRepository).findBySpecialization(specialization);
    }

    @Test
    void searchDoctorsByName_ShouldReturnMatchingDoctors() {
        // Given
        String searchTerm = "Juan";
        List<Doctor> doctors = Arrays.asList(testDoctor);
        when(doctorRepository.findByNameContaining(searchTerm)).thenReturn(doctors);

        // When
        List<DoctorDTO> result = doctorService.searchDoctorsByName(searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Juan", result.get(0).getFirstName());
        verify(doctorRepository).findByNameContaining(searchTerm);
    }

    @Test
    void createDoctor_WithValidData_ShouldCreateDoctor() {
        // Given
        Doctor newDoctor = Doctor.builder()
                .userId(UUID.randomUUID())
                .firstName("Dra. María")
                .lastName("García")
                .licenseNumber("MED-67890")
                .specialization("Neurología")
                .email("dra.garcia@hospital.com")
                .phone("555-9876")
                .build();

        Doctor savedDoctor = Doctor.builder()
                .id(UUID.randomUUID())
                .userId(newDoctor.getUserId())
                .firstName("Dra. María")
                .lastName("García")
                .licenseNumber("MED-67890")
                .specialization("Neurología")
                .email("dra.garcia@hospital.com")
                .phone("555-9876")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);

        // When
        DoctorDTO result = doctorService.createDoctor(newDoctor);

        // Then
        assertNotNull(result);
        assertEquals("Dra. María", result.getFirstName());
        assertEquals("Neurología", result.getSpecialization());
        assertEquals("MED-67890", result.getLicenseNumber());
        verify(doctorRepository).save(newDoctor);
    }

    @Test
    void updateDoctor_WhenDoctorExists_ShouldUpdateDoctor() {
        // Given
        Doctor updatedData = Doctor.builder()
                .firstName("Dr. Juan Carlos")
                .lastName("Pérez García")
                .specialization("Cardiología Intervencionista")
                .phone("555-9999")
                .email("dr.juan.carlos@hospital.com")
                .build();

        Doctor updatedDoctor = Doctor.builder()
                .id(testId)
                .userId(testUserId)
                .firstName("Dr. Juan Carlos")
                .lastName("Pérez García")
                .licenseNumber("MED-12345")
                .specialization("Cardiología Intervencionista")
                .phone("555-9999")
                .email("dr.juan.carlos@hospital.com")
                .createdAt(testDoctor.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(doctorRepository.findById(testId)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(updatedDoctor);

        // When
        Optional<DoctorDTO> result = doctorService.updateDoctor(testId, updatedData);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Dr. Juan Carlos", result.get().getFirstName());
        assertEquals("Cardiología Intervencionista", result.get().getSpecialization());
        verify(doctorRepository).findById(testId);
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    void updateDoctor_WhenDoctorDoesNotExist_ShouldReturnEmpty() {
        // Given
        Doctor updatedData = Doctor.builder()
                .firstName("Dr. Juan Carlos")
                .lastName("Pérez García")
                .build();

        when(doctorRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        Optional<DoctorDTO> result = doctorService.updateDoctor(testId, updatedData);

        // Then
        assertFalse(result.isPresent());
        verify(doctorRepository).findById(testId);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void deleteDoctor_WhenDoctorExists_ShouldReturnTrue() {
        // Given
        when(doctorRepository.existsById(testId)).thenReturn(true);

        // When
        boolean result = doctorService.deleteDoctor(testId);

        // Then
        assertTrue(result);
        verify(doctorRepository).existsById(testId);
        verify(doctorRepository).deleteById(testId);
    }

    @Test
    void deleteDoctor_WhenDoctorDoesNotExist_ShouldReturnFalse() {
        // Given
        when(doctorRepository.existsById(testId)).thenReturn(false);

        // When
        boolean result = doctorService.deleteDoctor(testId);

        // Then
        assertFalse(result);
        verify(doctorRepository).existsById(testId);
        verify(doctorRepository, never()).deleteById(any());
    }
}
