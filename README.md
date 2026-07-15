# Task Manager API

API REST para gestión de tareas personales con autenticación JWT.

**Stack:** Java 17 · Spring Boot 4.0.7 · PostgreSQL 16 · Flyway · Spring Security · JJWT 0.12.3

---

## Decisiones técnicas y desviaciones del backlog

### Spring Boot 4.0.7 en vez de 3.3.x
El backlog especifica Spring Boot 3.3. Al momento de iniciar el proyecto
(julio 2026), start.spring.io solo ofrecía versiones 4.x estables.
Se usó 4.0.7 — la API es prácticamente idéntica pero requirió ajustes:
- `@Where` deprecado → reemplazado por `@SQLRestriction` (Hibernate 6.x)
- `DaoAuthenticationProvider()` sin constructor vacío → se pasa `UserDetailsService` directo
- `@NonNull` de `org.springframework.lang` deprecated en Spring 7.0

### spring-dotenv para variables de entorno
El backlog no especifica cómo gestionar variables de entorno locales.
Se usó `springboot4-dotenv 5.0.1` (vía BOM `spring-dotenv-bom`) para
leer el archivo `.env` automáticamente. La versión `spring-dotenv 4.0.0`
es incompatible con Spring Boot 4.x.

### Dependencia circular en SecurityConfig
`SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig`
Solución: `JwtAuthenticationFilter` se recibe como parámetro del método
`securityFilterChain()` en vez de en el constructor, rompiendo el ciclo.

### JWT usa email como subject
El backlog no especifica qué campo usar como identificador en el token.
Se decidió usar el email porque es único, inmutable, y es el campo
usado para login — evita inconsistencias entre login y validación del token.

### UserRepository — métodos adicionales
Se agregaron `findByUsername` y `existsByUsername` durante el desarrollo
para explorar autenticación por username. No se usan actualmente pero
se mantienen para uso futuro.

### CORS habilitado para frontend
Se configuró CORS en `SecurityConfig` para permitir requests desde
`http://localhost:5173` (Vite dev server). Métodos permitidos:
GET, POST, PUT, DELETE, PATCH. Para producción se debe actualizar
con la URL de Vercel o el dominio del frontend.
---

## Configuración local

### Requisitos
- Java 17
- PostgreSQL 16
- Maven

### Variables de entorno
Crear archivo `.env` en la raíz del proyecto:
JWT_SECRET=tu_secret_generado_con_crypto
DB_PASSWORD=tu_password_postgresql

Generar JWT_SECRET seguro:
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"

### Base de datos
CREATE DATABASE task_manager_db;

### Ejecutar
./mvnw spring-boot:run

La app levanta en http://localhost:8080
Flyway crea las tablas automáticamente al primer arranque.

---

## Endpoints Sprint 1 — Auth

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| POST | /api/auth/register | No | Registro de usuario |
| POST | /api/auth/login | No | Login, retorna tokens |
| POST | /api/auth/refresh | Bearer | Renovar access token |
| GET | /api/auth/profile | Bearer | Perfil del usuario |

### Ejemplo registro
curl -X POST http://localhost:8080/api/auth/register 
-H "Content-Type: application/json" 
-d '{"username":"katerina","email":"katerina@test.com","password":"123456"}'

### Ejemplo login
curl -X POST http://localhost:8080/api/auth/login 
-H "Content-Type: application/json" 
-d '{"email":"katerina@test.com","password":"123456"}'

---

## Estado del proyecto

### Sprint 1 — Backend Auth completado
- [x] Setup Spring Boot + dependencias
- [x] Entidades JPA (User, Task) con auditoría y soft delete
- [x] Flyway V1 — migración inicial
- [x] Repositories (UserRepository, TaskRepository)
- [x] DTOs (Register, Login, AuthResponse, UserProfile, Task)
- [x] JwtService — generación y validación de tokens
- [x] JwtAuthenticationFilter — interceptor de requests
- [x] SecurityConfig — configuración Spring Security
- [x] AuthController — register, login, refresh, profile
- [x] GlobalExceptionHandler

### Sprint 1 — Listo!
- [x] QA Tests (register, login, refresh, profile)
- [x] Frontend UX (login, registro, dashboard, modal tarea)

### Sprint 2 — Próximo
- [ ] CRUD completo de tareas
- [ ] Soft delete
- [ ] Validaciones

---

## Estructura del proyecto
```text
src/main/java/com/katerinacampos/task_manager/
src/
├── main/
│ ├── java/
│ │ └── com/
│ │ └── katerinacampos.task_manager/
│ │ ├── controller/
│ │ │ └── AuthController.java
│ │ ├── dto/
│ │ │ ├── AuthResponse.java
│ │ │ ├── LoginRequest.java
│ │ │ ├── RegisterRequest.java
│ │ │ ├── TaskRequest.java
│ │ │ ├── TaskResponse.java
│ │ │ └── UserProfileResponse.java
│ │ ├── exception/
│ │ │ └── GlobalExceptionHandler.java
│ │ ├── model/
│ │ │ ├── Task.java
│ │ │ ├── TaskStatus.java
│ │ │ └── User.java
│ │ ├── repository/
│ │ │ ├── TaskRepository.java
│ │ │ └── UserRepository.java
│ │ └── security/
│ │ ├── JwtAuthenticationFilter.java
│ │ ├── JwtService.java
│ │ ├── SecurityConfig.java
│ │ ├── TaskManagerApplication.java
│ │ └── taskmanager.api
│ └── resources/
└── test/
└── java/
└── com.katerinacampos.task_manager/
└── controller/
│       └── AuthControllerTest.java
└── TaskManagerApplicationTests.java
```
## Testing

### Ejecutar tests

### Tests incluidos — Sprint 1
Los tests de integración usan `@SpringBootTest` con MockMvc y se ejecutan contra la base de datos local.

**Registro:**
- Registro exitoso retorna accessToken, refreshToken y tokenType Bearer
- Email duplicado retorna 400
- Password menor a 6 caracteres retorna 400
- Email con formato inválido retorna 400
- Username vacío retorna 400

**Login:**
- Login exitoso retorna accessToken y tokenType Bearer
- Credenciales incorrectas retorna 4xx

**Profile:**
- Sin token retorna 401
- Con token válido retorna email y username del usuario

### Notas técnicas
- `@AutoConfigureMockMvc` no existe en Spring Boot 4.x — se construye MockMvc manualmente con `MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()`
- Limpieza de BD antes de cada test con `userRepository.findByEmail().ifPresent(delete)`
- Tests corren contra PostgreSQL local, no H2 en memoria