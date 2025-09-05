# MedCal - Sistema de Gestión de Citas Médicas (Java/Spring Boot)

## 🚀 Migración de Node.js/TypeScript a Java/Spring Boot

Este proyecto es una migración completa del sistema MedCal original (Node.js + Express + Prisma) a una arquitectura moderna basada en **Java + Spring Boot**.

## 📋 Stack Tecnológico

### Backend (Java)
- **Spring Boot 3.2+** - Framework principal
- **Spring Security 6** - Autenticación JWT y autorización
- **Spring Data JPA** - ORM con Hibernate
- **PostgreSQL** - Base de datos relacional
- **Maven** - Gestión de dependencias
- **Lombok** - Reducción de código boilerplate
- **MapStruct** - Mapeo entre DTOs y entidades
- **JWT** - Autenticación stateless

### Frontend (Opciones)
1. **React + TypeScript** (migración más fácil)
2. **Angular 17+** (ecosistema Java-friendly)
3. **Thymeleaf + HTMX** (full-stack Java)

## 🏗️ Arquitectura del Proyecto

```
MedCalJava/
├── src/main/java/com/medcal/
│   ├── MedCalApplication.java          # Clase principal
│   ├── controller/                     # Controladores REST
│   │   └── DoctorController.java
│   ├── service/                        # Lógica de negocio
│   │   └── DoctorService.java
│   ├── repository/                     # Acceso a datos
│   │   ├── UserRepository.java
│   │   ├── DoctorRepository.java
│   │   ├── PatientRepository.java
│   │   └── AppointmentRepository.java
│   ├── model/
│   │   ├── entity/                     # Entidades JPA
│   │   │   ├── User.java
│   │   │   ├── Doctor.java
│   │   │   ├── Patient.java
│   │   │   ├── Appointment.java
│   │   │   └── ClinicalHistory.java
│   │   ├── dto/                        # Data Transfer Objects
│   │   │   ├── DoctorDTO.java
│   │   │   └── AppointmentDTO.java
│   │   └── enums/                      # Enumeraciones
│   │       ├── Role.java
│   │       ├── Gender.java
│   │       ├── AppointmentType.java
│   │       └── AppointmentStatus.java
│   └── security/                       # Configuración de seguridad
│       ├── SecurityConfig.java
│       ├── JwtUtil.java
│       ├── JwtRequestFilter.java
│       └── JwtAuthenticationEntryPoint.java
├── src/main/resources/
│   └── application.yml                 # Configuración de la aplicación
└── pom.xml                            # Dependencias Maven
```

## 🔄 Comparación: Node.js vs Java

### Node.js/Express (Original)
```javascript
// Controlador Express
app.get('/api/doctors', async (req, res) => {
  const doctors = await prisma.doctor.findMany();
  res.json(doctors);
});
```

### Java/Spring Boot (Migrado)
```java
// Controlador Spring Boot
@GetMapping
public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
    List<DoctorDTO> doctors = doctorService.getAllDoctors();
    return ResponseEntity.ok(doctors);
}
```

## 🚀 Cómo Ejecutar

### Prerrequisitos
- Java 17+
- Maven 3.8+
- PostgreSQL 12+

### Configuración de Base de Datos
1. Crear base de datos PostgreSQL:
```sql
CREATE DATABASE medcal_db;
CREATE USER medcal_user WITH PASSWORD 'medcal_password';
GRANT ALL PRIVILEGES ON DATABASE medcal_db TO medcal_user;
```

2. Configurar variables de entorno (opcional):
```bash
export DB_USERNAME=medcal_user
export DB_PASSWORD=medcal_password
export JWT_SECRET=tu_clave_secreta_jwt_muy_larga_y_segura
```

### Ejecutar la Aplicación
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run

# O compilar y ejecutar el JAR
mvn clean package
java -jar target/medcal-backend-1.0.0.jar
```

La aplicación estará disponible en: `http://localhost:8080/api`

## 📡 API Endpoints

### Doctores
- `GET /api/doctors` - Obtener todos los doctores
- `GET /api/doctors/{id}` - Obtener doctor por ID
- `GET /api/doctors/specialization/{specialization}` - Buscar por especialización
- `GET /api/doctors/search?name={name}` - Buscar por nombre
- `POST /api/doctors` - Crear nuevo doctor
- `PUT /api/doctors/{id}` - Actualizar doctor
- `DELETE /api/doctors/{id}` - Eliminar doctor

### Autenticación (Próximamente)
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/refresh` - Renovar token

## 🔐 Seguridad

- **JWT Authentication** - Tokens seguros para autenticación
- **Role-based Access Control** - Permisos basados en roles
- **Password Encryption** - Contraseñas encriptadas con BCrypt
- **CORS Configuration** - Configuración segura para frontend

## 🧪 Testing

```bash
# Ejecutar tests unitarios
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report
```

## 📈 Ventajas de la Migración a Java

### 1. **Tipado Fuerte y Seguridad**
- Detección de errores en tiempo de compilación
- Mejor refactoring y mantenimiento
- IntelliSense más robusto

### 2. **Ecosistema Maduro**
- Spring Boot: Framework battle-tested
- Amplia documentación y comunidad
- Herramientas de desarrollo avanzadas

### 3. **Performance y Escalabilidad**
- JVM optimizada para aplicaciones empresariales
- Mejor manejo de memoria
- Threading nativo más eficiente

### 4. **Integración Empresarial**
- Mejor integración con sistemas legacy
- Soporte nativo para microservicios
- Herramientas de monitoreo empresarial

## 🔄 Próximos Pasos

1. **Completar Autenticación JWT**
2. **Implementar Controladores de Pacientes y Citas**
3. **Agregar Validaciones de Negocio**
4. **Implementar Tests Unitarios e Integración**
5. **Configurar CI/CD Pipeline**
6. **Dockerizar la Aplicación**

## 📞 Soporte

Para dudas sobre la migración o implementación, consulta la documentación de Spring Boot o contacta al equipo de desarrollo.
