package com.medcal.repository;

import com.medcal.model.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    
    Optional<Doctor> findByUserId(UUID userId);
    
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    
    List<Doctor> findBySpecialization(String specialization);
    
    @Query("SELECT d FROM Doctor d WHERE d.firstName LIKE %:name% OR d.lastName LIKE %:name%")
    List<Doctor> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT d FROM Doctor d JOIN d.user u WHERE u.email = :email")
    Optional<Doctor> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT d FROM Doctor d WHERE d.active = true")
    List<Doctor> findAllByActiveTrue();
    
    @Query("SELECT d FROM Doctor d WHERE d.active = true AND d.specialization = :specialty")
    List<Doctor> findActiveBySpecialization(@Param("specialty") String specialty);
    
    @Query("SELECT d FROM Doctor d WHERE d.active = true AND d.id IN :ids")
    List<Doctor> findActiveByIds(@Param("ids") List<UUID> ids);
    
    @Query("SELECT d FROM Doctor d WHERE d.active = true AND d.id = :id")
    Optional<Doctor> findActiveById(@Param("id") UUID id);
    
    @Query("SELECT d FROM Doctor d WHERE d.active = true AND d.id = ?1")
    Optional<Doctor> findByIdAndActiveTrue(UUID id);
}
