# MedCal - Sistema de Gestión de Citas Médicas (Java/Spring Boot) con IA, Windsurf + Kilo Code + Cloude Sonnet 4 + Grok Code Fast 1 

## 🚀 Migración Completa de Node.js/TypeScript a Java/Spring Boot

Este proyecto es un sistema de atención de citas medicas, tiene una arquitectura moderna basada en **Java + Spring Boot + Thymeleaf + TailwindCSS + HTMX + Font Awesome + JavaScript Vanilla** con interfaz web completa.

## ✨ Estado Actual - Aplicación Completamente Funcional

### 🎯 Funcionalidades Implementadas
- ✅ **Sistema de Autenticación** - Login con múltiples roles
- ✅ **Gestión de Doctores** - CRUD completo con especialidades y horarios
- ✅ **Gestión de Pacientes** - Registro y administración de pacientes
- ✅ **Sistema de Citas** - Programación y gestión de citas médicas
- ✅ **Dashboard Interactivo** - Estadísticas y acciones rápidas
- ✅ **Interfaz Web Moderna** - Thymeleaf + TailwindCSS + HTMX
- ✅ **Base de Datos H2** - Configurada con datos de demostración
- ✅ **API REST** - Endpoints completos para integración
- ✅ **Seguridad JWT** - Autenticación y autorización por roles

## 📋 Stack Tecnológico

### Backend (Java)
- **Spring Boot 3.2+** - Framework principal
- **Spring Security 6** - Autenticación y autorización
- **Spring Data JPA** - ORM con Hibernate
- **H2 Database** - Base de datos en memoria (desarrollo)
- **PostgreSQL** - Base de datos relacional (producción)
- **Maven** - Gestión de dependencias
- **Lombok** - Reducción de código boilerplate
- **MapStruct** - Mapeo entre DTOs y entidades
- **JWT** - Autenticación stateless

### Frontend
- **Thymeleaf** - Motor de plantillas server-side
- **TailwindCSS** - Framework CSS moderno
- **HTMX** - Interactividad sin JavaScript complejo
- **Font Awesome** - Iconografía
- **JavaScript Vanilla** - Funcionalidades específicas

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

## 🚀 Cómo Acceder a la Aplicación

### 🌐 URL Principal
**La aplicación está disponible en:** `http://localhost:8080`

### 📋 Prerrequisitos
- Java 17+
- Maven 3.8+

### ⚡ Inicio Rápido
```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd MedCalJava

# 2. Compilar y ejecutar
mvn clean spring-boot:run
```

### 🔐 Paso a Paso para Acceder al Sistema

#### 1. **Iniciar la Aplicación**
```bash
mvn spring-boot:run
```
La aplicación se iniciará en el puerto 8080.

#### 2. **Abrir el Navegador**
Navegar a: `http://localhost:8080`

#### 3. **Página de Login**
- La aplicación redirigirá automáticamente a `/auth/login`
- Verás una interfaz moderna con el logo de MedCal

#### 4. **Autenticación Requerida**
- **TODOS los usuarios deben autenticarse** antes de acceder al sistema
- No hay acceso anónimo al dashboard o funcionalidades

#### 5. **Seleccionar Usuario de Demostración**
Haz clic en cualquiera de las cuentas de demostración para auto-completar las credenciales:

### 👥 Usuarios de Demostración

#### 🔴 **Administrador del Sistema**
- **Email:** `admin@medcal.com`
- **Contraseña:** `admin123`
- **Rol:** Administrador
- **Permisos:** Acceso completo al sistema

#### 👨‍⚕️ **Doctores**

**Doctor Principal:**
- **Email:** `doctor@medcal.com`
- **Contraseña:** `doctor123`
- **Nombre:** Dr. Juan García
- **Rol:** Doctor

**Cardiólogo:**
- **Email:** `dr.garcia@medcal.com`
- **Contraseña:** `doctor123`
- **Nombre:** Dr. Carlos García
- **Especialidad:** Cardiología
- **Teléfono:** 555-100-0001

**Pediatra:**
- **Email:** `dr.martinez@medcal.com`
- **Contraseña:** `doctor123`
- **Nombre:** Dra. Ana Martínez
- **Especialidad:** Pediatría
- **Teléfono:** 555-100-0002

#### 👩‍💼 **Personal Administrativo**
- **Email:** `recepcion@medcal.com`
- **Contraseña:** `recepcion123`
- **Nombre:** Ana Recepción
- **Rol:** Recepcionista

#### 👤 **Pacientes**

