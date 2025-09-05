package com.medcal.repository;

import com.medcal.model.entity.Appointment;
import com.medcal.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    
    List<Appointment> findByDoctorId(UUID doctorId);
    
    List<Appointment> findByPatientId(UUID patientId);
    
    List<Appointment> findByStatus(AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.startTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByDoctorIdAndDateRange(
        @Param("doctorId") UUID doctorId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId AND a.startTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientIdAndDateRange(
        @Param("patientId") UUID patientId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND " +
           "((a.startTime BETWEEN :startTime AND :endTime) OR " +
           "(a.endTime BETWEEN :startTime AND :endTime) OR " +
           "(a.startTime <= :startTime AND a.endTime >= :endTime)) AND " +
           "a.status = 'SCHEDULED'")
    List<Appointment> findConflictingAppointments(
        @Param("doctorId") UUID doctorId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
