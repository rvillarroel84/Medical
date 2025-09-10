package com.medcal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcal.model.dto.AppointmentDTO;
import com.medcal.model.entity.Appointment;
import com.medcal.model.enums.AppointmentStatus;
import com.medcal.model.enums.AppointmentType;
import com.medcal.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private AppointmentDTO testAppointmentDTO;
    private Appointment testAppointment;
    private UUID appointmentId;
    private UUID doctorId;
    private UUID patientId;
    private UUID createdBy;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        createdBy = UUID.randomUUID();

        testAppointmentDTO = AppointmentDTO.builder()
                .id(appointmentId)
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0))
                .type(AppointmentType.CONSULTATION)
                .status(AppointmentStatus.SCHEDULED)
                .notes("Consulta de rutina")
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testAppointment = Appointment.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0))
                .type(AppointmentType.CONSULTATION)
                .status(AppointmentStatus.SCHEDULED)
                .notes("Consulta de rutina")
                .createdBy(createdBy)
                .build();
    }

    @Test
    void getAllAppointments_ShouldReturnAllAppointments() throws Exception {
        // Given
        List<AppointmentDTO> appointments = Arrays.asList(testAppointmentDTO);
        when(appointmentService.getAllAppointments()).thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(appointmentId.toString()))
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()));

        verify(appointmentService).getAllAppointments();
    }

    @Test
    void getAppointmentById_WhenExists_ShouldReturnAppointment() throws Exception {
        // Given
        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(Optional.of(testAppointmentDTO));

        // When & Then
        mockMvc.perform(get("/api/appointments/{id}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.type").value("CONSULTATION"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(appointmentService).getAppointmentById(appointmentId);
    }

    @Test
    void getAppointmentById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/appointments/{id}", appointmentId))
                .andExpect(status().isNotFound());

        verify(appointmentService).getAppointmentById(appointmentId);
    }

    @Test
    void getAppointmentsByDoctorId_ShouldReturnDoctorAppointments() throws Exception {
        // Given
        List<AppointmentDTO> appointments = Arrays.asList(testAppointmentDTO);
        when(appointmentService.getAppointmentsByDoctorId(doctorId)).thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/api/appointments/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()));

        verify(appointmentService).getAppointmentsByDoctorId(doctorId);
    }

    @Test
    void getAppointmentsByPatientId_ShouldReturnPatientAppointments() throws Exception {
        // Given
        List<AppointmentDTO> appointments = Arrays.asList(testAppointmentDTO);
        when(appointmentService.getAppointmentsByPatientId(patientId)).thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/api/appointments/patient/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()));

        verify(appointmentService).getAppointmentsByPatientId(patientId);
    }

    @Test
    void getAppointmentsByStatus_ShouldReturnAppointmentsByStatus() throws Exception {
        // Given
        List<AppointmentDTO> appointments = Arrays.asList(testAppointmentDTO);
        when(appointmentService.getAppointmentsByStatus(AppointmentStatus.SCHEDULED)).thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/api/appointments/status/{status}", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));

        verify(appointmentService).getAppointmentsByStatus(AppointmentStatus.SCHEDULED);
    }

    @Test
    void checkDoctorAvailability_WhenAvailable_ShouldReturnTrue() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);
        when(appointmentService.isDoctorAvailable(doctorId, startTime, endTime)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/appointments/doctor/{doctorId}/availability", doctorId)
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(appointmentService).isDoctorAvailable(doctorId, startTime, endTime);
    }

    @Test
    void createAppointment_WithValidData_ShouldCreateAppointment() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(Appointment.class))).thenReturn(testAppointmentDTO);

        // When & Then
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.type").value("CONSULTATION"));

        verify(appointmentService).createAppointment(any(Appointment.class));
    }

    @Test
    void createAppointment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(Appointment.class)))
                .thenThrow(new IllegalArgumentException("El doctor especificado no existe"));

        // When & Then
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El doctor especificado no existe"));

        verify(appointmentService).createAppointment(any(Appointment.class));
    }

    @Test
    void createAppointment_WithConflict_ShouldReturnBadRequest() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(Appointment.class)))
                .thenThrow(new IllegalArgumentException("El doctor ya tiene una cita programada en ese horario"));

        // When & Then
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El doctor ya tiene una cita programada en ese horario"));

        verify(appointmentService).createAppointment(any(Appointment.class));
    }

    @Test
    void updateAppointment_WhenExists_ShouldUpdateAppointment() throws Exception {
        // Given
        AppointmentDTO updatedAppointmentDTO = AppointmentDTO.builder()
                .id(appointmentId)
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0))
                .type(AppointmentType.FOLLOWUP)
                .status(AppointmentStatus.SCHEDULED)
                .notes("Cita de seguimiento")
                .createdBy(createdBy)
                .build();

        when(appointmentService.updateAppointment(eq(appointmentId), any(Appointment.class)))
                .thenReturn(Optional.of(updatedAppointmentDTO));

        // When & Then
        mockMvc.perform(put("/api/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.type").value("FOLLOW_UP"));

        verify(appointmentService).updateAppointment(eq(appointmentId), any(Appointment.class));
    }

    @Test
    void updateAppointment_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(appointmentService.updateAppointment(eq(appointmentId), any(Appointment.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isNotFound());

        verify(appointmentService).updateAppointment(eq(appointmentId), any(Appointment.class));
    }

    @Test
    void updateAppointmentStatus_WhenExists_ShouldUpdateStatus() throws Exception {
        // Given
        AppointmentDTO updatedAppointmentDTO = AppointmentDTO.builder()
                .id(appointmentId)
                .doctorId(doctorId)
                .patientId(patientId)
                .startTime(testAppointmentDTO.getStartTime())
                .endTime(testAppointmentDTO.getEndTime())
                .type(AppointmentType.CONSULTATION)
                .status(AppointmentStatus.COMPLETED)
                .notes("Consulta completada")
                .createdBy(createdBy)
                .build();

        when(appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED))
                .thenReturn(Optional.of(updatedAppointmentDTO));

        // When & Then
        mockMvc.perform(patch("/api/appointments/{id}/status", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"COMPLETED\""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(appointmentService).updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);
    }

    @Test
    void cancelAppointment_WhenExists_ShouldCancelAppointment() throws Exception {
        // Given
        when(appointmentService.cancelAppointment(appointmentId)).thenReturn(true);

        // When & Then
        mockMvc.perform(patch("/api/appointments/{id}/cancel", appointmentId))
                .andExpect(status().isOk());

        verify(appointmentService).cancelAppointment(appointmentId);
    }

    @Test
    void cancelAppointment_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(appointmentService.cancelAppointment(appointmentId)).thenReturn(false);

        // When & Then
        mockMvc.perform(patch("/api/appointments/{id}/cancel", appointmentId))
                .andExpect(status().isNotFound());

        verify(appointmentService).cancelAppointment(appointmentId);
    }

    @Test
    void deleteAppointment_WhenExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(appointmentService.deleteAppointment(appointmentId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/appointments/{id}", appointmentId))
                .andExpect(status().isNoContent());

        verify(appointmentService).deleteAppointment(appointmentId);
    }

    @Test
    void deleteAppointment_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(appointmentService.deleteAppointment(appointmentId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/appointments/{id}", appointmentId))
                .andExpect(status().isNotFound());

        verify(appointmentService).deleteAppointment(appointmentId);
    }

    @Test
    void getAppointmentsByDoctorAndDateRange_ShouldReturnAppointments() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime endDate = startDate.plusDays(1);
        List<AppointmentDTO> appointments = Arrays.asList(testAppointmentDTO);
        
        when(appointmentService.getAppointmentsByDoctorAndDateRange(doctorId, startDate, endDate))
                .thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/api/appointments/doctor/{doctorId}/range", doctorId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()));

        verify(appointmentService).getAppointmentsByDoctorAndDateRange(doctorId, startDate, endDate);
    }

    @Test
    void getAppointmentsByPatientAndDateRange_ShouldReturnAppointments() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime endDate = startDate.plusDays(1);
        List<AppointmentDTO> appointments = Arrays.asList(testAppointmentDTO);
        
        when(appointmentService.getAppointmentsByPatientAndDateRange(patientId, startDate, endDate))
                .thenReturn(appointments);

        // When & Then
        mockMvc.perform(get("/api/appointments/patient/{patientId}/range", patientId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()));

        verify(appointmentService).getAppointmentsByPatientAndDateRange(patientId, startDate, endDate);
    }
}
