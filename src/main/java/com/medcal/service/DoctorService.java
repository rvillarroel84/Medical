package com.medcal.service;

import com.medcal.model.dto.DoctorDTO;
import com.medcal.model.entity.Doctor;
import com.medcal.model.entity.User;
import com.medcal.model.enums.Role;
import com.medcal.repository.DoctorRepository;
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
public class DoctorService {
    
    private final DoctorRepository doctorRepository;
    private final UserService userService;
    
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
