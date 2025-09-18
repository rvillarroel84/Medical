-- Insertar usuario administrador
-- Contraseña: admin123
INSERT INTO users (id, email, password, first_name, last_name, role_id, enabled) VALUES
('00000000-0000-0000-0000-000000000000', 'admin@medcal.com', '$2a$10$am9PIrBMY13a3GGT2BsX4eDx3kOYyoy6Uf5KGZYeILUo/U1pAl7vu', 'Admin', 'Sistema', '11111111-1111-1111-1111-111111111111', true);

-- Insertar usuarios de demostración adicionales
-- Contraseñas: doctor123, recepcion123
INSERT INTO users (id, email, password, first_name, last_name, role_id, enabled) VALUES
('50000000-0000-0000-0000-000000000001', 'doctor@medcal.com', '$2a$10$40Fvzr76HMPdANSHphUfQecul.sNUSxfZaLTgAm4pKtbUq5zgyNo6', 'Dr. Juan', 'García', '22222222-2222-2222-2222-222222222222', true),
('50000000-0000-0000-0000-000000000002', 'recepcion@medcal.com', '$2a$10$sY5x3v6L4CdEcBkKPqb0O.uwWU7oPDIYNsoZSH4kLJ/5aP5Dtoj9.', 'Ana', 'Recepción', '44444444-4444-4444-4444-444444444444', true);

-- Insertar doctores de prueba
-- Contraseña: doctor123
INSERT INTO users (id, email, password, first_name, last_name, role_id, enabled) VALUES
('10000000-0000-0000-0000-000000000001', 'dr.garcia@medcal.com', '$2a$10$40Fvzr76HMPdANSHphUfQecul.sNUSxfZaLTgAm4pKtbUq5zgyNo6', 'Carlos', 'García', '22222222-2222-2222-2222-222222222222', true),
('10000000-0000-0000-0000-000000000002', 'dr.martinez@medcal.com', '$2a$10$40Fvzr76HMPdANSHphUfQecul.sNUSxfZaLTgAm4pKtbUq5zgyNo6', 'Ana', 'Martínez', '22222222-2222-2222-2222-222222222222', true);

-- Insertar información de los doctores
INSERT INTO doctors (id, user_id, first_name, last_name, specialization, phone, license_number) VALUES
('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Carlos', 'García', 'Cardiología', '555-100-0001', 'LIC-001'),
('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'Ana', 'Martínez', 'Pediatría', '555-100-0002', 'LIC-002');

-- Insertar pacientes de prueba
-- Contraseña: patient123
INSERT INTO users (id, email, password, first_name, last_name, role_id, enabled) VALUES
('30000000-0000-0000-0000-000000000001', 'paciente1@example.com', '$2a$10$A47ezraXuKuwxLOidCVV/O2vfp6UV2rF4cjbYI0Ln7xRnn7SF3tSe', 'Juan', 'Pérez', '33333333-3333-3333-3333-333333333333', true),
('30000000-0000-0000-0000-000000000002', 'paciente2@example.com', '$2a$10$A47ezraXuKuwxLOidCVV/O2vfp6UV2rF4cjbYI0Ln7xRnn7SF3tSe', 'María', 'López', '33333333-3333-3333-3333-333333333333', true);

-- Insertar información de los pacientes
INSERT INTO patients (id, user_id, first_name, last_name, date_of_birth, gender, address, phone) VALUES
('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Juan', 'Pérez', '1990-05-15', 'MALE', 'Av. Principal 123, Ciudad', '555-200-0001'),
('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'María', 'López', '1985-10-22', 'FEMALE', 'Calle Secundaria 456, Ciudad', '555-200-0002');
