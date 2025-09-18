package com.medcal.model.enums;

public enum AppointmentStatus {
    PENDING,    // Initial status when appointment is created
    SCHEDULED,  // Doctor has confirmed the appointment
    COMPLETED,  // Appointment was completed
    CANCELLED,  // Appointment was cancelled
    NO_SHOW     // Patient didn't show up
}