**Paciente 1:**
- **Email:** `paciente1@example.com`
- **Contraseña:** `patient123`
- **Nombre:** Juan Pérez
- **Fecha de Nacimiento:** 15/05/1990
- **Género:** Masculino
- **Teléfono:** 555-200-0001

**Paciente 2:**
- **Email:** `paciente2@example.com`
- **Contraseña:** `patient123`
- **Nombre:** María López
- **Fecha de Nacimiento:** 22/10/1985
- **Género:** Femenino
- **Teléfono:** 555-200-0002

#### 6. **Acceso al Dashboard**
Después del login exitoso, serás redirigido a `/dashboard` donde podrás:
- Ver estadísticas del sistema
- Acceder a gestión de doctores, pacientes y citas
- Utilizar acciones rápidas
- Navegar por todas las funcionalidades

### 🗄️ Configuración de Base de Datos
La aplicación usa **H2 Database** en memoria con datos precargados:
- **URL H2 Console:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:file:./data/medcaldb`
- **Usuario:** `sa`
- **Contraseña:** (vacía)

### 🔧 Configuración Avanzada (Opcional)

#### Variables de Entorno
```bash
export JWT_SECRET=tu_clave_secreta_jwt_muy_larga_y_segura
export DB_USERNAME=sa
export DB_PASSWORD=
```

#### Configuración PostgreSQL (Producción)
```sql
CREATE DATABASE medcal_db;
CREATE USER medcal_user WITH PASSWORD 'medcal_password';
GRANT ALL PRIVILEGES ON DATABASE medcal_db TO medcal_user;
```

### 📦 Compilación y Despliegue
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar tests
mvn test

# Crear JAR ejecutable
mvn clean package

# Ejecutar JAR
java -jar target/medcal-backend-1.0.0.jar
```

## 🌐 Rutas Web Principales

### 🔐 Autenticación
- `GET /` - Redirige al dashboard (requiere autenticación)
- `GET /auth/login` - Página de inicio de sesión
- `POST /auth/login` - Procesar login
- `GET /auth/logout` - Cerrar sesión

### 📊 Dashboard y Navegación
- `GET /dashboard` - Panel principal con estadísticas
- Acceso a todas las funcionalidades desde el menú lateral

### 👨‍⚕️ Gestión de Doctores
- `GET /doctors` - Lista de doctores con búsqueda
- `GET /doctors/new` - Formulario nuevo doctor
- `POST /doctors` - Crear doctor
- `GET /doctors/{id}/edit` - Editar doctor
- `POST /doctors/{id}/delete` - Eliminar doctor

### 👤 Gestión de Pacientes
- `GET /patients` - Lista de pacientes con búsqueda
- `GET /patients/new` - Formulario nuevo paciente
- `POST /patients` - Crear paciente
- `GET /patients/{id}/edit` - Editar paciente
- `POST /patients/{id}/delete` - Eliminar paciente

### 📅 Gestión de Citas
- `GET /appointments` - Lista de citas
- `GET /appointments/new` - Formulario nueva cita
- `POST /appointments` - Crear cita
- `GET /appointments/{id}/edit` - Editar cita
- `POST /appointments/{id}/delete` - Eliminar cita
- `GET /appointments/calendar` - Vista de calendario

## 📡 API REST Endpoints

### 🔐 Autenticación API
- `POST /api/auth/login` - Login API (JWT)
- `POST /api/auth/refresh` - Renovar token

### 👨‍⚕️ API Doctores
- `GET /api/doctors` - Obtener todos los doctores
- `GET /api/doctors/{id}` - Obtener doctor por ID
- `GET /api/doctors/specialization/{specialization}` - Buscar por especialización
- `GET /api/doctors/search?name={name}` - Buscar por nombre
- `POST /api/doctors` - Crear nuevo doctor
- `PUT /api/doctors/{id}` - Actualizar doctor
- `DELETE /api/doctors/{id}` - Eliminar doctor

### 👤 API Pacientes
- `GET /api/patients` - Obtener todos los pacientes
- `GET /api/patients/{id}` - Obtener paciente por ID
- `POST /api/patients` - Crear nuevo paciente
- `PUT /api/patients/{id}` - Actualizar paciente
- `DELETE /api/patients/{id}` - Eliminar paciente

### 📅 API Citas
- `GET /api/appointments` - Obtener todas las citas
- `GET /api/appointments/{id}` - Obtener cita por ID
- `POST /api/appointments` - Crear nueva cita
- `PUT /api/appointments/{id}` - Actualizar cita
- `DELETE /api/appointments/{id}` - Eliminar cita

## 🔐 Seguridad y Autenticación

