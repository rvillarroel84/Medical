package com.medcal.model.request;

import com.medcal.model.entity.Appointment;
import com.medcal.model.enums.AppointmentType;
import com.medcal.model.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
    
    private UUID id;

    @NotNull(message = "El doctor es requerido")
    private UUID doctorId;

    @NotNull(message = "El paciente es requerido")
    private UUID patientId;

    @NotNull(message = "La fecha y hora de inicio son requeridas")
    @Future(message = "La fecha y hora de inicio deben ser futuras")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "La fecha y hora de fin son requeridas")
    @Future(message = "La fecha y hora de fin deben ser futuras")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    @NotNull(message = "El tipo de cita es requerido")
    private AppointmentType type;

    @Size(max = 1000, message = "Las notas no pueden exceder los 1000 caracteres")
    private String notes;

    private UUID createdBy;

    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    public Appointment toEntity() {
        return Appointment.builder()
                .id(id)
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(startTime)
                .endTime(endTime)
                .type(type)
                .status(status != null ? status : AppointmentStatus.PENDING)
                .notes(notes)
                .createdBy(createdBy)
                .build();
    }
}
