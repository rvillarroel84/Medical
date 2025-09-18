package com.medcal.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String specialization;
    private String phone;
    private String email;
    private String photoUrl;
    private String bio;
    private Double rating;
    private Integer experience; // years of experience
    private String location;
    private Map<String, Object> workingHours;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Computed properties for UI
    @JsonIgnore
    public String getName() {
        return String.format("%s %s", firstName, lastName).trim();
    }
    
    @JsonIgnore
    public String getSpecialty() {
        return specialization;
    }
}