### 🛡️ Características de Seguridad
- **Autenticación Obligatoria** - Todos los endpoints requieren login
- **JWT Authentication** - Tokens seguros para API
- **Role-based Access Control** - Permisos basados en roles
- **Password Encryption** - Contraseñas encriptadas con BCrypt
- **Session Management** - Gestión segura de sesiones web
- **CORS Configuration** - Configuración segura para frontend

### 🔑 Roles del Sistema
- **ROLE_ADMIN** - Acceso completo al sistema
- **ROLE_DOCTOR** - Gestión de citas y pacientes asignados
- **ROLE_PATIENT** - Acceso a información personal y citas
- **ROLE_RECEPTIONIST** - Gestión de citas y pacientes

## 🧪 Testing

```bash
# Ejecutar tests unitarios
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report
```

## 📈 Ventajas de la Migración a Java/Spring Boot

### 1. **Arquitectura Robusta y Escalable**
- **Tipado Fuerte** - Detección de errores en tiempo de compilación
- **Inyección de Dependencias** - Código más mantenible y testeable
- **Configuración Declarativa** - Menos código boilerplate
- **Transacciones Automáticas** - Gestión transparente de BD

### 2. **Ecosistema Empresarial Maduro**
- **Spring Boot** - Framework battle-tested con auto-configuración
- **Spring Security** - Seguridad robusta out-of-the-box
- **Spring Data JPA** - ORM potente con consultas automáticas
- **Amplia Documentación** - Comunidad activa y recursos abundantes

### 3. **Performance y Escalabilidad Superior**
- **JVM Optimizada** - Rendimiento superior para aplicaciones empresariales
- **Gestión de Memoria** - Garbage Collection automático y eficiente
- **Threading Nativo** - Manejo concurrente más eficiente
- **Connection Pooling** - Optimización automática de BD

### 4. **Herramientas de Desarrollo Avanzadas**
- **IntelliSense Robusto** - IDE con autocompletado inteligente
- **Debugging Avanzado** - Herramientas de depuración potentes
- **Refactoring Seguro** - Cambios automáticos sin romper código
- **Testing Integrado** - Framework de pruebas completo

### 5. **Integración y Despliegue Empresarial**
- **Microservicios Ready** - Arquitectura preparada para escalar
- **Monitoreo Nativo** - Actuator endpoints para métricas
- **Docker Support** - Contenedorización sencilla
- **Cloud Native** - Despliegue en AWS, Azure, GCP

## 🎯 Funcionalidades Destacadas

### ✨ Interfaz de Usuario
- **Diseño Responsivo** - Compatible con móviles y tablets
- **Tema Moderno** - TailwindCSS con paleta médica
- **Navegación Intuitiva** - Menú lateral colapsible
- **Feedback Visual** - Alertas y confirmaciones
- **Búsqueda en Tiempo Real** - Filtros dinámicos

### 📊 Dashboard Interactivo
- **Estadísticas en Tiempo Real** - Contadores dinámicos
- **Acciones Rápidas** - Botones de acceso directo
- **Doctores Recientes** - Lista de últimos registros
- **Calendario de Citas** - Vista próximas citas

### 🔄 Gestión Completa
- **CRUD Completo** - Crear, leer, actualizar, eliminar
- **Validaciones** - Formularios con validación client/server
- **Búsqueda Avanzada** - Filtros por múltiples criterios
- **Paginación** - Manejo eficiente de grandes datasets

## 🧪 Testing y Calidad

### 🔬 Tests Implementados
```bash
# Ejecutar tests unitarios
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

### 📈 Cobertura de Tests
- **Controladores** - Tests de integración web
- **Servicios** - Tests unitarios de lógica de negocio
- **Repositorios** - Tests de acceso a datos
- **Seguridad** - Tests de autenticación y autorización

## 🔄 Roadmap y Mejoras Futuras

### 🚀 Próximas Funcionalidades
1. **Notificaciones Push** - Recordatorios de citas
2. **Reportes Avanzados** - Gráficos y estadísticas
3. **Integración de Calendario** - Google Calendar, Outlook
4. **Telemedicina** - Videollamadas integradas
5. **Historial Clínico** - Gestión completa de expedientes
6. **Facturación** - Sistema de cobros y pagos

### 🔧 Mejoras Técnicas
1. **Dockerización** - Contenedores para despliegue
2. **CI/CD Pipeline** - Automatización de despliegues
3. **Monitoreo** - Métricas y logging avanzado
4. **Cache Redis** - Optimización de rendimiento
5. **Microservicios** - Arquitectura distribuida
6. **API Gateway** - Gestión centralizada de APIs

## 📞 Soporte

Para dudas sobre la migración o implementación, consulta la documentación de Spring Boot o contacta al equipo de desarrollo.
