package com.medcal.repository;

import com.medcal.model.entity.Patient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Cache(usage = CacheConcurrencyStrategy.NONE)
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    
    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.user WHERE p.userId = :userId")
    Optional<Patient> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT p FROM Patient p WHERE p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
    List<Patient> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Patient p JOIN p.user u WHERE u.email = :email")
    Optional<Patient> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT p FROM Patient p WHERE p.email = :email")
    Optional<Patient> findByEmail(@Param("email") String email);
}
