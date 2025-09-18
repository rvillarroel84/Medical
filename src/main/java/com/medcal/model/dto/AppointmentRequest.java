package com.medcal.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medcal.model.enums.AppointmentStatus;
import com.medcal.model.enums.AppointmentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    
    private UUID patientId;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private String notes;
    private AppointmentType type;
    private AppointmentStatus status;
    
    // Getters and setters
    public UUID getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }
    
    public UUID getPatientId() {
        return patientId;
    }
    
    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public AppointmentType getType() {
        return type;
    }
    
    public void setType(AppointmentType type) {
        this.type = type;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
