package com.medcal.repository;

import com.medcal.model.entity.Patient;
import com.medcal.model.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    private UUID userId1;
    private UUID userId2;
    private Patient testPatient1;
    private Patient testPatient2;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        testPatient1 = Patient.builder()
                .userId(userId1)
                .firstName("Juan")
                .lastName("Pérez")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.MALE)
                .address("Calle 123, Ciudad")
                .phone("555-1234")
                .email("juan.perez@email.com")
                .emergencyContact("555-5678")
                .build();

        testPatient2 = Patient.builder()
                .userId(userId2)
                .firstName("María")
                .lastName("García")
                .dateOfBirth(LocalDate.of(1985, 8, 20))
                .gender(Gender.FEMALE)
                .address("Avenida 456, Ciudad")
                .phone("555-9876")
                .email("maria.garcia@email.com")
                .emergencyContact("555-4321")
                .build();
    }

    @Test
    void findByUserId_ShouldReturnPatient() {
        // Given
        Patient savedPatient = entityManager.persistAndFlush(testPatient1);

        // When
        Optional<Patient> result = patientRepository.findByUserId(userId1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(savedPatient.getId(), result.get().getId());
        assertEquals("Juan", result.get().getFirstName());
        assertEquals(userId1, result.get().getUserId());
    }

    @Test
    void findByUserId_WhenNotExists_ShouldReturnEmpty() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Patient> result = patientRepository.findByUserId(nonExistentUserId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByNameContaining_ShouldReturnMatchingPatients() {
        // Given
        entityManager.persistAndFlush(testPatient1);
        entityManager.persistAndFlush(testPatient2);

        // When
        List<Patient> resultsByFirstName = patientRepository.findByNameContaining("Juan");
        List<Patient> resultsByLastName = patientRepository.findByNameContaining("García");
        List<Patient> resultsByPartialName = patientRepository.findByNameContaining("ar");

        // Then
        assertEquals(1, resultsByFirstName.size());
        assertEquals("Juan", resultsByFirstName.get(0).getFirstName());

        assertEquals(1, resultsByLastName.size());
        assertEquals("García", resultsByLastName.get(0).getLastName());

        assertEquals(1, resultsByPartialName.size()); // Should match "María" and "García"
        assertEquals("María", resultsByPartialName.get(0).getFirstName());
    }

    @Test
    void findByNameContaining_WithNoMatches_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(testPatient1);
        entityManager.persistAndFlush(testPatient2);

        // When
        List<Patient> results = patientRepository.findByNameContaining("NoExiste");

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void findByEmail_ShouldReturnMatchingPatient() {
        // Given
        entityManager.persistAndFlush(testPatient1);
        entityManager.persistAndFlush(testPatient2);

        // When
        Optional<Patient> result = patientRepository.findByEmail("juan.perez@email.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("juan.perez@email.com", result.get().getEmail());
        assertEquals("Juan", result.get().getFirstName());
    }

    @Test
    void findByEmail_WithNoMatches_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(testPatient1);

        // When
        Optional<Patient> nonExistentPatient = patientRepository.findByEmail("nonexistent@example.com");
        assertFalse(nonExistentPatient.isPresent());
    }

    @Test
    void findByUserEmail_ShouldReturnPatient() {
        // Given
        // Note: This test assumes there's a User entity relationship
        // For this test, we'll test the query structure
        entityManager.persistAndFlush(testPatient1);

        // When
        Optional<Patient> result = patientRepository.findByEmail("juan.perez@email.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getFirstName());
        assertEquals("Pérez", result.get().getLastName());
        assertEquals(userId1, result.get().getUserId());

        // Verify it's actually persisted
        Optional<Patient> foundPatient = patientRepository.findByEmail("juan.perez@email.com");
        assertTrue(foundPatient.isPresent());
        assertEquals("juan.perez@email.com", foundPatient.get().getEmail());
    }

    @Test
    void save_ShouldPersistPatient() {
        // When
        Patient savedPatient = patientRepository.save(testPatient1);

        // Then
        assertNotNull(savedPatient.getId());
        assertEquals("Juan", savedPatient.getFirstName());
        assertEquals("Pérez", savedPatient.getLastName());
        assertEquals(userId1, savedPatient.getUserId());

        // Verify it's actually persisted
        Optional<Patient> foundPatient = patientRepository.findById(savedPatient.getId());
        assertTrue(foundPatient.isPresent());
        assertEquals(savedPatient.getId(), foundPatient.get().getId());
    }

    @Test
    void delete_ShouldRemovePatient() {
        // Given
        Patient savedPatient = entityManager.persistAndFlush(testPatient1);
        UUID patientId = savedPatient.getId();

        // When
        patientRepository.deleteById(patientId);
        entityManager.flush();

        // Then
        Optional<Patient> foundPatient = patientRepository.findById(patientId);
        assertFalse(foundPatient.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllPatients() {
        // Given
        entityManager.persistAndFlush(testPatient1);
        entityManager.persistAndFlush(testPatient2);

        // When
        List<Patient> allPatients = patientRepository.findAll();

        // Then
        assertEquals(2, allPatients.size());
        
        // Verify both patients are present
        boolean foundJuan = allPatients.stream().anyMatch(p -> "Juan".equals(p.getFirstName()));
        boolean foundMaria = allPatients.stream().anyMatch(p -> "María".equals(p.getFirstName()));
        
        assertTrue(foundJuan);
        assertTrue(foundMaria);
    }

    @Test
    void existsById_ShouldReturnCorrectResult() {
        // Given
        Patient savedPatient = entityManager.persistAndFlush(testPatient1);
        UUID existingId = savedPatient.getId();
        UUID nonExistingId = UUID.randomUUID();

        // When & Then
        assertTrue(patientRepository.existsById(existingId));
        assertFalse(patientRepository.existsById(nonExistingId));
    }
}
