package com.medcal.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medcal.model.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    private String address;
    
    private String phone;
    
    private String email;
    
    @Column(name = "emergency_contact")
    private String emergencyContact;
    
    @JsonIgnore
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "insurance_info")
    private Map<String, Object> insuranceInfo;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
    
    // Relaciones
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;
    
    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Appointment> appointments;
    
    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ClinicalHistory> clinicalHistories;
}
