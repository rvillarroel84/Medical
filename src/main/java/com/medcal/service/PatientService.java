package com.medcal.service;

import com.medcal.model.dto.PatientDTO;
import com.medcal.model.entity.Patient;
import com.medcal.model.entity.User;
import com.medcal.model.enums.Role;
import com.medcal.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {
    
    private final PatientRepository patientRepository;
    private final UserService userService;
    
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<PatientDTO> getPatientById(UUID id) {
        return patientRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public Optional<PatientDTO> getPatientByUserId(UUID userId) {
        return patientRepository.findByUserId(userId)
                .map(this::convertToDTO);
    }
    
    public Optional<PatientDTO> getPatientByEmail(String email) {
        return patientRepository.findByUserEmail(email)
                .map(this::convertToDTO);
    }
    
    public List<PatientDTO> searchPatientsByName(String name) {
        return patientRepository.findByNameContaining(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PatientDTO createPatient(Patient patient) {
        try {
            // Validaciones básicas
            if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre es requerido");
            }
            if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
                throw new IllegalArgumentException("El apellido es requerido");
            }
            if (patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("El email es requerido");
            }

            // Verificar si ya existe un paciente con el mismo email
            Optional<Patient> existingPatient = patientRepository.findByEmail(patient.getEmail());
            if (existingPatient.isPresent()) {
                throw new IllegalArgumentException("Ya existe un paciente con este email");
            }

            // Crear usuario primero
            User user = userService.createUser(
                patient.getEmail(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhone(),
                Role.PATIENT
            );
            
            // Asignar el ID del usuario al paciente
            patient.setUserId(user.getId());
            
            // Guardar el paciente
            Patient savedPatient = patientRepository.save(patient);
            return convertToDTO(savedPatient);
            
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error al crear paciente: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar la excepción para manejarla en el controlador
        }
    }
    
    @Transactional
    public Optional<PatientDTO> updatePatient(UUID id, Patient patientDetails) {
        return patientRepository.findById(id)
                .map(patient -> {
                    // Actualizar campos
                    patient.setFirstName(patientDetails.getFirstName());
                    patient.setLastName(patientDetails.getLastName());
                    patient.setDateOfBirth(patientDetails.getDateOfBirth());
                    patient.setGender(patientDetails.getGender());
                    patient.setAddress(patientDetails.getAddress());
                    patient.setPhone(patientDetails.getPhone());
                    patient.setEmail(patientDetails.getEmail());
                    patient.setEmergencyContact(patientDetails.getEmergencyContact());
                    patient.setInsuranceInfo(patientDetails.getInsuranceInfo());
                    
                    // Validar antes de guardar
                    validatePatient(patient);
                    
                    return convertToDTO(patientRepository.save(patient));
                });
    }
    
    @Transactional
    public boolean deletePatient(UUID id) {
        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private void validatePatient(Patient patient) {
        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        
        if (patient.getUserId() == null) {
            throw new IllegalArgumentException("El ID de usuario es obligatorio");
        }
        
        // Validar formato de email si está presente
        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
            if (!isValidEmail(patient.getEmail())) {
                throw new IllegalArgumentException("El formato del email no es válido");
            }
        }
        
        // Validar formato de teléfono si está presente
        if (patient.getPhone() != null && !patient.getPhone().trim().isEmpty()) {
            if (!isValidPhone(patient.getPhone())) {
                throw new IllegalArgumentException("El formato del teléfono no es válido");
            }
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private boolean isValidPhone(String phone) {
        // Acepta formatos como: +1234567890, 123-456-7890, (123) 456-7890, etc.
        return phone.matches("^[\\+]?[1-9]?[0-9]{7,15}$") || 
               phone.matches("^[\\+]?[(]?[\\d\\s\\-\\(\\)]{10,}$");
    }
    
    private PatientDTO convertToDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .userId(patient.getUserId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .emergencyContact(patient.getEmergencyContact())
                .insuranceInfo(patient.getInsuranceInfo())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
